name := """ok-note"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-language:reflectiveCalls", "-language:postfixOps", "-language:implicitConversions")

resolvers ++= Seq(
	"Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases",
	"Atlassian Releases" at "https://maven.atlassian.com/public/",
	Resolver.sonatypeRepo("snapshots")
)

routesGenerator := InjectedRoutesGenerator

pipelineStages := Seq(gzip)

doc in Compile <<= target.map(_ / "none")


libraryDependencies ++= Seq(
	jdbc,
  cache,
  ws,
  specs2 % Test,
	"org.webjars" % "requirejs" % "2.1.19",
	"com.mohiva" %% "play-silhouette" % "3.0.0",
	"com.adrianhurt" %% "play-bootstrap3" % "0.4.4-P24",	// Add bootstrap3 helpers and field constructors (http://play-bootstrap3.herokuapp.com/)
	"com.typesafe.play" %% "play-mailer" % "3.0.1"
)

libraryDependencies ++= Seq(
	"com.zaxxer" % "HikariCP" % "2.4.1",
	"com.typesafe.slick" %% "slick" % "3.1.1",
	"com.typesafe.slick" %% "slick-hikaricp" % "3.1.1",
	"org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
	"com.github.tminglei" %% "slick-pg" % "0.12.0",
	"com.github.tminglei" %% "slick-pg_play-json" % "0.12.0",
	"com.github.tminglei" %% "slick-pg_joda-time" % "0.12.0",
	"com.github.tototoshi" %% "slick-joda-mapper" % "2.1.0"
)

libraryDependencies ++= Seq(
	"javax.inject" % "javax.inject" % "1",
	"joda-time" % "joda-time" % "2.9.2",
	"org.joda" % "joda-convert" % "1.2",
	"com.google.inject" % "guice" % "4.0"
)

fork in run := true