package com.github.kronenthaler.ghasimulator

data class Configuration(val timescale: Int = 20, val runnerSpecs: List<RunnerSpec> = emptyList()) {
    data class RunnerSpec(val label: String, val count: Int = 1)

    val runnerLabels: List<String> = runnerSpecs.map { it.label }

    fun getRunnerCount(label: String): Int {
        return runnerSpecs.find { it.label == label }?.count ?: 0
    }
}
