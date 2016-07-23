package models.db

import models.{ServiceId, UserRole, UserId}
import slick.driver.PostgresDriver.api._

object Implicits {

  implicit val userIdMapped = MappedColumnType.base[UserId, Int](_.id, UserId.apply)
  implicit val serviceIdMapped = MappedColumnType.base[ServiceId, Int](_.id, ServiceId.apply)
  implicit val userRoleMapped = MappedColumnType.base[UserRole, Int](_.id, UserRole.byId)

}
