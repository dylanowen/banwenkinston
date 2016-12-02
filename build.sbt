name := "wallboard-game"

version := "1.0"

scalaVersion := "2.12.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.14",
  "com.typesafe.akka" %% "akka-http" % "10.0.0",
  "org.json4s" %% "json4s-native" % "3.5.0"
)