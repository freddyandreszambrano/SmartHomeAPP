package com.modelomatematico.smarthome.features.lights.view.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.modelomatematico.smarthome.R
import com.modelomatematico.smarthome.features.lights.data.model.LightCardModel
import com.modelomatematico.smarthome.features.lights.view.ui.viewholder.LightsCardViewHolder

class LightsCardAdapter(
    private var lightCards: MutableList<LightCardModel>,
    private val onItemClick: (LightCardModel, Int) -> Unit,
    private val onSwitchToggle: (LightCardModel, Int, Boolean) -> Unit
) : RecyclerView.Adapter<LightsCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LightsCardViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return LightsCardViewHolder(
            layoutInflater.inflate(
                R.layout.item_lights_card,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: LightsCardViewHolder, position: Int) {
        val lightCard = lightCards[position]
        holder.render(lightCard, position)

        holder.itemView.setOnClickListener {
            onItemClick(lightCard, position)
        }

        holder.setSwitchToggleListener { isOn ->
            lightCards[position] = lightCard.copy(isOn = isOn)
            onSwitchToggle(lightCard, position, isOn)
        }
    }

    override fun getItemCount(): Int = lightCards.size

    fun updateCard(position: Int, isOn: Boolean) {
        if (position in 0 until lightCards.size) {
            lightCards[position] = lightCards[position].copy(isOn = isOn)
            notifyItemChanged(position)
        }
    }
}