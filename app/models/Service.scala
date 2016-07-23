package models

case class ServiceId(id: Int) extends AnyVal

case class Service(
    id: ServiceId,
    title: String,
    description: String,
    url: String,
    roleRequired: UserRole
)