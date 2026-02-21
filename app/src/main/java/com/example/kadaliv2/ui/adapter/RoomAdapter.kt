package com.example.kadaliv2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kadaliv2.databinding.ItemRoomBinding
import com.example.kadaliv2.domain.model.Room
import java.text.NumberFormat
import java.util.Locale

class RoomAdapter(
    private val onItemClick: (Room) -> Unit
) : ListAdapter<Room, RoomAdapter.RoomViewHolder>(DiffCallback) {

    /** Map of room name → daily cost in Rp. Updated separately from the list. */
    private var roomDailyCost: Map<String, Double> = emptyMap()

    /** Call this whenever dashboardState.roomDailyCost changes. */
    fun submitCosts(costs: Map<String, Double>) {
        roomDailyCost = costs
        notifyItemRangeChanged(0, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = ItemRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RoomViewHolder(private val binding: ItemRoomBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(room: Room) {
            binding.tvRoomName.text = room.name
            binding.tvDeviceCount.text = room.description ?: "No description"

            val cost = roomDailyCost[room.name] ?: 0.0
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            currencyFormat.maximumFractionDigits = 0
            binding.tvRoomCost.text = currencyFormat.format(cost)

            binding.ivRoomIcon.setImageResource(com.example.kadaliv2.R.drawable.ic_room_placeholder)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Room>() {
        override fun areItemsTheSame(oldItem: Room, newItem: Room): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Room, newItem: Room): Boolean = oldItem == newItem
    }
}
