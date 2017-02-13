package models.note

import models.{ServiceId, UserId}
import org.joda.time.DateTime

case class NodeId(id: Int) extends AnyVal
object NodeId {
  val noId = NodeId(Int.MinValue)
}

case class Node(id: NodeId,
                parentId: Option[NodeId],
                nodeType: NodeType,
                title: String,
                description: String,
                icon: Option[String],
                priority: NodePriority,
                rating: Int,
                author: UserId,
                created: DateTime,
                expired: Option[DateTime],
                service: ServiceId
               )
