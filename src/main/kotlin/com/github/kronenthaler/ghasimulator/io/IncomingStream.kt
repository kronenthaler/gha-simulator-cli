package com.github.kronenthaler.ghasimulator.io

fun interface IncomingStream {
    fun incomingStream(): Iterable<Double>
}
