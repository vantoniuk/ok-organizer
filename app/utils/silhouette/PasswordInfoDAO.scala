package utils.silhouette

import com.google.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.impl.util.BCryptPasswordHasher
import models.User
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.api.LoginInfo
import models.db.DAOProvider
import play.api.Configuration
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class PasswordInfoDAO @Inject() (daoProvider: DAOProvider, configuration: Configuration) extends DelegableAuthInfoDAO[PasswordInfo] {

  def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
    update(loginInfo, authInfo)

  def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] =
    daoProvider.userDAO.findByEmail(loginInfo.providerKey).map {
      case Some(user) if user.emailConfirmed =>
        Some(PasswordInfo(BCryptPasswordHasher.ID, user.password, salt = configuration.getString("play.crypto.secret")))
      case _ => None
    }

  def remove(loginInfo: LoginInfo): Future[Unit] = {
    daoProvider.userDAO.delete(loginInfo.providerKey).map(_ => ())
  }

  def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
    find(loginInfo).flatMap {
      case Some(_) => update(loginInfo, authInfo)
      case None => add(loginInfo, authInfo)
    }

  def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
    daoProvider.userDAO.findByEmail(loginInfo.providerKey).map {
      case Some(user) => {
        daoProvider.userDAO.save(user.copy(password = authInfo.password))
        authInfo
      }
      case _ => throw new Exception("PasswordInfoDAO - update : the user must exists to update its password")
    }

}