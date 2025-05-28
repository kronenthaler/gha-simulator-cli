package com.github.kronenthaler.ghasimulator.io

import com.github.kronenthaler.ghasimulator.Configuration
import org.yaml.snakeyaml.Yaml
import java.io.File

class YamlConfigurationFactory(val file: File) : ConfigurationFactory {
    override fun createConfiguration(): Configuration {
        val yaml = Yaml()
        val inputStream = file.inputStream()
        val data = yaml.load<Map<String, Any>>(inputStream)

        val timescale = (data["timescale"] as? Int) ?: 20
        val runners = (data["runners"] as? List<Map<String, Any>>)?.map { entry ->
            Configuration.RunnerSpec(
                label = entry["label"] as? String ?: "default",
                count = (entry["count"] as? Int) ?: 1
            )
        } ?: emptyList()

        return Configuration(timescale, runners)
    }
}
