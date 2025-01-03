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
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var pickVideoButton: Button
    private lateinit var useSampleVideoButton: Button
    private lateinit var startTransformButton: Button
    private lateinit var outputPathText: TextView

    private var selectVideoUri: Uri? = null
    private lateinit var outputFilePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pickVideoButton = findViewById(R.id.pickVideoButton)
        startTransformButton = findViewById(R.id.startTransformButton)
        useSampleVideoButton = findViewById(R.id.useSampleVideoButton)
        outputPathText = findViewById(R.id.outputPathText)

        pickVideoButton.setOnClickListener { pickVideo() }
        useSampleVideoButton.setOnClickListener{ useSampleVideo() }
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

    private fun useSampleVideo() {
        val videoUri = Uri.parse("android.resource://${packageName}/raw/sample_video")
        selectVideoUri = videoUri
        startTransformButton.isEnabled = true
    }

    private fun playOutput() {
        val outputUri = Uri.fromFile(File(outputFilePath))
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(outputUri, "video/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)
    }

    @OptIn(UnstableApi::class)
    private fun transformVideo() {
        selectVideoUri?.let { inputUri ->

            val mediaItem = MediaItem.fromUri(inputUri)
            val outputDir = cacheDir
            val outputFile = File(outputDir, "transformed_video.mp4")
            outputFilePath = outputFile.absolutePath

            val transformerListener: Transformer.Listener =
                object : Transformer.Listener {
                    override fun onCompleted(composition: Composition, result: ExportResult) {
                        runOnUiThread {
                            outputPathText.text = getString(R.string.transformation_completed, outputFilePath)
                            playOutput()
                        }
                    }

                    override fun onError(composition: Composition,
                                         result: ExportResult,
                                         exception: ExportException
                    ) {
                        runOnUiThread {
                            outputPathText.text = getString(R.string.error_transformation_failed, exception.message)
                        }
                    }
                }

            val transformer = Transformer.Builder(this)
                .setVideoMimeType(MimeTypes.VIDEO_H265)
                .addListener(transformerListener)
                .build()

            try {
                transformer.start(mediaItem, outputFilePath)
                outputPathText.text = getString(R.string.output_file_path, outputFilePath)
            } catch (e: Exception) {
                outputPathText.text = getString(R.string.error_transformation_failed, e.message)
            }
        } ?: run {
            outputPathText.text = getString(R.string.error_no_video_selected)
        }
    }
}
