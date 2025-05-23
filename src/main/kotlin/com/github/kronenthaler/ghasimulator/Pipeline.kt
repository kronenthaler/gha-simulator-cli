package com.github.kronenthaler.ghasimulator

import org.yaml.snakeyaml.Yaml
import java.io.File
import java.util.logging.Logger

class Pipeline(val name: String, val jobQueue: JobQueue, val stats: List<PipelineStats>, val roots: List<Job>) {
    private val logger: Logger = Logger.getLogger(Pipeline::class.java.name)

    private val startTime: Long = System.currentTimeMillis()
    private var endTime: Long = 0

    fun getQueueStats(): QueueStats {
        var totalQueueTime = 0L
        val queue = mutableListOf<Job>()
        queue.addAll(roots)
        val visited = mutableSetOf<Job>()
        while(!queue.isEmpty()){
            val current = queue.removeAt(0)
            if (visited.contains(current)) {
                continue
            }
            totalQueueTime += current.getQueueTime()
            visited.add(current)
            queue.addAll(current.needs)
        }

        return QueueStats(totalQueueTime, visited.size)
    }
}
