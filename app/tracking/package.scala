package object tracking {
  import models.db.MyPostgresDriver.api._

  val creditCardStatements = CreditCards.query.schema.createStatements.mkString("", ";\n", ";")
  val creditCardStatementsStatements = CreditCardStatements.query.schema.createStatements.mkString("", ";\n", ";")
  val spendingCategoriesStatements = SpendCategories.query.schema.createStatements.mkString("", ";\n", ";")
  val allStatements = List(
    creditCardStatements,
    spendingCategoriesStatements,
    creditCardStatementsStatements
  ).mkString("\n")

  val creditCardDropStatements = CreditCards.query.schema.dropStatements.mkString("", ";\n", ";")
  val creditCardStatementsDropStatements = CreditCardStatements.query.schema.dropStatements.mkString("", ";\n", ";")
  val spendingCategoriesDropStatements = SpendCategories.query.schema.dropStatements.mkString("", ";\n", ";")
  val allDropStatements = List(
    creditCardStatementsDropStatements,
    creditCardDropStatements,
    spendingCategoriesDropStatements
  ).mkString("\n")
}
