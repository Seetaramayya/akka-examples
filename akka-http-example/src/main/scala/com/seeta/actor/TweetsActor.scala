package com.seeta.actor

import akka.actor.{Actor, ActorLogging}
import com.seeta.Tweet
import com.seeta.actor.TweetsActor.{Done, GetTweets, PutTweets}

import scala.collection.mutable

object TweetsActor {
  sealed trait Command
  case object GetTweets extends Command
  case class PutTweets(tweet: Tweet) extends Command

  case object Done
}

class TweetsActor extends Actor with ActorLogging {
  private val tweets: mutable.Set[Tweet] = mutable.Set()
  override def receive: Receive = {
    case GetTweets        => sender() ! tweets.toList
    case PutTweets(tweet) => tweets += tweet
      sender() ! Done
  }
}
