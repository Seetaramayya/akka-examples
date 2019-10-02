package com.seeta

import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{RequestContext, Route, RouteResult}
import akka.stream.ActorMaterializer
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.io.StdIn
object ActorPerRequestLikeSpray extends App {
  object RequestHandler {
    case class Handle(ctx: ImperativeRequestContext)
  }
  class RequestHandler extends Actor {
    import RequestHandler._
    def receive = {
      case Handle(ctx) =>
        ctx.complete("ok")
        context.stop(self)
    }
  }
  // an imperative wrapper for request context
  final class ImperativeRequestContext(ctx: RequestContext, promise: Promise[RouteResult]) {
    private implicit val ec = ctx.executionContext
    def complete(obj: ToResponseMarshallable): Unit = ctx.complete(obj).onComplete(promise.complete)
    def fail(error: Throwable): Unit = ctx.fail(error).onComplete(promise.complete)
  }
  // a custom directive
  def imperativelyComplete(inner: ImperativeRequestContext => Unit): Route = { ctx =>
    val p = Promise[RouteResult]()
    inner(new ImperativeRequestContext(ctx, p))
    p.future
  }
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  val route =
    pathEndOrSingleSlash {
      get {
        imperativelyComplete { ctx =>
          system.actorOf(Props[RequestHandler]) ! RequestHandler.Handle(ctx)
        }
      }
    }
  Http().bindAndHandle(route, "localhost", 8080)

  println("ENTER to terminate, curl http://localhost:8080/ responds with 'ok' >>")
  StdIn.readLine()
  system.terminate()
}