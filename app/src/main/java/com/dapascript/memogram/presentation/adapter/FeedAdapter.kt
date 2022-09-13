package com.dapascript.memogram.presentation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.dapascript.memogram.data.source.local.model.FeedEntity
import com.dapascript.memogram.databinding.ItemListFeedBinding
import com.dapascript.memogram.utils.formatDate
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.iamageo.library.AnotherReadMore
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class FeedAdapter(
    context: Context
) : PagingDataAdapter<FeedEntity, FeedAdapter.FeedViewHolder>(DIFF_UTIL) {

    var onClick: ((FeedEntity) -> Unit)? = null

    private val readMore = AnotherReadMore.Builder(context)
        .textLength(50, AnotherReadMore.TYPE_CHARACTER)
        .moreLabel("Baca Selengkapnya")
        .lessLabel("Tutup")
        .build()

    inner class FeedViewHolder(
        private val binding: ItemListFeedBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(feedEntity: FeedEntity) {
            binding.apply {
                val shimmer =
                    Shimmer.AlphaHighlightBuilder()// The attributes for a ShimmerDrawable is set by this builder
                        .setDuration(1800) // how long the shimmering animation takes to do one full sweep
                        .setBaseAlpha(0.7f) //the alpha of the underlying children
                        .setHighlightAlpha(0.6f) // the shimmer alpha amount
                        .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
                        .setAutoStart(true)
                        .build()

                val shimmerDrawable = ShimmerDrawable().apply {
                    setShimmer(shimmer)
                }

                Glide.with(itemView.context)
                    .load(feedEntity.image)
                    .placeholder(shimmerDrawable)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivPhoto)
                tvName.text = feedEntity.name
                readMore.addReadMoreTo(tvDesc, feedEntity.description)
                tvDate.text = formatDate(feedEntity.date)
            }
        }

        init {
            binding.root.setOnClickListener {
                getItem(bindingAdapterPosition)?.let { data -> onClick?.invoke(data) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        return FeedViewHolder(
            ItemListFeedBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        if (getItem(position) != null) {
            holder.bind(getItem(position)!!)
        }
    }

    companion object {
        private val DIFF_UTIL = object : DiffUtil.ItemCallback<FeedEntity>() {
            override fun areItemsTheSame(oldItem: FeedEntity, newItem: FeedEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: FeedEntity, newItem: FeedEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}