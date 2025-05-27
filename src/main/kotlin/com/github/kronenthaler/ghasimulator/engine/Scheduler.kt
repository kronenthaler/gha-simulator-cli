package com.github.kronenthaler.ghasimulator.engine

import com.github.kronenthaler.ghasimulator.Configuration
import com.github.kronenthaler.ghasimulator.io.PipelineFactory
import com.github.kronenthaler.ghasimulator.io.IncomingStream
import com.github.kronenthaler.ghasimulator.stats.PipelineStats
import com.github.kronenthaler.ghasimulator.stats.StatsSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.PrintStream
import java.lang.Thread
import java.util.Collections
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.concurrent.thread


class Scheduler(val config: Configuration, val jobQueue: JobQueue) {
    private val logger: Logger = Logger.getLogger(Scheduler::class.java.name)

    fun simulate(pipelineFactory: PipelineFactory, incomingStream: IncomingStream, outputReport: PrintStream): StatsSummary {
        logger.log(Level.INFO, "Simulating...")

        val stats = Collections.synchronizedList(mutableListOf<PipelineStats>())

        val threads = mutableListOf<Thread>()

        val events = incomingStream.incomingStream().iterator()
        while (events.hasNext()) {
            val interval = events.next()

            val thread = thread(start = true) {
                val pipeline = pipelineFactory.createPipeline(jobQueue, stats)
                pipeline.waitForCompletion()
            }
            threads.add(thread)

            // wait for the next interval before starting the next pipeline
            Thread.sleep((interval * config.timescale).toLong())
        }

        // wait for all coroutines to complete
        threads.forEach { it.join() }


        logger.log(Level.INFO,"Done simulating.")
        logger.log(Level.INFO,"Exporting report and calculating stats...")

        // export report
        exportReport(stats, outputReport)

        // return a stat summary object.
        return StatsSummary(stats, config.timescale)
    }

    private fun exportReport(stats: List<PipelineStats>, outputReport: PrintStream) {
        outputReport.println("Run (m)\tStart (ms)\tEnd (ms)\tQueue (m)\tJobs");
        stats.forEach { stat ->
            outputReport.println(stat.toString(config.timescale))
        }
    }
}
