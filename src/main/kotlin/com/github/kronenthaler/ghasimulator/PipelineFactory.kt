package com.github.kronenthaler.ghasimulator

import java.io.File

interface PipelineFactory {
    fun loadFromFile(file: File, jobQueue: JobQueue, stats: MutableList<PipelineStats>): Pipeline
}
