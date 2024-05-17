import models.db.{MessageComponent, TagComponent, TopicComponent}
import models.entities.{Message, TagTheme, Topic}
import models.repositories.{MessageRepository, TagRepository, TopicRepository}

import scala.concurrent.Await
import scala.util.{Try,Success,Failure}
import scala.concurrent.duration.DurationInt

class MessageRepositorySpec extends munit.FunSuite {
  val tagRepository=new TagRepository(TestDatabaseConfig)
  val topicRepository= new TopicRepository(TestDatabaseConfig)

  val messageRepository= new MessageRepository(
    TestDatabaseConfig,
    tagRepository = tagRepository,
    topicRepository = topicRepository
  )

  override def beforeEach(context: BeforeEach): Unit = {
    Await.result(messageRepository.beforeEach(),10.seconds)
    Await.result(tagRepository.beforeEach(),10.seconds)
    Await.result(topicRepository.beforeEach(),10.seconds)
  }


  override def afterEach(context: AfterEach): Unit = {
    Await.result(messageRepository.afterEach(),10.seconds)
    Await.result(tagRepository.afterEach(),10.seconds)
    Await.result(topicRepository.afterEach(),10.seconds)

  }
  test("beforeAll should not recreate schema if exist "){
    val schemaCreated=Await.result(messageRepository.beforeEach(),10.seconds)
    assert(schemaCreated.isInstanceOf[Unit])
  }

  test("Add should insert a Message if topic_id and tag_id exist"){
    val msg:Message=Message(None,"Lorem Ipsum",1,1)
    println("-------Inserting Tag and Topic value------")

    Await.result(messageRepository.tagRepository.add(TagTheme(None,"Label1","Theme1")),10.seconds)
    Await.result(messageRepository.topicRepository.add(Topic(None,"titleTopic1","UrlTopic1")),10.seconds)
    Await.result(messageRepository.tagRepository.all(3,0),10.seconds).foreach(println)
    println("------------------------------------------")
    val addResult:Int=Await.result(messageRepository.add(msg), 10.seconds)
    assertEquals(addResult,1)
    val addedRow:Option[Message]=Await.result(messageRepository.getById(1),10.seconds)
    addedRow match {
      case Some(r)=> println(s"Row Added : $r")
      case _=>fail("Added Message as not been found")
    }
    assertEquals(addedRow.map(_.tag_id),Some(1))
    assertEquals(addedRow.map(_.topic_id),Some(1))
    assertEquals(addedRow.map(_.content),Some("Lorem Ipsum"))
    assertEquals(addedRow.map(_.id),Some(Some(1)))
    val addResultSecond:Int=Await.result(messageRepository.add(msg), 10.seconds)
    assertEquals(addResultSecond,2)
    val addedRowSecond:Option[Message]=Await.result(messageRepository.getById(2),10.seconds)
    addedRowSecond match {
      case Some(r)=> println(s"Row Added : $r")
      case _=>fail("Added Message as not been found")
    }
//    Await.result(tagRepository.getById(1),10.seconds) match {
//      case Some(r)=>println(r)
//      case _=>println("No rows in tags")
//    }
  }

  test("Add should Not insert a Message if topic_id and tag_id don't exist "){
    val msg:Message=Message(None,"Lorem Ipsum",1,2)
    val addResult=Try(Await.result(messageRepository.add(msg),10.seconds))
    addResult match{
      case Failure(exception)=>assertEquals(
        exception.getMessage,
        s"Topic Id :${msg.topic_id} and/or Tag Id:${msg.tag_id} don't exist"
      )
      case Success(value)=> fail("Expected the row to don't be added throwing exception")
    }
    val msgAllRows:Seq[Message]=Await.result(messageRepository.all(2,0),10.seconds)
//    msgAllRows.foreach(println)
    assertEquals(msgAllRows.length,0)
//    assertEquals(addResult,0)
  }


  test("Delete should delete an existing Message"){
    val msg:Message=Message(None,"Lorem Ipsum",1,1)
    println("-------Inserting Tag and Topic value------")
    Await.result(messageRepository.tagRepository.add(TagTheme(None,"Label1","Theme1")),10.seconds)
    Await.result(messageRepository.topicRepository.add(Topic(None,"titleTopic1","UrlTopic1")),10.seconds)
    Await.result(messageRepository.tagRepository.all(3,0),10.seconds).foreach(println)
    println("------------------------------------------")
    val insertedId:Int=Await.result(messageRepository.add(msg), 10.seconds)
    val insertedIdSecond:Int=Await.result(messageRepository.add(Message(None,"Lorem Ipsum2",1,1)), 10.seconds)
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

//    Await.result(messageRepository.deleteBy(insertedId))

  }
}
