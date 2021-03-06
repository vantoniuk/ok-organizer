package utils.services

import com.google.inject.{ImplementedBy, Inject}
import global.GlobalAppSettings
import models.{User, UserId}
import models.db.DAOProvider
import models.note.{NodeType, NodeId, Node}
import play.api.Logger
import utils._
import utils.services.data.{PagePart, Page, PageRecord}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future


@ImplementedBy(classOf[PageServiceImpl])
trait PageService {
  def getPage(id: NodeId): Future[Option[Page]]
  def getPages(author: User): Future[List[Page]]
  def getSubPages(parentId: NodeId): Future[List[PagePart]]

  /**
    * Get records to display on current page
    *
    * @param pageId page ID
    * @param subpage subpage number starts with 1
    * @return List of [[PageRecord]]
    */
  def getRecords(pageId: NodeId, subpage: Int): Future[List[PageRecord]]
  def save(page: Page): Future[Page]
  def addRecord(record: PageRecord, pageId: NodeId): Future[Option[PageRecord]]
  def updateRecord(record: PageRecord): Future[Option[PageRecord]]
  def updatePage(page: Page): Future[Page]
  def delete(id: NodeId): Future[Boolean]
}

class PageServiceImpl @Inject() (daoProvider: DAOProvider) extends PageService {
  private val nodeDAO = daoProvider.nodeDAO
  private val userDAO = daoProvider.userDAO
  private val logger = Logger.logger
  private case class PageContainer(parentPage: Page, recordContainer: PagePart, records: List[PageRecord])

  def getPage(id: NodeId): Future[Option[Page]] = {
    for {
      nodeOpt <- nodeDAO.find(id)
      userOpt <- Future.traverse(nodeOpt.toList)(node => userDAO.findById(node.author)).map(_.flatten.headOption)
    } yield {
      nodeOpt.and(userOpt).map({case (node, user) => Page(node, user)})
    }
  }

  def getPages(author: User): Future[List[Page]] = {
    for {
      nodes <- nodeDAO.findByUser(author.id, NodeType.PAGE_NODE)
    } yield {
      nodes.map(n => Page.apply(n, author))
    }
  }

  def getSubPages(parentId: NodeId): Future[List[PagePart]] = {
    getSubnodes(parentId)(PagePart.apply)
  }

  def save(page: Page): Future[Page] = {
    page.exec(GlobalAppSettings.service)(nodeDAO.save)
  }

  private def savePagePart(page: PagePart, author: UserId): Future[PagePart] = {
    page.exec(GlobalAppSettings.service, author)(nodeDAO.save)
  }

  def addRecord(record: PageRecord, pageId: NodeId): Future[Option[PageRecord]] = {
    for {
      containerOpt <- resolveRecordContainer(pageId)
      _ = if(containerOpt.isEmpty) logger.error(s"failed to get/create container for page $pageId")
      rs <- Future.sequence(containerOpt.toList.map { p =>
        val newOrder = if(p.records.isEmpty) GlobalAppSettings.pageCapacity else p.records.map(_.order).min - 1
        record.copy(
          container = p.recordContainer.id,
          order = newOrder
        ).exec(GlobalAppSettings.service, p.parentPage.author.id)(nodeDAO.save)
      })
    } yield rs.headOption
  }

  def updateRecord(pageRecord: PageRecord): Future[Option[PageRecord]] = for {
    userIdOpt <- nodeDAO.find(pageRecord.id).map(_.map(_.author))
    recordSeq <- Future.traverse(userIdOpt.toList)(userId => pageRecord.exec(GlobalAppSettings.service, userId)(nodeDAO.update))
  } yield recordSeq.headOption

  def updatePage(page: Page): Future[Page] = {
    page.exec(GlobalAppSettings.service)(nodeDAO.update)
  }

  def getRecordsForPagePart(container: PagePart, pageId: NodeId): Future[List[PageRecord]] = {
    getSubnodes(container.id)(PageRecord.apply(_, pageId))
  }

  private def getSubnodes[T](nodeId: NodeId)(convert: Node => T): Future[List[T]] = {
    for {
      nodes <- nodeDAO.findSubNodes(nodeId)
    } yield {
      nodes.map(convert)
    }
  }

  private def resolveRecordContainer(pageId: NodeId): Future[Option[PageContainer]] = {
    for {
      pageOpt <- getPage(pageId)
      _ = if(pageOpt.isEmpty) logger.error(s"No parent node for node $pageId")
      pages <- Future.traverse(pageOpt.toList)(getRecordContainer)
    } yield pages.headOption
  }

  private def createSubpage(page: Page, order: Int): Future[PageContainer] = {
    savePagePart(Page.createSubPage(page, order), page.author.id)
      .map(part => PageContainer(page, part, Nil))
  }

  private def getRecordContainer(page: Page): Future[PageContainer] = {
    getSubPages(page.id).flatMap {
      case Nil => createSubpage(page, GlobalAppSettings.pageLimit)
      case part :: _ => getRecordsForPagePart(part, page.id).flatMap {
        case records if records.length < GlobalAppSettings.pageCapacity =>
          Future.successful(PageContainer(page, part, records))
        case _ => createSubpage(page, part.order - 1)
      }
    }
  }

  def delete(id: NodeId): Future[Boolean] = nodeDAO.delete(id)

  def getRecords(pageId: NodeId, subpage: Int): Future[List[PageRecord]] = {
    for {
      pageParts <- getSubPages(pageId)
      recordList <- Future.traverse(pageParts.slice(subpage - 1, subpage + 1))(getRecordsForPagePart(_, pageId))
    } yield recordList.flatten.take(GlobalAppSettings.pageCapacity)
  }
}