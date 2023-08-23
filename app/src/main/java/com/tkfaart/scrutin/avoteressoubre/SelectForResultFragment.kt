package com.tkfaart.scrutin.avoteressoubre

import android.R
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
import com.tkfaart.scrutin.avoteressoubre.databinding.FragmentVotantSenderBinding
import com.tkfaart.scrutin.avoteressoubre.models.CommonModel
import com.tkfaart.scrutin.avoteressoubre.models.ElectoratModel
import com.tkfaart.scrutin.avoteressoubre.util.Commons
import com.tkfaart.scrutin.avoteressoubre.util.Commons.Companion.loadJSONFromAsset
import com.tkfaart.scrutin.avoteressoubre.util.PrefManager
import org.json.JSONObject


class SelectForResultFragment : Fragment() {


    private var currentLVName: String = ""
    private var currentBVName: String = ""
    private var listNamesLV : MutableList<String>?  = null
    private var listNamesBV: MutableList<String>? = null
    private var currentLV: String = "0"
    private var currentBV: String = "0"
    private var firstOpenPass: Boolean = false
    private var zoneId: String? = null
    private var lieuvote: List<CommonModel> = mutableListOf()
    private var bureauList: List<CommonModel> = mutableListOf()
    private val electoratsSelected: MutableList<String> = mutableListOf()
    private val electoratIdSelected: MutableList<String> = mutableListOf()
    private var listElector: MutableList<ElectoratModel>? = null
    private var newListElector: MutableList<ElectoratModel>? = null
    private val keyList: MutableList<String> = mutableListOf()
    private var _binding: FragmentVotantSenderBinding? = null
    private var prf: PrefManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentVotantSenderBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prf = PrefManager(requireContext())
        newListElector = mutableListOf<ElectoratModel>();

        //setElectoratModelist()

        zoneId = prf!!.getString("zoneId")
        val json = loadJSONFromAsset("lieuvote-sb.json", requireActivity())
        if (json != null) {
            lieuvote = parseJsonToLVModel(json)
            // Now you have a list of Person objects
        }
        val jsonBv = loadJSONFromAsset("bureauvote-sb.json", requireActivity())
        if (jsonBv != null) {
            bureauList = parseJsonToBVModel(jsonBv)
            // Now you have a list of Person objects
        }

        //val multiSelectionElectorat: MutableList<KeyPairBoolData> = mutableListOf()
//        val listOfSelElectorat = prf!!.getArray("liste_electorat")
//        val setToListElector = arrayListOf<String>()
//        listOfSelElectorat?.let {
//            setToListElector.addAll(it.toList())
//        }

        _binding!!.selectLieuVote.setTitle("Choix du lieu de vote")
        _binding!!.selectBureauVote.setTitle("Choix du bureau de vote")
        _binding!!.selectLieuVote.setPositiveButton("Fermer")
        _binding!!.selectBureauVote.setPositiveButton("Fermer")
        listNamesLV = mutableListOf<String>()
        listNamesBV = mutableListOf<String>()
        lieuvote.map {
            listNamesLV!!.add("${it.libel}")
        }

        Log.d("LV ", listNamesLV.toString())

        val personsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line,
            listNamesLV!!
        )
        _binding!!.selectLieuVote.adapter = personsAdapter
        _binding!!.selectLieuVote.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, l: Long) {

                val lvModel = lieuvote[position]
                Log.e(this.javaClass.simpleName, "Counter lv "+lvModel.toString())
                currentLV = "${lvModel.id}"
                currentLVName = "${lvModel.libel}"

                setUpBvSpinner(currentLV)
            }

            override fun onNothingSelected(arg0: AdapterView<*>) {
            }
        }

//        _binding!!.goBack.setOnClickListener {
//            requireActivity().supportFragmentManager.beginTransaction()
//                .setCustomAnimations(
//                    com.tkfaart.scrutin.avoteressoubre.R.anim.slide_in_from_right, com.tkfaart.scrutin.avoteressoubre.R.anim.slide_out_to_left,
//                    com.tkfaart.scrutin.avoteressoubre.R.anim.slide_in_from_left, com.tkfaart.scrutin.avoteressoubre.R.anim.slide_out_to_right)
//                .replace(com.tkfaart.scrutin.avoteressoubre.R.id.fragment_container, ManageWlFragment())
//                .commit()
//        }*

        _binding!!.votant.setText("")
        _binding!!.bultBlanc.setText("")
        _binding!!.bultNull.setText("")

        _binding?.btnNext?.setOnClickListener {
            if(_binding?.btnNext?.text.toString().contains("suivant", ignoreCase = true)){
                returnBackView(false)
            }else{
                sendInResultMessage()
            }
        }
        _binding?.btnRetour?.setOnClickListener {
            returnBackView(true)
        }
        _binding!!.goBack.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    com.tkfaart.scrutin.avoteressoubre.R.anim.slide_in_from_right, com.tkfaart.scrutin.avoteressoubre.R.anim.slide_out_to_left,
                    com.tkfaart.scrutin.avoteressoubre.R.anim.slide_in_from_left, com.tkfaart.scrutin.avoteressoubre.R.anim.slide_out_to_right)
                .replace(com.tkfaart.scrutin.avoteressoubre.R.id.fragment_container, OptionFragment())
                .commit()
        }

    }

    private fun sendInResultMessage() {
        var thereNotEmpty = false

        if(currentLVName.toString() != ""
            && currentBVName.toString() != ""
            && !_binding!!.votant.text.isNullOrBlank()
            && !_binding!!.bultBlanc.text.isNullOrBlank()
            && !_binding!!.bultNull.text.isNullOrBlank()
            ){
            thereNotEmpty = !thereNotEmpty;
        }else{
            SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Message")
                .setContentText("Un champ ou un choix dans le premier formulaire est vide!")
                .setConfirmText("Continuer")
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                }.show()
        }

        if(!_binding!!.votantA.text.isNullOrBlank()
            && !_binding!!.votantB.text.isNullOrBlank()
            && !_binding!!.votantC.text.isNullOrBlank()
            && !_binding!!.votantD.text.isNullOrBlank()
            && !_binding!!.votantE.text.isNullOrBlank()
        ){
            thereNotEmpty = !thereNotEmpty;
        }else{
            SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Message")
                .setContentText("Un champ dans le second formulaire est vide!")
                .setConfirmText("Continuer")
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                }.show()
        }

        if(thereNotEmpty){
            showAlertItem()
        }

    }

    private fun showAlertItem() {
        var message = "${currentLVName} - ${currentBVName} | Votant: ${_binding!!.votant.text} | Bulletin Nul: ${_binding!!.bultNull.text} | Bulletin Blanc: ${_binding!!.bultBlanc.text} | "
        message += "RICHARD KOFFI: ${_binding!!.votantA.text} | LASSINA TRAORE: ${_binding!!.votantB.text} | LIGNON ZIRIGNON: ${_binding!!.votantC.text} | PIENA COULIBALY: ${_binding!!.votantD.text} | CLEMENTINE GOBA: ${_binding!!.votantE.text}"
        SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Vérification des saisis")
            .setContentText("${message}")
            .setConfirmText("Continuer")
            .setCancelText("Reprendre")
            .setConfirmClickListener { sDialog ->
                sendMessageToSms()
            }.setCancelClickListener { sDialog ->
                sDialog.dismissWithAnimation()
            }.show()
    }

    private fun sendMessageToSms() {
        val saveCurrDate = Commons.getDateFormat()
        val ag_tel = prf?.getString("ag_tel")
        var message = "${Commons.CurrCodeScrut}*"+saveCurrDate+"*RES*${ag_tel}"
        message += "*"
        message += "${currentLV},${currentBV},${_binding!!.votant.text},${_binding!!.bultNull.text},${_binding!!.bultBlanc.text}*"
        message += "indep:${_binding!!.votantA.text},rhdp:${_binding!!.votantB.text},indep2:${_binding!!.votantC.text},indep3:${_binding!!.votantD.text},pdci:${_binding!!.votantE.text}"
        Commons.smsSendMessage(message, Commons.phoneNumber, requireActivity())
        //println("message sended enc "+encode)

        if(Commons.checkIfSimAvailaible(requireContext())){
            SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Message")
                .setContentText("Informations envoyées!")
                .setConfirmText("Continuer")
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                    goToRecreatePlace()
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

    private fun goToRecreatePlace() {
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                com.tkfaart.scrutin.avoteressoubre.R.anim.slide_in_from_right, com.tkfaart.scrutin.avoteressoubre.R.anim.slide_out_to_left,
                com.tkfaart.scrutin.avoteressoubre.R.anim.slide_in_from_left, com.tkfaart.scrutin.avoteressoubre.R.anim.slide_out_to_right)
            .replace(com.tkfaart.scrutin.avoteressoubre.R.id.fragment_container, SelectForResultFragment())
            .commit()
    }

    private fun returnBackView(b: Boolean) {
        if(b){
            _binding.let {
                it!!.lvContainer.visibility = View.VISIBLE
                it!!.btnRetour.visibility = View.GONE
                it!!.bvContainer.visibility = View.VISIBLE
                it!!.bvInfoContainer.visibility = View.VISIBLE
                it!!.btnNext.text = "SUIVANT"
                it!!.candidContainer.visibility = View.GONE
            }
        }else{
            _binding.let {
                it!!.lvContainer.visibility = View.GONE
                it!!.btnRetour.visibility = View.VISIBLE
                it!!.bvContainer.visibility = View.GONE
                it!!.bvInfoContainer.visibility = View.GONE
                it!!.btnNext.text = "ENRÉGISTRER"
                it!!.candidContainer.visibility = View.VISIBLE
            }
        }
    }

    private fun setUpBvSpinner(currentLV: String) {
        listNamesBV!!.clear()
        val newBvList = arrayListOf<CommonModel>()
        bureauList.map {
            if(it.otherId!!.toInt() == currentLV.toInt()) {
                listNamesBV!!.add("${it.libel}")
                newBvList.add(CommonModel(id= it.id, libel = it.libel,))
            }
        }

        val personsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line,
            listNamesBV!!
        )
        _binding!!.selectBureauVote.adapter = personsAdapter
        _binding!!.selectBureauVote.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, l: Long) {

                if(firstOpenPass){
                    val BvModel = newBvList[position]
                    Log.e(this.javaClass.simpleName, "Counter bv "+BvModel.toString())
                    currentBV = "${BvModel.id}"
                    currentBVName = "${BvModel.libel}"
                }
                firstOpenPass = true
            }

            override fun onNothingSelected(arg0: AdapterView<*>) {
            }
        }
    }

    fun parseJsonToLVModel(json: String): List<CommonModel> {
        val persons = mutableListOf<CommonModel>()
        try {
            val jsonObject = JSONObject(json)
            val jsonArray = jsonObject.getJSONArray("rows")

            for (i in 0 until jsonArray.length()) {
                val personObject = jsonArray.getJSONObject(i)
                if(personObject.getString("zoneid").toInt() == zoneId!!.toInt()) {
                    val person = CommonModel(
                        personObject.getString("id"),
                        personObject.getString("libel"),
                    )
                    persons.add(person)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return persons
    }

    fun parseJsonToBVModel(json: String): List<CommonModel> {
        val persons = mutableListOf<CommonModel>()
        try {
            val jsonObject = JSONObject(json)
            val jsonArray = jsonObject.getJSONArray("rows")

            for (i in 0 until jsonArray.length()) {
                val personObject = jsonArray.getJSONObject(i)
                //if(personObject.getString("zoneid").toInt() == zoneId!!.toInt()) {
                val person = CommonModel(
                    personObject.getString("id"),
                    personObject.getString("libel"),
                    personObject.getString("lieu_vote_id"),
                )
                persons.add(person)
                //}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return persons
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