package com.github.kronenthaler.ghasimulator.io

interface IncomingStream {
    fun incomingStream(): Iterable<Double>
}