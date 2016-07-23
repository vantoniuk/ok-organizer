package utils.services

import com.google.inject.{Inject, ImplementedBy}
import models.ServiceId
import models.db.DAOProvider
import models.note.NodeType
import utils.services.data.Menu
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

@ImplementedBy(classOf[MenuServiceImpl])
trait MenuService {
  def getMenus(service: ServiceId): Future[List[Menu]]
  def addMenu(menu: Menu, service: ServiceId)
  def updateMenu(menu: Menu, service: ServiceId)
}

class MenuServiceImpl @Inject() (daoProvider: DAOProvider) extends MenuService {
  private val nodeDAO = daoProvider.nodeDAO
  override def getMenus(service: ServiceId): Future[List[Menu]] = {
    nodeDAO.findByService(service, NodeType.MENU_NODE).map(_.map(Menu.apply))
  }

  override def addMenu(menu: Menu, service: ServiceId): Unit = {
    nodeDAO.save(menu.toNode(service))
  }

  override def updateMenu(menu: Menu, service: ServiceId): Unit = {
    nodeDAO.update(menu.toNode(service))
  }
}
