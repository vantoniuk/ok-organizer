package data.model.user

import scala.concurrent.Future
import utils.imports._

trait CouchDbModel {
  def save(): Future[DbAction]
//  def delete(): Future[DbAction]
}
