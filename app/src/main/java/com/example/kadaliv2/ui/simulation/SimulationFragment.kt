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

        binding.sliderHours.addOnChangeListener { _, value, _ ->
            binding.tvCurrentHours.text = "${value.toInt()} Hours"
        }

        binding.btnCalculateSim.setOnClickListener {
            val power = binding.etSimPower.text.toString().toDoubleOrNull() ?: 0.0
            val hours = binding.sliderHours.value.toDouble()
            
            // Assuming 1444.70 as default tariff for simulation if not injected
            viewModel.calculate(power, hours, 1444.70)
        }
        
        setupObserver()
    }
    
    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.simulationResult.collect { result ->
                    if (result != null) {
                        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                        binding.tvSimDaily.text = currencyFormat.format(result.daily)
                        binding.tvSimMonthly.text = currencyFormat.format(result.monthly)
                        binding.tvSimYearly.text = currencyFormat.format(result.yearly)
                        
                        val power = binding.etSimPower.text.toString().toDoubleOrNull() ?: 0.0
                        val dailyKwh = (power * binding.sliderHours.value) / 1000.0
                        binding.tvEnergyImpact.text = String.format("ENERGY: %.2f KWH/DAY", dailyKwh)
                        
                        if (result.monthly > 100000) {
                            binding.badgeImpact.text = "HIGH IMPACT"
                            binding.badgeImpact.setBackgroundResource(R.drawable.bg_badge_primary)
                            binding.badgeImpact.setTextColor(resources.getColor(R.color.navy_darker, null))
                        } else {
                            binding.badgeImpact.text = "LOW IMPACT"
                            binding.badgeImpact.setBackgroundResource(R.drawable.bg_badge_low)
                            binding.badgeImpact.setTextColor(android.graphics.Color.parseColor("#FF4CAF50"))
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
