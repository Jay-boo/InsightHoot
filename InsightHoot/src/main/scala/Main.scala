import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import com.johnsnowlabs.nlp.annotators.Tokenizer
import com.johnsnowlabs.nlp.annotators.pos.perceptron.PerceptronModel
import com.johnsnowlabs.nlp.base.DocumentAssembler
import models.{LocalDatabaseConfig, MainDatabaseConfig}
import org.apache.logging.log4j.scala.Logging
import org.apache.spark.ml.{Pipeline, PipelineModel};



object Main extends SparkMachine with Logging {
  import spark.implicits._

  def getPayload(df:DataFrame): DataFrame = {
    val schema = new StructType()
      .add("payload", StringType)

    val payloadDF = df
      .selectExpr("CAST(value AS STRING)")
      .select(from_json(col("value"), schema).as("data"))
      .select("data.payload").as("payload")


    val payloadSchema = StructType(Seq(
      StructField("feed", StructType(Seq(
        StructField("title", StringType, nullable = true),
        StructField("url", StringType, nullable = true)
      )), nullable = true),
      StructField("title", StringType, nullable = true),
      StructField("id", StringType, nullable = true),
      StructField("link", StringType, nullable = true),
      StructField("content", StringType, nullable = true),
      StructField("author", StringType, nullable = true),
      StructField("date", TimestampType, nullable = true)
    ))

    val parsedDF = payloadDF.withColumn("parsed_payload", from_json(col("payload"), payloadSchema))
    val payloadParsedDF=parsedDF.select(
      $"parsed_payload.feed.title".alias("feed_title"),
      $"parsed_payload.feed.url".alias("feed_url"),
      $"parsed_payload.title".alias("title"),
      $"parsed_payload.id".alias("id"),
      $"parsed_payload.link".alias("link"),
      $"parsed_payload.content".alias("content"),
      $"parsed_payload.author".alias("author"),
      $"parsed_payload.date".alias("date")
    )
    payloadParsedDF

  }

  def getRelevantTokens(model: PipelineModel,titleDF:DataFrame): DataFrame = {
    val res=model.transform(titleDF)
    val explodedDF=res.withColumn("explodedPosTagging",explode($"posTagging")).drop("token","document","posTagging")
    val pertinentDF=explodedDF.withColumn("token",$"explodedPosTagging.metadata")
      .filter($"explodedPosTagging.result".startsWith("NN"))
      .withColumn("token",expr("token['word']"))
    pertinentDF.groupBy("title","feed_title", "date", "feed_url", "link","content").agg(collect_list("token").as("relevant_tokens"))
  }




  def main(args: Array[String]): Unit = {
    if (args.length != 1 || (args(0) != "local" && args(0) != "k8s")) {
      logger.error("Usage: Main <mode>")
      logger.info("Modes: local | k8s")
      System.exit(1)
    }
    val mode: Boolean = args(0) == "k8s"
    val kafkaParams = Map[String, String](
      "kafka.bootstrap.servers" ->( if (mode) "kafka-service.default.svc.cluster.local:9092" else  "localhost:9092"),
      "subscribePattern" -> "topic-*",
      "startingOffsets"-> "earliest",
      "endingOffsets"-> "latest"
    )
    println(s"Mode : $mode")
    println(kafkaParams)

    val df=spark
      .read
      .format("kafka")
      .options(kafkaParams)
      .load()
    val titleDF=getPayload(df).select("title", "feed_title","date", "feed_url", "link","content")
    val documentAssembler= new DocumentAssembler()
      .setInputCol("title")
      .setOutputCol("document")
    val  tokenizer=new Tokenizer()
      .setInputCols("document").setOutputCol("token")

    val posTagger:PerceptronModel=try{
      PerceptronModel.load("/tmp/jars/pos_anc_en").setInputCols(Array("document", "token")).setOutputCol("posTagging")// Path model 4 k8s
    }catch{
      case exception: Exception=>{
       println("Failed to load model localy, Downloading it using ResourceDownloader")
        PerceptronModel.pretrained().setInputCols(Array("document", "token")).setOutputCol("posTagging")
      }
    }

    val pipeline_POS= new Pipeline()
      .setStages(Array(documentAssembler,tokenizer,posTagger))
    val model=pipeline_POS.fit(titleDF.select("title","feed_title", "date", "feed_url", "link","content"))
    val relevantTokens: DataFrame = getRelevantTokens(model, titleDF.select("title","feed_title", "date", "feed_url", "link","content"))
    relevantTokens.show(4)
    val taggedDF:DataFrame=Tagger.tagDF(relevantTokens,spark)
    val taggedCleanDF=taggedDF.withColumn("content",when(col("content").isNull,"").otherwise(col("content")))
    taggedCleanDF.show(5,truncate=false)
    logger.info(s"Adding Daily Messages to PSQL DB \n Number of today messages:${taggedDF.count()}")


    if (mode){
      DataBaseManagerRemote.afterEach()
      DataBaseManagerRemote.beforeEach()
      DataBaseManagerRemote.addMessages(taggedCleanDF)
    }else{
      DataBaseManagerLocal.afterEach()
      DataBaseManagerLocal.beforeEach()
      DataBaseManagerLocal.addMessages(taggedCleanDF)
    }
  }

}
