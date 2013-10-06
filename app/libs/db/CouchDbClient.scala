package libs.db

import play.api.libs.json._
import play.Logger
import utils.imports._
import scala.concurrent.Future

class CouchDbClient(dbUri: String) extends HttpClient {
  private def makeActionUrl(in: String): String = dbUri + "/" + in

  def save(id: String, obj: JsValue): Future[DbAction] = {
    put(makeActionUrl(id), obj) map { response =>
      if(response.status == CouchDbClient.CREATED)
        JsonHelpers.jsResultToDbAction(response.json.validate[CreatedOk], response.status)
      else if(response.status == CouchDbClient.DOC_CONFLICT || response.status == CouchDbClient.ALREADY_EXISTS)
        JsonHelpers.jsResultToDbAction(response.json.validate[CreatedError], response.status)
      else {
        Logger.info("CouchDb communication error:\n     " + response.body)
        DbError(response.status, DbError.UNEXPECTED_HTTP_STATUS)
      }
    }
  }

  def find[T](id: String)(implicit format: Format[T]): Future[DbAction] = {
    get(makeActionUrl(id)) map {
      response =>
        if(response.status == CouchDbClient.OK) JsonHelpers.jsDataResultToDbAction(response.json.validate[T], response.status)
        else {
          Logger.info("Not found object by ID %s:\n     " format id + response.body)
          DbError(response.status, DbError.NOT_FOUND)
        }

    }
  }
}

object CouchDbClient {
  val OK = 200
  val CREATED = 201
  val DOC_CONFLICT = 409
  val ALREADY_EXISTS = 412

  def apply(dbUri: String): CouchDbClient = new CouchDbClient(dbUri)


}
