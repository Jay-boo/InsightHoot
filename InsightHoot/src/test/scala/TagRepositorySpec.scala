import models.db.TagComponent
import models.repositories.TagRepository

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class TagRepositorySpec extends munit.FunSuite {
  object TestDatabaseConfig{
    val profile=slick.jdbc.H2Profile
    import profile.api._
    val db= Database.forConfig("h2mem1")
  }

  class TestTagRepository extends TagRepository{
    override val table: TagComponent = new TagComponent(TestDatabaseConfig.profile)
    import TestDatabaseConfig.profile.api._
    override val db:Database= TestDatabaseConfig.db
  }
  val tagRepository=new TestTagRepository

  test("beforeAll should create schema"){
    val schemaCreated=Await.result(tagRepository.beforeAll(),10.seconds)
    assert(schemaCreated.isInstanceOf[Unit])
  }

}
