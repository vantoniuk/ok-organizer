package data.db

import utils.imports._

/**
 * Trait that represents DB reaction. Doesn't depend on result of the performed operation, show the db availability
 * Would be DbSuccess if DB did response, and DbError if DB didn't response. Also would be DbSuccess even if operation
 * such as create pr delete failed
 */

sealed trait DbAction {
  def status: Int
  def isError: Boolean = false
  def isSuccess: Boolean = false
}

case class DbSuccess(status: Int, dbResponse: DbResponse) extends DbAction {
  override def isSuccess: Boolean = true
}

case class DbError(status: Int, message: String) extends DbAction {
  override def isError: Boolean = true
}

object DbError {
  val UNEXPECTED_HTTP_STATUS = "got unexpected http status"
  val NOT_FOUND = "not found object by id"
}
