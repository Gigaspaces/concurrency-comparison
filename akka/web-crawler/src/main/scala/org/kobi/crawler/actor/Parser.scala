package org.kobi.crawler.actor

import akka.actor.{Actor, Props}
import akka.event.Logging
import akka.routing.{RoundRobinPool, SmallestMailboxPool, SmallestMailboxRoutingLogic}
import org.jsoup.Jsoup
import org.kobi.crawler.actor.Master.UpdateState
import org.kobi.crawler.actor.Parser.Parse

import scala.collection.JavaConverters._
import scala.language.postfixOps


class Parser extends Actor {
  val baseUrl = "http://localhost:8080"
  val log = Logging(context.system, this)

  def receive: Receive = {
    case Parse(url) =>
      val links = getLinks(url)
      sender() ! UpdateState(url, links)
  }

  def getLinks(url: Url): List[Url] = {
    val response = Jsoup.connect(url.url).ignoreContentType(true).execute()
    val doc = response.parse()
    doc.getElementsByTag("a")
      .asScala
      .map(e => e.attr("href"))
      .map(link => Url(baseUrl + link))
      .toList
  }

}

object Parser {
  def props: Props = Props[Parser].withDispatcher("fixedDispatcher20").withRouter(new SmallestMailboxPool(20))
  case class Parse(url: Url)
}
