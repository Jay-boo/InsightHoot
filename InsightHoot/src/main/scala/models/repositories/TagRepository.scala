package models.repositories

import models.DatabaseConfig
import models.db.TagComponent
import models.entities.TagTheme

import scala.concurrent.Future

trait TagRepositoryComponent{
  def beforeAll():Future[Unit]
  def add(tagTheme: TagTheme):Future[Int]
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
}
