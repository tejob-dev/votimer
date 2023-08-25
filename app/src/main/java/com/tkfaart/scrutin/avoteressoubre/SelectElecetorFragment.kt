package com.tkfaart.scrutin.avoteressoubre

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
//import com.androidbuts.multispinnerfilter.KeyPairBoolData
import com.tkfaart.scrutin.avoteressoubre.adapters.ElectorAdapter
import com.tkfaart.scrutin.avoteressoubre.databinding.FragmentElectoratWlBinding
import com.tkfaart.scrutin.avoteressoubre.models.ElectoratModel
import com.tkfaart.scrutin.avoteressoubre.util.Commons.Companion.ElectorJsonName
import com.tkfaart.scrutin.avoteressoubre.util.Commons.Companion.loadJSONFromAsset
import com.tkfaart.scrutin.avoteressoubre.util.PrefManager
import org.json.JSONObject


class SelectElecetorFragment : Fragment() {


    private var firstOpenPass: Boolean = false
    private var zoneId: String? = null
    private var persons: List<ElectoratModel> = mutableListOf()
    private val electoratsSelected: MutableList<String> = mutableListOf()
    private val electoratIdSelected: MutableList<String> = mutableListOf()
    private var listElector: MutableList<ElectoratModel>? = null
    private var newListElector: MutableList<ElectoratModel>? = null
    private val keyList: MutableList<String> = mutableListOf()
    private var _binding: FragmentElectoratWlBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentElectoratWlBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var prf = PrefManager(requireContext())
        newListElector = mutableListOf<ElectoratModel>();

        //setElectoratModelist()

        zoneId = prf!!.getString("zoneId")
        val json = loadJSONFromAsset(ElectorJsonName, requireActivity())
        if (json != null) {
            persons = parseJsonToElectoratModel(json)
            // Now you have a list of Person objects
        }

        //val multiSelectionElectorat: MutableList<KeyPairBoolData> = mutableListOf()
        val listOfSelElectorat = prf.getArray("liste_electorat")
        val setToListElector = arrayListOf<String>()
        listOfSelElectorat?.let {
            setToListElector.addAll(it.toList())
        }

//        for (electorat in (persons ?: mutableListOf())) {
//            if(electorat.zone == zoneId.toInt()){
//                val electoratKeyPair = KeyPairBoolData()
//                electoratKeyPair.id = electorat.id!!.split("-").get(1).toLong()
//                electoratKeyPair.name = "${electorat.id!!.split("-")[1].toString()} - ${electorat.nomPrenoms}"
//                electoratKeyPair.isSelected = false //if(setToListElector.contains(electorat.id)) true else false
//                multiSelectionElectorat.add(electoratKeyPair)
//            }
//        }

        setRvList()

        _binding!!.selectMultiElecteur.setTitle("Choix récursive des électorats")
        _binding!!.selectMultiElecteur.setPositiveButton("Fermer")
        val listNames = mutableListOf<String>()
        persons.map {
            listNames.add("${it.id!!.split("-")[1].toString()} - ${it.nomPrenoms}")
        }
        Log.d("Electorat ", listNames.toString())
        newListElector!!.clear();
        val personsAdapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_dropdown_list_item, R.id.textView, listNames)
        _binding!!.selectMultiElecteur.adapter = personsAdapter
        _binding!!.selectMultiElecteur.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, l: Long) {

                val personsModel = persons[position]
                Log.e(this.javaClass.simpleName, "Counter electorat "+personsModel.toString())
                if(firstOpenPass){
                    if(newListElector!!.size<21 && newListElector!!.size >= 0){
                        newListElector!!.add(ElectoratModel(nomPrenoms =  personsModel.nomPrenoms, id = personsModel.id!!.split("-")[1].toString()))
                        _binding!!.listOfCurrentElector.adapter?.notifyDataSetChanged()
                        //Log.e("Error", "appened on setItem electorat "+newListElector.toString())
                    }else{
                        SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Message")
                            .setContentText("Trop de sélection (Limit: 20) ou la liste est vide")
                            .setConfirmText("Continuer")
                            .setConfirmClickListener { sDialog ->
                                sDialog.dismissWithAnimation()
                            }.show()
                    }
                }

                firstOpenPass = true

            }

            override fun onNothingSelected(arg0: AdapterView<*>) {
            }
        }

        _binding!!.goBack.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    com.tkfaart.scrutin.avoteressoubre.R.anim.slide_in_from_right, com.tkfaart.scrutin.avoteressoubre.R.anim.slide_out_to_left,
                    com.tkfaart.scrutin.avoteressoubre.R.anim.slide_in_from_left, com.tkfaart.scrutin.avoteressoubre.R.anim.slide_out_to_right)
                .replace(com.tkfaart.scrutin.avoteressoubre.R.id.fragment_container, ManageWlFragment())
                .commit()
        }

        _binding?.btnConnex?.setOnClickListener {
            if(newListElector!!.size == 0){
                SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("Aucune donnée sélectionner!")
                    .setConfirmText("Continuer")
                    .setConfirmClickListener { sDialog ->
                        sDialog.dismissWithAnimation()
                    }.show()
            }else{
                requireActivity().supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        com.tkfaart.scrutin.avoteressoubre.R.anim.slide_in_from_right, com.tkfaart.scrutin.avoteressoubre.R.anim.slide_out_to_left,
                        com.tkfaart.scrutin.avoteressoubre.R.anim.slide_in_from_left, com.tkfaart.scrutin.avoteressoubre.R.anim.slide_out_to_right)
                    .replace(com.tkfaart.scrutin.avoteressoubre.R.id.fragment_container, PreviewFragment().apply {
                        this.arguments = Bundle().apply {
                            putParcelableArrayList("list_elector", ArrayList(newListElector))
                        }
                    })
                    .commit()
            }
        }

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
                        //personObject.getString("zone").toInt(),
                    )
                    persons.add(person)
                //}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return persons
    }

    fun setRvList(){
        _binding!!.listOfCurrentElector.adapter = ElectorAdapter(newListElector)
        _binding!!.listOfCurrentElector.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

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