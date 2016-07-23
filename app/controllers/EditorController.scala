package controllers

import javax.inject.Inject

import global.Global
import models.UserRole
import models.db.DAOProvider
import models.note.NodeId
import org.joda.time.DateTime
import play.api.i18n.MessagesApi
import utils.services.MenuService
import utils.services.data.Menu
import utils.silhouette._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import play.api.data._
import play.api.data.Forms._

object EditorForms {
  case class MenuForValue(id: NodeId, order: Int, url: String, title: String)
  val menuForm = Form(mapping(
      "id" -> number.transform[NodeId](NodeId.apply, _.id),
      "order" -> number,
      "url" -> text,
      "title" -> text
    )(MenuForValue.apply)(MenuForValue.unapply)
  )
}

class EditorController @Inject()(val env: AuthenticationEnvironment, val messagesApi: MessagesApi, val daoProvider: DAOProvider, menuService: MenuService) extends AuthenticationController {
  import EditorForms._

  def menuEditor = SecuredAction(ForRole(UserRole.ADMIN)).async { implicit request =>

    menuService.getMenus(Global.service).map { menus =>
      Ok(views.html.editor.menu(menus.sortBy(_.order)))
    }
  }

  def addMenu() = SecuredAction(ForRole(UserRole.ADMIN)).async { implicit request =>
    val menuFormValue = menuForm.bindFromRequest().get
    menuService.addMenu(
      Menu(
        menuFormValue.id,
        None,
        menuFormValue.title,
        menuFormValue.url,
        None,
        menuFormValue.order,
        request.identity,
        DateTime.now
      ),
      Global.service
    )

    Future.successful(Ok(""))
  }

  def updateMenu() = SecuredAction(ForRole(UserRole.ADMIN)).async { implicit request =>
    val menuFormValue = menuForm.bindFromRequest().get
    menuService.updateMenu(
      Menu(
        menuFormValue.id,
        None,
        menuFormValue.title,
        menuFormValue.url,
        None,
        menuFormValue.order,
        request.identity,
        DateTime.now
      ),
      Global.service
    )
    Future.successful(Ok(""))
  }
}
