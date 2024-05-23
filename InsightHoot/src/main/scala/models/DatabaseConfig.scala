package models

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

trait DatabaseConfig{
  @transient val profile:JdbcProfile
  val db:Database
}

object MainDatabaseConfig extends DatabaseConfig {
  override val db = Database.forConfig("postgreskub")
  override val profile:JdbcProfile=slick.jdbc.PostgresProfile
}
object LocalDatabaseConfig extends DatabaseConfig with Serializable {
  override val db = Database.forConfig("postgres")
  override val profile:JdbcProfile=slick.jdbc.PostgresProfile
}
