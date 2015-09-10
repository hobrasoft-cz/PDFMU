package cz.hobrasoft.pdfmu.sign;

import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import cz.hobrasoft.pdfmu.ArgsConfiguration;
import java.util.Calendar;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
class SignatureAppearanceParameters implements ArgsConfiguration {

    public String reason = null;
    public String location = null;
    public String name = null;
    public String contact = null;
    public Calendar signDate = null;
    public int certificationLevel = PdfSignatureAppearance.NOT_CERTIFIED;

    @Override
    public void setFromNamespace(Namespace namespace) {
        reason = namespace.getString("reason");
        location = namespace.getString("location");
        contact = namespace.getString("contact");
        // TODO?: Expose `signDate`
        // TODO?: Expose `certificationLevel`
    }

    public void configureSignatureAppearance(PdfSignatureAppearance sap) {
        assert sap != null;
        // Configure signature metadata
        if (reason != null) {
            System.err.println(String.format("Reason: %s", reason));
            sap.setReason(reason);
        }
        if (location != null) {
            System.err.println(String.format("Location: %s", location));
            sap.setLocation(location);
        }
        if (contact != null) {
            System.err.println(String.format("Contact: %s", contact));
            sap.setContact(contact);
        }
        if (signDate != null) {
            // `setSignDate(null)` crashes
            System.err.println(String.format("Date: %s", signDate));
            sap.setSignDate(signDate);
        }
        sap.setCertificationLevel(certificationLevel);
    }

    public PdfSignatureAppearance getSignatureAppearance(PdfStamper stp) {
        assert stp != null;
        // Initialize the signature appearance
        PdfSignatureAppearance sap = stp.getSignatureAppearance();
        configureSignatureAppearance(sap);
        return sap;
    }

}
