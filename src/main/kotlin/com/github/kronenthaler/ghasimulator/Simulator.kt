package com.github.kronenthaler.ghasimulator

import com.github.ajalt.clikt.core.CliktCommand
import com.github.kronenthaler.ghasimulator.engine.*
import com.github.kronenthaler.ghasimulator.io.*
import com.github.kronenthaler.ghasimulator.stats.PipelineStats
import java.io.File

/// The core simulator class that orchestrates the simulation with the scheduler and pool.
object CoreSimulator {
    fun run(configuration: Configuration, pipelineFactory: PipelineFactory, incomingStream: IncomingStream) {
        val jobQueue = JobQueue(configuration.runnerLabels)

        val poolManager = RunnerPoolManager(configuration, jobQueue)
        poolManager.startRunnerPool()

        val scheduler = Scheduler(configuration, jobQueue)
        scheduler.simulate(pipelineFactory, incomingStream, System.out)

        poolManager.stopRunnerPool()
    }
}

/// The provides a CLI interface for a simulator that uses YAML as the base configuration format.
class FileBasedSimulator : CliktCommand() {
    override fun run() {

    }
}

fun main() {
    val config = Configuration(
        timescale = 10,
        runnerSpecs = listOf(
            Configuration.RunnerSpec("ubuntu-latest", 3),
        )
    )

    val pipeline = object : PipelineFactory {
        override fun createPipeline(jobQueue: JobQueue, stats: MutableList<PipelineStats>): Pipeline {
            val jobA = Job("a", 10, "ubuntu-latest", emptyList())
            val jobB = Job("b", 10, "ubuntu-latest", listOf(jobA))
            val jobC = Job("c", 10, "ubuntu-latest", listOf(jobA))
            val jobD = Job("d", 10, "ubuntu-latest", listOf(jobB, jobC))
            val jobE = Job("e", 10, "ubuntu-latest", listOf(jobD))
            return Pipeline("test", jobQueue, stats, listOf(jobE))
        }
    }

    val incomingStream = object : IncomingStream {
        override fun incomingStream(): Iterable<Double> {
            return listOf(20.0, 20.0, 20.0, 20.0, 20.0)
        }
    }

    val jobQueue = JobQueue(config.runnerLabels)
    val poolManager = RunnerPoolManager(config, jobQueue)
    poolManager.startRunnerPool()

    val scheduler = Scheduler(config, jobQueue)
    scheduler.simulate(pipeline, incomingStream, System.out)

    poolManager.stopRunnerPool()

    val pipelineFactory = Class.forName("com.github.kronenthaler.ghasimulator.io.YamlPipelineFactory")
                            .getDeclaredConstructor(File::class.java)
                            .newInstance(File("src/main/resources/pipelines/ghas-pipeline.yaml"))
                          ?: throw IllegalArgumentException("Failed to load YamlPipelineFactory")

    println("Pipeline factory loaded: $pipelineFactory")
}
