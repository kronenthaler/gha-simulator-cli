# Samples folder

In this folder, we provide some configuration files and structures that can be used to run a simulation and see how things behave, as well as some advise how to read the results and what they can mean.

The examples here use a hypothetical setup for an iOS app. 

We are running on-premise, and we have a number of fixed runners, let say 14 macOS runners, and 14 linux runners that can be used for miscellaneous tasks

The app is a white-label source code that can be built into 2 different skinned apps. Let's call them tenants A and B.

The app codebase also contains multiple standalone modules, each with their own test suite. 

For each app, we want to run different kinds of tests: unit tests, snapshot tests, UI tests.

We also have a reporting and code quality step (like sonar or codacy) at the end, this can run on a linux runner.

With all these baselines, we can sketch different ways to build and test the apps.

## Fully parallel

![your-UML-diagram-name](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/kronenthaler/gha-simulator-kotlin/master/samples/.md/fully-parallel.puml)

We can organize the workflow to have each thing build and tested on its own job. 
