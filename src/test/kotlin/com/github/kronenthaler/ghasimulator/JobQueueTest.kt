package com.github.kronenthaler.ghasimulator

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

@Timeout(5)
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

        assertThrows(IllegalArgumentException::class.java) {
            queue.getJob("c")
        }
    }

    @Test
    fun `test adding a job to an unexisting label throws exception`() {
        val queue = JobQueue(listOf("a", "b"))
        val job = Job("build", 10, "c", emptyList())

        assertThrows(IllegalArgumentException::class.java) {
            queue.addJob(job)
        }
    }
}