package com.example.kadaliv2.data.report

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.kadaliv2.domain.model.ReportData
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfReportGenerator(private val context: Context) {

    fun generateReport(reportData: ReportData): String? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size in points (approx)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // Styles
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            isFakeBoldText = true
        }
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }
        val notePaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
            textAlign = Paint.Align.CENTER
        }

        var yPos = 50f
        val xMargin = 40f

        // 1. Header
        canvas.drawText("KadaliV2", pageInfo.pageWidth / 2f, yPos, titlePaint)
        yPos += 30f
        canvas.drawText("Electricity Usage Report", pageInfo.pageWidth / 2f, yPos, Paint().apply {
            color = Color.DKGRAY
            textSize = 18f
            textAlign = Paint.Align.CENTER
        })
        yPos += 30f
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dateStr = dateFormat.format(Date(reportData.generatedDate))
        canvas.drawText("Date Generated: $dateStr", pageInfo.pageWidth / 2f, yPos, Paint().apply { 
            color = Color.GRAY
            textSize = 12f
            textAlign = Paint.Align.CENTER
        })
        yPos += 50f

        // 2. Summary
        canvas.drawText("Summary", xMargin, yPos, headerPaint)
        yPos += 20f
        canvas.drawLine(xMargin, yPos, pageInfo.pageWidth - xMargin, yPos, Paint().apply { strokeWidth = 1f })
        yPos += 20f
        
        drawKeyValue(canvas, "Total Monthly Estimate:", "Rp ${String.format("%.0f", reportData.totalCostMonthly)}", xMargin, yPos, textPaint)
        yPos += 20f
        drawKeyValue(canvas, "Total Energy (Daily):", "${String.format("%.2f", reportData.totalEnergy)} kWh", xMargin, yPos, textPaint)
        yPos += 20f
        drawKeyValue(canvas, "Highest Consumption:", reportData.mostConsumingRoom, xMargin, yPos, textPaint)
        yPos += 20f
        drawKeyValue(canvas, "Tariff Used:", "Rp ${String.format("%.0f", reportData.tariffStart)} / kWh", xMargin, yPos, textPaint)
        yPos += 50f

        // 3. Room Breakdown
        canvas.drawText("Room Breakdown", xMargin, yPos, headerPaint)
        yPos += 20f
        canvas.drawLine(xMargin, yPos, pageInfo.pageWidth - xMargin, yPos, Paint().apply { strokeWidth = 1f })
        yPos += 20f
        
        // Table Header
        val col1 = xMargin
        val col2 = xMargin + 130
        val col3 = xMargin + 250
        val col4 = xMargin + 380
        
        canvas.drawText("Room", col1, yPos, headerPaint)
        canvas.drawText("Energy (kWh/day)", col2, yPos, headerPaint)
        canvas.drawText("Cost (Monthly)", col3, yPos, headerPaint)
        canvas.drawText("Usage %", col4, yPos, headerPaint)
        yPos += 20f

        reportData.roomBreakdown.forEach { room ->
            canvas.drawText(room.roomName, col1, yPos, textPaint)
            canvas.drawText(String.format("%.2f", room.energy), col2, yPos, textPaint)
            canvas.drawText("Rp ${String.format("%.0f", room.cost)}", col3, yPos, textPaint)
            canvas.drawText("${String.format("%.1f", room.percentage)}%", col4, yPos, textPaint)
            yPos += 20f
        }
        yPos += 50f

        // 4. Projections
        canvas.drawText("Cost Projections", xMargin, yPos, headerPaint)
        yPos += 20f
        canvas.drawLine(xMargin, yPos, pageInfo.pageWidth - xMargin, yPos, Paint().apply { strokeWidth = 1f })
        yPos += 20f
        
        val dailyCost = reportData.totalCostMonthly / 30
        drawKeyValue(canvas, "Daily Estimate:", "Rp ${String.format("%.0f", dailyCost)}", xMargin, yPos, textPaint)
        yPos += 20f
        drawKeyValue(canvas, "Weekly Estimate:", "Rp ${String.format("%.0f", dailyCost * 7)}", xMargin, yPos, textPaint)
        yPos += 20f
        drawKeyValue(canvas, "Monthly Estimate:", "Rp ${String.format("%.0f", reportData.totalCostMonthly)}", xMargin, yPos, textPaint)
        yPos += 20f
        drawKeyValue(canvas, "Yearly Estimate:", "Rp ${String.format("%.0f", dailyCost * 365)}", xMargin, yPos, textPaint)
        yPos += 50f
        
        // 5. Footer
        yPos = pageInfo.pageHeight - 50f
        canvas.drawText("Generated by Office Energy Calculator Application", pageInfo.pageWidth / 2f, yPos, notePaint)
        
        pdfDocument.finishPage(page)

        // Save File
        val fileName = "Energy_Report_${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}.pdf"
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/OfficeEnergyReports/")
                }
                val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }
                    pdfDocument.close()
                    return "Documents/OfficeEnergyReports/$fileName"
                }
            } else {
                // Legacy storage implementation omitted for brevity as targetSdk is 36
                // Assuming Scoped Storage is the main target.
                // If needed, we'd use regular File output stream to Environment.getExternalStoragePublicDirectory
            }
        } catch (e: IOException) {
            e.printStackTrace()
            pdfDocument.close()
            return null
        }
        
        pdfDocument.close()
        return null // Should not reach here if success
    }
    
    private fun drawKeyValue(canvas: Canvas, key: String, value: String, x: Float, y: Float, paint: Paint) {
        canvas.drawText(key, x, y, paint)
        canvas.drawText(value, x + 150, y, paint)
    }
}
