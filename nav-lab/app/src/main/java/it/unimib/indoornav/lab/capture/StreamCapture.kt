package it.unimib.indoornav.lab.capture

interface StreamCapture {
    val streamKey: String
    fun start(): Boolean
    fun stop()
}
