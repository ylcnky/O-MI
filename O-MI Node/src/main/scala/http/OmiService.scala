package http

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import responses._

import parsing._
import sensorDataStructure.SensorMap

class OmiServiceActor(val sensormap: SensorMap) extends Actor with OmiService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)

}


// this trait defines our service behavior independently from the service actor
trait OmiService extends HttpService {

  val sensormap: SensorMap

  //Get the files from the html directory; http://localhost:8080/html/form.html
  val staticHtml =
    pathPrefix("html"){
      getFromDirectory("html")
    }

  // should be removed
  val helloWorld = 
    path("") { // Root
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Say hello to <i>spray-routing</i> on <i>spray-can</i>!</h1>
              </body>
            </html>
          }
        }
      }
    }

  val getDataDiscovery = 
    path(Rest){ path =>
      get {
        complete {
          Read.generateODF(path, sensormap)
        }
      }
    }

  // Combine all handlers
  val myRoute = helloWorld ~ staticHtml ~ getDataDiscovery

}
