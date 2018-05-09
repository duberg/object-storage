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
    parallelExecution in Test := false
  )
}