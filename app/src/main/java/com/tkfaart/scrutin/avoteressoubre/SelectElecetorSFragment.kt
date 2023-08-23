package com.tkfaart.scrutin.avoteressoubre

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import cn.pedant.SweetAlert.SweetAlertDialog
//import com.androidbuts.multispinnerfilter.KeyPairBoolData
import com.tkfaart.scrutin.avoteressoubre.databinding.FragmentElectoratSwlBinding
import com.tkfaart.scrutin.avoteressoubre.models.ElectoratModel
import com.tkfaart.scrutin.avoteressoubre.models.LieuVoteModel
import com.tkfaart.scrutin.avoteressoubre.util.Commons
import com.tkfaart.scrutin.avoteressoubre.util.Commons.Companion.loadJSONFromAsset
import com.tkfaart.scrutin.avoteressoubre.util.Commons.Companion.phoneNumber
import com.tkfaart.scrutin.avoteressoubre.util.Commons.Companion.smsSendMessage
import com.tkfaart.scrutin.avoteressoubre.util.PrefManager
import org.json.JSONObject


class SelectElecetorSFragment : Fragment() {


    private var selectedLVote: String = ""
    private var firstOpenPass: Boolean = false
    private var zoneId: String? = null
    private var listLV: List<LieuVoteModel> = mutableListOf()
    private val electoratsSelected: MutableList<String> = mutableListOf()
    private val electoratIdSelected: MutableList<String> = mutableListOf()
    private var listElector: MutableList<ElectoratModel>? = null
    private var newListElector: MutableList<ElectoratModel>? = null
    private val keyList: MutableList<String> = mutableListOf()
    private var _binding: FragmentElectoratSwlBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentElectoratSwlBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var prf = PrefManager(requireContext())
        newListElector = mutableListOf<ElectoratModel>();

        //setElectoratModelist()

        zoneId = prf!!.getString("zoneId")
        val json = loadJSONFromAsset("lieuvote-sb.json", requireActivity())
        if (json != null) {
            listLV = parseJsonToElectoratModel(json)
            // Now you have a list of Person objects
        }

        //val multiSelectionElectorat: MutableList<KeyPairBoolData> = mutableListOf()
        //val listOfSelElectorat = prf.getArray("liste_electorat")
        val setToListElector = arrayListOf<String>()
//        listOfSelElectorat?.let {
//            setToListElector.addAll(it.toList())
//        }

//        for (electorat in (persons ?: mutableListOf())) {
//            if(electorat.zone == zoneId.toInt()){
//                val electoratKeyPair = KeyPairBoolData()
//                electoratKeyPair.id = electorat.id!!.split("-").get(1).toLong()
//                electoratKeyPair.name = "${electorat.id!!.split("-")[1].toString()} - ${electorat.nomPrenoms}"
//                electoratKeyPair.isSelected = false //if(setToListElector.contains(electorat.id)) true else false
//                multiSelectionElectorat.add(electoratKeyPair)
//            }
//        }

        _binding!!.selectMultiElecteur.setTitle("Choix du Lieu de Vote")
        _binding!!.selectMultiElecteur.setPositiveButton("Fermer")
        val listNames = mutableListOf<String>()
        listLV.map {
            listNames.add("${it.libel}")
        }
        Log.d("Electorat ", listNames.toString())
        newListElector!!.clear();
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

        _binding?.btnConnex?.setOnClickListener {

            if(_binding!!.edNumPass.text.isEmpty()){
                SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("Le champ est vide!")
                    .setConfirmText("Continuer")
                    .setConfirmClickListener { sDialog ->
                        sDialog.dismissWithAnimation()
                    }.show()
            }else{
                val saveCurrDate = Commons.getDateFormat()
                val ag_tel = prf.getString("ag_tel")
                var message = "${Commons.CurrCodeScrut}*"+saveCurrDate+"*SWL*${ag_tel}"
                message += "*"
                message += "${selectedLVote}*${_binding!!.edNumPass.text}"
                smsSendMessage(message, phoneNumber, requireActivity())
                //println("message sended enc "+encode)

                if(Commons.checkIfSimAvailaible(requireContext())){
                    val newVal = (prf.getInt("num_vote_send"))+(_binding!!.edNumPass.text.toString()).toInt()
                    prf.setInt("num_vote_send", newVal)
                    SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Message")
                        .setContentText("Informations envoyées!")
                        .setConfirmText("Continuer")
                        .setConfirmClickListener { sDialog ->
                            sDialog.dismissWithAnimation()
                            goToManageWs()
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
        }

        _binding!!.goBack.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    com.tkfaart.scrutin.avoteressoubre.R.anim.slide_in_from_right, com.tkfaart.scrutin.avoteressoubre.R.anim.slide_out_to_left,
                    com.tkfaart.scrutin.avoteressoubre.R.anim.slide_in_from_left, com.tkfaart.scrutin.avoteressoubre.R.anim.slide_out_to_right)
                .replace(com.tkfaart.scrutin.avoteressoubre.R.id.fragment_container, ManageSWlFragment())
                .commit()
        }

    }

    private fun goToManageWs() {
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                com.tkfaart.scrutin.avoteressoubre.R.anim.slide_in_from_right, com.tkfaart.scrutin.avoteressoubre.R.anim.slide_out_to_left,
                com.tkfaart.scrutin.avoteressoubre.R.anim.slide_in_from_left, com.tkfaart.scrutin.avoteressoubre.R.anim.slide_out_to_right)
            .replace(com.tkfaart.scrutin.avoteressoubre.R.id.fragment_container, ManageSWlFragment())
            .commit()
    }

    fun parseJsonToElectoratModel(json: String): List<LieuVoteModel> {
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


//    fun parseJsonToRabatteurModel(json: String): List<RabatteurModel> {
//        val persons = mutableListOf<RabatteurModel>()
//        try {
//            val jsonObject = JSONObject(json)
//            val jsonArray = jsonObject.getJSONArray("rows")
//
//            for (i in 0 until jsonArray.length()) {
//                val personObject = jsonArray.getJSONObject(i)
//                val person = RabatteurModel(
//                    personObject.getString("subid"),
//                    personObject.getString("nom_prenoms"),
//                    personObject.getString("zone").toInt(),
//                )
//                persons.add(person)
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        return persons
//    }

}