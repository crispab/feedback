package controllers

import models._
import play.api.data.Form
import play.api.data.Forms._
import anorm.{Pk, NotAssigned}
import play.api.mvc._
import jp.t2v.lab.play2.auth.Auth

object Metrics extends Controller with Auth with AuthConfigImpl {

  def createForm(uuid: String) = optionalUserAction { implicit user => implicit request =>
    try {
      val forPoll = Poll.findByUuid(uuid)
      Ok(views.html.metrics.createForm(forPoll))
    } catch {
      case e: Exception => NotFound(views.html.error(NOT_FOUND, "Kan inte hitta undersökning med id '" + uuid + "'"))
    }
  }

  def create = optionalUserAction { implicit user => implicit request =>
    metricForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.error(BAD_REQUEST, "Fel i inmatningen")),
      metric => {
        Metric.create(metric)
        Redirect(routes.Polls.show(metric.poll.uuid))
      }
    )
  }

  private val metricForm:Form[Metric] =
    Form(
      mapping(
        "id" -> ignored(NotAssigned:Pk[Long]),
        "uuid" -> nonEmptyText,
        "score" -> longNumber,
        "comment" -> text(maxLength = 256)
      )(toMetric)(fromMetric)
    )

  private def toMetric(
                       id: Pk[Long],
                       uuid: String,
                       score: Long,
                       comment: String): Metric = {
    Metric(
      id = id,
      poll = Poll.findByUuid(uuid),
      score = score,
      comment = comment
    )
  }

  private def fromMetric(metric: Metric) = {
    Option((metric.id, metric.poll.uuid, metric.score, metric.comment))
  }

}

