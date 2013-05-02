package models

import play.api.Play.current

import anorm._
import java.util.Date
import anorm.SqlParser._
import anorm.~
import play.api.db.DB

case class Metric(id: Pk[Long] = NotAssigned,
                  when: Date = new Date(),
                  poll: Poll,
                  score: Long,
                  comment: String)


object Metric {

  import scala.language.postfixOps

  private val parser = {
    get[Pk[Long]]("id") ~
      get[Date]("whenx") ~
      get[Long]("poll") ~
      get[Long]("score") ~
      get[String]("comment") map {
      case id ~ whenx ~ poll ~ score ~ comment =>
        Metric(
          id = id,
          when = whenx,
          poll = Poll.find(poll),
          score = score,
          comment = comment
        )
    }
  }

  def findByPoll(poll: Poll): Seq[Metric] = {
    DB.withTransaction {
      implicit connection =>
        SQL("select * from metrics where poll={pollId} order by whenx").on('pollId -> poll.id.get).as(Metric.parser *)
    }
  }

  def create(metric: Metric): Long = {
    DB.withTransaction {
      implicit connection =>
        SQL(insertQueryString).on(
          'whenx -> metric.when,
          'poll -> metric.poll.id,
          'score -> metric.score,
          'comment -> metric.comment
        ).executeInsert()
    } match {
      case Some(primaryKey: Long) => primaryKey
      case _ => throw new RuntimeException("Could not insert into database, no PK returned")
    }
  }

  private val insertQueryString =
    """
INSERT INTO metrics (
      whenx,
      poll,
      score,
      comment
    )
    VALUES (
      {whenx},
      {poll},
      {score},
      {comment}
    )
    """
}