package com.ebiznext.sbt.plugins

import sbt._
import sbt.Keys._
import java.io.File

trait Keys {
  def Config : Configuration
  lazy val groovyVersion = settingKey[String]("groovy version")
  lazy val groovySource = settingKey[File]("Default groovy source directory")
  lazy val groovyc = taskKey[Seq[File]]("Compile Groovy sources")
  lazy val defaultSettings = Seq(
    groovyVersion := "2.1.8",
    libraryDependencies ++= Seq[ModuleID](
      "org.codehaus.groovy" % "groovy-all" % groovyVersion.value % Config.name,
      "org.apache.ant" % "ant" % "1.8.4" % Config.name
    ),
    managedClasspath in groovyc <<= (classpathTypes in groovyc, update) map { (ct, report) =>
      Classpaths.managedJars(Config, ct, report)
    }
  )
}

trait CompileKeys extends Keys{
  override lazy val Config = (config("groovy") extend Compile).hide
}

trait TestKeys extends Keys {
	override lazy val Config = (config("test-groovy") extend Test).hide
}

trait IntegrationTestKeys extends TestKeys {
	override lazy val Config = (config("it-groovy") extend IntegrationTest).hide
}