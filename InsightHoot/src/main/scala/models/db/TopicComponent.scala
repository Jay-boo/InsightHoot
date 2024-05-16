package models.db
import slick.jdbc.JdbcProfile
import models.entities.Topic



class TopicComponent(val profile:JdbcProfile){
  import profile.api._

  class Topics(tag:Tag) extends Table[Topic](tag,"topics"){
    def id = column[Int]("id", O.PrimaryKey,O.AutoInc )
    def title = column[String]("title")
    def url = column[String]("url")
    def * = (id.?, title,url).mapTo[Topic]
  }
  val topicQuery:TableQuery[Topics]=TableQuery[Topics]

}
