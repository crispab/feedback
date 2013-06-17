package controllers

import play.api.mvc._
import models.{Metric, User, Poll}
import jp.t2v.lab.play2.auth.{OptionalAuthElement, AuthElement}
import models.security.{Administrator, NormalUser}
import play.api.data.Form
import play.api.data.Forms._
import anorm.{Pk, NotAssigned}
import util.AuthHelper._


object Polls extends Controller with OptionalAuthElement with AuthConfigImpl {

  def show(uuid: String) = StackAction { implicit request =>
    implicit val loggedInUser = Option(loggedIn)
    try {
      val pollToShow = Poll.findByUuid(uuid)
      val metrics = Metric.findByPoll(pollToShow)
      Ok(views.html.polls.show(pollToShow, metrics))
    } catch {
      case e: Exception => NotFound(views.html.error(NOT_FOUND, "Kan inte hitta undersökning med id '" + uuid + "'"))
    }
  }
}



object PollsSecured extends Controller with AuthElement with AuthConfigImpl {

  def list = StackAction(AuthorityKey -> hasPermission(NormalUser)_) { implicit request =>
    implicit val loggedInUser = Option(loggedIn)
    val pollsToList = Poll.findAll()
    Ok(views.html.polls.list(pollsToList))
  }

  def createForm(forConsultantId: Option[Long] = None) = StackAction(AuthorityKey -> hasPermissionOrSelf(Administrator, userId = forConsultantId)_) { implicit request =>
    implicit val loggedInUser = Option(loggedIn)
    val consultants = User.findAll()
    Ok(views.html.polls.edit(pollForm, consultants = consultants, forConsultantId = forConsultantId))
  }

  def create = StackAction(AuthorityKey -> hasPermission(NormalUser)_) { implicit request =>
    implicit val loggedInUser = Option(loggedIn)
    pollForm.bindFromRequest.fold(
      formWithErrors => {
        val consultants = User.findAll()
        BadRequest(views.html.polls.edit(formWithErrors, consultants = consultants))
      },
      poll => {
        if(isAdminOrSelf(loggedInUser, poll.consultant)){
          Poll.create(poll)
          Redirect(routes.PollsSecured.list())
        } else {
          Unauthorized(views.html.error(UNAUTHORIZED, "Du har inte behörighet att skapa undersökningar för användaren " + poll.consultant.firstName + " " + poll.consultant.lastName))
        }
      }
    )
  }

  def updateForm(uuid: String) = StackAction(AuthorityKey -> hasPermission(NormalUser)_) { implicit request =>
    implicit val loggedInUser = Option(loggedIn)
    val pollToUpdate = Poll.findByUuid(uuid)
    val consultants = User.findAll()
    if(isAdmin(loggedInUser)){
      Ok(views.html.polls.edit(pollForm.fill(pollToUpdate), uuidToUpdate = Option(uuid), consultants = consultants))
    } else if(isSelf(loggedInUser, pollToUpdate.consultant)){
      Ok(views.html.polls.edit(pollForm.fill(pollToUpdate), uuidToUpdate = Option(uuid), consultants = consultants, forConsultantId = Option(loggedInUser.get.id.get)))
    } else {
      Unauthorized(views.html.error(UNAUTHORIZED, "Du har inte behörighet att redigera undersökningar för användaren " + pollToUpdate.consultant.firstName + " " + pollToUpdate.consultant.lastName))
    }
  }

  def update(uuid: String) = StackAction(AuthorityKey -> hasPermission(NormalUser)_) { implicit request =>
    implicit val loggedInUser = Option(loggedIn)
    pollForm.bindFromRequest.fold(
      formWithErrors => {
        val consultants = User.findAll()
        BadRequest(views.html.polls.edit(formWithErrors, uuidToUpdate = Option(uuid), consultants = consultants))
      },
      poll => {
        if(isAdminOrSelf(loggedInUser, poll.consultant)){
          Poll.update(uuid, poll)
          Redirect(routes.Polls.show(uuid))
        } else {
          Unauthorized(views.html.error(UNAUTHORIZED, "Du har inte behörighet att redigera undersökningar för användaren " + poll.consultant.firstName + " " + poll.consultant.lastName))
        }
      }
    )
  }


  val pollForm: Form[Poll] = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "uuid" -> ignored(""),
      "customer" -> nonEmptyText(maxLength = 127),
      "contactPerson" -> optional(text(maxLength = 127)),
      "assignment" -> optional(text(maxLength = 127)),
      "consultantId" -> longNumber,
      "isOpen" -> boolean
    )(toPoll)(fromPoll)
  )

  def toPoll(id: Pk[Long], uuid: String, customer: String, contactPerson:Option[String], assignment:Option[String], consultantId: Long, isOpen: Boolean): Poll = {
    Poll(id = id,
      uuid = uuid,
      customer = customer,
      contactPerson = contactPerson.getOrElse(""),
      assignment = assignment.getOrElse(""),
      consultant = User.find(consultantId),
      isOpen = isOpen)
  }

  def fromPoll(poll: Poll) = {
    Option((poll.id, poll.uuid, poll.customer, Option(poll.contactPerson), Option(poll.assignment), poll.consultant.id.get, poll.isOpen))
  }

}


