package com.aptcoder.manwomanrecognizer

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.databinding.DataBindingUtil
import com.aptcoder.manwomanrecognizer.databinding.ActivityMainBinding
import com.aptcoder.manwomanrecognizer.ml.Manwoman
import com.github.dhaval2404.imagepicker.ImagePicker
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.lang.Exception
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    private lateinit var mainActivity: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = DataBindingUtil.setContentView(this, R.layout.activity_main)

        mainActivity.getImagebtn.setOnClickListener {
            ImagePicker.with(this)
                .compress(1024)         //Final image size will be less than 1 MB(Optional)
                .maxResultSize(
                    255,
                    255
                )  //Final image resolution will be less than 1080 x 1080(Optional)
                .start { resultCode, data ->
                    when (resultCode) {
                        Activity.RESULT_OK -> {
                            //Image Uri will not be null for RESULT_OK
                            val fileUri: Uri = data?.data!!
                            mainActivity.imageView.setImageURI(fileUri)

                            //You can get File object from intent
                            val file: File? = ImagePicker.getFile(data)

                            //You can also get File Path from intent
                            val filePath: String? = ImagePicker.getFilePath(data)
                        }
                        ImagePicker.RESULT_ERROR -> {
                            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT)
                                .show()
                        }
                        else -> {
                            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
        val model = Manwoman.newInstance(this)
        mainActivity.recognizebtn.setOnClickListener {


            val bitmap:Bitmap
            mainActivity.imageView.invalidate()
            try {
           bitmap = mainActivity.imageView.drawable.toBitmap()
            }catch (e:Exception){
                Toast.makeText(this, "Select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Creates inputs for reference.
            val image = TensorImage.fromBitmap(bitmap)

            // Runs model inference and gets result.
            val outputs = model.process(image)
            val probability = outputs.probabilityAsCategoryList
         probability.sortBy {
             it.score
         }
            if((probability[1].score * 100).roundToInt()>=70){
                mainActivity.result.text = "---->"+probability[1].label+ "<----\n" +"Model is "+ (probability[1].score * 100).roundToInt() +"% Confident"
            }else{
                mainActivity.result.text="Not Sure Please Take image again \nBut ${(probability[1].score * 100).roundToInt()} % chances are that image is of a ${probability[1].label}"
            }


// Releases model resources if no longer used.
           
        }

    }


}