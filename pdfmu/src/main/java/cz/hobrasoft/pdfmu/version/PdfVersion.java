package cz.hobrasoft.pdfmu.version;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a PDF version
 *
 * <p>
 * Versions 1.2 to 1.7 are supported. This range corresponds to the versions
 * supported by iText, as seen in {@link com.itextpdf.text.pdf.PdfWriter} static
 * fields {@link com.itextpdf.text.pdf.PdfWriter#VERSION_1_2} etc.
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class PdfVersion implements Comparable<PdfVersion> {

    private static Pattern p = Pattern.compile("1\\.(?<charValue>[2-7])");
    private char charValue;

    /**
     * Creates a PDF version from its string representation
     *
     * <p>
     * Versions 1.2 to 1.7 are supported. Valid string representations include:
     * <ul>
     * <li>{@code 1.3}</li>
     * <li>{@code 1.6}</li>
     * </ul>
     *
     * @param stringValue string representation of the version
     * @throws IllegalArgumentException if stringValue does not represent a
     * valid version
     */
    public PdfVersion(String stringValue) throws IllegalArgumentException {
        Matcher m = p.matcher(stringValue);
        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid or unsupported PDF version; use 1.2 to 1.7");
        }
        String charValueString = m.group("charValue");
        assert charValueString.length() == 1;
        charValue = charValueString.charAt(0);
    }

    /**
     * Creates a PDF version from the last character of its string
     * representation
     *
     * <p>
     * Since versions 1.2 to 1.7 are supported, a valid PDF version is uniquely
     * specified by its last character (the digit 2 to 7).
     *
     * <p>
     * Version in this format is returned by
     * {@link com.itextpdf.text.pdf.PdfReader#getPdfVersion()}.
     *
     * @param charValue last character of the PDF version
     * @throws IllegalArgumentException if charValue does not represent a valid
     * version
     */
    public PdfVersion(char charValue) throws IllegalArgumentException {
        if (charValue < '2' || charValue > '7') {
            throw new IllegalArgumentException("Invalid or unsupported PDF version; use 2 to 7");
        }
        this.charValue = charValue;
    }

    @Override
    public String toString() {
        return String.format("1.%c", charValue);
    }

    /**
     * Returns the last character of the PDF version
     *
     * <p>
     * This format of PDF version is accepted for example by
     * {@link PdfStamper#PdfStamper(PdfReader, OutputStream, char)}
     *
     * @return last character of the PDF version
     */
    public char toChar() {
        return charValue;
    }

    @Override
    public int compareTo(PdfVersion o) {
        return charValue - o.charValue;
    }

}
