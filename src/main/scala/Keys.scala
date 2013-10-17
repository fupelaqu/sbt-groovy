package com.ebiznext.sbt.plugins

import sbt._
import sbt.Keys._
import java.io.File

trait Keys {

    lazy val Config = config("groovy") extend(Compile) hide
    lazy val groovyVersion = settingKey[String]("groovy version")
    lazy val groovySource = settingKey[File]("Default groovy source directory")
    lazy val generateStubs = taskKey[Seq[File]]("Generate Java Stubs from Groovy sources")
    lazy val groovyc = taskKey[Unit]("Compile Groovy sources")

}

trait TestKeys extends Keys {
	override lazy val Config = config("test-groovy") extend(Test) hide
}

trait IntegrationTestKeys extends TestKeys {
	override lazy val Config = config("it-groovy") extend(IntegrationTest) hide
}