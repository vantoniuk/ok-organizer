package utils.imports

trait ModelImports extends ModelTypes with ModelObjects

trait ModelTypes {
  type BaseUser = data.model.user.BaseUser
  type NotRegisteredUser = data.model.user.NotRegisteredUser

  type PersonalData = data.model.user.PersonalData
  type UserAppData = data.model.user.UserAppData
}

trait ModelObjects {
  val NotRegisteredUser = data.model.user.NotRegisteredUser

  val PersonalData = data.model.user.PersonalData
  val UserAppData = data.model.user.UserAppData

}
