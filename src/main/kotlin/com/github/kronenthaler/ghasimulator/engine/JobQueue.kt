package com.github.kronenthaler.ghasimulator.engine

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class JobQueue(val labels: List<String>) {
    private val queue: MutableMap<String, BlockingQueue<Job>> = mutableMapOf()

    init {
        labels.forEach { label ->
            queue[label] = LinkedBlockingQueue<Job>()
        }
    }

    fun getSize(label: String): Int = synchronized(this) {
        return queue[label]?.size ?: 0
    }

    fun addJob(job: Job) {
        queue[job.runsOn]?.let { queue ->
            job.startQueueTime = System.currentTimeMillis()
            queue.put(job)
        } ?: throw IllegalArgumentException("Label ${job.runsOn} not found")
    }

    fun getJob(label: String): Job {
        queue[label]?.let { queue ->
            var job = queue.take()
            job.endQueueTime = System.currentTimeMillis()
            return job
        } ?: throw IllegalArgumentException("Label $label not found")
    }
}
