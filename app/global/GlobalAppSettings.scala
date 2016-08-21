package global

import models.ServiceId
import play.api.Play.current
import play.api._

object GlobalAppSettings {
  val service = ServiceId(Play.application.configuration.getInt("application.service").get)
  val defaultPreview = Play.application.configuration.getString("application.page.preview").get
  val pageLimit = Play.application.configuration.getInt("application.page.limit").get
  val pageCapacity = Play.application.configuration.getInt("application.page.capacity").get
}
