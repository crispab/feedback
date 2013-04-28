package util

import java.util.TimeZone
import play.api.GlobalSettings
import play.api.Logger

object Global extends GlobalSettings {

  override def onStart(app: play.api.Application) {
    Logger.debug("onStart called")

    setTimeZoneToCET
  }

  def setTimeZoneToCET() {
    // not so pretty, but convenient since Heroku servers run in another time zone
    TimeZone.setDefault(TimeZone.getTimeZone("Europe/Stockholm"))
  }
}
