package models.note

import models.UserId
import org.joda.time.DateTime

case class NodeId(id: Int) extends AnyVal

case class Node(id: NodeId,
                parentId: Option[NodeId],
                nodeType: NodeType,
                title: String,
                description: String,
                icon: Option[String],
                priority: NodePriority,
                rating: Int,
                author: UserId,
                created: DateTime
               )
