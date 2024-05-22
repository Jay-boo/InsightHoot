package models.db

import slick.jdbc.JdbcProfile
import models.entities.Message

import java.sql.Timestamp

class MessageComponent(val profile:JdbcProfile,val topicComponent: TopicComponent) {
  import profile.api._


  class Messages(tag:Tag) extends Table[Message](tag,"messages") {
    def id = column[Int]("id", O.PrimaryKey,O.AutoInc )
    def date = column[Timestamp]("date")
    def content = column[String]("content")
    def title=column[String]("title")
    def link=column[String]("link")
    def topic_id = column[Int]("topic_id")
//    def tag_id = column[Int]("tag_id")
    def * = (id.?,date, content,title,link,topic_id).mapTo[Message]
    def fk_topic=foreignKey("fk_topic",topic_id,topicComponent.topicQuery)(_.id,onDelete = ForeignKeyAction.Cascade)
  }
  val messagesQuery:TableQuery[Messages]=TableQuery[Messages]

}
