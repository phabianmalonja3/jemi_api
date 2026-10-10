package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.Entities.Invoice;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoicePdfService {

	private static final float GREEN_R = 16 / 255f;
	private static final float GREEN_G = 185 / 255f;
	private static final float GREEN_B = 129 / 255f;

	private static final float DARK_R = 15 / 255f;
	private static final float DARK_G = 23 / 255f;
	private static final float DARK_B = 42 / 255f;

	private static final float GRAY_R = 100 / 255f;
	private static final float GRAY_G = 116 / 255f;
	private static final float GRAY_B = 139 / 255f;

	private static final float LIGHT_R = 248 / 255f;
	private static final float LIGHT_G = 250 / 255f;
	private static final float LIGHT_B = 252 / 255f;

	private static final DateTimeFormatter DATE_FORMAT =
			DateTimeFormatter.ofPattern("dd MMM yyyy");

	public byte[] generateInvoicePdf(Invoice invoice) {

		try (
				PDDocument document = new PDDocument();
				ByteArrayOutputStream outputStream =
						new ByteArrayOutputStream()
		) {

			PDPage page = new PDPage(PDRectangle.A4);
			document.addPage(page);

			PDType1Font boldFont =
					new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

			PDType1Font normalFont =
					new PDType1Font(Standard14Fonts.FontName.HELVETICA);

			PDType1Font italicFont =
					new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

			try (
					PDPageContentStream content =
							new PDPageContentStream(document, page)
			) {

				float pageWidth = page.getMediaBox().getWidth();
				float pageHeight = page.getMediaBox().getHeight();

				float margin = 45;
				float contentWidth = pageWidth - (margin * 2);

				// =========================================================
				// TOP GREEN BAR
				// =========================================================

				content.setNonStrokingColor(
						GREEN_R,
						GREEN_G,
						GREEN_B
				);

				content.addRect(
						0,
						pageHeight - 8,
						pageWidth,
						8
				);

				content.fill();

				try {

					ClassPathResource logoResource =
							new ClassPathResource(
									"static/logo.png"
							);

					if (logoResource.exists()) {

						try (InputStream inputStream =
									 logoResource.getInputStream()) {

							PDImageXObject logo =
									LosslessFactory.createFromImage(
											document,
											javax.imageio.ImageIO.read(inputStream)
									);

							float logoWidth = 95;
							float logoHeight =
									logoWidth * logo.getHeight() /
											logo.getWidth();

							content.drawImage(
									logo,
									margin,
									pageHeight - 75 - logoHeight,
									logoWidth,
									logoHeight
							);
						}

					} else {

						drawText(
								content,
								boldFont,
								20,
								margin,
								pageHeight - 55,
								"JEMIGRAPH"
						);
					}

				} catch (Exception e) {

					drawText(
							content,
							boldFont,
							20,
							margin,
							pageHeight - 55,
							"JEMIGRAPH"
					);
				}

				// =========================================================
				// COMPANY INFO
				// =========================================================

				drawText(
						content,
						normalFont,
						9,
						margin,
						pageHeight - 90,
						"Photography & Creative Services"
				);

				// =========================================================
				// INVOICE TITLE
				// =========================================================

				float titleX = pageWidth - margin - 180;

				drawText(
						content,
						boldFont,
						26,
						titleX,
						pageHeight - 55,
						"INVOICE"
				);

				drawText(
						content,
						normalFont,
						9,
						titleX,
						pageHeight - 75,
						"Professional Service Invoice"
				);

				// =========================================================
				// INVOICE NUMBER BOX
				// =========================================================

				float boxY = pageHeight - 125;

				content.setNonStrokingColor(
						LIGHT_R,
						LIGHT_G,
						LIGHT_B
				);

				content.addRect(
						margin,
						boxY - 55,
						contentWidth,
						55
				);

				content.fill();

				drawText(
						content,
						boldFont,
						9,
						margin + 15,
						boxY - 20,
						"INVOICE NUMBER"
				);

				drawText(
						content,
						normalFont,
						10,
						margin + 15,
						boxY - 37,
						invoice.getInvoiceNumber()
				);

				float dateX = margin + 190;

				drawText(
						content,
						boldFont,
						9,
						dateX,
						boxY - 20,
						"ISSUED DATE"
				);

				drawText(
						content,
						normalFont,
						10,
						dateX,
						boxY - 37,
						invoice.getIssuedAt()
								.format(DATE_FORMAT)
				);

				float dueX = margin + 330;

				drawText(
						content,
						boldFont,
						9,
						dueX,
						boxY - 20,
						"DUE DATE"
				);

				drawText(
						content,
						normalFont,
						10,
						dueX,
						boxY - 37,
						invoice.getDueDate()
								.format(DATE_FORMAT)
				);

				// =========================================================
				// BILL TO
				// =========================================================

				float customerY = boxY - 95;

				drawText(
						content,
						boldFont,
						10,
						margin,
						customerY,
						"BILL TO"
				);

				drawText(
						content,
						boldFont,
						13,
						margin,
						customerY - 22,
						safe(invoice.getUser().getName())
				);

				drawText(
						content,
						normalFont,
						10,
						margin,
						customerY - 40,
						safe(invoice.getUser().getEmail())
				);

				// =========================================================
				// STATUS
				// =========================================================

				String status = invoice.getStatus().name();

				float statusWidth = 80;
				float statusHeight = 25;

				float statusX =
						pageWidth - margin - statusWidth;

				float statusY =
						customerY - 32;

				if ("PAID".equals(status)) {

					content.setNonStrokingColor(
							220 / 255f,
							252 / 255f,
							231 / 255f
					);

				} else if ("CANCELLED".equals(status)) {

					content.setNonStrokingColor(
							254 / 255f,
							226 / 255f,
							226 / 255f
					);

				} else {

					content.setNonStrokingColor(
							254 / 255f,
							249 / 255f,
							195 / 255f
					);
				}

				content.addRect(
						statusX,
						statusY,
						statusWidth,
						statusHeight
				);

				content.fill();

				float statusTextX =
						statusX + 20;

				drawText(
						content,
						boldFont,
						10,
						statusTextX,
						statusY + 8,
						status
				);

				// =========================================================
				// ITEMS TABLE
				// =========================================================

				float tableTop = customerY - 85;

				float descriptionX = margin;
				float amountX = pageWidth - margin - 100;

				// Header
				content.setNonStrokingColor(
						DARK_R,
						DARK_G,
						DARK_B
				);

				content.addRect(
						margin,
						tableTop - 35,
						contentWidth,
						35
				);

				content.fill();

				content.setNonStrokingColor(1, 1, 1);

				drawText(
						content,
						boldFont,
						10,
						descriptionX + 12,
						tableTop - 22,
						"DESCRIPTION"
				);

				drawText(
						content,
						boldFont,
						10,
						amountX,
						tableTop - 22,
						"AMOUNT"
				);

				// Row
				content.setNonStrokingColor(
						1,
						1,
						1
				);

				content.addRect(
						margin,
						tableTop - 85,
						contentWidth,
						50
				);

				content.fill();

				drawText(
						content,
						normalFont,
						10,
						descriptionX + 12,
						tableTop - 57,
						safe(invoice.getDescription())
				);

				drawText(
						content,
						normalFont,
						10,
						amountX,
						tableTop - 57,
						"TZS " +
								invoice.getAmount().toPlainString()
				);

				// Bottom line
				content.setStrokingColor(
						226 / 255f,
						232 / 255f,
						240 / 255f
				);

				content.moveTo(
						margin,
						tableTop - 85
				);

				content.lineTo(
						margin + contentWidth,
						tableTop - 85
				);

				content.stroke();

				// =========================================================
				// TOTALS
				// =========================================================

				float totalsY = tableTop - 125;

				float labelX = pageWidth - margin - 180;
				float valueX = pageWidth - margin - 100;

				drawText(
						content,
						normalFont,
						10,
						labelX,
						totalsY,
						"Subtotal"
				);

				drawText(
						content,
						normalFont,
						10,
						valueX,
						totalsY,
						"TZS " +
								invoice.getAmount().toPlainString()
				);

				drawText(
						content,
						normalFont,
						10,
						labelX,
						totalsY - 22,
						"Tax"
				);

				drawText(
						content,
						normalFont,
						10,
						valueX,
						totalsY - 22,
						"TZS " +
								invoice.getTaxAmount().toPlainString()
				);

				// Total background
				content.setNonStrokingColor(
						236 / 255f,
						253 / 255f,
						245 / 255f
				);

				content.addRect(
						labelX - 15,
						totalsY - 65,
						240,
						35
				);

				content.fill();

				drawText(
						content,
						boldFont,
						12,
						labelX,
						totalsY - 51,
						"TOTAL"
				);

				drawText(
						content,
						boldFont,
						12,
						valueX,
						totalsY - 51,
						"TZS " +
								invoice.getTotalAmount().toPlainString()
				);

				// =========================================================
				// PAYMENT INFORMATION
				// =========================================================

				float paymentY = totalsY - 105;

				drawText(
						content,
						boldFont,
						10,
						margin,
						paymentY,
						"PAYMENT INFORMATION"
				);

				drawText(
						content,
						normalFont,
						9,
						margin,
						paymentY - 20,
						"Payment status: " + status
				);

				if (invoice.getPaidAt() != null) {

					drawText(
							content,
							normalFont,
							9,
							margin,
							paymentY - 37,
							"Paid on: " +
									invoice.getPaidAt()
											.format(DATE_FORMAT)
					);
				}

				// =========================================================
				// FOOTER
				// =========================================================

				float footerY = 65;

				content.setStrokingColor(
						226 / 255f,
						232 / 255f,
						240 / 255f
				);

				content.moveTo(
						margin,
						footerY + 25
				);

				content.lineTo(
						pageWidth - margin,
						footerY + 25
				);

				content.stroke();

				drawText(
						content,
						boldFont,
						10,
						margin,
						footerY,
						"JEMIGRAPH"
				);

				drawText(
						content,
						italicFont,
						8,
						margin,
						footerY - 15,
						"Thank you for choosing Jemigraph."
				);

				drawText(
						content,
						normalFont,
						8,
						pageWidth - margin - 150,
						footerY,
						"Computer generated invoice"
				);
			}

			document.save(outputStream);

			return outputStream.toByteArray();

		} catch (IOException e) {

			throw new RuntimeException(
					"Failed to generate invoice PDF",
					e
			);
		}
	}

	private void drawText(
			PDPageContentStream content,
			PDType1Font font,
			float fontSize,
			float x,
			float y,
			String text
	) throws IOException {

		content.beginText();

		content.setFont(font, fontSize);

		content.newLineAtOffset(x, y);

		content.showText(
				safe(text)
		);

		content.endText();
	}

	private String safe(String value) {

		if (value == null || value.isBlank()) {
			return "-";
		}

		// Helvetica doesn't support every Unicode character.
		return value
				.replace("–", "-")
				.replace("—", "-")
				.replace("’", "'")
				.replace("“", "\"")
				.replace("”", "\"");
	}
}

