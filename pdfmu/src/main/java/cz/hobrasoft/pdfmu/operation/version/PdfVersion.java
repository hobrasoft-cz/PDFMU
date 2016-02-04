/* 
 * Copyright (C) 2016 Hobrasoft s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.hobrasoft.pdfmu.operation.version;

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

    private static final Pattern p = Pattern.compile("1\\.(?<charValue>[2-7])");

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
     * {@link com.itextpdf.text.pdf.PdfStamper#PdfStamper(PdfReader, OutputStream, char)}
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
