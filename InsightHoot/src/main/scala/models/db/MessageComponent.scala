package models.db

import slick.jdbc.JdbcProfile
import models.entities.Message

class MessageComponent(val profile:JdbcProfile,val topicComponent: TopicComponent) {
  import profile.api._


  class Messages(tag:Tag) extends Table[Message](tag,"messages") {
    def id = column[Int]("id", O.PrimaryKey,O.AutoInc )
    def content = column[String]("content")
    def topic_id = column[Int]("topic_id")
//    def tag_id = column[Int]("tag_id")
    def * = (id.?, content,topic_id).mapTo[Message]
    def fk_topic=foreignKey("fk_topic",topic_id,topicComponent.topicQuery)(_.id,onDelete = ForeignKeyAction.Cascade)
  }
  val messagesQuery:TableQuery[Messages]=TableQuery[Messages]

}
