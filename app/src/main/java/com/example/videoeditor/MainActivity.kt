package com.example.videoeditor

import android.content.Intent
import android.media.browse.MediaBrowser
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.media3.transformer.TransformationRequest
import androidx.media3.transformer.Transformer
import androidx.media3.common.MediaItem
import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var pickVideoButton: Button
    private lateinit var startTransformButton: Button
    private lateinit var outputPathText: TextView

    private var selectVideoUri: Uri? = null
    private lateinit var outputFilePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pickVideoButton = findViewById(R.id.pickVideoButton)
        startTransformButton = findViewById(R.id.startTransformButton)
        outputPathText = findViewById(R.id.outputPathText)

        pickVideoButton.setOnClickListener { pickVideo() }
        startTransformButton.setOnClickListener { transformVideo() }
    }

    private fun pickVideo() {
        val pickVideoIntent = Intent(Intent.ACTION_PICK).apply{
            type = "video/*"
        }
        videoPickerLauncher.launch(pickVideoIntent)
    }

    private val videoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            selectVideoUri = result.data?.data
            selectVideoUri?.let {
                startTransformButton.isEnabled = true
            }
        }
    }

    private fun transformVideo() {
        selectVideoUri?.let { inputUri ->

            val mediaItem = MediaBrowser.MediaItem.fromUri(inputUri)

            val outputDir = cacheDir
            val outputFile = File(outputDir, "transfomed_video.mp4")
            outputFilePath = outputFile.absolutePath

            val transformer = Transformer.Builder(this)
                .setTransformationRequest(
                    TransformationRequest.Builder()
                        .build()
            )
            .build()

            transformer.startTransformation( mediaItem, outputFilePath)

            outputPathText.text = "Output File Path: $outputFilePath"
        }
    }

}