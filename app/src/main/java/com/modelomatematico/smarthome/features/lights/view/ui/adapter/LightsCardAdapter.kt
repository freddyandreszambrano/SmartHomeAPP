package com.modelomatematico.smarthome.features.lights.view.ui.adapter

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.modelomatematico.smarthome.R
import com.modelomatematico.smarthome.databinding.ItemLightsCardBinding

data class LightCard(
    val title: String,
    val isOn: Boolean = false
)

class LightsCardAdapter(
    private var lightCards: MutableList<LightCard>,
    private val onItemClick: (LightCard, Int) -> Unit,
    private val onSwitchToggle: (LightCard, Int, Boolean) -> Unit
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

        cardIconMap = mapOf(
            titlesArray[0].uppercase() to R.drawable.ic_sun,
            titlesArray[1].uppercase() to R.drawable.ic_sun,
            titlesArray[2].uppercase() to R.drawable.ic_sun,
            titlesArray[3].uppercase() to R.drawable.ic_sun,
            titlesArray[4].uppercase() to R.drawable.ic_sun,
        )

        setupSwitchClickListener()
    }

    fun render(lightCard: LightCard, position: Int) {
        binding.tvTitle.text = lightCard.title
        currentState = lightCard.isOn

        updateSwitchState(lightCard.isOn, false)

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

    private fun setupSwitchClickListener() {
        val switchClickListener = View.OnClickListener {
            val newState = !currentState
            currentState = newState
            updateSwitchState(newState, true)
            switchToggleListener?.invoke(newState)
        }

        binding.switchContainer.setOnClickListener(switchClickListener)
        binding.tvOnOff.setOnClickListener(switchClickListener)

        binding.switchContainer.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    v.alpha = 0.7f
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    v.alpha = 1.0f
                }
            }
            false
        }
    }

    private fun updateSwitchState(isOn: Boolean, animate: Boolean) {
        val context = binding.root.context

        // Actualizar texto On/Off
        binding.tvOnOff.text = if (isOn) "On" else "Off"
        binding.tvOnOff.setTextColor(
            ContextCompat.getColor(
                context,
                if (isOn) R.color.brightBlue else R.color.textGrayDark
            )
        )

        // Cambiar color del fondo del switch
        binding.switchContainer.setCardBackgroundColor(
            ContextCompat.getColor(
                context,
                if (isOn) R.color.brightBlue else R.color.switchInactive
            )
        )

        // Animar o posicionar el thumb del switch
        val targetTranslationX = if (isOn) 20f else 0f

        if (animate) {
            val currentTranslationX = binding.switchThumb.translationX
            val animator = ObjectAnimator.ofFloat(
                binding.switchThumb,
                "translationX",
                currentTranslationX,
                targetTranslationX
            )
            animator.duration = 250
            animator.interpolator = android.view.animation.DecelerateInterpolator()

            // Agregar un pequeño efecto de escala durante la animación
            val scaleAnimator = ObjectAnimator.ofFloat(binding.switchThumb, "scaleX", 1f, 0.9f, 1f)
            scaleAnimator.duration = 250

            val scaleYAnimator = ObjectAnimator.ofFloat(binding.switchThumb, "scaleY", 1f, 0.9f, 1f)
            scaleYAnimator.duration = 250

            animator.start()
            scaleAnimator.start()
            scaleYAnimator.start()
        } else {
            binding.switchThumb.translationX = targetTranslationX
        }
    }
}