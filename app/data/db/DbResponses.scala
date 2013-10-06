package data.db


object DbResponses {

  sealed trait DbResponse {
    def isOk: Boolean = false
    def isError: Boolean = false
  }

  case class CreatedOk(id: String, rev: String) extends DbResponse {
    override def isOk = true
  }

  case class CreatedError(error: String, reason: String) extends DbResponse {
    override def isError = true
  }

  case class DataResponseOk[T](data: T) extends DbResponse {
    override def isOk = true
  }

  case class DataResponseError(message: String) extends DbResponse {
    override def isError = true
  }

}
