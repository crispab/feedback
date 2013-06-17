package util

import models.User
import models.security.{Permission, Administrator}
import java.security.MessageDigest
import anorm.Pk

object AuthHelper {

  def hasPermission(permission: Permission)(loggedInUser: User): Boolean = authorize(loggedInUser, permission)

  def hasPermissionOrSelf(permission: Permission, userId: Long)(loggedInUser: User): Boolean = authorize(loggedInUser, permission) || (userId == loggedInUser.id.get)

  def hasPermissionOrSelf(permission: Permission, userId: Option[Long])(loggedInUser: User): Boolean = authorize(loggedInUser, permission) || (userId.isDefined && (userId.get == loggedInUser.id.get))

  def isAdmin(loggedInUser: Option[User]):Boolean = {
    if (loggedInUser.isDefined) {
      authorize(loggedInUser.get, Administrator)
    } else {
      false
    }
  }

  def isAdminOrSelf(loggedInUser: Option[User], userId: Pk[Long]): Boolean = {
    if (loggedInUser.isDefined) {
      (loggedInUser.get.id == userId) || authorize(loggedInUser.get, Administrator)
    } else {
      false
    }
  }

  def isAdminOrSelf(loggedInUser: Option[User], user: User): Boolean = {
    if (loggedInUser.isDefined) {
      (loggedInUser.get.id == user.id) || authorize(loggedInUser.get, Administrator)
    } else {
      false
    }
  }

  def isSelf(loggedInUser: Option[User], user: User): Boolean = {
    loggedInUser.isDefined && (loggedInUser.get.id == user.id)
  }

  def isLoggedIn(loggedInUser: Option[User]): Boolean = {
    loggedInUser.isDefined
  }

  def authorize(user: User, permission: Permission): Boolean = {
    user.permission match {
      case Administrator => true
      case _ => user.permission == permission
    }
  }

  def checkPassword(user : Option[User], enteredPassword: String): Option[User] = {
    user.filter(usr => usr.password.toLowerCase == calculateHash(enteredPassword))
  }


  def calculateHash(password: String): String = {
    val hash = MessageDigest.getInstance("MD5").digest((password + salt).getBytes).map("%02X".format(_)).mkString.toLowerCase
    hash
  }

  import play.api.Play.current
  private def salt = play.api.Play.configuration.getString("password.salt").getOrElse("")

}
