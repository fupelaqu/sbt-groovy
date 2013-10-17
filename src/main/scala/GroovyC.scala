package com.ebiznext.sbt.plugins

import sbt._
import sbt.Keys._
import java.io.File

import sbt.classpath.ClasspathUtilities

class GroovyC(val classpath : Seq[File], val sourceDirectory : File, val stubDirectory : File, val destinationDirectory : File) {

    //lazy val oldContextClassLoader = Thread.currentThread.getContextClassLoader

    lazy val classLoader = ClasspathUtilities.toLoader(classpath)

    lazy val projectClass = classLoader.loadClass("org.apache.tools.ant.Project")
    lazy val generateStubsClass = classLoader.loadClass("org.codehaus.groovy.ant.GenerateStubsTask")
    lazy val groovycClass = classLoader.loadClass("org.codehaus.groovy.ant.Groovyc")
    lazy val javacClass = classLoader.loadClass("org.apache.tools.ant.taskdefs.Javac")
    lazy val pathClass = classLoader.loadClass("org.apache.tools.ant.types.Path")

    lazy val pathConstructor = pathClass.getConstructor(projectClass)
    lazy val setLocationMethod = pathClass.getMethod("setLocation", classOf[java.io.File])

    lazy val setGroovycSrcdirMethod = groovycClass.getMethod("setSrcdir", pathClass)
    lazy val setGroovycStubdirMethod = groovycClass.getMethod("setStubdir", classOf[java.io.File])
    lazy val setGroovycDestdirMethod = groovycClass.getMethod("setDestdir", classOf[java.io.File])
    lazy val setGroovycProjectMethod = groovycClass.getMethod("setProject", projectClass)
    lazy val addGroovycConfiguredJavacMethod = groovycClass.getMethod("addConfiguredJavac", javacClass)
    lazy val setGroovycKeepStubsMethod = groovycClass.getMethod("setKeepStubs", java.lang.Boolean.TYPE)
    lazy val setGroovycVerboseMethod = groovycClass.getMethod("setVerbose", java.lang.Boolean.TYPE)
    lazy val executeGroovycMethod = groovycClass.getMethod("execute")

    def compile() : Unit =  {
        IO.createDirectory(sourceDirectory)
        IO.createDirectory(destinationDirectory)
        try{
          //Thread.currentThread.setContextClassLoader(classLoader)
          val project = projectClass.newInstance()
          val javac = javacClass.newInstance()
          val groovyc = groovycClass.newInstance()
          val path = pathConstructor.newInstance(project.asInstanceOf[AnyRef])
          setLocationMethod.invoke(path, sourceDirectory)
          setGroovycSrcdirMethod.invoke(groovyc, path.asInstanceOf[AnyRef])
          setGroovycStubdirMethod.invoke(groovyc, stubDirectory)
          setGroovycDestdirMethod.invoke(groovyc, destinationDirectory)
          setGroovycProjectMethod.invoke(groovyc, project.asInstanceOf[AnyRef])
          addGroovycConfiguredJavacMethod.invoke(groovyc, javac.asInstanceOf[AnyRef])
          setGroovycKeepStubsMethod.invoke(groovyc, true.asInstanceOf[AnyRef])
          setGroovycVerboseMethod.invoke(groovyc, true.asInstanceOf[AnyRef])
          executeGroovycMethod.invoke(groovyc)
        }
        finally{
          //Thread.currentThread.setContextClassLoader(oldContextClassLoader)          
        }
    }

    lazy val setGenerateStubsSrcdirMethod = generateStubsClass.getMethod("setSrcdir", pathClass)
    lazy val setGenerateStubsDestdirMethod = generateStubsClass.getMethod("setDestdir", classOf[java.io.File])
    lazy val setGenerateStubsProjectMethod = generateStubsClass.getMethod("setProject", projectClass)
    lazy val executeGenerateStubsMethod = generateStubsClass.getMethod("execute")

    def generateStubs() : Seq[File] =  {
        IO.createDirectory(sourceDirectory)
        IO.createDirectory(stubDirectory)
        try{
          //Thread.currentThread.setContextClassLoader(classLoader)
          val project = projectClass.newInstance()
          val generateStubs = generateStubsClass.newInstance()
          val path = pathConstructor.newInstance(project.asInstanceOf[AnyRef])
          setLocationMethod.invoke(path, sourceDirectory)
          setGenerateStubsSrcdirMethod.invoke(generateStubs, path.asInstanceOf[AnyRef])
          setGenerateStubsDestdirMethod.invoke(generateStubs, stubDirectory)
          setGenerateStubsProjectMethod.invoke(generateStubs, project.asInstanceOf[AnyRef])
          executeGenerateStubsMethod.invoke(generateStubs)
        }
        finally{
          //Thread.currentThread.setContextClassLoader(oldContextClassLoader)          
        }
        (stubDirectory ** "*.java").get
    }

}