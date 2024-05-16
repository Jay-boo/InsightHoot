package models.repositories

import models.DatabaseConfig
import models.db.TagComponent
import models.entities.TagTheme

import scala.concurrent.Future

trait TagRepositoryComponent{
  def beforeAll():Future[Unit]
  def add(tagTheme: TagTheme):Future[Int]
  def update(tagTheme: TagTheme):Future[Int]
  def deleteBy(tagId: Int):Future[Int]
  def getById(tagId:Int):Future[Option[TagTheme]]
  def all(limit:Int,offset:Int):Future[Seq[TagTheme]]
}

class TagRepository  extends  TagRepositoryComponent {
  import DatabaseConfig.profile.api._
  val table:TagComponent= new TagComponent(DatabaseConfig.profile)
  val db:Database = DatabaseConfig.db
  import table.tagQuery


  override def add(tagTheme: TagTheme): Future[Int] = {
    db.run(tagQuery+=tagTheme)
  }

  override def beforeAll(): Future[Unit] = {
    db.run(tagQuery.schema.createIfNotExists)
  }

  override def getById(tagId: Int): Future[Option[TagTheme]] = {
    db.run(
      tagQuery.filter(_.id=== tagId).result.headOption
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
