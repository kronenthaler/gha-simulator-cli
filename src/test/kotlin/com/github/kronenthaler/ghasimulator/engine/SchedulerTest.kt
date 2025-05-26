package com.github.kronenthaler.ghasimulator.engine

import com.github.kronenthaler.ghasimulator.Configuration
import com.github.kronenthaler.ghasimulator.PipelineFactory
import com.github.kronenthaler.ghasimulator.io.IncomingStream
import com.github.kronenthaler.ghasimulator.stats.PipelineStats
import org.junit.jupiter.api.Test
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.BufferedReader
import java.io.File
import java.io.PrintStream

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

        val scheduler = Scheduler(config)
        val summary = scheduler.simulate(pipeline, incomingStream, report)

        val lines = tempFile.inputStream().buffered().bufferedReader().lines().toList()
        assertEquals(6, lines.size)

        System.out.println(summary.formattedSummary())
    }
}