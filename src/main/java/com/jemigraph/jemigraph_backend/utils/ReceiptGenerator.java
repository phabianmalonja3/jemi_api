package com.jemigraph.jemigraph_backend.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReceiptGenerator {

    public static void generateReceipt(
            String filePath,
            String orderId,
            String amount,
            String phone,
            String receiptNumber,
            String logoPath,
            String planName,
            String startDate,
            String endDate,
            int durationInDays
    ) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDType1Font helveticaBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font helvetica = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();

                // 1. Logo kama Watermark
                if (logoPath != null && new File(logoPath).exists()) {
                    try {
                        PDImageXObject watermarkImage = PDImageXObject.createFromFile(logoPath, document);

                        cs.saveGraphicsState();
                        PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
                        gs.setNonStrokingAlphaConstant(0.12f);
                        cs.setGraphicsStateParameters(gs);

                        float imgWidth = 250;
                        float imgHeight = 250;
                        float imgX = (pageWidth - imgWidth) / 2;
                        float imgY = (pageHeight - imgHeight) / 2;

                        cs.drawImage(watermarkImage, imgX, imgY, imgWidth, imgHeight);
                        cs.restoreGraphicsState();
                    } catch (Exception e) {
                        System.err.println("Imeshindikana kupakia watermark logo: " + e.getMessage());
                    }
                }

                // 2. Border ya Nje
                cs.setStrokingColor(new Color(203, 213, 225));
                cs.setLineWidth(1.5f);
                cs.addRect(40, 40, pageWidth - 80, pageHeight - 80);
                cs.stroke();

                // 3. Header Banner
                cs.setNonStrokingColor(new Color(16, 124, 65));
                cs.addRect(41, pageHeight - 120, pageWidth - 82, 80);
                cs.fill();

                String headerText = "JEMIGRAPH RECEIPT";
                float fontSize = 22;
                float textWidth = helveticaBold.getStringWidth(headerText) * fontSize / 1000f;
                float centerX = 41 + ((pageWidth - 82) - textWidth) / 2;

                cs.beginText();
                cs.setNonStrokingColor(Color.WHITE);
                cs.setFont(helveticaBold, fontSize);
                cs.newLineAtOffset(centerX, pageHeight - 75);
                cs.showText(headerText);
                cs.endText();

                // 4. Section Title: Transaction Details
                cs.beginText();
                cs.setFont(helveticaBold, 14);
                cs.setNonStrokingColor(new Color(15, 23, 42));
                cs.newLineAtOffset(70, pageHeight - 155);
                cs.showText("Transaction Details");
                cs.endText();

                // Mstari chini ya kichwa cha habari
                cs.setStrokingColor(new Color(226, 232, 240));
                cs.setLineWidth(1f);
                cs.moveTo(70, pageHeight - 165);
                cs.lineTo(pageWidth - 70, pageHeight - 165);
                cs.stroke();

                // 5. Durations & Rows Data
                String durationText = formatDuration(durationInDays);

                String[][] rows = {
                        {"Order ID:", orderId},
                        {"Receipt Number:", receiptNumber},
                        {"Phone Number:", phone},
                        {"Plan:", planName + " (" + durationText + ")"},
                        {"Amount Paid:", "TZS " + amount},
                        {"Payment Status:", "SUCCESS"},
                        {"Start Date:", startDate},
                        {"Expiry Date:", endDate},
                        {"Date & Time:", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}
                };

                float startY = pageHeight - 195;
                float labelX = 70;
                float valueX = 250;
                float rowHeight = 24;

                for (int i = 0; i < rows.length; i++) {
                    // Label
                    cs.beginText();
                    cs.setFont(helveticaBold, 10);
                    cs.setNonStrokingColor(new Color(71, 85, 105));
                    cs.newLineAtOffset(labelX, startY - (i * rowHeight));
                    cs.showText(rows[i][0]);
                    cs.endText();

                    // Value
                    cs.beginText();
                    cs.setFont(helvetica, 10);

                    if (i == 4) { // Amount Paid
                        cs.setNonStrokingColor(new Color(16, 124, 65));
                    } else if (i == 5) { // Payment Status
                        cs.setNonStrokingColor(new Color(22, 163, 74));
                    } else {
                        cs.setNonStrokingColor(new Color(15, 23, 42));
                    }

                    cs.newLineAtOffset(valueX, startY - (i * rowHeight));
                    cs.showText(rows[i][1]);
                    cs.endText();
                }

                // 6. Subscription Summary Box
                float boxY = startY - (rows.length * rowHeight) - 10;
                float boxHeight = 80;
                float boxWidth = pageWidth - 140;
                float boxX = 70;

                // Box Background
                cs.setNonStrokingColor(new Color(240, 249, 255));
                cs.addRect(boxX, boxY - boxHeight, boxWidth, boxHeight);
                cs.fill();

                // Box Border
                cs.setStrokingColor(new Color(191, 219, 254));
                cs.setLineWidth(1f);
                cs.addRect(boxX, boxY - boxHeight, boxWidth, boxHeight);
                cs.stroke();

                // Box Title
                cs.beginText();
                cs.setFont(helveticaBold, 11);
                cs.setNonStrokingColor(new Color(30, 58, 138));
                cs.newLineAtOffset(boxX + 15, boxY - 20);
                cs.showText("Subscription Summary");
                cs.endText();

                // Box Details Columns
                cs.beginText();
                cs.setFont(helvetica, 9.5f);
                cs.setNonStrokingColor(new Color(30, 58, 138));
                cs.newLineAtOffset(boxX + 15, boxY - 38);
                cs.showText("Plan: " + planName);
                cs.endText();

                cs.beginText();
                cs.setFont(helvetica, 9.5f);
                cs.setNonStrokingColor(new Color(30, 58, 138));
                cs.newLineAtOffset(boxX + 200, boxY - 38);
                cs.showText("Duration: " + durationText);
                cs.endText();

                cs.beginText();
                cs.setFont(helvetica, 9.5f);
                cs.setNonStrokingColor(new Color(30, 58, 138));
                cs.newLineAtOffset(boxX + 15, boxY - 56);
                cs.showText("Valid From: " + startDate);
                cs.endText();

                cs.beginText();
                cs.setFont(helvetica, 9.5f);
                cs.setNonStrokingColor(new Color(30, 58, 138));
                cs.newLineAtOffset(boxX + 200, boxY - 56);
                cs.showText("Expires On: " + endDate);
                cs.endText();

                // 7. Status Badge Inside Box area right side
                String statusText = "ACTIVE";
                float statusSize = 11;
                float statusWidth = helveticaBold.getStringWidth(statusText) * statusSize / 1000f;
                float statusX = pageWidth - 70 - statusWidth - 15;

                cs.setNonStrokingColor(new Color(220, 252, 231));
                cs.addRect(statusX - 8, boxY - 26, statusWidth + 16, 20);
                cs.fill();

                cs.setStrokingColor(new Color(74, 222, 128));
                cs.setLineWidth(0.5f);
                cs.addRect(statusX - 8, boxY - 26, statusWidth + 16, 20);
                cs.stroke();

                cs.beginText();
                cs.setFont(helveticaBold, statusSize);
                cs.setNonStrokingColor(new Color(22, 163, 74));
                cs.newLineAtOffset(statusX, boxY - 19);
                cs.showText(statusText);
                cs.endText();

                // 8. Plan Features Section
                float featuresY = boxY - boxHeight - 20;
                if (featuresY > 110) {
                    cs.beginText();
                    cs.setFont(helveticaBold, 10);
                    cs.setNonStrokingColor(new Color(15, 23, 42));
                    cs.newLineAtOffset(70, featuresY);
                    cs.showText("Plan Features:");
                    cs.endText();

                    String[] features = getPlanFeatures(planName);
                    float featureY = featuresY - 16;
                    for (String feature : features) {
                        cs.beginText();
                        cs.setFont(helvetica, 9.5f);
                        cs.setNonStrokingColor(new Color(71, 85, 105));
                        cs.newLineAtOffset(85, featureY);
                        cs.showText("- " + feature);
                        cs.endText();
                        featureY -= 14;
                    }
                }

                // 9. Footer Line & Text
                cs.setStrokingColor(new Color(226, 232, 240));
                cs.moveTo(70, 75);
                cs.lineTo(pageWidth - 70, 75);
                cs.stroke();

                cs.beginText();
                cs.setFont(helvetica, 8.5f);
                cs.setNonStrokingColor(new Color(148, 163, 184));
                cs.newLineAtOffset(70, 58);
                cs.showText("Thank you for using Jemigraph Services.");
                cs.endText();

                cs.beginText();
                cs.setFont(helvetica, 8.5f);
                cs.newLineAtOffset(70, 46);
                cs.showText("This is a computer-generated document. No signature is required.");
                cs.endText();
            }

            document.save(new File(filePath));
            System.out.println("PDF Generated successfully at: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String formatDuration(int days) {
        if (days == 30) {
            return "1 Month";
        } else if (days == 90) {
            return "3 Months";
        } else if (days == 180) {
            return "6 Months";
        } else if (days == 365) {
            return "1 Year";
        } else if (days == 7) {
            return "1 Week";
        } else {
            return days + " Days";
        }
    }

    private static String[] getPlanFeatures(String planName) {
        if (planName == null) return new String[]{"Basic features"};

        String upper = planName.toUpperCase();

        if (upper.contains("PREMIUM")) {
            return new String[]{
                    "Full access to all features",
                    "Unlimited uploads",
                    "Priority support",
                    "Advanced analytics",
                    "Watermark removal"
            };
        } else if (upper.contains("PRO") || upper.contains("PROFESSIONAL")) {
            return new String[]{
                    "All PRO features",
                    "Unlimited uploads",
                    "Priority support",
                    "Advanced analytics"
            };
        } else if (upper.contains("BASIC") || upper.contains("FREE")) {
            return new String[]{
                    "Basic features",
                    "Limited uploads",
                    "Standard support"
            };
        } else {
            return new String[]{
                    "Access to all features",
                    "Standard support"
            };
        }
    }
}