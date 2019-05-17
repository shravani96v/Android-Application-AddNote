package com.decoders.demoapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentUris
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Base64
import android.util.Log
import android.util.LruCache
import android.widget.Toast

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.Random

/*
ImageUtil.showSourcePopup(this);

@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    Bitmap bmp;
    switch (requestCode) {
        case ImageUtil.GALL_CODE:
            bmp = ImageUtil.getGalleryImage(Test.this, data);
            if(bmp != null){
                imageView.setImageBitmap(bmp);
            }
            break;
        case ImageUtil.CAM_CODE:
            bmp = ImageUtil.getCameraImage(Test.this, data);
            if(bmp != null){
                imageView.setImageBitmap(bmp);
            }
            break;
        default:
            break;
    }
}
 */

object ImageUtil {

    //-------------------------------------------------Image Selection--------------------------------------------
    val GALL_CODE = 100
    var filePath: String? = null
    val CAM_CODE = 200
    private var cameraFile: File? = null
    var fileUri: Uri? = null

    //--------------------------------------------------Cache Image-------------------------------------------------

    private var mMemoryCache: LruCache<String, Bitmap>? = null

    fun showSourcePopup(context: Activity) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
            return
        }
        val options = arrayOf<CharSequence>("Camera", "Gallery")
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Source")
        builder.setItems(options) { dialog, which ->
            if (options[which] == "Camera") {
                openCamera(context)
            } else {
                openGallery(context)
            }
        }
        builder.show()
    }

    // call this function
    fun openGallery(context: Activity) {
        // gallery code
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        // optional paramters
        /*intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("outputX", 256);
        intent.putExtra("outputY", 256);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("return-data", true);*/
        try {
            context.startActivityForResult(intent, GALL_CODE)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, "Please install a File Manager.", Toast.LENGTH_SHORT).show()
        }

    }

    fun getGalleryImage(context: Activity, data: Intent?): Bitmap? {
        if (data != null) {
            Log.e("tag", "uri: " + data.data!!)
            fileUri = data.data
            return handleGallerySelection(context, data.data)
        }
        return null
    }

    private fun handleGallerySelection(context: Activity, uri: Uri?): Bitmap? {
        filePath = getPath(context, uri)
        Log.e("File path", "File Path: " + filePath!!)
        var bmp: Bitmap? = BitmapFactory.decodeFile(filePath)
        bmp = bmpAfterRotationCheck(bmp, filePath)
        return bmp
    }

    // call this function
    fun openCamera(context: Activity) {
        val extDir = Environment.getExternalStorageDirectory()
        val r = Random()
        val name = (r.nextInt(100000000) + r.nextInt(100000000)).toString() + "."
        cameraFile = File(extDir, name)
        if (checkExternalStorage()) {
            val text = "writing into externanal storage"
            val fos: FileOutputStream
            try {
                fos = FileOutputStream(cameraFile)
                fos.write(text.toByteArray())
                fos.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (cameraFile!!.isFile) {
                Log.e("tag", "is file")
                val cameraintent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraintent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile))
                context.startActivityForResult(cameraintent, CAM_CODE)
            } else {
                Log.e("tag", "oops not file")
            }
        } else {
            Log.e("tag", "External storage not available!")
        }
    }

    fun checkExternalStorage(): Boolean {
        val state = Environment.getExternalStorageState()
        return if (state == Environment.MEDIA_MOUNTED) {
            true
        } else false
    }

    private fun bmpAfterRotationCheck(bmp: Bitmap?, filePath: String?): Bitmap? {
        var bmp = bmp
        try {
            val exif = ExifInterface(filePath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
            Log.e("EXIF", "Exif: $orientation")
            val matrix = Matrix()
            if (orientation == 6) {
                matrix.postRotate(90f)
            } else if (orientation == 3) {
                matrix.postRotate(180f)
            } else if (orientation == 8) {
                matrix.postRotate(270f)
            }
            bmp = Bitmap.createBitmap(bmp!!, 0, 0, bmp.width, bmp.height, matrix, true) // rotating bitmap
            return bmp
        } catch (e: Exception) {
        }

        return bmp
    }

    @SuppressLint("NewApi")
    fun getPath(context: Context, uri: Uri?): String? {
        var uri = uri
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context.applicationContext, uri)) {
            if (isExternalStorageDocument(uri!!)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                uri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                if ("image" == type) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                selection = "_id=?"
                selectionArgs = arrayOf(split[1])
            }
        }
        if ("content".equals(uri!!.scheme!!, ignoreCase = true)) {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            var cursor: Cursor? = null
            try {
                cursor = context.contentResolver
                    .query(uri, projection, selection, selectionArgs, null)
                val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index)
                }
            } catch (e: Exception) {
            }

        } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    fun getCameraImage(context: Activity, data: Intent?): Bitmap? {
        if (data != null) {
            var uri: Uri? = null
            if (data.data == null) {
                //from camera
                if (cameraFile == null) {
                    Log.e("tag", "Cannot fetch photo!")
                    return null
                }
                uri = Uri.fromFile(cameraFile)
            } else {
                uri = data.data
            }
            fileUri = uri
            return handleCameraSelection(context, uri)
        } else {
            var uri: Uri? = null
            if (cameraFile == null) {
                Log.e("tag", "unable to fetch photo!")
                return null
            }
            uri = Uri.fromFile(cameraFile)
            fileUri = uri
            return handleCameraSelection(context, uri)
        }
    }

    private fun handleCameraSelection(context: Activity, uri: Uri?): Bitmap? {
        Log.e("tag", "uri: " + uri!!)
        filePath = getPath(context, uri)
        Log.e("File path", "File Path: " + filePath!!)
        var bmp: Bitmap? = BitmapFactory.decodeFile(filePath)
        bmp = bmpAfterRotationCheck(bmp, filePath)
        if (bmp != null) {
            return bmp
        } else {
            Log.e("tag", "Cannot retrieve photo!")
            return null
        }
    }

    private fun setMemoryCache() {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        mMemoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.byteCount / 1024
            }
        }
    }

    private fun addBitmapToCache(key: String?, bitmap: Bitmap) {
        if (mMemoryCache == null)
            setMemoryCache()
        if (key == null)
            return
        if (getBitmapFromCache(key) == null) {
            mMemoryCache!!.put(key, bitmap)
        }
    }

    fun getBitmapFromCache(key: String?): Bitmap? {
        if (mMemoryCache == null)
            setMemoryCache()
        return if (key == null) null else mMemoryCache!!.get(key)
    }

    //------------------------------------------------Image Resolution--------------------------------------------

    fun lessResolution(filePath: String, reqWidth: Int, reqHeight: Int): Bitmap {
        val options = BitmapFactory.Options()
        // First decode with inJustDecodeBounds=true to check dimensions
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(filePath, options)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            // Calculate ratios of height and width to requested height and width
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
    }

    fun getRoundedCornerBitmap(bitmap: Bitmap, pixels: Int): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)
        val roundPx = pixels.toFloat()

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

    fun convertToBase64(bitmapOrg: Bitmap): String {
        val bao = ByteArrayOutputStream()
        bitmapOrg.compress(Bitmap.CompressFormat.JPEG, 100, bao)
        val ba = bao.toByteArray()
        val ba1 = Base64.encodeToString(ba, Base64.DEFAULT)
        return ba1
    }

}
