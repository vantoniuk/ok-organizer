package utils.services.data

import models.note.{Node, NodeId, NodePriority, NodeType}
import models.{UserId, ServiceId, User}
import org.joda.time.DateTime

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

case class PageRecord(
    id: NodeId,
    parentPage: NodeId,
    container: NodeId,
    title: String,
    content: String,
    icon: Option[String],
    order: Int,
    created: DateTime
)

object PageRecord {
  val noId = NodeId(Int.MinValue)

  def apply(node: Node, pageId: NodeId): PageRecord = {
    PageRecord(
      id = node.id,
      container = node.parentId.getOrElse(NodeId.noId),
      parentPage = pageId,
      title = node.title,
      content = node.description,
      icon = node.icon,
      order = node.rating,
      created = node.created
    )
  }

  implicit class PageRecordOps(page: PageRecord) {
    def toNode(serviceId: ServiceId, authorId: UserId): Node = Node(
      id = page.id,
      parentId = Some(page.container),
      nodeType = NodeType.RECORD_NODE,
      title = page.title,
      description = page.content,
      icon = page.icon,
      priority = NodePriority.NO_PRIORITY,
      rating = page.order,
      author = authorId,
      created = page.created,
      expired = None,
      service = serviceId
    )

    def exec(service: ServiceId, authorId: UserId)(action: Node => Future[Node]): Future[PageRecord] = {
      action(page.toNode(service, authorId)).map(node => page.copy(id = node.id))
    }
  }

}
