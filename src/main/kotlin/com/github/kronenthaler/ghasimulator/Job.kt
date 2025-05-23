package com.github.kronenthaler.ghasimulator

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

    fun markAsCompleted() {
        isCompleted = true
        logger.log(Level.FINE, "Job $name completed")
    }

    fun getQueueTime(): Long {
        return endQueueTime - startQueueTime
    }
}
