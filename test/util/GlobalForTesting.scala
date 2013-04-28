package util

import play.api.GlobalSettings
import play.api.Logger
import play.api.db.DB
import java.io.FileReader

object GlobalForTesting extends GlobalSettings {

  override def onStart(application: play.api.Application) {
    Logger.debug("TestGlobal: onStart called")

    util.Global.onStart(application)

    loadTestData(application)
  }

  private def loadTestData(implicit application: play.api.Application) {
    DB.withTransaction {
      implicit connection => connection
      val scriptRunner = new ScriptRunner(/*connection =*/ connection, /*autoCommit =*/ false, /*stopOnError =*/ true)
      scriptRunner.setLogWriter(new DebugLogPrintWriter(Logger.logger))
      scriptRunner.setErrorLogWriter(new DebugLogPrintWriter(Logger.logger))

      val sqlReader = new FileReader("./test/test_data.sql")
      scriptRunner.runScript(sqlReader)
    }
  }
}
