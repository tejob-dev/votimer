package com.tkfaart.scrutin.avoteressoubre

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import cn.pedant.SweetAlert.SweetAlertDialog
import com.tkfaart.scrutin.avoteressoubre.databinding.FragmentAvoteWlRegisterBinding
import com.tkfaart.scrutin.avoteressoubre.models.ElectoratModel
import com.tkfaart.scrutin.avoteressoubre.models.LieuVoteModel
import com.tkfaart.scrutin.avoteressoubre.util.Commons
import com.tkfaart.scrutin.avoteressoubre.util.KeyChangeListener
import com.tkfaart.scrutin.avoteressoubre.util.PrefManager
import org.json.JSONObject


/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class RegisterElectorFragment : Fragment() {
    private var selectedLVote: String = ""
    private var listLV: MutableList<LieuVoteModel> = arrayListOf()
    private var maxZone: Int = 0
    private var keyListen: KeyChangeListener? = null
    private var prf: PrefManager? = null
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

    private var _binding: FragmentAvoteWlRegisterBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAvoteWlRegisterBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visible = true
        prf = PrefManager(requireContext())

        val nom = _binding?.nom
        val prenom = _binding?.prenom
        val telephone = _binding?.telephone
        val zoneText = _binding?.edZone

        _binding?.btnRegister?.setOnClickListener{
            Log.d(this.javaClass.toString(), "text content ${zoneText!!.text}")
            if(Commons.iS_RESP){
                if(zoneText!!.text.isNotEmpty()) {
                    prf!!.setString("zoneId", zoneText.text.toString())
                    checkMobile(telephone)
                }else{
                    SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Erreur")
                        .setContentText("Le champ de zone à renseignée est vide")
                        .setConfirmText("Reprendre")
                        .setConfirmClickListener { sDialog ->
                            sDialog.dismissWithAnimation()
                        }.show()
                }
            }else{
                if(selectedLVote.isNotEmpty()) {
                    prf!!.setString("user_lv", selectedLVote)
                    checkMobile(telephone)
                }else{
                    SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Erreur")
                        .setContentText("Il n'y a pas eu de selection de Lieu de Vote!")
                        .setConfirmText("Reprendre")
                        .setConfirmClickListener { sDialog ->
                            sDialog.dismissWithAnimation()
                        }.show()
                }
            }
        }

        val json = Commons.loadJSONFromAsset("lieuvote-sb.json", requireActivity())
        if (json != null) {
            listLV = parseJsonToLVModel(json)
            // Now you have a list of Person objects
        }
        _binding!!.selectMultiElecteur.setTitle("Choix du Lieu de Vote")
        _binding!!.selectMultiElecteur.setPositiveButton("Fermer")
        val listNames = mutableListOf<String>()
        listLV?.map {
            listNames.add("${it.libel}")
        }
        
        val personsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listNames)
        _binding!!.selectMultiElecteur.adapter = personsAdapter
        _binding!!.selectMultiElecteur.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, l: Long) {

                val lvModel = listLV[position]
                Log.e(this.javaClass.simpleName, "Counter lv "+lvModel.toString())
                selectedLVote = "${lvModel.id}"

            }

            override fun onNothingSelected(arg0: AdapterView<*>) {
            }
        }

        if(Commons.iS_RESP){
            //_binding!!.selectMultiElecteur.visibility = View.GONE
            //_binding!!.edZone.visibility = View.VISIBLE
        }else{
            _binding!!.selectMultiElecteur.visibility = View.VISIBLE
            _binding!!.edZone.visibility = View.GONE
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


    }

    private fun parseJsonToLVModel(json: String): MutableList<LieuVoteModel> {
        val lieuv = mutableListOf<LieuVoteModel>()
        try {
            val jsonObject = JSONObject(json)
            val jsonArray = jsonObject.getJSONArray("rows")

            for (i in 0 until jsonArray.length()) {
                val personObject = jsonArray.getJSONObject(i)
                val person = LieuVoteModel(
                    personObject.getString("id"),
                    personObject.getString("libel"),
                )
                lieuv.add(person)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lieuv
    }

    private fun isCorrectZone(text: Editable): Boolean {
        val json = Commons.loadJSONFromAsset(Commons.ElectorJsonName, requireActivity())
        if(json!!.isNotEmpty()) parseJsonToElectoratModel(json.toString())
        if(text.toString().isNotEmpty()){
            if(text.toString().toInt() <= maxZone) return true
        }
        Log.d(this.javaClass.toString(), "not correct ${maxZone}")
        return false
    }

    fun parseJsonToElectoratModel(json: String): List<ElectoratModel> {
        val persons = mutableListOf<ElectoratModel>()
        val jsonObject = JSONObject(json)
        val jsonArray = jsonObject.getJSONArray("rows")
        try {

            for (i in 0 until jsonArray.length()) {
                val personObject = jsonArray.getJSONObject(i)
                var currentZone = 0
                personObject.getString("zone")?.let { 
                    currentZone = it.toInt()
                }
                if(maxZone <=  currentZone) maxZone = currentZone
                Log.d("Log maxZone ", "${maxZone}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return persons
    }

    private fun checkMobile(telephone: EditText?) {
        val mobile = prf!!.getString("ag_tel")
        if(mobile.isNullOrBlank()){
            var telephonePh = telephone?.text
            var phone = checkPhoneNumber(telephonePh?.trim().toString())

            if (phone != null) {
                prf!!.setString("ag_tel", phone)
                SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Message")
                    .setContentText("Votre enregistrement est effectué avec succès!")
                    .setConfirmText("Suivant")
                    .setConfirmClickListener { sDialog ->
                        sDialog.dismissWithAnimation()
                        runLoginPage()
                    }.show()
            } else {
                SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Erreur")
                    .setContentText("Le numéro de téléphone n'est pas valide.\nVoici un exemple: 0708070809")
                    .setConfirmText("Reprendre")
                    .setConfirmClickListener { sDialog ->
                        sDialog.dismissWithAnimation()
                    }.show()
            }

        }else{

            var telephone = checkPhoneNumber(telephone?.text?.trim().toString())
            //var item = mobile

            if(mobile.equals(telephone, ignoreCase = false)){
                requireActivity().supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left,
                        R.anim.slide_in_from_left, R.anim.slide_out_to_right)
                    .replace(R.id.fragment_container, OptionFragment())
                    .commit()
            }else{
                SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Message")
                    .setContentText("Un compte est déjà connecté, passe à la page de connexion!")
                    .setConfirmText("Continuer")
                    .setConfirmClickListener { sDialog ->
                        sDialog.dismissWithAnimation()
                        runLoginPage()
                    }.show()
            }

        }
    }

    fun checkPhoneNumber(phoneNumber: String): String? {
        // Remove leading plus sign or "225" from phone number
        val strippedPhoneNumber = phoneNumber.replace(Regex("^(\\+|225)"), "")
        // Check if phone number is less than 10 characters
        if (strippedPhoneNumber.length < 10) {
            return null // Phone number is too short
        }
        println("phone num "+strippedPhoneNumber)
        return strippedPhoneNumber
    }

    private fun runLoginPage() {
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left,
                R.anim.slide_in_from_left, R.anim.slide_out_to_right)
            .replace(R.id.fragment_container, LoginFragment())
            .commit()
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
}