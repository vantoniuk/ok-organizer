package utils.services

import com.google.inject.{Inject, ImplementedBy}
import models.ServiceId
import models.db.DAOProvider
import models.note.{NodeId, NodeType}
import utils.services.data.Menu
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

@ImplementedBy(classOf[MenuServiceImpl])
trait MenuService {
  def getMenus(service: ServiceId): Future[List[Menu]]
  def addMenu(menu: Menu, service: ServiceId)
  def updateMenu(menu: Menu, service: ServiceId)
  def delete(id: NodeId): Future[Boolean]
}

class MenuServiceImpl @Inject() (daoProvider: DAOProvider) extends MenuService {
  private val nodeDAO = daoProvider.nodeDAO
  private val userDao = daoProvider.userDAO
  override def getMenus(service: ServiceId): Future[List[Menu]] = {
    for {
      nodes <- nodeDAO.findByService(service, NodeType.MENU_NODE)
      users <- Future.traverse(nodes.map(_.author))(userDao.findById)
    } yield {
      val userMap = users.flatMap(_.map(u => u.id -> u)).toMap
      for {
        node <- nodes
        user <- userMap.get(node.author)
      } yield Menu(node, user)
    }
  }

  override def addMenu(menu: Menu, service: ServiceId): Unit = {
    nodeDAO.save(menu.toNode(service))
  }

  override def updateMenu(menu: Menu, service: ServiceId): Unit = {
    nodeDAO.update(menu.toNode(service))
  }

  def delete(id: NodeId): Future[Boolean] = {
    nodeDAO.delete(id)
  }
}
