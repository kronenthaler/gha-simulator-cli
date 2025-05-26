package com.github.kronenthaler.ghasimulator.engine

import com.github.kronenthaler.ghasimulator.Configuration
import com.github.kronenthaler.ghasimulator.PipelineFactory
import com.github.kronenthaler.ghasimulator.PipelineStats
import com.github.kronenthaler.ghasimulator.io.IncomingStream
import java.io.PrintStream
import java.lang.Thread
import java.util.Collections
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.concurrent.thread

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

    fun simulate(pipelineFactory: PipelineFactory, incomingStream: IncomingStream, outputReport: PrintStream) {
        // start the runner pool
        startRunnerPool()

        logger.log(Level.INFO, "Simulating")

        val stats = Collections.synchronizedList(mutableListOf<PipelineStats>())
        val threads = mutableListOf<Thread>()

        val events = incomingStream.incomingStream().iterator()
        while(events.hasNext()) {
            val interval = events.next()

            // TODO refactor as coroutines!
            val thread = thread() {
                val pipeline = pipelineFactory.createPipeline(jobQueue, stats)
                pipeline.waitForCompletion()
            }
            threads.add(thread)

            Thread.sleep((interval * config.timescale).toLong())
        }

        // wait for all threads to complete
        threads.forEach { it.join() }

        logger.log(Level.INFO,"Done simulating")

        // export report
        exportReport(stats, outputReport)

        // stop the runner pool
        stopRunnerPool()

        // return a stat summary object.
    }

    private fun exportReport(stats: List<PipelineStats>, outputReport: PrintStream) {
        outputReport.println("Run (m)\tStart (ms)\tEnd (ms)\tQueue (m)\tJobs");
        stats.forEach { stat ->
            outputReport.println(stat.toString(config.timescale))
        }
    }
}
