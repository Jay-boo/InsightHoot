import org.apache.spark.sql.SparkSession;
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

    val df=spark
      .read
      .format("kafka")
      .options(kafkaParams)
      .load()

    println("Getting DF from kafka")
    df.printSchema()
    df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)").show(4,truncate=false)

//    val kafkaStreamDF = spark
//      .readStream
//      .format("kafka")
//      .options(kafkaParams)
//      .load()
//
//    val valueSchema = "value STRING"
//    val parsedDF = kafkaStreamDF
//      .selectExpr("CAST(value AS STRING)")
//      .selectExpr(valueSchema)
//      .as(Encoders.STRING)
//
//
//    val query = parsedDF
//      .writeStream
//      .outputMode("append")
//      .format("console")
//      .start()
//    query.awaitTermination()
  }
}
