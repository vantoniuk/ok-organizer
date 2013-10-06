package utils

package object imports extends DataImports with ModelImports with HttpImports with DbImports with  HelpersImports with ServicesImports with Implicits {
  val app = play.api.Play.current

  val AppMain = organizer.AppMain

  implicit val defaultExecutionContext = play.api.libs.concurrent.Execution.defaultContext
}
