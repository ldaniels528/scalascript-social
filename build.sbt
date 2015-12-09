lazy val root = (project in file(".")).
  enablePlugins(ScalaJSPlugin).
  settings(
    name := "scalascript-social",
    description := "Scala.js binding for Facebook and LinkedIn",
    organization := "com.github.ldaniels528",
    version := "0.2.15",
    scalaVersion := "2.11.7",
    scalacOptions ++= Seq("-feature", "-deprecation"),
    homepage := Some(url("http://github.com.ldaniels528/scalascript")),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full),
    libraryDependencies ++= Seq(
      "be.doeraene" %%% "scalajs-jquery" % "0.8.1",
      "com.github.ldaniels528" %%% "scalascript" % "0.2.15",
      "org.scala-js" %%% "scalajs-dom" % "0.8.1",
      "org.scala-lang" % "scala-reflect" % "2.11.7"
    )
  )
