import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "organizer"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "com.github.nscala-time" %% "nscala-time" % "0.4.2"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
//    resolvers += "ancelin" at "https://raw.github.com/mathieuancelin/play2-couchbase/master/repository/snapshots",
  )

}
