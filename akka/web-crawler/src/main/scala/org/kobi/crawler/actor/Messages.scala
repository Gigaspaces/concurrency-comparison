package org.kobi.crawler.actor

case class Start(url: Url)
case class ParseMulti(urls: List[Url])
case class Parse(url: Url)
case class Stop()
