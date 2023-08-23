package com.tkfaart.scrutin.avoteressoubre.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.os.Build
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.NetworkUtils
import com.tkfaart.scrutin.avoteressoubre.models.ElectoratModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.UnknownHostException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Commons {

    companion object{
        fun loadJSONFromAsset(filename: String, context: Activity): String? {
            val json: String

            if(Build.VERSION_CODES.O <= Build.VERSION.SDK_INT ){

                try {
                    val inputStream: InputStream = context.assets.open(filename)
                    val size: Int = inputStream.available()
                    val buffer = ByteArray(size)
                    inputStream.read(buffer)
                    inputStream.close()
                    json = String(buffer, Charsets.UTF_8)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                }
            }else{
                val assetManager: AssetManager = context.assets

                try {
                    val inputStream = assetManager.open(filename)
                    val inputStreamReader = InputStreamReader(inputStream)
                    val bufferedReader = BufferedReader(inputStreamReader)
                    val stringBuilder = StringBuilder()
                    var line: String? = bufferedReader.readLine()
                    while (line != null) {
                        stringBuilder.append(line).append('\n')
                        line = bufferedReader.readLine()
                    }
                    json = stringBuilder.toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                }
            }

            Log.d(context.localClassName, "Asset Content ${json}")
            return json
        }

        @SuppressLint("NewApi")
        fun getDateFormat(): String? {
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")
            val formattedDateTime = currentDateTime.format(formatter)

            return formattedDateTime
        }

        val iS_RESP: Boolean = false
        val IS_RESULT: Boolean = true //to define si c'est pour RESULTAT
        var phoneNumber = "+2250565362426"

        fun checkIfSimAvailaible(ctx: Context): Boolean {
            val telMgr = ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val simState = telMgr.simState
            var state = false
            when (simState) {
                TelephonyManager.SIM_STATE_ABSENT ->                 // do something
                    state = false
                TelephonyManager.SIM_STATE_READY -> //                // do something
                    state = true
                TelephonyManager.SIM_STATE_UNKNOWN ->                 // do something
                    state = false
            }
            return state
        }

        fun parseJsonToElectoratModel(json: String): List<ElectoratModel> {
            val persons = mutableListOf<ElectoratModel>()
            try {
                val jsonObject = JSONObject(json)
                val jsonArray = jsonObject.getJSONArray("rows")

                for (i in 0 until jsonArray.length()) {
                    val personObject = jsonArray.getJSONObject(i)
                    //if(personObject.getString("zone").toInt() == zoneId!!.toInt()) {
                    val person = ElectoratModel(
                        personObject.getString("subid"),
                        personObject.getString("nom_prenoms"),
                        personObject.getString("zone").toInt(),
                    )
                    persons.add(person)
                    //}
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return persons
        }

        fun smsSendMessage(message: String, phonenumber: String, ctx: Context) {
            val prf = PrefManager(ctx)
            if (checkIfSimAvailaible(ctx) === true) {
                // Get the phone number
                val destinationAddress: String = phonenumber
                // Get the text of the SMS message.
                val smsMessage: String = message
                // Check for permission first.
                checkForSmsPermission(ctx as Activity)
                // Use SmsManager.
                val smsManager: SmsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(
                    destinationAddress, null, smsMessage,
                    null, null
                )
                Toast.makeText(ctx,
                    "Envoie votant effectué avec succès !", Toast.LENGTH_SHORT
                ).show()

                //resetALl()
            } else {
                Toast.makeText(
                    ctx,
                    "Svp insérer une carte sim puis réessayer !", Toast.LENGTH_SHORT
                ).show()
            }
        }

        val MY_PERMISSIONS_REQUEST_SEND_SMS = 1
        val ElectorJsonName = "electorat-sb.json"
        val CurrCodeScrut = "SO41"

        private fun checkForSmsPermission(ctx: Activity) {
            if (ActivityCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.SEND_SMS
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                // Permission not yet granted. Use requestPermissions().
                // MY_PERMISSIONS_REQUEST_SEND_SMS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
                ActivityCompat.requestPermissions(
                    ctx,
                    arrayOf<String>(Manifest.permission.SEND_SMS),
                    MY_PERMISSIONS_REQUEST_SEND_SMS
                )
            }
//        Toast.makeText(
//            requireActivity(),
//            "Permission accordée !", Toast.LENGTH_SHORT
//        ).show()
        }

        fun checkIfNetworkAvailaible(callback: (result: Boolean) -> Unit) {

            var networkFlag = true
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    networkFlag = NetworkUtils.isAvailable()
                } catch (ex: UnknownHostException) {
                    networkFlag = false
                    LogUtils.e("Internet error !")
                }

                if (networkFlag) {
                    callback(true)
                }else callback(false)
            }
        }
    }

}