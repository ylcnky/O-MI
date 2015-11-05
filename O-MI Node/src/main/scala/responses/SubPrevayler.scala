package responses

import java.util.Date
import scala.concurrent.duration.{FiniteDuration, Duration}
import scala.concurrent.stm.Ref
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.immutable.HashMap
import scala.util.Try

//import java.util.concurrent.ConcurrentSkipListSet

import akka.actor.{ActorLogging, Actor}
import database._
import org.prevayler.{Transaction, PrevaylerFactory}
import CallbackHandlers._
import types.OmiTypes.SubscriptionRequest

import types.OdfTypes.OdfValue
import types._


import scala.collection.JavaConversions.asScalaIterator

case object HandleIntervals

case object CheckTTL

case class RegisterRequestHandler(reqHandler: RequestHandler)

case class NewSubscription(subscription: SubscriptionRequest)

case class RemoveSubscription(id: Long)

//private val subOrder: Ordering[TTLTimeout] = Ordering.by(_.endTimeMillis)


///**
// * PriorityQueue with subOrder ordering. value with earliest timeout is first.
// * This val is lazy and is computed when needed for the first time
// *
// * This queue contains only subs that have no callback address defined and have ttl > 0.
// */
//private val ttlQueue: ConcurrentSkipListSet[TTLTimeout] = new ConcurrentSkipListSet(subOrder)

case class PrevaylerSub(
                         val id: Long,
                         val ttl: Duration,
                         val interval: Duration,
                         val callback: Option[String],
                         val paths: Seq[Path]

                         )

//TODO remove initial value
class SubscriptionHandler(subIDCounter:Ref[Long] = Ref(0L))(implicit val dbConnection: DB) extends Actor with ActorLogging {

  val scheduler = system.scheduler


//TODO EventSub
  case class AddEventSub(eventSub: SubscriptionRequest, sId: Long) extends Transaction[EventSubs] {
    def executeOn(store: EventSubs, d: Date) = {
      //val sId = subIDCounter.single.getAndTransform(_+1)
      val currentTime = System.currentTimeMillis()

      val expiredSub: Boolean = eventSub.ttl match{
        case finite: FiniteDuration => {
          val finiteDuration = finite - Duration( currentTime - d.getTime(), "milliseconds")
          if(finiteDuration < Duration(0, "seconds")){
            true
          } else {
            //TODO scheduler is optimized for short durations, might fail with long durations find better solution
            scheduler.scheduleOnce(finiteDuration, self, RemoveSubscription(sId)) //possible to tell also where to remove
            finiteDuration
            false
          }
        }
        case other => false//infinite
      }
      if(!expiredSub){
        val paths: Seq[Path] = OdfTypes.getLeafs(eventSub.odf).iterator().map(_.path).toSeq
        val newSub: EventSub = EventSub(
          sId,
          paths,
          ???,
          eventSub.callback,
          OdfValue("", "", None) //TODO do we store subscription values here or in database?
        )
        val newSubs: HashMap[String, Seq[EventSub]] = paths.map(path => (path.toString, Seq(newSub)))(collection.breakOut)
        //store.eventSubs = (store.eventSubs.toSeq ++ newSubs).groupBy(_._1).mapValues(n => n.map(_._2).flatten)(collection.breakOut)
        store.eventSubs = store.eventSubs.merged(newSubs)((a, b) => (a._1, a._2 ++ b._2))
      }
    }
    //      store.data = store.data.copy(name = newName)
  }

  case class AddIntervalSub(intervalSub: IntervalSub) extends Transaction[IntervalSubs] {
    def executeOn(store: IntervalSubs, d: Date) = {
      //val sId = subIDCounter.single.getAndTransform(_+1)
      if(d.after(intervalSub.endTime)){
???
      }
      val currentTime = System.currentTimeMillis()

    }
  }
  //  case class PollSubs(var pollSubs: ConcurrentSkipListSet[TTLTimeout])





  /*
  re schedule when starting in new subscription transactions
  */

  //  val pollPrevayler = PrevaylerFactory.createPrevayler()
  def receive = {
    case NewSubscription(subscription) => {
      subscription.callback match {
        case Some(callback) => subscription.interval match{
          //case dur @ Duration(-1, "seconds") => ??? //eventPrevayler execute AddEventSub()
          //case dur @ Duration(-2, "seconds") => ???
          case dur: FiniteDuration => ???
          case dur => ??? //log.error(Exception("unsupported Duration for subscription"), s"Duration $dur is unsupported")
        }
        case None => ??? //PollSub
      }

    }
      //temp: Any => Unit
  }

  def setSubscription(subscription: SubscriptionRequest): Try[Long] = {
    Try()
    ???
  }

}
