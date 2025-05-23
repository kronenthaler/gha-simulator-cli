package com.github.kronenthaler.ghasimulator

data class Configuration(val timescale: Int = 20, val runnerSpecs: List<RunnerSpec> = emptyList()) {
    data class RunnerSpec(val label: String, val count: Int = 1)

    fun getRunnerLabels(): List<String> {
        return runnerSpecs.map { it.label }
    }

    fun getRunnerCount(label: String): Int {
        return runnerSpecs.find { it.label == label }?.count ?: 0
    }
}
