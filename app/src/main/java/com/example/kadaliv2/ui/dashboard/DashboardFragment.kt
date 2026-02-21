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
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.NumberFormat
import java.util.Locale

import android.text.Editable
import android.text.TextWatcher
import com.example.kadaliv2.domain.model.Room

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private val viewModel: DashboardViewModel by viewModel()
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var roomAdapter: RoomAdapter

    // Sort & search state
    private var sortDescending = true   // true = Newest first
    private var searchQuery = ""
    private var allRooms: List<Room> = emptyList()

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

        // Sort toggle
        binding.btnSortRooms.setOnClickListener {
            sortDescending = !sortDescending
            binding.btnSortRooms.text = if (sortDescending) "Newest" else "Oldest"
            applyRoomFilter()
        }

        // Search
        binding.etSearchRooms.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                applyRoomFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
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
                        allRooms = rooms
                        applyRoomFilter()
                    }
                }
                launch {
                    viewModel.dashboardState.collect { state ->
                        updateDashboard(state)
                        updateVisibility(state.isRoomListEmpty)
                        roomAdapter.submitCosts(state.roomDailyCost)
                    }
                }
            }
        }
    }
    
    /** Filter by name search query and sort by createdAt, then submit to adapter. */
    private fun applyRoomFilter() {
        val filtered = allRooms
            .filter { it.name.contains(searchQuery, ignoreCase = true) }
            .let { list ->
                if (sortDescending) list.sortedByDescending { it.createdAt }
                else list.sortedBy { it.createdAt }
            }
        roomAdapter.submitList(filtered)
    }

    private fun updateVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.svContent.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.svContent.visibility = View.VISIBLE
        }
    }

    private fun updateDashboard(state: DashboardViewModel.DashboardState) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        currencyFormat.maximumFractionDigits = 0
        
        binding.tvMonthlyCost.text = currencyFormat.format(state.monthlyCost)
        
        // ---------- Simple Bar Chart: total energy per device type across all rooms ----------
        val totals = state.deviceTypeTotals

        if (totals.isNotEmpty()) {
            val colorPrimary  = requireContext().getColor(R.color.primary)
            val colorTextMed  = requireContext().getColor(R.color.text_medium)
            val colorTextHigh = requireContext().getColor(R.color.text_high)
            val colorBorder   = requireContext().getColor(R.color.border_navy)

            val labels = totals.map { it.first }
            val entries = totals.mapIndexed { idx, (_, energy) ->
                com.github.mikephil.charting.data.BarEntry(idx.toFloat(), energy.toFloat())
            }

            val dataSet = com.github.mikephil.charting.data.BarDataSet(entries, "kWh / day").apply {
                color = colorPrimary
                valueTextColor = colorTextHigh
                valueTextSize = 9f
                setDrawValues(true)
            }

            val barData = com.github.mikephil.charting.data.BarData(dataSet)
            barData.barWidth = 0.55f

            binding.barChart.apply {
                data = barData

                xAxis.apply {
                    valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val idx = value.toInt()
                            val label = labels.getOrNull(idx) ?: ""
                            return if (label.length > 9) label.take(8) + "…" else label
                        }
                    }
                    position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                    setCenterAxisLabels(false)
                    axisMinimum = -0.5f
                    axisMaximum = totals.size - 0.5f
                    granularity = 1f
                    textColor = colorTextMed
                    textSize = 11f
                    setDrawGridLines(false)
                    setDrawAxisLine(true)
                    axisLineColor = colorBorder
                }

                axisLeft.apply {
                    textColor = colorTextMed
                    setDrawAxisLine(false)
                    setDrawGridLines(true)
                    gridColor = android.graphics.Color.argb(50, 100, 120, 150)
                    axisMinimum = 0f
                }
                axisRight.isEnabled = false

                legend.isEnabled = false
                setDrawGridBackground(false)
                setDrawBorders(false)
                description.isEnabled = false
                setScaleEnabled(false)
                setPinchZoom(false)

                invalidate()
                animateY(500)
            }
        } else {
            binding.barChart.clear()
            binding.barChart.setNoDataTextColor(requireContext().getColor(R.color.text_medium))
            binding.barChart.setNoDataText("Add rooms & devices to see consumption chart")
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
