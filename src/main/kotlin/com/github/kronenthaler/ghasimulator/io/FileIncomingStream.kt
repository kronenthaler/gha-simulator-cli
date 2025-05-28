package com.github.kronenthaler.ghasimulator.io

import java.io.File

class FileIncomingStream(val file: File) : IncomingStream {
    override fun incomingStream(): Iterable<Double> {
        return file.readLines()
            .asSequence()
            .map { it.split(",") }
            .flatMap { it.asSequence() }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map(String::toDouble)
            .asIterable()
    }
}
