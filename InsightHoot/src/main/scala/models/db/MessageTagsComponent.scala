package models.db

import slick.jdbc.JdbcProfile
import models.entities.MessageTag
import slick.lifted.ProvenShape

class MessageTagsComponent(val profile:JdbcProfile,val tagComponent: TagComponent,val messageComponent: MessageComponent) {
  import profile.api._
  class MessageTags(tag: Tag) extends Table[MessageTag](tag,"message_tags"){
    def messageId= column[Int]("messageId")
    def tagId=column[Int]("tagId")

    override def * : ProvenShape[MessageTag] =(messageId,tagId).mapTo[MessageTag]
    def fk_tag=foreignKey("fk_tag",tagId,tagComponent.tagQuery)(_.id,onDelete = ForeignKeyAction.Cascade)
    def fk_message=foreignKey("fk_message",messageId,messageComponent.messagesQuery)(_.id,onDelete = ForeignKeyAction.Cascade)
  }
  val messageTagsQuery:TableQuery[MessageTags]=TableQuery[MessageTags]
}
