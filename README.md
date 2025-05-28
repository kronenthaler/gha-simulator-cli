# gha-simulator-kotlin

A quick experimentation tool to optimize your GHA workflows under limited resources

## Usage

You can use this simulator in 2 different ways:
* right out of the box with the binary
* as a library and extend the functionality

### CLI

As a CLI, it will simulate one pipeline structure at a time, for a given configuration, with a given incoming stream of events.

For simplicity, the configuration and the pipeline structure use YAML format. You can see some examples in the `samples` folder.

The incoming stream file, is a comma-separated text file with the spacing (in minutes) between each incoming event. You can see an example in the `samples` folder.

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

## Limitations

* Job's `runs-on` are treated as a single label
* Pipeline yaml definition format is based on the GHA format. However, does not support strategy matrix definitions.
