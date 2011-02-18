import sbt._

class ChirperStreamerProject(info: ProjectInfo) extends DefaultProject(info) with AutoCompilerPlugins
{
  override def useDefaultConfigurations = true

  val httpclient = "commons-httpclient" % "commons-httpclient" % "3.1"
  val logging    = "commons-logging" % "commons-logging" % "1.1"
  val json = "org.json" % "json" % "20080701"
  // Logging
  System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
  // System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
  // System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");

  // Show unchecked errors when compiling
  override def compileOptions = super.compileOptions ++ Seq(Unchecked)
}
