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
import java.sql.Timestamp
import scala.util.{Failure, Success, Try}


object DataBaseManager  extends SparkMachine  with Logging{
  import spark.implicits._
  val tagRepository=new TagRepository(MainDatabaseConfig)
  val topicRepository=new TopicRepository(MainDatabaseConfig)
  val messageRepository= new MessageRepository(MainDatabaseConfig,topicRepository)
  val messageTagsRepository:MessageTagsRepository=new MessageTagsRepository(MainDatabaseConfig,tagRepository, messageRepository)

  def beforeEach(): Unit = {
    Await.result(tagRepository.beforeEach(),10.seconds)
    Await.result(topicRepository.beforeEach(),10.seconds)
    Await.result(messageRepository.beforeEach(),10.seconds)
    Await.result(messageTagsRepository.beforeEach(),10.seconds)
  }

  def afterEach():Unit={
    Await.result(tagRepository.afterEach(),10.seconds)
    Await.result(topicRepository.afterEach(),10.seconds)
    Await.result(messageRepository.afterEach(),10.seconds)
    Await.result(messageTagsRepository.afterEach(),10.seconds)
  }


//  def addTopics(df_topics: DataFrame): DataFrame = {
//    val df: DataFrame = df_topics.map {
//      case Row(title: String, tokens: Seq[String], tags: Seq[Map[String, String]]) => {
//        println("foreach ----", title)
//        val topic: Topic = Topic(None, title, s"tmpUrl_$title")
//        val insertedTopicId: Try[Int] = Try(Await.result(topicRepository.add(topic), 5.seconds))
//        println(insertedTopicId)
//
//        insertedTopicId match {
//          case Success(v) => (title, tokens, tags, Some(v))
//          case Failure(e)=> (title,tokens,tags,None)
//        }
//      }
//    }.toDF("title", "tokens", "tags", "topic_id")
//    df.show()
//    df
//  }

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

//  def addMessages(df_messages:DataFrame): Unit = {
//    df_messages.rdd.foreach {
//      case Row(title:String,tokens:Seq[String],tags:Seq[Map[String,String]])=>{
//        val topic:Topic=Topic(None,"topic_name",s"tmp_url$title")
//        val topicId:Try[Int]=Try(Await.result(topicRepository.add(topic),10.seconds))
//        val topic_id:Option[Int]=topicId match {
//          case Success(value)=>Some(value)
//          case Failure(e)=>{
//            Await.result(topicRepository.getByUrl(s"tmp_utl$title"),10.seconds) match {
//              case Some(v)=>v.id
//              case None =>None
//            }
//          }
//        }
//
//        val tag:TagTheme=TagTheme(None, label = "labelTag" , theme = "themeTag")
//
//        println("topic_id",topic_id)
//
//
//        var msg:Message=Message(None,
//          content = "No content for now",
//          topic_id = 1,
//         )
//      }
//    }
//
//  }
  def addRowMessage(row:Row): Unit = {
    row match {
      case Row(title:String,
      feed_title:String,
      date:Timestamp,
      feed_url:String,
      link:String,
      content:String,
      relevant_tokens:Seq[String],
      tags:Seq[Map[String,String]]
      )=>{
        val topic:Topic=Topic(None,feed_title,feed_url)
        val topicId:Option[Int]=Try(Await.result(topicRepository.add(topic),10.seconds)) match {
          case Success(value)=>Some(value)
          case Failure(e)=>{
            Await.result(topicRepository.getByUrl(topic.url),10.seconds) match {
              case Some(value)=> value.id
              case None=>None
            }
          }
        }
        val topic_id=topicId.getOrElse(throw  new NoSuchElementException("topicId not found"))
        println("topicIs",topic_id)
      }
      case _=>{println("row not matched :",row)}
    }

  }

  def main(args:Array[String]): Unit = {
    afterEach()
    beforeEach()
    val columns = Seq("title", "feed_title", "date", "feed_url", "link", "content", "relevant_tokens", "tags")
    val data = Seq(
      ("Daily Active People is A Bigger Concern Right Now Than User Experience", "top scoring links : technology", Timestamp.valueOf("2024-05-20 20:49:52"), "https://www.reddit.com/r/technology/top.rss?t=day", "https://www.reddit.com/r/technology/comments/1cwn4ln/daily_active_people_is_a_bigger_concern_right_now/", null, Seq("Daily", "People", "Concern", "User", "Experience", "Mark", "Zuckerberg", "allies", "Facebook", "pursuit", "growth"), Seq()),
      ("$100M in Funding Boosts Alkira’s Secure, Scalable Infrastructure as-a-Service Solution", "Database Trends and Applications : All Articles", Timestamp.valueOf("2024-05-15 08:30:00"), "https://feeds.feedburner.com/DBTA-Articles", "https://www.dbta.com/Editorial/News-Flashes/100M-in-Funding-Boosts-Alkiras-Secure-Scalable-Infrastructure-as-a-Service-Solution-164084.aspx", "Alkira, the leader in on-demand network infrastructure as-a-service, is announcing the results of its Series C funding round, having raised $100 million—amounting to a total funding to date of $176 million. This funding round—led by Tiger Global Management with additional investment and participation from Dallas Venture Capital, Geodesic Capital, NextEquity Partners, Kleiner Perkins, Koch Disruptive Technologies, and Sequoia Capital—aims to accelerate Alkira's innovation efforts in the space of secure network infrastructure as-a-service.", Seq("$100M", "Funding", "Boosts", "Alkira’s", "Secure", "Scalable", "Infrastructure", "Solution"), Seq()),
      ("Portal' installation linking Dublin and New York reopens after 'inappropriate behavior'", "top scoring links : technology", Timestamp.valueOf("2024-05-20 09:03:45"), "https://www.reddit.com/r/technology/top.rss?t=day", "https://www.reddit.com/r/technology/comments/1cw9ekq/portal_installation_linking_dublin_and_new_york/", null, Seq("Portal", "installation", "Dublin", "New", "York", "reopens", "behavior"), Seq()),
      ("Prism' Translation Layer Does For Arm PCs What Rosetta Did For Macs", "Slashdot", Timestamp.valueOf("2024-05-20 22:12:00"), "http://rss.slashdot.org/Slashdot/slashdotMain", "https://tech.slashdot.org/story/24/05/20/2011258/prism-translation-layer-does-for-arm-pcs-what-rosetta-did-for-macs?utm_source=rss1.0mainlinkanon&utm_medium=feed", "An anonymous reader quotes a report from Ars Technica: Microsoft is going all-in on Arm-powered Windows PCs today with the introduction of a Snapdragon X Elite-powered Surface Pro convertible and Surface Laptop, and there are inevitable comparisons to draw with another big company that recently shifted from Intel's processors to Arm-based designs: Apple. A huge part of the Apple Silicon transition's success was Rosetta 2, a translation layer that makes it relatively seamless to run most Intel Mac apps on an Apple Silicon Mac with no extra effort required from the user or the app's developer. Windows 11 has similar translation capabilities, and with the Windows 11 24H2 update, that app translation technology is getting a name: Prism.\n\nMicrosoft says that Prism isn't just a new name for the same old translation technology. Translated apps should run between 10 and 20 percent faster on the same Arm hardware after installing the Windows 11 24H2 update, offering some trickle-down benefits that users of the handful of Arm-based Windows 11 PCs should notice even if they don't shell out for new hardware. The company says that Prism's performance should be similar to Rosetta's, though obviously this depends on the speed of the hardware you're running it on. Microsoft also claims that Prism will further improve the translation layer's compatibility with x86 apps, though the company didn't get into detail about the exact changes it had made on this front.", Seq("Prism", "Translation", "Layer", "Arm", "PCs", "Rosetta", "Macs"), Seq()),
      ("10 tips to avoid planting AI timebombs in your organization", "Tech News – TechToday", Timestamp.valueOf("2024-04-17 00:33:19"), "https://techtoday.co/category/tech-news/feed/", "https://techtoday.co/10-tips-to-avoid-planting-ai-timebombs-in-your-organization/", "[ad_1] At the recent HIMSS Global Health Conference & Exhibition in Orlando, I delivered a talk focused on protecting against some of the pitfalls of artificial intelligence in healthcare. The objective was to encourage healthcare professionals to think deeply about the realities of AI transformation, while providing them with real-world examples of how to proceed…", Seq("tips", "AI", "timebombs", "organization"), Seq(Map("AI" ->"IA")))
    )


    val df = data.toDF(columns: _*)
    val dfCleaned=df.withColumn("content",when(col("content").isNull,"").otherwise(col("content")))

    println("Starting")
    val my_row:Row=dfCleaned.first()
    println("content",my_row,my_row.size,my_row.get(2),my_row.get(2).getClass)
//    if(content==null){println("greta")}else{println("NOT OK")}
    addRowMessage(my_row)
//    addMessages(df)
//    addTags(df)
//    tagRepository.all(20,0).foreach(println)
    println("-------TopicState")
    topicRepository.all(20,0).foreach(println)

  }


}
