package models.db

import models.entities.TagTheme
import slick.jdbc.JdbcProfile

class TagComponent(val profile:JdbcProfile) {
  import profile.api._

  class Tags(tag:Tag) extends Table[TagTheme](tag,"tags"){
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def label = column[String]("label")
    def theme = column[String]("theme")
    def * = (id.?, label,  theme).mapTo[TagTheme]
  }
  val tagQuery:TableQuery[Tags]=TableQuery[Tags]
}
