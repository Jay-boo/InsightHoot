import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import com.johnsnowlabs.nlp.annotators.Tokenizer
import com.johnsnowlabs.nlp.annotators.pos.perceptron.PerceptronModel
import com.johnsnowlabs.nlp.base.DocumentAssembler
import org.apache.spark.ml.Pipeline;

object Main extends SparkMachine {
  import spark.implicits._

  def getRelevantTokens(df:DataFrame): DataFrame = {
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



  def main(args: Array[String]): Unit = {
    if (args.length != 1 || (args(0) != "local" && args(0) != "k8s")) {
      println("Usage: Main <mode>")
      println("Modes: local | k8s")
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
    val titleDF=getRelevantTokens(df).select("title")

    val documentAssembler= new DocumentAssembler()
      .setInputCol("title")
      .setOutputCol("document")
    val  tokenizer=new Tokenizer()
      .setInputCols("document").setOutputCol("token")

    val posTagger= PerceptronModel.pretrained().setInputCols(Array("document","token")).setOutputCol("posTagging")

    val pipeline_POS= new Pipeline()
      .setStages(Array(documentAssembler,tokenizer,posTagger))

    val model=pipeline_POS.fit(titleDF)
    val res=model.transform(titleDF)
    val explodedDF=res.withColumn("explodedPosTagging",explode($"posTagging")).drop("token","document","posTagging")
    println("Tagging ...")
//    explodedDF.withColumn("token",$"explodedPosTagging.metadata")
//      .withColumn("tag",$"explodedPosTagging.result").select("title","token","tag").groupBy("title")
//      .agg(collect_list("token").as("tokens"),collect_list("tag").as("tags")).select("title","tags").show(truncate=false)



    val pertinentDF=explodedDF.withColumn("token",$"explodedPosTagging.metadata")
      .filter($"explodedPosTagging.result".startsWith("NN"))
      .withColumn("token",expr("token['word']"))
    println("Relevant tokens  :")
    pertinentDF.groupBy("title").agg(collect_list("token").as("relevant_tokens")).show(truncate=false)
  }

}
