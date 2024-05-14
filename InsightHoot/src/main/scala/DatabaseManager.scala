import Tagger.getDocumentTags
import slick.jdbc.PostgresProfile.api._
import java.time.Duration
import scala.concurrent.Await
import scala.concurrent.duration._


case class Topic(id:Int,title:String)
case class TagTheme (id:Int,label:String,theme:String)
case class Message(id:Int,content:String,topic_id:Int,tag_id:Int)

class Messages(tag:Tag) extends Table[Message](tag,"messages") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def content = column[String]("title")
  def topic_id = column[Int]("topic_id")
  def tag_id = column[Int]("tag_id")

  def * = (id, content, topic_id, tag_id) <> (Message.tupled, Message.unapply)
}

class Topics(tag:Tag) extends Table[Topic](tag,"topics") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def title = column[String]("title")
  def * = (id, title) <> (Topic.tupled, Topic.unapply)
}

class Tags(tag:Tag) extends Table[TagTheme](tag,"tags") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def label = column[String]("label")
  def theme = column[String]("theme")
  def * = (id, label,theme) <> (TagTheme.tupled, TagTheme.unapply)
}


object DatabaseManager {
  val db=Database.forConfig("mydb")
  val messages=TableQuery[Messages]
  val topics=TableQuery[Topics]
  val tags=TableQuery[Tags]

  def createSchema(): Unit = {
    val setup=DBIO.seq(
      (messages.schema ++ topics.schema ++ tags.schema).create
    )
    Await.result(db.run(setup),40.seconds)
//    db.run(messages.schema.create)
//    db.run(tags.schema.create)
  }

  def insertTopic(topic: Topic): Unit = {
    Await.result(db.run(topics+=topic),30.seconds)
  }
//  def getAllUsers(): Unit = {
//    db.run(users.result)
//  }

  def main(args: Array[String]): Unit = {
//    createSchema()
    insertTopic(Topic(29,"exampletopic"))
  }
}
