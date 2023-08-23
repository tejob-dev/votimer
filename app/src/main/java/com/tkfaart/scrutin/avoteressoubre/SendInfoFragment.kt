package com.tkfaart.scrutin.avoteressoubre

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import cn.pedant.SweetAlert.SweetAlertDialog
import com.tkfaart.scrutin.avoteressoubre.databinding.FragmentSendInfoBinding
import com.tkfaart.scrutin.avoteressoubre.util.KeyChangeListener
import com.tkfaart.scrutin.avoteressoubre.util.PrefManager
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.*


/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class SendInfoFragment : Fragment(), View.OnClickListener {
    private var pos_lv: Int = 0
    private var pos_cartel: Int = 0
    private var pos_listel: Int = 0
    private var ag_recenser: EditText? = null
    private var keyListen: KeyChangeListener? = null
    private var selectedLvTitle: String? =null
    private var selectedElecto: String? = null
    //private var spinnerCartMilit: AppCompatSpinner? = null
    private lateinit var prf: PrefManager
    private var observation: EditText? = null
    private var residence: EditText? = null
    private var cart_elect: EditText? = null
    private var spin_list_elect: AppCompatSpinner? = null
    private var spinnerLv: AppCompatSpinner? = null
    private var profession: EditText? = null
    private var contact: EditText? = null
    private var date_naiss: EditText? = null
    private var nom: EditText? = null
    private var prenom: EditText? = null
    var phoneNumber = "+2250565362426"
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

    private var _binding: FragmentSendInfoBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSendInfoBinding.inflate(inflater, container, false)
        //_binding!!.scrollRoot.addOnLayoutChangeListener(this);
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visible = true

        prf = PrefManager(requireContext())

        nom = _binding?.nom
        prenom = _binding?.prenom
        date_naiss = _binding?.dateNaiss
        contact = _binding?.contact
        profession = _binding?.profession
        spin_list_elect = _binding?.spinnerListElect
        cart_elect = _binding?.cartElect
        residence = _binding?.residence
        observation = _binding?.observation

        ag_recenser = _binding?.agRecenser


        spinnerLv = _binding?.spinnerLv

        //spinnerCartMilit = _binding?.spinnerCartMilit

        if(arguments != null){

            nom?.setText(requireArguments().getString("nom").toString())
            prenom?.setText(requireArguments().getString("prenom").toString())
            date_naiss?.setText(requireArguments().getString("date").toString())
            contact?.setText(requireArguments().getString("contact").toString())
            profession?.setText(requireArguments().getString("profession").toString())
            cart_elect?.setText(requireArguments().getString("cartelect").toString())
            residence?.setText(requireArguments().getString("residence").toString())
            observation?.setText(requireArguments().getString("observation").toString())

            ag_recenser?.setText(requireArguments().getString("ag_recenser").toString())

        }

        keyListen = KeyChangeListener.create(activity)
        keyListen!!.setKeyboardListener { isShow, keyboardHeight ->
            Log.d("TAG", "isShow = [$isShow], keyboardHeight = [$keyboardHeight]")
            //Toast.makeText(getApplicationContext(),"keyboard is show "+isShow , Toast.LENGTH_LONG).show();
//            if (edMatricule.isFocused() === true && isShow == false) {
//                resetmargeOnBtn()
//            }

            if(isShow) {
                val params = _binding!!.bottomSpace.getLayoutParams()
                params.height = keyboardHeight+200
                _binding!!.bottomSpace.layoutParams = params
            }else{
                val params = _binding!!.bottomSpace.getLayoutParams()
                params.height = 1
                _binding!!.bottomSpace.layoutParams = params
            }

        }

        date_naiss?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val input = s.toString()
                if (!input.matches(Regex("[0-9a-zA-Z-/]+"))) {
                    s.delete(s.length - 1, s.length)
                }
            }
        })



        val list_electo = mutableListOf<String?>("Etes-vous sur la liste électorale?", "Oui", "Non")
        val arrayAdapterElecto = ArrayAdapter<String?>(this.requireContext(), R.layout.spinner_layout_list, R.id.textView, list_electo)
        arrayAdapterElecto.setDropDownViewResource(R.layout.simple_spinner_dropdown_list_item);
        spin_list_elect!!.adapter = arrayAdapterElecto


//        val list_cart_mili = mutableListOf<String?>("Avez-vous la carte militant?", "Oui", "Non", "Sympathisant")
//        val arrayAdapter = ArrayAdapter<String?>(this.requireContext(), R.layout.spinner_layout_list, R.id.textView, list_cart_mili)
//        arrayAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_list_item);
//        spinnerCartMilit!!.adapter = arrayAdapter

        keyList = ArrayList<String>()
        //keyList.add("Votre lieu de vote")
        valueList = ArrayList<String>()
        ///valueList.add("--first value--")

        if(loadJSONFromAsset()){
            println("prf = list of key")
            print(keyList.toString())
            val arrayAdapterLV = ArrayAdapter<String?>(this.requireContext(), R.layout.spinner_layout_list, R.id.textView,
                keyList as List<String?>
            )
            arrayAdapterLV.setDropDownViewResource(R.layout.simple_spinner_dropdown_list_item);
            spinnerLv!!.adapter = arrayAdapterLV
//            arrayAdapterLV.notifyDataSetChanged()
        }

        spinnerLv!!.prompt = "Votre lieu de vote"
        spinnerLv?.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View,
                position: Int,
                id: Long
            ) {
                println("Item selected "+position)
                if(isFLaunch){
                    isFLaunch = false
                }else {
                    selectedLv = valueList.get(position).toString();
                    selectedLvTitle = parentView?.getItemAtPosition(position).toString();
                }
                pos_lv = position
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // your code here
            }
        }

//        spinnerCartMilit!!.onItemSelectedListener = object : OnItemSelectedListener {
//            override fun onItemSelected(
//                parentView: AdapterView<*>?,
//                selectedItemView: View,
//                position: Int,
//                id: Long
//            ) {
//                println("Item selected "+position)
//                //val arr = arrayListOf<String>("Avez-vous la carte militant?","Oui", "Non", "Sympathisant")
//                selectedCartMilit = parentView?.getItemAtPosition(position).toString();
//                pos_cartel = position
//            }
//
//            override fun onNothingSelected(parentView: AdapterView<*>?) {
//                // your code here
//            }
//        }

        spin_list_elect!!.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View,
                position: Int,
                id: Long
            ) {
                println("Item selected "+position)
                //val arr = arrayListOf<String>("Avez-vous la carte militant?","Oui", "Non", "Sympathisant")
                selectedElecto = parentView?.getItemAtPosition(position).toString();
                pos_listel = position
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // your code here
            }
        }

        _binding?.btnConnex?.setOnClickListener(this)

        if(arguments != null){
            spinnerLv!!.setSelection(requireArguments().getInt("pos_lv"))
            spin_list_elect!!.setSelection(requireArguments().getInt("pos_listel"))
            //spinnerCartMilit!!.setSelection(requireArguments().getInt("pos_cartel"))
        }
    }

    private fun loadJSONFromAsset(): Boolean {
        try {
            // Read the JSON file from assets folder
            val inputStream: InputStream = requireActivity().assets.open(requireContext().getString(R.string.lv_name))
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val json = String(buffer)

            // Convert the JSON string to a JSON object
            val jsonObject = JSONObject(json)

            // Get the keys from the JSON object
            val keysIterator = jsonObject.keys()
            ///keyList = ArrayList<String>()
            while (keysIterator.hasNext()) {
                val key = keysIterator.next()
                keyList.add(key)
            }
            println("keyList Okay")
            println(keyList.toString())
            // Get the values from the JSON object
            //valueList = ArrayList<String>()
            for (i in 0 until keyList.size) {
                val key = keyList[i]
                val value = jsonObject.getString(key)
                valueList.add(value)
            }

            return true
            // Create an array of JSON objects with key-value pairs
//            val jsonArray = ArrayList<JSONObject>()
//            for (i in 0 until keyList.size) {
//                val key = keyList[i]
//                val value = valueList[i]
//                val jsonObj = JSONObject()
//                jsonObj.put(key, value)
//                jsonArray.add(jsonObj)
//            }

        } catch (e: JSONException) {
            e.printStackTrace()
            print("error Json "+e.message)
        } catch (e: IOException) {
            e.printStackTrace()
            print("error java "+e.message)
        }

        return false

    }


    override fun onResume() {
        super.onResume()
        //activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        //delayedHide(100)
    }

    override fun onPause() {
        super.onPause()
        //activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Clear the systemUiVisibility flag
        //activity?.window?.decorView?.systemUiVisibility = 0
        //show()
    }

    override fun onDestroy() {
        keyListen?.destroy()
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
        keyListen?.destroy()
        super.onDestroyView()
        _binding = null
    }

    private fun textEdIsPlent(item: EditText?): Boolean {
        return item?.text.toString().isNotEmpty()
    }

    override fun onClick(p0: View?) {
        var contactCheck = checkPhoneNumber(contact?.text.toString())
        if(textEdIsPlent(nom)
            && textEdIsPlent(prenom)
            && contactCheck.toString().isNotEmpty()
            && (textEdIsPlent(date_naiss) && (date_naiss?.text.toString().contains("-")  || date_naiss?.text.toString().contains("/")) )
            && textEdIsPlent(profession)
            && (selectedElecto.toString().isNotEmpty() && selectedElecto.toString().equals("Etes-vous sur la liste électorale?") == false)
            && textEdIsPlent(cart_elect)
            && textEdIsPlent(residence)
            && selectedLv.toString().isNotEmpty()

        ){
            val ag_tel = prf.getString("ag_tel")
            var message = "PAR:*"
            message += "null*"
            message += "$ag_tel*"
            message += nom?.text.toString()+"*"
            message += prenom?.text.toString()+"*"

            message += selectedElecto.toString()+"*"
            message += cart_elect?.text.toString()+"*"
            message += contactCheck.toString()+"*"
            message += date_naiss?.text.toString()+"*"
            message += selectedLv.toString()+"*"
            message += residence?.text.toString()+"*"
            message += profession?.text.toString()+"*"
            message += observation?.text.toString()+"*"
            message += ag_recenser?.text.toString()
            println("message sended to validator "+message)

            var bundle = Bundle()
            bundle.putString("nom", nom?.text.toString().trim())
            bundle.putString("prenom", prenom?.text.toString().trim())
            bundle.putString("date", date_naiss?.text.toString().trim())
            bundle.putString("date", date_naiss?.text.toString().trim())
            bundle.putString("contact", contactCheck?.toString()?.trim())
            bundle.putString("profession", profession?.text.toString().trim())
            bundle.putString("listelect", selectedElecto?.toString()?.trim())
            bundle.putString("cartelect", cart_elect?.text.toString().trim())
            bundle.putString("lieuvoteTl", selectedLvTitle?.toString()?.trim())
            bundle.putString("lieuvote", selectedLv?.toString()?.trim())
            bundle.putString("residence", residence?.text.toString().trim())

            bundle.putString("observation", observation?.text.toString().trim())
            bundle.putString("ag_recenser", ag_recenser?.text.toString().trim())

            bundle.putInt("pos_lv", pos_lv)
            bundle.putInt("pos_cartel", pos_cartel)
            bundle.putInt("pos_listel", pos_listel)

            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left,
                    R.anim.slide_in_from_left, R.anim.slide_out_to_right)
                .replace(R.id.fragment_container, ValidateFragment().apply {
                    this.arguments = bundle
                })
                .commit()

//            val encode = com.tkfaart.scrutin.avoteressoubre.util.Base64.getEncoder().encodeToString(message.toByteArray())
//            smsSendMessage(encode)
//            println("message sended enc "+encode)
//
//            SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
//                .setTitleText("Message")
//                .setContentText("Récensement éffectué avec succès!")
//                .setConfirmText("Continuer")
//                .setConfirmClickListener { sDialog -> (
//                    sDialog.dismissWithAnimation()
//                ) }.show()

        }else{
            SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Erreur")
                .setContentText("L'une des cases est vide!")
                .setConfirmText("Reprendre")
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                }.show()
        }
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

    fun smsSendMessage(message: String) {
        if (checkIfSimAvailaible() === true) {

            // Get the phone number
            val destinationAddress: String = phoneNumber
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
        ag_recenser?.setText("")
        //spinnerCartMilit?.setSelection(0)
        spinnerLv?.setSelection(0)

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

//    override fun onLayoutChange(
//        v: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int,
//        oldRight: Int, oldBottom: Int
//    ) {
//        val r = Rect()
//        v.getWindowVisibleDisplayFrame(r)
//        val screenHeight = v.rootView.height
//        val keyboardHeight: Int = screenHeight - r.bottom
//        if (keyboardHeight > screenHeight * 0.15) {
//            val difference: Int = keyboardHeight - (screenHeight - _binding!!.scrollRoot.getBottom())
//            if (difference > 0) {
//                val params = _binding!!.scrollRoot.getLayoutParams() as MarginLayoutParams
//                params.bottomMargin = difference
//                _binding!!.scrollRoot.setLayoutParams(params)
//            }
//        } else {
//            val params = _binding!!.scrollRoot.getLayoutParams() as MarginLayoutParams
//            params.bottomMargin = 0
//            _binding!!.scrollRoot.setLayoutParams(params)
//        }
//    }

}