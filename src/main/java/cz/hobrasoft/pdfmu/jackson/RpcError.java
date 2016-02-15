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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.Map;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class RpcError {

    @JsonPropertyDescription("A Number that indicates the error type that occurred.")
    @JsonProperty(required = true)
    public int code;

    @JsonPropertyDescription("A String providing a short description of the error.\n"
            + "The message SHOULD be limited to a concise single sentence.")
    @JsonProperty(required = true)
    public String message;

    public static class Data {

        @JsonInclude(Include.NON_NULL)
        public Class causeClass = null;

        @JsonInclude(Include.NON_NULL)
        public String causeMessage = null;

        @JsonInclude(Include.NON_NULL)
        public Map<String, Object> arguments = null;
    }

    @JsonPropertyDescription("A Primitive or Structured value that contains additional information about the error.\n"
            + "This may be omitted.\n"
            + "The value of this member is defined by the Server (e.g. detailed error information, nested errors etc.).")
    @JsonInclude(Include.NON_NULL)
    public Data data = null;

    public RpcError(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
