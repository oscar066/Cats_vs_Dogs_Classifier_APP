package com.example.cats_and_dogs.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import com.example.cats_and_dogs.R
import com.example.cats_and_dogs.tflite.Classifier
import com.google.android.material.appbar.MaterialToolbar

private val REQUEST_IMAGE_CAPTURE = 1

class ImageClassifierActivity : AppCompatActivity(), View.OnClickListener {

    private val mInputSize = 224
    private val mModelPath = "converted_model.tflite"
    private val mLabelPath = "label.txt"
    private lateinit var classifier: Classifier

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        // Set the toolbar as the support action bar
        setSupportActionBar(toolbar)
        initClassifier()
        initViews()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu_toolbar.xml menu resource
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        // Call the superclass method for creating the options menu
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_camera -> {
                // Handle the camera button click here
                openCameraApp()
                true
            }
            R.id.menu_settings -> {
                // Handle the Settings button click
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openCameraApp() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
        } else {
            Toast.makeText(this, "Camera app not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initClassifier() {
        classifier = Classifier(assets, mModelPath, mLabelPath, mInputSize)
    }

    private fun initViews(){
        findViewById<ImageView>(R.id.iv_1).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_2).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_3).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_4).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_5).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_6).setOnClickListener(this)
    }
    override fun onClick(view: View?) {
        val bitmap = ((view as? ImageView)?.drawable as? BitmapDrawable)?.bitmap

        if (bitmap != null) {
            val results = classifier.recognizeImage(bitmap)

            if (results.isNotEmpty()) {
                val topResult = results[0]

                val title = topResult.title
                val confidence = topResult.confidence

                val message = "Title: $title\nConfidence: $confidence"

                runOnUiThread {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            } else {
                // Handle the case where no objects were recognized
                runOnUiThread {
                    Toast.makeText(this, "No objects recognized", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}