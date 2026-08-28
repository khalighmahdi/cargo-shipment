package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.example.data.entity.Shipment
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtil {

    /**
     * Export shipments to a CSV file.
     * Returns the file path if successful, or null if it failed.
     */
    fun exportToExcel(context: Context, shipments: List<Shipment>, fileName: String = "shipments"): String? {
        return try {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (dir != null && !dir.exists()) dir.mkdirs()
            val file = File(dir, "${fileName}_${System.currentTimeMillis()}.csv")

            FileWriter(file).use { writer ->
                // BOM for Excel UTF-8 detection
                writer.write("\uFEFF")

                // Header
                writer.write("ID,Cargo Description,Sender,Receiver,Destination,Sent By,Jalali Date,Notes,Status,Created At\n")

                // Data rows
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                shipments.forEach { shipment ->
                    val row = listOf(
                        shipment.id.toString(),
                        shipment.cargoDescription.csvEscape(),
                        shipment.senderName.csvEscape(),
                        shipment.receiverName.csvEscape(),
                        shipment.destination.csvEscape(),
                        shipment.sentBy.csvEscape(),
                        "${shipment.jalaliYear}/${shipment.jalaliMonth}/${shipment.jalaliDay}",
                        shipment.notes.csvEscape(),
                        shipment.status.csvEscape(),
                        dateFormat.format(Date(shipment.createdAt))
                    ).joinToString(",")
                    writer.write("$row\n")
                }
            }

            Toast.makeText(context, "CSV file saved: ${file.name}", Toast.LENGTH_LONG).show()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to export: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    private fun String.csvEscape(): String {
        return if (contains(",") || contains("\"") || contains("\n")) {
            "\"${replace("\"", "\"\"")}\""
        } else {
            this
        }
    }

    /**
     * Export shipments to a PDF file.
     * Returns the file path if successful, or null if it failed.
     */
    fun exportToPdf(context: Context, shipments: List<Shipment>, fileName: String = "shipments"): String? {
        return try {
            val document = PdfDocument()

            val pageWidth = 595
            val pageHeight = 842
            val margin = 40
            val lineHeight = 20
            val headerHeight = 60

            val titlePaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 18f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val headerPaint = Paint().apply {
                color = Color.WHITE
                textSize = 11f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val headerBgPaint = Paint().apply {
                color = Color.parseColor("#1A237E")
                style = Paint.Style.FILL
            }
            val cellPaint = Paint().apply {
                color = Color.BLACK
                textSize = 9f
                isAntiAlias = true
            }
            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 0.5f
            }
            val statusPaint = Paint().apply {
                textSize = 9f
                isAntiAlias = true
            }

            var pageNum = 0
            var page: PdfDocument.Page
            var canvas: Canvas
            var yPos = 0

            fun createNewPage(): PdfDocument.Page {
                pageNum++
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                val newPage = document.startPage(pageInfo)
                canvas = newPage.canvas
                canvas.drawText("Cargo Shipments Report", margin.toFloat(), (margin + 20).toFloat(), titlePaint)
                canvas.drawText("Page $pageNum", (pageWidth - margin - 60).toFloat(), (margin + 20).toFloat(), titlePaint)
                yPos = margin + headerHeight

                canvas.drawRect(
                    margin.toFloat(), yPos.toFloat(),
                    (pageWidth - margin).toFloat(), (yPos + lineHeight).toFloat(),
                    headerBgPaint
                )
                val colX = intArrayOf(margin + 5, margin + 40, margin + 140, margin + 220, margin + 300, margin + 380, margin + 440, margin + 490)
                val colHeaders = listOf("ID", "Cargo", "Sender", "Receiver", "Dest.", "Date", "Status", "Notes")
                colHeaders.forEachIndexed { i, h ->
                    canvas.drawText(h, colX[i].toFloat(), (yPos + lineHeight - 5).toFloat(), headerPaint)
                }
                yPos += lineHeight
                return newPage
            }

            page = createNewPage()

            for (shipment in shipments) {
                if (yPos + lineHeight * 2 > pageHeight - margin) {
                    document.finishPage(page)
                    page = createNewPage()
                }

                if ((shipment.id % 2) == 0) {
                    val altPaint = Paint().apply { color = Color.parseColor("#F5F5F5"); style = Paint.Style.FILL }
                    canvas.drawRect(
                        margin.toFloat(), yPos.toFloat(),
                        (pageWidth - margin).toFloat(), (yPos + lineHeight).toFloat(),
                        altPaint
                    )
                }

                val textY = yPos + lineHeight - 5
                canvas.drawText("${shipment.id}", colX(0).toFloat(), textY.toFloat(), cellPaint)
                canvas.drawText(shipment.cargoDescription.take(18), colX(1).toFloat(), textY.toFloat(), cellPaint)
                canvas.drawText(shipment.senderName.take(14), colX(2).toFloat(), textY.toFloat(), cellPaint)
                canvas.drawText(shipment.receiverName.take(14), colX(3).toFloat(), textY.toFloat(), cellPaint)
                canvas.drawText(shipment.destination.take(10), colX(4).toFloat(), textY.toFloat(), cellPaint)
                canvas.drawText(
                    "${shipment.jalaliYear}/${shipment.jalaliMonth}/${shipment.jalaliDay}",
                    colX(5).toFloat(), textY.toFloat(), cellPaint
                )

                statusPaint.color = when (shipment.status) {
                    Shipment.STATUS_DELIVERED -> Color.parseColor("#2E7D32")
                    Shipment.STATUS_RETURNED -> Color.parseColor("#C62828")
                    else -> Color.parseColor("#1565C0")
                }
                canvas.drawText(shipment.status, colX(6).toFloat(), textY.toFloat(), statusPaint)
                canvas.drawText(shipment.notes.take(10), colX(7).toFloat(), textY.toFloat(), cellPaint)

                canvas.drawLine(
                    margin.toFloat(), (yPos + lineHeight).toFloat(),
                    (pageWidth - margin).toFloat(), (yPos + lineHeight).toFloat(),
                    linePaint
                )
                yPos += lineHeight
            }

            document.finishPage(page)

            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (dir != null && !dir.exists()) dir.mkdirs()
            val file = File(dir, "${fileName}_${System.currentTimeMillis()}.pdf")
            FileOutputStream(file).use { document.writeTo(it) }
            document.close()

            Toast.makeText(context, "PDF file saved: ${file.name}", Toast.LENGTH_LONG).show()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to export PDF: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    private fun colX(index: Int): Int {
        val margin = 40
        return when (index) {
            0 -> margin + 5
            1 -> margin + 40
            2 -> margin + 140
            3 -> margin + 220
            4 -> margin + 300
            5 -> margin + 380
            6 -> margin + 440
            7 -> margin + 490
            else -> margin
        }
    }
}
