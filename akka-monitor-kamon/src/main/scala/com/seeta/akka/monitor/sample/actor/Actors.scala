package com.seeta.akka.monitor.sample.actor

import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.dispatch.{BoundedDequeBasedMessageQueueSemantics, RequiresMessageQueue}

import scala.concurrent.duration._

case class Response(client: ActorRef, message: String)
case class Request(id: Long)

class InitialActor extends Actor
  with ActorLogging
  with RequiresMessageQueue[BoundedDequeBasedMessageQueueSemantics] {
  private[this] val child = context.actorOf(Props[ChildActor], "child")
  private[this] val started = new AtomicBoolean(false)
  private[this] val counter = new AtomicLong()

  override def receive: Receive = {
    case "Start" if !started.get() =>
      started.set(true)
      log.info("Started")
      import context.dispatcher
      context.system.scheduler.schedule(0.second, 100.millis, self, "Tick")
    case "Start" =>
      log.info("Actor already started, nothing to start")
      sender() ! "Started already"
    case "Tick" =>
      val id = counter.incrementAndGet()
      log.info("Tick {} sending to client", id)
      child ! Request(id)
    case m: Response => log.info("Response is {}", m)
  }
}

class ChildActor extends Actor
  with ActorLogging
  with RequiresMessageQueue[BoundedDequeBasedMessageQueueSemantics] {
  var counter = new AtomicLong()
  override def receive: Receive = {
    case r @ Response(client, _)         => client ! r
    case Request(id) =>
      log.info("Request id {}", id)
      import context.dispatcher
      val client = sender()
      val message = Response(client, s"Request: $id, Response ${counter.getAndIncrement()}")
      context.system.scheduler.scheduleOnce(0.second, self, message)
  }
}