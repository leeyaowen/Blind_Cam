package com.example.myapplication

import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivityMainBinding
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExcutor:ExecutorService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExcutor = Executors.newSingleThreadExecutor()

        if (allPermissionGranted()){
            startCamera()
        }else{
            ActivityCompat.requestPermissions(
                this, Constants.REQIRED_PERMISSIONS,
                Constants.REQUEST_CODE_PERMISSIONS
            )
        }

        binding.btnTakePhoto.setOnClickListener{
            takephoto()
        }
    }

    private fun takephoto(){

        val imageCapture = imageCapture ?: return
        val photoFile = File(
            externalMediaDirs.first(),
            SimpleDateFormat(Constants.FILE_NAME_FORMAT,
                Locale.getDefault())
                .format(System
                    .currentTimeMillis()) + ".jpg")

        val outputOption = ImageCapture
            .OutputFileOptions
            .Builder(photoFile)
            .build()

        imageCapture.takePicture(
            outputOption, ContextCompat.getMainExecutor(this),
            object :ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                    val saveUri = Uri.fromFile(photoFile)
                    val msg = "Photo Saved"

                    Toast.makeText(this@MainActivity,
                    "$msg $saveUri",
                    Toast.LENGTH_SHORT
                    ).show()

                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(Constants.TAG,
                    "onError: ${exception.message}",
                    exception)
                }
            }
        )
    }

    private fun startCamera(){

        val cameraProvderFuture = ProcessCameraProvider
            .getInstance(this)

        cameraProvderFuture.addListener({

            val cameraProvider: ProcessCameraProvider = cameraProvderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { mPreview->

                    mPreview.setSurfaceProvider(
                        binding.viewFinder.surfaceProvider
                    )

                }
            imageCapture = ImageCapture.Builder()
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this, cameraSelector,
                    preview, imageCapture
                )

            }catch (e:Exception){
                Log.d(Constants.TAG, "startCamera Fail", e)
            }
        }, ContextCompat.getMainExecutor(this))

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_CODE_PERMISSIONS){
            if (allPermissionGranted()){
                startCamera()
            }else{
                Toast.makeText(this,
                "Permission not granted by the user.",
                Toast.LENGTH_SHORT).show()

                finish()
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val action: Int = event.getAction()
        val keyCode: Int = event.getKeyCode()
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (action == KeyEvent.ACTION_DOWN) {
                    //TODO
                }
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (action == KeyEvent.ACTION_DOWN) {
                    takephoto()
                }
                true
            }
            else -> super.dispatchKeyEvent(event)
        }
    }

    private fun allPermissionGranted() =
        Constants.REQIRED_PERMISSIONS.all{
            ContextCompat.checkSelfPermission(
                baseContext, it
            ) == PackageManager.PERMISSION_GRANTED
        }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExcutor.shutdown()
    }


}