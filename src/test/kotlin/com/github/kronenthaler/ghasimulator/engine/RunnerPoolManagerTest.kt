package com.github.kronenthaler.ghasimulator.engine

import com.github.kronenthaler.ghasimulator.Configuration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RunnerPoolManagerTest {
    @Test
    fun `test start and stop runner pool`() {
        val config = Configuration(
            timescale = 20,
            runnerSpecs = listOf(
                Configuration.RunnerSpec(label = "label1", count = 2),
                Configuration.RunnerSpec(label = "label2", count = 0)
            )
        )

        val jobQueue = JobQueue(config.runnerLabels)
        jobQueue.addJob(Job("job1", 10, "label1", emptyList()))
        jobQueue.addJob(Job("job2", 10, "label1", emptyList()))
        jobQueue.addJob(Job("job3", 10, "label2", emptyList()))

        assertEquals(2, jobQueue.getSize("label1"))
        assertEquals(1, jobQueue.getSize("label2"))

        val poolManager = RunnerPoolManager(config, jobQueue)
        poolManager.startRunnerPool()

        Thread.sleep(3000) // Allow some time for runners to process jobs

        // all jobs should be consumed by the runners
        assertEquals(0, jobQueue.getSize("label1"))
        assertEquals(1, jobQueue.getSize("label2"))

        poolManager.stopRunnerPool()
    }
}
