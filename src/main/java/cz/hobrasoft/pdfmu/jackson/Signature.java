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
package cz.hobrasoft.pdfmu.jackson;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class Signature {

    @JsonPropertyDescription("AcroForm field name")
    public String id;

    @JsonPropertyDescription("Does the signature cover the whole document?")
    public Boolean coversWholeDocument;

    @JsonPropertyDescription("Document revision associated with this signature")
    public Integer revision;

    @JsonPropertyDescription("Metadata")
    public SignatureMetadata metadata;

    @JsonPropertyDescription("Certificate chain. The certificate used for signing the document is the first in the array.")
    public List<CertificateResult> certificates;
}
