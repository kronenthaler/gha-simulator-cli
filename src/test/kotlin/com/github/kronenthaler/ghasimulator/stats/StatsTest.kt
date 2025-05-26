package com.github.kronenthaler.ghasimulator.stats

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class StatsTest {
    @Test
    fun `test output formatting pipeline stats`() {
        val stats = PipelineStats(123, 1000, 2000, 3000)
        val expectedOutput = "${(1000-123)/20}\t123\t1000\t2000\t3000"
        Assertions.assertEquals(expectedOutput, stats.toString(20))
    }
}
