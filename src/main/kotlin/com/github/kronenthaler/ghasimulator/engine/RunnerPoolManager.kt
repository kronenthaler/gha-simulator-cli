package com.github.kronenthaler.ghasimulator.engine

import com.github.kronenthaler.ghasimulator.Configuration


class RunnerPoolManager(val config: Configuration, val jobQueue: JobQueue) {
    private val runners: MutableMap<String, MutableList<Runner>> = mutableMapOf()

    fun startRunnerPool() {
        config.runnerLabels.forEach { label ->
            runners[label] = mutableListOf()
            repeat(config.getRunnerCount(label)) {
                val runner = Runner(jobQueue, label, config.timescale)
                runner.start()
                runners[label]?.add(runner)
            }
        }
    }

    fun stopRunnerPool() {
        runners.values.flatten().forEach { runner ->
            runner.requestToStop()
        }
    }

    fun getActiveRunners(label: String): List<Runner> {
        return runners[label]?.filter { it.isAlive } ?: emptyList()
    }
}
