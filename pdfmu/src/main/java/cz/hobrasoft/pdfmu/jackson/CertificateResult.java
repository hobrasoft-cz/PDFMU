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
import java.util.SortedMap;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class CertificateResult {

    @JsonPropertyDescription("The type of this certificate. Only type X.509 is supported for further examination.")
    public String type;

    @JsonPropertyDescription("Is this certificate self-signed? In other words, is the subject identical to the issuer?")
    public boolean selfSigned;

    // Maps types to values.
    // Common types: CN, E, OU, O, L, ST, C
    @JsonPropertyDescription("Subject distinguished name attributes and their values. Common attributes: CN, E, OU, O, L, ST, C. An attribute may have more than one value associated. The values of an attribute are contained in an array.")
    public SortedMap<String, List<String>> subject;
    @JsonPropertyDescription("Issuer distinguished name attributes and their values. Common attributes: CN, E, OU, O, L, ST, C. An attribute may have more than one value associated. The values of an attribute are contained in an array.")
    public SortedMap<String, List<String>> issuer;
}
