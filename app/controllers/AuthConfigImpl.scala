package controllers

import jp.t2v.lab.play2.auth.AuthConfig
import models.security.Permission
import play.api.mvc._
import play.api.mvc.Results._
import play.api.Logger
import util.AuthHelper
import reflect._
import play.api.http.Status._
import java.net.URI


trait AuthConfigImpl extends AuthConfig {

  /**
   * A type that is used to identify a user.
   */
  type Id = Long

  /**
   * A type that represents a user in your application.
   */
  type User = models.User

  /**
   * A type that is defined by every action for authorization.
   * This sample uses the following trait:
   *
   * sealed trait Permission
   * case object Administrator extends Permission
   * case object NormalUser extends Permission
   */
  type Authority = Permission

  /**
   * A `ClassManifest` is used to retrieve an id from the Cache API.
   * Use something like this:
   */
  //val idManifest: ClassManifest[Id] = classManifest[Id]
  val idTag: ClassTag[Id] = classTag[Id]

  /**
   * The session timeout in seconds
   */
  val sessionTimeoutInSeconds: Int = 3600

  /**
   * A function that returns a `User` object from an `Id`.
   * You can alter the procedure to suit your application.
   */
  def resolveUser(id: Id): Option[User] = {
    Option(models.User.find(id))
  }


  /**
   * Where to redirect the user after a successful login.
   */
  def loginSucceeded(request: RequestHeader): Result = {
    val indexUri = routes.Application.index.url
    var uri = request.session.get("access_uri").getOrElse(indexUri)
    uri = new URI(uri).getPath
    if(uri.equals(indexUri)){
      uri = routes.PollsSecured.list.url
    }
    Logger.debug("Login succeeded. Redirecting to uri " + uri)
    Redirect(uri).withSession(request.session - "access_uri")
  }

  /**
   * Where to redirect the user after logging out
   */
  def logoutSucceeded(request: RequestHeader): Result = Redirect(routes.Application.index())

  /**
   * If the user is not logged in and tries to access a protected resource then redirect them as follows:
   */
  def authenticationFailed(request: RequestHeader): Result = {
    Redirect(routes.Application.loginForm).withSession(("access_uri" , request.uri)).flashing(("error" , "Du behöver vara inloggad för att komma åt denna sida"))
  }

  /**
   * If authorization failed (usually incorrect password) redirect the user as follows:
   */
  def authorizationFailed(request: RequestHeader): Result = Forbidden(views.html.error(FORBIDDEN, "Du har inte behörighet att se denna sida eller utföra operationen"))

  /**
   * A function that determines what `Authority` a user has.
   * You should alter this procedure to suit your application.
   */
  def authorize(user: User, authority: Authority): Boolean = {
    AuthHelper.authorize(user, authority)
  }
}

