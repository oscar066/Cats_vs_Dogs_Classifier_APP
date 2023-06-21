package com.example.cats_and_dogs

import android.graphics.drawable.BitmapDrawable
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val mInputSize = 224
    private val mModelPath = "converted_model.tflite"
    private val mLabelPath = "label.txt"
    private lateinit var classifier: Classifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initClassifier()
        initViews()
    }

    private fun initClassifier(){
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
    override fun onClick(view: View?){
        val bitmap = ((view as ImageView).drawable as BitmapDrawable).bitmap

        val result = classifier.recognizeImage(bitmap)

        runOnUiThread{
            Toast.makeText(this, result.get(0).title, Toast.LENGTH_SHORT).show()
        }
    }
}