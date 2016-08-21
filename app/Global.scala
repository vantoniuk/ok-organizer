import global.GlobalAppSettings
import play.api._
import play.api.Play.current

object Global extends GlobalSettings {

  override def onStart(app: Application): Unit = {
    val startingMessage =
      List(
        s"Starting ${Play.application.configuration.getString("application.name").get} application with settings:",
        s"service ID: ${GlobalAppSettings.service.id}",
        s"default page preview img: ${GlobalAppSettings.defaultPreview}",
        s"page number limit: ${GlobalAppSettings.pageLimit}",
        s"page capacity: ${GlobalAppSettings.pageCapacity}",
        ""
      )

    Logger.logger.info(startingMessage.mkString("\n", "\n\t", "\n"))
  }

}
