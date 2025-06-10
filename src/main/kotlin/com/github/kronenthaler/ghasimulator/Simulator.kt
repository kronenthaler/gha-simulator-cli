package com.github.kronenthaler.ghasimulator

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.outputStream
import com.github.kronenthaler.ghasimulator.engine.JobQueue
import com.github.kronenthaler.ghasimulator.engine.RunnerPoolManager
import com.github.kronenthaler.ghasimulator.engine.Scheduler
import com.github.kronenthaler.ghasimulator.io.*
import java.io.File
import java.io.PrintStream

/// The core simulator class that orchestrates the simulation with the scheduler and pool.
object CoreSimulator {
    fun run(
        configuration: Configuration,
        pipelineFactory: PipelineFactory,
        incomingStream: IncomingStream,
        outputReport: PrintStream = System.out,
        printSummary: Boolean = false
    ) {
        val jobQueue = JobQueue(configuration.runnerLabels)

        val poolManager = RunnerPoolManager(configuration, jobQueue)
        poolManager.startRunnerPool()

        val scheduler = Scheduler(configuration, jobQueue)
        val statsSummary = scheduler.simulate(pipelineFactory, incomingStream, outputReport)

        if (printSummary) {
            println(statsSummary.formattedSummary())
        }

        poolManager.stopRunnerPool()

        outputReport.flush()
        outputReport.close()
    }
}

/// Provides a CLI interface for a simulator that uses YAML as the base configuration format.
class DefaultSimulator : CliktCommand() {
    val configFile: File by argument("configurationFile", help = "Path to the YAML configuration file.")
        .file(mustExist = true, mustBeReadable = true)
    val pipelineFile: File by argument("pipelineFile", help = "Path to the YAML pipeline file.")
        .file(mustExist = true, mustBeReadable = true)
    val incomingStreamFile: File by argument("incomingFile", help = "Path to the incoming stream file.")
        .file(mustExist = true, mustBeReadable = true)
    val outputReport: PrintStream by option("--output", help = "Path to the output report file.")
        .outputStream()
        .convert { PrintStream(it) }
        .default(System.out)
    val printSummary: Boolean by option("-p", "--print-stats", help = "Print stats summary to console.")
        .flag(default = false)

    override fun run() {
        val configuration = YamlConfigurationFactory(configFile).createConfiguration()
        val pipelineFactory = YamlPipelineFactory(pipelineFile)
        val incomingStream = FileIncomingStream(incomingStreamFile)

        CoreSimulator.run(configuration, pipelineFactory, incomingStream, outputReport, printSummary)
    }
}

fun main(args: Array<String>) = DefaultSimulator().main(args)
