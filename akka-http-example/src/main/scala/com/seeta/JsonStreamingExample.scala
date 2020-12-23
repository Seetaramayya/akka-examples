package com.seeta

import akka.{Done, NotUsed}
import akka.actor.{ActorSystem, Props, Status}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.HttpEntity.ChunkStreamPart
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, MediaType, MediaTypes, RequestEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.scaladsl.{Concat, Flow, Framing, Merge, Sink, Source, ZipN}
import akka.stream.{ActorMaterializer, Attributes, OverflowStrategy}
import akka.util.{ByteString, Timeout}
import com.seeta.actor.TweetsActor
import com.seeta.actor.TweetsActor.PutTweets
import spray.json._

import scala.collection.Set
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.StdIn
import scala.util.{Failure, Success}


sealed trait Response
case class Tweet(id: Int, txt: String, timestamp: Long = System.currentTimeMillis()) extends Response
case class Tweets(user: String, tweets: Set[Tweet]) extends Response
case object NewLine extends Response

object MyTweetJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val tweetFormat: RootJsonFormat[Tweet] = jsonFormat3(Tweet.apply)
  implicit val tweetsFormat: RootJsonFormat[Tweets] = jsonFormat2(Tweets.apply)
  implicit val ResponseMarshaller: ToEntityMarshaller[Response] = responseMarshallerConverter(MediaTypes.`application/json`)

  val maxSize = 2048
  def split(data: ByteString): List[ByteString] = {
    @scala.annotation.tailrec
    def loop(input: ByteString, acc: List[ByteString]): List[ByteString] = {
      if (input.isEmpty) acc
      else {
        val (chunk, remaining) = if (input.size > maxSize) input.splitAt(maxSize) else (input, ByteString.empty)
        loop(remaining, acc :+ chunk)
      }
    }

    loop(data, List())
  }

  object Dummy {
    val description = "I want some long tweet, so writing some crap here to fill the space. This will be repeated 1000."
    //TODO: read tweets from twitter, instead of dummy :)
    val thousandTweets: Tweets = Tweets("user1", (1 to 1000).map(i => Tweet(i, description)).toSet)
  }

  val newLine: ByteString = ByteString("\n")
  def responseMarshallerConverter(mediaType: MediaType.WithFixedCharset)(implicit printer: JsonPrinter = CompactPrinter): ToEntityMarshaller[Response] = {
    val marshal: Response => RequestEntity = {
        case NewLine => HttpEntity(mediaType, newLine)
        case t: Tweet => HttpEntity(mediaType, t.toJson.toString())
        case t: Tweets =>
          val flattenedOutput = t.tweets.map(tweet => ByteString(tweet.toJson.toString()) ++ newLine ).reduce(_ ++ _)
          val chunks = split(flattenedOutput)
          println(s"Total chunks are ${chunks.size}")
          println(chunks.map(_.size).mkString("Chunk Sizes are ", ",", "."))
          HttpEntity.Chunked(ContentTypes.`application/json`, Source(chunks).map(ChunkStreamPart.apply))
      }

      Marshaller.withFixedContentType(mediaType)(marshal)
    }
}

// Space in between tweets
// https://doc.akka.io/docs/akka-http/current/routing-dsl/source-streaming-support.html
object JsonStreamingExample extends App {
  import MyTweetJsonProtocol._
  import akka.http.scaladsl.marshalling._

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()
  implicit val timeout = Timeout(2.seconds)
  val tweetActor = system.actorOf(Props(new TweetsActor), "tweets")

  private val getTweets: Route = (pathPrefix("tweets") & get) {
    implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json(1048)
      .withFramingRenderer(Flow[ByteString].map(identity))
    path("queue") {
      complete(infiniteTweetsWithSourceQueue)
    } ~ path("actorRef") {
      complete(infiniteTweetsWithActorRef)
    }
  }

  private val putTweets: Route = (path("tweets") & put) {
    extractRequestContext { context =>
      onComplete(context.request.entity.dataBytes
        .via(Framing.delimiter(newLine, 256))
        .map(_.utf8String)
        .map(_.parseJson.convertTo[Tweet])
        .mapAsync(5)(m => tweetActor ? PutTweets(m))
        .runWith(Sink.ignore)) {
        case Success(value) => complete(StatusCodes.Created)
        case Failure(t)     =>
          t.printStackTrace()
          complete(StatusCodes.InternalServerError)
      }
    }
  }

  val route: Route = getTweets ~ putTweets

  def tweetToByteString(tweet: Tweet): ByteString = ByteString(tweet.toJson.toString())
  lazy val newLinesSource: Source[NewLine.type, NotUsed] = Source.repeat(NewLine)

  private val parallelism = 10
  private val bufferSize = 1000

  def infiniteTweetsWithSourceQueue: Source[Response, NotUsed] = {
    val (queue, source) = Source.queue[Response](bufferSize, OverflowStrategy.backpressure).preMaterialize()
    infiniteTweetsSource
      .mapAsync(parallelism)( tweets => queue.offer(tweets))
      .runWith(Sink.ignore)
      .onComplete(_ => queue.complete())
//    Source.zipN(infiniteTweetSource :: newLinesSource :: Nil).mapAsync(1) { responses =>
//      Future.sequence(responses.map { response =>
//        queue.offer(response)
//      })
//    }
//      .runWith(Sink.ignore).onComplete(_ => queue.complete())

    source
      .log("received message in stream")
      .addAttributes(Attributes.logLevels(onFailure = Logging.WarningLevel))
  }

  def infiniteTweetsWithActorRef: Source[Response, NotUsed] = {
    // backpressure is not supported
    val (actorRef, source) = Source.actorRef[Response](bufferSize, OverflowStrategy.fail).preMaterialize()
    infiniteTweetsSource
      .map { tweets =>
        actorRef ! tweets
      }.recover {
        case t => actorRef ! Status.Failure(t)
      }
      .runWith(Sink.ignore).onComplete(_ => actorRef ! Status.Success(Done))
    source
  }

  private def infiniteTweetsSource: Source[Tweets, NotUsed] = Source.repeat(Dummy.thousandTweets).throttle(1, 1.second)
  private def infiniteTweetSource: Source[Tweet, NotUsed] = Source.cycle(() => Dummy.thousandTweets.tweets.toIterator).throttle(bufferSize, 1.second)

  Http().bindAndHandle(route, "localhost", 9090 )
  println("*" * 40)
  println("Listening on 'localhost:9090'")
  println("ENTER to stop server")
  println("*" * 40)
  StdIn.readLine()
  system.terminate()
}
