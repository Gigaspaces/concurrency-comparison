package org.kobi.crawler.actor

import akka.actor.{ActorSystem, Props}
import akka.routing.RoundRobinPool
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by kobikis on 20/11/16.
  *
  * @since 12.0.1
  */
object Main extends App{
  val system = ActorSystem.create("mypool", ConfigFactory.load()
    .getConfig("MyDispatcherExample"))

  val master = system.actorOf(Props(new Master(system)).withRouter(new RoundRobinPool(4)).withDispatcher("defaultDispatcher"))

  time {
    master ! Start(new Url("http://localhost:8080/"))
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
