name := "InsightHootKafka"
version := "0.1"
scalaVersion := "2.12.18"


// https://mvnrepository.com/artifact/org.scala-lang/scala-library
//libraryDependencies += "org.scala-lang" % "scala-library" % "2.11.12"
// https://mvnrepository.com/artifact/org.apache.spark/spark-core
libraryDependencies += "org.apache.spark" %% "spark-core" % "3.5.1"
// https://mvnrepository.com/artifact/org.apache.spark/spark-sql
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.5.1"
libraryDependencies += "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.5.1"


// https://mvnrepository.com/artifact/org.apache.spark/spark-streaming-kafka-0-10
libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "3.5.1"

libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case _                        => MergeStrategy.first
  case "META-INF/services/org.apache.spark.sql.sources.DataSourceRegister" => MergeStrategy.concat
}

assemblyOutputPath in assembly := file("../jars/InsightHootKafka-3.5.1-8-2.12.jar")