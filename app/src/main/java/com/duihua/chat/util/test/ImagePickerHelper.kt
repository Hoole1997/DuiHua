package com.duihua.chat.util.test

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImagePickerHelper(private val activity: ComponentActivity) {
    // 图片选择回调
    private var imageCallback: ((Uri?) -> Unit)? = null
    // 视频选择回调
    private var videoCallback: ((Uri?) -> Unit)? = null
    // 临时保存拍照图片的 Uri
    private lateinit var tempPhotoUri: Uri

    // 注册图片选择器
    private val pickImage = activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        imageCallback?.invoke(uri)
    }
    
    // 注册视频选择器
    private val pickVideo = activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        videoCallback?.invoke(uri)
    }

    // 注册拍照
    private val takePhoto = activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageCallback?.invoke(tempPhotoUri)
        } else {
            imageCallback?.invoke(null)
        }
    }

    // 注册存储权限请求
    private val requestStoragePermission = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchPhotoPicker()
        } else {
            imageCallback?.invoke(null)
        }
    }
    
    // 注册视频存储权限请求
    private val requestVideoStoragePermission = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchVideoPicker()
        } else {
            videoCallback?.invoke(null)
        }
    }

    // 注册相机权限请求
    private val requestCameraPermission = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            imageCallback?.invoke(null)
            Toast.makeText(activity, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 选择图片（从相册）
     */
    fun pickImage(callback: (Uri?) -> Unit) {
        imageCallback = callback

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                launchPhotoPicker()
            }
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchPhotoPicker()
            }
            else -> {
                requestStoragePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
    
    /**
     * 选择视频（从相册）
     */
    fun pickVideo(callback: (Uri?) -> Unit) {
        videoCallback = callback

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                launchVideoPicker()
            }
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchVideoPicker()
            }
            else -> {
                requestVideoStoragePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    /**
     * 拍照
     */
    fun takePhoto(callback: (Uri?) -> Unit) {
        imageCallback = callback

        when {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            else -> {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchPhotoPicker() {
        pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    
    private fun launchVideoPicker() {
        pickVideo.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
    }

    private fun launchCamera() {
        try {
            val photoFile = createImageFile()
            tempPhotoUri = FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.fileprovider",
                photoFile
            )
            takePhoto.launch(tempPhotoUri)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(activity, "无法启动相机", Toast.LENGTH_SHORT).show()
            imageCallback?.invoke(null)
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }
}
