package com.beginning.tugasakhirpam.features.quiz.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.beginning.tugasakhirpam.R
import com.beginning.tugasakhirpam.databinding.ActivityContentBinding
import com.beginning.tugasakhirpam.databinding.ItemContentQuizBinding
import com.beginning.tugasakhirpam.features.quiz.model.AnswerChoice
import com.beginning.tugasakhirpam.features.quiz.model.Question

class QuizContentAdapter : RecyclerView.Adapter<QuizContentAdapter.ViewHolder>() {
    private var contents = mutableListOf<Question>()

    class ViewHolder(private val itemContentBinding: ItemContentQuizBinding)
        : RecyclerView.ViewHolder(itemContentBinding.root) {
        fun bindItem(question: Question) {
            val answerAdapter = AnswerAdapter()
            itemContentBinding.txQuizQn.text = question.body

            question.answerChoices?.let { answers ->
                answerAdapter.setData(answers)
                itemContentBinding.rvAnswerQuiz.adapter = answerAdapter
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContentQuizBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(contents[position])
    }

    override fun getItemCount(): Int = contents.size

    fun setData(contents: List<Question>) {
        this.contents = contents.toMutableList()
        notifyDataSetChanged()
    }
}