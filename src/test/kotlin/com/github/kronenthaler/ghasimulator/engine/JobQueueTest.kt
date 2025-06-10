package com.github.kronenthaler.ghasimulator.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class JobQueueTest {
    @Test
    fun `test sizes of different labels`() {
        val queue = JobQueue(listOf("a", "b"))
        assertEquals(0, queue.getSize("a"))
        assertEquals(0, queue.getSize("b"))

        val job = Job("build", 10, "a", emptyList())
        assertEquals(0, job.startQueueTime)

        queue.addJob(job)

        assertEquals(1, queue.getSize("a"))
        assertNotEquals(0, job.startQueueTime)
    }

    @Test
    fun `test getting job from queue`() {
        val queue = JobQueue(listOf("a", "b"))
        val job = Job("build", 10, "a", emptyList())
        queue.addJob(job)

        val retrievedJob = queue.getJob("a")
        assertEquals(job.name, retrievedJob.name)
        assertEquals(job.runsOn, retrievedJob.runsOn)
        assertEquals(job.runningTime, retrievedJob.runningTime)
        assertNotEquals(0, retrievedJob.endQueueTime)
    }

    @Test
    fun `test getting a job from an unexisting label throws exception`() {
        val queue = JobQueue(listOf("a", "b"))
        val job = Job("build", 10, "a", emptyList())
        queue.addJob(job)

        assertFailsWith<IllegalArgumentException> {
            queue.getJob("c")
        }
    }

    @Test
    fun `test adding a job to an unexisting label throws exception`() {
        val queue = JobQueue(listOf("a", "b"))
        val job = Job("build", 10, "c", emptyList())

        assertFailsWith<IllegalArgumentException> {
            queue.addJob(job)
        }
    }
}
