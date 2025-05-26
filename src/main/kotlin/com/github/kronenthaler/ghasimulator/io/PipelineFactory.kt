package com.github.kronenthaler.ghasimulator

import com.github.kronenthaler.ghasimulator.engine.JobQueue
import com.github.kronenthaler.ghasimulator.engine.Pipeline
import com.github.kronenthaler.ghasimulator.stats.PipelineStats

interface PipelineFactory {
    fun createPipeline(jobQueue: JobQueue, stats: MutableList<PipelineStats>): Pipeline
}
