package org.kobi.crawler.streaming

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.actor.{ActorPublisher, ActorSubscriber}
import akka.stream.scaladsl.{Sink, Source}
import org.jsoup.Jsoup
import org.kobi.crawler.actor.Url
import spray.client.pipelining._
import spray.http.{HttpRequest, HttpResponse}

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object Main extends App {

  implicit val system = ActorSystem("main")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher

  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  val producerRef = system.actorOf(Props(classOf[ProducerActor]))
  val consumerRef = system.actorOf(Props(classOf[ConsumerActor], producerRef))
  val publisher = ActorPublisher[Url](producerRef)
  val subscriber = ActorSubscriber[Url](consumerRef)

  val basrUrl = "http://localhost:8080"
  val source = Source.fromPublisher(publisher)


  source.map(url => pipeline(Get(url.url))
    .map((url, _)))
    .mapAsyncUnordered(8)(identity)
    .map(parseUrls)
    .mapConcat(identity)
    .map(url => Url(url.url))
    .runWith(Sink.fromSubscriber(subscriber))


  Thread.sleep(1000L)

  time {
    producerRef ! Url(basrUrl)
    Await.result(system.whenTerminated, 100 minutes)
  }


  def parseUrls: ((Url, HttpResponse)) => List[Url] = {
    case (url, resp) =>
      val list = Jsoup.parse(resp.entity.asString)
        .select("a")
        .filter(!_.text().equals("back"))
        .toList
        .map(_.attr("href"))
        .map(l => basrUrl + l)
        .map(Url(_))
      list
  }

  def time[R](block: => R): R = {
    val t0 = System.currentTimeMillis()
    val result = block
    val t1 = System.currentTimeMillis()
    println("Elapsed time: " + (t1 - t0) + "ms")
    result
  }

}
