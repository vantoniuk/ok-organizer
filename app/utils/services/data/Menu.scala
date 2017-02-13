package utils.services.data

import models.{User, ServiceId, UserId}
import models.note._
import org.joda.time.DateTime

case class Menu(
    id: NodeId,
    parentId: Option[NodeId],
    title: String,
    url: String,
    icon: Option[String],
    order: Int,
    author: User,
    created: DateTime
)

object Menu {
  def apply(node: Node, author: User): Menu = {
    Menu(
      id = node.id,
      parentId = node.parentId,
      title = node.title,
      url = node.description,
      icon = node.icon,
      order = node.rating,
      author = author,
      created = node.created
    )
  }

  implicit class MenuOps(menu: Menu) {
    def toNode(service: ServiceId): Node = {
      Node(
        id = menu.id,
        parentId = menu.parentId,
        nodeType = NodeType.MENU_NODE,
        title = menu.title,
        description = menu.url,
        icon = menu.icon,
        priority = NodePriority.NO_PRIORITY,
        rating = menu.order,
        author = menu.author.id,
        created = menu.created,
        expired = None,
        service = service
      )
    }
  }

}
