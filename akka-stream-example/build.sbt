name := "akka-stream-example"

version := "1.0"

scalaVersion := "2.12.6"

lazy val akkaVersion = "2.5.25"
lazy val jodaVersion = "2.8.1"
lazy val akkaStreamVersion = "2.5.25"
lazy val scalaTestVersion = "3.0.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka"       %% "akka-actor"              % akkaVersion,
  "com.typesafe.akka"       %% "akka-testkit"            % akkaVersion,
  "com.typesafe.akka"       %% "akka-stream"             % akkaStreamVersion,
  "joda-time"               %  "joda-time"               % jodaVersion,
  "org.scalatest"           %% "scalatest"               % scalaTestVersion              % "test"
)
