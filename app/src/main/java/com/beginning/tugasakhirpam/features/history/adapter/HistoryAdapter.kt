package com.beginning.tugasakhirpam.features.history.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.beginning.tugasakhirpam.R
import com.beginning.tugasakhirpam.features.user.model.QuizHistory

class HistoryAdapter(
    private val historyList: List<QuizHistory>,
    private val onItemClick: (QuizHistory) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val quizTitle: TextView = itemView.findViewById(R.id.historyQuizTitle)
        val accuracyPercentage: TextView = itemView.findViewById(R.id.accuracyPercentage)
        val completionPercentage: TextView = itemView.findViewById(R.id.completionPercentage)
        val submittedDate: TextView = itemView.findViewById(R.id.submittedDate)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(historyList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quiz_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val history = historyList[position]

        holder.quizTitle.text = history.quizTitle
        holder.accuracyPercentage.text = buildString {
            append(history.accuracy)
            append("%")
        }
        holder.completionPercentage.text = buildString {
            append(history.completionRate)
            append("%")
        }
        holder.submittedDate.text = buildString {
            append("Submitted ")
            append(history.submittedDate)
        }

        val accuracyColor = when {
            history.accuracy >= 80 -> R.color.status_open // Green
            history.accuracy >= 60 -> R.color.orange
            else -> R.color.status_closed // Red
        }

        holder.accuracyPercentage.setTextColor(
            ContextCompat.getColor(holder.itemView.context, accuracyColor)
        )

        val completionColor = when {
            history.completionRate >= 80 -> R.color.status_open
            history.completionRate >= 60 -> R.color.orange
            else -> R.color.status_closed
        }

        holder.completionPercentage.setTextColor(
            ContextCompat.getColor(holder.itemView.context, completionColor)
        )
    }

    override fun getItemCount(): Int = historyList.size
}