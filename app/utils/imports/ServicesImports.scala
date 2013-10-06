package utils.imports

trait ServicesImports extends ServicesTypes with ServicesObject

trait ServicesTypes {
  type Service = services.Services.Value
}

trait ServicesObject {
  val Services = services.Services
}