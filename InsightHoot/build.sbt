name := "InsightHootKafka"
version := "0.1"
scalaVersion := "2.12.18"


libraryDependencies += "org.apache.spark" %% "spark-core" % "3.5.1"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.5.1"
libraryDependencies += "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.5.1"
libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "3.5.1"
libraryDependencies += "com.johnsnowlabs.nlp" %% "spark-nlp" % "5.3.3"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "3.5.1"
libraryDependencies += "org.apache.logging.log4j" %% "log4j-api-scala" % "13.0.0"
libraryDependencies += "com.lihaoyi" %% "upickle" % "3.3.0"
libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.9.1"
libraryDependencies += "com.github.vickumar1981" %% "stringdistance" % "1.2.7"
libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.3.3",
  "org.postgresql" % "postgresql" % "42.3.4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
)

libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test
libraryDependencies += "com.h2database" % "h2" % "1.4.200"
libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.19.0" % Runtime

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case _                        => MergeStrategy.first
  case "META-INF/services/org.apache.spark.sql.sources.DataSourceRegister" => MergeStrategy.concat
}

assemblyOutputPath in assembly := file("../jars/InsightHootKafka-3.5.1-8-2.12.jar")