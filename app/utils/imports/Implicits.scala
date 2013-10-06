package utils.imports

trait Implicits extends data.basic.BasicConvertions
  with data.model.user.UserJsonConvertions
  with data.db.DbJsonConversions
