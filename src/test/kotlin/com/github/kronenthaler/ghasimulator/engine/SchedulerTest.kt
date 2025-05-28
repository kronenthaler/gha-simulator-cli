package com.github.kronenthaler.ghasimulator.engine

import com.github.kronenthaler.ghasimulator.Configuration
import com.github.kronenthaler.ghasimulator.io.IncomingStream
import com.github.kronenthaler.ghasimulator.io.PipelineFactory
import com.github.kronenthaler.ghasimulator.stats.PipelineStats
import java.io.File
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertEquals

class SchedulerTest {
    @Test
    fun `test simulate starts the runner pool`() {
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

        val tempFile = File.createTempFile("test", ".txt")
        val report = PrintStream(tempFile.outputStream())

        val jobQueue = JobQueue(config.runnerLabels)
        val poolManager = RunnerPoolManager(config, jobQueue)
        poolManager.startRunnerPool()

        val scheduler = Scheduler(config, jobQueue)
        val summary = scheduler.simulate(pipeline, incomingStream, report)

        poolManager.stopRunnerPool()

        val lines = tempFile.inputStream().buffered().bufferedReader().lines().toList()
        assertEquals(6, lines.size)

        println(summary.formattedSummary())
    }
}
