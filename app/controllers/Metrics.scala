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
      Ok(views.html.metrics.create(metricForm, forPoll))
    } catch {
      case e: Exception => NotFound(views.html.error(NOT_FOUND, "Kan inte hitta undersökning med id '" + uuid + "'"))
    }
  }

  def create(uuid: String) = optionalUserAction { implicit user => implicit request =>
    metricForm.bindFromRequest.fold(
      formWithErrors => {
        val forPoll = Poll.findByUuid(uuid)
        BadRequest(views.html.metrics.create(formWithErrors, forPoll))
      },
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
        "comment" -> text(maxLength = 256),
        "name" -> text(maxLength = 127)
      )(toMetric)(fromMetric)
    )

  private def toMetric(
                       id: Pk[Long],
                       uuid: String,
                       score: Long,
                       comment: String,
                       name: String): Metric = {
    Metric(
      id = id,
      poll = Poll.findByUuid(uuid),
      score = score,
      comment = comment,
      name = name
    )
  }

  private def fromMetric(metric: Metric) = {
    Option((metric.id, metric.poll.uuid, metric.score, metric.comment, metric.name))
  }

}

