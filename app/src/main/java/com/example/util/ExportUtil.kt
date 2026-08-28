package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.example.data.entity.Shipment
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtil {

    /**
     * Export shipments to an Excel (.xlsx) file.
     * Returns the file path if successful, or null if it failed.
     */
    fun exportToExcel(context: Context, shipments: List<Shipment>, fileName: String = "shipments"): String? {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Shipments")

            // Create header style
            val headerStyle = workbook.createCellStyle().apply {
                fillForegroundColor = IndexedColors.DARK_BLUE.index
                fillPattern = FillPatternType.SOLID_FOREGROUND
                alignment = HorizontalAlignment.CENTER
                borderBottom = BorderStyle.THIN
                borderTop = BorderStyle.THIN
                borderLeft = BorderStyle.THIN
                borderRight = BorderStyle.THIN
            }
            val headerFont = workbook.createFont().apply {
                color = IndexedColors.WHITE.index
                bold = true
                fontHeightInPoints = 12
            }
            headerStyle.setFont(headerFont)

            // Create cell style for data
            val dataStyle = workbook.createCellStyle().apply {
                borderBottom = BorderStyle.THIN
                borderTop = BorderStyle.THIN
                borderLeft = BorderStyle.THIN
                borderRight = BorderStyle.THIN
                alignment = HorizontalAlignment.CENTER
            }

            // Header row
            val headers = listOf(
                "ID", "Cargo Description", "Sender", "Receiver",
                "Sent By", "Jalali Date", "Notes", "Status", "Created At"
            )
            val headerRow = sheet.createRow(0)
            headers.forEachIndexed { index, title ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(title)
                cell.cellStyle = headerStyle
            }

            // Data rows
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            shipments.forEachIndexed { rowIndex, shipment ->
                val row = sheet.createRow(rowIndex + 1)

                row.createCell(0).apply {
                    setCellValue(shipment.id.toDouble())
                    cellStyle = dataStyle
                }
                row.createCell(1).apply {
                    setCellValue(shipment.cargoDescription)
                    cellStyle = dataStyle
                }
                row.createCell(2).apply {
                    setCellValue(shipment.senderName)
                    cellStyle = dataStyle
                }
                row.createCell(3).apply {
                    setCellValue(shipment.receiverName)
                    cellStyle = dataStyle
                }
                row.createCell(4).apply {
                    setCellValue(shipment.sentBy)
                    cellStyle = dataStyle
                }
                row.createCell(5).apply {
                    setCellValue("${shipment.jalaliYear}/${shipment.jalaliMonth}/${shipment.jalaliDay}")
                    cellStyle = dataStyle
                }
                row.createCell(6).apply {
                    setCellValue(shipment.notes)
                    cellStyle = dataStyle
                }
                row.createCell(7).apply {
                    setCellValue(shipment.status)
                    cellStyle = dataStyle
                }
                row.createCell(8).apply {
                    setCellValue(dateFormat.format(Date(shipment.createdAt)))
                    cellStyle = dataStyle
                }
            }

            // Auto-size columns
            for (i in headers.indices) {
                sheet.setColumnWidth(i, 4000)
            }

            // Save file
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (dir != null && !dir.exists()) dir.mkdirs()
            val file = File(dir, "${fileName}_${System.currentTimeMillis()}.xlsx")
            FileOutputStream(file).use { workbook.write(it) }
            workbook.close()

            Toast.makeText(context, "Excel file saved: ${file.name}", Toast.LENGTH_LONG).show()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to export Excel: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    /**
     * Export shipments to a PDF file.
     * Returns the file path if successful, or null if it failed.
     */
    fun exportToPdf(context: Context, shipments: List<Shipment>, fileName: String = "shipments"): String? {
        return try {
            val document = PdfDocument()

            // Page dimensions (A4-ish: 595 x 842 points)
            val pageWidth = 595
            val pageHeight = 842
            val margin = 40
            val lineHeight = 20
            val headerHeight = 60

            // Paints
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
            var pageInfo: PdfDocument.PageInfo
            var page: PdfDocument.Page
            var canvas: Canvas
            var yPos = 0

            fun createNewPage(): PdfDocument.Page {
                pageNum++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                val newPage = document.startPage(pageInfo)
                canvas = newPage.canvas
                // Title
                canvas.drawText("Cargo Shipments Report", margin.toFloat(), (margin + 20).toFloat(), titlePaint)
                canvas.drawText("Page $pageNum", (pageWidth - margin - 60).toFloat(), (margin + 20).toFloat(), titlePaint)
                yPos = margin + headerHeight

                // Table header
                canvas.drawRect(
                    margin.toFloat(), yPos.toFloat(),
                    (pageWidth - margin).toFloat(), (yPos + lineHeight).toFloat(),
                    headerBgPaint
                )
                val colX = intArrayOf(margin + 5, margin + 40, margin + 160, margin + 260, margin + 360, margin + 430, margin + 490)
                val colHeaders = listOf("ID", "Cargo", "Sender", "Receiver", "Date", "Status", "Notes")
                colHeaders.forEachIndexed { i, h ->
                    canvas.drawText(h, colX[i].toFloat(), (yPos + lineHeight - 5).toFloat(), headerPaint)
                }
                yPos += lineHeight
                return newPage
            }

            page = createNewPage()

            // Draw each shipment
            for (shipment in shipments) {
                if (yPos + lineHeight * 2 > pageHeight - margin) {
                    document.finishPage(page)
                    page = createNewPage()
                }

                // Row background (alternating)
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
                canvas.drawText(
                    shipment.cargoDescription.take(18),
                    colX(1).toFloat(), textY.toFloat(), cellPaint
                )
                canvas.drawText(
                    shipment.senderName.take(14),
                    colX(2).toFloat(), textY.toFloat(), cellPaint
                )
                canvas.drawText(
                    shipment.receiverName.take(14),
                    colX(3).toFloat(), textY.toFloat(), cellPaint
                )
                canvas.drawText(
                    "${shipment.jalaliYear}/${shipment.jalaliMonth}/${shipment.jalaliDay}",
                    colX(4).toFloat(), textY.toFloat(), cellPaint
                )

                // Status with color
                statusPaint.color = when (shipment.status) {
                    Shipment.STATUS_DELIVERED -> Color.parseColor("#2E7D32")
                    Shipment.STATUS_RETURNED -> Color.parseColor("#C62828")
                    else -> Color.parseColor("#1565C0")
                }
                canvas.drawText(
                    shipment.status,
                    colX(5).toFloat(), textY.toFloat(), statusPaint
                )
                canvas.drawText(
                    shipment.notes.take(10),
                    colX(6).toFloat(), textY.toFloat(), cellPaint
                )

                // Separator line
                canvas.drawLine(
                    margin.toFloat(), (yPos + lineHeight).toFloat(),
                    (pageWidth - margin).toFloat(), (yPos + lineHeight).toFloat(),
                    linePaint
                )
                yPos += lineHeight
            }

            document.finishPage(page)

            // Save file
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
            2 -> margin + 160
            3 -> margin + 260
            4 -> margin + 360
            5 -> margin + 430
            6 -> margin + 490
            else -> margin
        }
    }
}
