package com.tkfaart.scrutin.avoteressoubre.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tkfaart.scrutin.avoteressoubre.databinding.EllectorListItemBinding
import com.tkfaart.scrutin.avoteressoubre.models.ElectoratModel

class ElectorAdapter(private var listElector: MutableList<ElectoratModel>?) : RecyclerView.Adapter<ElectorAdapter.ElectorHolder>() {


    companion object {
        const val TAG = "ElectorAdapter::class"
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElectorHolder {
        return ElectorHolder(EllectorListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    class ElectorHolder(private val binding: EllectorListItemBinding): RecyclerView.ViewHolder(binding.root)  {
        val namedTv = binding.labelTitle
    }

    override fun onBindViewHolder(holder: ElectorHolder, position: Int) {
        val electo = listElector!![position]

        holder.namedTv.text = "${electo.id} - ${electo.nomPrenoms}"

        holder.namedTv.setOnClickListener {
            Log.d("item : ", electo.toString())
        }
    }


    fun getElectorSelected(): MutableList<ElectoratModel>? = listElector


    override fun getItemCount() = listElector?.size ?: 0

}