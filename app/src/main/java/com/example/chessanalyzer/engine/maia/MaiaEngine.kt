package com.example.chessanalyzer.engine.maia

import android.content.Context
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MaiaEngine(private val context: Context) {

    private var interpreter: Interpreter? = null

    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            val assetFileDescriptor = context.assets.openFd("maia_1500.tflite")
            val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            val mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            interpreter = Interpreter(mappedByteBuffer)
            assetFileDescriptor.close()
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error: e.g., log it, show a toast, or disable Maia functionality
        }
    }

    fun predictMove(fen: String): String? {
        if (interpreter == null) {
            println("Maia TFLite interpreter not loaded.")
            return null
        }

        val inputBuffer = FenProcessor.fenToTensor(fen)

        // Output buffer for policy head (1858 floats)
        val outputBuffer = ByteBuffer.allocateDirect(1858 * 4).order(ByteOrder.nativeOrder())

        try {
            interpreter?.run(inputBuffer, outputBuffer)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        outputBuffer.rewind()
        val policyOutput = FloatArray(1858)
        outputBuffer.asFloatBuffer().get(policyOutput)

        // Find the index with the highest probability
        val bestMoveIndex = policyOutput.indices.maxByOrNull { policyOutput[it] }

        return bestMoveIndex?.let { FenProcessor.indexToMove(it) }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
