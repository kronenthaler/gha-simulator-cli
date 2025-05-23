# gha-simulator-kotlin
A quick experimentation tool to optimize your GHA workflows under limited resources


## Limitations

* Job's `runs-on` are treated as a single label
* Pipeline yaml definition format is based on the GHA format. However, does not support strategy matrix definitions.
