package com.seeta.akka

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActors, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class SampleActorsSpec extends TestKit(ActorSystem("testing")) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Testing messages" should {
    "receive hello message" in {
      val a = system.actorOf(A.props(B.props()))
      a ! InputA
      expectMsgType[Hello]
    }
  }
}
