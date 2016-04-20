package responses

import akka.testkit.TestProbe
import org.specs2.mutable._
import org.specs2.time.NoTimeConversions
import testHelpers.Actors
import scala.concurrent.duration._
/**
 * Created by satsuma on 19.4.2016.
 */
class SubscriptionSchedulerTest extends Specification with NoTimeConversions {
  val scheduler = new SubscriptionScheduler

  "Subcscription scheduler" should {

    "Answer to correct actor" in new Actors{
      val probe1 = TestProbe()
      val probe2 = TestProbe()
      scheduler.scheduleOnce(2 seconds, probe1.ref, "meg")

      probe2.expectNoMsg(2500 milliseconds)
      probe1.receiveN(1, 2500 milliseconds)
    }
    "Be accurate to few a milliseconds" in new Actors {
      val probe = TestProbe()
      scheduler.scheduleOnce(3 seconds,probe.ref, "hello!")

      probe.receiveN(1,3010 milliseconds)
      probe.expectNoMsg(2990 milliseconds)
    }
  }
}
