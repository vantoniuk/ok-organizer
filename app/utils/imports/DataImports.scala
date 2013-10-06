package utils.imports

trait DataImports extends DataTypes with DataObjects

trait DataTypes {
  type OptionalString = data.user.basic.DataTypes.OptionalString
  type DataType = data.model.DataTypes.Value
}

trait DataObjects {
  val OptionalString = data.user.basic.DataTypes.OptionalString
  val DataTypes = data.model.DataTypes
}