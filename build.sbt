name := """play-starter"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.5"

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"


val testDeps = Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "com.github.simplyscala" %% "scalatest-embedmongo" % "0.2.2" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.6" % "test")
    
libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "org.scalaz" %% "scalaz-core" % "7.1.0",
  "org.scalaz" %% "scalaz-effect" % "7.1.0",
  "mysql" % "mysql-connector-java" % "5.1.30",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23",
  "com.mohiva" %% "play-silhouette" % "1.0"
) ++ testDeps
