import scala.concurrent.Future
import models.entities.Message
import models.db.{MessageComponent, TagComponent, TopicComponent}
import slick.jdbc.JdbcProfile

trait MessageRepositoryComponent{
  def beforeAll():Future[Unit]
  def add(message:Message):Future[Int]
  def update(message: Message):Future[Int]
  def deleteBy(messageId:Int):Future[Int]
  def getById(messageId:Int):Future[Option[Message]]
  def all(limit:Int,offset:Int):Future[Seq[Message]]
}
class MessageRepository(tagComponent:TagComponent,topicComponent:TopicComponent) extends MessageRepositoryComponent {
  val profile:JdbcProfile=tagComponent.profile
  import profile.api._
  val table:MessageComponent= new MessageComponent(models.DatabaseConfig.profile,tagComponent)
  val db:Database=models.DatabaseConfig.db
  import table.messagesQuery

  override def beforeAll(): Future[Unit] = {
    db.run(
      messagesQuery.schema.createIfNotExists
    )
  }

  override def add(message: Message): Future[Int] = {
    db.run(
      messagesQuery+=message
    )
  }

  override def update(message: Message): Future[Int] = {
    db.run(messagesQuery.filter(_.id===message.id).update(message))
  }

  override def deleteBy(messageId: Int): Future[Int] = {
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
