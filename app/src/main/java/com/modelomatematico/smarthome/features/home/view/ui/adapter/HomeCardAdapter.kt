package com.modelomatematico.smarthome.features.home.view.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.modelomatematico.smarthome.R
import com.modelomatematico.smarthome.databinding.ItemHomeCardBinding

class HomeCardAdapter(
    private val cardTitles: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<HomeCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeCardViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return HomeCardViewHolder(layoutInflater.inflate(R.layout.item_home_card, parent, false))
    }

    override fun onBindViewHolder(holder: HomeCardViewHolder, position: Int) {
        val cardTitle = cardTitles[position]
        holder.render(cardTitle, position)
        holder.itemView.setOnClickListener {
            onItemClick(cardTitle)
        }
    }

    override fun getItemCount(): Int = cardTitles.size
}

class HomeCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemHomeCardBinding.bind(view)
    private val cardIconMap: Map<String, Int>
    private val actions: Array<String>
    private val backgrounds: Array<String>
    private val iconTints: Array<String>

    init {
        val context = view.context
        val titlesArray = context.resources.getStringArray(R.array.home_card_titles)
        actions = context.resources.getStringArray(R.array.home_card_actions)
        backgrounds = context.resources.getStringArray(R.array.home_card_backgrounds)
        iconTints = context.resources.getStringArray(R.array.home_card_icon_tints)

        cardIconMap = mapOf(
            titlesArray[0].uppercase() to R.drawable.ic_lightbulb_solid,
            titlesArray[1].uppercase() to R.drawable.ic_fan_solid,
            titlesArray[2].uppercase() to R.drawable.ic_rocket_solid,
            titlesArray[3].uppercase() to R.drawable.ic_rocket_solid,
        )
    }

    fun render(cardTitle: String, position: Int) {
        binding.tvTitle.text = cardTitle
        binding.iconContainer.setCardBackgroundColor(backgrounds[position].toColorInt())

        val iconResource = cardIconMap[cardTitle.uppercase()]
        iconResource?.let {
            binding.ivIcon.setImageResource(it)
            binding.ivIcon.setColorFilter(iconTints[position].toColorInt())
        }
    }
}