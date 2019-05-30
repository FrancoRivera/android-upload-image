package pe.wemake.samplefileupload

import android.Manifest
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import java.io.File
import com.android.volley.VolleyError
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private val IMG_RESULT = 1
    var ImageDecode: String? = null
    var imageViewLoad: ImageView? = null
    val I_HAVE_PERIMISSIONS = 1
    //var intent: Intent? = null

    var FILE: Array<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageViewLoad = findViewById(R.id.imageView1)

        val LoadImage: Button = findViewById(R.id.button1)
        LoadImage.setOnClickListener {

            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        I_HAVE_PERIMISSIONS)
                }
            } else {
                // Permission has already been granted
                intent = Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                startActivityForResult(intent, IMG_RESULT)
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try
        {
            if ((requestCode === IMG_RESULT && resultCode === RESULT_OK
                        && null != data))
            {
                val URI = data.getData()
                val FILE = arrayOf<String>(MediaStore.Images.Media.DATA)
                val cursor = getContentResolver().query(URI,
                    FILE, null, null, null)
                cursor.moveToFirst()
                val columnIndex = cursor.getColumnIndex(FILE[0])
                ImageDecode = cursor.getString(columnIndex)
                cursor.close()
                imageViewLoad!!.setImageBitmap(
                    BitmapFactory
                    .decodeFile(ImageDecode))


                // send data to API

                val url = "http://10.0.2.2:3000/upload_category/"
                val multipartRequest = object :
                    VolleyMultipartRequest(Request.Method.POST, url, object : Response.Listener<NetworkResponse> {
                        override fun onResponse(response: NetworkResponse) {
                            val resultResponse = String(response.data)
                            // parse success output
                        }
                    }, object : Response.ErrorListener {
                        override fun onErrorResponse(error: VolleyError) {
                            error.printStackTrace()
                        }
                    }) {
                    override fun getParams():HashMap<String, String> {
                        return hashMapOf("id" to "3")
                    }
                    // file name could found file base or direct access from real path
                    // for now just get bitmap data from ImageView

                    override fun getByteData(): Map<String, DataPart>{
                            val params = HashMap<String, DataPart>()
                        imageViewLoad.let {
                            params.put(
                                "file", DataPart(
                                    "event_image.jpg",
                                    readBytes(this@MainActivity, URI),
                                    "image/jpeg"
                                )
                            )

                        }
                            return params
                        }
                }
                VolleySingleton.getInstance(baseContext).addToRequestQueue(multipartRequest)
            }
        }
        catch (e:Exception) {
            Toast.makeText(this, "Please try again", Toast.LENGTH_LONG)
                .show()
        }

    }
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            I_HAVE_PERIMISSIONS -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    intent = Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    startActivityForResult(intent, IMG_RESULT)
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }
    @Throws(IOException::class)
    private fun readBytes(context: Context, uri: Uri): ByteArray? =
        context.contentResolver.openInputStream(uri)?.buffered()?.use { it.readBytes() }
}
