package controllers

import javax.inject.Inject

import models.UserId
import models.db.DAOProvider
import models.note.NodeId
import org.joda.time.{Interval, DateTimeZone, DateTime}
import play.api.http.Writeable
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, Result}
import tracking.{CreditCardStatement, CreditCard, CreditCardId, SpendCategory}
import utils.services.{MenuService, PageService}
import utils.silhouette._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import play.api.libs.json._
import play.api.libs.functional.syntax._

object SpendTrackingJsMapping {

  implicit val categoriesReads = (
    (JsPath \ "user_id").readNullable[Int].fmap(x => x.map(UserId.apply).getOrElse(UserId.empty)) ~
    (JsPath \ "name").read[String] ~
    (JsPath \ "description").read[String] ~
    (JsPath \ "limit").read[Int]
  )(SpendCategory.apply _)

  implicit val categoriesWrites = (
    (JsPath \ "user_id").write[Int].contramap[UserId](_.id) ~
    (JsPath \ "name").write[String] ~
    (JsPath \ "description").write[String] ~
    (JsPath \ "limit").write[Int]
  )(unlift(SpendCategory.unapply))

  implicit val creditCardReads = (
    (JsPath \ "card_id").readNullable[Int].map(_.fold(CreditCardId.empty)(CreditCardId.apply)) ~
    (JsPath \ "user_id").readNullable[Int].map(_.fold(UserId.empty)(UserId.apply)) ~
    (JsPath \ "vendor").read[String] ~
    (JsPath \ "name").read[String] ~
    (JsPath \ "description").read[String] ~
    (JsPath \ "available").read[Int] ~
    (JsPath \ "total").read[Int] ~
    (JsPath \ "added").readNullable[Long].map(_.fold(DateTime.now(DateTimeZone.UTC))(ms => new DateTime(ms, DateTimeZone.UTC)))
  )(CreditCard.apply _)

  implicit val creditCardWrites = (
    (JsPath \ "card_id").write[Int].contramap[CreditCardId](_.id) ~
    (JsPath \ "user_id").write[Int].contramap[UserId](_.id) ~
    (JsPath \ "vendor").write[String] ~
    (JsPath \ "name").write[String] ~
    (JsPath \ "description").write[String] ~
    (JsPath \ "available").write[Int] ~
    (JsPath \ "total").write[Int] ~
    (JsPath \ "added").write[Long].contramap[DateTime](_.getMillis)
  )(unlift(CreditCard.unapply))

  implicit val creditCardStatementReads = (
    (JsPath \ "id").readNullable[Int].map(_.getOrElse(0)) ~
    (JsPath \ "user_id").readNullable[Int].map(_.fold(UserId.empty)(UserId.apply)) ~
    (JsPath \ "card_id").read[Int].map(CreditCardId.apply) ~
    (JsPath \ "available").read[Int] ~
    (JsPath \ "added").readNullable[Long].map(_.fold(DateTime.now(DateTimeZone.UTC))(ms => new DateTime(ms, DateTimeZone.UTC)))
  )(CreditCardStatement.apply _)

  implicit val creditCardStatementWrites = (
    (JsPath \ "id").write[Int] ~
    (JsPath \ "user_id").write[Int].contramap[UserId](_.id) ~
    (JsPath \ "card_id").write[Int].contramap[CreditCardId](_.id) ~
    (JsPath \ "available").write[Int] ~
    (JsPath \ "added").write[Long].contramap[DateTime](_.getMillis)
  )(unlift(CreditCardStatement.unapply))
}

class SpendTrackingController @Inject()(val env: AuthenticationEnvironment, val messagesApi: MessagesApi, val daoProvider: DAOProvider, menuService: MenuService) extends AuthenticationController {
  import SpendTrackingJsMapping._

  def mainTracking() = SecuredAction async withMenusSecured(menuService) { (request, menus) =>
    implicit val (m, r) = (menus, request)
    Future.successful(Ok(views.html.spendings.main_tracking()))
  }

  def categories() = Action async withMenusAndUser(menuService) { (request, menus, user) =>
    implicit val (m,r,u) = (menus, request, user)
    for {
      categories <- Future.traverse(user.toList)(u1 => daoProvider.spendCategoriesDAO.findByUserId(u1.id))
    } yield {
      categories.flatten match {
        case Nil => NotFound("not found spending categories for user with id " + user.map(_.id))
        case cs => Ok(Json.obj("categories" -> Json.toJson(cs)))
      }
    }
  }

  def saveCategory(category: String) = Action async withMenusAndUser(menuService) { (request, menus, user) =>
    implicit val (m, r, u) = (menus, request, user)
    user.fold(Future.successful(BadRequest("""no user logged in"""))) { loggedInUser =>
      Json.fromJson[SpendCategory](Json.parse(category)).map { categoryValue =>
        daoProvider.spendCategoriesDAO.saveSpendCategory(categoryValue.copy(
          userId = loggedInUser.id
        )).map({
          case true => Ok("success")
          case false => Ok("couldn't save category for user " + loggedInUser.id)
        })
      }.getOrElse(
        Future.successful(BadRequest("couldn't parse category json " + category))
      )
    }
  }

  def creditCards() = Action async withMenusAndUser(menuService) { (request, menus, user) =>
    implicit val (m,r,u) = (menus, request, user)
    for {
      cards <- Future.traverse(user.toList)(u1 => daoProvider.creditCardsDAO.findByUserId(u1.id))
    } yield {
      cards.flatten match {
        case Nil => NotFound("not found credit cards for user with id " + user.map(_.id))
        case cs => Ok(Json.obj("cards" -> Json.toJson(cs)))
      }
    }
  }

  def saveCreditCard(card: String) = Action async withMenusAndUser(menuService) { (request, menus, user) =>
    implicit val (m, r, u) = (menus, request, user)
    user.fold(Future.successful(BadRequest("""no user logged in"""))) { loggedInUser =>
      Json.fromJson[CreditCard](Json.parse(card)).map { cardValue =>
        daoProvider.creditCardsDAO.saveCreditCard(cardValue.copy(
          userId = loggedInUser.id
        )).map({
          case true => Ok("success")
          case false => Ok("couldn't save credit card for user " + loggedInUser.id)
        })
      }.getOrElse(
        Future.successful(BadRequest("couldn't parse credit card json " + card))
      )
    }
  }

  def statements(start: Long, end: Long) = Action async withMenusAndUser(menuService) { (request, menus, user) =>
    implicit val (m,r,u) = (menus, request, user)
    val interval = new Interval(start, end, DateTimeZone.UTC)
    for {
      statements <- Future.traverse(user.toList)(u1 => daoProvider.creditCardStatementsDAO.findByUserId(u1.id, interval))
    } yield {
      statements.flatten match {
        case Nil => NotFound("not found credit card statements for user with id " + user.map(_.id))
        case cs => Ok(Json.obj("statements" -> Json.toJson(cs)))
      }
    }
  }

  def saveCreditCardStatement(statement: String) = Action async withMenusAndUser(menuService) { (request, menus, user) =>
    implicit val (m, r, u) = (menus, request, user)
    user.fold(Future.successful(BadRequest("""no user logged in"""))) { loggedInUser =>
      Json.fromJson[CreditCardStatement](Json.parse(statement)).map { cardValue =>
        daoProvider.creditCardStatementsDAO.saveStatement(cardValue.copy(
          userId = loggedInUser.id
        )).map({
          case true => Ok("success")
          case false => Ok("couldn't save credit card statement for user " + loggedInUser.id)
        })
      }.getOrElse(
        Future.successful(BadRequest("couldn't parse credit card statement json " + statement))
      )
    }
  }
}
