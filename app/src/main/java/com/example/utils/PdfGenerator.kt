package com.example.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.data.GuestEntry
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {
    fun generateMonthlyStatementPdf(
        context: Context,
        yearMonth: String, // e.g. "2026-11"
        entries: List<GuestEntry>,
        businessName: String,
        location: String
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size: 595 x 842 pt
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            isFakeBoldText = true
        }
        val subTitlePaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
        }
        val headerPaint = Paint().apply {
            color = Color.rgb(40, 53, 147)
            textSize = 10f
            isFakeBoldText = true
        }
        val rowPaint = Paint().apply {
            color = Color.BLACK
            textSize = 9f
        }
        val linePaint = Paint().apply {
            color = Color.GRAY
            strokeWidth = 1f
        }

        var yPos = 50f
        val leftMargin = 40f
        val rightMargin = 555f

        // Header
        canvas.drawText(businessName, leftMargin, yPos, titlePaint)
        yPos += 20f
        canvas.drawText("Location: $location", leftMargin, yPos, subTitlePaint)
        yPos += 20f
        
        val parsedMonth = try {
            val inputFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val dateObj = inputFormat.parse(yearMonth)
            if (dateObj != null) outputFormat.format(dateObj) else yearMonth
        } catch (e: Exception) {
            yearMonth
        }

        canvas.drawText("Monthly Statement: $parsedMonth", leftMargin, yPos, subTitlePaint)
        yPos += 15f
        canvas.drawText("Generated on: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}", leftMargin, yPos, subTitlePaint)
        yPos += 25f

        // Divider line
        canvas.drawLine(leftMargin, yPos, rightMargin, yPos, linePaint)
        yPos += 20f

        // Summary box
        val totalAc = entries.sumOf { it.ac }
        val totalFood = entries.sumOf { it.food }
        val totalExtra = entries.sumOf { it.extra }
        val grandTotal = entries.sumOf { it.total }

        canvas.drawText("Summary Totals:", leftMargin, yPos, headerPaint)
        yPos += 15f
        canvas.drawText("Total Entries: ${entries.size}   |   A/C: $totalAc   |   Food: $totalFood   |   Extra: $totalExtra", leftMargin, yPos, rowPaint)
        yPos += 18f
        canvas.drawText("Grand Total Amount Received: $grandTotal", leftMargin, yPos, titlePaint)
        yPos += 30f

        // Table Header
        canvas.drawText("Date", leftMargin, yPos, headerPaint)
        canvas.drawText("Name", leftMargin + 70f, yPos, headerPaint)
        canvas.drawText("Phone", leftMargin + 170f, yPos, headerPaint)
        canvas.drawText("RS", leftMargin + 260f, yPos, headerPaint)
        canvas.drawText("A/C", leftMargin + 310f, yPos, headerPaint)
        canvas.drawText("Food", leftMargin + 360f, yPos, headerPaint)
        canvas.drawText("Extra", leftMargin + 410f, yPos, headerPaint)
        canvas.drawText("Total", leftMargin + 460f, yPos, headerPaint)
        yPos += 8f
        canvas.drawLine(leftMargin, yPos, rightMargin, yPos, linePaint)
        yPos += 18f

        // Table Rows
        for (entry in entries) {
            if (yPos > 780f) {
                // If page overflows, for simplicity we stop or handle multi-page. A4 fits ~25 rows comfortably.
                break
            }
            canvas.drawText(entry.date, leftMargin, yPos, rowPaint)
            canvas.drawText(entry.name.take(15), leftMargin + 70f, yPos, rowPaint)
            canvas.drawText(entry.phoneNumber.take(12), leftMargin + 170f, yPos, rowPaint)
            canvas.drawText(entry.rsNo.take(6), leftMargin + 260f, yPos, rowPaint)
            canvas.drawText(if (entry.ac > 0) entry.ac.toString() else "-", leftMargin + 310f, yPos, rowPaint)
            canvas.drawText(if (entry.food > 0) entry.food.toString() else "-", leftMargin + 360f, yPos, rowPaint)
            canvas.drawText(if (entry.extra > 0) entry.extra.toString() else "-", leftMargin + 410f, yPos, rowPaint)
            canvas.drawText(entry.total.toString(), leftMargin + 460f, yPos, rowPaint)

            yPos += 18f
        }

        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "Guest_Statement_$yearMonth.pdf")
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }
}
