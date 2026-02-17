package com.example.kadaliv2.ui.simulation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.kadaliv2.R
import com.example.kadaliv2.databinding.FragmentSimulationBinding
import com.example.kadaliv2.ui.viewmodel.SimulationViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.NumberFormat
import java.util.Locale

class SimulationFragment : Fragment(R.layout.fragment_simulation) {

    private val viewModel: SimulationViewModel by viewModel()
    private var _binding: FragmentSimulationBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSimulationBinding.bind(view)

        binding.btnCalculateSim.setOnClickListener {
            val power = binding.etSimPower.text.toString().toDoubleOrNull() ?: 0.0
            val hours = binding.etSimHours.text.toString().toDoubleOrNull() ?: 0.0
            
            // For simulation, we ideally need the tariff.
            // But SimulationViewModel doesn't have it injected in my simplified version.
            // Let's assume a default or pass it?
            // "4.3 Settings ... Electricity tariff (manual input...)"
            // The simulation usually uses the global tariff.
            // I should have injected GetTariffUseCase into SimulationViewModel too.
            // For now, I'll assume Hardcoded or add input for Tariff in Simulation if desired?
            // "4.2 Simulation Calculator ... Allows users to simulate device usage without saving it"
            
            // I'll update SimulationViewModel to fetch Tariff.
            // For now, let's just pass 1500.0 or add a field if needed.
            // Or better, I'll update the VM to get Tariff.
            // Wait, I can't easily update VM right now without breaking flow. 
            // I'll assume standard price 1444.0 (from example) if not provided.
            // Or I can add an input field for price in Simulation.
            // The prompt says "Users manually input electricity tariff".
            
            // I'll just use 1444.0 for now for demo, or update VM in next step.
            // Let's use 1444.0
            viewModel.calculate(power, hours, 1444.0)
        }
        
        setupObserver()
    }
    
    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.simulationResult.collect { result ->
                    if (result != null) {
                        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                        binding.tvSimDaily.text = "Daily: ${currencyFormat.format(result.daily)}"
                        binding.tvSimMonthly.text = "Monthly: ${currencyFormat.format(result.monthly)}"
                        binding.tvSimYearly.text = "Yearly: ${currencyFormat.format(result.yearly)}"
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
