package models

import slick.jdbc.PostgresProfile.api._
import slick.jdbc.JdbcProfile


object DatabaseConfig {
  val db = Database.forConfig("postgres")
  val profile:JdbcProfile=slick.jdbc.PostgresProfile
}
