import models.db.{MessageComponent, TagComponent, TopicComponent}
import models.entities.{Message, TagTheme, Topic}
import models.repositories.{MessageRepository, TagRepository, TopicRepository}

import java.sql.Timestamp
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration.DurationInt

class MessageRepositorySpec extends munit.FunSuite {
  val topicRepository= new TopicRepository(TestDatabaseConfig)
  val messageRepository= new MessageRepository(
    TestDatabaseConfig,
    topicRepository = topicRepository
  )
  val msg:Message=Message(None,Timestamp.valueOf("2024-05-20 20:49:52"),"Lorem Ipsum","titleMessage","link1",1)

  override def beforeEach(context: BeforeEach): Unit = {
    Await.result(messageRepository.beforeEach(),10.seconds)
    Await.result(topicRepository.beforeEach(),10.seconds)
  }


  override def afterEach(context: AfterEach): Unit = {
    Await.result(messageRepository.afterEach(),10.seconds)
    Await.result(topicRepository.afterEach(),10.seconds)

  }

  test("Add should insert a Message if topic_id exist"){
    println("-------Inserting Topic value------")
    Await.result(messageRepository.topicRepository.add(Topic(None,"topicName","UrlTopic1")),10.seconds)
    Await.result(messageRepository.topicRepository.all(3,0),10.seconds).foreach(println)
    println("------------------------------------------")
    val addResult:Int=Await.result(messageRepository.add(msg), 10.seconds)
    assertEquals(addResult,1)
    val addedRow:Option[Message]=Await.result(messageRepository.getById(1),10.seconds)
    addedRow match {
      case Some(r)=> println(s"Row Added : $r")
      case _=>fail("Added Message as not been found")
    }
    assertEquals(addedRow.map(_.id),Some(Some(1)))
    assertEquals(addedRow.map(_.content),Some("Lorem Ipsum"))
    assertEquals(addedRow.map(_.topic_id),Some(1))
    val addResultSecond:Int=Await.result(messageRepository.add(msg), 10.seconds)
    assertEquals(addResultSecond,2)
    val addedRowSecond:Option[Message]=Await.result(messageRepository.getById(2),10.seconds)
    addedRowSecond match {
      case Some(r)=> println(s"Row Added : $r")
      case _=>fail("Added Message as not been found")
    }
  }

  test("Add should Not insert a Message if topic_id and tag_id don't exist "){
    topicRepository.all(20,0).foreach(println)
    val addResult=Try(Await.result(messageRepository.add(msg),10.seconds))
    addResult match{
      case Failure(exception)=>
      case Success(value)=> fail("Expected the row to don't be added throwing exception")
    }
    val allMsg:Seq[Message]=Await.result(messageRepository.all(2,0),10.seconds)
    assertEquals(allMsg.length,0)
  }


  test("Delete should delete an existing Message"){
    val msg:Message=Message(None,Timestamp.valueOf("2024-05-20 20:49:52"),"Lorem Ipsum","Title1","link1",1)
    println("-------Inserting Tag and Topic value------")
    Await.result(messageRepository.topicRepository.add(Topic(None,"titleTopic1","UrlTopic1")),10.seconds)
    Await.result(messageRepository.topicRepository.all(3,0),10.seconds).foreach(println)
    println("------------------------------------------")
    val insertedId:Int=Await.result(messageRepository.add(msg), 10.seconds)
    val insertedIdSecond:Int=Await.result(messageRepository.add(Message(None,Timestamp.valueOf("2024-05-20 20:49:52"),"Lorem Ipsum2","title2","link2",1)), 10.seconds)
    val deleteResult:Int=Await.result(messageRepository.deleteById(insertedId),10.seconds)
    assertEquals(deleteResult,1)
    val deletedRecord:Option[Message]=Await.result(
      messageRepository.getById(insertedId),
      10.seconds
    )
    deletedRecord match {
      case Some(r)=> fail("Record still found after delete")
      case None =>
    }


  }
}
