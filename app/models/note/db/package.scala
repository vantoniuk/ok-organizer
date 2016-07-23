package models.note

import models.{ServiceId, UserId}

import scala.concurrent.Future

package object db {
  trait NodeDAO {
    def find(id: NodeId): Future[Option[Node]]
    def findByUser(author: UserId, nodeType: NodeType): Future[List[Node]]
    def findByService(author: ServiceId, nodeType: NodeType): Future[List[Node]]

    def save(node: Node): Future[Node]
    def update(node: Node): Future[Node]

    def delete(nodeId: NodeId): Future[Boolean]
  }
}
