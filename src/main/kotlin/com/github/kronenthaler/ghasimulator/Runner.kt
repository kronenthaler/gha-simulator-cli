package com.github.kronenthaler.ghasimulator

import java.util.logging.Level
import java.util.logging.Logger

class Runner(val jobQueue: JobQueue, val label: String, val timescale: Int) : Thread () {
    private val logger: Logger = Logger.getLogger(Runner::class.java.name)
    private var isRunning: Boolean = true

    fun requestToStop() {
        isRunning = false
        interrupt()
    }

    override fun run() {
        while (isRunning) {
            try {
                val job = jobQueue.getJob(label)
                logger.log(Level.FINER, "Running job: ${job.name} on label: $label")
                sleep(job.runningTime * timescale.toLong())
                job.markCompleted()
            } catch (_: InterruptedException) {
                logger.log(Level.FINE, "Runner interrupted for label: $label")
            }
        }
        logger.log(Level.FINE, "Runner stopping for label: $label")
    }
}