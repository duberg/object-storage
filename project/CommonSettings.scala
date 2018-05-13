import sbt.Keys._
import sbt._

object CommonSettings {
  lazy val commonSettings: Seq[Def.Setting[_]] = Seq(
    name := "object-storage",
    organization := "ru.kantemirov.object-storage",
    version := "0.1.2-SNAPSHOT",
    scalaVersion := "2.12.6",
    libraryDependencies ++= Dependencies.all,
    testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
    parallelExecution in Test := false,
    scalacOptions ++= Seq(
      "-target:jvm-1.8",
      "-encoding", "UTF-8",
      "-deprecation", // warning and location for usages of deprecated APIs
      "-feature", // warning and location for usages of features that should be imported explicitly
      "-unchecked", // additional warnings where generated code depends on assumptions
      "-language:postfixOps",
      "-language:implicitConversions"
    )
  )
}