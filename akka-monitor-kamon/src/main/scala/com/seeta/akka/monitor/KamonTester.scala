package com.seeta.akka.monitor

import akka.actor.{ActorSystem, Props}
import com.seeta.akka.monitor.sample.actor.InitialActor
import kamon.Kamon

object KamonTester extends App {
  Kamon.loadReportersFromConfig()
  val system = ActorSystem("kamon-tester")
  val parent = system.actorOf(Props[InitialActor], "parent")
  parent ! "Start"
}
