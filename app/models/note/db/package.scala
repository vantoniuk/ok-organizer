package models.note

import models.UserId

import scala.concurrent.Future

package object db {
  trait NodeDAO {
    def find(id: NodeId): Future[Option[Node]]
    def find(author: UserId, nodeType: NodeType): Future[List[Node]]

    def save(node: Node): Future[Node]
    def update(node: Node): Future[Node]

    def delete(nodeId: NodeId): Future[Boolean]
  }
}
