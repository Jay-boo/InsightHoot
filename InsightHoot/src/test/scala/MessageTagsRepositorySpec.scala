import models.repositories.{MessageRepository, TagRepository, TopicRepository}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import models.entities.{Message, MessageTag, TagTheme, Topic}

import java.sql.Timestamp
import scala.util.{Failure, Success, Try}

class MessageTagsRepositorySpec extends munit.FunSuite {

  val tagRepository=new TagRepository(TestDatabaseConfig)
  val topicRepository= new TopicRepository(TestDatabaseConfig)
  val messageRepository= new MessageRepository(
    TestDatabaseConfig,
    topicRepository = topicRepository
  )
  val messageTagsRepository:MessageTagsRepository=new MessageTagsRepository(TestDatabaseConfig,tagRepository, messageRepository)

  override def beforeEach(context: BeforeEach): Unit = {
    Await.result(tagRepository.beforeEach(),10.seconds)
    Await.result(topicRepository.beforeEach(),10.seconds)
    Await.result(messageRepository.beforeEach(),10.seconds)
    Await.result(messageTagsRepository.beforeEach(),10.seconds)

  }


  override def afterEach(context: AfterEach): Unit = {
    Await.result(tagRepository.afterEach(),10.seconds)
    Await.result(messageRepository.afterEach(),10.seconds)
    Await.result(topicRepository.afterEach(),10.seconds)
    Await.result(messageTagsRepository.afterEach(),10.seconds)

  }

  test("Add should not insert a Message-tag relation if tag id not existing"){

    val msg_tags:MessageTag=MessageTag(1,1)
    val insertJob:Try[Int]=Try(Await.result(messageTagsRepository.add(msg_tags),10.seconds))
    insertJob match{
      case Success(value)=>fail("Insert Job has not failed while adding MessageTag with not existing message and tag id")
      case Failure(e)=>
    }
    Await.result(messageTagsRepository.all(20,0),10.seconds).foreach(println)
  }
  test("Add should not insert a Message-tag relation if message id not existing"){
    val msg_tags:MessageTag=MessageTag(1,1)
    val tag:TagTheme= TagTheme(None,"tag1","them1")
    Await.result(tagRepository.add(tag),10.seconds)
    val insertJob:Try[Int]=Try{Await.result(messageTagsRepository.add(msg_tags),10.seconds)}
    insertJob match {
      case Success(value)=>fail("Insert Job has not failed while adding MessageTag with not existing message id")
      case Failure(exception)=>
    }
    Await.result(messageTagsRepository.all(20,0),10.seconds).foreach(println)
  }

  test("Add should insert if message id and tag id both exist"){
    val tag:TagTheme= TagTheme(None,"tag1","them1")
    val topic:Topic=Topic(None,"topic1","url1")
    val tagId:Int=Await.result(tagRepository.add(tag),10.seconds)
    val topicId:Int=Await.result(topicRepository.add(topic),10.seconds)
    val msg:Message=Message(None,Timestamp.valueOf("2024-05-20 20:49:52"),"Lorem Ipsum","title","link1",topic_id = topicId)
    val msgId=Await.result(messageRepository.add(msg),10.seconds)
    val msg_tags:MessageTag=MessageTag(msgId, tagId)
    val insertJob:Int=Await.result(messageTagsRepository.add(msg_tags),10.seconds)

  }

}
