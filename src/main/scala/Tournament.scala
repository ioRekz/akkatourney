package ioio.akkatourney

import akka.actor._
import akka.pattern.ask
import scala.concurrent.duration._
import akka.util.Timeout

import scala.math._

case object Start
case class Started(players: List[String])
case class Versus(firstPlayer: String, secondPlayer: String)
case class Result(winner: String, looser: String)
case class RoundOver(winners: List[String])
case class Winner(winner: String)

class Tournament(players: List[String], gameCreator: (String, String) => Actor, listener: ActorRef) extends Actor {
  require(players.distinct == players)

  val bracket = makeByes(players, countByes(players.size), true)

  def receive = {
    case Start =>
      sender ! Started(this.bracket)
      startRound(this.bracket)

    case RoundOver(winners) =>
      winners.size match {
        case 1 =>
          listener ! Winner(winners.head)
          context.stop(self)
        case _ => startRound(players.intersect(winners))
      }
  }

  def startRound(playerz: List[String]) = {
    val roundNbr : Int = (log(bracket.size/playerz.size) / log(2)).toInt
    val round = context.actorOf(Props(new Round(playerz, gameCreator, listener)), name = self.path.name+"-"+roundNbr.toString)
    round ! Start
  }


  def makeByes(players: List[String], nbByeLeft: Int, top: Boolean) : List[String] = {
    nbByeLeft match {
      case 0 => players
      case 1 =>
        if(top) players.head :: "BYE" :: players.tail
        else players.dropRight(1) ::: "BYE" :: players.last :: Nil
      case _ =>
        val splitted = players.splitAt(players.size / 2)
        makeByes(splitted._1, nbByeLeft/2 + nbByeLeft%2, true) ::: makeByes(splitted._2, nbByeLeft/2, false)
    }
  }

  import scala.math.pow
  def countByes(nbP: Int) = {
    near(nbP) - nbP
  }

  def near(nbP: Int, curr:Int = 1) : Int = {
    if(pow(2,curr) < nbP) near(nbP, curr+1) else pow(2,curr).toInt
  }

}

class Round(players: List[String], gameCreator: (String, String) => Actor, listener: ActorRef) extends Actor {

  require(players.filterNot(_ == "BYE").distinct == players.filterNot(_ == "BYE"))
  var winners : Set[String] = Set.empty

  def receive = {
    case Start =>
      startGames(players)

    case Result(winner, looser) =>
      winners = winners + winner
      if(winners.size == players.size/2) {
        context.parent ! RoundOver(winners.toList)
      }
  }

  def startGames(players: List[String]) {
    println(players)
    players.sliding(2,2).foreach { pair =>
      (pair(0), pair(1)) match {
        case ("BYE", p) =>
          self ! Result(p, "BYE")
        case (p, "BYE") =>
          self ! Result(p, "BYE")
        case (p1, p2) =>
          val game = context.actorOf(Props(gameCreator(pair(0),pair(1))), name=pair(0)+"-"+pair(1))
          game ! Start
      }

    }
  }
}


