package com.example.cats_and_dogs.tflite

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*

// Define a Kotlin class named Classifier
class Classifier(assetManager: AssetManager, modelPath: String, labelPath: String, inputSize: Int) {
    // Declare properties and constants within the class
    private lateinit var interpreter: Interpreter
    private lateinit var labelList: List<String>
    private val INPUT_SIZE: Int = inputSize
    private val PIXEL_SIZE: Int = 3
    private val IMAGE_MEAN = 0
    private val IMAGE_STD = 255.0f
    private val MAX_RESULTS = 3
    private val THRESHOLD = 0.4f

    // Define a data class for recognition results
    data class Recognition(
        var id: String = "",
        var title: String = "",
        var confidence: Float = 0F
    ) {
        override fun toString(): String {
            return "Title = $title, Confidence = $confidence)"
        }
    }

    // Initialize the Classifier instance
    init {
        // Configure TensorFlow Lite Interpreter options
        val options = Interpreter.Options()
        options.numThreads = 5
        options.useNNAPI = true

        // Load the TensorFlow Lite model and label list
        interpreter = Interpreter(loadModelFile(assetManager, modelPath), options)
        labelList = loadLabelList(assetManager, labelPath)
    }

    // Helper function to load the TensorFlow Lite model file
    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // Helper function to load the label list
    private fun loadLabelList(assetManager: AssetManager, labelPath: String): List<String> {
        return assetManager.open(labelPath).bufferedReader().useLines { it.toList() }
    }

    // Perform image recognition on the input Bitmap
    fun recognizeImage(bitmap: Bitmap): List<Recognition> {
        // Scale the input Bitmap to the desired size
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false)

        // Convert the Bitmap to a ByteBuffer for model input
        val byteBuffer = convertBitmapToByteBuffer(scaledBitmap)

        // Run inference using the TensorFlow Lite model
        val result = Array(1) { FloatArray(labelList.size) }
        interpreter.run(byteBuffer, result)

        // Post-process the results and return a sorted list of Recognitions
        return getSortedResult(result)
    }

    // Helper function to convert a Bitmap to a ByteBuffer for model input
    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)

        // Extract pixel values from the Bitmap and normalize them
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until INPUT_SIZE) {
            for (j in 0 until INPUT_SIZE) {
                val input = intValues[pixel++]

                byteBuffer.putFloat((((input.shr(16) and 0xFF) - IMAGE_MEAN) / IMAGE_STD))
                byteBuffer.putFloat((((input.shr(6) and 0xFF) - IMAGE_MEAN) / IMAGE_STD))
                byteBuffer.putFloat((((input and 0xFF) - IMAGE_MEAN) / IMAGE_STD))
            }
        }
        return byteBuffer
    }

    // Helper function to sort and filter recognition results
    private fun getSortedResult(labelProbArray: Array<FloatArray>): List<Classifier.Recognition> {
        Log.d("Classifier", "List Size: (%d, %d, %d)".format(labelProbArray.size,labelProbArray[0].size,labelList.size))

        // Use a PriorityQueue to sort recognitions by confidence
        val pq = PriorityQueue(
            MAX_RESULTS,
            Comparator<Classifier.Recognition> {
                    (_, _, confidence1), (_, _, confidence2)
                -> confidence1.compareTo(confidence2) * -1
            })

        // Add recognitions to the PriorityQueue if they meet the confidence threshold
        for (i in labelList.indices) {
            val confidence = labelProbArray[0][i]
            if (confidence >= THRESHOLD) {
                pq.add(
                    Classifier.Recognition(
                        "" + i,
                        if (labelList.size > i) labelList[i] else "Unknown", confidence )
                )
            }
        }

        Log.d("Classifier", "pqsize:(%d)".format(pq.size))

        // Retrieve and return the top recognitions
        val recognitions = ArrayList<Classifier.Recognition>()
        val recognitionsSize = Math.min(pq.size, MAX_RESULTS)
        for (i in 0 until recognitionsSize) {
            recognitions.add(pq.poll())
        }
        return recognitions
    }
}
