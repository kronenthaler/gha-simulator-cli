package com.github.kronenthaler.ghasimulator.io

import com.github.kronenthaler.ghasimulator.engine.Job
import com.github.kronenthaler.ghasimulator.engine.JobQueue
import com.github.kronenthaler.ghasimulator.engine.Pipeline
import com.github.kronenthaler.ghasimulator.stats.PipelineStats
import org.yaml.snakeyaml.Yaml
import java.io.File

class YamlPipelineFactory(val file: File) : PipelineFactory {

    override fun createPipeline(jobQueue: JobQueue, stats: MutableList<PipelineStats>): Pipeline {
        val yaml = Yaml()
        val inputStream = file.inputStream()
        val data = yaml.load<Map<String, Any>>(inputStream)

        val jobs = data["jobs"] as? Map<String, Map<String, Any>> ?: emptyMap()

        val jobsCache = mutableMapOf<String, Job>()
        val jobsInProgress = mutableSetOf<String>()
        jobs.forEach { name, _ ->
            resolveJob(name, jobs, jobsCache, jobsInProgress)
        }

        // determine potential roots
        val isDependentOn = jobsCache.values.map { it.needs.map { it.name } }.flatten().toSet()
        val allJobs = jobsCache.values.map { it.name }.toSet()
        val rootNames = allJobs - isDependentOn // all jobs that are not depended on

        val pipelineName = data["name"] as? String ?: file.nameWithoutExtension
        val roots = jobsCache.values.filter { rootNames.contains(it.name) }.toList()

        return Pipeline(pipelineName, jobQueue, stats, roots)
    }

    private fun resolveJob(
        jobName: String,
        jobs: Map<String, Map<String, Any>>,
        jobsCache: MutableMap<String, Job>,
        jobsInProgress: MutableSet<String>
    ): Job {
        if (jobsCache.containsKey(jobName)) {
            return jobsCache[jobName]
                ?: throw IllegalStateException("Job $jobName not found in cache")
        }

        check(!jobsInProgress.contains(jobName)) {
            "Circular dependency detected for job: $jobName"
        }

        // mark job as resolving
        jobsInProgress.add(jobName)

        val job = jobs[jobName] ?: throw IllegalArgumentException("Job $jobName not found in YAML")
        val runningTime =
            job["time"] as? Int ?: throw IllegalArgumentException("Job $jobName misses required `time` definition")
        val runsOn = job["runs-on"] as? String
            ?: throw IllegalArgumentException("Job $jobName misses required `runs-on` definition")
        val needs = (job["needs"] as? List<String>) ?: emptyList()

        // resolve dependencies
        val resolvedNeeds = mutableListOf<Job>()
        for (needName in needs) {
            val resolvedJob = resolveJob(needName, jobs, jobsCache, jobsInProgress)
            resolvedNeeds.add(resolvedJob)
        }

        val resolvedJob = Job(jobName, runningTime, runsOn, resolvedNeeds)
        jobsCache[jobName] = resolvedJob // cache resolved job
        jobsInProgress.remove(jobName) // mark job as resolved

        return resolvedJob
    }
}
