package org.acme.quarkus.scala.three.deployment

import dotty.tools.dotc.core.Contexts.{Context, ContextBase, FreshContext}
import dotty.tools.dotc.interfaces.CompilerCallback
import dotty.tools.dotc.Main
import dotty.tools.dotc.core.Contexts
import dotty.tools.dotc.Compiler
import dotty.tools.dotc.interfaces.SourceFile
import dotty.tools.dotc.reporting.Diagnostic
import dotty.tools.dotc.reporting.Reporter
import dotty.tools.io.{AbstractFile, Directory, PlainDirectory, PlainFile}

import io.quarkus.bootstrap.model.PathsCollection
import io.quarkus.deployment.dev.CompilationProvider

import java.io.File
import java.io.IOException
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import java.util
import java.util.{ArrayList, Collections, List, Set}
import java.util.stream.Collectors
import scala.collection.JavaConverters
import org.jboss.logging.Logger

import collection.JavaConverters.*

object Scala3CompilationProvider:
  private val log = Logger.getLogger(classOf[Scala3CompilationProvider])

class Scala3CompilationProvider extends CompilationProvider:
  override def handledExtensions: util.Set[String]  = Collections.singleton(".scala")
  override def handledSourcePaths: util.Set[String] = super.handledSourcePaths

  override def compile(files: util.Set[File], context: CompilationProvider.Context): Unit =
    Scala3CompilationProvider.log.info("[Scala3CompilationProvider] compile() called")
    val callback = CustomCompilerCallback()
    val classPath =
      context.getClasspath.stream
        .map(_.getAbsolutePath)
        .collect(Collectors.joining(File.pathSeparator))

    Scala3CompilationProvider.log.info("[Scala3CompilationProvider NEW] context classPath =")
    Scala3CompilationProvider.log.info(classPath)

    val contextBase = new ContextBase()

    given scalaCtx: FreshContext = contextBase.initialCtx.fresh

    scalaCtx.setSetting(scalaCtx.settings.classpath, classPath)
    scalaCtx.setSetting(
      scalaCtx.settings.outputDir,
      new PlainDirectory(Directory(context.getOutputDirectory.getAbsolutePath))
    )

    val dottyFiles: List[AbstractFile] = files.stream().map(it =>
      new PlainFile(dotty.tools.io.Path(it.toPath))
    ).collect(Collectors.toList)

    val compiler = new Compiler
    val run      = compiler.newRun
    run.compile(dottyFiles.iterator().asScala.toList)

  override def getSourcePath(classFilePath: Path, sourcePaths: PathsCollection, classesPath: String): Path =
    classFilePath

  @throws[IOException]
  override def close(): Unit =
    super.close()

//  private class CustomReporter extends Nothing { // with UniqueMessagePositions
//    // with HideNonSensicalMessages
//    def doReport(message: Nothing, ctx: Contexts.Context): Unit = {
//      //
//    }
//  }

  private class CustomCompilerCallback extends CompilerCallback:
    final private val pathsList = new util.ArrayList[String]

    def getPathsList: util.ArrayList[String] = pathsList

    override def onSourceCompiled(source: SourceFile): Unit =
      Scala3CompilationProvider.log.info("[Scala3CompilationProvider:CustomCompilerCallback] onSourceCompiled() called")
      Scala3CompilationProvider.log.info(source)
      if source.jfile.isPresent then pathsList.add(source.jfile.get.getPath)
