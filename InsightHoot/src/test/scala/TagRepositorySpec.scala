import models.db.TagComponent
import models.entities.TagTheme
import models.repositories.TagRepository

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.Try

class TagRepositorySpec extends munit.FunSuite {


  val tagRepository=new TagRepository(TestDatabaseConfig)
  override def beforeEach(context:BeforeEach): Unit = {
    Await.result(tagRepository.beforeEach(),10.seconds)
  }

  override def afterEach(context: AfterEach): Unit = {
    Await.result(tagRepository.afterEach(),10.seconds)
}



  test ("add should insert a TagTheme"){
    val tagTheme:TagTheme=TagTheme(None,"Tag1","AI")
    val addResult:Int=Await.result(tagRepository.add(tagTheme),10.seconds)
    assertEquals(addResult,1)
    val addedRow: Option[TagTheme]=Await.result(tagRepository.getById(1),10.seconds)
    addedRow match {
      case Some(addedRecord)=> assertEquals(addedRecord.id,Some(1))
      case None => fail("No record found with id == 1 after insert Tag ")
    }
    println("info :",addedRow)
    val addResultSecond:Int=Await.result(tagRepository.add(TagTheme(None,"Tag2","AI")),10.seconds)
    assertEquals(addResultSecond,2)
  }
  test ("add should raise exception when inserting a TagTheme if (tag, theme) already exist "){
    val tagTheme:TagTheme=TagTheme(None,"Tag1","AI")
    val addResult:Int=Await.result(tagRepository.add(tagTheme),10.seconds)
    try{
      val addedSecondRow=Await.result(tagRepository.add(tagTheme), 10.seconds)

    }catch {
      case e:org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException=>{println("Expected Exception Raised")}
      case _=> fail("Exception waited iw JdbcSQLIntegrityConstrintViolationException")
    }
  }


  test( "Update should modify an existing Tag"){
    val initialTagTheme = TagTheme(None, "Tag1", "AI")
    val insertedId = Await.result(tagRepository.add(initialTagTheme), 10.seconds)
    val updatedTagTheme = TagTheme(Some(insertedId), "UpdatedTag", "ML")
    val updateResult = Await.result(tagRepository.update(updatedTagTheme), 10.seconds)
    assertEquals(updateResult, 1)
    val retrievedUpdatedTagTheme:Option[TagTheme] = Await.result(tagRepository.getById(insertedId), 10.seconds)
    assertEquals(retrievedUpdatedTagTheme.map(_.label), Some("UpdatedTag"))
    assertEquals(retrievedUpdatedTagTheme.map(_.theme), Some("ML"))
  }

  test("Delete should delete an existing Tag"){
    val initialTagTheme = TagTheme(None, "Tag1", "AI")
    val insertedId = Await.result(tagRepository.add(initialTagTheme), 10.seconds)
    val deleteResult=Await.result(tagRepository.deleteBy(insertedId),10.seconds)
    assertEquals(deleteResult,1)
    val deletedRecord:Option[TagTheme]=Await.result(tagRepository.getById(insertedId),10.seconds)
    deletedRecord match {
      case Some(record)=> fail("Record still found after delete")
      case None =>
    }
  }
  test("Get Id should return the id of  existing Tag"){
    val initialTagTheme = TagTheme(None, "Tag1", "AI")
    Await.result(tagRepository.add(initialTagTheme), 10.seconds)
    val tagId:Int=Await.result(tagRepository.getId(initialTagTheme.label,initialTagTheme.theme),10.seconds).getOrElse(fail("Expected to find Tag"))
    assertEquals(tagId,1)
  }


}
