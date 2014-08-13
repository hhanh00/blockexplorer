import scalabuff._

name := "blockexplorer"

scalaVersion := "2.11.2"

libraryDependencies ++= Seq(
"com.typesafe.akka" % "akka-stream-experimental_2.11" % "0.4",
"com.typesafe.akka" %% "akka-persistence-experimental" % "2.3.3",
"net.sandrogrzicic" %% "scalabuff-runtime" % "1.3.8",
"commons-codec" % "commons-codec" % "1.9",
"org.apache.commons" % "commons-lang3" % "3.1"
)

lazy val root = Project("main", file("."), settings = Defaults.defaultSettings ++ scalabuffSettings
   ++ Seq(scalabuffArgs := Seq("--target=2.11.2"), scalabuffVersion := "1.3.9")).configs(ScalaBuff)

