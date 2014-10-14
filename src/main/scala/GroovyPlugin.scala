package org.softnetwork.sbt.plugins

import sbt._
import Keys._
import java.io.File
import Path.relativeTo

object GroovyPlugin extends Plugin {

  // to avoid namespace clashes, use a nested object
  object groovy extends CompileKeys {

    lazy val groovycFilter : ScopeFilter = ScopeFilter(inDependencies(ThisProject, transitive = true, includeRoot = false), inConfigurations(Config), inTasks(groovyc))

    lazy val compileFilter : ScopeFilter = ScopeFilter(inDependencies(ThisProject, transitive = true, includeRoot = false), inConfigurations(Compile))

    val settings = Seq(ivyConfigurations += Config) ++ defaultSettings ++ Seq(
      groovySource in Compile := (sourceDirectory in Compile).value / "groovy",
      unmanagedSourceDirectories in Compile += {(groovySource in Compile).value},
      classDirectory in (Config, groovyc) := (crossTarget in Compile).value / "groovy-classes",
      managedClasspath in groovyc <<= (classpathTypes in groovyc, update) map { (ct, report) =>
          Classpaths.managedJars(Config, ct, report)
      },
      groovyc in Compile := {
        val sourceDirectory : File = (groovySource in Compile).value
        val nb = (sourceDirectory ** "*.groovy").get.size
        if(nb > 0){
	        val s: TaskStreams = streams.value
          s.log.info(s"Start Compiling Groovy sources : ${sourceDirectory.getAbsolutePath} ")

          val classDirectories: Seq[File] = classDirectory.all(compileFilter).value ++
              classDirectory.all(groovycFilter).value ++
              Seq((classDirectory in Compile).value)

          val classpath : Seq[File] = (managedClasspath in groovyc).value.files ++ classDirectories ++ (managedClasspath in Compile).value.files
          s.log.debug(classpath.mkString(";"))
	        val stubDirectory : File = (sourceManaged in Compile).value
	        val destinationDirectory : File = (classDirectory in (Config, groovyc)).value

	        new GroovyC(classpath, sourceDirectory, stubDirectory, destinationDirectory).compile()

          ((destinationDirectory ** "*.class").get pair relativeTo(destinationDirectory)).map{case(k,v) =>
            IO.copyFile(k, (resourceManaged in Compile).value / v, preserveLastModified = true)
            (resourceManaged in Compile).value / v
          }
        }
        else{
          Seq.empty
        }
      },
      resourceGenerators in Compile <+= groovyc in Compile,
      groovyc in Compile <<= (groovyc in Compile) dependsOn (compile in Compile)
    )
  }

  object testGroovy extends TestKeys {

    lazy val groovycTestFilter : ScopeFilter = ScopeFilter(inDependencies(ThisProject, transitive = true, includeRoot = false), inConfigurations(Config), inTasks(groovyc))

    lazy val compileTestFilter : ScopeFilter = ScopeFilter(inDependencies(ThisProject, transitive = true, includeRoot = false), inConfigurations(Test))

    val settings = Seq(ivyConfigurations += Config) ++ inConfig(Config)(Defaults.testTasks ++ Seq(
      definedTests <<= definedTests in Test,
      definedTestNames <<= definedTestNames in Test,
      fullClasspath <<= fullClasspath in Test)) ++ defaultSettings ++ Seq(

      groovySource in Test := (sourceDirectory in Test).value / "groovy",
      unmanagedSourceDirectories in Test += {(groovySource in Test).value},
      classDirectory in (Config, groovyc) := (crossTarget in Test).value / "groovy-test-classes",
      managedClasspath in groovyc <<= (classpathTypes in groovyc, update) map { (ct, report) =>
        Classpaths.managedJars(Config, ct, report)
      },
      groovyc in Test := {
        val sourceDirectory : File = (groovySource in Test).value
        val nb = (sourceDirectory ** "*.groovy").get.size
        if(nb > 0){
	        val s: TaskStreams = streams.value
	        s.log.info(s"Start Compiling Test Groovy sources : ${sourceDirectory.getAbsolutePath} ")

          val classDirectories: Seq[File] = classDirectory.all(groovy.compileFilter).value ++
            classDirectory.all(groovy.groovycFilter).value ++ classDirectory.all(compileTestFilter).value ++
            classDirectory.all(groovycTestFilter).value ++
            Seq((classDirectory in Compile).value, (classDirectory in (groovy.Config, groovyc)).value)

          val classpath : Seq[File] = (managedClasspath in groovyc).value.files ++ classDirectories ++ (managedClasspath in Test).value.files
          s.log.debug(classpath.mkString(";"))

	        val stubDirectory : File = (sourceManaged in Test).value

	        val destinationDirectory : File = (classDirectory in (Config, groovyc)).value

	        new GroovyC(classpath, sourceDirectory, stubDirectory, destinationDirectory).compile()

          ((destinationDirectory ** "*.class").get pair relativeTo(destinationDirectory)).map{case(k,v) =>
            IO.copyFile(k, (resourceManaged in Test).value / v, preserveLastModified = true)
            (resourceManaged in Test).value / v
          }
        }
        else{
          Seq.empty
        }
      },
      resourceGenerators in Test <+= groovyc in Test,
      groovyc in Test <<= (groovyc in Test) dependsOn (compile in Test)
    )
  }
}
