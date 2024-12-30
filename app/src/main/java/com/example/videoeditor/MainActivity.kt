package com.example.videoeditor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Transformer
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
        val pickVideoIntent = Intent(Intent.ACTION_PICK).apply {
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

    @OptIn(UnstableApi::class)
    private fun transformVideo() {
        selectVideoUri?.let { inputUri ->

            val mediaItem = MediaItem.fromUri(inputUri)
            val outputDir = cacheDir
            val outputFile = File(outputDir, "transformed_video.mp4")
            outputFilePath = outputFile.absolutePath

            val transformer = Transformer.Builder(this)
                .setAudioMimeType("audio/mp4a-latm")
                .setVideoMimeType("video/avc")
                .build()

            transformer.start(mediaItem, outputFilePath)

            outputPathText.text = getString(R.string.output_file_path, outputFilePath)
        }
    }
}
