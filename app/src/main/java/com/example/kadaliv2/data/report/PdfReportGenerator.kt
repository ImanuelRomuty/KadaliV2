package com.example.kadaliv2.data.report

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.res.ResourcesCompat
import com.example.kadaliv2.R
import com.example.kadaliv2.domain.model.DeviceReportItem
import com.example.kadaliv2.domain.model.ReportData
import com.example.kadaliv2.domain.model.RoomReportItem
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfReportGenerator(private val context: Context) {

    private val pageWidth = 595
    private val pageHeight = 842
    private val xMargin = 50f
    private var yPos = 50f
    private var pageNumber = 1
    private lateinit var pdfDocument: PdfDocument
    private var currentPage: PdfDocument.Page? = null
    private var canvas: Canvas? = null

    // Paints
    private val titlePaint = Paint().apply {
        color = Color.BLACK
        textSize = 18f
        isFakeBoldText = true
    }
    private val sectionHeaderPaint = Paint().apply {
        color = Color.BLACK
        textSize = 14f
        isFakeBoldText = true
    }
    private val bodyPaint = Paint().apply {
        color = Color.BLACK
        textSize = 10f
    }
    private val bodyBoldPaint = Paint().apply {
        color = Color.BLACK
        textSize = 10f
        isFakeBoldText = true
    }
    private val footerPaint = Paint().apply {
        color = Color.GRAY
        textSize = 8f
        textAlign = Paint.Align.CENTER
    }
    private val linePaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 1f
    }
    private val secondaryTextPaint = Paint().apply {
        color = Color.DKGRAY
        textSize = 10f
    }

    fun generateReport(reportData: ReportData): String? {
        pdfDocument = PdfDocument()
        currentPage = null
        pageNumber = 1
        startNewPage()

        try {
            // Apply Custom Fonts if available
            ResourcesCompat.getFont(context, R.font.montserrat_bold)?.let { titlePaint.typeface = it; sectionHeaderPaint.typeface = it }
            ResourcesCompat.getFont(context, R.font.roboto_regular)?.let { bodyPaint.typeface = it; secondaryTextPaint.typeface = it }
            ResourcesCompat.getFont(context, R.font.roboto_bold)?.let { bodyBoldPaint.typeface = it }

            // Section A: REPORT INFORMATION
            drawSectionHeader("A. REPORT INFORMATION")
            drawTextLine("Report Generated On : ${SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault()).format(Date(reportData.generatedDate))}")
            drawTextLine("Reporting Period     : Monthly Estimate")
            drawTextLine("Office / Location    : ${reportData.officeName}")
            drawTextLine("Prepared By          : Kadali System")
            drawTextLine("This report provides an estimated analysis of electrical energy consumption based on recorded devices, usage duration, and configured electricity tariff.", isSecondary = true)
            drawSeparator()

            // Section B: TARIFF CONFIGURATION
            drawSectionHeader("B. TARIFF CONFIGURATION")
            drawTextLine("Electricity Tariff (per kWh) : ${String.format("%.0f", reportData.tariffStart)} ${reportData.currency}")
            drawTextLine("All calculations in this report are derived using the tariff above.", isSecondary = true)
            drawTextLine("Values represent analytical estimates intended for monitoring and internal planning.", isSecondary = true)
            drawSeparator()

            // Section C: OVERALL ENERGY SUMMARY
            drawSectionHeader("C. OVERALL ENERGY SUMMARY")
            drawTwoColumn("Total Rooms Monitored", "${reportData.totalRooms}")
            drawTwoColumn("Total Devices Recorded", "${reportData.totalDevices}")
            yPos += 10f
            drawTwoColumn("Total Estimated Energy Usage", "${String.format("%.2f", reportData.totalEnergy)} kWh")
            drawTwoColumn("Total Estimated Cost", "${String.format("%.0f", reportData.totalCostMonthly)} ${reportData.currency}")
            yPos += 10f
            drawTwoColumn("Average Daily Cost", "${String.format("%.0f", reportData.avgDailyCost)} ${reportData.currency}")
            drawTwoColumn("Projected Monthly Cost", "${String.format("%.0f", reportData.totalCostMonthly)} ${reportData.currency}")
            drawTwoColumn("Projected Yearly Cost", "${String.format("%.0f", reportData.yearlyCost)} ${reportData.currency}")
            drawSeparator()

            // Section D: OFFICE ROOM DIRECTORY
            drawSectionHeader("D. OFFICE ROOM DIRECTORY")
            drawTextLine("The following table lists all monitored rooms within the office.", isSecondary = true)
            yPos += 10f
            drawRoomTable(reportData)
            drawSeparator()

            // Section E: DEVICE INVENTORY PER ROOM
            checkPageOverflow(100f)
            drawSectionHeader("E. DEVICE INVENTORY PER ROOM")
            drawTextLine("Detailed list of electrical devices grouped by room.", isSecondary = true)
            reportData.roomBreakdown.forEach { room ->
                yPos += 10f
                drawTextLine("ROOM : ${room.roomName}", isBold = true)
                drawDeviceInventoryTable(room)
                yPos += 10f
            }
            drawSeparator()

            // Section F: HIGHEST ENERGY CONSUMPTION
            checkPageOverflow(80f)
            drawSectionHeader("F. HIGHEST ENERGY CONSUMPTION")
            drawTwoColumn("Room with Highest Usage", reportData.mostConsumingRoom)
            val topRoom = reportData.roomBreakdown.find { it.roomName == reportData.mostConsumingRoom }
            drawTwoColumn("Estimated Energy", "${String.format("%.2f", topRoom?.energy ?: 0.0)} kWh")
            drawTwoColumn("Estimated Cost", "${String.format("%.0f", topRoom?.cost ?: 0.0)} ${reportData.currency}")
            drawSeparator()

            // Section G: CALCULATION METHOD
            checkPageOverflow(100f)
            drawSectionHeader("G. CALCULATION METHOD")
            drawTextLine("Energy Consumption (kWh):", isBold = true)
            drawTextLine("Power (Watt) × Usage Hours × Quantity ÷ 1000")
            yPos += 5f
            drawTextLine("Cost Estimation:", isBold = true)
            drawTextLine("Energy (kWh) × Tariff per kWh")
            drawSeparator()

            // Section H: TECHNICAL DEVICE ANALYSIS (DETAILED)
            drawSectionHeader("H. TECHNICAL DEVICE ANALYSIS (DETAILED)")
            reportData.roomBreakdown.forEach { room ->
                yPos += 10f
                drawTextLine("ROOM : ${room.roomName}", isBold = true)
                room.devices.forEach { device ->
                    checkPageOverflow(150f)
                    drawTextLine("Device Name            : ${device.deviceName}")
                    drawTextLine("Power Rating           : ${String.format("%.0f", device.power)} Watt")
                    drawTextLine("Quantity Installed     : ${device.quantity} Unit(s)")
                    drawTextLine("Average Usage Duration : ${String.format("%.1f", device.hours)} Hour(s) / Day")
                    yPos += 5f
                    drawTextLine("Connected Load =", isBold = true)
                    drawTextLine("${String.format("%.0f", device.power)} × ${device.quantity} = ${String.format("%.0f", device.connectedLoad)} Watt")
                    drawTextLine("Daily Energy =", isBold = true)
                    drawTextLine("${String.format("%.0f", device.connectedLoad)} × ${String.format("%.1f", device.hours)} ÷ 1000 = ${String.format("%.2f", device.dailyEnergy)} kWh")
                    drawTextLine("Monthly Energy =", isBold = true)
                    drawTextLine("${String.format("%.2f", device.dailyEnergy)} × 30 = ${String.format("%.2f", device.monthlyEnergy)} kWh")
                    drawTextLine("Yearly Energy =", isBold = true)
                    drawTextLine("${String.format("%.2f", device.dailyEnergy)} × 365 = ${String.format("%.2f", device.yearlyEnergy)} kWh")
                    drawTextLine("Daily Cost =", isBold = true)
                    drawTextLine("${String.format("%.2f", device.dailyEnergy)} × ${String.format("%.0f", reportData.tariffStart)} = ${String.format("%.0f", device.dailyCost)} ${reportData.currency}")
                    drawSeparator(isLight = true)
                }
            }
            drawSeparator()

            // Section I: LOAD CLASSIFICATION REFERENCE
            checkPageOverflow(120f)
            drawSectionHeader("I. LOAD CLASSIFICATION REFERENCE")
            drawTwoColumn("Low Load Device", "< 200 Watt")
            drawTwoColumn("Medium Load Device", "200 – 800 Watt")
            drawTwoColumn("High Load Device", "800 – 2000 Watt")
            drawTwoColumn("Critical Load Device", "> 2000 Watt")
            drawSeparator()

            // Section J: ENERGY MANAGEMENT RECOMMENDATIONS
            checkPageOverflow(100f)
            drawSectionHeader("J. ENERGY MANAGEMENT RECOMMENDATIONS")
            drawTextLine("Typical optimization strategies:", isBold = true)
            drawTextLine("• Reduce unnecessary operating hours")
            drawTextLine("• Replace inefficient equipment")
            drawTextLine("• Monitor high-consumption rooms regularly")
            drawTextLine("• Implement scheduled shutdown practices")
            drawSeparator()

            // Section K: INTERPRETATION GUIDELINES
            checkPageOverflow(100f)
            drawSectionHeader("K. INTERPRETATION GUIDELINES")
            drawTextLine("• Values represent estimated operational consumption.")
            drawTextLine("• Useful for identifying energy-intensive equipment or spaces.")
            drawTextLine("• Supports budgeting, planning, and efficiency initiatives.")
            drawTextLine("• Should be reviewed periodically for trend analysis.")
            drawSeparator()

            // Section L: DISCLAIMER
            checkPageOverflow(80f)
            drawSectionHeader("L. DISCLAIMER")
            drawTextLine("All values are engineering estimates derived from declared wattage and usage duration.")
            drawTextLine("Actual consumption may vary depending on equipment efficiency and environmental conditions.")
            drawTextLine("This report is intended for internal evaluation and planning purposes only.", isBold = true)

            currentPage?.let { pdfDocument.finishPage(it) }
            return savePdfFile()

        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            return null
        }
    }

    private fun startNewPage() {
        currentPage?.let {
            pdfDocument.finishPage(it)
        }
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create()
        currentPage = pdfDocument.startPage(pageInfo)
        canvas = currentPage?.canvas
        yPos = 50f
        
        // Header watermark/header
        canvas?.drawText("KADALI – ELECTRICAL USAGE ANALYSIS REPORT", pageWidth / 2f, 25f, footerPaint)
    }

    private fun checkPageOverflow(requiredHeight: Float) {
        if (yPos + requiredHeight > pageHeight - 50f) {
            startNewPage()
        }
    }

    private fun drawSectionHeader(header: String) {
        checkPageOverflow(30f)
        yPos += 10f
        canvas?.drawText(header, xMargin, yPos, sectionHeaderPaint)
        yPos += 20f
    }

    private fun drawTextLine(text: String, isBold: Boolean = false, isSecondary: Boolean = false) {
        val paint = if (isBold) bodyBoldPaint else if (isSecondary) secondaryTextPaint else bodyPaint
        val textLines = wrapText(text, pageWidth - 2 * xMargin, paint)
        textLines.forEach { line ->
            checkPageOverflow(15f)
            canvas?.drawText(line, xMargin, yPos, paint)
            yPos += 15f
        }
    }

    private fun drawTwoColumn(key: String, value: String) {
        checkPageOverflow(15f)
        canvas?.drawText(key, xMargin, yPos, bodyPaint)
        canvas?.drawText(value, pageWidth - xMargin, yPos, bodyBoldPaint.apply { textAlign = Paint.Align.RIGHT })
        bodyBoldPaint.textAlign = Paint.Align.LEFT // Reset
        yPos += 15f
    }

    private fun drawSeparator(isLight: Boolean = false) {
        yPos += 5f
        linePaint.alpha = if (isLight) 50 else 255
        canvas?.drawLine(xMargin, yPos, pageWidth - xMargin, yPos, linePaint)
        yPos += 15f
    }

    private fun drawRoomTable(reportData: ReportData) {
        checkPageOverflow(50f)
        val colWidths = floatArrayOf(30f, 150f, 120f, 60f, 80f, 80f)
        val headers = arrayOf("No", "Room Name", "Description", "Dev", "Energy", "Cost")
        
        var currentX = xMargin
        headers.forEachIndexed { i, h ->
            canvas?.drawText(h, currentX, yPos, bodyBoldPaint)
            currentX += colWidths[i]
        }
        yPos += 15f
        canvas?.drawLine(xMargin, yPos - 12f, pageWidth - xMargin, yPos - 12f, linePaint.apply { strokeWidth = 0.5f })

        reportData.roomBreakdown.forEachIndexed { index, room ->
            checkPageOverflow(15f)
            currentX = xMargin
            canvas?.drawText("${index + 1}", currentX, yPos, bodyPaint); currentX += colWidths[0]
            canvas?.drawText(room.roomName.take(20), currentX, yPos, bodyPaint); currentX += colWidths[1]
            canvas?.drawText(room.roomDescription.take(15), currentX, yPos, bodyPaint); currentX += colWidths[2]
            canvas?.drawText("${room.deviceCount}", currentX, yPos, bodyPaint); currentX += colWidths[3]
            canvas?.drawText("${String.format("%.1f", room.energy)}", currentX, yPos, bodyPaint); currentX += colWidths[4]
            canvas?.drawText("${String.format("%.0f", room.cost)}", currentX, yPos, bodyPaint)
            yPos += 15f
        }
    }

    private fun drawDeviceInventoryTable(room: RoomReportItem) {
        checkPageOverflow(40f)
        val colWidths = floatArrayOf(25f, 140f, 60f, 40f, 60f, 80f, 80f)
        val headers = arrayOf("No", "Device Name", "Watt", "Qty", "Hrs", "Energy", "Cost")
        
        var currentX = xMargin
        headers.forEachIndexed { i, h ->
            canvas?.drawText(h, currentX, yPos, bodyBoldPaint.apply { textSize = 8f })
            currentX += colWidths[i]
        }
        bodyBoldPaint.textSize = 10f // reset
        yPos += 12f
        
        room.devices.forEachIndexed { index, device ->
            checkPageOverflow(15f)
            currentX = xMargin
            val p = bodyPaint.apply { textSize = 8f }
            canvas?.drawText("${index + 1}", currentX, yPos, p); currentX += colWidths[0]
            canvas?.drawText(device.deviceName.take(25), currentX, yPos, p); currentX += colWidths[1]
            canvas?.drawText("${String.format("%.0f", device.power)}", currentX, yPos, p); currentX += colWidths[2]
            canvas?.drawText("${device.quantity}", currentX, yPos, p); currentX += colWidths[3]
            canvas?.drawText("${String.format("%.1f", device.hours)}", currentX, yPos, p); currentX += colWidths[4]
            canvas?.drawText("${String.format("%.2f", device.dailyEnergy)}", currentX, yPos, p); currentX += colWidths[5]
            canvas?.drawText("${String.format("%.0f", device.dailyCost)}", currentX, yPos, p)
            yPos += 12f
        }
        bodyPaint.textSize = 10f // reset
    }

    private fun wrapText(text: String, width: Float, paint: Paint): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= width) {
                currentLine = testLine
            } else {
                lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)
        return lines
    }

    private fun savePdfFile(): String? {
        val fileName = "Kadali_Energy_Analysis_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.pdf"
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/KadaliReports/")
                }
                val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }
                    pdfDocument.close()
                    return "Documents/KadaliReports/$fileName"
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        pdfDocument.close()
        return null
    }
}

