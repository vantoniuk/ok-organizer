package controllers

import javax.inject.Inject

import global.GlobalAppSettings
import models.{User, UserRole}
import models.db.DAOProvider
import models.note.{NodeType, NodeId}
import org.joda.time.{DateTimeZone, DateTime}
import play.api.http.Writeable
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import utils.services.{PageService, MenuService}
import utils.services.data.{PageRecord, Page, Menu}
import utils.silhouette._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import play.api.data._
import play.api.data.Forms._

class DisplayController @Inject()(val env: AuthenticationEnvironment, val messagesApi: MessagesApi, val daoProvider: DAOProvider, menuService: MenuService, pageService: PageService) extends AuthenticationController {
  import EditorForms._

  private def abstractRead[T, C](read : => Future[List[T]], toContent: List[T] => C)(implicit writeable: Writeable[C]): Future[Result] = {
    read.map(toContent).map(Ok(_))
  }

  def records(pageId: Int, subpage: Int) = Action async withMenusAndUser(menuService) { (request, menus, user) =>
    implicit val (m,r,u) = (menus, request,user)
    for {
      pageOpt <- pageService.getPage(NodeId(pageId))
      records <- pageService.getRecords(NodeId(pageId), subpage)
    } yield {
      pageOpt match {
        case None => NotFound("not found page with id " + pageId)
        case Some(page) => Ok(views.html.pageContent(page, records))
      }
    }
  }
}
