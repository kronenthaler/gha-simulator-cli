package com.github.kronenthaler.ghasimulator

data class PipelineStats(val startTime: Long, val endTime: Long, val totalQueueTime: Long, val jobCount: Long) {
    fun toString(timescale: Int): String {
        return "${(endTime - startTime) / timescale}\t$startTime\t$endTime\t$totalQueueTime\t$jobCount"
    }
}

data class QueueStats(val totalQueuetime: Long, val jobCount: Int)
