package com.beginning.tugasakhirpam.features.quiz.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.beginning.tugasakhirpam.R
import com.beginning.tugasakhirpam.databinding.ItemAnswerQuizBinding
import com.beginning.tugasakhirpam.features.quiz.model.AnswerChoice

class AnswerAdapter: RecyclerView.Adapter<AnswerAdapter.ViewHolder>() {
    private var answers = mutableListOf<AnswerChoice>()
    private var selectedPosition = -1
    var onAnswerSelected: ((Int) -> Unit)? = null

    inner class ViewHolder(private val binding: ItemAnswerQuizBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(choice: AnswerChoice, position: Int) {
            binding.apply {
                tvAnswer.text = choice.text

                if (position == selectedPosition) {
                    setSelectedState()
                } else {
                    setUnselectedState()
                }

                root.setOnClickListener {
                    updateSelection(position)
                    onAnswerSelected?.invoke(position)
                }
            }
        }

        private fun setSelectedState() {
            binding.ivCheckAnswer.setImageResource(R.drawable.ic_circle_fill)
        }

        private fun setUnselectedState() {
            binding.ivCheckAnswer.setImageResource(R.drawable.ic_circle)
        }
    }

    private fun updateSelection(newPosition: Int) {
        val previousSelected = selectedPosition
        selectedPosition = newPosition

        if (previousSelected != -1) {
            answers[previousSelected].isClick = false
            notifyItemChanged(previousSelected)
        }

        answers[selectedPosition].isClick = true
        notifyItemChanged(selectedPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAnswerQuizBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(answers[position], position)
    }

    override fun getItemCount(): Int = answers.size

    fun setData(answers: List<AnswerChoice>) {
        this.answers = answers.toMutableList()
        selectedPosition = -1
        notifyDataSetChanged()
    }
}