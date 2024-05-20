import models.DatabaseConfig
import slick.jdbc.JdbcBackend.Database

object TestDatabaseConfig extends DatabaseConfig {
  val profile=slick.jdbc.H2Profile
  val db= Database.forConfig("h2mem1")
}
