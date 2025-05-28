package com.github.kronenthaler.ghasimulator.io

import java.io.File
import java.io.FileWriter
import kotlin.test.Test
import kotlin.test.assertEquals

class FileIncomingStreamTest {
    @Test
    fun `load file and return stream`() {
        val tempFile = File.createTempFile("test_incoming_stream", ".txt")
        tempFile.deleteOnExit()

        FileWriter(tempFile).use { fileWriter ->
            fileWriter.write(
                """
                0, 1, 2, 3, 4,
                5, 6, 7
                8,
                9,
                10, 11
            """.trimIndent()
            )
        }

        val incomingStream = FileIncomingStream(tempFile)
        val values = incomingStream.incomingStream().toList()
        assertEquals(12, values.size)
        assertEquals(listOf<Double>(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0), values)
    }
}
