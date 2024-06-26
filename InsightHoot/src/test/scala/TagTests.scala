import com.johnsnowlabs.nlp.annotators.Tokenizer
import com.johnsnowlabs.nlp.annotators.pos.perceptron.PerceptronModel
import com.johnsnowlabs.nlp.base.DocumentAssembler
import org.apache.spark.ml.Pipeline
import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spark.sql.functions._
import org.joda.time.LocalDateTime

import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.collection.mutable
import scala.io.Source

class TagTests extends munit.FunSuite with SparkMachine {

  import spark.implicits._
  val df: DataFrame=spark.read.parquet("src/test/resources/raw_data/*.parquet")
  val payloadDF:DataFrame=Main.getPayload(df)
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
  val model=pipeline_POS.fit(payloadDF.select("title","feed_title", "date", "feed_url", "link","content"))
  val titleDF:DataFrame = Seq(("Ubuntu new upgrade is now available !","UbuntuFAQ",
    Timestamp.valueOf("2024-05-19 17:41:48"),
    "https://www.reddit.com/r/technology/top.rss?t=day",
    "https://www.reddit.com/r/technology/comments/1cvr5av/where_is_the_optout_button_slack_users_horrified/",
    "Lorem Ipsum"
  )).toDF("title","feed_title", "date", "feed_url", "link","content")
  val relevantTokensDF:DataFrame=Main.getRelevantTokens(model,titleDF.select("title", "feed_title","date", "feed_url", "link","content"))

  test("Payload  collect"){
    val expectedColumns: Seq[String]= Seq("feed_title","feed_url","title","id","link","content","author","date")
    assertEquals(payloadDF.columns.toSeq,expectedColumns)
    val expectedDistinctFeedTitles:Seq[String]=Seq(
      "Slashdot",
      "WSJ.com: WSJD",
      "4sysops",
      "Flux Cloud Computing Developpez",
      "Technology | The Guardian",
      "top scoring links : technology"
    )
    assert(
      payloadDF.filter(
        !$"feed_title".isin(expectedDistinctFeedTitles:_*)
      ).isEmpty
    )
  }


  test("Detect relevant tokens"){

    assertEquals(relevantTokensDF.columns.toSeq,Seq("title", "feed_title","date", "feed_url", "link","content", "relevant_tokens"))
    val row : Row =relevantTokensDF.collect()(0)
    val resultedRelevantTokens:Seq[String]=row.getAs[mutable.WrappedArray[String]]("relevant_tokens").toSeq
    println(resultedRelevantTokens)
    assertEquals(resultedRelevantTokens,Seq("Ubuntu","upgrade"))
  }



  test("Getting document tags"){
    val taggedDF:DataFrame=Tagger.tagDF(relevantTokensDF,spark)
    taggedDF.show()
    assertEquals(taggedDF.columns.toSeq,Seq("title","feed_title","date", "feed_url", "link","content", "relevant_tokens","tags"))
    val tags:Seq[Seq[String]]=taggedDF.collect()(0).getAs[mutable.WrappedArray[Seq[String]]]("tags")
    assertEquals(
      tags.length,
      1
    )
    val row:Row=tags(0).asInstanceOf[Row]
    assertEquals(
      Seq(row.getString(0),row.getString(1)),
      Seq("Ubuntu","OS")
    )
  }
}
