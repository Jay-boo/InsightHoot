import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession;

trait SparkMachine {
  val spark:SparkSession = SparkSession
    .builder()
    .appName("KafkaToSparkStreaming")
    .config("spark.master","local")
    .getOrCreate()
  val rootLogger=Logger.getRootLogger()
  rootLogger.setLevel(Level.INFO)
}
