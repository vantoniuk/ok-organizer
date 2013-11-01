package data.user.basic

/**
 * Object contains basic wrappers for dataTypes
 */
object DataTypes {
  case class OptionalString(underlying: String) {
    def get: String = underlying
    override def toString: String = underlying
  }

}
