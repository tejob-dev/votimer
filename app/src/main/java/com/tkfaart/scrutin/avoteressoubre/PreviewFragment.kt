package com.tkfaart.scrutin.avoteressoubre

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.Fragment
import cn.pedant.SweetAlert.SweetAlertDialog
import com.tkfaart.scrutin.avoteressoubre.databinding.FragmentElectoratWlPrevBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.tkfaart.scrutin.avoteressoubre.adapters.StringListAdapter
import com.tkfaart.scrutin.avoteressoubre.models.ElectoratModel
import com.tkfaart.scrutin.avoteressoubre.util.Commons
import com.tkfaart.scrutin.avoteressoubre.util.Commons.Companion.phoneNumber
import com.tkfaart.scrutin.avoteressoubre.util.Commons.Companion.smsSendMessage
import com.tkfaart.scrutin.avoteressoubre.util.PrefManager
import java.util.*


/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class PreviewFragment : Fragment(), View.OnClickListener {
    private val listOfIds: MutableList<String> = mutableListOf()
    private var listArrayPrev: MutableList<ElectoratModel> = arrayListOf()
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

    private var _binding: FragmentElectoratWlPrevBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentElectoratWlPrevBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visible = true

        if(arguments != null){
            bundle = arguments as Bundle
            listArrayPrev = bundle!!.getParcelableArrayList<ElectoratModel>("list_elector")!!
        }

        prf = PrefManager(requireContext())
        val listOfName = mutableListOf<String>()
        listArrayPrev.map {
            listOfName.add("${it.id.toString()} - ${it.nomPrenoms.toString()}")
        }

        listArrayPrev.map {
            listOfIds.add(it.id.toString())
        }

        _binding!!.listOfPrevCurrentElector.adapter = StringListAdapter(listOfName)
        _binding!!.listOfPrevCurrentElector.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        _binding!!.listOfPrevCurrentElector.adapter?.notifyDataSetChanged()

        _binding?.btnConnex?.setOnClickListener(this)
        _binding?.goBack?.setOnClickListener(View.OnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left,
                    R.anim.slide_in_from_left, R.anim.slide_out_to_right)
                .replace(R.id.fragment_container, SelectElecetorFragment())
                .commit()
        })

        saveCurrDate = Commons.getDateFormat()

    }

    private fun runLoginPage() {
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left,
                R.anim.slide_in_from_left, R.anim.slide_out_to_right)
            .replace(R.id.fragment_container, ManageWlFragment())
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
        var message = "${Commons.CurrCodeScrut}*"+saveCurrDate+"*WL*${ag_tel}"
        message += "*"
        message += "${listOfIds.toString().replace("[", "").replace("]", "")}"
        if (Commons.checkIfSimAvailaible(requireContext()) === true) {

            val lSet = prf.getArray("liste_electorat")
            if(lSet!=null) {
                listOfIds.addAll(lSet.toList())
            }
            prf.setArray("liste_electorat",listOfIds.toSet())
        }

        smsSendMessage(message, phoneNumber, requireActivity())
        //println("message sended enc "+encode)

        if(Commons.checkIfSimAvailaible(requireContext())){
            SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Message")
                .setContentText("Informations envoyées!")
                .setConfirmText("Continuer")
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                    runLoginPage()
                }.show()
        }else{
            SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Message")
                .setContentText("Informations non envoyées!")
                .setConfirmText("Continuer")
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



}