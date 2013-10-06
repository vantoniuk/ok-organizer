package controllers
import play.api._
import play.api.mvc._

import utils.imports._
import utils.AppConfig

object Application extends Controller {

  def index = Action {

    val personal = PersonalData("vantoniuk", "vantoniuk@gmail.com", None, None)
    val notRegistreredUser = NotRegisteredUser(personal, Services.UNKNOWN)
    Async {
      notRegistreredUser.save flatMap{ case DbSuccess(_, CreatedOk(id, _)) =>
      NotRegisteredUser.get(id) map {
        dbResult =>
//          Ok(views.html.index("User maybe is still not saved!! " + AppConfig.usersDbUri))
          Ok(views.html.index("User maybe is still not saved!!",  dbResult.toString))
      }}
//    val userData = UserDataString("random_id", None, None)
  }}

}