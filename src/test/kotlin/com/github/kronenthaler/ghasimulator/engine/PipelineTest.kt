package com.github.kronenthaler.ghasimulator.engine

import com.github.kronenthaler.ghasimulator.stats.PipelineStats
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
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
        val pipeline = Pipeline("test", jobQueue, stats, listOf(jobD))

        val queeStats = pipeline.getQueueStats()

        assertEquals(5, queeStats.jobCount)
        assertEquals(10+10+10+10+15, queeStats.totalQueuetime)
    }

    @Test
    fun `test isCompleted when all root jobs are completed`() {
        val jobA = Job("a", 10, "ubuntu-latest", emptyList())
        val jobB = Job("b", 10, "ubuntu-latest", listOf(jobA))
        val jobC = Job("c", 10, "ubuntu-latest", listOf(jobA))
        val jobE = Job("e", 10, "ubuntu-latest", emptyList())
        val jobD = Job("d", 10, "ubuntu-latest", listOf(jobB, jobC, jobE))

        val jobQueue = JobQueue(listOf("ubuntu-latest"))
        val stats = mutableListOf<PipelineStats>()
        val pipeline = Pipeline("test", jobQueue, stats, listOf(jobD))

        assertFalse(pipeline.isCompleted())
        jobA.markAsCompleted()

        assertFalse(pipeline.isCompleted())
        jobB.markAsCompleted()

        assertFalse(pipeline.isCompleted())
        jobC.markAsCompleted()

        assertFalse(pipeline.isCompleted())
        jobE.markAsCompleted()

        assertFalse(pipeline.isCompleted())
        jobD.markAsCompleted()

        assertTrue(pipeline.isCompleted())
    }

    @Test
    fun `test that all jobs are assigned a parent`() {
        val jobA = Job("a", 10, "ubuntu-latest", emptyList())
        val jobB = Job("b", 10, "ubuntu-latest", listOf(jobA))
        val jobC = Job("c", 10, "ubuntu-latest", listOf(jobA))
        val jobE = Job("e", 10, "ubuntu-latest", emptyList())
        val jobD = Job("d", 10, "ubuntu-latest", listOf(jobB, jobC, jobE))

        assertNull(jobA.parent)
        assertNull(jobB.parent)
        assertNull(jobC.parent)
        assertNull(jobD.parent)
        assertNull(jobE.parent)

        val jobQueue = JobQueue(listOf("ubuntu-latest"))
        val stats = mutableListOf<PipelineStats>()
        val pipeline = Pipeline("test", jobQueue, stats, listOf(jobD))

        assertEquals(pipeline, jobA.parent)
        assertEquals(pipeline, jobB.parent)
        assertEquals(pipeline, jobC.parent)
        assertEquals(pipeline, jobD.parent)
        assertEquals(pipeline, jobE.parent)
    }

    @Test
    fun `test job scheduling`() {
        val jobA = Job("a", 10, "ubuntu-latest", emptyList())
        val jobB = Job("b", 10, "ubuntu-latest", listOf(jobA))
        val jobC = Job("c", 10, "ubuntu-latest", listOf(jobA))
        val jobE = Job("e", 10, "ubuntu-latest", emptyList())
        val jobD = Job("d", 10, "ubuntu-latest", listOf(jobB, jobC, jobE))

        val jobQueue = JobQueue(listOf("ubuntu-latest"))
        val stats = mutableListOf<PipelineStats>()
        val pipeline = Pipeline("test", jobQueue, stats, listOf(jobD))

        assertEquals(0, stats.size)

        // 2 heads should be scheduled (A + E)
        assertEquals(2, jobQueue.getSize("ubuntu-latest"))

        jobA.markAsCompleted()
        jobE.markAsCompleted()

        // +2 jobs should be scheduled (B + C)
        assertEquals(4, jobQueue.getSize("ubuntu-latest"))

        jobC.markAsCompleted()
        jobB.markAsCompleted()

        // +1 jobs should be scheduled (D)
        assertEquals(5, jobQueue.getSize("ubuntu-latest"))
        jobD.markAsCompleted()

        assertTrue(pipeline.isCompleted())
        assertEquals(1, stats.size)
        assertEquals(5, stats[0].jobCount)
    }
}
