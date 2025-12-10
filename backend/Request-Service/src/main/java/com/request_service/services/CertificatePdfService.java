package com.request_service.services;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.request_service.models.Certificate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Service pour générer des PDF de certificats médicaux avec le style SehaMaroc.
 * Optimisé pour tenir sur une seule page A4 avec un design professionnel.
 * 
 * @author Request-Service Team
 * @version 5.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CertificatePdfService {

    // Couleurs SehaMaroc
    private static final DeviceRgb SEHA_BLUE = new DeviceRgb(0, 102, 204);      // #0066CC
    private static final DeviceRgb SEHA_GREEN = new DeviceRgb(0, 170, 68);          // #00AA44
    private static final DeviceRgb SEHA_LIGHT_BLUE = new DeviceRgb(230, 242, 255);  // Clair pour fonds
    private static final DeviceRgb SEHA_DARK_BLUE = new DeviceRgb(0, 51, 102);     // Bleu foncé
    
    private static final Border THIN_BORDER = new SolidBorder(SEHA_BLUE, 0.5f);

    /**
     * Génère un PDF pour un certificat médical avec le style SehaMaroc.
     * Optimisé pour tenir sur une seule page A4.
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
        
        // Marges minimales pour optimiser l'espace
        document.setMargins(20, 25, 15, 25);
        
        try {
            DateTimeFormatter shortFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            // ========== EN-TÊTE ULTRA-COMPACT AVEC LOGO ==========
            Div headerDiv = new Div()
                    .setBorderBottom(new SolidBorder(SEHA_BLUE, 2f))
                    .setPaddingBottom(8)
                    .setMarginBottom(8)
                    .setBackgroundColor(SEHA_LIGHT_BLUE)
                    .setPadding(8);
            
            // Tableau en-tête : Logo | Titre et Numéro
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{0.8f, 3.2f}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(0);
            
            // Colonne logo
            Cell logoCell = new Cell()
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE)
                    .setPadding(3);
            
            Image logo = loadLogo();
            if (logo != null) {
                logo.setWidth(UnitValue.createPointValue(65));
                logo.setAutoScale(true);
                logo.setHorizontalAlignment(HorizontalAlignment.LEFT);
                logoCell.add(logo);
            } else {
                Div logoTextDiv = new Div()
                        .setPadding(2)
                        .setBorder(new SolidBorder(SEHA_BLUE, 0.8f))
                        .setBackgroundColor(SEHA_LIGHT_BLUE);
                
                Paragraph sehaText = new Paragraph("Seha")
                        .setFontSize(14)
                        .setBold()
                        .setFontColor(SEHA_BLUE)
                        .setMarginBottom(0);
                logoTextDiv.add(sehaText);
                
                Paragraph marocText = new Paragraph("Maroc")
                        .setFontSize(14)
                        .setBold()
                        .setFontColor(SEHA_GREEN)
                        .setMarginTop(-3);
                logoTextDiv.add(marocText);
                logoCell.add(logoTextDiv);
            }
            
            headerTable.addCell(logoCell);
            
            // Colonne titre et numéro
            Cell titleCell = new Cell()
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE)
                    .setPadding(3)
                    .setTextAlignment(TextAlignment.RIGHT);
            
            Paragraph title = new Paragraph(
                    certificate.getTitle() != null ? certificate.getTitle().toUpperCase() : "CERTIFICAT MÉDICAL")
                    .setFontSize(16)
                    .setBold()
                    .setFontColor(SEHA_BLUE)
                    .setMarginBottom(2);
            titleCell.add(title);
            
            String certNumber = certificate.getCertificateNumber() != null ? 
                    certificate.getCertificateNumber() : certificate.getCertificateId();
            Paragraph certNumberPara = new Paragraph("N° " + certNumber)
                    .setFontSize(9)
                    .setFontColor(SEHA_GREEN)
                    .setBold();
            titleCell.add(certNumberPara);
            
            headerTable.addCell(titleCell);
            headerDiv.add(headerTable);
            document.add(headerDiv);
            
            // ========== INFORMATIONS PRINCIPALES EN DEUX COLONNES COMPACTES ==========
            Table mainInfoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(6);
            
            // Colonne gauche : Certificat + Médecin
            Cell leftCell = new Cell()
                    .setBorder(new SolidBorder(SEHA_BLUE, 0.5f))
                    .setPadding(6)
                    .setBackgroundColor(SEHA_LIGHT_BLUE)
                    .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.TOP);
            
            // Informations du certificat
            Paragraph certInfoTitle = new Paragraph("📋 INFORMATIONS")
                    .setFontSize(9)
                    .setBold()
                    .setFontColor(SEHA_BLUE)
                    .setMarginBottom(3)
                    .setBorderBottom(new SolidBorder(SEHA_BLUE, 0.8f))
                    .setPaddingBottom(2);
            leftCell.add(certInfoTitle);
            
            String issueDate = certificate.getIssueDate() != null ? 
                    certificate.getIssueDate().format(shortFormatter) : "Non spécifiée";
            leftCell.add(new Paragraph("Date : " + issueDate)
                    .setFontSize(8)
                    .setMarginBottom(1));
            
            if (certificate.getExpiryDate() != null) {
                String expiryDate = certificate.getExpiryDate().format(shortFormatter);
                leftCell.add(new Paragraph("Expire : " + expiryDate)
                        .setFontSize(8)
                        .setMarginBottom(1));
            }
            
            if (certificate.getType() != null) {
                leftCell.add(new Paragraph("Type : " + certificate.getType())
                        .setFontSize(8)
                        .setMarginBottom(4));
            }
            
            // Informations du médecin
            Paragraph doctorTitle = new Paragraph("👨‍⚕️ MÉDECIN")
                    .setFontSize(9)
                    .setBold()
                    .setFontColor(SEHA_BLUE)
                    .setMarginTop(4)
                    .setMarginBottom(3)
                    .setBorderBottom(new SolidBorder(SEHA_BLUE, 0.8f))
                    .setPaddingBottom(2);
            leftCell.add(doctorTitle);
            
            String doctorFullName = buildDoctorFullName(certificate);
            leftCell.add(new Paragraph(doctorFullName)
                    .setFontSize(9)
                    .setBold()
                    .setFontColor(SEHA_DARK_BLUE)
                    .setMarginBottom(1));
            
            if (certificate.getProviderProfessionalTitle() != null && !certificate.getProviderProfessionalTitle().isEmpty()) {
                leftCell.add(new Paragraph(certificate.getProviderProfessionalTitle())
                        .setFontSize(7)
                        .setItalic()
                        .setFontColor(SEHA_BLUE)
                        .setMarginBottom(1));
            }
            
            if (certificate.getProviderFirstName() != null && !certificate.getProviderFirstName().isEmpty() &&
                certificate.getProviderLastName() != null && !certificate.getProviderLastName().isEmpty()) {
                leftCell.add(new Paragraph(certificate.getProviderFirstName() + " " + certificate.getProviderLastName())
                        .setFontSize(8)
                        .setMarginBottom(0));
            }
            
            mainInfoTable.addCell(leftCell);
            
            // Colonne droite : Patient
            Cell rightCell = new Cell()
                    .setBorder(new SolidBorder(SEHA_BLUE, 0.5f))
                    .setPadding(6)
                    .setBackgroundColor(SEHA_LIGHT_BLUE)
                    .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.TOP);
            
            Paragraph patientTitle = new Paragraph("👤 PATIENT")
                    .setFontSize(9)
                    .setBold()
                    .setFontColor(SEHA_BLUE)
                    .setMarginBottom(3)
                    .setBorderBottom(new SolidBorder(SEHA_BLUE, 0.8f))
                    .setPaddingBottom(2);
            rightCell.add(patientTitle);
            
            String patientName = certificate.getPatientName() != null ? 
                    certificate.getPatientName() : "Non spécifié";
            rightCell.add(new Paragraph(patientName)
                    .setFontSize(9)
                    .setBold()
                    .setFontColor(SEHA_DARK_BLUE)
                    .setMarginBottom(1));
            
            if (certificate.getPatientFirstName() != null && !certificate.getPatientFirstName().isEmpty()) {
                rightCell.add(new Paragraph("Prénom : " + certificate.getPatientFirstName())
                        .setFontSize(8)
                        .setMarginBottom(1));
            }
            
            if (certificate.getPatientLastName() != null && !certificate.getPatientLastName().isEmpty()) {
                rightCell.add(new Paragraph("Nom : " + certificate.getPatientLastName())
                        .setFontSize(8)
                        .setMarginBottom(1));
            }
            
            if (certificate.getPatientEmail() != null && !certificate.getPatientEmail().isEmpty()) {
                rightCell.add(new Paragraph("Email : " + certificate.getPatientEmail())
                        .setFontSize(7)
                        .setFontColor(SEHA_BLUE)
                        .setMarginBottom(0));
            }
            
            mainInfoTable.addCell(rightCell);
            document.add(mainInfoTable);
            
            // ========== CAS TRAITÉ / DIAGNOSTIC ULTRA-COMPACT ==========
            if (certificate.getCaseTreated() != null && !certificate.getCaseTreated().isEmpty()) {
                Div caseDiv = new Div()
                        .setMarginBottom(6)
                        .setPadding(6)
                        .setBackgroundColor(SEHA_LIGHT_BLUE)
                        .setBorder(new SolidBorder(SEHA_BLUE, 1f));
                
                Paragraph caseTitle = new Paragraph("🔍 CAS TRAITÉ / DIAGNOSTIC")
                        .setFontSize(9)
                        .setBold()
                        .setFontColor(SEHA_BLUE)
                        .setMarginBottom(3);
                caseDiv.add(caseTitle);
                
                Paragraph casePara = new Paragraph(certificate.getCaseTreated())
                        .setFontSize(9)
                        .setFontColor(SEHA_DARK_BLUE)
                        .setMarginBottom(0);
                caseDiv.add(casePara);
                
                document.add(caseDiv);
            }
            
            // ========== DÉTAILS DU CERTIFICAT ULTRA-COMPACT ==========
            if (certificate.getContent() != null && !certificate.getContent().isEmpty()) {
                Div contentDiv = new Div()
                        .setMarginBottom(6)
                        .setPadding(6)
                        .setBorder(THIN_BORDER);
                
                Paragraph contentTitle = new Paragraph("📝 DÉTAILS DU CERTIFICAT")
                        .setFontSize(9)
                        .setBold()
                        .setFontColor(SEHA_BLUE)
                        .setMarginBottom(3);
                contentDiv.add(contentTitle);
                
                Paragraph contentPara = new Paragraph(certificate.getContent())
                        .setFontSize(8)
                        .setTextAlignment(TextAlignment.JUSTIFIED)
                        .setMarginBottom(0);
                contentDiv.add(contentPara);
                
                document.add(contentDiv);
            }
            
            // ========== SIGNATURE NUMÉRIQUE ULTRA-COMPACTE ==========
            Table signatureTable = new Table(UnitValue.createPercentArray(new float[]{2.2f, 0.8f}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(8)
                    .setMarginBottom(5);
            
            // Colonne gauche : Zone de signature
            Cell signatureCell = new Cell()
                    .setBorder(new SolidBorder(SEHA_BLUE, 1.5f))
                    .setPadding(8)
                    .setBackgroundColor(SEHA_LIGHT_BLUE)
                    .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);
            
            String signerName = buildDoctorFullName(certificate);
            
            Paragraph signaturePara = new Paragraph(signerName)
                    .setFontSize(11)
                    .setBold()
                    .setFontColor(SEHA_BLUE)
                    .setMarginBottom(4);
            signatureCell.add(signaturePara);
            
            Div signatureLine = new Div()
                    .setBorderBottom(new SolidBorder(SEHA_BLUE, 1.5f))
                    .setMarginBottom(6)
                    .setWidth(UnitValue.createPercentValue(65));
            signatureCell.add(signatureLine);
            
            String signatureDate = certificate.getIssueDate() != null ? 
                    certificate.getIssueDate().format(shortFormatter) : "Non spécifiée";
            signatureCell.add(new Paragraph("Date : " + signatureDate)
                    .setFontSize(7)
                    .setItalic()
                    .setFontColor(SEHA_BLUE)
                    .setMarginBottom(2));
            
            if (certificate.getProviderProfessionalTitle() != null && !certificate.getProviderProfessionalTitle().isEmpty()) {
                signatureCell.add(new Paragraph(certificate.getProviderProfessionalTitle())
                        .setFontSize(7)
                        .setItalic()
                        .setFontColor(SEHA_BLUE)
                        .setMarginBottom(0));
            }
            
            signatureTable.addCell(signatureCell);
            
            // Colonne droite : Certification numérique
            Cell certInfoCell = new Cell()
                    .setBorder(Border.NO_BORDER)
                    .setPadding(4)
                    .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE)
                    .setTextAlignment(TextAlignment.CENTER);
            
            Paragraph certTitle = new Paragraph("SIGNATURE\nNUMÉRIQUE")
                    .setFontSize(8)
                    .setBold()
                    .setFontColor(SEHA_BLUE)
                    .setMarginBottom(4);
            certInfoCell.add(certTitle);
            
            if (certificate.getCreatedAt() != null) {
                String timestamp = certificate.getCreatedAt()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy\nHH:mm"));
                certInfoCell.add(new Paragraph(timestamp)
                        .setFontSize(6)
                        .setItalic()
                        .setFontColor(SEHA_GREEN)
                        .setMarginBottom(3));
            }
            
            Paragraph digitalNote = new Paragraph("✓ Certifié")
                    .setFontSize(7)
                    .setFontColor(SEHA_GREEN)
                    .setBold();
            certInfoCell.add(digitalNote);
            
            signatureTable.addCell(certInfoCell);
            document.add(signatureTable);
            
            // ========== PIED DE PAGE ULTRA-COMPACT ==========
            Div footerDiv = new Div()
                    .setMarginTop(5)
                    .setPaddingTop(5)
                    .setBorderTop(new SolidBorder(SEHA_BLUE, 1f))
                    .setBackgroundColor(SEHA_LIGHT_BLUE)
                    .setPadding(4);
            
            Table footerTable = new Table(UnitValue.createPercentArray(new float[]{2.5f, 0.5f}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBorder(Border.NO_BORDER);
            
            Cell footerLeft = new Cell()
                    .setBorder(Border.NO_BORDER)
                    .setPadding(2);
            
            footerLeft.add(new Paragraph("Ce certificat est valable uniquement pour l'usage indiqué.")
                    .setFontSize(6)
                    .setItalic()
                    .setFontColor(SEHA_BLUE)
                    .setMarginBottom(1));
            
            if (certificate.getCreatedAt() != null) {
                String generatedDate = certificate.getCreatedAt()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                footerLeft.add(new Paragraph("Généré le : " + generatedDate)
                        .setFontSize(6)
                        .setItalic()
                        .setFontColor(SEHA_GREEN));
            }
            
            footerTable.addCell(footerLeft);
            
            Cell footerRight = new Cell()
                    .setBorder(Border.NO_BORDER)
                    .setPadding(2)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);
            
            Image footerLogo = loadLogo();
            if (footerLogo != null) {
                footerLogo.setWidth(UnitValue.createPointValue(40));
                footerLogo.setAutoScale(true);
                footerLogo.setHorizontalAlignment(HorizontalAlignment.RIGHT);
                footerRight.add(footerLogo);
            } else {
                Div footerLogoText = new Div();
                footerLogoText.add(new Paragraph("Seha")
                        .setFontSize(8)
                        .setBold()
                        .setFontColor(SEHA_BLUE)
                        .setMarginBottom(0));
                footerLogoText.add(new Paragraph("Maroc")
                        .setFontSize(8)
                        .setBold()
                        .setFontColor(SEHA_GREEN)
                        .setMarginTop(-2));
                footerRight.add(footerLogoText);
            }
            
            footerTable.addCell(footerRight);
            footerDiv.add(footerTable);
            document.add(footerDiv);
            
            document.close();
            
            log.info("✅ PDF généré avec succès pour le certificat : {}", certificate.getCertificateId());
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("❌ Erreur lors de la génération du PDF : {}", e.getMessage(), e);
            throw new Exception("Erreur lors de la génération du PDF : " + e.getMessage(), e);
        }
    }
    
    /**
     * Charge le logo SehaMaroc depuis les ressources.
     * Essaie plusieurs emplacements et formats possibles.
     * 
     * @return L'image du logo ou null si non trouvé
     */
    private Image loadLogo() {
        // Liste des chemins possibles pour le logo
        String[] logoPaths = {
            "static/sehamaroc-logo.png",
            "static/sehamaroc-logo.jpg",
            "static/sehamaroc-logo.jpeg",
            "static/logo.png",
            "static/logo.jpg",
            "sehamaroc-logo.png",
            "sehamaroc-logo.jpg"
        };
        
        for (String path : logoPaths) {
            try {
                Resource logoResource = new ClassPathResource(path);
                if (logoResource.exists() && logoResource.isReadable()) {
                    byte[] imageBytes = logoResource.getInputStream().readAllBytes();
                    if (imageBytes != null && imageBytes.length > 0) {
                        Image logo = new Image(ImageDataFactory.create(imageBytes));
                        log.info("✅ Logo chargé depuis : {}", path);
                        return logo;
                    }
                }
            } catch (Exception e) {
                // Continuer avec le chemin suivant
                log.debug("Logo non trouvé à : {} - {}", path, e.getMessage());
            }
        }
        
        log.warn("⚠️ Logo SehaMaroc non trouvé. Utilisation du texte à la place.");
        return null;
    }
    
    /**
     * Construit le nom complet du médecin avec titre professionnel.
     * Format : "Dr. [Prénom] [Nom]" ou "[Titre] [Prénom] [Nom]"
     */
    private String buildDoctorFullName(Certificate certificate) {
        StringBuilder nameBuilder = new StringBuilder();
        
        // Ajouter le titre professionnel
        if (certificate.getProviderProfessionalTitle() != null && 
            !certificate.getProviderProfessionalTitle().isEmpty()) {
            nameBuilder.append(certificate.getProviderProfessionalTitle());
            if (!certificate.getProviderProfessionalTitle().endsWith(".")) {
                nameBuilder.append(".");
            }
            nameBuilder.append(" ");
        } else {
            // Par défaut, utiliser "Dr." si pas de titre
            nameBuilder.append("Dr. ");
        }
        
        // Ajouter prénom et nom
        if (certificate.getProviderFirstName() != null && !certificate.getProviderFirstName().isEmpty() &&
            certificate.getProviderLastName() != null && !certificate.getProviderLastName().isEmpty()) {
            nameBuilder.append(certificate.getProviderFirstName())
                      .append(" ")
                      .append(certificate.getProviderLastName());
        } else if (certificate.getProviderName() != null && !certificate.getProviderName().isEmpty()) {
            nameBuilder.append(certificate.getProviderName());
        } else {
            nameBuilder.append("Médecin non identifié");
        }
        
        return nameBuilder.toString();
    }
}
