package com.example.kadaliv2.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.kadaliv2.R
import com.example.kadaliv2.databinding.FragmentSettingsBinding
import com.example.kadaliv2.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: SettingsViewModel by viewModel()
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        setupObserver()

        binding.btnSaveSettings.setOnClickListener {
            val priceStr = binding.etTariff.text.toString()
            val price = priceStr.toDoubleOrNull()
            
            if (price != null) {
                viewModel.saveTariff(price)
                Toast.makeText(requireContext(), "Tariff saved", Toast.LENGTH_SHORT).show()
            } else {
                binding.etTariff.error = "Invalid price"
            }
        }
    }
    
    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tariff.collect { tariff ->
                    if (tariff != null) {
                        val currencyFormat = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("id", "ID"))
                        currencyFormat.maximumFractionDigits = 0
                        
                        binding.tvCurrentTariff.text = "${currencyFormat.format(tariff.pricePerKwh)} / kWh"
                        
                        // Only set EditText if empty to allow editing
                        if (binding.etTariff.text.isNullOrEmpty()) {
                            binding.etTariff.setText(tariff.pricePerKwh.toString())
                        }
                    } else {
                        binding.tvCurrentTariff.text = "Not Set"
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
