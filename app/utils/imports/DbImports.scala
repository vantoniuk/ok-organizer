package utils.imports

trait DbImports extends DbTypes with DbObjects

trait DbTypes {
  type DbAction = data.db.DbAction

  type DbSuccess = data.db.DbSuccess
  type DbError = data.db.DbError

  type DbResponse = data.db.DbResponses.DbResponse
  type CreatedOk = data.db.DbResponses.CreatedOk
  type CreatedError = data.db.DbResponses.CreatedError
  type DataResponseOk[T] = data.db.DbResponses.DataResponseOk[T]
  type DataResponseError = data.db.DbResponses.DataResponseError

  type CouchDbClient = libs.db.CouchDbClient
}

trait DbObjects {
  val DbSuccess = data.db.DbSuccess
  val DbError = data.db.DbError

  val CreatedOk = data.db.DbResponses.CreatedOk
  val CreatedError = data.db.DbResponses.CreatedError
  def DataResponseOk[T](dataIn: T): DataResponseOk[T] = data.db.DbResponses.DataResponseOk(dataIn)
  val DataResponseError = data.db.DbResponses.DataResponseError

  val CouchDbClient = libs.db.CouchDbClient
}
