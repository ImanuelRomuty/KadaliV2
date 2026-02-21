package com.example.kadaliv2.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.kadaliv2.R
import com.example.kadaliv2.data.report.PdfReportGenerator
import com.example.kadaliv2.databinding.FragmentSettingsBinding
import com.example.kadaliv2.ui.viewmodel.DashboardViewModel
import com.example.kadaliv2.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val settingsViewModel: SettingsViewModel by viewModel()
    private val dashboardViewModel: DashboardViewModel by viewModel()
    private val pdfReportGenerator: PdfReportGenerator by inject()
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        setupObserver()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSaveSettings.setOnClickListener {
            val priceStr = binding.etTariff.text.toString()
            val price = priceStr.toDoubleOrNull()

            if (price != null) {
                settingsViewModel.saveTariff(price)
                Toast.makeText(requireContext(), "Tariff updated successfully", Toast.LENGTH_SHORT).show()
            } else {
                binding.etTariff.error = "Invalid price"
            }
        }

        binding.btnSavePdf.setOnClickListener {
            Toast.makeText(requireContext(), "Generating PDF…", Toast.LENGTH_SHORT).show()
            dashboardViewModel.generateReport(pdfReportGenerator,
                onSuccess = { path ->
                    Toast.makeText(requireContext(), "PDF saved: $path", Toast.LENGTH_LONG).show()
                },
                onError = {
                    Toast.makeText(requireContext(), "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                }
            )
        }

        binding.layoutAbout.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_about)
        }
    }

    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.tariff.collect { tariff ->
                    if (tariff != null) {
                        if (binding.etTariff.text.toString().isEmpty()) {
                            binding.etTariff.setText(tariff.pricePerKwh.toString())
                        }
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
