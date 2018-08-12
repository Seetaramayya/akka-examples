package com.seeta.akka.mailbox.sample

import akka.actor.{ActorSystem, Props}
import com.seeta.akka.cluster.sample.actor.ParentActor
import kamon.Kamon
import kamon.prometheus.PrometheusReporter
import kamon.zipkin.ZipkinReporter

object MailboxTester extends App {
  Kamon.loadReportersFromConfig()

  val system = ActorSystem("mailbox-tester")
  val parent = system.actorOf(Props[ParentActor], "parent")
  parent ! "Start"

  sys.addShutdownHook {
    println("shutting down ...")
  }
}
