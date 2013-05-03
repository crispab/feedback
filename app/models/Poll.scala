package models

import play.api.Play.current

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import org.apache.commons.lang3.RandomStringUtils

case class Poll(
                 id: Pk[Long] = NotAssigned,
                 uuid: String = "",
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
        SQL("SELECT * FROM polls WHERE id={id}").on('id -> id).as(Poll.parser single)
    }
  }

  def findByUuid(uuid: String): Poll = {
    DB.withTransaction {
      implicit connection =>
        SQL("SELECT * FROM polls WHERE uuid={uuid}").on('uuid -> uuid).as(Poll.parser single)
    }
  }

  def findAll(): Seq[Poll] = {
    DB.withTransaction {
      implicit connection =>
        SQL("SELECT * FROM polls p ORDER BY p.is_open DESC, p.customer, p.assignment").as(Poll.parser *)
    }
  }

  def create(poll: Poll): Long = {
    DB.withTransaction {
      implicit connection =>
        SQL(insertQueryString).on(
          'uuid -> RandomStringUtils.randomAlphanumeric(17),
          'customer -> poll.customer,
          'contactPerson -> poll.contactPerson,
          'assignment -> poll.assignment,
          'consultant -> poll.consultant.id.get,
          'isOpen -> poll.isOpen
        ).executeInsert()
    } match {
      case Some(primaryKey: Long) => primaryKey
      case _ => throw new RuntimeException("Could not insert into database, no PK returned")
    }
  }

  private val insertQueryString =
    """
INSERT INTO polls (
      uuid,
      customer,
      contact_person,
      assignment,
      consultant,
      is_open
    )
    values (
      {uuid},
      {customer},
      {contactPerson},
      {assignment},
      {consultant},
      {isOpen}
    )
    """

  def update(uuid: String, poll: Poll) {
    DB.withTransaction {
      implicit connection =>
        SQL(updateQueryString).on(
          'uuid -> uuid,
          'customer -> poll.customer,
          'contactPerson -> poll.contactPerson,
          'assignment -> poll.assignment,
          'consultant -> poll.consultant.id.get,
          'isOpen -> poll.isOpen
        ).executeUpdate()
    }
  }

  private val updateQueryString =
    """
UPDATE polls
SET customer = {customer},
    contact_person = {contactPerson},
    assignment = {assignment},
    consultant = {consultant},
    is_open = {isOpen}
WHERE uuid = {uuid}
    """

}