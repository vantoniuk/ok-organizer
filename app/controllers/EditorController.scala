package controllers

import javax.inject.Inject

import global.GlobalAppSettings
import models.{User, UserRole}
import models.db.DAOProvider
import models.note.{NodeType, NodeId}
import org.joda.time.{DateTimeZone, DateTime}
import play.api.http.Writeable
import play.api.i18n.MessagesApi
import play.api.mvc.{AnyContent, Result}
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

  def menuForm(user: User) = Form(mapping(
    "id" -> number.transform[NodeId](NodeId.apply, _.id),
    "parent_id" -> ignored[Option[NodeId]](None),
    "title" -> text,
    "url" -> text,
    "icon" -> optional(text),
    "order" -> number,
    "author" -> ignored[User](user),
    "created" -> longNumber.transform[DateTime](l => new DateTime(l, DateTimeZone.UTC), _.getMillis)
    )(Menu.apply)(Menu.unapply)
  )
  def pageForm(user: User) = Form(mapping(
    "id" -> number.transform[NodeId](NodeId.apply, _.id),
    "parent_id" -> ignored[Option[NodeId]](None),
    "title" -> text,
    "description" -> text,
    "preview_icon" -> optional(text).transform(iconOpt => iconOpt.getOrElse(Page.defaultPreview), (icon: String) => Some(icon)),
    "order" -> number,
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

  private def abstractRead[T, C](read : => Future[List[T]], toContent: List[T] => C)(implicit writeable: Writeable[C]): Future[Result] = {
    read.map(toContent).map(Ok(_))
  }

  private def abstractDBSave[T](form: Form[T], create: T => Future[T])(implicit req: SecuredRequest[AnyContent]): Future[Result] = {
    val element = form.bindFromRequest().get
    create(element).map{ _ =>
      Ok("success")
    }
  }

  def read(nodeType: String) = SecuredAction(ForRole(UserRole.ADMIN)).async { implicit request =>
    NodeType.withName(nodeType) match {
      case NodeType.MENU_NODE =>
        abstractRead(menuService.getMenus(GlobalAppSettings.service), (menus: List[Menu] )=> views.html.editor.menu(menus))
      case NodeType.PAGE_NODE =>
        abstractRead(pageService.getPages(request.identity), (pages: List[Page] )=> views.html.editor.page(pages))
    }

  }

  def create(nodeType: String) = SecuredAction(ForRole(UserRole.ADMIN)).async { implicit request =>
    NodeType.withName(nodeType) match {
      case NodeType.MENU_NODE =>
        abstractDBSave(menuForm(request.identity), (menu: Menu) => menuService.addMenu(menu, GlobalAppSettings.service))
      case NodeType.PAGE_NODE =>
        abstractDBSave(pageForm(request.identity), pageService.save)
    }
  }

  def update(nodeType: String) = SecuredAction(ForRole(UserRole.ADMIN)).async { implicit request =>
    NodeType.withName(nodeType) match {
      case NodeType.MENU_NODE =>
        abstractDBSave(menuForm(request.identity), (menu: Menu) => menuService.updateMenu(menu, GlobalAppSettings.service))
      case NodeType.PAGE_NODE =>
        abstractDBSave(pageForm(request.identity), pageService.updatePage)
    }
  }

  def delete(nodeType: String, id: Int) =  SecuredAction(ForRole(UserRole.ADMIN)).async { implicit request =>
    val resultFuture =  NodeType.withName(nodeType) match {
      case NodeType.MENU_NODE => menuService.delete(NodeId(id))
      case NodeType.PAGE_NODE => pageService.delete(NodeId(id))
      case NodeType.PAGE_PART_NODE => pageService.delete(NodeId(id))
      case NodeType.RECORD_NODE => pageService.delete(NodeId(id))
    }
    resultFuture.map{ result => Ok(result.toString) }
  }

}
