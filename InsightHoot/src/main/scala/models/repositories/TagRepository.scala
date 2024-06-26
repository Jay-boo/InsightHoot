package models.repositories

import models.DatabaseConfig
import models.db.TagComponent
import models.entities.TagTheme

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait TagRepositoryComponent{
  def beforeEach():Future[Unit]
  def afterEach():Future[Unit]
  def add(tagTheme: TagTheme):Future[Int]
  def update(tagTheme: TagTheme):Future[Int]
  def deleteBy(tagId: Int):Future[Int]
  def getById(tagId:Int):Future[Option[TagTheme]]
  def getId(label:String,theme:String):Future[Option[Int]]
  def all(limit:Int,offset:Int):Future[Seq[TagTheme]]
}

class TagRepository(val databaseConfig: DatabaseConfig)  extends  TagRepositoryComponent {
  import databaseConfig.profile.api._
  val table:TagComponent= new TagComponent(databaseConfig.profile)
  val db:Database = databaseConfig.db
  import table.tagQuery


  override def add(tagTheme: TagTheme): Future[Int] = {
    db.run((tagQuery returning tagQuery.map(_.id))+=tagTheme)
  }

  override def beforeEach(): Future[Unit] = {
    db.run(tagQuery.schema.createIfNotExists)
  }
  override def afterEach(): Future[Unit] = {
    db.run(
      tagQuery.schema.dropIfExists
    )
  }

  override def getById(tagId: Int): Future[Option[TagTheme]] = {
    db.run(
      tagQuery.filter(_.id=== tagId).result.headOption
    )
  }
  override def getId(label: String,theme:String): Future[Option[Int]] = {
    db.run(
      tagQuery.filter(x=>(x.label===label && x.theme===theme)).map(_.id).result.headOption
    )
  }

  override def all(limit: Int, offset: Int): Future[Seq[TagTheme]] = {
    db.run(
      tagQuery.drop(offset).take(limit).result
    )
  }

  override def update(tagTheme: TagTheme): Future[Int] = {
    db.run(
      tagQuery.filter(_.id===tagTheme.id).map(
        tag=>(tag.label,tag.theme)).update( (tagTheme.label,tagTheme.theme))
    )
  }

  override def deleteBy(tagId: Int): Future[Int] = {
   db.run(
     tagQuery.filter(_.id===tagId).delete
   )
  }
}
