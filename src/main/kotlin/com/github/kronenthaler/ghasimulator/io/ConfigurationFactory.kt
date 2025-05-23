package com.github.kronenthaler.ghasimulator.io

import com.github.kronenthaler.ghasimulator.Configuration
import java.io.File

interface ConfigurationFactory {
    fun loadFromFile(file: File): Configuration
}
