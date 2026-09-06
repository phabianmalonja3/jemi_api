package com.jemigraph.jemigraph_backend.utils;

import com.jemigraph.jemigraph_backend.Entities.Payment;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class BulkReceiptGenerator   {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_ONLY =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Maximum rows per page
    private static final int MAX_ROWS_PER_PAGE = 15;

    public static byte[] generateBulkReceiptBytes(
            List<Payment> payments,
            String userName,
            String userEmail,
            BigDecimal totalAmount,
            String logoPath) throws IOException {

        try (PDDocument document = new PDDocument()) {

            // Calculate how many pages we need
            int totalPayments = payments.size();
            int totalPages = (int) Math.ceil((double) totalPayments / MAX_ROWS_PER_PAGE);

            // If no payments, create at least one page with empty table
            if (totalPages == 0) {
                totalPages = 1;
            }

            // Process each page
            for (int pageNum = 0; pageNum < totalPages; pageNum++) {
                int startIndex = pageNum * MAX_ROWS_PER_PAGE;
                int endIndex = Math.min(startIndex + MAX_ROWS_PER_PAGE, totalPayments);
                List<Payment> pagePayments = payments.subList(startIndex, endIndex);

                // Create new page
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                // Build content for this page
                buildPageContent(
                        document,
                        page,
                        pagePayments,
                        userName,
                        userEmail,
                        totalAmount,
                        logoPath,
                        pageNum + 1,
                        totalPages,
                        startIndex
                );
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private static void buildPageContent(
            PDDocument document,
            PDPage page,
            List<Payment> payments,
            String userName,
            String userEmail,
            BigDecimal totalAmount,
            String logoPath,
            int pageNumber,
            int totalPages,
            int startIndex) throws IOException {

        PDType1Font helveticaBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font helvetica = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        float margin = 50;
        float yPosition = page.getMediaBox().getHeight() - margin;

        // Create content stream for this page (no append mode needed for new pages)
        try (PDPageContentStream cs = new PDPageContentStream(document, page)) {

            // ============================================================
            // 1. LOGO
            // ============================================================
            if (logoPath != null && !logoPath.isEmpty()) {
                try {
                    File logoFile = new File(logoPath);
                    if (logoFile.exists()) {
                        PDImageXObject logo = PDImageXObject.createFromFile(logoPath, document);
                        float logoWidth = 80;
                        float logoHeight = 40;
                        cs.drawImage(logo, margin, yPosition - logoHeight, logoWidth, logoHeight);
                        yPosition -= logoHeight + 15;
                    }
                } catch (Exception e) {
                    System.err.println("Logo loading failed: " + e.getMessage());
                    yPosition -= 20;
                }
            } else {
                yPosition -= 20;
            }

            // ============================================================
            // 2. HEADER
            // ============================================================
            cs.beginText();
            cs.setFont(helveticaBold, 24);
            cs.setNonStrokingColor(new Color(16, 124, 65));
            cs.newLineAtOffset(margin + 100, yPosition);
            cs.showText("JEMI GRAPHER");
            cs.endText();

            yPosition -= 30;

            cs.beginText();
            cs.setFont(helvetica, 12);
            cs.setNonStrokingColor(new Color(100, 116, 139));
            cs.newLineAtOffset(margin + 100, yPosition);
            cs.showText("Professional Photography Services");
            cs.endText();

            yPosition -= 40;

            cs.beginText();
            cs.setFont(helveticaBold, 18);
            cs.setNonStrokingColor(new Color(15, 23, 42));
            cs.newLineAtOffset(margin, yPosition);
            cs.showText("PAYMENT HISTORY REPORT");
            cs.endText();

            yPosition -= 25;

            // Page info
            cs.beginText();
            cs.setFont(helvetica, 10);
            cs.setNonStrokingColor(new Color(100, 116, 139));
            String pageInfo = "Page " + pageNumber + " of " + totalPages;
            float pageInfoWidth = helvetica.getStringWidth(pageInfo) * 10 / 1000f;
            cs.newLineAtOffset(page.getMediaBox().getWidth() - margin - pageInfoWidth, yPosition);
            cs.showText(pageInfo);
            cs.endText();

            yPosition -= 25;

            // Date generated
            cs.beginText();
            cs.setFont(helvetica, 10);
            cs.setNonStrokingColor(new Color(100, 116, 139));
            String generatedDate = "Generated: " + LocalDateTime.now().format(DATE_FORMATTER);
            float dateWidth = helvetica.getStringWidth(generatedDate) * 10 / 1000f;
            cs.newLineAtOffset(page.getMediaBox().getWidth() - margin - dateWidth, yPosition);
            cs.showText(generatedDate);
            cs.endText();

            yPosition -= 30;

            // Separator
            cs.setStrokingColor(new Color(203, 213, 225));
            cs.setLineWidth(1f);
            cs.moveTo(margin, yPosition);
            cs.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
            cs.stroke();

            yPosition -= 20;

            // ============================================================
            // 3. CUSTOMER INFORMATION
            // ============================================================
            cs.beginText();
            cs.setFont(helveticaBold, 12);
            cs.setNonStrokingColor(new Color(15, 23, 42));
            cs.newLineAtOffset(margin, yPosition);
            cs.showText("Customer Information");
            cs.endText();

            yPosition -= 18;

            cs.beginText();
            cs.setFont(helvetica, 10);
            cs.setNonStrokingColor(new Color(51, 65, 85));
            cs.newLineAtOffset(margin + 10, yPosition);
            cs.showText("Name: " + (userName != null ? userName : "N/A"));
            cs.endText();

            yPosition -= 16;

            cs.beginText();
            cs.setFont(helvetica, 10);
            cs.setNonStrokingColor(new Color(51, 65, 85));
            cs.newLineAtOffset(margin + 10, yPosition);
            cs.showText("Email: " + (userEmail != null ? userEmail : "N/A"));
            cs.endText();

            yPosition -= 16;

            cs.beginText();
            cs.setFont(helvetica, 10);
            cs.setNonStrokingColor(new Color(51, 65, 85));
            cs.newLineAtOffset(margin + 10, yPosition);
            cs.showText("Total Payments: " + payments.size());
            cs.endText();

            yPosition -= 16;

            // Total Amount
            cs.beginText();
            cs.setFont(helveticaBold, 11);
            cs.setNonStrokingColor(new Color(16, 124, 65));
            cs.newLineAtOffset(margin + 10, yPosition);
            String totalStr = totalAmount != null ?
                    String.format("%,.2f", totalAmount) : "0.00";
            cs.showText("Total Amount: TZS " + totalStr);
            cs.endText();

            yPosition -= 25;

            // ============================================================
            // 4. TABLE HEADER
            // ============================================================
            float[] columnWidths = {30, 80, 90, 70, 80, 60, 70};
            float tableWidth = page.getMediaBox().getWidth() - (2 * margin);
            float[] xPositions = new float[columnWidths.length];
            float currentX = margin;
            for (int i = 0; i < columnWidths.length; i++) {
                xPositions[i] = currentX;
                currentX += (tableWidth * columnWidths[i] / 500);
            }

            // Table header background
            cs.setNonStrokingColor(new Color(16, 124, 65));
            cs.addRect(margin, yPosition - 25, tableWidth, 25);
            cs.fill();

            String[] headers = {"#", "Order ID", "Date", "Plan", "Amount", "Status", "Payment"};
            cs.setNonStrokingColor(Color.WHITE);
            cs.setFont(helveticaBold, 10);

            for (int i = 0; i < headers.length; i++) {
                float textWidth = helveticaBold.getStringWidth(headers[i]) * 10 / 1000f;
                float cellWidth = tableWidth * columnWidths[i] / 500;
                float centerX = xPositions[i] + (cellWidth - textWidth) / 2;
                cs.beginText();
                cs.newLineAtOffset(centerX, yPosition - 17);
                cs.showText(headers[i]);
                cs.endText();
            }

            yPosition -= 25;

            // ============================================================
            // 5. TABLE DATA
            // ============================================================
            float rowHeight = 20;
            int rowNumber = startIndex + 1;

            for (Payment payment : payments) {
                // Check if we have space for this row
                if (yPosition < 50) {
                    // We've run out of space - but we shouldn't get here
                    // since we pre-calculated rows per page
                    break;
                }

                // Alternating row colors
                if (rowNumber % 2 == 0) {
                    cs.setNonStrokingColor(new Color(248, 250, 252));
                    cs.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
                    cs.fill();
                }

                // Row data with null safety
                String orderId = payment.getOrderId() != null ?
                        payment.getOrderId().substring(0, Math.min(8, payment.getOrderId().length())) : "N/A";
                String dateStr = payment.getCreatedAt() != null ?
                        payment.getCreatedAt().format(DATE_ONLY) : "N/A";
                String planName = payment.getPlanId() != null ?
                        payment.getPlanId().toString() : "N/A";
                String amountStr = payment.getAmount() != null ?
                        String.format("%,.0f", payment.getAmount()) : "0";
                String status = payment.getStatus() != null ?
                        payment.getStatus().toString() : "N/A";
                Color statusColor = getStatusColor(status);
                String provider = payment.getProvider() != null ?
                        payment.getProvider() : "N/A";

                String[] rowData = {
                        String.valueOf(rowNumber),
                        orderId,
                        dateStr,
                        planName,
                        amountStr,
                        status,
                        provider
                };

                cs.setFont(helvetica, 9);

                for (int i = 0; i < rowData.length; i++) {
                    float textWidth = helvetica.getStringWidth(rowData[i]) * 9 / 1000f;
                    float cellWidth = tableWidth * columnWidths[i] / 500;
                    float centerX = xPositions[i] + (cellWidth - textWidth) / 2;

                    if (i == 5) {
                        cs.setNonStrokingColor(statusColor);
                    } else {
                        cs.setNonStrokingColor(new Color(15, 23, 42));
                    }

                    cs.beginText();
                    cs.newLineAtOffset(centerX, yPosition - 14);
                    cs.showText(rowData[i]);
                    cs.endText();
                }

                yPosition -= rowHeight;
                rowNumber++;
            }

            // ============================================================
            // 6. FOOTER
            // ============================================================
            addFooter(document, page, payments.size(), totalAmount, pageNumber);
        }
    }

    private static void addFooter(
            PDDocument document,
            PDPage page,
            int totalPayments,
            BigDecimal totalAmount,
            int pageNumber) throws IOException {

        PDType1Font helveticaBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font helvetica = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        // Use APPEND mode for footer (adds to existing content)
        try (PDPageContentStream cs = new PDPageContentStream(
                document, page, PDPageContentStream.AppendMode.APPEND, true)) {

            float pageWidth = page.getMediaBox().getWidth();
            float margin = 50;
            float yPosition = 50;

            // Summary line
            cs.setStrokingColor(new Color(203, 213, 225));
            cs.setLineWidth(1f);
            cs.moveTo(margin, yPosition + 20);
            cs.lineTo(pageWidth - margin, yPosition + 20);
            cs.stroke();

            cs.beginText();
            cs.setFont(helveticaBold, 10);
            cs.setNonStrokingColor(new Color(15, 23, 42));
            cs.newLineAtOffset(margin, yPosition + 5);
            String totalStr = totalAmount != null ?
                    String.format("%,.2f", totalAmount) : "0.00";
            cs.showText("SUMMARY: Total Payments = " + totalPayments +
                    " | Total Amount = TZS " + totalStr);
            cs.endText();

            // Thank you message
            cs.beginText();
            cs.setFont(helvetica, 8);
            cs.setNonStrokingColor(new Color(148, 163, 184));
            String thankYou = "Thank you for using JEMI GRAPHER Services!";
            float tw = helvetica.getStringWidth(thankYou) * 8 / 1000f;
            cs.newLineAtOffset((pageWidth - tw) / 2, 30);
            cs.showText(thankYou);
            cs.endText();

            // Page number
            cs.beginText();
            cs.setFont(helvetica, 8);
            cs.setNonStrokingColor(new Color(148, 163, 184));
            String pageText = "Page " + pageNumber;
            float pw = helvetica.getStringWidth(pageText) * 8 / 1000f;
            cs.newLineAtOffset(pageWidth - margin - pw, yPosition + 5);
            cs.showText(pageText);
            cs.endText();
        }
    }

    private static Color getStatusColor(String status) {
        if (status == null) return new Color(15, 23, 42);

        switch (status.toUpperCase()) {
            case "SUCCESS":
            case "COMPLETED":
            case "PAID":
                return new Color(22, 163, 74);
            case "PENDING":
                return new Color(234, 179, 8);
            case "FAILED":
            case "CANCELLED":
                return new Color(220, 38, 38);
            default:
                return new Color(15, 23, 42);
        }
    }
}