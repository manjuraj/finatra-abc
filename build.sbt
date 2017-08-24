lazy val root = Project(id = "finatra-abc", base = file("."))
  .settings(
    name := "finatra-abc",
    version := "1.0.0",
    organization := "com.example",
    scalaVersion := "2.11.8",
    libraryDependencies ++= List(
      "com.typesafe" % "config" % versions.config,
      "com.twitter" %% "finatra-http" % versions.finatra,
      "ch.qos.logback" % "logback-classic" % versions.logback,
      "org.scalaz" %% "scalaz-core" % versions.scalaz
    ),
    resolvers ++= {
      val typesafeUrl = "http://repo.typesafe.com/typesafe"
      List(
        Resolver.mavenLocal,
        "Typesafe Releases" at s"$typesafeUrl/releases/",
        "Typesafe Snapshots" at s"$typesafeUrl/snapshots/"
      )
    },
    scalacOptions ++= List(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Xfatal-warnings"
    )
  )

lazy val versions = new {
  val config = "1.3.1"
  val scalaz = "7.2.11"
  val finatra = "2.12.0"
  val logback = "1.1.7"
}

assemblyMergeStrategy in assembly := {
  case "BUILD" => MergeStrategy.discard
  case "META-INF/io.netty.versions.properties" => MergeStrategy.last
  case other => MergeStrategy.defaultMergeStrategy(other)
}

Revolver.settings
