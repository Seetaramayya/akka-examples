import sbt._

object Dependencies {
  lazy private val AkkaVersion = "2.5.31"
  lazy private val AkkaHttpVersion = "10.1.10"
  lazy private val AlpAkkaVersion = "2.0.6"
  lazy private val JodaVersion = "2.8.1"
  lazy private val JacksonVersion = "2.10.5.1"
  lazy private val ScalaTestVersion = "3.0.5"

  // https://github.com/akka/akka
  lazy val AkkaActor = "com.typesafe.akka" %% "akka-actor" % AkkaVersion
  lazy val AkkaTestKit = "com.typesafe.akka" %% "akka-testkit" % AkkaVersion
  lazy val AkkaStream = "com.typesafe.akka" %% "akka-stream" % AkkaVersion

  // https://github.com/akka/akka-http
  lazy val AkkaHttp = "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
  lazy val SprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion

  // https://github.com/akka/alpakka-kafka depends on kafka client : 2.4.1 https://github.com/apache/kafka
  lazy val KafkaStream = "com.typesafe.akka" %% "akka-stream-kafka" % AlpAkkaVersion

  lazy val Joda = "joda-time" % "joda-time" % JodaVersion

  lazy val Jackson = "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVersion

  lazy val ScalaTest = "org.scalatest" %% "scalatest" % ScalaTestVersion % "test"
}
