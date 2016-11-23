package controllers

import javax.inject.Inject

import models.UserId
import models.db.DAOProvider
import models.note.NodeId
import org.joda.time.{Interval, DateTimeZone, DateTime}
import play.api.http.Writeable
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, Result}
import tracking._
import utils.services.{MenuService, PageService}
import utils.silhouette._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import play.api.libs.json._
import play.api.libs.functional.syntax._

object SpendTrackingJsMapping {

  implicit val categoriesReads = (
    (JsPath \ "id").readNullable[Int].fmap[SpendCategoryId](x => x.map(SpendCategoryId.apply).getOrElse(SpendCategoryId.empty)) ~
    (JsPath \ "user_id").readNullable[Int].fmap(x => x.map(UserId.apply).getOrElse(UserId.empty)) ~
    (JsPath \ "name").read[String] ~
    (JsPath \ "description").read[String] ~
    (JsPath \ "limit").read[Double].map(l => (l * 100).toInt)
  )(SpendCategory.apply _)

  implicit val categoriesWrites = (
    (JsPath \ "id").write[Int].contramap[SpendCategoryId](_.id) ~
    (JsPath \ "user_id").write[Int].contramap[UserId](_.id) ~
    (JsPath \ "name").write[String] ~
    (JsPath \ "description").write[String] ~
    (JsPath \ "limit").write[Double].contramap[Int](l => l.toDouble / 100)
  )(unlift(SpendCategory.unapply))

  implicit val creditCardReads = (
    (JsPath \ "card_id").readNullable[Int].map(_.fold(CreditCardId.empty)(CreditCardId.apply)) ~
    (JsPath \ "user_id").readNullable[Int].map(_.fold(UserId.empty)(UserId.apply)) ~
    (JsPath \ "vendor").read[String] ~
    (JsPath \ "name").read[String] ~
    (JsPath \ "description").read[String] ~
    (JsPath \ "available").read[Double].map(available => (available * 100).toInt) ~
    (JsPath \ "total").read[Double].map(total => (total * 100).toInt) ~
    (JsPath \ "added").readNullable[Long].map(_.fold(DateTime.now(DateTimeZone.UTC))(ms => new DateTime(ms, DateTimeZone.UTC)))
  )(CreditCard.apply _)

  implicit val creditCardWrites = (
    (JsPath \ "card_id").write[Int].contramap[CreditCardId](_.id) ~
    (JsPath \ "user_id").write[Int].contramap[UserId](_.id) ~
    (JsPath \ "vendor").write[String] ~
    (JsPath \ "name").write[String] ~
    (JsPath \ "description").write[String] ~
    (JsPath \ "available").write[Double].contramap[Int](a => a.toDouble / 100) ~
    (JsPath \ "total").write[Double].contramap[Int](t => t.toDouble / 100) ~
    (JsPath \ "added").write[Long].contramap[DateTime](_.getMillis)
  )(unlift(CreditCard.unapply))

  implicit val creditCardStatementReads = (
    (JsPath \ "id").readNullable[Int].map(_.getOrElse(0)) ~
    (JsPath \ "user_id").readNullable[Int].map(_.fold(UserId.empty)(UserId.apply)) ~
    (JsPath \ "card_id").read[Int].map(CreditCardId.apply) ~
    (JsPath \ "available").read[Double].map(available => (available * 100).toInt) ~
    (JsPath \ "timestamp").readNullable[Long].map(_.fold(DateTime.now(DateTimeZone.UTC))(ms => new DateTime(ms, DateTimeZone.UTC)))
  )(CreditCardStatement.apply _)

  implicit val creditCardStatementWrites = (
    (JsPath \ "id").write[Int] ~
    (JsPath \ "user_id").write[Int].contramap[UserId](_.id) ~
    (JsPath \ "card_id").write[Int].contramap[CreditCardId](_.id) ~
    (JsPath \ "amount").write[Int] ~
    (JsPath \ "timestamp").write[Long].contramap[DateTime](_.getMillis)
  )(unlift(CreditCardStatement.unapply))

  implicit val creditCardRichStatementWrites = (
    (JsPath \ "id").write[Int] ~
    (JsPath \ "card_vendor").write[String] ~
    (JsPath \ "card_name").write[String] ~
    (JsPath \ "amount").write[Double].contramap[Int](_.toDouble / 100) ~
    (JsPath \ "timestamp").write[Long].contramap[DateTime](_.getMillis)
  )(unlift(RichCreditCardStatement.unapply))

  implicit val creditCardSpendingsReads = (
    (JsPath \ "id").readNullable[Int].map(_.getOrElse(0)) ~
    (JsPath \ "user_id").readNullable[Int].map(_.fold(UserId.empty)(UserId.apply)) ~
    (JsPath \ "card_id").read[Int].map(CreditCardId.apply) ~
    (JsPath \ "category_id").read[Int].map(SpendCategoryId.apply) ~
    (JsPath \ "available").read[Double].map(available => (available * 100).toInt) ~
    (JsPath \ "timestamp").readNullable[Long].map(_.fold(DateTime.now(DateTimeZone.UTC))(ms => new DateTime(ms, DateTimeZone.UTC)))
  )(CreditCardSpending.apply _)

  implicit val creditCardSpendingsWrites = (
    (JsPath \ "id").write[Int] ~
    (JsPath \ "user_id").write[Int].contramap[UserId](_.id) ~
    (JsPath \ "card_id").write[Int].contramap[CreditCardId](_.id) ~
    (JsPath \ "category_id").write[Int].contramap[SpendCategoryId](_.id) ~
    (JsPath \ "amount").write[Int] ~
    (JsPath \ "timestamp").write[Long].contramap[DateTime](_.getMillis)
  )(unlift(CreditCardSpending.unapply))

  implicit val creditCardRichSpendingsWrites = (
    (JsPath \ "card_vendor").write[String] ~
    (JsPath \ "card_name").write[String] ~
    (JsPath \ "category").write[String] ~
    (JsPath \ "amount").write[Double].contramap[Int](_.toDouble / 100) ~
    (JsPath \ "timestamp").write[Long].contramap[DateTime](_.getMillis)
  )(unlift(RichCreditCardSpending.unapply))
}

class SpendTrackingController @Inject()(val env: AuthenticationEnvironment, val messagesApi: MessagesApi, val daoProvider: DAOProvider, menuService: MenuService) extends AuthenticationController {
  import SpendTrackingJsMapping._

  def mainTracking() = SecuredAction async withMenusSecured(menuService) { (request, menus) =>
    implicit val (m, r) = (menus, request)
    Future.successful(Ok(views.html.spendings.main_tracking()))
  }

  def metaTracking() = SecuredAction async withMenusSecured(menuService) { (request, menus) =>
    implicit val (m, r) = (menus, request)
    Future.successful(Ok(views.html.spendings.meta_tracking()))
  }

  def categories() = Action async withMenusAndUser(menuService) { (request, menus, user) =>
    implicit val (m,r,u) = (menus, request, user)
    for {
      categories <- Future.traverse(user.toList)(u1 => daoProvider.spendCategoriesDAO.findByUserId(u1.id))
    } yield {
      categories.flatten match {
        case Nil => NotFound("not found spending categories for user with id " + user.map(_.id))
        case cs => Ok(Json.obj("items" -> Json.toJson(cs)))
      }
    }
  }

  def saveCategory(category: String) = Action async withMenusAndUser(menuService) { (request, menus, user) =>
    implicit val (m, r, u) = (menus, request, user)
    logger.info("saving category " + category)
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
        case cs => Ok(Json.obj("items" -> Json.toJson(cs)))
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

  def statements(start: Long, end: Long, cardId: Option[Int]) = Action async withMenusAndUser(menuService) { (request, menus, user) =>
    implicit val (m,r,u) = (menus, request, user)
    val interval = new Interval(start, end, DateTimeZone.UTC)
    val statementFuture = cardId match {
      case Some(id) =>
        daoProvider.creditCardStatementsDAO.findRichByCardId(CreditCardId(id), interval)
      case _ =>
        Future.traverse(user.toList)(u1 => daoProvider.creditCardStatementsDAO.findRichByUserId(u1.id, interval)).map(_.flatten)
    }
    for {
      statements <- statementFuture
    } yield {
      Ok(Json.obj("items" -> Json.toJson(statements)))
    }
  }

  def saveCreditCardStatement(statement: String) = Action async withMenusAndUser(menuService) { (request, menus, user) =>
    println("------------------ " + statement)
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
