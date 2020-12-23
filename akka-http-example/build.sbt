import Dependencies._
name := "akka-http-example"

version := "1.0"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  AkkaActor, AkkaTestKit, AkkaStream, AkkaHttp, SprayJson, KafkaStream, Joda, Jackson, ScalaTest
)
