name := "finatra-abc"

organization := "com.example"

version := "1.0.0"

scalaVersion := "2.11.8"

lazy val versions = new {
  val finatra = "2.9.0"
  val logback = "1.1.7"
}

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)

scalacOptions ++= List(
  "-deprecation",
  "-feature",
  "-unchecked"
)

assemblyMergeStrategy in assembly := {
  case "BUILD" => MergeStrategy.discard
  case "META-INF/io.netty.versions.properties" => MergeStrategy.last
  case other => MergeStrategy.defaultMergeStrategy(other)
}

Revolver.settings

libraryDependencies ++= List(
  "com.twitter" %% "finatra-http" % versions.finatra,
  "ch.qos.logback" % "logback-classic" % versions.logback
)
