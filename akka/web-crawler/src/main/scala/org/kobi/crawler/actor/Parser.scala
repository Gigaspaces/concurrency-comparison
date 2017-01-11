package org.kobi.crawler.actor

import java.util.concurrent.CountDownLatch

import akka.actor.{Actor, ActorRef}
import org.jsoup.Jsoup

import scala.collection.JavaConverters._
import scala.language.postfixOps


class Parser(manager: ActorRef, counter: CountDownLatch) extends Actor {
  val baseUrl = "http://localhost:8080"

  def receive: Receive = {
    case Parse(url) =>
//      println("Parsing " + url)
      if(counter.getCount == 0) {
        sender() ! Stop
      }
      else {
        val links = getLinks(url)
        links.foreach(url => counter.countDown())
        sender() ! links
      }
  }

  def getLinks(url: Url): List[Url] = {
    val response = Jsoup.connect(url.url).ignoreContentType(true).execute()
    val doc = response.parse()
    doc.getElementsByTag("a")
      .asScala
      .map(e => e.attr("href"))
      .map(link => new Url(baseUrl + link))
      .toList
  }

}
