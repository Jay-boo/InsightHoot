import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions._;
import org.apache.spark.sql.types._;
import org.apache.spark.sql.{Encoders};

object Main {
  def main(args: Array[String]): Unit = {
    if (args.length != 1 || (args(0) != "local" && args(0) != "k8s")) {
      println("Usage: Main <mode>")
      println("Modes: local | k8s")
      System.exit(1)
    }
    val mode: Boolean = args(0) == "k8s"
    val kafkaParams = Map[String, String](
      "kafka.bootstrap.servers" ->( if (mode) "kafka-service.default.svc.cluster.local:9092" else  "localhost:9092"),
      "subscribe" -> "databricks",
      "startingOffsets"-> "earliest",
      "endingOffsets"-> "latest"
    )
    println(s"Mode : $mode")
    println(kafkaParams)

    val spark = SparkSession
      .builder()
      .appName("KafkaToSparkStreaming")
      .config("spark.master","local")
      .getOrCreate()
    import spark.implicits._

    val df=spark
      .read
      .format("kafka")
      .options(kafkaParams)
      .load()

    df.selectExpr("CAST(value AS STRING)").show(truncate=false)
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
    payloadParsedDF.show()
    payloadParsedDF.printSchema()
  }
}
