package com.example.loginwithanimation.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.loginwithanimation.R

class StoryLoadStateAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<StoryLoadStateAdapter.LoadStateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_main, parent, false)
        return LoadStateViewHolder(view, retry)
    }

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    class LoadStateViewHolder(itemView: View, retry: () -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val progressBar: ProgressBar = itemView.findViewById(R.id.story_progress_bar)
        private val errorMsg: TextView = itemView.findViewById(R.id.error_msg)
        private val retryButton: Button = itemView.findViewById(R.id.retry_button)

        init {
            retryButton.setOnClickListener { retry() }
        }

        fun bind(loadState: LoadState) {
            if (loadState is LoadState.Error) {
                errorMsg.text = loadState.error.localizedMessage
            }
            progressBar.visibility = if (loadState is LoadState.Loading) View.VISIBLE else View.GONE
            retryButton.visibility = if (loadState is LoadState.Error) View.VISIBLE else View.GONE
            errorMsg.visibility = if (loadState is LoadState.Error) View.VISIBLE else View.GONE
        }
    }
}
