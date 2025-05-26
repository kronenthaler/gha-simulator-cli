package com.github.kronenthaler.ghasimulator.stats

import kotlin.math.pow
import kotlin.math.sqrt

class StatsSummary(val stats: List<PipelineStats>, val timescale: Int) {
    fun formattedSummary(): String {
        val tokens = stats.map { stat -> stat.toString(timescale) }
            .map { line -> line.split("\t") }
            .toList()

        val runTimes = tokens.map { it[0] }.map { it.toInt() }.sorted()
        val queueTimes = tokens.map { it[3] }.map { it.toInt() }.sorted()

        val result = StringBuilder()
        result.append("Pipeline summary stats:\n")
        result.append(
            String.format(
                "%-20s\t%-7s\t%-7s\t%-7s\t%-7s\t%-7s\t%-7s\n",
                "",
                "avg",
                "stdev",
                "pc50",
                "pc75",
                "min",
                "max"
            ))
        result.append(formattedStats("Run Time", runTimes))
        result.append(formattedStats("Queue Time", queueTimes))

        return result.toString()
    }

    private fun formattedStats(name: String, times: List<Int>): String {
        val avg = times.average()
        val stdev = sqrt(times.map { (it - avg).pow(2) }.average())
        val pc50 = times[times.size / 2]
        val pc75 = times[(times.size * 3) / 4]
        val min = times.min()
        val max = times.max()

        return String.format(
            "%-20s\t%-7.2f\t%-7.2f\t%-7d\t%-7d\t%-7d\t%-7d\n",
            name, avg, stdev, pc50, pc75, min, max
        )
    }
}