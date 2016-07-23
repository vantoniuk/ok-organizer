package utils.services.data

import models.{ServiceId, UserId}
import models.note._
import org.joda.time.DateTime

case class Menu(id: NodeId,
    parentId: Option[NodeId],
    title: String,
    description: String,
    icon: Option[String],
    order: Int,
    author: UserId,
    created: DateTime
)

object Menu {
  def apply(node: Node): Menu = {
    Menu(
      id = node.id,
      parentId = node.parentId,
      title = node.title,
      description = node.description,
      icon = node.icon,
      order = node.rating,
      author = node.author,
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
        description = menu.description,
        icon = menu.icon,
        priority = NodePriority.NO_PRIORITY,
        rating = menu.order,
        author = menu.author,
        created = menu.created,
        service = service
      )
    }
  }

}
