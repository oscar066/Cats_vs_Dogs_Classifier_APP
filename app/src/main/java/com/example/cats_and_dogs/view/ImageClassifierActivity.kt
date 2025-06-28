package com.example.cats_and_dogs.view

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.cats_and_dogs.R
import com.example.cats_and_dogs.tflite.Classifier
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar

class ImageClassifierActivity : AppCompatActivity(), View.OnClickListener {

    private val mInputSize = 224
    private val mModelPath = "converted_model.tflite"
    private val mLabelPath = "label.txt"
    private lateinit var classifier: Classifier
    private lateinit var classificationResultText: TextView
    private lateinit var classificationCardView: CardView
    private var lastClickedImageView: ImageView? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Appbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "PetDetect Pro"

        // Classification result view
        classificationResultText = findViewById(R.id.classification_result)
        classificationCardView = findViewById(R.id.classification_card)

        // Bottom navigation
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        initClassifier()
        initViews()
    }

    private fun initClassifier() {
        classifier = Classifier(assets, mModelPath, mLabelPath, mInputSize)
    }

    private fun initViews() {
        val imageViews = listOf(
            R.id.iv_1, R.id.iv_2, R.id.iv_3,
            R.id.iv_4, R.id.iv_5, R.id.iv_6
        )

        imageViews.forEach { imageViewId ->
            findViewById<ImageView>(imageViewId).setOnClickListener(this)
        }
    }

    override fun onClick(view: View?) {
        // Reset previous image view
        lastClickedImageView?.alpha = 1f
        classificationCardView.visibility = View.GONE

        // Set current clicked image view
        lastClickedImageView = view as? ImageView

        // Dim the image slightly to indicate selection
        view?.alpha = 0.5f

        val bitmap = ((view as? ImageView)?.drawable as? BitmapDrawable)?.bitmap

        if (bitmap != null) {
            val results = classifier.recognizeImage(bitmap)

            if (results.isNotEmpty()) {
                val topResult = results[0]

                val title = topResult.title
                val confidence = topResult.confidence

                runOnUiThread {
                    // Show classification in card view with animation
                    classificationResultText.text = "Detected: $title\nConfidence: ${String.format("%.2f%%", confidence * 100)}"
                    classificationCardView.visibility = View.VISIBLE

                    // Fade in animation for classification result
                    val fadeIn = AlphaAnimation(0f, 1f)
                    fadeIn.duration = 500
                    classificationCardView.startAnimation(fadeIn)

                    // Slight elevation animation
                    ObjectAnimator.ofFloat(classificationCardView, "translationZ", 10f).apply {
                        duration = 300
                        start()
                    }
                }
            } else {
                // No objects recognized
                runOnUiThread {
                    Snackbar.make(view, "No objects recognized", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Updated bottom navigation listener to launch About page
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.menu_upload -> {
                Toast.makeText(this, "Uploading ...", Toast.LENGTH_SHORT).show()
                return@OnNavigationItemSelectedListener true
            }
            R.id.menu_video -> {
                // Launch About page
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true
            }
            R.id.menu_camera -> {
                Toast.makeText(this, "Coming soon ...", Toast.LENGTH_SHORT).show()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_camera -> {
                true
            }
            R.id.menu_settings -> {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_upload -> {
                Toast.makeText(this, "Upload on progress", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_video -> {
                // Launch About page from options menu as well
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}