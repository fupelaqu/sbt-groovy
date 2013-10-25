sbt-groovy
==========

an sbt plugin for groovy

## Requirements

* [SBT 0.13+](http://www.scala-sbt.org/)


## Quick start

Add plugin to *project/plugins.sbt*:

```scala

resolvers += "Sonatype Repository" at "https://oss.sonatype.org/content/groups/public"

addSbtPlugin("com.ebiznext.sbt.plugins" % "sbt-groovy" % "0.1.1")
```

For *.sbt* build definitions, inject the plugin settings in *build.sbt*:

```scala
seq(groovy.settings :_*)

seq(testGroovy.settings :_*)
```

For *.scala* build definitions, inject the plugin settings in *Build.scala*:

```scala
Project(..., settings = Project.defaultSettings ++ com.ebiznext.sbt.plugins.GroovyPlugin.groovy.settings ++ com.ebiznext.sbt.plugins.GroovyPlugin.testGroovy.settings)
```

## Configuration

Plugin keys are located in `com.ebiznext.sbt.plugins.Keys`

### Groovy sources

```scala
groovySource in Compile := (sourceDirectory in Compile).value / "groovy"

groovySource in Test := (sourceDirectory in Test).value / "groovy"
```

