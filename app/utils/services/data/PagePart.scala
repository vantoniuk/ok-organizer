package utils.services.data

import models.note.{Node, NodeId, NodePriority, NodeType}
import models.{UserId, ServiceId, User}
import org.joda.time.DateTime

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

case class PagePart(
                     id: NodeId,
                     container: NodeId,
                     title: String,
                     order: Int,
                     created: DateTime
                   )

object PagePart {
  val noId = NodeId(Int.MinValue)

  def apply(node: Node): PagePart = {
    PagePart(
      id = node.id,
      container = node.parentId.getOrElse(NodeId.noId),
      title = node.title,
      order = node.rating,
      created = node.created
    )
  }

  implicit class PagePartOps(page: PagePart) {
    def toNode(serviceId: ServiceId, authorId: UserId): Node = Node(
      id = page.id,
      parentId = Some(page.container),
      nodeType = NodeType.PAGE_PART_NODE,
      title = page.title,
      description = "",
      icon = None,
      priority = NodePriority.NO_PRIORITY,
      rating = page.order,
      author = authorId,
      created = page.created,
      completed = None,
      expired = None,
      service = serviceId
    )

    def exec(service: ServiceId, authorId: UserId)(action: Node => Future[Node]): Future[PagePart] = {
      action(page.toNode(service, authorId)).map(node => page.copy(id = node.id))
    }
  }

}
