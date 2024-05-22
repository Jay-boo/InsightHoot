import models.DatabaseConfig
import models.entities.{Message, MessageTag}
import models.db.MessageTagsComponent
import slick.jdbc.JdbcProfile
import models.db.{MessageComponent, TagComponent}
import models.repositories.{MessageRepository, TagRepository, TopicRepository}

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

trait MessageTagsRepositoryComponent{
  def beforeEach():Future[Unit]
  def afterEach():Future[Unit]
  def add(messageTag:MessageTag):Future[Int]
  def all(limit:Int,offset:Int):Future[Seq[MessageTag]]
}


class MessageTagsRepository(val databaseConfig:DatabaseConfig,val tagRepository: TagRepository, val messageRepository: MessageRepository) extends MessageTagsRepositoryComponent {
  val profile:JdbcProfile=databaseConfig.profile
  import profile.api._
  val table:MessageTagsComponent= new MessageTagsComponent(databaseConfig.profile, tagRepository.table, messageRepository.table)
  val db:Database=databaseConfig.db
  import table.messageTagsQuery

  override def beforeEach(): Future[Unit] = {
    db.run(
      messageTagsQuery.schema.createIfNotExists
    )
  }
  override def afterEach(): Future[Unit] = {
    db.run(
      messageTagsQuery.schema.dropIfExists
    )
  }


  override def add(messageTag: MessageTag): Future[Int] = {
    import tagRepository.table.tagQuery
    import messageRepository.table.messagesQuery
    val tagExist:Future[Boolean]=db.run(tagQuery.filter(_.id===messageTag.tagId).exists.result)
    val messageExist:Future[Boolean]=db.run(messagesQuery.filter(_.id===messageTag.msgId).exists.result)
    val bothExist:Future[Boolean]=for{
      tag_exist<-tagExist
      msg_exist <- messageExist
    }yield tag_exist && msg_exist
    bothExist.flatMap(
      (exist)=>{
        if(exist){db.run((messageTagsQuery+=messageTag))}
        else{
          Future.failed[Int](new Exception(s"Tag Id ${messageTag.tagId} / Msg Id ${messageTag.msgId} not found in messages/tags"))

        }
      }
    )
  }



  override def all(limit: Int, offset: Int): Future[Seq[MessageTag]] = {
    db.run(
      messageTagsQuery.drop(offset).take(limit).result
    )
  }
}
