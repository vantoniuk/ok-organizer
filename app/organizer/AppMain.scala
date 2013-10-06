package organizer

import utils.imports._
import utils.AppConfig

object AppMain {

  val dbClient = CouchDbClient(AppConfig.usersDbUri)

}
