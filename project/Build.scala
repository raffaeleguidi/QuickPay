import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "QuickPay"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    //"commons-httpclient" % "commons-httpclient" % "3.1",
    //"com.h2database" % "h2" % "1.3.168",
    //"com.github.theon" %% "scala-uri" % "0.3.6",
    "com.braintreepayments.gateway" % "braintree-java" % "2.24.0",
    "mysql" % "mysql-connector-java" % "5.1.26",
    "org.webjars" % "webjars-play" % "2.1.0-1",
    "org.webjars" % "bootstrap" % "3.0.0",
    "de.sven-jacobs" % "loremipsum" % "1.0",
    "com.typesafe.play" %% "play-slick" % "0.5.0.2-SNAPSHOT"
    
            
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    scalaVersion := "2.10.2" 
  )

}
