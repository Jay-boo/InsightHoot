package models

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

trait DatabaseConfig{
  val profile:JdbcProfile
  val db:Database
}

object MainDatabaseConfig extends DatabaseConfig {
  override val db = Database.forConfig("postgres")
  override val profile:JdbcProfile=slick.jdbc.PostgresProfile
}
