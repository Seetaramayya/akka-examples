name := "akka-http-example"

version := "1.0"

scalaVersion := "2.12.6"

lazy val akkaVersion = "2.5.21"
lazy val jodaVersion = "2.8.1"
lazy val akkaHttpVersion = "10.1.10"
lazy val akkaStreamVersion = "2.5.25"
lazy val scalaTestVersion = "3.0.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka"       %% "akka-actor"              % akkaVersion,
  "com.typesafe.akka"       %% "akka-testkit"            % akkaVersion,
  "com.typesafe.akka"       %% "akka-http"               % akkaHttpVersion,
  "com.typesafe.akka"       %% "akka-http-spray-json"    % akkaHttpVersion,
  "com.typesafe.akka"       %% "akka-stream"             % akkaStreamVersion,
  "joda-time"               %  "joda-time"               % jodaVersion,
  "org.scalatest"           %% "scalatest"               % scalaTestVersion              % "test"
)
