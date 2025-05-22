package com.github.kronenthaler.ghasimulator

data class Stats(val startTime: Long, val endTime: Long, val totalQueueTime: Long, val jobCount: Long) {
    fun toString(timescale: Int): String {
        return "${(endTime - startTime) / timescale}\t$startTime\t$endTime\t$totalQueueTime\t$jobCount"
    }
}
