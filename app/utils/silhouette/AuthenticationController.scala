package utils.silhouette

import global.GlobalAppSettings
import models.User
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import play.api.i18n.I18nSupport
import play.api.mvc.{Request, Result, AnyContent}
import utils.services.MenuService
import utils.services.data.Menu
import scala.concurrent.ExecutionContext.Implicits._

import scala.concurrent.Future

trait AuthenticationController extends Silhouette[User, CookieAuthenticator] with I18nSupport {
  def env: AuthenticationEnvironment
  def withMenus(menuService: MenuService)(body: (Request[AnyContent], List[Menu]) => Future[Result]): Request[AnyContent] => Future[Result] = {
    r => menuService.getMenus(GlobalAppSettings.service).flatMap(menus => body(r, menus))
  }
  def withMenusSecured(menuService: MenuService)(body: (SecuredRequest[AnyContent], List[Menu]) => Future[Result]): SecuredRequest[AnyContent] => Future[Result] = {
    r => menuService.getMenus(GlobalAppSettings.service).flatMap(menus => body(r, menus))
  }
  def withMenusUserAware(menuService: MenuService)(body: (UserAwareRequest[AnyContent], List[Menu]) => Future[Result]): UserAwareRequest[AnyContent] => Future[Result] = {
    r => menuService.getMenus(GlobalAppSettings.service).flatMap(menus => body(r, menus))
  }
  implicit def securedRequest2User[A](implicit request: SecuredRequest[A]): User = request.identity
  implicit def userAwareRequest2UserOpt[A](implicit request: UserAwareRequest[A]): Option[User] = request.identity
}