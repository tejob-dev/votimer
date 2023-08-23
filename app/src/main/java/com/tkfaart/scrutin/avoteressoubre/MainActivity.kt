package com.tkfaart.scrutin.avoteressoubre

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.permissionx.guolindev.PermissionX
import com.tkfaart.scrutin.avoteressoubre.util.Commons
import com.tkfaart.scrutin.avoteressoubre.util.Commons.Companion.parseJsonToElectoratModel
import com.tkfaart.scrutin.avoteressoubre.util.PrefManager


class MainActivity : AppCompatActivity() {
    private var fabAlerte: FloatingActionButton? = null
    private var bottomSpace: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fabAlerte = findViewById<FloatingActionButton>(R.id.fabAlerte)

        val fragmentManager: FragmentManager = supportFragmentManager
        //val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val modelName = Build.MODEL
        val prf = PrefManager(this)

        fabAlerte!!.setOnClickListener{
            val ag_tel = prf.getString("ag_tel")
            val saveCurrDate = Commons.getDateFormat()
            var message = "${Commons.CurrCodeScrut}*"+saveCurrDate+"*ALT*${ag_tel}"

            Commons.smsSendMessage(message, Commons.phoneNumber, this)
            //println("message sended enc "+encode)

            if(Commons.checkIfSimAvailaible(this)){
                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Message")
                    .setContentText("L'Alerte a été envoyé!")
                    .setConfirmText("Continuer")
                    .setConfirmClickListener { sDialog ->
                        sDialog.dismissWithAnimation()
                    }.show()
            }
        }
        /// To unlock block for motorola if( modelName.contains("motorola", ignoreCase = true) ){
//        if(!modelName.contains("motorola", ignoreCase = true) ){
//            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
//                .setTitleText("Message")
//                .setContentText("L'Application n'est pas en production. Merci de nous contactez")
//                .setConfirmText("Continuez")
//                .setConfirmClickListener { sDialog ->
//                    sDialog.dismissWithAnimation()
//                    finish()
//                }.show()
//        }else{
            PermissionX.init(this@MainActivity)
                .permissions(arrayListOf(
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ))
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        //Toast.makeText(this@MainActivity, "Permissions accordées", Toast.LENGTH_LONG).show()
                        gotoNext()
                    } else {
                        //Toast.makeText(this@MainActivity, "Permissions réfusées", Toast.LENGTH_LONG).show()
                        gotoNext()
                    }
                }
        //}



    }

    fun MainActivity.setBottomSpace(bottomSpace: View){
        this.bottomSpace = bottomSpace
    }

    private fun gotoNext() {


        val prf = PrefManager(this@MainActivity)
        val mobile = prf.getString("ag_tel")
        if(mobile.isEmpty()){
            Handler().postDelayed(Runnable {
                run {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left,
                            R.anim.slide_in_from_left, R.anim.slide_out_to_right)
                        .replace(R.id.fragment_container, RegisterElectorFragment())
                        .commit()
                }
            }, 5000)
        }else{
//            val json = Commons.loadJSONFromAsset(Commons.ElectorJsonName, this)
//            if (json != null) {
//                var persons = parseJsonToElectoratModel(json)
//                // Now you have a list of Person objects
//                if(Commons.IS_RESULT){
//                    Handler().postDelayed(Runnable {
//                        run {
//                            fabAlerte?.visibility = View.VISIBLE
//                            supportFragmentManager.beginTransaction()
//                                .setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left,
//                                    R.anim.slide_in_from_left, R.anim.slide_out_to_right)
//                                .replace(R.id.fragment_container, SelectForResultFragment())
//                                .commit()
//                        }
//                    }, 5000)
//                }else{
//                    if(persons.size == 0){
//                        Handler().postDelayed(Runnable {
//                            run {
//                                fabAlerte?.visibility = View.VISIBLE
//                                supportFragmentManager.beginTransaction()
//                                    .setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left,
//                                        R.anim.slide_in_from_left, R.anim.slide_out_to_right)
//                                    .replace(R.id.fragment_container, ManageSWlFragment())
//                                    .commit()
//                            }
//                        }, 5000)
//                    }else{
//                        Handler().postDelayed(Runnable {
//                            run {
//                                fabAlerte?.visibility = View.VISIBLE
//                                supportFragmentManager.beginTransaction()
//                                    .setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left,
//                                        R.anim.slide_in_from_left, R.anim.slide_out_to_right)
//                                    .replace(R.id.fragment_container, ManageWlFragment())
//                                    .commit()
//                            }
//                        }, 5000)
//                    }
//                }
//
//            }else{
//                if(Commons.IS_RESULT){
//                    Handler().postDelayed(Runnable {
//                        run {
//                            fabAlerte?.visibility = View.VISIBLE
//                            supportFragmentManager.beginTransaction()
//                                .setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left,
//                                    R.anim.slide_in_from_left, R.anim.slide_out_to_right)
//                                .replace(R.id.fragment_container, SelectForResultFragment())
//                                .commit()
//                        }
//                    }, 5000)
//                }else{
//                    Handler().postDelayed(Runnable {
//                        run {
//                            fabAlerte?.visibility = View.VISIBLE
//                            supportFragmentManager.beginTransaction()
//                                .setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left,
//                                    R.anim.slide_in_from_left, R.anim.slide_out_to_right)
//                                .replace(R.id.fragment_container, ManageWlFragment())
//                                .commit()
//                        }
//                    }, 5000)
//                }
//            }

                Handler().postDelayed(Runnable {
                    run {
                        fabAlerte?.visibility = View.VISIBLE
                        supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left,
                                R.anim.slide_in_from_left, R.anim.slide_out_to_right)
                            .replace(R.id.fragment_container, OptionFragment())
                            .commit()
                    }
                }, 5000)

        }

    }

    fun setBottomSpace(bottomSpace: LinearLayout?) {
        this.bottomSpace = bottomSpace
    }


}