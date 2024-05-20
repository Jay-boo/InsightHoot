import models.entities.{TagTheme, Topic}
import models.repositories.TopicRepository

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}

class TopicRepositorySpec  extends munit.FunSuite {
  val topicRepository=new TopicRepository(TestDatabaseConfig)

  override def beforeEach(context:BeforeEach): Unit = {
    Await.result(topicRepository.beforeEach(),10.seconds)
  }

  override def afterEach(context: AfterEach): Unit = {
    Await.result(topicRepository.afterEach(),10.seconds)
  }


  test("beforeEach should create schema if not exist else "){
    val schemaCreated=Await.result(topicRepository.beforeEach(),10.seconds)
    assert(schemaCreated.isInstanceOf[Unit])
  }

  test ("add should insert a TagTheme"){
    val tagTheme:Topic=Topic(None,"topic1","url1")
    val addResult:Int=Await.result(topicRepository.add(tagTheme),10.seconds)
    assertEquals(addResult,1)
    val addedRow: Option[Topic]=Await.result(topicRepository.getById(1),10.seconds)
    addedRow match {
      case Some(addedRecord)=> assertEquals(addedRecord.id,Some(1))
      case None => fail("No record found with id == 1 after insert Tag ")
    }
    println("info :",addedRow)
    val addResultSecond:Int=Await.result(topicRepository.add(Topic(None,"topic1","url2")),10.seconds)
    assertEquals(addResultSecond,2)
  }
  test ("add should not insert a Topic with similar Url"){
    val topic:Topic=Topic(None,"topic1","url1")
    val topic2:Topic=Topic(None,"topic2","url1")
    val addResult:Int=Await.result(topicRepository.add(topic),10.seconds)
    val addResultSecond:Try[Int]=Try(Await.result(topicRepository.add(topic2),10.seconds))
    addResultSecond match {
      case Success(value)=>fail("Expected to topic with similar url not added")
      case Failure(exception)=>
    }
  }

  test( "Update should modify an existing Topic"){
    val initialTopic = Topic(None, "Title1", "url1")
    val insertedId = Await.result(topicRepository.add(initialTopic), 10.seconds)
    val updatedTopic = Topic(Some(insertedId), "TitleUpdated", initialTopic.url)
    val updateResult = Await.result(topicRepository.update(updatedTopic), 10.seconds)
    assertEquals(updateResult, 1)
    val retrievedUpdatedTopic:Option[Topic] = Await.result(topicRepository.getById(insertedId), 10.seconds)
    assertEquals(retrievedUpdatedTopic.map(_.url), Some(initialTopic.url))
    assertEquals(retrievedUpdatedTopic.map(_.title), Some(updatedTopic.title))
  }
  test("Delete should delete an existing Tag"){
    val initialTopic = Topic(None, "Title1", "url1")
    val insertedId = Await.result(topicRepository.add(initialTopic), 10.seconds)
    val deleteResult=Await.result(topicRepository.deleteBy(insertedId),10.seconds)
    assertEquals(deleteResult,1)
    val deletedRecord:Option[Topic]=Await.result(topicRepository.getById(insertedId),10.seconds)
    deletedRecord match {
      case Some(record)=> fail("Record still found after delete")
      case None =>
    }
  }

}
