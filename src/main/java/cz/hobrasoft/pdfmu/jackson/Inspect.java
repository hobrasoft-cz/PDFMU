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
import java.util.Map;

/**
 *
 * @author Filip Bartek
 */
public class Inspect extends Result {

    @JsonPropertyDescription("PDF version of the input PDF document.")
    public String version;

    @JsonPropertyDescription("PDF properties. "
            + "Key-value pairs; keys are case-sensitive.")
    public Map<String, String> properties;

    public SignatureDisplay signatures;
}
