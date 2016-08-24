package utils.silhouette

import com.mohiva.play.silhouette.api.Authorization
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.{UserRole, User}
import play.api.mvc.Request
import play.api.i18n.Messages
import scala.concurrent.Future

/**
 * Only allows those users that have at least a service of the selected.
 * Master service is always allowed.
 * Ex: WithService("serviceA", "serviceB") => only users with services "serviceA" OR "serviceB" (or "master") are allowed.
 */
case class ForRole(role: UserRole) extends Authorization[User, CookieAuthenticator] {
  def isAuthorized[A](user: User, authenticator: CookieAuthenticator)(implicit r: Request[A], m: Messages) = Future.successful {
    ForRole.isAuthorized(user, role)
  }
}
object ForRole {
  def isAuthorized(user: User, role: UserRole): Boolean = UserRole.GUEST == role || user.role.id <= role.id
}