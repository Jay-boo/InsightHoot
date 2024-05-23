import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession;

trait SparkMachine {
  @transient val spark:SparkSession = SparkSession
    .builder()
    .appName("KafkaToSparkStreaming")
    .config("spark.master","local")
    .getOrCreate()
  @transient val rootLogger=Logger.getRootLogger()
  rootLogger.setLevel(Level.INFO)

}
