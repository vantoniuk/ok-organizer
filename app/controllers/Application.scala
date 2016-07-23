package controllers

import javax.inject.Inject

import models._
import play.api._
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc._
import utils.silhouette._

import scala.concurrent.Future

class Application @Inject() (val env: AuthenticationEnvironment, val messagesApi: MessagesApi) extends AuthenticationController {

  def index = UserAwareAction.async { implicit request =>
    Future.successful(Ok(views.html.index()))
  }

  def myAccount = SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.myAccount()))
  }

  // REQUIRED ROLES: serviceA (or master)
  def serviceA = SecuredAction(ForRole(UserRole.USER)).async { implicit request =>
    Future.successful(Ok(views.html.serviceA()))
  }

  // REQUIRED ROLES: master
  def settings = SecuredAction(ForRole(UserRole.MASTER)).async { implicit request =>
    Future.successful(Ok(views.html.settings()))
  }

  def selectLang(lang: String) = Action { implicit request =>
    Logger.logger.debug("Change user lang to : " + lang)
    request.headers.get(REFERER).map { referer =>
      Redirect(referer).withLang(Lang(lang))
    }.getOrElse {
      Redirect(routes.Application.index).withLang(Lang(lang))
    }
  }

}