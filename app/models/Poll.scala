package models

import play.api.Play.current

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import java.util.UUID

case class Poll(
                 id: Pk[Long] = NotAssigned,
                 uuid: String = UUID.randomUUID().toString,
                 customer: String,
                 contactPerson: String,
                 assignment: String,
                 consultant: User,
                 isOpen: Boolean = true
                 )


object Poll {

  import scala.language.postfixOps

  private val parser = {
    get[Pk[Long]]("id") ~
    get[String]("uuid") ~
    get[String]("customer") ~
    get[String]("contact_person") ~
    get[String]("assignment") ~
    get[Long]("consultant") ~
    get[Boolean]("is_open") map {
      case id ~ uuid ~ customer ~ contact_person ~ assignment ~ consultant ~ is_open =>
        Poll(
          id = id,
          uuid = uuid,
          customer = customer,
          contactPerson = contact_person,
          assignment = assignment,
          consultant = User.find(consultant),
          isOpen = is_open
        )
    }
  }

  def find(id: Long): Poll = {
    DB.withTransaction {
      implicit connection =>
        SQL("select * from polls where id={id}").on('id -> id).as(Poll.parser single)
    }
  }

  def findByUuid(uuid: String): Poll = {
    DB.withTransaction {
      implicit connection =>
        SQL("select * from polls where uuid={uuid}").on('uuid -> uuid).as(Poll.parser single)
    }
  }

  def findAll(): Seq[Poll] = {
    DB.withTransaction {
      implicit connection =>
        SQL("select * from polls p ORDER BY p.is_open, p.customer, p.assignment").as(Poll.parser *)
    }
  }

}