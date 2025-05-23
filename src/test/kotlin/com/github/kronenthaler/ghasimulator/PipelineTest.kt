package com.github.kronenthaler.ghasimulator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PipelineTest {
    @Test
    fun `test queue time calculation`() {
        // create a diamond graph a -> b -> d and a -> c -> d, e -> d
        val jobA = Job("a", 10, "ubuntu-latest", emptyList())
        val jobB = Job("b", 10, "ubuntu-latest", listOf(jobA))
        val jobC = Job("c", 10, "ubuntu-latest", listOf(jobA))
        val jobE = Job("e", 10, "ubuntu-latest", emptyList())
        val jobD = Job("d", 10, "ubuntu-latest", listOf(jobB, jobC, jobE))

        jobA.startQueueTime = 0
        jobA.endQueueTime = 10

        jobB.startQueueTime = 10
        jobB.endQueueTime = 20

        jobC.startQueueTime = 10
        jobC.endQueueTime = 20

        jobE.startQueueTime = 0
        jobE.endQueueTime = 15

        jobD.startQueueTime = 20
        jobD.endQueueTime = 30

        val jobQueue = JobQueue(listOf("ubuntu-latest"))
        val stats = mutableListOf<PipelineStats>()
        val pipeline = Pipeline("test", jobQueue, stats, listOf(jobA, jobB, jobC, jobD, jobE))

        val queeStats = pipeline.getQueueStats()

        assertEquals(5, queeStats.jobCount)
        assertEquals(10+10+10+10+15, queeStats.totalQueuetime)
    }

    fun `test isCompleted when all root jobs are completed`() {
        val jobA = Job("a", 10, "ubuntu-latest", emptyList())
        val jobB = Job("b", 10, "ubuntu-latest", listOf(jobA))
        val jobC = Job("c", 10, "ubuntu-latest", listOf(jobA))
        val jobE = Job("e", 10, "ubuntu-latest", emptyList())
        val jobD = Job("d", 10, "ubuntu-latest", listOf(jobB, jobC, jobE))

        val jobQueue = JobQueue(listOf("ubuntu-latest"))
        val stats = mutableListOf<PipelineStats>()
        val pipeline = Pipeline("test", jobQueue, stats, listOf(jobA, jobB, jobC, jobD, jobE))

        assertFalse(pipeline.isCompleted())
        jobA.markAsCompleted()

        assertFalse(pipeline.isCompleted())
        jobB.markAsCompleted()

        assertFalse(pipeline.isCompleted())
        jobC.markAsCompleted()

        assertFalse(pipeline.isCompleted())
        jobD.markAsCompleted()

        assertFalse(pipeline.isCompleted())
        jobE.markAsCompleted()

        assertTrue(pipeline.isCompleted())
    }
}
