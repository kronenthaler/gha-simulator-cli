package com.github.kronenthaler.ghasimulator.engine

import com.github.kronenthaler.ghasimulator.Configuration
import com.github.kronenthaler.ghasimulator.PipelineFactory
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


class Scheduler(val config: Configuration) {
    private val logger: Logger = Logger.getLogger(Scheduler::class.java.name)
    private val jobQueue: JobQueue = JobQueue(config.runnerLabels)
    private val runners: MutableMap<String, MutableList<Runner>> = mutableMapOf()

    private fun startRunnerPool() {
        config.runnerLabels.forEach { label ->
            runners[label] = mutableListOf()
            for (i in 0 until config.getRunnerCount(label)) {
                val runner = Runner(jobQueue, label, config.timescale)
                runner.start()
                runners[label]?.add(runner)
            }
        }
    }

    private fun stopRunnerPool() {
        runners.values.flatten().forEach { runner ->
            runner.requestToStop()
        }
    }

    fun simulate(pipelineFactory: PipelineFactory, incomingStream: IncomingStream, outputReport: PrintStream): StatsSummary {
        // start the runner pool
        startRunnerPool()

        logger.log(Level.INFO, "Simulating...")

        val stats = Collections.synchronizedList(mutableListOf<PipelineStats>())

        runBlocking {
            val threads = mutableListOf<Job>()

            val events = incomingStream.incomingStream().iterator()
            while (events.hasNext()) {
                val interval = events.next()

                threads.add(launch(Dispatchers.Default) {
                    val pipeline = pipelineFactory.createPipeline(jobQueue, stats)
                    pipeline.waitForCompletion()
                })

                // wait for the next interval before starting the next pipeline
                Thread.sleep((interval * config.timescale).toLong())
            }

            // wait for all coroutines to complete
            threads.joinAll()
        }

        // stop the runner pool
        stopRunnerPool()

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
