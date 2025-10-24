package com.modelomatematico.smarthome.features.lights.view.ui.viewholder

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.modelomatematico.smarthome.R
import com.modelomatematico.smarthome.databinding.ItemLightsCardBinding
import com.modelomatematico.smarthome.features.lights.data.model.LightCardModel


class LightsCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemLightsCardBinding.bind(view)
    private val cardIconMap: Map<String, Int>
    private val backgrounds: Array<String>
    private val iconTints: Array<String>
    private var switchToggleListener: ((Boolean) -> Unit)? = null
    private var currentState = false

    init {
        val context = view.context
        val titlesArray = context.resources.getStringArray(R.array.lights_card_titles)
        backgrounds = context.resources.getStringArray(R.array.Lights_card_backgrounds)
        iconTints = context.resources.getStringArray(R.array.Lights_card_icon_tints)
        cardIconMap = titlesArray.associate { it.uppercase() to R.drawable.ic_sun }
        binding.materialSwitch.setOnCheckedChangeListener { _, isChecked ->
            switchToggleListener?.invoke(isChecked)
        }
    }

    fun render(lightCard: LightCardModel, position: Int) {
        binding.tvTitle.text = lightCard.title
        binding.materialSwitch.isChecked = lightCard.isOn

        if (position < backgrounds.size) {
            binding.iconContainer.setCardBackgroundColor(backgrounds[position].toColorInt())
        } else {
            binding.iconContainer.setCardBackgroundColor(
                ContextCompat.getColor(binding.root.context, R.color.sunYellow)
            )
        }

        val iconResource = cardIconMap[lightCard.title.uppercase()] ?: R.drawable.ic_sun
        binding.ivIcon.setImageResource(iconResource)

        if (position < iconTints.size) {
            binding.ivIcon.setColorFilter(iconTints[position].toColorInt())
        } else {
            binding.ivIcon.setColorFilter(
                ContextCompat.getColor(binding.root.context, R.color.white)
            )
        }
    }

    fun setSwitchToggleListener(listener: (Boolean) -> Unit) {
        switchToggleListener = listener
    }
}