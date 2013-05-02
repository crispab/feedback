package controllers

import play.api.mvc._
import models.Poll
import jp.t2v.lab.play2.auth.Auth
import models.security.NormalUser

object Polls extends Controller with Auth with AuthConfigImpl {

  def list = authorizedAction(NormalUser) { user => implicit request =>
    implicit val loggedInUser = Option(user)
    val pollsToList = Poll.findAll()
    Ok(views.html.polls.list(pollsToList))
  }

  def show(uuid: String) = optionalUserAction { implicit user => implicit request =>
    try {
      val pollToShow = Poll.findByUuid(uuid)
      Ok(views.html.polls.show(pollToShow))
    } catch {
      case e: Exception => NotFound(views.html.error(NOT_FOUND, "Kan inte hitta undersökning med id '" + uuid + "'"))
    }
  }
}

