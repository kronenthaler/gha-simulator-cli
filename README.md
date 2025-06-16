

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=kronenthaler_gha-simulator-cli&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=kronenthaler_gha-simulator-cli)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=kronenthaler_gha-simulator-cli&metric=coverage)](https://sonarcloud.io/summary/new_code?id=kronenthaler_gha-simulator-cli)

# gha-simulator-cli

A quick experimentation tool to optimize your GHA workflows under limited resources

## What it models

This project offers as model of the Github Actions' workflow execution flow. As any model, by definition is incomplete and does not reflect reality 100% accurately. However, this model allows you to run simulations on how the workflows will behave under stress and under constraints, such as limited runners.

But why? many companies run Github Actions with self-hosted runners, are often limited resources (e.g. macminis), it's a good idea to see if your current workflow setup is making the best of your runner's hardware.

## What can be used for

This project can be used to simulate different scenarios of your setup, including but not limited to:
* What will be the impact of adding more runners? It can help to justify the investment
* Test different workflow configurations without making the actual change
* Explore and diagnose wider effects

## How to use it

You can use this simulator in 2 different ways:
* right out of the box with the binary as a CLI
* as a library and extend the functionality

### CLI usage

As a CLI, it will simulate one pipeline structure at a time, for a given configuration, with a given incoming stream of events.

For simplicity, the configuration and the pipeline structure use YAML format. You can see some examples in the `samples` folder.

The incoming stream file is a comma-separated text file with the spacing (in minutes) between each incoming event. You can see an example in the [samples](./samples/README.md) folder.

The basic usage is as follows:
```
Usage: gha-simulator-cli [<options>] <configurationfile> <pipelinefile> <incomingfile>

Options:
  --output=<file>    Path to the output report file.
  -p, --print-stats  Print stats summary to console.
  -h, --help         Show this message and exit

Arguments:
  <configurationfile>  Path to the YAML configuration file.
  <pipelinefile>       Path to the YAML pipeline file.
  <incomingfile>       Path to the incoming stream file (comma-separated).
```

### Library usage

The project provides you with basic building blocks that will cover most of your use cases. However, for advanced uses, you might need to write some code to make the tool more powerful.

For example, you can provide Configurations from a file or generate them programmatically.

You can write a custom `PipelineFactory` that delivers different types of pipelines, this can be used to simulate interactions in codebases that has more than one type of pipelines consuming the same resources.

You can write a custom `IncomingStream` that generates random intervals of waits with additional rules or reads them from a disk/network system.

All of these buidling blocks can be passed to the generic `CoreSimulator` and let it rip.

```kotlin
fun main() {
    val configuration = YamlConfigurationFactory(configFile).createConfiguration()
    val pipelineFactory = MyCustomPipelineFactory()
    val incomingStream = MyCustomIncomingStream()
    val outputReport = System.out
    val printSummary = true
    
    CoreSimulator.run(configuration, pipelineFactory, incomingStream, outputReport, printSummary)
}
```

At the end, all the output will be left in the given outputReport stream and a small summary stats table can be printed.

## Post-processing

The simulator will output a report file with the running time (in minutes), start and end timestamp (in milliseconds), queue time (in minutes) and the number of jobs of each run executed.

With this data, you can run any statistical analysis your heart desires.

## Limitations

* Job's `runs-on` are treated as a single label
* Pipeline's yaml definition format is loosely based on the GHA format. However, does not support strategy matrix definitions.
* Pipeline's structure is static and known up-front. It does not support conditional jobs, or the like.

## Samples and analysis

If you are new to queue theory the sample folder contains a good starting point of how to configure your simulation, how to interpret some of the results, at a basic level at least.
