package models.db

import play.api.Play.current
import play.api.db.{DB => PlayDB}
import slick.driver.PostgresDriver.api._

object DB {

  val database = Database.forConfig("database")

}
