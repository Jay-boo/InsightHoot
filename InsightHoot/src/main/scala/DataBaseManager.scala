import Tagger.{DocumentTag, getClass}
import models.{DatabaseConfig, LocalDatabaseConfig, MainDatabaseConfig}
import models.entities.{Message, MessageTag, TagTheme, Topic}
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
import scala.io.Source
import scala.util.{Failure, Success, Try}



object DataBaseManagerLocal  extends SparkMachine  with Logging{
  import spark.implicits._
  val databaseConfig:DatabaseConfig= LocalDatabaseConfig
  lazy val tagRepository=new TagRepository(databaseConfig)
  lazy val topicRepository=new TopicRepository(databaseConfig)
  lazy val messageRepository= new MessageRepository(databaseConfig,topicRepository)
  lazy val messageTagsRepository:MessageTagsRepository=new MessageTagsRepository(databaseConfig,tagRepository, messageRepository)

  def beforeEach(): Unit = {
    //Create file (Do not use without afterEach)

    Try(Await.result(tagRepository.beforeEach(),10.seconds)) match {
      case Failure(e)=> {
        logger.error("Idx tags already exist")
        Await.result(tagRepository.all(5,0),10.seconds).foreach(println)
      }
      case _=>{
        println("Initializing Tags relation")
        logger.info("Initializing Tags relation")
      }
    }
    Await.result(topicRepository.beforeEach(),10.seconds)
    Await.result(messageRepository.beforeEach(),10.seconds)
    Await.result(messageTagsRepository.beforeEach(),10.seconds)
    addAllDefinedTags()
  }

  def afterEach():Unit={
    //Drop existing table
    Await.result(tagRepository.afterEach(),10.seconds)
    Await.result(topicRepository.afterEach(),10.seconds)
    Await.result(messageRepository.afterEach(),10.seconds)
    Await.result(messageTagsRepository.afterEach(),10.seconds)
  }




  def addAllDefinedTags():Unit={
    val tagThemeDB:Seq[TagTheme]=Await.result(tagRepository.all(4000,0),10.seconds)
    val inputStream=getClass.getResourceAsStream("/tags.json")
    val jsonString=Source.fromInputStream(inputStream).mkString
    val jsonTags=ujson.read(jsonString)
    var tags:Seq[TagTheme]=Seq.empty[TagTheme]
    jsonTags.obj.foreach({
      case (theme:String,tagsList)=>
        tagsList.arr.foreach(
          tag=> tags:+= TagTheme(None,tag.str,theme)
        )
    })
    val tagsNotInDB:Seq[TagTheme]=tags.filterNot((tag:TagTheme)=>{
      tagThemeDB.exists( (tagInDb)=>{
        tagInDb.label== tag.label && tagInDb.theme==tag.theme
      })
    })
    println(s"TagToAdd ${tagsNotInDB.size}")
    tagsNotInDB.foreach {
      tagToAppend=> Await.result(tagRepository.add(tagToAppend),10.seconds)
    }
    println("End adding needed tags")

  }

  def addMessages(df_messages:DataFrame): Unit = {
    println("addMessages")
    df_messages.foreach(row=>addRowMessage(row))
  }
  def addRowMessage(row:Row): Unit = {
    println("-----",row)
    row match {
      case Row(title:String,
      feed_title:String,
      date:Timestamp,
      feed_url:String,
      link:String,
      content:String,
      relevant_tokens:Seq[String],
      tags:Seq[Row]
      )=>{
        val topic:Topic=Topic(None,feed_title,feed_url)
        val topicIdOption:Option[Int]=Try(Await.result(topicRepository.add(topic),10.seconds)) match {
          case Success(value)=>Some(value)
          case Failure(e)=>{
            Await.result(topicRepository.getByUrl(topic.url),10.seconds) match {
              case Some(value)=> value.id
              case None=>None
            }
          }
        }
        val topicId=topicIdOption.getOrElse(throw  new NoSuchElementException("topicId not found"))
        val message:Message=Message(None,date,content,title,link,topicId)
        val messageIdOption:Option[Int]=Try(Await.result(messageRepository.add(message),10.seconds)) match {
          case Success(v)=>Some(v)
          case Failure(e)=>None
        }
        val messageId:Int=messageIdOption.getOrElse(throw new NoSuchElementException("Message could not be added"))


        tags.foreach(
          (docTag:Row)=>{
            docTag match {
              case Row(label:String,theme:String)=>{
                val tag:TagTheme=TagTheme(None,label,theme)
                val tagId:Int=Await.result(tagRepository.getId(tag.label,tag.theme),10.seconds).getOrElse(
                  throw new NoSuchElementException(s"Tag unknow =>  Theme: ${tag.theme},Label ${tag.label}"))
                val msg_tag:MessageTag=MessageTag(messageId,tagId)
                Await.result(messageTagsRepository.add(msg_tag),10.seconds)

              }
              case _=> throw new Exception("Unexpected Tags Struct ")

            }
          }
        )

      }

      case _=>{println("Row not matched :",row)}
    }
  }
}

object DataBaseManagerRemote  extends SparkMachine  with Logging{
  import spark.implicits._
  println("DB_MANAGER")
  val databaseConfig:DatabaseConfig= MainDatabaseConfig
  lazy val tagRepository=new TagRepository(databaseConfig)
  lazy val topicRepository=new TopicRepository(databaseConfig)
  lazy val messageRepository= new MessageRepository(databaseConfig,topicRepository)
  lazy val messageTagsRepository:MessageTagsRepository=new MessageTagsRepository(databaseConfig,tagRepository, messageRepository)

  def beforeEach(): Unit = {
    //Create file (Do not use without afterEach)

    Try(Await.result(tagRepository.beforeEach(),10.seconds)) match {
      case Failure(e)=> {
        logger.error("Idx tags already exist")
        Await.result(tagRepository.all(5,0),10.seconds).foreach(println)
      }
      case _=>{
        println("Initializing Tags relation")
        logger.info("Initializing Tags relation")
      }
    }
    Await.result(topicRepository.beforeEach(),10.seconds)
    Await.result(messageRepository.beforeEach(),10.seconds)
    Await.result(messageTagsRepository.beforeEach(),10.seconds)
    addAllDefinedTags()
  }

  def afterEach():Unit={
    //Drop existing table
    Await.result(tagRepository.afterEach(),10.seconds)
    Await.result(topicRepository.afterEach(),10.seconds)
    Await.result(messageRepository.afterEach(),10.seconds)
    Await.result(messageTagsRepository.afterEach(),10.seconds)
  }




  def addAllDefinedTags():Unit={
    val tagThemeDB:Seq[TagTheme]=Await.result(tagRepository.all(4000,0),10.seconds)
    val inputStream=getClass.getResourceAsStream("/tags.json")
    val jsonString=Source.fromInputStream(inputStream).mkString
    val jsonTags=ujson.read(jsonString)
    var tags:Seq[TagTheme]=Seq.empty[TagTheme]
    jsonTags.obj.foreach({
      case (theme:String,tagsList)=>
        tagsList.arr.foreach(
          tag=> tags:+= TagTheme(None,tag.str,theme)
        )
    })
    val tagsNotInDB:Seq[TagTheme]=tags.filterNot((tag:TagTheme)=>{
      tagThemeDB.exists( (tagInDb)=>{
        tagInDb.label== tag.label && tagInDb.theme==tag.theme
      })
    })
    println(s"TagToAdd ${tagsNotInDB.size}")
    tagsNotInDB.foreach {
      tagToAppend=> Await.result(tagRepository.add(tagToAppend),10.seconds)
    }
    println("End adding needed tags")

  }

  def addMessages(df_messages:DataFrame): Unit = {
    println("addMessages")
    df_messages.foreach(row=>addRowMessage(row))
  }
  def addRowMessage(row:Row): Unit = {
    println("-----",row)
    row match {
      case Row(title:String,
      feed_title:String,
      date:Timestamp,
      feed_url:String,
      link:String,
      content:String,
      relevant_tokens:Seq[String],
      tags:Seq[Row]
      )=>{
        val topic:Topic=Topic(None,feed_title,feed_url)
        val topicIdOption:Option[Int]=Try(Await.result(topicRepository.add(topic),10.seconds)) match {
          case Success(value)=>Some(value)
          case Failure(e)=>{
            Await.result(topicRepository.getByUrl(topic.url),10.seconds) match {
              case Some(value)=> value.id
              case None=>None
            }
          }
        }
        val topicId=topicIdOption.getOrElse(throw  new NoSuchElementException("topicId not found"))
        val message:Message=Message(None,date,content,title,link,topicId)
        val messageIdOption:Option[Int]=Try(Await.result(messageRepository.add(message),10.seconds)) match {
          case Success(v)=>Some(v)
          case Failure(e)=>None
        }
        val messageId:Int=messageIdOption.getOrElse(throw new NoSuchElementException("Message could not be added"))


        tags.foreach(
          (docTag:Row)=>{
            docTag match {
              case Row(label:String,theme:String)=>{
                val tag:TagTheme=TagTheme(None,label,theme)
                val tagId:Int=Await.result(tagRepository.getId(tag.label,tag.theme),10.seconds).getOrElse(
                  throw new NoSuchElementException(s"Tag unknow =>  Theme: ${tag.theme},Label ${tag.label}"))
                val msg_tag:MessageTag=MessageTag(messageId,tagId)
                Await.result(messageTagsRepository.add(msg_tag),10.seconds)

              }
              case _=> throw new Exception("Unexpected Tags Struct ")

            }
          }
        )

      }

      case _=>{println("Row not matched :",row)}
    }

  }
}
