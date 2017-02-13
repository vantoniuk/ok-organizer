package utils.services.data

import global.GlobalAppSettings
import models.note.{Node, NodeId, NodePriority, NodeType}
import models.{ServiceId, User}
import org.joda.time.DateTime

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

case class Page(
                 id: NodeId,
                 parentId: Option[NodeId],
                 title: String,
                 description: String,
                 previewIcon: String,
                 order: Int,
                 author: User,
                 created: DateTime
               )

object Page {
  val noId = NodeId(Int.MinValue)
  val defaultPreview = GlobalAppSettings.defaultPreview
  def createSubPage(page: Page, order: Int): PagePart = PagePart(
    id = NodeId.noId,
    container = page.id,
    title = s"subpage-${page.id}-${GlobalAppSettings.pageLimit}",
    order = order,
    created = DateTime.now
  )

  def apply(node: Node, author: User): Page = {
    Page(
      id = node.id,
      parentId = node.parentId,
      title = node.title,
      description = node.description,
      previewIcon = node.icon.getOrElse(defaultPreview),
      order = node.rating,
      author = author,
      created = node.created
    )
  }

  implicit class PageOps(page: Page) {
    def toNode(serviceId: ServiceId): Node = Node(
      id = page.id,
      parentId = page.parentId,
      nodeType = NodeType.PAGE_NODE,
      title = page.title,
      description = page.description,
      icon = Some(page.previewIcon),
      priority = NodePriority.NO_PRIORITY,
      rating = page.order,
      author = page.author.id,
      created = page.created,
      expired = None, 
      service = serviceId
    )

    def exec(service: ServiceId)(action: Node => Future[Node]): Future[Page] = {
      action(page.toNode(service)).map(node => page.copy(id = node.id))
    }
  }

}
