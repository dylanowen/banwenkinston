name := "wallboard-game"

version := "0.1"

scalaVersion := "2.12.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.14",
  "com.typesafe.akka" %% "akka-http" % "10.0.0",
  "org.json4s" %% "json4s-native" % "3.5.0",
  "com.jsuereth" %% "scala-arm" % "2.0"
)

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)