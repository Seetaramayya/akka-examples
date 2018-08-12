import com.typesafe.sbt.SbtMultiJvm.multiJvmSettings

name := "akka-examples"

version := "0.1"

scalaVersion := "2.12.6"

lazy val root = (project in file("."))
  .enablePlugins(MultiJvmPlugin)
  .settings(multiJvmSettings: _*)
  .settings(libraryDependencies ++= Dependencies.dependencies)
  .configs(MultiJvm)