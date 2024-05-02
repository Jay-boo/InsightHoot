import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, get_json_object}

object Main {
  def main(args: Array[String]): Unit = {
    if (args.length != 1 || (args(0) != "local" && args(0) != "k8s")) {
      println("Usage: Main <mode>")
      println("Modes: local | k8s")
      System.exit(1)
    }
    val mode: Boolean = args(0) == "k8s"
    val kafkaParams = Map[String, String](
      "kafka.bootstrap.servers" -> (if (mode) "kafka-service.default.svc.cluster.local:9092" else "localhost:9092"),
      "subscribe" -> "4sysops, developpez, reddittech, slashdot, theguardian, wsj", // Update with your topics
      "startingOffsets" -> "earliest",
      "endingOffsets" -> "latest"
    )
    println(s"Mode : $mode")
    println(kafkaParams)

    val spark = SparkSession
      .builder()
      .appName("KafkaToSparkStreaming")
      .config("spark.master", "local")
      .getOrCreate()

    val df = spark
      .read
      .format("kafka")
      .options(kafkaParams)
      .load()

    println("Getting DF from kafka")
    df.printSchema()
    val dfWithPayload = df.selectExpr("topic", "CAST(key AS STRING)", "CAST(value AS STRING)")
      .withColumn("topic_name", col("topic"))
      .withColumn("payload", get_json_object(col("value"), "$.payload"))
      .drop("value")

    dfWithPayload
      .write
      .partitionBy("topic_name")
      .mode("overwrite")
      .parquet("output/raw_data") // Update with your desired output path
  }
}
