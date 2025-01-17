val scalaV = "2.12.8"

import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

lazy val server = (project in file("server")).settings(
  scalaVersion := scalaV,
  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  // triggers scalaJSPipeline when using compile or continuous compilation
  compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-http" % "10.0.7",
    "com.vmunier" %% "scalajs-scripts" % "1.1.1"
  ),
  WebKeys.packagePrefix in Assets := "public/",
  managedClasspath in Runtime += (packageBin in Assets).value
).enablePlugins(SbtWeb, SbtTwirl, JavaAppPackaging).
  dependsOn(sharedJvm)

lazy val client = (project in file("client")).settings(
  resolvers += Resolver.jcenterRepo,
  scalaVersion := scalaV,
  scalaJSUseMainModuleInitializer := true,
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.1",
    "com.definitelyscala" %%% "scala-js-phaser" % "1.0.2",
    "com.definitelyscala" %%% "scala-js-phaserpixi" % "1.0.2",
    "com.definitelyscala" %%% "scala-js-phaserp2" % "1.0.2"
  ),
  jsDependencies += ProvidedJS / "phaser.js" minified "phaser.min.js" commonJSName "Phaser"
).enablePlugins(ScalaJSPlugin, ScalaJSWeb).
  dependsOn(sharedJs)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(scalaVersion := scalaV).
  jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// loads the server project at sbt startup
onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value
