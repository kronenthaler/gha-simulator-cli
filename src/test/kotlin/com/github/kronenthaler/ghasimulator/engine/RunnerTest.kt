package com.github.kronenthaler.ghasimulator.engine

import kotlin.test.*

class RunnerTest {
    private lateinit var runner: Runner
    private lateinit var queue: JobQueue

    @BeforeTest
    fun init() {
        queue = JobQueue(listOf("a", "b"))
        runner = Runner(queue, "a", 100)
        runner.start()
    }

    @AfterTest
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

        Thread.sleep(2000)

        assertFalse(runner.isAlive)
    }
}
