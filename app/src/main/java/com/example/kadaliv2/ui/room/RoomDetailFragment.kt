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
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.MenuProvider

class RoomDetailFragment : Fragment(R.layout.fragment_room_detail) {

    private val viewModel: com.example.kadaliv2.ui.viewmodel.DeviceViewModel by viewModel()
    private val roomViewModel: com.example.kadaliv2.ui.viewmodel.RoomViewModel by viewModel() // Need RoomViewModel for Delete/GetRoom
    private val args: RoomDetailFragmentArgs by navArgs()
    private var _binding: FragmentRoomDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var deviceAdapter: DeviceAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRoomDetailBinding.bind(view)

        viewModel.setRoomId(args.roomId)
        
        setupRecyclerView()
        setupObservers()
        setupListeners()
        setupMenu()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.room_detail_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_edit_room -> {
                        val action = RoomDetailFragmentDirections.actionRoomDetailToEditRoom(roomId = args.roomId)
                        findNavController().navigate(action)
                        true
                    }
                    R.id.action_delete_room -> {
                        showDeleteConfirmation()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showDeleteConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Room?")
            .setMessage("All devices inside this room will also be removed. This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                val currentRoom = roomViewModel.room.value
                if (currentRoom != null) {
                    roomViewModel.deleteRoom(currentRoom)
                    android.widget.Toast.makeText(requireContext(), "Room deleted", android.widget.Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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
        }
    }
    
    private fun showDeleteDeviceConfirmation(device: com.example.kadaliv2.domain.model.Device) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Remove Device?")
            .setMessage("This device will be removed from the room.")
            .setPositiveButton("Remove") { _, _ ->
                viewModel.deleteDevice(device)
                android.widget.Toast.makeText(requireContext(), "Device removed", android.widget.Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupListeners() {
        binding.fabAddDevice.setOnClickListener {
            val action = RoomDetailFragmentDirections.actionRoomDetailToAddDevice(args.roomId)
            findNavController().navigate(action)
        }
    }

    private fun setupObservers() {
        roomViewModel.getRoom(args.roomId) // Fetch Room info
        viewModel.setRoomId(args.roomId) // Fetch Devices

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.roomDevices.collect { devices ->
                        deviceAdapter.submitList(devices)
                        updateEmptyState(devices.isEmpty())
                    }
                }
                launch {
                    roomViewModel.room.collect { room ->
                        if (room != null) {
                            (requireActivity() as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = room.name
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

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.tvEmptyDevices.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvDevices.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun updateSummary(energy: Double, cost: Double) {
        val locale = Locale.forLanguageTag("id-ID")
        val currencyFormat = NumberFormat.getCurrencyInstance(locale)
        binding.tvDetailTotalCost.text = "Monthly: ${currencyFormat.format(cost)} | Daily: ${String.format(locale, "%.2f", energy)} kWh"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
