package com.github.kronenthaler.ghasimulator

import com.github.kronenthaler.ghasimulator.engine.JobQueue
import com.github.kronenthaler.ghasimulator.engine.Pipeline
import java.io.File

interface PipelineFactory {
    fun loadFromFile(file: File, jobQueue: JobQueue, stats: MutableList<PipelineStats>): Pipeline
}
