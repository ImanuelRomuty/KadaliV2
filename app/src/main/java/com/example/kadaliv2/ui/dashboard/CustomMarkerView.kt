package com.example.kadaliv2.ui.dashboard

import android.content.Context
import android.widget.TextView
import com.example.kadaliv2.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.NumberFormat
import java.util.Locale

class CustomMarkerView(context: Context, layoutResource: Int, private val labels: List<String>) : MarkerView(context, layoutResource) {

    private val tvMarkerLabel: TextView = findViewById(R.id.tvMarkerLabel)
    
    private val numberFormat = NumberFormat.getNumberInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 2
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e != null) {
            val idx = e.x.toInt()
            val label = labels.getOrNull(idx) ?: "Unknown"
            val valueStr = numberFormat.format(e.y)
            tvMarkerLabel.text = "$label\n$valueStr kWh/hari"
        }
        super.refreshContent(e, highlight)
    }

    private var mOffset: MPPointF? = null

    override fun getOffset(): MPPointF {
        if (mOffset == null) {
            mOffset = MPPointF(-(width / 2).toFloat(), -height.toFloat() - 10f)
        }
        return mOffset!!
    }
}
