package com.github.kronenthaler.ghasimulator

import com.github.kronenthaler.ghasimulator.engine.*
import com.github.kronenthaler.ghasimulator.io.IncomingStream
import com.github.kronenthaler.ghasimulator.stats.PipelineStats
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {

    val config = Configuration(
        timescale = 1000,
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
            return listOf(1.0, 1.0, 1.0, 1.0, 1.0)
        }
    }

    val scheduler = Scheduler(config)
    scheduler.simulate(pipeline, incomingStream, System.out)
}
