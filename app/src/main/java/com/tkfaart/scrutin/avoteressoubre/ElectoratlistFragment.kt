package com.tkfaart.scrutin.avoteressoubre

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.tkfaart.scrutin.avoteressoubre.adapters.StringListAdapter
import com.tkfaart.scrutin.avoteressoubre.databinding.FragmentElectoratWlListBinding
import com.tkfaart.scrutin.avoteressoubre.models.ElectoratModel
import com.tkfaart.scrutin.avoteressoubre.util.Commons
import com.tkfaart.scrutin.avoteressoubre.util.PrefManager
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class ElectoratlistFragment : Fragment(), View.OnClickListener {
    private val listOfName: MutableList<String> = arrayListOf()
    private var persons: List<ElectoratModel> = arrayListOf()
    private var bundle: Bundle? = null
    private var selectedElecto: String? = null
    private var saveCurrDate: String? = null
    
    private var spinnerCartMilit: AppCompatSpinner? = null
    private lateinit var prf: PrefManager
    private var observation: EditText? = null
    private var residence: EditText? = null
    private var cart_elect: EditText? = null
    private var spin_list_elect: AppCompatSpinner? = null
    private var profession: EditText? = null
    private var contact: EditText? = null
    private var date_naiss: EditText? = null
    private var nom: EditText? = null
    private var prenom: EditText? = null
    var phoneNumber = "+2250565362426"
/**var phoneNumber2 = "+2250565361864"*/
    private val MY_PERMISSIONS_REQUEST_SEND_SMS = 1

    private var selectedCartMilit: String? = null
    private var selectedLv: String? = null
    private var isFLaunch = true
    private lateinit var valueList: ArrayList<String>
    private lateinit var keyList: ArrayList<String>
    private val hideHandler = Handler(Looper.myLooper()!!)

    @Suppress("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        val flags =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        activity?.window?.decorView?.systemUiVisibility = flags
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        fullscreenContentControls?.visibility = View.VISIBLE
    }
    private var visible: Boolean = false
    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val delayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    private var dummyButton: Button? = null
    private var fullscreenContent: View? = null
    private var fullscreenContentControls: View? = null

    private var _binding: FragmentElectoratWlListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentElectoratWlListBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visible = true

        if(arguments != null){
            bundle = arguments as Bundle
        }

        prf = PrefManager(requireContext())

        val listOfSelElectorat = prf.getArray("liste_electorat")
        var setToListElector = arrayListOf<String>()
        listOfSelElectorat?.let {
            setToListElector.addAll(it.toList())
        }

        val json = Commons.loadJSONFromAsset(Commons.ElectorJsonName, requireActivity())
        if (json != null) {
            persons = parseJsonToElectoratModel(json)
            // Now you have a list of Person objects
        }
        
        persons.map { 
            Log.d("Lsit view ", "${it.id}")
            Log.d("Lsit String ", "${setToListElector.toString()}")
            if(setToListElector.contains(it.id!!.split("-")[1])) {
                Log.d("Count Lsit view ", "${it.id}")
                listOfName.add("${it.id!!.split("-")[1]} - ${it.nomPrenoms.toString()}")
            }
        }

        if(listOfName.size == 0 ) _binding?.listTvEmpty!!.visibility = View.VISIBLE

        _binding!!.listOfSavedElector.adapter = StringListAdapter(listOfName)
        _binding!!.listOfSavedElector.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        _binding!!.listOfSavedElector.adapter?.notifyDataSetChanged()

        _binding?.goBack?.setOnClickListener(View.OnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left,
                    R.anim.slide_in_from_left, R.anim.slide_out_to_right)
                .replace(R.id.fragment_container, ManageWlFragment())
                .commit()
        })

       // saveCurrDate = getDateFormat()


    }

    fun parseJsonToElectoratModel(json: String): List<ElectoratModel> {
        val persons = mutableListOf<ElectoratModel>()
        try {
            val jsonObject = JSONObject(json)
            val jsonArray = jsonObject.getJSONArray("rows")

            for (i in 0 until jsonArray.length()) {
                val personObject = jsonArray.getJSONObject(i)
                val person = ElectoratModel(
                    personObject.getString("subid"),
                    personObject.getString("nom_prenoms"),
                    personObject.getString("zone").toInt(),
                )
                persons.add(person)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return persons
    }

    @SuppressLint("NewApi")
    private fun getDateFormat(): String? {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")
        val formattedDateTime = currentDateTime.format(formatter)
        
        return formattedDateTime
    }

    private fun runLoginPage() {
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left,
                R.anim.slide_in_from_left, R.anim.slide_out_to_right)
            .replace(R.id.fragment_container, SendInfoFragment())
            .commit()
    }


    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Clear the systemUiVisibility flag
        activity?.window?.decorView?.systemUiVisibility = 0
        show()
    }

    override fun onDestroy() {
        super.onDestroy()
        dummyButton = null
        fullscreenContent = null
        fullscreenContentControls = null
    }

    private fun toggle() {
        if (visible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        fullscreenContentControls?.visibility = View.GONE
        visible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    @Suppress("InlinedApi")
    private fun show() {
        // Show the system bar
        fullscreenContent?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        visible = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun textEdIsPlent(item: EditText?): Boolean {
        return item?.text.toString().isNotEmpty()
    }

    override fun onClick(p0: View?) {

        val ag_tel = prf.getString("ag_tel")
        var message = "CO04*"+saveCurrDate+"*PAR:*"
        message += "*"
        message += "$ag_tel*"
        message += bundle?.getString("nom")+"*"
        message += bundle?.getString("prenom")+"*"

        message += bundle?.getString("listelect")+"*"
        message += bundle?.getString("cartelect")+"*"
        message += bundle?.getString("contact")+"*"
        message += bundle?.getString("date")+"*"
        message += bundle?.getString("lieuvote")+"*"
        message += bundle?.getString("residence")+"*"
        message += bundle?.getString("profession")+""

        var message2 = "CO04*"+saveCurrDate+"*PAR:*"
        message2 += bundle?.getString("observation")+"*"
        message2 += bundle?.getString("ag_recenser")
        println("message sended "+message)
        //val encode = com.tkfaart.scrutin.avoteressoubre.util.Base64.getEncoder().encodeToString(message.toByteArray())
        if(message.length > 140){
            smsSendMessage(message, phoneNumber)
            smsSendMessage(message2, phoneNumber)

            //smsSendMessage(message, phoneNumber2)
            //smsSendMessage(message2, phoneNumber2)
        }else{
            var message3 = message
            message3 += "*"+bundle?.getString("observation")
            message3 += "*"+bundle?.getString("ag_recenser")
            if(message3.length <= 140){
                smsSendMessage(message3, phoneNumber)

                //smsSendMessage(message3, phoneNumber2)
            }else{
                smsSendMessage(message, phoneNumber)
                smsSendMessage(message2, phoneNumber)

                //smsSendMessage(message, phoneNumber2)
                //smsSendMessage(message2, phoneNumber2)
            }
        }
        //println("message sended enc "+encode)

        SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText("Message")
            .setContentText("Recensement effectué avec succès!")
            .setConfirmText("Continuer")
            .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                    runLoginPage()
            }.show()

    }

    fun checkPhoneNumber(phoneNumber: String): String? {
        // Remove leading plus sign or "225" from phone number
        val strippedPhoneNumber = phoneNumber.replace(Regex("^(\\+|225)"), "")
        // Check if phone number is less than 10 characters
        if (strippedPhoneNumber.length < 10) {
            return null // Phone number is too short
        }

        return strippedPhoneNumber
    }

    fun composeSmsMessage(message: String) {
        // Concatenate "smsto:" with phone number to create smsNumber.
        val smsNumber = "smsto:" + phoneNumber
        // Create the intent.
        val smsIntent = Intent(Intent.ACTION_SENDTO)
        // Set the data for the intent as the phone number.
        smsIntent.data = Uri.parse(smsNumber)
        // Add the message (sms) with the key ("sms_body").
        smsIntent.putExtra("sms_body",  message)
        // If package resolves (target app installed), send intent.
        if (smsIntent.resolveActivity(requireActivity().packageManager) != null) {
            requireActivity().startActivity(smsIntent)
        } else {
            Toast.makeText(
                requireActivity(), "Echec de l'envoi des informations!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_SEND_SMS -> {
                if (permissions[0].equals(Manifest.permission.SEND_SMS, ignoreCase = true)
                    && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission was granted.
                    Toast.makeText(
                        requireActivity(), "Permission accordée",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Permission denied.
                    Toast.makeText(
                        requireActivity(), "Message non envoyé",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun smsSendMessage(message: String, phonenumber: String) {
        if (checkIfSimAvailaible() === true) {

            // Get the phone number
            val destinationAddress: String = phonenumber
            // Get the text of the SMS message.
            val smsMessage: String = message
            // Check for permission first.
            checkForSmsPermission()
            // Use SmsManager.
            val smsManager: SmsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(
                destinationAddress, null, smsMessage,
                null, null
            )
            Toast.makeText(
                requireActivity(),
                "Recensement effectué avec succès !", Toast.LENGTH_SHORT
            ).show()

            resetALl()
        } else {
            Toast.makeText(
                requireActivity(),
                "Svp insérer une carte sim puis réessayer !", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun resetALl() {
        nom?.setText("")
        prenom?.setText("")
        date_naiss?.setText("")
        profession?.setText("")
        contact?.setText("")
        spin_list_elect?.setSelection(0)
        cart_elect?.setText("")
        residence?.setText("")
        observation?.setText("")


    }

    fun checkIfSimAvailaible(): Boolean {
        val telMgr = requireActivity().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
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

    private fun checkForSmsPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.SEND_SMS
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not yet granted. Use requestPermissions().
            // MY_PERMISSIONS_REQUEST_SEND_SMS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(Manifest.permission.SEND_SMS),
                MY_PERMISSIONS_REQUEST_SEND_SMS
            )
        }
//        Toast.makeText(
//            requireActivity(),
//            "Permission accordée !", Toast.LENGTH_SHORT
//        ).show()
    }

}