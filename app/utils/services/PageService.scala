package utils.services

import com.google.inject.{ImplementedBy, Inject}
import global.GlobalAppSettings
import models.db.DAOProvider
import models.note.NodeId
import play.api.Logger
import utils._
import utils.services.data.{Page, PageRecord}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

@ImplementedBy(classOf[PageServiceImpl])
trait PageService {
  def getPage(id: NodeId): Future[Option[Page]]
  def getSubPages(parentId: NodeId): Future[List[Page]]
  def save(page: Page): Future[Page]
  def addRecord(record: PageRecord, pageId: NodeId): Future[Option[PageRecord]]
  def updateRecord(record: PageRecord): Future[Option[PageRecord]]
  def updatePage(page: Page): Future[Page]
}

class PageServiceImpl @Inject() (daoProvider: DAOProvider) extends PageService {
  private val nodeDAO = daoProvider.nodeDAO
  private val userDAO = daoProvider.userDAO
  private val logger = Logger.logger
  def getPage(id: NodeId): Future[Option[Page]] = {
    for {
      nodeOpt <- nodeDAO.find(id)
      userOpt <- Future.traverse(nodeOpt.toList)(node => userDAO.findById(node.author)).map(_.flatten.headOption)
    } yield {
      nodeOpt.and(userOpt).map({case (node, user) => Page(node, user)})
    }
  }

  def getSubPages(parentId: NodeId): Future[List[Page]] = {
    for {
      nodes <- nodeDAO.findSubNodes(parentId)
      users <- Future.traverse(nodes.map(_.author))(userDAO.findById).map(_.flatten)
    } yield {
      val userMap = users.map(u => u.id -> u).toMap
      for {
        node <- nodes
        user <- userMap.get(node.author)
      } yield {
        Page(node, user)
      }
    }
  }

  def save(page: Page): Future[Page] = {
    page.exec(GlobalAppSettings.service)(nodeDAO.save)
  }

  def addRecord(record: PageRecord, pageId: NodeId): Future[Option[PageRecord]] = {
    for {
      containerOpt <- resolveRecordContainer(pageId)
      _ = if(containerOpt.isEmpty) logger.error(s"failed to get/create container for page $pageId")
      rs <- Future.sequence(containerOpt.toList.map(p =>
        record.copy(container = p.id).exec(GlobalAppSettings.service, p.author.id)(nodeDAO.save)
      ))
    } yield rs.headOption
  }

  def updateRecord(pageRecord: PageRecord): Future[Option[PageRecord]] = for {
    userIdOpt <- nodeDAO.find(pageRecord.id).map(_.map(_.author))
    recordSeq <- Future.traverse(userIdOpt.toList)(userId => pageRecord.exec(GlobalAppSettings.service, userId)(nodeDAO.update))
  } yield recordSeq.headOption

  def updatePage(page: Page): Future[Page] = {
    page.exec(GlobalAppSettings.service)(nodeDAO.update)
  }

  private def resolveRecordContainer(pageId: NodeId): Future[Option[Page]] = {
    for {
      pageOpt <- getPage(pageId)
      _ = if(pageOpt.isEmpty) logger.error(s"No parent node for node $pageId")
      pages <- Future.traverse(pageOpt.toList)(getRecordContainer)
    } yield pages.headOption
  }

  private def getRecordContainer(page: Page): Future[Page] = {
    if(page.isContainer) Future.successful(page)
    else getSubPages(page.id).flatMap {
      case Nil => save(Page.createSubPage(page))
      case p :: _ => Future.successful(p)
    }
  }
}