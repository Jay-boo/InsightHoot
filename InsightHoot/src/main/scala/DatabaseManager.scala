//import scala.concurrent.{Await, Future}
//import scala.concurrent.duration._
//import slick.jdbc.PostgresProfile.api._
//
//import scala.util.{Failure, Success}
//import scala.concurrent.ExecutionContext.Implicits.global
//
//
//
//case class Topic(id:Option[Int],title:String,url:String)
//case class Message(id:Option[Int],content:String,topic_id:Int,tag_id:Int)
//
//class Tags(tag: Tag) extends Table[TagTheme](tag, "tags") {
//  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
//  def label = column[String]("label")
//  def theme = column[String]("theme")
//  def * = (id.?, label,  theme).mapTo[TagTheme]
//}
//
//
//class Topics(tag:Tag) extends Table[Topic](tag,"topics"){
//  def id = column[Int]("id", O.PrimaryKey,O.AutoInc )
//  def title = column[String]("title")
//  def url = column[String]("url")
//  def * = (id.?, title,url).mapTo[Topic]
//}
//class Messages(tag:Tag) extends Table[Message](tag,"messages") {
//  def id = column[Int]("id", O.PrimaryKey,O.AutoInc )
//  def content = column[String]("content")
//  def topic_id = column[Int]("topic_id")
//  def tag_id = column[Int]("tag_id")
//  def * = (id.?, content,topic_id,tag_id).mapTo[Message]
//  def fk_topic=foreignKey("fk_topic",topic_id,TableQuery[Topics])(_.id,onDelete = ForeignKeyAction.Cascade)
//  def fk_tag=foreignKey("fk_tag",tag_id,TableQuery[Tags])(_.id,onDelete = ForeignKeyAction.Cascade)
//}
//
//
//object DataManager{
//  val db=Database.forConfig("postgres")
//  val tags = TableQuery[Tags]
//  val topics=TableQuery[Topics]
//  val messages=TableQuery[Messages]
//  def setupSchemas(): Unit = {
//    val setup=DBIO.seq(
//      (tags.schema++ topics.schema++messages.schema ).createIfNotExists
//    )
//    Await.result(
//      db.run(setup),
//      20.seconds
//    )
//  }
//
//  def insertTag(tag: TagTheme): Future[Int] = {
//    db.run(tags+=tag)
//  }
//  def insertTopic(topic: Topic): Future[Int] = {
//    db.run(topics+=topic)
//  }
//  def addMessages(message: Message): Future[Int] = {
//    val topicExists:Future[Boolean] = db.run(topics.filter(_.id === message.topic_id).exists.result)
//    val tagExists:Future[Boolean] = db.run(tags.filter(_.id === message.tag_id).exists.result)
//    val bothExist:Future[Boolean]=for {
//      topicExist <- topicExists
//      tagExist <- tagExists
//    }yield topicExist && tagExist
//
//
//    println("----addMessages")
//    bothExist.flatMap( (exist)=>{
//      if (exist){
//        db.run(messages+=message)
//      }else{
//        Future.failed(new Exception(s"Topic ${message.topic_id} or Tag ${message.tag_id} doesn't exist"))
//      }
//
//    })
////    println("topicExist",topicExists)
//  }
//
//  def main(ags:Array[String]): Unit = {
//    setupSchemas()
//    println("hello")
////    Await.result(insertTag(TagTheme(None,"testBisNewNewsssGANAG","3")),20.seconds)
////    Await.result(insertTopic(Topic(None,"testBisNewNewsssGANAG","3")),20.seconds)
////    Await.result(addMessages(Message(None,"Lorem Ipsum",4,5)),20.seconds)
//    Await.result(addMessages(Message(None,"lorem",3,4)),20.seconds)
//    println("hello")
//
//  }
//
//}