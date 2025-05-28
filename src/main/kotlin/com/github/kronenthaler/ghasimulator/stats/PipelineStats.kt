package com.github.kronenthaler.ghasimulator.stats

data class PipelineStats(val startTime: Long, val endTime: Long, val totalQueueTime: Long, val jobCount: Int) {
    fun toString(timescale: Int): String {
        return "${(endTime - startTime) / timescale}\t$startTime\t$endTime\t${totalQueueTime / timescale}\t$jobCount"
    }
}