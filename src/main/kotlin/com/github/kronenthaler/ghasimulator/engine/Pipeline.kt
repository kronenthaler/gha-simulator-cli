package com.github.kronenthaler.ghasimulator.engine

import com.github.kronenthaler.ghasimulator.stats.PipelineStats
import com.github.kronenthaler.ghasimulator.stats.QueueStats
import java.util.logging.Level
import java.util.logging.Logger

class Pipeline(val name: String, val jobQueue: JobQueue, val stats: MutableList<PipelineStats>, val roots: List<Job>) {
    private val logger: Logger = Logger.getLogger(Pipeline::class.java.name)

    private val startTime: Long = System.currentTimeMillis()
    private var endTime: Long = 0
    private val lock = Object()

    init {
        flattenJobs().forEach { job ->
            job.parent = this
        }
        check()
    }

    private lateinit var jobs: List<Job>

    // memoize flattend jobs to avoid recalculating them multiple times
    private fun flattenJobs(): List<Job> {
        if (::jobs.isInitialized) {
            return jobs
        }

        val flatList = mutableSetOf<Job>()

        val queue = mutableListOf<Job>()
        queue.addAll(roots)
        while (queue.isNotEmpty()) {
            val current = queue.removeAt(0)
            flatList.add(current)
            queue.addAll(current.needs)
        }
        jobs = flatList.toList()
        return jobs
    }

    fun getQueueStats(): QueueStats {
        var totalQueueTime = 0L

        val allJobs = flattenJobs()
        allJobs.forEach { job ->
            totalQueueTime += job.getQueueTime()
        }

        return QueueStats(totalQueueTime, allJobs.size)
    }

    fun isCompleted(): Boolean {
        return roots.all { it.isCompleted }
    }

    fun check() = synchronized(lock) {
        if (isCompleted()) {
            endTime = System.currentTimeMillis()
            val queueStats = getQueueStats()
            stats.add(PipelineStats(startTime, endTime, queueStats.totalQueuetime, queueStats.jobCount))
            logger.log(Level.FINE, "Pipeline $name completed in ${endTime - startTime} ms")
            lock.notifyAll()
            return
        }

        // schedule all jobs that are ready to be scheduled
        flattenJobs()
            .filter { !it.isScheduled && !it.isCompleted && it.needs.all { it.isCompleted } }
            .forEach { job ->
                jobQueue.addJob(job)
            }
        lock.notifyAll()
    }

    fun waitForCompletion() = synchronized(lock) {
        while (!isCompleted()) {
            lock.wait()
        }
    }
}
