import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Encoders;

object Main {
  def main(args: Array[String]): Unit = {
    // Create SparkSession
    val spark = SparkSession
      .builder()
      .appName("KafkaToSparkStreaming")
      .config("spark.master","local")
      .getOrCreate()

    // Define Kafka parameters
    val kafkaParams = Map[String, String](
      "kafka.bootstrap.servers" -> "kafka-service.default.svc.cluster.local:9092", // Kafka broker
      "subscribe" -> "topic_from_producer" // Kafka topic to subscribe to
      //"auto.offset.reset" -> "latest" // Read from the latest offset
    )

    // Define Kafka topic to subscribe to
    val kafkaTopic = "databricktopic"

    // Read data from Kafka as a DataFrame
    val kafkaStreamDF = spark
      .readStream
      .format("kafka")
      .options(kafkaParams)
      .load()

    // Define value schema
    val valueSchema = "value STRING"

    // Parse the value from Kafka message into a DataFrame
    val parsedDF = kafkaStreamDF
      .selectExpr("CAST(value AS STRING)")
      .selectExpr(valueSchema)
      .as(Encoders.STRING)

    // Your processing logic here
    // For example, you can write the DataFrame to console

    val query = parsedDF
      .writeStream
      .outputMode("append")
      .format("console")
      .start()

    // Wait for the termination of the query
    query.awaitTermination()
  }
}
