package com.github.kronenthaler.ghasimulator.io

import com.github.kronenthaler.ghasimulator.engine.JobQueue
import com.github.kronenthaler.ghasimulator.stats.PipelineStats
import java.io.File
import java.io.FileWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class YamlPipelineFactoryTest {

    @Test
    fun `test load from Yaml pipeline name defaults to file name`() {
        val tempFile = File.createTempFile("test", ".yaml")
        tempFile.deleteOnExit()

        FileWriter(tempFile).use { fileWriter ->
            fileWriter.write("jobs: {}")
        }

        val mockJobQueue = JobQueue(listOf("label1", "label2"))
        val stats = mutableListOf<PipelineStats>()

        val result = YamlPipelineFactory(tempFile).createPipeline(mockJobQueue, stats)
        assertEquals(result.name, tempFile.nameWithoutExtension)
    }

    @Test
    fun `test load from Yaml with single root`() {
        val tempFile = File.createTempFile("test", ".yaml")
        tempFile.deleteOnExit()

        FileWriter(tempFile).use { fileWriter ->
            fileWriter.write(
                """
                name: Single-root pipeline
                jobs:
                  A:
                    time: 10 
                    runs-on: label1
                  B:
                    time: 5
                    runs-on: label2
                  C:
                    time: 20
                    runs-on: label2
                    needs: [B]
                  D:
                    time: 20
                    runs-on: label1
                    needs: [B]
                  E: 
                    time: 20
                    runs-on: label1
                    needs: [A, B, C, D]
            """.trimIndent()
            )
        }

        val mockJobQueue = JobQueue(listOf("label1", "label2"))
        val stats = mutableListOf<PipelineStats>()

        val result = YamlPipelineFactory(tempFile).createPipeline(mockJobQueue, stats)
        assertEquals(1, result.roots.size)

        val root = result.roots[0]
        assertEquals("E", root.name)
        assertEquals(4, root.needs.size)
        assertEquals("A", root.needs[0].name)
        assertEquals("B", root.needs[1].name)
        assertEquals("C", root.needs[2].name)
        assertEquals("D", root.needs[3].name)

        val A = root.needs[0]
        assertEquals(0, A.needs.size)

        val B = root.needs[1]
        assertEquals(0, B.needs.size)

        val C = root.needs[2]
        assertEquals(1, C.needs.size)
        assertEquals("B", C.needs[0].name)

        val D = root.needs[3]
        assertEquals(1, D.needs.size)
        assertEquals("B", D.needs[0].name)
    }

    @Test
    fun `test load from Yaml with multiple roots`() {
        val tempFile = File.createTempFile("test", ".yaml")
        tempFile.deleteOnExit()

        FileWriter(tempFile).use { fileWriter ->
            fileWriter.write(
                """
                name: Multi-root pipeline
                jobs:
                  A:
                    time: 10 
                    runs-on: label1
                  B:
                    time: 5
                    runs-on: label2
                  C:
                    time: 20
                    runs-on: label2
                    needs: [B]
                  D:
                    time: 20
                    runs-on: label1
                    needs: [B]
                  E: 
                    time: 20
                    runs-on: label1
                    needs: [B, C, D]
            """.trimIndent()
            )
        }

        val mockJobQueue = JobQueue(listOf("label1", "label2"))
        val stats = mutableListOf<PipelineStats>()

        val result = YamlPipelineFactory(tempFile).createPipeline(mockJobQueue, stats)
        assertEquals(2, result.roots.size)

        val A = result.roots[0]
        assertEquals("A", A.name)
        assertEquals(0, A.needs.size)

        val E = result.roots[1]
        assertEquals("E", E.name)
        assertEquals(3, E.needs.size)
        assertEquals("B", E.needs[0].name)
        assertEquals("C", E.needs[1].name)
        assertEquals("D", E.needs[2].name)

        val B = E.needs[0]
        assertEquals(0, B.needs.size)

        val C = E.needs[1]
        assertEquals(1, C.needs.size)
        assertEquals("B", C.needs[0].name)

        val D = E.needs[2]
        assertEquals(1, D.needs.size)
        assertEquals("B", D.needs[0].name)
    }

    @Test
    fun `test load from Yaml with cycle`() {
        val tempFile = File.createTempFile("test", ".yaml")
        tempFile.deleteOnExit()

        FileWriter(tempFile).use { fileWriter ->
            fileWriter.write(
                """
                name: Cyclical pipeline
                jobs:
                  A:
                    time: 10 
                    runs-on: label1
                    needs: [B]
                  B:
                    time: 5
                    runs-on: label2
                    needs: [A]
            """.trimIndent()
            )
        }

        val mockJobQueue = JobQueue(listOf("label1", "label2"))
        val stats = mutableListOf<PipelineStats>()

        val exception = assertFailsWith<IllegalStateException> {
            YamlPipelineFactory(tempFile).createPipeline(mockJobQueue, stats)
        }

        assertTrue(exception.message!!.contains("Circular dependency detected for job:"))
    }

    @Test
    fun `test load from Yaml validate time`() {
        val tempFile = File.createTempFile("test", ".yaml")
        tempFile.deleteOnExit()

        FileWriter(tempFile).use { fileWriter ->
            fileWriter.write(
                """
                name: Missing time
                jobs:
                  A:
                    runs-on: label1
                  B:
                    time: 5
                    runs-on: label2
                    needs: [A]
            """.trimIndent()
            )
        }

        val mockJobQueue = JobQueue(listOf("label1", "label2"))
        val stats = mutableListOf<PipelineStats>()

        val exception = assertFailsWith<IllegalArgumentException> {
            YamlPipelineFactory(tempFile).createPipeline(mockJobQueue, stats)
        }

        assertTrue(exception.message!!.contains("Job A misses required `time` definition"))
    }

    @Test
    fun `test load from Yaml validate runs-on`() {
        val tempFile = File.createTempFile("test", ".yaml")
        tempFile.deleteOnExit()

        FileWriter(tempFile).use { fileWriter ->
            fileWriter.write(
                """
                name: Missing runs-on
                jobs:
                  A:
                    time: 10
                  B:
                    time: 5
                    runs-on: label2
                    needs: [A]
            """.trimIndent()
            )
        }

        val mockJobQueue = JobQueue(listOf("label1", "label2"))
        val stats = mutableListOf<PipelineStats>()

        val exception = assertFailsWith<IllegalArgumentException> {
            YamlPipelineFactory(tempFile).createPipeline(mockJobQueue, stats)
        }

        assertTrue(exception.message!!.contains("Job A misses required `runs-on` definition"))
    }
}
