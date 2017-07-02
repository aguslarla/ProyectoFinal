name := "ProyectoFinal"

version := "1.0"

scalaVersion := "2.11.8"


libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.2",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.2" % Test,
  "com.typesafe.akka" %% "akka-persistence" % "2.5.3",
  "org.iq80.leveldb"  %  "leveldb" % "0.7",
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-cluster" % "2.5.2",
  "com.typesafe.akka" % "akka-cluster-metrics_2.11" % "2.5.2",
  "com.typesafe.akka" %% "akka-cluster-tools" % "2.5.2",
  "org.apache.kafka" % "kafka-clients" % "0.10.2.1",
  "org.apache.spark" %% "spark-core" % "2.0.2",
  "org.apache.spark" %% "spark-sql" % "2.0.2"
)