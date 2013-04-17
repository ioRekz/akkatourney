import sbt._
import Keys._

object BuildSettings {
  val buildName = "akkatourney"
  val buildOrganization = "ioio"
  val buildVersion      = "0.1-SNAPSHOT"
  val buildScalaVersion = "2.10.0"


  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion,
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
  )
}

object ApplicationBuild extends Build {

  val typesafeRepo = Seq(
    "Typesafe repository snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
    "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/"
  )

  lazy val datomic = Project(
    "akkatourney", file("."),
    settings = BuildSettings.buildSettings ++ Seq(
      resolvers ++= typesafeRepo,
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor" % "2.1.0",
        "com.typesafe.akka" %% "akka-testkit" % "2.1.0",
        "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"
      )
    )
  )
}
