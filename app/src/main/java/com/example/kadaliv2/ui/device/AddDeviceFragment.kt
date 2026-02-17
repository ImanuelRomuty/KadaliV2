package com.example.kadaliv2.ui.device

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.kadaliv2.R
import com.example.kadaliv2.databinding.FragmentAddDeviceBinding
import com.example.kadaliv2.ui.viewmodel.DeviceViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.launch

class AddDeviceFragment : Fragment(R.layout.fragment_add_device) {

    private val viewModel: DeviceViewModel by viewModel()
    private val args: AddDeviceFragmentArgs by navArgs()
    private var _binding: FragmentAddDeviceBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddDeviceBinding.bind(view)

        val isEditMode = args.deviceId != -1L

        if (isEditMode) {
            setupEditMode(args.deviceId)
        }

        binding.btnSaveDevice.setOnClickListener {
            val name = binding.etDeviceName.text.toString()
            val powerStr = binding.etPower.text.toString()
            val qtyStr = binding.etQuantity.text.toString()
            val hoursStr = binding.etHours.text.toString()

            if (name.isBlank() || powerStr.isBlank() || hoursStr.isBlank()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val power = powerStr.toDoubleOrNull() ?: 0.0
            val qty = qtyStr.toIntOrNull() ?: 1
            val hours = hoursStr.toDoubleOrNull() ?: 0.0

            if (isEditMode) {
                viewModel.updateDevice(args.deviceId, args.roomId, name, power, hours, qty)
                Toast.makeText(requireContext(), "Device updated", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.saveDevice(args.roomId, name, power, hours, qty)
                Toast.makeText(requireContext(), "Device saved", Toast.LENGTH_SHORT).show()
            }
            findNavController().popBackStack()
        }
    }

    private fun setupEditMode(deviceId: Long) {
        binding.btnSaveDevice.text = "Update Device"
        (requireActivity() as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = "Edit Device"

        viewModel.getDevice(deviceId)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.device.collect { device ->
                    if (device != null) {
                        binding.etDeviceName.setText(device.name)
                        binding.etPower.setText(device.powerWatt.toString())
                        binding.etQuantity.setText(device.quantity.toString())
                        binding.etHours.setText(device.usageHoursPerDay.toString())
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
