package com.example.cats_and_dogs.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cats_and_dogs.R
import com.google.android.material.snackbar.Snackbar

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        // Set the content view
        setContentView(R.layout.activity_about)

        // Set up window insets handling for proper system bar spacing
        val mainView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up social media and contact buttons
        setupSocialButtons()
    }

    private fun setupSocialButtons() {
        // GitHub Button
        findViewById<ImageButton>(R.id.btnGitHub)?.setOnClickListener {
            openWebUrl("https://github.com/yourusername/PetDetector")
        }

        // Email Button
        findViewById<ImageButton>(R.id.btnEmail)?.setOnClickListener {
            sendEmail()
        }

        // Website Button
        findViewById<ImageButton>(R.id.btnWebsite)?.setOnClickListener {
            openWebUrl("https://www.petdetector.com")
        }
    }

    private fun openWebUrl(url: String) {
        try {
            val webpage = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            startActivity(intent)
        } catch (e: Exception) {
            // Show error if no browser app is available
            Snackbar.make(
                findViewById(R.id.main),
                "Unable to open web page",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun sendEmail() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:support@petdetector.com")
            putExtra(Intent.EXTRA_SUBJECT, "PetDetector App Inquiry")
        }

        try {
            startActivity(emailIntent)
        } catch (e: Exception) {
            // Show error if no email app is available
            Snackbar.make(
                findViewById(R.id.main),
                "No email app found",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    // Optional: Add back navigation
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}