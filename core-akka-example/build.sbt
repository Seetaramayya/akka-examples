name := "core-akka-example"

version := "1.0"

scalaVersion := "2.12.6"

lazy val akkaVersion = "2.6.15"
lazy val jodaVersion = "2.8.1"
lazy val scalaTestVersion = "3.0.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"         % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed"   % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit"       % akkaVersion,
  "joda-time"         %  "joda-time"          % jodaVersion,
  "org.scalatest"     %% "scalatest"          % scalaTestVersion             % "test"
)
