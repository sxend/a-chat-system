
organization := "arimitsu.sf"

name := "a-chat-system"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.8"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  Resolver.bintrayRepo("sxend", "releases"),
  Resolver.bintrayRepo("sxend", "snapshots"),
  "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
)

libraryDependencies ++= {
  val akkaVersion = "2.4.7"
  val shapelessVersion = "2.3.1"
  val spec2Version = "3.8.4"
  val scalaTestVersion = "2.2.6"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-agent" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
    "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
    "com.typesafe.akka" %% "akka-remote" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-distributed-data-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-jackson-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.17",
    "org.iq80.leveldb" % "leveldb" % "0.7",
    "com.chuusai" %% "shapeless" % shapelessVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
    "org.specs2" %% "specs2-html" % spec2Version % "test",
    "org.specs2" %% "specs2-junit" % spec2Version % "test",
    "org.specs2" %% "specs2-core" % spec2Version % "test"
  )
}

// https://mvnrepository.com/artifact/org.springframework.boot/spring-boot
libraryDependencies += ("org.springframework.boot" % "spring-boot-starter-web" % "1.3.5.RELEASE")
                        .exclude("org.springframework.boot", "spring-boot-starter-tomcat")

// https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-jetty
libraryDependencies += "org.springframework.boot" % "spring-boot-starter-jetty" % "1.3.5.RELEASE"

// https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-websocket
libraryDependencies += "org.springframework.boot" % "spring-boot-starter-websocket" % "1.3.5.RELEASE"


publishMavenStyle := false

bintrayRepository := {
  if (version.value.matches("^[0-9]+\\.[0-9]+\\.[0-9]+$")) "releases" else "snapshots"
}

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

javacOptions ++= Seq("-source", "1.8")

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:reflectiveCalls",
  "-language:postfixOps"
)

assemblyMergeStrategy in assembly := {
  case PathList(ps @ _*) if ps.last endsWith "bnd.bnd" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "additional-spring-configuration-metadata.json" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "io.netty.versions.properties" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "spring-configuration-metadata.json" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "spring.factories" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "spring.provides" => MergeStrategy.first
  case x => (assemblyMergeStrategy in assembly).value(x)
}

testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "junitxml", "html", "console")

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/ScalaTest-reports/unit")

assemblyJarName in assembly := s"chat-system-assembly-${version.value}.jar"
