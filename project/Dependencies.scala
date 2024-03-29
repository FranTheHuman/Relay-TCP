import sbt._

object Dependencies {

  object V {
    val circe      = "0.14.2"
    val logback    = "1.2.11"
    val log4cats   = "2.4.0"
    val pureconfig = "0.17.1"
    val cats       = "3.3.14"
    val http4s     = "0.23.15"
    val akka = "2.6.20"
  }

  val circeCore         = "io.circe"              %% "circe-core"          % V.circe
  val circeGenerics     = "io.circe"              %% "circe-generic"       % V.circe
  val circeParser       = "io.circe"              %% "circe-parser"        % V.circe
  val logbackClassic    = "ch.qos.logback"         % "logback-classic"     % V.logback
  val log4catsCore      = "org.typelevel"         %% "log4cats-core"       % V.log4cats
  val log4catsSlf4j     = "org.typelevel"         %% "log4cats-slf4j"      % V.log4cats
  val pureconfig        = "com.github.pureconfig" %% "pureconfig"          % V.pureconfig
  val catsEffect        = "org.typelevel"         %% "cats-effect"         % V.cats
  val http4sDsl         = "org.http4s"            %% "http4s-dsl"          % V.http4s
  val http4sEmberServer = "org.http4s"            %% "http4s-ember-server" % V.http4s
  val http4sCirce       = "org.http4s"            %% "http4s-circe"        % V.http4s
  val circeGeneric      = "io.circe"              %% "circe-generic"       % V.circe
  val circeLiteral      = "io.circe"              %% "circe-literal"       % V.circe
  val fs2Io             = "co.fs2"                %% "fs2-io"              % "3.2.14"
  val fs2Scodec         = "co.fs2"                %% "fs2-scodec"          % "3.2.14"
  val akka              = "com.typesafe.akka"     %% "akka-actor"          % V.akka

  val dependencies: Seq[ModuleID] = Seq(
    circeCore,
    circeGenerics,
    circeParser,
    pureconfig,
    log4catsCore,
    log4catsSlf4j,
    catsEffect,
    http4sDsl,
    http4sEmberServer,
    http4sCirce,
    circeGeneric,
    circeLiteral,
    fs2Io,
    fs2Scodec,
    akka,
    logbackClassic % Runtime
  )

}
