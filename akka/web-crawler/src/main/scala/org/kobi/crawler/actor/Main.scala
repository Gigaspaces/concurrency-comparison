package org.kobi.crawler.actor

import akka.actor.{ActorSystem}
import com.typesafe.config.ConfigFactory
import org.kobi.crawler.actor.Master.Start

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

object Main extends App {
  val system = ActorSystem.create("mypool", ConfigFactory.load().getConfig("MyDispatcherExample"))

  val master = system.actorOf(Master.props)

  time {
    master ! Start(Url("http://localhost:8080/index.html"))
    Await.result(system.whenTerminated, 10 minutes)
  }

  def time[R](block: => R): R = {
    val t0 = System.currentTimeMillis()
    val result = block
    val t1 = System.currentTimeMillis()
    println("Elapsed time: " + (t1 - t0) + "ms")
    result
  }

}
