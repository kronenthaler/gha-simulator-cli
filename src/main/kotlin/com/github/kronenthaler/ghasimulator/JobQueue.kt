package com.github.kronenthaler.ghasimulator

import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class JobQueue(val labels: List<String>) {
    private val queue: MutableMap<String, BlockingQueue<Job>> = mutableMapOf()

    init {
        labels.forEach { label ->
            queue[label] = LinkedBlockingQueue<Job>()
        }
    }

    fun getSize(label: String) : Int {
        synchronized(this) {
            return queue[label]?.size ?: 0
        }
    }

    fun addJob(job: Job) {
        queue[job.runsOn]?.let { it ->
            it.put(job)
            job.startQueueTime = System.currentTimeMillis()
        } ?: throw IllegalArgumentException("Label ${job.runsOn} not found")
    }

    fun getJob(label: String) : Job {
        queue[label]?.let { it ->
            var job = it.take()
            job.endQueueTime = System.currentTimeMillis()
            return job
        } ?: throw IllegalArgumentException("Label $label not found")
    }
}