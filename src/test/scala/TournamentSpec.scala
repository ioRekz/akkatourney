package ioio.akkatourney

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.testkit.TestKit
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import org.scalatest.BeforeAndAfterAll
import akka.testkit.ImplicitSender
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.util.Success

import akka.testkit.TestActorRef


import scala.collection.immutable.{ListSet}

class TournamentSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpec with BeforeAndAfterAll with MustMatchers {

  implicit val timeout = Timeout(3.seconds)


  override def afterAll {
    system.shutdown()
  }

  class SimpleGame(p1: String, p2: String) extends Actor {
    def receive = {
      case Start =>
        context.parent ! Result(p1, p2)
    }
  }

  val simpleGame = (p1: String, p2:String) => new SimpleGame(p1, p2)

  val players = "Jeremy" :: "Fred" :: Nil
  def aTourney(name: String,
               players: List[String] = this.players,
               listener: ActorRef = self) =
        TestActorRef(new Tournament(players, simpleGame, self), name = name)


  def this() = this(ActorSystem("MySpec2"))

  "A Tournament" must {
    "start and notify" in {


      val tournament = aTourney("t1")

      val future = tournament.underlyingActor.self ? Start

      // val Started(playa: List[String]) = Await.result(future, 3.seconds)
      val Success(Started(playa)) = future.value.get
      playa must be(players.toList)
      expectMsg(Winner("Jeremy"))
      system.stop(tournament.underlyingActor.self)
    }

    "notify winner" in {
      val tournament = TestActorRef(new Tournament("HRs" :: "Driz" :: players, simpleGame, self), name = "t2")

      ignoreMsg {
        case s: Started => true
      }
      tournament ! Start
      expectMsg(Winner("HRs"))
    }

    "should play with not enough players" in {
      val tournament = aTourney("t3", "lol" :: "lol2" :: "lol3" :: "lol4" :: players)
      ignoreMsg {
        case s: Started => true
      }
      tournament ! Start
      expectMsg(Winner("lol"))
    }
  }
}