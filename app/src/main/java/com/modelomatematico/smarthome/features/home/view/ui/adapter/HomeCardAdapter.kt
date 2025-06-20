package com.modelomatematico.smarthome.features.home.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.modelomatematico.smarthome.R
import com.modelomatematico.smarthome.databinding.ItemHomeCardBinding
import androidx.core.graphics.toColorInt

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
    private val subtitles: Array<String>
    private val actions: Array<String>
    private val backgrounds: Array<String>
    private val iconTints: Array<String>
    private val textColors: Array<String>
    private val subtitleColors: Array<String>

    init {
        val context = view.context
        val titlesArray = context.resources.getStringArray(R.array.home_card_titles)
        subtitles = context.resources.getStringArray(R.array.home_card_subtitles)
        actions = context.resources.getStringArray(R.array.home_card_actions)
        backgrounds = context.resources.getStringArray(R.array.home_card_backgrounds)
        iconTints = context.resources.getStringArray(R.array.home_card_icon_tints)
        textColors = context.resources.getStringArray(R.array.home_card_text_colors)
        subtitleColors = context.resources.getStringArray(R.array.home_card_subtitle_colors)

        cardIconMap = mapOf(
            titlesArray[0].uppercase() to R.drawable.ic_lightbulb,
            titlesArray[1].uppercase() to R.drawable.ic_bathroom,
            titlesArray[2].uppercase() to R.drawable.ic_door,
            titlesArray[3].uppercase() to R.drawable.ic_dining,
        )
    }

    fun render(cardTitle: String, position: Int) {
        // Configurar textos
        binding.tvTitle.text = cardTitle
        binding.tvSubtitle.text = subtitles[position]

        // Configurar colores
        binding.cardView.setCardBackgroundColor(backgrounds[position].toColorInt())
        binding.tvTitle.setTextColor(textColors[position].toColorInt())
        binding.tvSubtitle.setTextColor(subtitleColors[position].toColorInt())

        // Configurar icono
        val iconResource = cardIconMap[cardTitle.uppercase()]
        iconResource?.let {
            binding.ivIcon.setImageResource(it)
            binding.ivIcon.setColorFilter(iconTints[position].toColorInt())
        }
    }
}