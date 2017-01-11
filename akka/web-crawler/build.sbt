name := "web-crawler-akka"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= {
  val akkaV       = "2.4.12"
  Seq(
    "com.typesafe.akka" %% "akka-stream"                          % "2.4.12",
    "com.typesafe.akka" %% "akka-actor"                           % "2.4.12",
    "com.typesafe.akka" %% "akka-remote"                          % "2.4.12",
    "org.jsoup"         % "jsoup"                                 % "1.8+",
    "io.spray" %% "spray-client"                                  % "1.3.2",
    "commons-validator" % "commons-validator"                     % "1.5+"
  )
}