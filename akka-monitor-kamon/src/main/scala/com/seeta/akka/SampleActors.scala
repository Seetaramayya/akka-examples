package com.seeta.akka

import akka.actor.{Actor, ActorRef, Props}


sealed trait Message
case object InputA extends Message
case class InputB(replyTo: ActorRef) extends Message
case class Hello(string: String) extends Message

object A {
  def props(childProps: Props) = Props(new A(childProps))
}
class A(props: Props) extends Actor {
  private val child = context.actorOf(props)
  override def receive: Receive = {
    case InputA =>
      println(s"InputA from ${sender()}")
      child ! InputB(sender())
  }
}

object B {
  def props(): Props = Props(new B())
}

class B extends Actor {
  override def receive: Receive = {
    case InputB(replyTo) =>
      println(s"InputB from ${sender()} and reply to $replyTo")
      replyTo ! Hello(s"hello $replyTo")
  }
}
