package com.github.kronenthaler.ghasimulator.io

import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileWriter

class YamlConfigurationFactoryTest {
    @Test
    fun `test configuration with defaults`() {
        val tempFile = File.createTempFile("test", ".yaml")
        tempFile.deleteOnExit()

        FileWriter(tempFile).use { fileWriter ->
            fileWriter.write(
                """
                runners:
                - label: label1
                - label: label2
            """.trimIndent()
            )
        }

        val configs = YamlConfigurationFactory.loadFromFile(tempFile)
        assert(configs.timescale == 20)
        assert(configs.runnerSpecs.size == 2)
        assert(configs.runnerSpecs[0].label == "label1")
        assert(configs.runnerSpecs[0].count == 1)
        assert(configs.runnerSpecs[1].label == "label2")
        assert(configs.runnerSpecs[1].count == 1)
    }

    @Test
    fun `test configuration with explicit values`() {
        val tempFile = File.createTempFile("test", ".yaml")
        tempFile.deleteOnExit()

        FileWriter(tempFile).use { fileWriter ->
            fileWriter.write(
                """
                timescale: 25
                runners:
                - label: label1
                  count: 2
                - label: label2
                  count: 12
            """.trimIndent()
            )
        }

        val configs = YamlConfigurationFactory.loadFromFile(tempFile)
        assert(configs.timescale == 25)
        assert(configs.runnerSpecs.size == 2)
        assert(configs.runnerSpecs[0].label == "label1")
        assert(configs.runnerSpecs[0].count == 2)
        assert(configs.runnerSpecs[1].label == "label2")
        assert(configs.runnerSpecs[1].count == 12)
    }
}
