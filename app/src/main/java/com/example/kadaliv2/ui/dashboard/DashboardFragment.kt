package com.example.kadaliv2.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kadaliv2.R
import com.example.kadaliv2.databinding.FragmentDashboardBinding
import com.example.kadaliv2.ui.adapter.RoomAdapter
import com.example.kadaliv2.ui.viewmodel.DashboardViewModel
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.NumberFormat
import java.util.Locale

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private val viewModel: DashboardViewModel by viewModel()
    private val pdfReportGenerator: com.example.kadaliv2.data.report.PdfReportGenerator by inject()
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var roomAdapter: RoomAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDashboardBinding.bind(view)

        setupRecyclerView()
        setupChart()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        roomAdapter = RoomAdapter { room ->
            val action = DashboardFragmentDirections.actionDashboardToRoomDetail(room.id)
            findNavController().navigate(action)
        }
        binding.rvRooms.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = roomAdapter
        }
    }

    private fun setupListeners() {
        binding.fabAddRoom.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_addRoom)
        }
        binding.btnAddRoomEmpty.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_addRoom)
        }
        
        requireActivity().addMenuProvider(object : androidx.core.view.MenuProvider {
            override fun onCreateMenu(menu: android.view.Menu, menuInflater: android.view.MenuInflater) {
                menuInflater.inflate(R.menu.dashboard_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: android.view.MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_export_report -> {
                        exportReport()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
    
    private fun exportReport() {
        // Show loading (optional, but good UX)
        android.widget.Toast.makeText(requireContext(), "Generating Report...", android.widget.Toast.LENGTH_SHORT).show()
        
        // Inject Generator here or in VM? VM has it via Koin? 
        // Wait, VM calculate function takes generator as param in my previous step: 
        // fun generateReport(pdfReportGenerator: ...
        // So I need to get it here.
        
        viewModel.generateReport(pdfReportGenerator, 
            onSuccess = { path ->
                android.widget.Toast.makeText(requireContext(), "Report saved to $path", android.widget.Toast.LENGTH_LONG).show()
                // Optional: Share intent
            },
            onError = {
                android.widget.Toast.makeText(requireContext(), "Failed to generate report", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setupChart() {
        binding.barChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setFitBars(true)
            
            xAxis.apply {
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
            
            setNoDataText("Add rooms to see chart")
            invalidate()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.rooms.collect { rooms ->
                        roomAdapter.submitList(rooms)
                    }
                }
                launch {
                    viewModel.dashboardState.collect { state ->
                        updateDashboard(state)
                        updateVisibility(state.isRoomListEmpty)
                    }
                }
            }
        }
    }
    
    private fun updateVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.groupContent.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.groupContent.visibility = View.VISIBLE
        }
    }

    private fun updateDashboard(state: DashboardViewModel.DashboardState) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        currencyFormat.maximumFractionDigits = 0
        
        binding.tvDailyCost.text = currencyFormat.format(state.dailyCost)
        binding.tvMonthlyCost.text = currencyFormat.format(state.monthlyCost)
        
        // Prepare Bar Data
        val roomConsumption = state.roomConsumption.filter { it.value > 0.0 }
        val roomList = roomConsumption.keys.toList()
        val entries = roomList.mapIndexed { index, roomName ->
            val energy = roomConsumption[roomName] ?: 0.0
            com.github.mikephil.charting.data.BarEntry(index.toFloat(), energy.toFloat())
        }

        if (entries.isNotEmpty()) {
            val dataSet = com.github.mikephil.charting.data.BarDataSet(entries, "Energy Consumption (kWh)")
            
            // Get Colors from Theme
            val colorPrimary = requireContext().getColor(R.color.primary)
            val colorTextSecondary = requireContext().getColor(R.color.text_secondary)
            val colorTextPrimary = requireContext().getColor(R.color.text_primary)
            
            dataSet.color = colorPrimary
            dataSet.valueTextColor = colorTextPrimary
            dataSet.valueTextSize = 10f
            
            val data = com.github.mikephil.charting.data.BarData(dataSet)
            data.barWidth = 0.6f
            
            binding.barChart.apply {
                this.data = data
                xAxis.textColor = colorTextSecondary
                axisLeft.textColor = colorTextSecondary
                legend.textColor = colorTextSecondary
                
                // Styling
                setDrawGridBackground(false)
                setDrawBorders(false)
                xAxis.setDrawAxisLine(true)
                xAxis.axisLineColor = requireContext().getColor(R.color.outline)
                xAxis.setDrawGridLines(false)
                axisLeft.setDrawAxisLine(false)
                axisLeft.setDrawGridLines(true)
                axisLeft.gridColor = requireContext().getColor(R.color.outline).let { color ->
                    android.graphics.Color.argb(50, android.graphics.Color.red(color), android.graphics.Color.green(color), android.graphics.Color.blue(color))
                }
                
                xAxis.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index >= 0 && index < roomList.size) roomList[index] else ""
                    }
                }
                invalidate()
            }
        } else {
            binding.barChart.clear()
            binding.barChart.setNoDataTextColor(requireContext().getColor(R.color.text_secondary))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
