package org.kobi.crawler.actor

import java.util.Calendar

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import org.kobi.crawler.actor.Master.{Start, Stop, UpdateState}
import org.kobi.crawler.actor.Parser.Parse

import scala.collection.mutable


class Master extends Actor {

  val log = Logging(context.system, this)
  var visiting: mutable.Map[Url, Boolean] = mutable.Map[Url, Boolean]()
  var visited: mutable.Map[Url, Boolean] = mutable.Map[Url, Boolean]()
  val parser: ActorRef = context.actorOf(Parser.props)

  def currentTime: Int = Calendar.getInstance().get(Calendar.MILLISECOND)

  override def receive: Receive = {
    case Start(url: Url) =>
      log.info("starting with url: " + url)
      visiting += (url -> true)
      parser ! Parse(url)

    case UpdateState(sourceUrl: Url, urls: List[Url]) =>
      urls.foreach(link => {
        if (!visiting.contains(link) && !visited.contains(link)) {
          parser ! Parse(link)
          visiting = visiting += (link -> false)
        }
      })
      visiting -= sourceUrl
      visited += sourceUrl -> true
      if (visiting.isEmpty) self ! Stop

    case Stop =>
      log.info("stopping, processed " + visited.size + " links")
      context.system.terminate()
  }
}

object Master {
  def props: Props = Props[Master].withDispatcher("fixedDispatcher1")

  case object Stop

  case class UpdateState(sourceUrl: Url, urls: List[Url])

  case class Start(url: Url)

}