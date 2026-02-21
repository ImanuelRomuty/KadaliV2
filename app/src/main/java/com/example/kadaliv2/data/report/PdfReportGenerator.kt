package com.example.kadaliv2.data.report

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.res.ResourcesCompat
import com.example.kadaliv2.R
import com.example.kadaliv2.domain.model.DeviceReportItem
import com.example.kadaliv2.domain.model.ReportData
import com.example.kadaliv2.domain.model.RoomReportItem
import java.io.IOException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfReportGenerator(private val context: Context) {

    /** Available after a successful generateReport() call – use for share intent. */
    var lastSavedUri: Uri? = null
        private set

    // ── Page constants ────────────────────────────────────────────────────────
    private val pageWidth  = 595
    private val pageHeight = 842
    private val xMargin    = 50f
    private var yPos       = 50f
    private var pageNumber = 1
    private lateinit var pdfDocument: PdfDocument
    private var currentPage: PdfDocument.Page? = null
    private var canvas: Canvas? = null

    // ── Theme colors ──────────────────────────────────────────────────────────
    private val colorNavy      = Color.parseColor("#0A1221")
    private val colorPrimary   = Color.parseColor("#FFC107")
    private val colorSectionBg = Color.parseColor("#1E293B")
    private val colorBody      = Color.parseColor("#1A1A1A")
    private val colorSecondary = Color.parseColor("#64748B")
    private val colorRowAlt    = Color.parseColor("#F1F5F9")
    private val colorWhite     = Color.WHITE

    // ── Reusable Paints ───────────────────────────────────────────────────────
    private val titlePaint = Paint().apply { color = colorWhite; textSize = 28f; isFakeBoldText = true }
    private val subtitlePaint = Paint().apply { color = colorPrimary; textSize = 13f }
    private val sectionHeaderPaint = Paint().apply { color = colorWhite; textSize = 11f; isFakeBoldText = true }
    private val bodyPaint = Paint().apply { color = colorBody; textSize = 10f }
    private val bodyBoldPaint = Paint().apply { color = colorBody; textSize = 10f; isFakeBoldText = true }
    private val secondaryPaint = Paint().apply { color = colorSecondary; textSize = 9f }
    private val footerPaint = Paint().apply { color = colorSecondary; textSize = 7f; textAlign = Paint.Align.CENTER }
    private val linePaint = Paint().apply { color = Color.parseColor("#CBD5E1"); strokeWidth = 0.5f; style = Paint.Style.STROKE }
    private val sectionBgPaint = Paint().apply { color = colorSectionBg; style = Paint.Style.FILL }
    private val tableHeaderBgPaint = Paint().apply { color = colorNavy; style = Paint.Style.FILL }
    private val tableHeaderTextPaint = Paint().apply { color = colorWhite; textSize = 8f; isFakeBoldText = true }
    private val rowAltPaint = Paint().apply { color = colorRowAlt; style = Paint.Style.FILL }
    private val accentFillPaint = Paint().apply { color = colorPrimary; style = Paint.Style.FILL }

    // ── Currency helper ───────────────────────────────────────────────────────
    private fun formatRp(value: Double): String {
        val fmt = NumberFormat.getNumberInstance(Locale("id", "ID"))
        fmt.maximumFractionDigits = 0
        return "Rp ${fmt.format(value.toLong())}"
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PUBLIC ENTRY POINT
    // ═════════════════════════════════════════════════════════════════════════
    fun generateReport(reportData: ReportData): String? {
        pdfDocument = PdfDocument()
        currentPage = null
        pageNumber  = 1
        lastSavedUri = null

        try {
            // Load custom fonts
            ResourcesCompat.getFont(context, R.font.montserrat_bold)?.let {
                titlePaint.typeface = it
                sectionHeaderPaint.typeface = it
                bodyBoldPaint.typeface = it
                tableHeaderTextPaint.typeface = it
            }
            ResourcesCompat.getFont(context, R.font.roboto_regular)?.let {
                bodyPaint.typeface = it
                secondaryPaint.typeface = it
                footerPaint.typeface = it
            }
            ResourcesCompat.getFont(context, R.font.roboto_bold)?.let {
                subtitlePaint.typeface = it
            }

            drawCoverPage(reportData)
            startNewPage()

            // A – Report Information
            drawSectionHeader("A. REPORT INFORMATION")
            drawKeyValue("Report Generated", SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date(reportData.generatedDate)))
            drawKeyValue("Reporting Period", "Monthly Estimate")
            drawKeyValue("Office / Location", reportData.officeName.ifBlank { "—" })
            drawKeyValue("Prepared By", "Kadali System")
            drawNote("Estimated electrical energy analysis based on recorded devices, usage duration, and configured tariff.")
            drawDivider()

            // B – Tariff
            drawSectionHeader("B. TARIFF CONFIGURATION")
            drawKeyValue("Electricity Tariff", "${formatRp(reportData.tariffStart)} / kWh")
            drawNote("All cost calculations in this report use the tariff above.")
            drawDivider()

            // C – Summary Table
            drawSectionHeader("C. OVERALL ENERGY SUMMARY")
            drawSummaryTable(reportData)
            drawDivider()

            // D – Room Directory
            drawSectionHeader("D. OFFICE ROOM DIRECTORY")
            drawNote("All monitored rooms with energy and cost estimates.")
            yPos += 6f
            drawRoomTable(reportData)
            drawDivider()

            // E – Device Inventory
            checkPageOverflow(80f)
            drawSectionHeader("E. DEVICE INVENTORY PER ROOM")
            drawNote("Detailed list of devices grouped by room.")
            reportData.roomBreakdown.forEach { room ->
                yPos += 8f
                drawRoomSubHeader(room.roomName)
                drawDeviceInventoryTable(room)
                yPos += 6f
            }
            drawDivider()

            // F – Top 5 Devices
            checkPageOverflow(80f)
            drawSectionHeader("F. TOP 5 HIGHEST ENERGY DEVICES")
            drawNote("Devices consuming the most electricity across all rooms (daily basis).")
            yPos += 6f
            val top5 = reportData.roomBreakdown
                .flatMap { room -> room.devices.map { room.roomName to it } }
                .sortedByDescending { it.second.dailyEnergy }
                .take(5)
            drawTop5DevicesTable(top5, reportData)
            drawDivider()

            // G – Top Room
            checkPageOverflow(60f)
            drawSectionHeader("G. HIGHEST ENERGY CONSUMPTION ROOM")
            val topRoom = reportData.roomBreakdown.find { it.roomName == reportData.mostConsumingRoom }
            drawKeyValue("Room", reportData.mostConsumingRoom)
            drawKeyValue("Estimated Monthly Energy", "${String.format("%.2f", topRoom?.energy ?: 0.0)} kWh")
            drawKeyValue("Estimated Monthly Cost",   formatRp(topRoom?.cost ?: 0.0))
            drawKeyValue("Share of Total",           "${String.format("%.1f", topRoom?.percentage ?: 0.0)}%")
            drawDivider()

            // H – Technical Analysis
            checkPageOverflow(80f)
            drawSectionHeader("H. TECHNICAL DEVICE ANALYSIS")
            reportData.roomBreakdown.forEach { room ->
                yPos += 8f
                drawRoomSubHeader(room.roomName)
                room.devices.forEach { device ->
                    checkPageOverflow(100f)
                    drawDeviceAnalysisCard(device, reportData)
                    yPos += 4f
                }
            }
            drawDivider()

            // I – Load Classification
            checkPageOverflow(100f)
            drawSectionHeader("I. LOAD CLASSIFICATION REFERENCE")
            drawLoadClassificationTable()
            drawDivider()

            // J – Dynamic Recommendations
            checkPageOverflow(100f)
            drawSectionHeader("J. ENERGY MANAGEMENT RECOMMENDATIONS")
            drawDynamicRecommendations(reportData)
            drawDivider()

            // K – Interpretation
            checkPageOverflow(80f)
            drawSectionHeader("K. INTERPRETATION GUIDELINES")
            listOf(
                "Values represent estimated operational consumption.",
                "Useful for identifying energy-intensive equipment or spaces.",
                "Supports budgeting, planning, and efficiency initiatives.",
                "Should be reviewed periodically for trend analysis."
            ).forEach { drawBullet(it) }
            drawDivider()

            // L – Disclaimer
            checkPageOverflow(60f)
            drawSectionHeader("L. DISCLAIMER")
            drawNote("All values are engineering estimates derived from declared wattage and usage duration. Actual consumption may vary depending on equipment efficiency and environmental conditions.")
            drawTextLine("This report is intended for internal evaluation and planning purposes only.", isBold = true)

            currentPage?.let { pdfDocument.finishPage(it) }
            return savePdfFile()

        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            return null
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // COVER PAGE
    // ═════════════════════════════════════════════════════════════════════════
    private fun drawCoverPage(reportData: ReportData) {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create()
        val page = pdfDocument.startPage(pageInfo)
        val c = page.canvas

        // Navy background
        c.drawColor(colorNavy)

        // Top accent bar
        c.drawRect(0f, 0f, pageWidth.toFloat(), 8f, accentFillPaint)

        // Logo badge
        val logoBg = Paint().apply { color = colorPrimary; style = Paint.Style.FILL }
        c.drawRoundRect(RectF(xMargin, 110f, xMargin + 64f, 174f), 10f, 10f, logoBg)
        val logoTxt = Paint().apply { color = colorNavy; textSize = 26f; isFakeBoldText = true; textAlign = Paint.Align.CENTER }
        c.drawText("K", xMargin + 32f, 154f, logoTxt)

        // App title
        c.drawText("KADALI", xMargin, 220f, titlePaint)
        c.drawText("Electrical Usage Analysis Report", xMargin, 242f, subtitlePaint)

        // Accent divider
        c.drawLine(xMargin, 256f, pageWidth - xMargin, 256f, Paint().apply { color = colorPrimary; strokeWidth = 2f })

        // Info labels
        val infoLabel = Paint().apply { color = Color.parseColor("#94A3B8"); textSize = 8f }
        val infoValue = Paint().apply { color = colorWhite; textSize = 12f; isFakeBoldText = true }

        fun infoBlock(label: String, value: String, x: Float, y: Float) {
            c.drawText(label, x, y, infoLabel)
            c.drawText(value, x, y + 18f, infoValue)
        }

        infoBlock("OFFICE / LOCATION", reportData.officeName.ifBlank { "—" }, xMargin, 295f)
        infoBlock("GENERATED ON", SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date(reportData.generatedDate)), xMargin, 345f)
        infoBlock("TOTAL ROOMS",    "${reportData.totalRooms}",   xMargin,         395f)
        infoBlock("TOTAL DEVICES",  "${reportData.totalDevices}", xMargin + 180f,  395f)
        infoBlock("ELECTRICITY TARIFF", "${formatRp(reportData.tariffStart)} / kWh", xMargin, 445f)

        // Stats banner
        val bannerBg = Paint().apply { color = colorSectionBg; style = Paint.Style.FILL }
        c.drawRoundRect(RectF(xMargin - 10f, 490f, pageWidth - xMargin + 10f, 592f), 8f, 8f, bannerBg)

        val statLabel = Paint().apply { color = Color.parseColor("#94A3B8"); textSize = 8f; textAlign = Paint.Align.CENTER }
        val statValue = Paint().apply { color = colorPrimary; textSize = 15f; isFakeBoldText = true; textAlign = Paint.Align.CENTER }

        val col1 = xMargin + 65f
        val col2 = pageWidth / 2f
        val col3 = pageWidth - xMargin - 65f

        c.drawText("MONTHLY COST",  col1, 520f, statLabel); c.drawText(formatRp(reportData.totalCostMonthly), col1, 548f, statValue)
        c.drawText("DAILY ENERGY",  col2, 520f, statLabel); c.drawText("${String.format("%.1f", reportData.totalEnergy)} kWh", col2, 548f, statValue)
        c.drawText("YEARLY COST",   col3, 520f, statLabel); c.drawText(formatRp(reportData.yearlyCost), col3, 548f, statValue)

        // Footer
        val footerLine = Paint().apply { color = colorSectionBg; strokeWidth = 1f }
        c.drawLine(0f, pageHeight - 55f, pageWidth.toFloat(), pageHeight - 55f, footerLine)
        val footerTxt = Paint().apply { color = Color.parseColor("#64748B"); textSize = 7f; textAlign = Paint.Align.CENTER }
        c.drawText("Generated by Kadali  ·  Internal Use Only", pageWidth / 2f, pageHeight - 32f, footerTxt)
        c.drawText("Values are estimates based on declared device specifications.", pageWidth / 2f, pageHeight - 18f, footerTxt)

        pdfDocument.finishPage(page)
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PAGE MANAGEMENT
    // ═════════════════════════════════════════════════════════════════════════
    private fun startNewPage() {
        currentPage?.let { pdfDocument.finishPage(it) }
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create()
        currentPage = pdfDocument.startPage(pageInfo)
        canvas = currentPage?.canvas
        yPos = 38f

        // Sticky header bar
        val c = canvas ?: return
        c.drawRect(0f, 0f, pageWidth.toFloat(), 22f, Paint().apply { color = colorNavy; style = Paint.Style.FILL })
        c.drawText("KADALI — ELECTRICAL USAGE ANALYSIS", xMargin, 14f,
            Paint().apply { color = colorPrimary; textSize = 7f; isFakeBoldText = true })
        c.drawText("Page ${pageNumber - 1}", pageWidth - xMargin, 14f,
            Paint().apply { color = Color.parseColor("#94A3B8"); textSize = 7f; textAlign = Paint.Align.RIGHT })
    }

    private fun checkPageOverflow(required: Float) {
        if (yPos + required > pageHeight - 40f) startNewPage()
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION PRIMITIVES
    // ═════════════════════════════════════════════════════════════════════════
    private fun drawSectionHeader(header: String) {
        checkPageOverflow(26f)
        yPos += 6f
        canvas?.drawRoundRect(RectF(xMargin - 5f, yPos - 13f, pageWidth - xMargin + 5f, yPos + 5f), 3f, 3f, sectionBgPaint)
        canvas?.drawText(header, xMargin, yPos, sectionHeaderPaint)
        yPos += 14f
    }

    private fun drawRoomSubHeader(name: String) {
        checkPageOverflow(18f)
        val subBg = Paint().apply { color = Color.parseColor("#E2E8F0"); style = Paint.Style.FILL }
        canvas?.drawRoundRect(RectF(xMargin - 5f, yPos - 11f, pageWidth - xMargin + 5f, yPos + 3f), 3f, 3f, subBg)
        val txt = Paint().apply { color = colorNavy; textSize = 9f; isFakeBoldText = true }
        canvas?.drawText("ROOM: $name", xMargin, yPos, txt)
        yPos += 12f
    }

    private fun drawKeyValue(key: String, value: String) {
        checkPageOverflow(14f)
        canvas?.drawText(key, xMargin, yPos, secondaryPaint)
        val vp = bodyBoldPaint.apply { textAlign = Paint.Align.RIGHT }
        canvas?.drawText(value, pageWidth - xMargin, yPos, vp)
        bodyBoldPaint.textAlign = Paint.Align.LEFT
        yPos += 14f
    }

    private fun drawNote(text: String) {
        yPos += 2f
        drawTextLine(text, isSecondary = true)
        yPos += 2f
    }

    private fun drawBullet(text: String) {
        val lines = wrapText("•  $text", pageWidth - 2 * xMargin - 10f, bodyPaint)
        lines.forEachIndexed { i, line ->
            checkPageOverflow(13f)
            canvas?.drawText(if (i == 0) line else "    $line", xMargin, yPos, bodyPaint)
            yPos += 13f
        }
    }

    private fun drawTextLine(text: String, isBold: Boolean = false, isSecondary: Boolean = false) {
        val paint = when { isBold -> bodyBoldPaint; isSecondary -> secondaryPaint; else -> bodyPaint }
        wrapText(text, pageWidth - 2 * xMargin, paint).forEach { line ->
            checkPageOverflow(13f)
            canvas?.drawText(line, xMargin, yPos, paint)
            yPos += 13f
        }
    }

    private fun drawDivider(isLight: Boolean = false) {
        yPos += 6f
        linePaint.alpha = if (isLight) 80 else 180
        canvas?.drawLine(xMargin, yPos, pageWidth - xMargin, yPos, linePaint)
        yPos += 10f
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TABLES
    // ═════════════════════════════════════════════════════════════════════════
    private fun drawSummaryTable(reportData: ReportData) {
        checkPageOverflow(130f)
        data class Row(val label: String, val value: String)
        val rows = listOf(
            Row("Total Rooms Monitored",   "${reportData.totalRooms}"),
            Row("Total Devices Recorded",  "${reportData.totalDevices}"),
            Row("Daily Energy",            "${String.format("%.2f", reportData.totalEnergy)} kWh"),
            Row("Weekly Energy",           "${String.format("%.2f", reportData.totalEnergy * 7)} kWh"),
            Row("Monthly Energy",          "${String.format("%.2f", reportData.totalEnergy * 30)} kWh"),
            Row("Daily Cost",              formatRp(reportData.avgDailyCost)),
            Row("Weekly Cost",             formatRp(reportData.weeklyCost)),
            Row("Monthly Cost",            formatRp(reportData.totalCostMonthly)),
            Row("Yearly Cost",             formatRp(reportData.yearlyCost)),
        )
        rows.forEachIndexed { idx, row ->
            checkPageOverflow(14f)
            if (idx % 2 == 0) canvas?.drawRect(RectF(xMargin - 5f, yPos - 11f, pageWidth - xMargin + 5f, yPos + 3f), rowAltPaint)
            canvas?.drawText(row.label, xMargin, yPos, bodyPaint)
            val vp = bodyBoldPaint.apply { textAlign = Paint.Align.RIGHT }
            canvas?.drawText(row.value, pageWidth - xMargin, yPos, vp)
            bodyBoldPaint.textAlign = Paint.Align.LEFT
            yPos += 14f
        }
    }

    private fun drawRoomTable(reportData: ReportData) {
        checkPageOverflow(50f)
        val c = canvas ?: return
        val cols = floatArrayOf(25f, 135f, 95f, 35f, 80f, 90f, 55f)
        val headers = arrayOf("No", "Room Name", "Description", "Dev", "Energy", "Cost", "Share")

        c.drawRect(RectF(xMargin - 5f, yPos - 11f, pageWidth - xMargin + 5f, yPos + 3f), tableHeaderBgPaint)
        var cx = xMargin
        headers.forEachIndexed { i, h -> c.drawText(h, cx, yPos, tableHeaderTextPaint); cx += cols[i] }
        yPos += 14f

        reportData.roomBreakdown.forEachIndexed { index, room ->
            checkPageOverflow(14f)
            if (index % 2 == 0) c.drawRect(RectF(xMargin - 5f, yPos - 11f, pageWidth - xMargin + 5f, yPos + 3f), rowAltPaint)
            cx = xMargin
            val p = bodyPaint.apply { textSize = 8f }
            c.drawText("${index + 1}",                      cx, yPos, p); cx += cols[0]
            c.drawText(room.roomName.take(22),              cx, yPos, p); cx += cols[1]
            c.drawText(room.roomDescription.take(17),       cx, yPos, p); cx += cols[2]
            c.drawText("${room.deviceCount}",               cx, yPos, p); cx += cols[3]
            c.drawText("${String.format("%.2f", room.energy)} kWh", cx, yPos, p); cx += cols[4]
            c.drawText(formatRp(room.cost),                 cx, yPos, p); cx += cols[5]
            c.drawText("${String.format("%.1f", room.percentage)}%", cx, yPos, p)
            yPos += 13f
        }
        bodyPaint.textSize = 10f
    }

    private fun drawDeviceInventoryTable(room: RoomReportItem) {
        checkPageOverflow(40f)
        val c = canvas ?: return
        val cols = floatArrayOf(22f, 128f, 55f, 35f, 52f, 68f, 85f)
        val headers = arrayOf("No", "Device Name", "Watt", "Qty", "Hrs/Day", "kWh/Day", "Cost/Day")

        c.drawRect(RectF(xMargin - 5f, yPos - 11f, pageWidth - xMargin + 5f, yPos + 3f), tableHeaderBgPaint)
        var cx = xMargin
        headers.forEachIndexed { i, h -> c.drawText(h, cx, yPos, tableHeaderTextPaint); cx += cols[i] }
        yPos += 13f

        room.devices.forEachIndexed { index, device ->
            checkPageOverflow(13f)
            if (index % 2 == 0) c.drawRect(RectF(xMargin - 5f, yPos - 10f, pageWidth - xMargin + 5f, yPos + 3f), rowAltPaint)
            val p = bodyPaint.apply { textSize = 8f }
            cx = xMargin
            c.drawText("${index + 1}",                           cx, yPos, p); cx += cols[0]
            c.drawText(device.deviceName.take(22),               cx, yPos, p); cx += cols[1]
            c.drawText("${String.format("%.0f", device.power)}", cx, yPos, p); cx += cols[2]
            c.drawText("${device.quantity}",                     cx, yPos, p); cx += cols[3]
            c.drawText("${String.format("%.1f", device.hours)}", cx, yPos, p); cx += cols[4]
            c.drawText("${String.format("%.3f", device.dailyEnergy)}", cx, yPos, p); cx += cols[5]
            c.drawText(formatRp(device.dailyCost),               cx, yPos, p)
            yPos += 12f
        }
        bodyPaint.textSize = 10f
    }

    private fun drawTop5DevicesTable(top5: List<Pair<String, DeviceReportItem>>, reportData: ReportData) {
        checkPageOverflow(60f)
        val c = canvas ?: return
        val cols = floatArrayOf(20f, 120f, 95f, 50f, 52f, 68f, 90f)
        val headers = arrayOf("#", "Device", "Room", "Watt", "Hrs/Day", "kWh/Day", "Cost/Day")

        c.drawRect(RectF(xMargin - 5f, yPos - 11f, pageWidth - xMargin + 5f, yPos + 3f), tableHeaderBgPaint)
        var cx = xMargin
        headers.forEachIndexed { i, h -> c.drawText(h, cx, yPos, tableHeaderTextPaint); cx += cols[i] }
        yPos += 13f

        top5.forEachIndexed { index, (roomName, device) ->
            checkPageOverflow(13f)
            if (index % 2 == 0) c.drawRect(RectF(xMargin - 5f, yPos - 10f, pageWidth - xMargin + 5f, yPos + 3f), rowAltPaint)
            val p = bodyPaint.apply { textSize = 8f }
            cx = xMargin
            c.drawText("${index + 1}",                           cx, yPos, p); cx += cols[0]
            c.drawText(device.deviceName.take(18),               cx, yPos, p); cx += cols[1]
            c.drawText(roomName.take(16),                        cx, yPos, p); cx += cols[2]
            c.drawText("${String.format("%.0f", device.power)}", cx, yPos, p); cx += cols[3]
            c.drawText("${String.format("%.1f", device.hours)}", cx, yPos, p); cx += cols[4]
            c.drawText("${String.format("%.3f", device.dailyEnergy)}", cx, yPos, p); cx += cols[5]
            c.drawText(formatRp(device.dailyCost),               cx, yPos, p)
            yPos += 12f
        }
        bodyPaint.textSize = 10f
    }

    private fun drawLoadClassificationTable() {
        val c = canvas ?: return
        data class LoadClass(val label: String, val range: String, val color: Int)
        val classes = listOf(
            LoadClass("Low Load",      "< 200 Watt",        Color.parseColor("#22C55E")),
            LoadClass("Medium Load",   "200 – 800 Watt",    Color.parseColor("#F59E0B")),
            LoadClass("High Load",     "800 – 2000 Watt",   Color.parseColor("#EF4444")),
            LoadClass("Critical Load", "> 2000 Watt",       Color.parseColor("#7C3AED")),
        )
        classes.forEachIndexed { idx, lc ->
            checkPageOverflow(14f)
            if (idx % 2 == 0) c.drawRect(RectF(xMargin - 5f, yPos - 11f, pageWidth - xMargin + 5f, yPos + 3f), rowAltPaint)
            val dot = Paint().apply { color = lc.color; style = Paint.Style.FILL }
            c.drawCircle(xMargin + 5f, yPos - 4f, 4f, dot)
            c.drawText(lc.label, xMargin + 15f, yPos, bodyPaint)
            val vp = bodyBoldPaint.apply { textAlign = Paint.Align.RIGHT }
            c.drawText(lc.range, pageWidth - xMargin, yPos, vp)
            bodyBoldPaint.textAlign = Paint.Align.LEFT
            yPos += 14f
        }
    }

    private fun drawDeviceAnalysisCard(device: DeviceReportItem, reportData: ReportData) {
        checkPageOverflow(95f)
        val c = canvas ?: return
        val top = yPos - 4f
        val cardBg = Paint().apply { color = Color.parseColor("#F8FAFC"); style = Paint.Style.FILL }
        c.drawRoundRect(RectF(xMargin - 5f, top, pageWidth - xMargin + 5f, top + 88f), 4f, 4f, cardBg)

        drawTextLine("${device.deviceName}  ·  ${String.format("%.0f", device.power)} W  ×  qty ${device.quantity}  ×  ${String.format("%.1f", device.hours)} h/day", isBold = true)
        drawTextLine("Connected Load: ${String.format("%.0f", device.connectedLoad)} W")

        val col = (pageWidth - 2 * xMargin - 10f) / 4f
        checkPageOverflow(28f)
        var x = xMargin
        val labelP = Paint().apply { color = colorSecondary; textSize = 7f }
        listOf("Daily", "Weekly", "Monthly", "Yearly").forEach { label -> c.drawText(label, x, yPos, labelP); x += col }
        yPos += 12f

        x = xMargin
        val bp = bodyBoldPaint.apply { textSize = 8f }
        listOf(device.dailyEnergy, device.dailyEnergy * 7, device.monthlyEnergy, device.yearlyEnergy).forEach { v ->
            c.drawText("${String.format("%.2f", v)} kWh", x, yPos, bp); x += col
        }
        yPos += 12f

        x = xMargin
        listOf(device.dailyCost, device.dailyCost * 7, device.dailyCost * 30, device.dailyCost * 365).forEach { v ->
            c.drawText(formatRp(v), x, yPos, bp); x += col
        }
        yPos += 14f

        bodyPaint.textSize = 10f
        bodyBoldPaint.textSize = 10f
    }

    private fun drawDynamicRecommendations(reportData: ReportData) {
        val recs = mutableListOf<String>()

        val criticalDevices = reportData.roomBreakdown.flatMap { it.devices }.filter { it.power > 2000 }
        if (criticalDevices.isNotEmpty())
            recs.add("⚠ ${criticalDevices.size} critical load device(s) detected (>2000 W). Evaluate necessity and usage schedule.")

        val topRoom = reportData.roomBreakdown.maxByOrNull { it.cost }
        if (topRoom != null)
            recs.add("Room '${topRoom.roomName}' is your highest-cost area (${formatRp(topRoom.cost)}/month). Prioritize efficiency measures there first.")

        val alwaysOnDevices = reportData.roomBreakdown.flatMap { it.devices }.filter { it.hours >= 22.0 }
        if (alwaysOnDevices.isNotEmpty())
            recs.add("${alwaysOnDevices.size} device(s) appear to run nearly 24 h/day. Consider automated shutdown schedules.")

        val highDeviceRoom = reportData.roomBreakdown.maxByOrNull { it.deviceCount }
        if (highDeviceRoom != null && highDeviceRoom.deviceCount >= 5)
            recs.add("Room '${highDeviceRoom.roomName}' has ${highDeviceRoom.deviceCount} devices. Review if all are operationally necessary.")

        recs.add("Replace inefficient equipment nearing end-of-life with energy-rated alternatives.")
        recs.add("Implement office-wide scheduled shutdown at closing time.")
        recs.add("Monitor the '${reportData.mostConsumingRoom}' room monthly for consumption trends.")

        recs.forEach { drawBullet(it) }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // UTILITIES
    // ═════════════════════════════════════════════════════════════════════════
    private fun wrapText(text: String, width: Float, paint: Paint): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= width) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
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
                    context.contentResolver.openOutputStream(uri)?.use { pdfDocument.writeTo(it) }
                    pdfDocument.close()
                    lastSavedUri = uri
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
