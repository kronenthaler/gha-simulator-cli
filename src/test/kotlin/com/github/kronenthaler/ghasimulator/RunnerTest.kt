package com.github.kronenthaler.ghasimulator


import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RunnerTest {
    private lateinit var runner: Runner
    private lateinit var queue: JobQueue

    @BeforeEach
    fun init() {
        queue = JobQueue(listOf("a", "b"))
        runner = Runner(queue, "a", 100)
        runner.start()
    }

    @AfterEach
    fun tearDown() {
        runner.requestToStop()
    }

    @Test
    fun `test runner completes job`() {
        val job = Job("build", 1, "a", emptyList())
        queue.addJob(job)

        assertTrue(runner.isAlive)
        Thread.sleep(500)

        assertTrue(job.isCompleted)
    }

    @Test
    fun `test runner gracefully stops`() {
        val job = Job("build", 1, "a", emptyList())
        queue.addJob(job)

        assertTrue(runner.isAlive)
        runner.requestToStop()
        assertTrue(runner.isInterrupted)

        Thread.sleep(500)

        assertFalse(runner.isAlive)
    }
}