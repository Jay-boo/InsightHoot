package models.db

import slick.jdbc.JdbcProfile
import models.entities.Message

class MessageComponent(profile:JdbcProfile,tagComponent: TagComponent) {
  import profile.api._


  class Messages(tag:Tag) extends Table[Message](tag,"messages") {
    def id = column[Int]("id", O.PrimaryKey,O.AutoInc )
    def content = column[String]("content")
    def topic_id = column[Int]("topic_id")
    def tag_id = column[Int]("tag_id")
    def * = (id.?, content,topic_id,tag_id).mapTo[Message]
//    def fk_topic=foreignKey("fk_topic",topic_id,TableQuery[Topics])(_.id,onDelete = ForeignKeyAction.Cascade)
    def fk_tag=foreignKey(
      "fk_tag",
      tag_id,
      tagComponent.tagQuery)(_.id,onDelete = ForeignKeyAction.Cascade)
  }
  val messagesQuery:TableQuery[Messages]=TableQuery[Messages]

}
