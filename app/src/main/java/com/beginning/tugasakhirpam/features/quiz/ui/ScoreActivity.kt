package com.beginning.tugasakhirpam.features.quiz.ui

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.beginning.tugasakhirpam.MainActivity
import com.beginning.tugasakhirpam.databinding.ActivityScoreBinding

class ScoreActivity : AppCompatActivity() {

    private lateinit var contentBinding: ActivityScoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentBinding = ActivityScoreBinding.inflate(layoutInflater)
        setContentView(contentBinding.root)


        val quizId = intent.getStringExtra("QUIZ_ID") ?: ""
        val userId = intent.getStringExtra("USER_ID") ?: ""
        val result = intent.getIntExtra("RESULT", 0)

        contentBinding.tvResult.text = result.toString()

        contentBinding.btnSaveImg.setOnClickListener {
            val bitmap = takeScreenshot(contentBinding.root)
            val uri = saveBitmapToGallery(this, bitmap, "quiz_result_${System.currentTimeMillis()}")

            if (uri != null) {
                Toast.makeText(this, "Screenshot saved!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to save screenshot.", Toast.LENGTH_SHORT).show()
            }
        }

        contentBinding.btnDone.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }

    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, filename: String): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$filename.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QuizScreenshots")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (uri != null) {
            try {
                resolver.openOutputStream(uri)?.use { outStream ->
                    if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)) {
                        Log.e("SaveBitmap", "Failed to compress and save bitmap")
                        return null
                    }
                } ?: run {
                    Log.e("SaveBitmap", "OutputStream is null")
                    return null
                }

                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)

                return uri
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        } else {
            Log.e("SaveBitmap", "Failed to create MediaStore entry")
            return null
        }
    }


    fun takeScreenshot(view: View): Bitmap {
        val screenshot = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(screenshot)
        view.draw(canvas)
        return screenshot
    }
}