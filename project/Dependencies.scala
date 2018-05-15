import sbt._

object Dependencies {
  val akkaVersion = "2.5.12"
  val akkaHttpVersion = "10.1.1"
  val akkaPersistenceCassandra = "0.84"
  val scalameterVersion = "0.8.2"
  val scalatestVersion = "3.0.5"
  val circleVersion = "0.9.3"

  object Akka {
    val groupID = "com.typesafe.akka"
    val actor = groupID %% "akka-actor" % akkaVersion
    val http = groupID %% "akka-http" % akkaHttpVersion
    val httpTestKit = groupID %% "akka-http-testkit" % akkaHttpVersion % Test
    val persistence = groupID %% "akka-persistence" % akkaVersion
    val stream = groupID %% "akka-stream" % akkaVersion
    val persistenceCassandra = groupID %% "akka-persistence-cassandra" % akkaPersistenceCassandra
    val persistenceCassandraLauncher = groupID %% "akka-persistence-cassandra-launcher" % akkaPersistenceCassandra % Test
    val slf4j = groupID %% "akka-slf4j" % akkaVersion
    val all = Seq(actor, http, httpTestKit, persistence, stream, persistenceCassandra, persistenceCassandraLauncher, slf4j)
  }

  object Circe {
    val groupID = "io.circe"
    val core = groupID %% "circe-core" % circleVersion
    val generic = groupID %% "circe-generic" % circleVersion
    val parser = groupID %% "circe-parser" % circleVersion
    val java8 = groupID %% "circe-java8" % circleVersion
    val all = Seq(core, generic, parser, java8)
  }

  val scalameter = "com.storm-enroute" %% "scalameter" % scalameterVersion

  val scalactic = "org.scalactic" %% "scalactic" % scalatestVersion
  val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion

  val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.3"

  val all = Seq(
    scalameter,
    scalactic,
    scalatest,
    logbackClassic
  ) ++ Akka.all ++ Circe.all
}