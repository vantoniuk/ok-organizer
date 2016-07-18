package models.note.db

import models.note.{NodePriority, NodeId, NodeType}
import slick.driver.PostgresDriver.api._

object Implicits {
  implicit val nodeIdMapped = MappedColumnType.base[NodeId, Int](_.id, NodeId.apply)
  implicit val nodeTypeMapped = MappedColumnType.base[NodeType, Int](_.id, NodeType.byId)
  implicit val nodePriorityMapped = MappedColumnType.base[NodePriority, Int](_.id, NodePriority.byId)
}
