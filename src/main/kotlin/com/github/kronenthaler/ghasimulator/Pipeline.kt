package com.github.kronenthaler.ghasimulator

import org.yaml.snakeyaml.Yaml
import java.io.File
import java.util.logging.Logger

class Pipeline(val name: String, val jobQueue: JobQueue, val stats: MutableList<PipelineStats>, val roots: List<Job>) {
    private val logger: Logger = Logger.getLogger(Pipeline::class.java.name)

    private val startTime: Long = System.currentTimeMillis()
    private var endTime: Long = 0

    init {
       flattenJobs(roots).forEach { job ->
            job.parent = this
       }
    }

    private fun flattenJobs(jobs: List<Job>): List<Job> {
        val flatList = mutableSetOf<Job>()

        val queue = mutableListOf<Job>()
        queue.addAll(roots)
        while (queue.isNotEmpty()) {
            val current = queue.removeAt(0)
            flatList.add(current)
            queue.addAll(current.needs)
        }
        return flatList.toList()
    }

    fun getQueueStats(): QueueStats {
        var totalQueueTime = 0L

        val allJobs = flattenJobs(roots)
        allJobs.forEach { job ->
            totalQueueTime += job.getQueueTime()
        }

        return QueueStats(totalQueueTime, allJobs.size)
    }

    fun isCompleted(): Boolean {
        return roots.all { it.isCompleted }
    }
}
