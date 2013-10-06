package utils

import com.github.nscala_time.time.Imports._

import com.typesafe.config.ConfigFactory
import play.api.{Play, Mode}
import play.api.Play.current

object AppConfig {
  private object internal {
    val config = ConfigFactory.load()
    val confPrefix = Play.mode.toString.toLowerCase

    def getString(string: String) = config.getString(confPrefix + "." + string)

    val dateStringFormat = getString("date.format")

    val dbUri = (getString("user_db.host") :: getString("user_db.port") :: Nil) mkString ":"
    val usersDbName = getString("user_db.name")

  }

  val usersDbUri = internal.dbUri + "/" + internal.usersDbName
  val dateFormat = DateTimeFormat.forPattern(internal.dateStringFormat)

}
