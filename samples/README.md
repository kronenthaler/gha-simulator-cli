# Samples folder

In this folder, we provide configuration files and structures that can be used to run a simulation and observe how things behave, as well as some advice on how to read the results and what they can mean.

The examples here use a hypothetical setup for an iOS app.

We are running on-premises, and we have a number of fixed runners: let's say 10 macOS runners and 10 Linux runners that can be used for miscellaneous tasks.

The app is a white-label source code that can be built into two different skinned apps. Let's call them tenants A and B.

The app codebase also contains multiple standalone modules, each with its own test suite.

For each app, we want to run different kinds of tests: unit tests, snapshot tests, and UI tests.

We also have a code quality step (like Sonar or Codacy) and a final reporting step at the end. These can run on a Linux runner.

With all these baselines, we can sketch different ways to build and test the apps, with different trade-offs. For example, we can try to minimize the total running time, minimize the queue time, or optimize for re-run scenarios (e.g., flaky tests).

We can go wild with considerations. For example, in GitHub Enterprise Server (on-premises GH), uploading large artifacts has a significant time cost. A 500MB artifact could take about 5-7 minutes to upload.

## Fully Parallel

![fully parallel](.md/fully-parallel.svg)

We can organize the workflow so that each item is built and tested in its own job. Assuming unlimited runners, this will be the most optimal way to run, giving the lowest theoretical minimum running time. However, runners are not unlimited.

### Highlights

* Minimum theoretical optimum.
* Each phase is a full build-and-test cycle.
* Avoids the large artifact time cost.
* Faster worst-case re-runs.

## Single Build with Parallel Tests

![](./.md/single-build-parallel-test.svg)

We can organize the app project to build all the tests and both tenants in a single step (build-all-tests) and pass that artifact to the next stage, where tests are simply run against that artifact.
The premise of this approach is that the build, done this way, can leverage some compiler internals and speed up the build time. However, this will produce a large artifact that has to be uploaded. On the other hand, the tests will run for a shorter time, making more runners available faster for other jobs.

### Highlights

* The build job builds all tests in one go, including all module tests.
* Tests are run in parallel.
* Large artifact.
* The first step creates a bottleneck.

## Build Tenants Separately, Test in Parallel Together

![](./.md/tenants-separately-parallel-test.svg)

Another possibility is to build the tenants separately (and the modules as well). Each job will compile in a shorter time (than [single build with parallel tests](#single-build-with-parallel-tests)) and produce smaller artifacts.

### Highlights

* Slightly faster theoretical optimum than single build.
* Faster build stages.
* Smaller artifacts → faster upload.
* Segregated build stages allow re-building only the failing tenant.

## Independent Tenant Build and Tests

![](./.md/independent-tenant-build-and-tests.svg)

An iteration on the structure above, where the testing stage will not wait for both tenants to be built before starting the testing. This will start testing a tenant as soon as it's ready.

### Highlights

* Slightly faster theoretical optimum than single build.
* Faster build stages.
* Smaller artifacts → faster upload.
* Segregated build stages allow re-building only the failing tenant.
* Hypothesis: Less delay before tests per tenant can positively impact queuing time.

# Simulation

Using the CLI provided out of the box, we can script the simulation of these structures and collect the data in files for post-processing.
We also add some logging options that will be helpful for unsupervised runs.

```shell
cd samples
mkdir -p logs
mkdir -p data
for structure in $(find structures -name '*.yaml'); do
  time env JAVA_OPTS="-Djava.util.logging.config.file=logging.properties" \
  ../build/install/gha-simulator-cli/bin/gha-simulator-cli \
    --output data/$(basename $structure).output \
    --print-stats \
    configs.yml \
    $structure \
    incoming.txt
done
```

Bear in mind that the simulation could take a while, especially if you have a large number of events in the `incoming.txt` file.

> Note: For the configuration, incoming events, and structures provided, the simulation time was around 11 minutes on an M2 MacBook Pro.

## Sample Results

After each run, a data file is generated in the `data` folder, containing the results of each pipeline run. The data file is a CSV file with the following columns:
Run in mins, Start time in ms, End time in ms, Queue time in mins, and number of jobs.

If you decide to print the stats from the CLI, that will already provide some basic statistics, like average, standard deviation, percentiles (50 & 75), min, and max for the run time and queue time.

Below is a short table comparing the results of the different structures and some hints on how to read it.

### Run Time (in min)

| Structure                        | Average | St.Dev | pc50 | pc75 | Min | Max |
|----------------------------------|---------|--------|------|------|-----|-----|
| FullyParallel                    | 52.04   | 30.92  | 40   | 60   | 30  | 178 |
| SingleBuildParallelTest          | 58.14   | 12.08  | 53   | 63   | 49  | 118 |
| TenantSeparatelyAndTest          | 59.49   | 27.98  | 47   | 64   | 41  | 165 |
| IndependentBuildAndTestPerTenant | 59.58   | 27.96  | 47   | 64   | 41  | 165 |

A few things to note here: the minimum provides you with the theoretical optimal performance of that particular structure.
This theoretical minimum can always be achieved with enough runners. This means two things:
1. If that minimum is not good enough for your case, no matter how many runners you have, you will not be able to achieve it.
2. If that minimum is good enough for your use case, you can always scale up the runners to achieve it.

For example, we see that fully parallel, given enough resources, is the best running time. However, under constraints (limited runners), the average run time is much worse than the optimal. It has a high standard deviation, and the median (pc50) is 10 minutes above the optimal.

On the other hand, the single build with parallel tests has a worse theoretical minimum, but it's much more stable under constraints (lower standard deviation), and the median is much closer to the optimal.

Oddly enough, between these two structures, the single build might be a better choice depending on the trade-offs you want to make—if stability and predictability are important, versus minimizing average run time.

But we have two more structures that sit in between those two extremes, taking advantage of both cases (at least in theory). These two structures are very similar to one another, and the results are also very close.
When it comes to the theoretical minimum, they are slightly better than the single build with parallel tests, but not as good as the fully parallel. Their standard deviation is slightly lower than the fully parallel, but the difference between the median and the minimum is the smallest.
However, the averages are the worst of the four.

This could be a good example if you know how your incoming events are distributed over time. It might be very effective with sparse events, but performs very badly under heavy load.

### Queue Time (in min)

The reports also provide the queue time, which is the time that a job spends waiting in the queue before being picked up by a runner. This is important to understand how well the structure can handle incoming events and how it scales with the number of runners.

| Structure                        | Average | St.Dev  | pc50 | pc75 | Min | Max  |
|----------------------------------|---------|---------|------|------|-----|------|
| FullyParallel                    | 133.02  | 210.11  | 33   | 101  | 0   | 1011 |
| SingleBuildParallelTest          | 30.23   | 50.84   | 4    | 42   | 0   | 317  |
| TenantSeparatelyAndTest          | 73.83   | 122.05  | 11   | 91   | 0   | 549  |
| IndependentBuildAndTestPerTenant | 74.25   | 122.56  | 11   | 95   | 0   | 559  | 

The minimum queue time is almost always going to be 0. There is always one runner that will be available for the first job.

These numbers help to understand the running time, and we can make some considerations on where the time is spent during the run. Notice one important thing: despite having the highest average queue time, the fully parallel structure has the lowest average run time.
This is because the jobs are running in parallel; the only limitation is the number of runners available and the duration of the jobs themselves.

The queue time can also be useful to diagnose bottlenecks and help make a case to get more runners, or to pivot to a solution that makes better use of the runners available.

## Next steps

From here, you can simulate how many additional runners are needed to improve performance to a level that is acceptable for your case. Simply use different configuration files with various runner distributions and script it to run those different configurations.