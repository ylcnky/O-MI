package agents

import agentSystem._
import parsing.Types._
import database._

import scala.io.Source
import akka.actor.{ ActorSystem, Actor, ActorRef, Props, Terminated, ActorLogging}
import akka.event.{Logging, LoggingAdapter}
import akka.io.{ IO, Tcp }
import akka.util.{ByteString, Timeout}
import akka.pattern.ask
import scala.language.postfixOps

import java.io.File

/* JSON4s */
import org.json4s._
import org.json4s.native.JsonMethods._

// HTTP related imports
import spray.can.Http
import spray.http._
import HttpMethods._
import spray.client.pipelining._

// Futures related imports

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{ Success, Failure }

import parsing.Types._
import parsing.Types.Path._

// Scala XML
import scala.xml
import scala.xml._

// Mutable map for sensordata
import scala.collection.mutable.Map


// Need to wrap in a package to get application supervisor actor
// "you need to provide exactly one argument: the class of the application supervisor actor"
/**
  * The main program for getting SensorData
  */
class SensorAgent(uri : String) extends IAgentActor {
  // Used to inform that database might be busy
  var loading = false

  // bring the actor system in scope
  // Define formats
  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val system = context.system
  implicit val formats = DefaultFormats
  implicit val timeout = akka.util.Timeout(10 seconds)
  def receive = {
    case Start => 
    queueSensors
  }
  def httpRef = IO(Http) //If problems change to def
    def queueSensors(): Unit = {
      // Set loading to true, 
      loading = true

      system.log.info("Queuing for new sensor data from: " + uri)


      // send GET request with absolute URI (http://121.78.237.160:2100/)
      val futureResponse: Future[HttpResponse] =
        (httpRef ? HttpRequest(GET, Uri(uri))).mapTo[HttpResponse]

      // wait for Future to complete
      futureResponse onComplete {
        case Success(response) =>
          // Json data received from the server
          val json = parse(response.entity.asString)

          // List of (sensorname, value) objects
          val list = for {
            JObject(child) <- json
            JField(sensor, JString(value)) <- child
          } yield (sensor, value)
          addToDatabase(list)
          //        

          system.log.info("Sensors Added to Database!")

          // Schedule for new future in 5 minutes
          //TEST: 1 minute
          akka.pattern.after(300 seconds, using = system.scheduler)(Future { queueSensors() })
          loading = false

        case Failure(error) =>
          loading = false
          system.log.error("An error has occured: " + error.getMessage)
      }
    }

    /**
     * Generate ODF from the parsed & formatted Json data
     * @param list of sensor-value pairs
     * @return generated XML Node
     */
    private def addToDatabase(list: List[(String, String)]): Unit = {
      // Define dateformat for dateTime value
      val date = new java.util.Date()
      var i = 0

      if (!list.isEmpty) {
        // InfoItems filtered out
        SQLite.setMany(list.filter(_._1.split('_').length > 3).map(item => {
          val sensor: String = item._1
          val value: String = item._2 // Currently as string, convert to double?
          // Split name from underlines
          val split = sensor.split('_')

          // Object id
          val path = split.dropRight(2) ++  split.takeRight(2).reverse
          system.log.debug("Data gained from: " + path.mkString("/"))

          ("Objects/" + path.mkString("/"), value)
        }))
      }
    }
  /*
  def queueSensors(): Unit = {
    // Set loading to true, 
    loading = true

    system.log.info("Queuing for new sensor data from: " + uri)


    // send GET request with absolute URI (http://121.78.237.160:2100/)
    val futureResponse: Future[HttpResponse] =
    (httpRef ? HttpRequest(GET, Uri(uri))).mapTo[HttpResponse]

    // wait for Future to complete
    futureResponse onComplete {
      case Success(response) =>
      // Json data received from the server
      val json = parse(response.entity.asString)

      // List of (sensorname, value) objects
      val list = for {
        JObject(child) <- json
        JField(sensor, JString(value)) <- child
      } yield (sensor, value)
      InputPusher.handleObjects(toODFObjSeq(list))

      akka.pattern.after(300 seconds, using = system.scheduler)(Future { queueSensors() })
      loading = false

      case Failure(error) =>
        loading = false
        system.log.error("An error has occured: " + error.getMessage)
    }
  }
  */
  /**
    * Generate ODF from the parsed & formatted Json data
    * @param list of sensor-value pairs
    * @return generated XML Node
    */
  /*
  private def toODFObjSeq(list: List[(String, String)]): Seq[OdfObject] = {
    // Define dateformat for dateTime value
    var date = new java.sql.Timestamp(new java.util.Date().getTime)

    // InfoItems filtered out
    val infoItems: Seq[OdfInfoItem] = list.map{ item =>
      val sensor: String = item._1
      val value: String = item._2 // Currently as string, convert to double?
      // Split name from underlines
      val split = sensor.split('_')
      val room = split.last
      val infoname= split.takeRight(2).head
      val path = Seq("Objects") ++ split.dropRight(2)
      println( path / room / infoname)
      OdfInfoItem(path.toSeq ++ Seq(  room, infoname), Seq(TimedValue(Some(date), value)), "")
    }.toSeq
    val rooms = infoItems.map{ i => 
     i.path.takeRight(2).head
    }
    val roomObjs = rooms.map{ room =>
      OdfObject(Seq("Objects",  "vtt", "otaakari4" , room), Seq.empty,
      infoItems.filter{ i => 
        i.path.takeRight(2).head == room
      }.toSeq)
    }

    val vtt = OdfObject(Seq("Objects","vtt"),roomObjs,Seq())
    return Seq(OdfObject(Seq("Objects"),Seq(vtt),Seq.empty))
  }
  */
}

object SensorAgent {
  def props( uri: String) : Props = {Props(new SensorAgent(uri)) }
}

class SensorBoot extends Bootable {
  private var configPath : String = ""
  private var agentActor : ActorRef = null

  override def startup( system: ActorSystem, pathToConfig: String) : Boolean = { 
    if(pathToConfig.isEmpty || !(new File(pathToConfig).exists()))
      return false 

    configPath = pathToConfig
    val lines = scala.io.Source.fromFile(configPath).getLines().toArray
    var uri = lines.head
    agentActor = system.actorOf(SensorAgent.props(uri), "Sensor-Agent")    
    agentActor ! Start
    return true
  }
  override def shutdown() : Unit = {}
  override def getAgentActor() : ActorRef = agentActor 

}

