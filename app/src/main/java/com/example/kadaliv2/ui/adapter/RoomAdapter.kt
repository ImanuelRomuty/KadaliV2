package com.example.kadaliv2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kadaliv2.databinding.ItemRoomBinding
import com.example.kadaliv2.domain.model.Room

class RoomAdapter(
    private val onItemClick: (Room) -> Unit
) : ListAdapter<Room, RoomAdapter.RoomViewHolder>(DiffCallback) {

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
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(room: Room) {
            binding.tvRoomName.text = room.name
            // Note: Energy and Cost per room require calculating devices for that room.
            // For simplicity in this adapter, we might just show basic info or 
            // we need to pass a map of room stats. 
            // For now, let's just show the name.
            binding.tvRoomEnergy.text = room.description ?: ""
            binding.tvRoomCost.text = "" // TODO: Bind cost if available
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Room>() {
        override fun areItemsTheSame(oldItem: Room, newItem: Room): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Room, newItem: Room): Boolean = oldItem == newItem
    }
}
