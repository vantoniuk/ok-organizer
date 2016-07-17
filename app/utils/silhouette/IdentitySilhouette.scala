package utils.silhouette

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider

trait IdentitySilhouette extends Identity {
  def key: String
  def loginInfo: LoginInfo = LoginInfo(CredentialsProvider.ID, key)
}