package models.repositories

import models.DatabaseConfig

import scala.concurrent.Future
import models.entities.Topic
import models.db.TopicComponent

trait TopicRepositoryComponent{
  def beforeEach():Future[Unit]
  def afterEach():Future[Unit]
  def add(topic:Topic):Future[Int]
  def update(topic:Topic):Future[Int]
  def deleteBy(topicId:Int):Future[Int]
  def getById(topicId:Int):Future[Option[Topic]]
  def getByUrl(topicUrl:String):Future[Option[Topic]]
  def all(limit:Int,offset:Int):Future[Seq[Topic]]
}


class TopicRepository(val databaseConfig: DatabaseConfig) extends TopicRepositoryComponent{
  import databaseConfig.profile.api._
  val table:TopicComponent=new TopicComponent(databaseConfig.profile)
  val db: Database = databaseConfig.db
  import table.topicQuery

  override def beforeEach(): Future[Unit] ={
    db.run(
      topicQuery.schema.createIfNotExists
    )
  }
  override def afterEach(): Future[Unit] = {
    db.run(
      topicQuery.schema.dropIfExists
    )
  }

  override def add(topic: Topic): Future[Int] = {
    db.run(
      (topicQuery returning topicQuery.map(_.id))+=topic
    )
  }

  override def update(topic: Topic): Future[Int] = {
    db.run(
      topicQuery.filter(_.id===topic.id).map(t=>(t.name,t.url)).update((topic.name,topic.url))
    )
  }

  override def deleteBy(topicId: Int): Future[Int] = {
    db.run(
      topicQuery.filter(_.id===topicId).delete
    )

  }

  override def getById(topicId: Int): Future[Option[Topic]] = {
    db.run(
      topicQuery.filter(_.id===topicId).result.headOption
    )
  }
  override def getByUrl(topicUrl: String): Future[Option[Topic]] = {
    db.run(
      topicQuery.filter(_.url===topicUrl).result.headOption
    )
  }

  override def all(limit: Int, offset: Int): Future[Seq[Topic]] = {
    db.run(
      topicQuery.drop(offset).take(limit).result
    )
  }
}