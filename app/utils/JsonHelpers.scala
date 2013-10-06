package utils

import utils.imports._
import play.api.libs.json.{JsPath, JsError, JsSuccess, JsResult}
import play.api.data.validation.ValidationError

object JsonHelpers {
  private def accumulateErrors(errors: Seq[(JsPath, Seq[ValidationError])]): String = {
    val |+| = "\n     - "
    ("Parsing errors:" /: errors)  {
      case (acc, (path, validationErrors) ) =>
        acc + |+| + path.toJsonString + |+| + (validationErrors map (_.message) mkString ", " )
    }
  }

  /**
   * converts converted JsResult to DbAction
   * @param jsResult
   * @param status
   * @tparam T
   * @return
   */
  def jsResultToDbAction[T <: DbResponse](jsResult: JsResult[T], status: Int): DbAction = {
    jsResult match {
      case JsSuccess(value, _) => DbSuccess(status, value)
      case JsError(errors) => DbError(status, accumulateErrors(errors))

    }
  }

  /**
   * converts converted JsResult to DbAction
   * @param jsResult
   * @param status
   * @tparam T
   * @return
   */
  def jsDataResultToDbAction[T](jsResult: JsResult[T], status: Int): DbAction = {
    jsResult match {
      case JsSuccess(value, _) => DbSuccess(status, DataResponseOk(value))
      case JsError(errors) => DbError(status, accumulateErrors(errors))

    }
  }
}
