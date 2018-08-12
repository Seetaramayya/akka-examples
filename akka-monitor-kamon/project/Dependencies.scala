import sbt._
import Keys._

object Dependencies {
  lazy val akkaVersion = "2.5.12"
  lazy val dependencies = Seq(
    "com.typesafe.akka"      %% "akka-cluster"                          % akkaVersion,
    "io.kamon"               %% "kamon-core"                            % "1.1.2",
    "io.kamon"               %% "kamon-logback"                         % "1.0.0",
    "io.kamon"               %% "kamon-akka-2.5"                        % "1.0.1",
    "io.kamon"               %% "kamon-graphite"                        % "1.2.0"
  )
}
