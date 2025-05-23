package com.github.kronenthaler.ghasimulator

data class Configuration(val timescale: Int = 20, val runnerSpecs: List<RunnerSpec> = emptyList()) {
    data class RunnerSpec(val label: String, val count: Int = 1)
}
