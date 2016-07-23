package controllers

import javax.inject.Inject

import models.UserRole
import models.db.DAOProvider
import models.db.DB.PostgresDAOProvider
import play.api.i18n.MessagesApi
import play.api.mvc._
import utils.silhouette._

import scala.concurrent.Future

class Editor @Inject() (val env: AuthenticationEnvironment, val messagesApi: MessagesApi, val daoProvider: DAOProvider) extends AuthenticationController {
  def menuEditor = SecuredAction(ForRole(UserRole.ADMIN)).async { implicit request =>
    Future.successful(Ok(views.html.editor.menu()))
  }
}
