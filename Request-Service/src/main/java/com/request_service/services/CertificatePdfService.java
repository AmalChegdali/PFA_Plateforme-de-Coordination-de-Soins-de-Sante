package com.request_service.services;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.request_service.models.Certificate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Service pour générer des PDF de certificats médicaux.
 * 
 * @author Request-Service Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CertificatePdfService {

    /**
     * Génère un PDF pour un certificat médical.
     * 
     * @param certificate Le certificat à convertir en PDF
     * @return Le PDF sous forme de tableau d'octets
     * @throws Exception Si une erreur survient lors de la génération
     */
    public byte[] generatePdf(Certificate certificate) throws Exception {
        log.info("📄 Génération du PDF pour le certificat : {}", certificate.getCertificateId());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        
        try {
            // En-tête avec logo et titre
            Paragraph header = new Paragraph("CERTIFICAT MÉDICAL")
                    .setFontSize(24)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(header);
            
            // Informations du certificat
            Paragraph certId = new Paragraph("N° " + certificate.getCertificateId())
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(30);
            document.add(certId);
            
            // Date d'émission
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", java.util.Locale.FRENCH);
            String issueDateStr = certificate.getIssueDate().format(formatter);
            Paragraph date = new Paragraph("Date d'émission : " + issueDateStr)
                    .setFontSize(11)
                    .setMarginBottom(20);
            document.add(date);
            
            // Informations du patient
            Paragraph patientInfo = new Paragraph()
                    .add(new Paragraph("Je soussigné(e), " + certificate.getProviderName())
                            .setBold())
                    .add(new Paragraph("certifie avoir examiné :")
                            .setMarginTop(10))
                    .add(new Paragraph(certificate.getPatientName())
                            .setBold()
                            .setFontSize(14)
                            .setMarginTop(5));
            document.add(patientInfo);
            
            // Contenu du certificat
            Paragraph content = new Paragraph(certificate.getContent())
                    .setMarginTop(20)
                    .setMarginBottom(20)
                    .setFontSize(11);
            document.add(content);
            
            // Date d'expiration si disponible
            if (certificate.getExpiryDate() != null) {
                String expiryDateStr = certificate.getExpiryDate().format(formatter);
                Paragraph expiry = new Paragraph("Date d'expiration : " + expiryDateStr)
                        .setFontSize(10)
                        .setItalic()
                        .setMarginTop(10);
                document.add(expiry);
            }
            
            // Signature
            Paragraph signature = new Paragraph()
                    .add(new Paragraph("Signature du professionnel de santé :")
                            .setMarginTop(40)
                            .setFontSize(10))
                    .add(new Paragraph(certificate.getProviderName())
                            .setBold()
                            .setMarginTop(30))
                    .add(new Paragraph("ID Provider : " + certificate.getProviderId())
                            .setFontSize(9)
                            .setMarginTop(5));
            document.add(signature);
            
            // Pied de page
            Paragraph footer = new Paragraph()
                    .add(new Paragraph("Ce certificat est valable uniquement pour l'usage indiqué.")
                            .setFontSize(8)
                            .setItalic()
                            .setTextAlignment(TextAlignment.CENTER)
                            .setMarginTop(40));
            document.add(footer);
            
            document.close();
            
            log.info("✅ PDF généré avec succès pour le certificat : {}", certificate.getCertificateId());
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("❌ Erreur lors de la génération du PDF : {}", e.getMessage(), e);
            throw new Exception("Erreur lors de la génération du PDF : " + e.getMessage(), e);
        }
    }
}

