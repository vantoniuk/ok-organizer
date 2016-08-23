package controllers

import javax.inject.Inject

import global.GlobalAppSettings
import models._
import play.api._
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc._
import utils.services.MenuService
import utils.silhouette._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

class Application @Inject() (val env: AuthenticationEnvironment, val messagesApi: MessagesApi, menuService: MenuService) extends AuthenticationController {
  def getMenus = menuService.getMenus(GlobalAppSettings.service)

  def index = UserAwareAction.async { withMenusUserAware(menuService){ (request, menus) =>
    implicit val (r,m) = (request, menus)
    getMenus.map(menus => Ok(views.html.index()))
  }}


  def myAccount = SecuredAction async withMenusSecured(menuService){ (request, menus) =>
    implicit val (r,m) = (request, menus)
    Future.successful(Ok(views.html.myAccount()))
  }

  // REQUIRED ROLES: serviceA (or master)
  def serviceA = SecuredAction(ForRole(UserRole.USER)) async withMenusSecured(menuService){ (request, menus) =>
    implicit val (r,m) = (request, menus)
    Future.successful(Ok(views.html.serviceA()))
  }

  // REQUIRED ROLES: master
  def settings = SecuredAction(ForRole(UserRole.MASTER)) async withMenusSecured(menuService){ (request, menus) =>
    implicit val (r,m) = (request, menus)
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