# Samples folder

In this folder, we provide some configuration files and structures that can be used to run a simulation and see how things behave, as well as some advise how to read the results and what they can mean.

The examples here use a hypothetical setup for an iOS app. 

We are running on-premise, and we have a number of fixed runners, let say 14 macOS runners, and 14 linux runners that can be used for miscellaneous tasks

The app is a white-label source code that can be built into 2 different skinned apps. Let's call them tenants A and B.

The app codebase also contains multiple standalone modules, each with their own test suite. 

For each app, we want to run different kinds of tests: unit tests, snapshot tests, UI tests.

We also have a code quality step (like sonar or codacy), and final reporting step at the end. These can run on a linux runner.

With all these baselines, we can sketch different ways to build and test the apps, with different tradeoffs. For example, we can try to minimize the total running time, or minimize the queue time, or optimize for re-run scenarios (e.g. flaky tests).

Also consider that in Github Enterprise Server (on-premise GH), uploading big artifacts, has a big time tax. 500mb could take about 5-7min to upload.

## Fully parallel

![fully parallel](.md/fully-parallel.svg)

We can organize the workflow to have each thing build and tested on its own job. Assuming unlimited runners, this will be the most optimal way to run, giving the lowest theoretical minimum running time. However, runners are not limited.

### Highlights

* Minimum theoretical optimal.
* Each phase is a full build-and-test cycle.
* It avoids the big artifact time tax
* Faster worse case re-runs.

## Single build with parallel tests

![](./.md/single-build-parallel-test.svg)

We can organize the app project to build all the tests and both tenants in a single step (build-all-tests) and pass that artifact to the next stage, where tests are simply run against that artifact. The premise of this approach is that the build is done this way can leverage some compiler internals and speed up the build time. However, this will produce a big artifact that has to be uploaded. On the other hand, the tests will run for shorter time making more runners available faster for other jobs.

### Highlights

* Build job builds all tests in one go, including all modules tests
* Tests are run in parallel
* Creates a big artifact
* The first step creates a bottleneck

## Build tenants separately, test in parallel together

![](./.md/tenants-separately-parallel-test.svg)

Another possibility is to build the tenants separately (and the modules as well). Each job will compile in a shorter time (than [single build with parallel tests](#single-build-with-parallel-tests)), and produce smaller artifacts.

### Highlights

* Slightly faster theoretical optimal than single build.
* Faster build stages
* Smaller artifacts -> faster upload
* Segregated build stages allows re-build only of the failing tenant
 
## Independent tenant build and tests

![](./.md/independent-tenant-build-and-tests.svg)

An interation on the struture above, where the testing stage will wait for both tenants to be built before start the testing. This will start the tenant as soon as it's ready for tests.

### Highlights

* Slightly faster theoretical optimal than single build.
* Faster build stages
* Smaller artifacts -> faster upload
* Segregated build stages allows re-build only of the failing tenant
* Hypothesis: Less delay before tests per tenant, can impact queuing time positively.
