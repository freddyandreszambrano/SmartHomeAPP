package com.modelomatematico.smarthome.features.lights.view.ui.adapter

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.modelomatematico.smarthome.R
import com.modelomatematico.smarthome.databinding.ItemLightsCardBinding
import com.modelomatematico.smarthome.features.lights.data.model.LightCardModel

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

        setupSwitchClickListener()
    }

    fun render(lightCard: LightCardModel, position: Int) {
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

        binding.switchContainer.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.alpha = 0.7f
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.alpha = 1.0f
                }
            }
            false
        }
    }

    private fun updateSwitchState(isOn: Boolean, animate: Boolean) {
        val context = binding.root.context

        binding.tvSwitchText.text = if (isOn) "ON" else "OFF"
        binding.tvSwitchText.setTextColor(
            ContextCompat.getColor(context, R.color.white)
        )

        binding.switchContainer.setCardBackgroundColor(
            ContextCompat.getColor(
                context,
                if (isOn) R.color.brightBlue else R.color.switchInactive
            )
        )

        val thumbTargetX = if (isOn) 44f else 4f
        val textTargetX = if (isOn) 0f else 40f

        if (animate) {
            val thumbAnimator = ObjectAnimator.ofFloat(
                binding.switchThumb,
                "translationX",
                binding.switchThumb.translationX,
                thumbTargetX
            )
            thumbAnimator.duration = 300
            thumbAnimator.interpolator = AccelerateDecelerateInterpolator()

            val textAnimator = ObjectAnimator.ofFloat(
                binding.tvSwitchText,
                "translationX",
                binding.tvSwitchText.translationX,
                textTargetX
            )
            textAnimator.duration = 300
            textAnimator.interpolator = AccelerateDecelerateInterpolator()
            val scaleAnimator = ObjectAnimator.ofFloat(binding.switchThumb, "scaleX", 1f, 0.85f, 1f)
            scaleAnimator.duration = 300

            val scaleYAnimator =
                ObjectAnimator.ofFloat(binding.switchThumb, "scaleY", 1f, 0.85f, 1f)
            scaleYAnimator.duration = 300

            thumbAnimator.start()
            textAnimator.start()
            scaleAnimator.start()
            scaleYAnimator.start()
        } else {
            binding.switchThumb.translationX = thumbTargetX
            binding.tvSwitchText.translationX = textTargetX
        }
    }
}