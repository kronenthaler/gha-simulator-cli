package com.github.kronenthaler.ghasimulator.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JobTest {

    @Test
    fun `test default property values`() {
        val job = Job("build", 10, "ubuntu-latest", emptyList())
        assertEquals("build", job.name)
        assertEquals(10, job.runningTime)
        assertEquals("ubuntu-latest", job.runsOn)
        assertTrue(job.needs.isEmpty())
        assertFalse(job.isCompleted)
        assertFalse(job.isScheduled)
        assertEquals(0, job.startQueueTime)
        assertEquals(0, job.endQueueTime)
    }

    @Test
    fun `test scheduling sets scheduled true and updates startQueueTime`() {
        val job = Job("test", 5, "ubuntu-latest", emptyList())
        job.startQueueTime = 12345L
        assertTrue(job.isScheduled)
        assertEquals(12345L, job.startQueueTime)
    }

    @Test
    fun `test completed method sets completed true`() {
        val job = Job("deploy", 3, "ubuntu-latest", emptyList())
        assertFalse(job.isCompleted)
        job.markAsCompleted()
        assertTrue(job.isCompleted)
    }
}
