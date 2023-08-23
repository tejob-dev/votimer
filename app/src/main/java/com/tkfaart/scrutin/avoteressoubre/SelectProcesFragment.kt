package com.tkfaart.scrutin.avoteressoubre

import android.R
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.UriUtils
import com.tkfaart.scrutin.avoteressoubre.apiservice.ApiClient
//import com.androidbuts.multispinnerfilter.KeyPairBoolData
import com.tkfaart.scrutin.avoteressoubre.databinding.FragmentPverbalBinding
import com.tkfaart.scrutin.avoteressoubre.models.CommonModel
import com.tkfaart.scrutin.avoteressoubre.models.ElectoratModel
import com.tkfaart.scrutin.avoteressoubre.models.ImagePvModel
import com.tkfaart.scrutin.avoteressoubre.util.Commons
import com.tkfaart.scrutin.avoteressoubre.util.Commons.Companion.loadJSONFromAsset
import com.tkfaart.scrutin.avoteressoubre.util.PrefManager
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class SelectProcesFragment : Fragment() {


    private val REQUEST_IMAGE_CAPTURE: Int = 10
    private var fileGlobal: File? = null
    private var photoPath: String = ""
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
    private var _binding: FragmentPverbalBinding? = null
    private var prf: PrefManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPverbalBinding.inflate(inflater, container, false)
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

        val pvList = prf!!.getArray("list_of_pv")
        if(pvList != null){
            val mutPvList = pvList.toMutableList()
            _binding!!.counterPvs.setText("${mutPvList.size}")
        }

        //val multiSelectionElectorat: MutableList<KeyPairBoolData> = mutableListOf()
//        val listOfSelElectorat = prf!!.getArray("liste_electorat")
//        val setToListElector = arrayListOf<String>()
//        listOfSelElectorat?.let {
//            setToListElector.addAll(it.toList())
//        }

        listNamesBV = mutableListOf<String>()
        val currentLV = prf!!.getString("user_lv")
        setUpBvSpinner(currentLV)
//        _binding!!.selectLieuVote.setTitle("Choix du lieu de vote")
//        _binding!!.selectBureauVote.setTitle("Choix du bureau de vote")
//        _binding!!.selectLieuVote.setPositiveButton("Fermer")
//        _binding!!.selectBureauVote.setPositiveButton("Fermer")
//        listNamesLV = mutableListOf<String>()
       // listNamesBV = mutableListOf<String>()
//        lieuvote.map {
//            listNamesLV!!.add("${it.libel}")
//        }
//
//        Log.d("LV ", listNamesLV.toString())
//
//        val personsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line,
//            listNamesLV!!
//        )
//        _binding!!.selectLieuVote.adapter = personsAdapter
//        _binding!!.selectLieuVote.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, l: Long) {
//
//                val lvModel = lieuvote[position]
//                Log.e(this.javaClass.simpleName, "Counter lv "+lvModel.toString())
//                currentLV = "${lvModel.id}"
//                currentLVName = "${lvModel.libel}"
//
//            }
//
//            override fun onNothingSelected(arg0: AdapterView<*>) {
//            }
//        }


        _binding?.btnNext?.setOnClickListener {
            sendImageToWeb()
        }

        _binding?.imageCharger?.setOnClickListener {
            dialogPickerPhoto()
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

    private fun sendImageToWeb() {

        if(currentLV.isNullOrBlank()
            && currentBV.isNullOrBlank()
            && photoPath.isNullOrBlank()
        ){
            SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Message")
                .setContentText("Un choix ou une sélection n'à pas été prise en compte!")
                .setConfirmText("Continuer")
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                }.show()
            return
        }

        Commons.checkIfNetworkAvailaible{
            if(it == false){

                val pvList = prf!!.getArray("list_of_pv")
                var mutPvList = mutableListOf<String>()
                if(pvList != null){
                    mutPvList = pvList.toMutableList()
                }
                mutPvList.add("${Commons.CurrCodeScrut}-${currentLV}*${currentBV}*${convertPathBase64(photoPath, 0)}")

                prf!!.setArray("list_of_pv", mutPvList.toSet())

                MainScope().launch {
                    SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Message")
                        .setContentText("Etant donné que le téléphone n'est pas connecté, l'envoie sera en attente!")
                        .setConfirmText("Continuer")
                        .setConfirmClickListener { sDialog ->
                            sDialog.dismissWithAnimation()
                            requireActivity().supportFragmentManager.beginTransaction()
                                .setCustomAnimations(
                                    com.tkfaart.scrutin.avoteressoubre.R.anim.slide_in_from_right, com.tkfaart.scrutin.avoteressoubre.R.anim.slide_out_to_left,
                                    com.tkfaart.scrutin.avoteressoubre.R.anim.slide_in_from_left, com.tkfaart.scrutin.avoteressoubre.R.anim.slide_out_to_right)
                                .replace(com.tkfaart.scrutin.avoteressoubre.R.id.fragment_container, OptionFragment())
                                .commit()

                        }.show()
                }
            }else{
                val imagePvModel = ImagePvModel(
                    lieuVoteId = "${Commons.CurrCodeScrut}-${currentLV}",
                    bureauVoteId = currentBV.toString(),
                    imageContent = convertPathBase64(photoPath, 0)
                )
                val reqManager = ApiClient.apiService.passImagePvOnline(imagePvModel)
                val responseOption = reqManager.execute()
                //var body = responseOption.body()
                if(responseOption.isSuccessful){
                    MainScope().launch {
                        SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Message")
                            .setContentText("Image transférée avec succès!")
                            .setConfirmText("Continuer")
                            .setConfirmClickListener { sDialog ->
                                sDialog.dismissWithAnimation()
                                requireActivity().supportFragmentManager.beginTransaction()
                                    .setCustomAnimations(
                                        com.tkfaart.scrutin.avoteressoubre.R.anim.slide_in_from_right, com.tkfaart.scrutin.avoteressoubre.R.anim.slide_out_to_left,
                                        com.tkfaart.scrutin.avoteressoubre.R.anim.slide_in_from_left, com.tkfaart.scrutin.avoteressoubre.R.anim.slide_out_to_right)
                                    .replace(com.tkfaart.scrutin.avoteressoubre.R.id.fragment_container, OptionFragment())
                                    .commit()
                            }.show()
                    }
                }else{
                    MainScope().launch {
                        SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Erreur")
                            .setContentText("Problème survenue, réprenez l'envoie ou vérifier la connection!")
                            .setConfirmText("Continuer")
                            .setConfirmClickListener { sDialog ->
                                sDialog.dismissWithAnimation()
                            }.show()
                    }
                }

            }
        }

    }

    fun convertPathBase64(filePath: String?, which: Int): String {
        if (filePath == null) return ""

        val imgFile = File(filePath)
        val options = BitmapFactory.Options()
        options.inSampleSize = 8
        val myBitmap = if (which == 3) BitmapFactory.decodeFile(imgFile.absolutePath) else BitmapFactory.decodeFile(imgFile.absolutePath, options)

        val byteArrayOutputStream = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun dialogPickerPhoto() {
        val dialogPicker = AlertDialog.Builder(requireContext())
            .setMessage("Source de la photo ?")
            .setPositiveButton("Avec la caméra") { dialog, _ ->
                dialog.dismiss()
                dispatchTakePictureIntent()
            }
            .setNegativeButton("Dans le téléphone") { dialog, _ ->
                dialog.dismiss()
                showFileChooser(11)
            }
            .create()

        dialogPicker.show()
    }

    private fun showFileChooser(pView: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Selectionnez une image"), pView)
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        var carnetFile: File? = null
        try {
            carnetFile = createImageFile()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        // Continue only if the File was successfully created
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            try {
                if (carnetFile != null) {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireActivity(),
                        requireContext().packageName+".fileprovider",
                        carnetFile
                    )

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE)  {
                testImageTaked(null)
            } else {
                testImageTaked(data?.data)
            }
        } else {
            SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Avertissement")
                .setContentText("Aucune image selectionnée")
                .setConfirmText("Continuer")
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                }.show()
        }
    }

    fun testImageTaked(bundleData: Uri?) {
        // Get the dimensions of the View
        try {
            val options = BitmapFactory.Options()
            options.inSampleSize = 8

            if (bundleData == null) {
                _binding!!.imageCharger.setImageBitmap(BitmapFactory.decodeFile(photoPath, options))
            } else {
                options.inJustDecodeBounds = true
                options.inPurgeable = true
                photoPath = UriUtils.uri2File(bundleData).path
                Log.e("Current Photo TAG", photoPath)
                _binding!!.imageCharger.setImageURI(bundleData)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        var imageFileName = ""

        imageFileName = "proces_verbal_" + timeStamp + "_"

        val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        photoPath = image.absolutePath
        return image
    }

    private fun createImageFileCompressed() {
        var compressedFile: File? = null
        try {
            fileGlobal = File(photoPath)

            CoroutineScope(Dispatchers.IO).launch {
                compressedFile = Compressor.compress(requireActivity(), fileGlobal!!) {
                    quality(75)
                    format(Bitmap.CompressFormat.JPEG)
                }
            }

            if ( com.blankj.utilcode.util.FileUtils.isFileExists(compressedFile)) {
                val finalCompressedFile = compressedFile
                com.blankj.utilcode.util.FileUtils.copy(compressedFile, fileGlobal) { srcFile, destFile ->
                    prf?.setString("current_encimg",
                        encodeFileToBase64Binary(finalCompressedFile)
                        )

                    true
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("","ERREUR -> enter here")
        }
    }

    fun encodeFileToBase64Binary(file: File?): String? {
        var encodedBase64: String? = null
        try {
            val fileInputStreamReader = FileInputStream(file)
            val bytes = ByteArray(file!!.length().toInt())
            fileInputStreamReader.read(bytes)
            encodedBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return encodedBase64
    }

//    private fun sendInResultMessage() {
//        var thereNotEmpty = false
//
//        if(thereNotEmpty){
//            showAlertItem()
//        }
//
//    }

//    private fun showAlertItem() {
//        var message = "${currentLVName} - ${currentBVName} | Votant: ${_binding!!.votant.text} | Bulletin Nul: ${_binding!!.bultNull.text} | Bulletin Blanc: ${_binding!!.bultBlanc.text} | "
//        message += "RICHARD KOFFI: ${_binding!!.votantA.text} | LASSINA TRAORE: ${_binding!!.votantB.text} | LIGNON ZIRIGNON: ${_binding!!.votantC.text} | PIENA COULIBALY: ${_binding!!.votantD.text} | CLEMENTINE GOBA: ${_binding!!.votantE.text}"
//        SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
//            .setTitleText("Vérification des saisis")
//            .setContentText("${message}")
//            .setConfirmText("Continuer")
//            .setCancelText("Reprendre")
//            .setConfirmClickListener { sDialog ->
//                sendMessageToSms()
//            }.setCancelClickListener { sDialog ->
//                sDialog.dismissWithAnimation()
//            }.show()
//    }

//    private fun sendMessageToSms() {
//        val saveCurrDate = Commons.getDateFormat()
//        val ag_tel = prf?.getString("ag_tel")
//        var message = "${Commons.CurrCodeScrut}*"+saveCurrDate+"*RES*${ag_tel}"
//        message += "*"
//        message += "${currentLV},${currentBV},${_binding!!.votant.text},${_binding!!.bultNull.text},${_binding!!.bultBlanc.text}*"
//        message += "indep:${_binding!!.votantA.text},rhdp:${_binding!!.votantB.text},indep2:${_binding!!.votantC.text},indep3:${_binding!!.votantD.text},pdci:${_binding!!.votantE.text}"
//        Commons.smsSendMessage(message, Commons.phoneNumber, requireActivity())
//        //println("message sended enc "+encode)
//
//        if(Commons.checkIfSimAvailaible(requireContext())){
//            SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
//                .setTitleText("Message")
//                .setContentText("Informations envoyées!")
//                .setConfirmText("Continuer")
//                .setConfirmClickListener { sDialog ->
//                    sDialog.dismissWithAnimation()
//                    goToRecreatePlace()
//                }.show()
//        }else{
//            SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
//                .setTitleText("Message")
//                .setContentText("Informations non envoyées!")
//                .setConfirmText("Continuer")
//                .setConfirmClickListener { sDialog ->
//                    sDialog.dismissWithAnimation()
//                }.show()
//        }
//    }

    private fun goToRecreatePlace() {
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                com.tkfaart.scrutin.avoteressoubre.R.anim.slide_in_from_right, com.tkfaart.scrutin.avoteressoubre.R.anim.slide_out_to_left,
                com.tkfaart.scrutin.avoteressoubre.R.anim.slide_in_from_left, com.tkfaart.scrutin.avoteressoubre.R.anim.slide_out_to_right)
            .replace(com.tkfaart.scrutin.avoteressoubre.R.id.fragment_container, SelectProcesFragment())
            .commit()
    }

//    private fun returnBackView(b: Boolean) {
//        if(b){
//            _binding.let {
//                it!!.lvContainer.visibility = View.VISIBLE
//                it!!.btnRetour.visibility = View.GONE
//                it!!.bvContainer.visibility = View.VISIBLE
//                it!!.bvInfoContainer.visibility = View.VISIBLE
//                it!!.btnNext.text = "SUIVANT"
//                it!!.candidContainer.visibility = View.GONE
//            }
//        }else{
//            _binding.let {
//                it!!.lvContainer.visibility = View.GONE
//                it!!.btnRetour.visibility = View.VISIBLE
//                it!!.bvContainer.visibility = View.GONE
//                it!!.bvInfoContainer.visibility = View.GONE
//                it!!.btnNext.text = "ENRÉGISTRER"
//                it!!.candidContainer.visibility = View.VISIBLE
//            }
//        }
//    }

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