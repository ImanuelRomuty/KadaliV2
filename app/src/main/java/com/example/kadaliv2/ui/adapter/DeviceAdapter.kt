package com.example.kadaliv2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kadaliv2.databinding.ItemDeviceBinding
import com.example.kadaliv2.domain.model.Device
import java.text.NumberFormat
import java.util.Locale

class DeviceAdapter(
    private val onEditClick: (Device) -> Unit,
    private val onDeleteClick: (Device) -> Unit
) : ListAdapter<Device, DeviceAdapter.DeviceViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DeviceViewHolder(private val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(device: Device) {
            binding.tvDeviceName.text = device.name
            binding.tvDeviceQuantity.text = "x${device.quantity}"
            binding.tvDeviceSpecs.text = "${device.powerWatt}W | ${device.usageHoursPerDay} hrs/day"
            
            // Calculate daily cost approx if needed or just show consumption
            val dailyKwh = (device.powerWatt * device.usageHoursPerDay * device.quantity) / 1000.0
            binding.tvDeviceCost.text = String.format("%.2f kWh/day", dailyKwh)
            
            binding.btnEditDevice.setOnClickListener {
                onEditClick(device)
            }
            
            binding.btnDeleteDevice.setOnClickListener {
                onDeleteClick(device)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Device>() {
        override fun areItemsTheSame(oldItem: Device, newItem: Device): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Device, newItem: Device): Boolean = oldItem == newItem
    }
}
