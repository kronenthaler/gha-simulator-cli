package com.github.kronenthaler.ghasimulator

class Job(val name: String, val runningTime: Int, val runsOn: String, val needs: List<Job>) {
    var isCompleted: Boolean = false
        private set

    var isScheduled: Boolean = false
        private set

    var startQueueTime: Long = 0
        set(time) {
            field = time
            isScheduled = true
        }

    var endQueueTime: Long = 0

    fun markCompleted() {
        isCompleted = true
    }
}