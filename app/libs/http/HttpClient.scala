package libs.http

import play.api.libs.ws.{Response, WS}
import scala.concurrent.Future
import play.api.libs.json.{JsValue, JsString}

trait HttpClient {

  def get(url: String): Future[Response] =
    WS.url( url ).get()

  def post(url: String, content: JsValue): Future[Response] =
    WS.url( url ).post(content)

  def put(url: String, content: JsValue): Future[Response] =
    WS.url( url ).put(content)

}
