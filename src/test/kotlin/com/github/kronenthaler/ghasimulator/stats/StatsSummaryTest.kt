package com.github.kronenthaler.ghasimulator.stats

import kotlin.test.Test
import kotlin.test.assertEquals

class StatsSummaryTest {
    @Test
    fun `test stats summary`() {
        val stats = listOf(
            PipelineStats(0, 100, 10, 5),
            PipelineStats(0, 200, 10, 5),
            PipelineStats(0, 1000, 100, 5)
        )

        val summary = StatsSummary(stats, 1)

        assertEquals(
            """
            |Pipeline summary stats:
            |                    	avg    	stdev  	pc50   	pc75   	min    	max    
            |Run Time            	433.33 	402.77 	200    	1000   	100    	1000   
            |Queue Time          	40.00  	42.43  	10     	100    	10     	100    
            |
        """.trimMargin(), summary.formattedSummary()
        )
    }
}
