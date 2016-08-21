package controllers

import javax.inject.Inject

import global.GlobalAppSettings
import models.{User, UserRole}
import models.db.DAOProvider
import models.note.NodeId
import org.joda.time.{DateTimeZone, DateTime}
import play.api.i18n.MessagesApi
import utils.services.{PageService, MenuService}
import utils.services.data.{PageRecord, Page, Menu}
import utils.silhouette._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import play.api.data._
import play.api.data.Forms._

object EditorForms {
  case class MenuForValue(id: NodeId, order: Int, url: String, title: String)  {
    def toMenu(author: User): Menu = {
      Menu(
        id = id,
        parentId = None,
        title = title,
        url = url,
        icon = None,
        order = order,
        author = author,
        created = DateTime.now
      )
    }
  }
  val menuForm = Form(mapping(
      "id" -> number.transform[NodeId](NodeId.apply, _.id),
      "order" -> number,
      "url" -> text,
      "title" -> text
    )(MenuForValue.apply)(MenuForValue.unapply)
  )
  def pageForm(user: User) = Form(mapping(
    "id" -> number.transform[NodeId](NodeId.apply, _.id),
    "parent_id" -> optional(number.transform[NodeId](NodeId.apply, _.id)),
    "title" -> text,
    "content" -> text,
    "preview" -> text,
    "order" -> ignored[Int](GlobalAppSettings.pageLimit),
    "author" -> ignored[User](user),
    "created" -> longNumber.transform[DateTime](l => new DateTime(l, DateTimeZone.UTC), _.getMillis)
  )(Page.apply)(Page.unapply)
  )

  val pageRecordForm = Form(mapping(
    "id" -> number.transform[NodeId](NodeId.apply, _.id),
    "container" -> number.transform[NodeId](NodeId.apply, _.id),
    "title" -> text,
    "content" -> text,
    "icon" -> optional(text),
    "order" -> ignored[Int](GlobalAppSettings.pageLimit),
    "created" -> longNumber.transform[DateTime](l => new DateTime(l, DateTimeZone.UTC), _.getMillis)
  )(PageRecord.apply)(PageRecord.unapply))
}

class EditorController @Inject()(val env: AuthenticationEnvironment, val messagesApi: MessagesApi, val daoProvider: DAOProvider, menuService: MenuService, pageService: PageService) extends AuthenticationController {
  import EditorForms._

  def menuEditor = SecuredAction(ForRole(UserRole.ADMIN)).async { implicit request =>

    menuService.getMenus(GlobalAppSettings.service).map { menus =>
      Ok(views.html.editor.menu(menus.sortBy(_.order)))
    }
  }

  def addMenu() = SecuredAction(ForRole(UserRole.ADMIN)).async { implicit request =>
    val menuFormValue = menuForm.bindFromRequest().get
    menuService.addMenu(menuFormValue.toMenu(request.identity), GlobalAppSettings.service).map{ _ =>
      Ok("success")
    }
  }

  def updateMenu() = SecuredAction(ForRole(UserRole.ADMIN)).async { implicit request =>
    val menuFormValue = menuForm.bindFromRequest().get
    menuService.updateMenu(menuFormValue.toMenu(request.identity), GlobalAppSettings.service).map{ _ =>
      Ok("success")
    }
  }

  def deleteMenu(id: Int) =  SecuredAction(ForRole(UserRole.ADMIN)).async { implicit request =>
    menuService.delete(NodeId(id)).map{ result =>
      Ok(result.toString)
    }
  }


}
