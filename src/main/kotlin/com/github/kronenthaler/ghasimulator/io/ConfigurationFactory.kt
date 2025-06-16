package com.github.kronenthaler.ghasimulator.io

import com.github.kronenthaler.ghasimulator.Configuration

fun interface ConfigurationFactory {
    fun createConfiguration() : Configuration
}
