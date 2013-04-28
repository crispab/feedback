package util;


import org.slf4j.Logger;

import java.io.PrintWriter;
import java.io.Writer;

public class DebugLogPrintWriter extends PrintWriter {

  private final Logger logger;

  public DebugLogPrintWriter(Logger logger) {
    super(System.out);
    this.logger = logger;
  }

  @Override
  public void flush() {
    // ignore
  }

  @Override
  public void println() {
    logger.debug("");
  }

  @Override
  public void println(Object x) {
    logger.debug(x.toString());
  }
}
