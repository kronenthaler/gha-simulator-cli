package com.github.kronenthaler.ghasimulator.engine

import com.github.kronenthaler.ghasimulator.engine.Pipeline
import java.util.logging.Level
import java.util.logging.Logger

class Job(val name: String, val runningTime: Int, val runsOn: String, val needs: List<Job>) {
    private val logger: Logger = Logger.getLogger(Job::class.java.name)

    var isCompleted: Boolean = false
        private set

    var isScheduled: Boolean = false
        private set

    var startQueueTime: Long = 0
        set(time) {
            field = time
            isScheduled = true
        }

    var endQueueTime: Long = 0

    var parent: Pipeline? = null

    fun markAsCompleted() {
        isCompleted = true
        // inform the pipeline that this job is completed
        parent?.check()
        logger.log(Level.FINE, "Job $name completed")
    }

    fun getQueueTime(): Long {
        return endQueueTime - startQueueTime
    }
}
