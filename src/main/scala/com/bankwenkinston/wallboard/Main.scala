package com.bankwenkinston.wallboard

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives.{encodeResponse, _}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.io.StdIn

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Nov-2016
  */
object Main {
  def main(args: Array[String]): Unit = {
    if (args.length < 1) {
      println("Expected path for static resources")
      sys.exit(1)
    }

    val staticRoot = args(0)

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher


    import akka.stream.scaladsl.GraphDSL.Implicits._

    val mainSocket: Flow[Message, Message, Any] = Flow[Message].mapConcat({
      case tm: TextMessage =>
        TextMessage(Source.single("Hello ") ++ tm.textStream ++ Source.single("!")) :: Nil
      case bm: BinaryMessage =>
        // ignore binary messages but drain content to avoid the stream being clogged
        bm.dataStream.runWith(Sink.ignore)
        Nil
    }) //~> Flow[Message].map(_)

    //val testSocket: Flow[Message, Message, Any] = Flow[Message] ~

    val route: Flow[HttpRequest, HttpResponse, Any] =
      path("ws") {
        parameters("type" ! "client", "serverId") { (serverId) =>
          val maybeServer: Option[Server] = Server.get(serverId)
          if (maybeServer.isDefined) {
            handleWebSocketMessages(ClientGraph(maybeServer.get))
          }
          else {
            complete(notFound)
          }
        } ~
        parameters("type" ! "server") {
          complete(s"hi server")
        }
      } ~ encodeResponse {
        getFromDirectory(staticRoot)
      }
      /*
      path("client") {
        parameters("serverId") {
          (serverId) => {
            complete(s"The color is '$serverId'")
            //handleWebSocketMessages(mainSocket)
          }
        }
      } ~ path("server") {
        handleWebSocketMessages(mainSocket)
      }*/

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

  val notFound: HttpResponse = HttpResponse(404, entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,
    """
      |<html>
      | <body>404</body>
      |</html>
    """.stripMargin))
}
