package org.kobi.crawler.actor

import java.util.concurrent.{ConcurrentHashMap, CountDownLatch}

import akka.actor.{Actor, ActorSystem, Props}
import akka.routing.RoundRobinPool

/**
  * Created by kobikis on 20/11/16.
  *
  * @since 12.0.1
  */
class Master(system: ActorSystem) extends Actor {


  var visitedLinks: ConcurrentHashMap[Url, Boolean] = new ConcurrentHashMap[Url, Boolean]()
  var counter: CountDownLatch = new CountDownLatch((math.pow(2,17) - 1)##)

  val parser = system.actorOf(Props(new Parser(self, counter)).withRouter(new RoundRobinPool(4)).withDispatcher("defaultDispatcher"))

  override def receive: Receive = {
    case Start(url : Url) =>
      visitedLinks.put(url, true)
      parser ! Parse(url)

    case urls: List[Url] =>
      for(url <- urls) {
        if (!visitedLinks.containsKey(url)) {
          parser ! Parse(url)
        }
        visitedLinks .put(url, true)
      }

    case Stop =>
//      println("counter = " + counter.getCount)
      system.terminate()
  }
}
