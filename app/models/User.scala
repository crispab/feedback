package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._
import security.{Permission, NormalUser}
import util.AuthHelper

case class User(
                 id: Pk[Long] = NotAssigned,
                 firstName: String,
                 lastName: String,
                 email: String,
                 phone: String = "",
                 permission: Permission = NormalUser,
                 password: String = "*"
                 )  extends Ordered[User] {
  def compare(that: User) = {
    val c = this.firstName.compare(that.firstName)
    if (c != 0) {
      c
    } else {
      this.lastName.compare(that.lastName)
    }
  }
}

object User {
  import scala.language.postfixOps

  val NOT_CHANGED_PASSWORD = "Y2j1EsDUvc6V" // just a random string

  val parser = {
    get[Pk[Long]]("id") ~
      get[String]("first_name") ~
      get[String]("last_name") ~
      get[String]("email") ~
      get[String]("phone") ~
      get[String]("permission") ~
      get[String]("pwd") map {
      case id ~ firstName ~ lastName ~ email ~ phone ~ permission ~ pwd =>
        User(
          id = id,
          firstName = firstName,
          lastName = lastName,
          email = email.toLowerCase,
          phone = phone,
          permission = Permission.withName(permission),
          password = pwd
        )
    }
  }

  def find(id: Long): User = {
    DB.withTransaction {
      implicit connection =>
        SQL("select * from users where id={id}").on('id -> id).as(User.parser single)
    }
  }


  def findByEmail(email: String): Option[User] = {
    DB.withTransaction {
      implicit connection =>
        SQL("select * from users where lower(email)={email}").on('email -> email.toLowerCase).as(User.parser singleOpt)
    }
  }

  def findAll(): Seq[User] = {
    DB.withTransaction {
      implicit connection =>
        SQL("select * from users u ORDER BY u.first_name, u.last_name").as(User.parser *)
    }
  }

  def create(user: User): Long = {
    DB.withTransaction {
      implicit connection =>
        val password = user.password match {
          case User.NOT_CHANGED_PASSWORD => "*"
          case _ => AuthHelper.calculateHash(user.password)
        }
        SQL(insertQueryString).on(
          'firstName -> user.firstName,
          'lastName -> user.lastName,
          'email -> user.email.toLowerCase,
          'phone -> user.phone,
          'permission -> user.permission.toString,
          'pwd -> password
        ).executeInsert()
    } match {
      case Some(primaryKey: Long) => primaryKey
      case _ => throw new RuntimeException("Could not insert into database, no PK returned")
    }
  }

  val insertQueryString =
    """
INSERT INTO users (
      first_name,
      last_name,
      email,
      phone,
      permission,
      pwd
    )
    values (
      {firstName},
      {lastName},
      {email},
      {phone},
      {permission},
      {pwd}
    )
    """

  def update(id: Long, user: User) {
    if(user.password == NOT_CHANGED_PASSWORD) {
      updateWithoutPassword(id, user)
    } else {
      updateWithPassword(id, user)
    }
  }

  private def updateWithoutPassword(id: Long, user: User) {
    DB.withTransaction {
      implicit connection =>
        SQL(updateWithoutPasswordQueryString).on(
          'id -> id,
          'firstName -> user.firstName,
          'lastName -> user.lastName,
          'email -> user.email.toLowerCase,
          'phone -> user.phone,
          'permission -> user.permission.toString
        ).executeUpdate()
    }
  }

  val updateWithoutPasswordQueryString =
    """
UPDATE users
SET first_name = {firstName},
    last_name = {lastName},
    email = {email},
    phone = {phone},
    permission = {permission}
WHERE id = {id}
    """

  private def updateWithPassword(id: Long, user: User) {
    DB.withTransaction {
      implicit connection =>
        SQL(updateWithPasswordQueryString).on(
          'id -> id,
          'firstName -> user.firstName,
          'lastName -> user.lastName,
          'email -> user.email.toLowerCase,
          'phone -> user.phone,
          'permission -> user.permission.toString,
          'pwd -> AuthHelper.calculateHash(user.password)
        ).executeUpdate()
    }
  }

  val updateWithPasswordQueryString =
    """
UPDATE users
SET first_name = {firstName},
    last_name = {lastName},
    email = {email},
    phone = {phone},
    permission = {permission},
    pwd = {pwd}
WHERE id = {id}
    """

  def delete(id: Long) {
    DB.withTransaction {
      implicit connection => {
        SQL("DELETE FROM users u WHERE u.id={id}").on('id -> id).executeUpdate()
      }
    }
  }

  def verifyUniqueEmail(userToVerify: User): Boolean = {
    val userInDb = User.findByEmail(userToVerify.email)
    userInDb.isEmpty || userToVerify.id.isDefined && (userInDb.get.id == userToVerify.id)
  }
}

