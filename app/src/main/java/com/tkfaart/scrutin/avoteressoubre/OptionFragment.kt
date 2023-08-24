package com.tkfaart.scrutin.avoteressoubre

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.LogUtils
import com.tkfaart.scrutin.avoteressoubre.apiservice.ApiClient
import com.tkfaart.scrutin.avoteressoubre.databinding.FragmentOptionBinding
import com.tkfaart.scrutin.avoteressoubre.models.ImagePvModel
import com.tkfaart.scrutin.avoteressoubre.util.Commons
import com.tkfaart.scrutin.avoteressoubre.util.PrefManager

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class OptionFragment : Fragment() {
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

    private var _binding: FragmentOptionBinding ? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentOptionBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visible = true
        prf = PrefManager(requireContext())

        if( prf!!.getString("user_lv").isNullOrBlank()){
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    com.tkfaart.scrutin.avoteressoubre.R.anim.slide_in_from_right, com.tkfaart.scrutin.avoteressoubre.R.anim.slide_out_to_left,
                    com.tkfaart.scrutin.avoteressoubre.R.anim.slide_in_from_left, com.tkfaart.scrutin.avoteressoubre.R.anim.slide_out_to_right)
                .replace(com.tkfaart.scrutin.avoteressoubre.R.id.fragment_container, RegisterElectorFragment())
                .commit()
        }

        _binding!!.btnSuivi.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_from_right, R.anim.slide_out_to_left,
                    R.anim.slide_in_from_left, R.anim.slide_out_to_right
                )
                .replace(R.id.fragment_container, SelectForSuiviFragment())
                .commit()
        }

        _binding!!.btnAvote.setOnClickListener{
            val json = Commons.loadJSONFromAsset(Commons.ElectorJsonName, requireActivity())
            if (json != null) {
                var persons = Commons.parseJsonToElectoratModel(json)
                if (persons.size == 0) {

                    requireActivity().supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_from_right, R.anim.slide_out_to_left,
                            R.anim.slide_in_from_left, R.anim.slide_out_to_right
                        )
                        .replace(R.id.fragment_container, ManageSWlFragment())
                        .commit()

                } else {

                    requireActivity().supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_from_right, R.anim.slide_out_to_left,
                            R.anim.slide_in_from_left, R.anim.slide_out_to_right
                        )
                        .replace(R.id.fragment_container, ManageWlFragment())
                        .commit()

                }
            }
        }

        _binding!!.btnPvs.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left,
                    R.anim.slide_in_from_left, R.anim.slide_out_to_right)
                .replace(R.id.fragment_container, SelectProcesFragment())
                .commit()
        }

        _binding!!.btnResultat.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left,
                    R.anim.slide_in_from_left, R.anim.slide_out_to_right)
                .replace(R.id.fragment_container, SelectForResultFragment())
                .commit()
        }

        if(!Commons.iS_RESP){
            _binding!!.btnAvote.visibility = VISIBLE
            _binding!!.btnResultat.visibility = GONE
            _binding!!.btnPvs.visibility = GONE
            _binding!!.btnSuivi.visibility = GONE
        }else{
            binding!!.btnAvote.visibility = GONE
            _binding!!.btnResultat.visibility = VISIBLE
            _binding!!.btnPvs.visibility = VISIBLE
            _binding!!.btnSuivi.visibility = VISIBLE
        }

        if(Commons.iS_RESP){
            if(prf!!.getString("zoneId").isNullOrBlank()){
                requireActivity().supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left,
                        R.anim.slide_in_from_left, R.anim.slide_out_to_right)
                    .replace(R.id.fragment_container, RegisterElectorFragment())
                    .commit()
            }
        }

        Commons.checkIfNetworkAvailaible{
            if(it){

                val pvList = prf!!.getArray("list_of_pv")
                var mutPvList = mutableListOf<String>()
                if(pvList != null){
                    mutPvList = pvList.toMutableList()
                }

                if (mutPvList.size > 0){

                    for (imagePv in (mutPvList)){
                        val listCont = imagePv.split("*")
                        sendRequestToWeb(listCont, imagePv)
                    }

                }
                //mutPvList.add("${Commons.CurrCodeScrut}-${currentLV}*${currentBV}*${convertPathBase64(photoPath, 0)}")
            }
        }

    }

    private fun sendRequestToWeb(listCont: List<String>, imagePv: String) {
        val imagePvModel = ImagePvModel(
            lieuVoteId = "${listCont[0]}",
            bureauVoteId = listCont[2],
            imageContent = listCont[3]
        )
        val reqManager = ApiClient.apiService.passImagePvOnline(imagePvModel)
        val responseOption = reqManager.execute()

        if(responseOption.isSuccessful){
            retrieveAndDelete(imagePv)
        }
    }

    private fun retrieveAndDelete(imagePv: String) {
        val pvList = prf!!.getArray("list_of_pv")
        var mutPvList = mutableListOf<String>()
        val newMutPvList = mutableListOf<String>()
        if(pvList != null){
            mutPvList = pvList.toMutableList()
        }

        for (imageMp in mutPvList){
            if(imageMp != imagePv){
                newMutPvList.add(imageMp)
            }
        }
        prf!!.setArray("list_of_pv", newMutPvList.toSet())
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
}