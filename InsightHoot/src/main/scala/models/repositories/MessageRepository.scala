package models.repositories

import models.DatabaseConfig

import scala.concurrent.Future
import models.entities.Message
import models.repositories.TopicRepositoryComponent
import models.db.{MessageComponent, TagComponent, TopicComponent}
import slick.jdbc.JdbcProfile
import scala.concurrent.ExecutionContext.Implicits.global

trait MessageRepositoryComponent{
  def beforeEach():Future[Unit]
  def afterEach():Future[Unit]
  def add(message:Message):Future[Int]
  def deleteById(messageId:Int):Future[Int]
  def getById(messageId:Int):Future[Option[Message]]
  def all(limit:Int,offset:Int):Future[Seq[Message]]
}


class MessageRepository(val databaseConfig:DatabaseConfig,val topicRepository:TopicRepository) extends MessageRepositoryComponent {
  val profile:JdbcProfile=databaseConfig.profile
  import profile.api._
  val table:MessageComponent= new MessageComponent(databaseConfig.profile,topicRepository.table)
  val db:Database=databaseConfig.db
  import table.messagesQuery

  override def beforeEach(): Future[Unit] = {
    db.run(
      messagesQuery.schema.createIfNotExists
    )
  }
  override def afterEach(): Future[Unit] = {
    db.run(
      messagesQuery.schema.dropIfExists
    )
  }

  override def add(message: Message): Future[Int] = {
    import topicRepository.table.topicQuery
    val topicExists:Future[Boolean]=db.run(topicQuery.filter(_.id===message.topic_id).exists.result)


    topicExists.flatMap((exist)=>{
      if(exist){
        db.run(
          (messagesQuery returning messagesQuery.map(_.id))+=message
        )
      }else{
        Future.failed[Int](new Exception(s"Topic Id not Found in 'topics' relation"))
      }
    }
    )
  }


  override def deleteById(messageId: Int): Future[Int] = {
    db.run(messagesQuery.filter(_.id===messageId).delete)
  }

  override def getById(messageId: Int): Future[Option[Message]] = {
    db.run(
      messagesQuery.filter(_.id===messageId).result.headOption
    )
  }

  override def all(limit: Int, offset: Int): Future[Seq[Message]] = {
    db.run(
      messagesQuery.drop(offset).take(limit).result
    )
  }
}
