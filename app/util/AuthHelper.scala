package util

import models.User
import models.security.{Permission, Administrator}
import java.security.MessageDigest
import anorm.Pk

object AuthHelper {
  def isAdmin(loggedInUser: Option[User]):Boolean = {
    if (loggedInUser.isDefined) {
      authorize(loggedInUser.get, Administrator)
    } else {
      false
    }
  }

  def isSameOrAdmin(loggedInUser: Option[User], userId: Pk[Long]): Boolean = {
    if (loggedInUser.isDefined) {
      (loggedInUser.get.id == userId) || authorize(loggedInUser.get, Administrator)
    } else {
      false
    }
  }

  def isSameOrAdmin(loggedInUser: Option[User], user: User): Boolean = {
    if (loggedInUser.isDefined) {
      (loggedInUser.get.id == user.id) || authorize(loggedInUser.get, Administrator)
    } else {
      false
    }
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
