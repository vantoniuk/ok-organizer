package utils.silhouette

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.impl.util.BCryptPasswordHasher

/**
  * Created by vantoniuk on 7/17/16.
  */
object Helpers {
  def email2loginInfo(key: String): LoginInfo = LoginInfo(CredentialsProvider.ID, key)
  def pwd2passwordInfo(pwd: String): PasswordInfo = PasswordInfo(BCryptPasswordHasher.ID, pwd, salt = Some("your-salt"))
}
