package models.entities

import java.sql.Timestamp


case class Message(id:Option[Int], date:Timestamp, content:String, title:String, link:String, topic_id:Int)
