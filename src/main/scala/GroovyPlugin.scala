package com.ebiznext.sbt.plugins

import sbt._
import Keys._
import java.io.File

object GroovyPlugin extends Plugin {

  private object GroovyDefaults extends Keys {
    val settings = Seq(
      groovyVersion := "2.1.7",
      libraryDependencies ++= Seq[ModuleID](
        "org.codehaus.groovy" % "groovy-all" % groovyVersion.value % Config.name,
        "org.apache.ant" % "ant" % "1.8.4" % Config.name
      ),
      managedClasspath in generateStubs <<= (classpathTypes in generateStubs, update) map { (ct, report) =>
          Classpaths.managedJars(Config, ct, report)
      },
      managedClasspath in groovyc <<= (classpathTypes in groovyc, update) map { (ct, report) =>
          Classpaths.managedJars(Config, ct, report)
      }
    )
  }

  // generateStubs in Compile -> compile in Compile -> groovyc in Compile -> generateStubs in Test -> compile in Test -> groovyc in Test -> test in Test

  // to avoid namespace clashes, use a nested object
  object groovy extends Keys {
    val settings = Seq(ivyConfigurations += Config) ++ GroovyDefaults.settings ++ Seq(
      groovySource in Compile := (sourceDirectory in Compile).value / "groovy",
      unmanagedSourceDirectories in Compile += {(groovySource in Compile).value},
      generateStubs in Compile := {
        val sourceDirectory : File = (groovySource in Compile).value
        val nb = (sourceDirectory ** "*.groovy").get.size
        if(nb > 0){
	        val s: TaskStreams = streams.value
	        s.log.info("Start Generating Stubs from Groovy sources")
          val classpath : Seq[File] = ((managedClasspath in generateStubs).value).files
	        val stubDirectory : File = (sourceManaged in Compile).value / "groovy"
	        val destinationDirectory : File = stubDirectory
	        new GroovyC(classpath, sourceDirectory, stubDirectory, destinationDirectory).generateStubs
        }
        else{
          Nil
        }
      },
      sourceGenerators in Compile <+= generateStubs in Compile,
      managedClasspath in (Config, groovyc) <<= (classpathTypes in (Config, groovyc), update) map { (ct, report) =>
          Classpaths.managedJars(Config, ct, report)
      },
      groovyc in Compile := {
        val sourceDirectory : File = (groovySource in Compile).value
        val nb = (sourceDirectory ** "*.groovy").get.size
        if(nb > 0){
	        val s: TaskStreams = streams.value
	        s.log.info("Start Compiling Groovy sources")
          val classpath : Seq[File] = (((managedClasspath in groovyc).value).files) ++ Seq((classDirectory in Compile).value) ++ ((managedClasspath in Compile).value).files
	        val stubDirectory : File = (sourceManaged in Compile).value
	        val destinationDirectory : File = (classDirectory in Compile).value

	        def groovyClazz(file : File) : File = {
	          val p = file.getAbsolutePath()
	          new File(destinationDirectory.getAbsolutePath() + p.substring(sourceDirectory.getAbsolutePath().length(), p.length() - ".groovy".length()) + ".class")
	        }

	        (sourceDirectory ** "*.groovy").get map (groovyClazz) foreach {f => if(f.exists()){IO.delete(f)}}

	        new GroovyC(classpath, sourceDirectory, stubDirectory, destinationDirectory).compile
        }
      },
      compile in Compile <<= (compile in Compile) dependsOn (generateStubs in Compile),
      groovyc in Compile <<= (groovyc in Compile) dependsOn (compile in Compile)
    )
  }

  object testGroovy extends TestKeys {
    val settings = Seq(ivyConfigurations += Config) ++ inConfig(Config)(Defaults.testTasks ++ GroovyDefaults.settings ++ Seq(
      definedTests <<= definedTests in Test,
      definedTestNames <<= definedTestNames in Test,
      fullClasspath <<= fullClasspath in Test,

      groovySource in Test := (sourceDirectory in Test).value / "groovy",
      unmanagedSourceDirectories in Test += {(groovySource in Test).value},
      generateStubs in Test := {
        val sourceDirectory : File = (groovySource in Test).value
        val nb = (sourceDirectory ** "*.groovy").get.size
        if(nb > 0){
	        val s: TaskStreams = streams.value
	        s.log.info("Start Generating Stubs from Test Groovy sources")
          val classpath : Seq[File] = ((managedClasspath in generateStubs).value).files
	        val stubDirectory : File = (sourceManaged in Test).value
	        val destinationDirectory : File = stubDirectory
	        new GroovyC(classpath, sourceDirectory, stubDirectory, destinationDirectory).generateStubs
        }
        else{
          Nil
        }
      },
      sourceGenerators in Test <+= generateStubs in Test,
      groovyc in Test := {
        val sourceDirectory : File = (groovySource in Test).value
        val nb = (sourceDirectory ** "*.groovy").get.size
        if(nb > 0){
	        val s: TaskStreams = streams.value
	        s.log.info("Start Compiling Test Groovy sources")
          val classpath : Seq[File] = (((managedClasspath in groovyc).value).files) ++ Seq((classDirectory in Test).value) ++ Seq((classDirectory in Compile).value) ++ ((managedClasspath in Test).value).files
	        val stubDirectory : File = (sourceManaged in Test).value
	        val destinationDirectory : File = (classDirectory in Test).value

	        def groovyClazz(file : File) : File = {
	          val p = file.getAbsolutePath()
	          new File(destinationDirectory.getAbsolutePath() + p.substring(sourceDirectory.getAbsolutePath().length(), p.length() - ".groovy".length()) + ".class")
	        }

	        (sourceDirectory ** "*.groovy").get map (groovyClazz) foreach {f => if(f.exists()){IO.delete(f)}}

	        new GroovyC(classpath, sourceDirectory, stubDirectory, destinationDirectory).compile
        }
      },
      generateStubs in Test <<= (generateStubs in Test) dependsOn (groovyc in Compile),
      compile in Test <<= (compile in Test) dependsOn (generateStubs in Test),
      groovyc in Test <<= (groovyc in Test) dependsOn (compile in Test),
      test in Test <<= (test in Test) dependsOn (groovyc in Test)
    ))
  }
}
