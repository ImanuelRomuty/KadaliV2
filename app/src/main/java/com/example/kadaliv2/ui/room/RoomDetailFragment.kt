package com.example.kadaliv2.ui.room

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kadaliv2.R
import com.example.kadaliv2.databinding.FragmentRoomDetailBinding
import com.example.kadaliv2.ui.adapter.DeviceAdapter
import com.example.kadaliv2.ui.viewmodel.DeviceViewModel
import com.example.kadaliv2.ui.viewmodel.RoomViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.NumberFormat
import java.util.Locale
import android.widget.Toast

class RoomDetailFragment : Fragment(R.layout.fragment_room_detail) {

    private val viewModel: DeviceViewModel by viewModel()
    private val roomViewModel: RoomViewModel by viewModel()
    private val args: RoomDetailFragmentArgs by navArgs()
    private var _binding: FragmentRoomDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var deviceAdapter: DeviceAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRoomDetailBinding.bind(view)

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter(
            onEditClick = { device ->
                val action = RoomDetailFragmentDirections.actionRoomDetailToAddDevice(args.roomId, device.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { device ->
                showDeleteDeviceConfirmation(device)
            }
        )
        binding.rvDevices.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = deviceAdapter
            isNestedScrollingEnabled = false
        }
    }
    
    private fun showDeleteDeviceConfirmation(device: com.example.kadaliv2.domain.model.Device) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Remove Device?")
            .setMessage("This device will be removed from the room.")
            .setPositiveButton("Remove") { _, _ ->
                viewModel.deleteDevice(device)
                Toast.makeText(requireContext(), "Device removed", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        
        binding.btnEditRoom.setOnClickListener {
            val action = RoomDetailFragmentDirections.actionRoomDetailToEditRoom(roomId = args.roomId)
            findNavController().navigate(action)
        }
        
        binding.btnDeleteRoom.setOnClickListener {
            showDeleteRoomConfirmation()
        }

        binding.fabAddDevice.setOnClickListener {
            val action = RoomDetailFragmentDirections.actionRoomDetailToAddDevice(args.roomId)
            findNavController().navigate(action)
        }
    }
    
    private fun showDeleteRoomConfirmation() {
        val room = roomViewModel.room.value ?: return
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Room?")
            .setMessage("This will permanently delete \"${room.name}\" and all its devices.")
            .setPositiveButton("Delete") { _, _ ->
                roomViewModel.deleteRoom(room)
                Toast.makeText(requireContext(), "Room deleted", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupObservers() {
        roomViewModel.getRoom(args.roomId)
        viewModel.setRoomId(args.roomId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.roomDevices.collect { devices ->
                        deviceAdapter.submitList(devices)
                        binding.tvEmptyDevices.visibility = if (devices.isEmpty()) View.VISIBLE else View.GONE
                        binding.tvActiveDevicesLabel.text = "${devices.size} ACTIVE"
                    }
                }
                launch {
                    roomViewModel.room.collect { room ->
                        if (room != null) {
                            binding.tvDetailRoomName.text = room.name
                        }
                    }
                }
                launch {
                    viewModel.roomStats.collect { (energy, cost) ->
                        updateSummary(energy, cost)
                    }
                }
            }
        }
    }

    private fun updateSummary(energy: Double, cost: Double) {
        val locale = Locale.forLanguageTag("id-ID")
        val currencyFormat = NumberFormat.getCurrencyInstance(locale)
        currencyFormat.maximumFractionDigits = 0
        
        binding.tvDetailEnergy.text = String.format(locale, "%.1f", energy)
        binding.tvDetailCost.text = currencyFormat.format(cost)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
