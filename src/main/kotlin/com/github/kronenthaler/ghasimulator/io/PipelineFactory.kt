package com.github.kronenthaler.ghasimulator.io

import com.github.kronenthaler.ghasimulator.engine.JobQueue
import com.github.kronenthaler.ghasimulator.engine.Pipeline
import com.github.kronenthaler.ghasimulator.stats.PipelineStats

fun interface PipelineFactory {
    fun createPipeline(jobQueue: JobQueue, stats: MutableList<PipelineStats>): Pipeline
}
