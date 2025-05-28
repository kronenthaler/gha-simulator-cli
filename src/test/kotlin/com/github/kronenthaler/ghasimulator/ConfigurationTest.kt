package com.github.kronenthaler.ghasimulator

import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigurationTest {

    @Test
    fun `test getRunnerCount for label`() {
        val configs = Configuration(20, listOf(Configuration.RunnerSpec("label1", 3)));
        assertEquals(3, configs.getRunnerCount("label1"));
        assertEquals(0, configs.getRunnerCount("label2"));
    }

    @Test
    fun `test getRunnerLabels`() {
        val configs = Configuration(20, listOf(
                Configuration.RunnerSpec("label1", 3),
                Configuration.RunnerSpec("label2", 5)
        ));
        assertEquals(listOf("label1", "label2"), configs.runnerLabels);
    }
}
