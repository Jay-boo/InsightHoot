import models.{DatabaseConfig, MainDatabaseConfig}
import models.entities.{Message, TagTheme, Topic}
import models.repositories.{MessageRepository, TagRepository, TopicRepository}
import org.apache.logging.log4j.scala.Logging
import org.apache.spark.sql.{DataFrame, Row}

import scala.concurrent.ExecutionContext.Implicits.global
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.{JdbcBackend, JdbcProfile}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}


object DataBaseManager  extends SparkMachine  with Logging{
  import spark.implicits._
  val tagRepository=new TagRepository(MainDatabaseConfig)
  val topicRepository=new TopicRepository(MainDatabaseConfig)
  val messageRepository= new MessageRepository(MainDatabaseConfig,tagRepository,topicRepository)

  def beforeEach(): Unit = {
    Await.result(messageRepository.beforeEach(),10.seconds)
    Await.result(tagRepository.beforeEach(),10.seconds)
    Await.result(topicRepository.beforeEach(),10.seconds)
  }
  def afterEach():Unit={
    Await.result(messageRepository.afterEach(),10.seconds)
    Await.result(tagRepository.afterEach(),10.seconds)
    Await.result(topicRepository.afterEach(),10.seconds)
  }


  def addTopics(df_topics: DataFrame): DataFrame = {
    val df: DataFrame = df_topics.map {
      case Row(title: String, tokens: Seq[String], tags: Seq[Map[String, String]]) => {
        println("foreach ----", title)
        val topic: Topic = Topic(None, title, s"tmpUrl_$title")
        val insertedTopicId: Try[Int] = Try(Await.result(topicRepository.add(topic),5.seconds))
        insertedTopicId match {
          case Success(v)=> (title,tokens,tags,v)
          case Failure(e)=> _
        }
      }
    }.toDF("title", "tokens","tags","topic_id")
    df.show()
    df
  }
  def addTags(df_tags:DataFrame): Unit={
    df_tags.rdd.foreach {
      case Row(title:String,tokens:Seq[String],tags:Seq[Map[String,String]])=>{
        if(tags.size>0){
          tags.foreach[Unit](r=>{
            val tag=TagTheme(None,
              r.keys.head,
              r(r.keys.head)
            )
            tagRepository.add(tag)
            println("tag added",tags(0).keys.head,tags(0)(tags(0).keys.head))
          }
          )
        }
      }
    }

  }

  def addMessages(df_messages:DataFrame): Unit = {
    df_messages.rdd.foreach {
      case Row(title:String,tokens:Seq[String],tags:Seq[Map[String,String]])=>{
        var msg:Message=Message(None,
          content = "No content for now",
          topic_id = 1,
          tag_id = 1)

      }
    }

  }

  def main(args:Array[String]): Unit = {
    afterEach()
    beforeEach()
    val data_topic = Seq(
      ("Where is the opt-out button? — Slack users horrified to discover messages used for AI training",
        Seq("button", "?", "—", "Slack", "users", "messages", "AI", "training"),
        Seq(Map("AI" -> "IA"))),
      ("$100M in Funding Boosts Alkira?s Secure, Scalable Infrastructure as-a-Service Solution",
        Seq("$100M", "Funding", "Boosts", "Alkira?s", "Secure", "Scalable", "Infrastructure", "Solution"),
        Seq(Map("omega"->"lul"))),
      ("'Portal' installation linking Dublin and New York reopens after 'inappropriate behavior'",
        Seq("Portal", "installation", "Dublin", "New", "York", "reopens", "behavior"),
        Seq(Map("prout"->"then"))),
      ("10 tips to avoid planting AI timebombs in your organization",
        Seq("tips", "AI", "timebombs", "organization"),
        Seq(Map("AI" -> "IA"))),
      ("3 Questions: Enhancing last-mile logistics with machine learning | MIT News",
        Seq("Questions", "Enhancing", "logistics", "machine", "|", "MIT", "News"),
        Seq(Map("3emeTag"->"theme")))
    )
    val df:DataFrame= data_topic.toDF("title","relevant_tokens","tags")

    println("Starting")
//    addTags(df)
//    tagRepository.all(20,0).foreach(println)
    addTopics(df)
//    topicRepository.all(20,0).foreach(println)






  }


}
