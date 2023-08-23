package com.tkfaart.scrutin.avoteressoubre

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import cn.pedant.SweetAlert.SweetAlertDialog
import com.tkfaart.scrutin.avoteressoubre.databinding.FragmentRegisterBinding
import com.tkfaart.scrutin.avoteressoubre.util.KeyChangeListener
import com.tkfaart.scrutin.avoteressoubre.util.PrefManager


/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class RegisterFragment : Fragment() {
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

    private var _binding: FragmentRegisterBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visible = true
        prf = PrefManager(requireContext())

        val nom = _binding?.nom
        val prenom = _binding?.prenom
        val telephone = _binding?.telephone
        val section = _binding?.section
        val sousection = _binding?.souSection

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

        _binding?.btnConnex?.setOnClickListener(View.OnClickListener {
            //if(nom!!.text.isNotEmpty() && prenom!!.text.isNotEmpty() && telephone!!.text.isNotEmpty()){
            //    SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
            //        .setTitleText("Message")
            //        .setContentText("Enrégistrement éffectué avec succès!")
            //        .setConfirmText("Continuer")
            //        .setConfirmClickListener { sDialog ->
            //            sDialog.dismissWithAnimation()
            //            runLoginPage()
            //        }.show()
            //}else{
            //    SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
            //        .setTitleText("Erreur")
            //       .setContentText("L'une des cases est vide!")
            //        .setConfirmText("Reprendre")
            //        .setConfirmClickListener { sDialog ->
            //            sDialog.dismissWithAnimation()
            //         }.show()
            //}

            val mobile = prf!!.getString("ag_tel")
            if(mobile.isEmpty()){
                var telephone = telephone?.text
                var phone = checkPhoneNumber(telephone?.trim().toString())

                if (phone != null) {
                    prf!!.setString("ag_tel", phone)
                    prf!!.setString("ag_sect", section!!.text.toString())
                    prf!!.setString("ag_sou_sect", sousection!!.text.toString())

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
                var item = mobile

                if(item.equals(telephone, ignoreCase = false)){
                    requireActivity().supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left,
                            R.anim.slide_in_from_left, R.anim.slide_out_to_right)
                        .replace(R.id.fragment_container, SendInfoFragment())
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
        })

    }

    private fun runSendInfo(){
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left,
                R.anim.slide_in_from_left, R.anim.slide_out_to_right)
            .replace(R.id.fragment_container, SendInfoFragment())
            .commit()
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