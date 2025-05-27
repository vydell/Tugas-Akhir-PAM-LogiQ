package com.beginning.tugasakhirpam.features.homepage.adapter


import com.beginning.tugasakhirpam.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.beginning.tugasakhirpam.features.quiz.model.Quiz
import com.beginning.tugasakhirpam.features.quiz.model.QuizStatusEnum


class HomepageQuizAdapter(
    private val quizList: List<Quiz>,
    private val onItemClick: (Quiz) -> Unit
) : RecyclerView.Adapter<HomepageQuizAdapter.QuizViewHolder>() {

    inner class QuizViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val quizTitle: TextView = itemView.findViewById(R.id.quizTitle)
        val quizOpenDate: TextView = itemView.findViewById(R.id.quizOpenDate)
        val quizCloseDate: TextView = itemView.findViewById(R.id.quizCloseDate)
        val statusText: TextView = itemView.findViewById(R.id.statusText)
        val statusCard: CardView = itemView.findViewById(R.id.statusCard)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(quizList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quiz_homepage, parent, false)
        return QuizViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val quiz = quizList[position]

        holder.quizTitle.text = quiz.title
        holder.quizOpenDate.text = "Opens: ${quiz.openDate}"
        holder.quizCloseDate.text = "Closed: ${quiz.closeDate}"

        // Set status appearance
        when (quiz.status) {
            QuizStatusEnum.OPENED -> {
                holder.statusText.text = "opened"
                holder.statusCard.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.status_open)
                )
                holder.statusText.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.white)
                )
            }
            QuizStatusEnum.CLOSED -> {
                holder.statusText.text = "closed"
                holder.statusCard.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.status_closed)
                )
                holder.statusText.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.white)
                )
            }

            null -> TODO()
        }
    }

    override fun getItemCount(): Int = quizList.size
}