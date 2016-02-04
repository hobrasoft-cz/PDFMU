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

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class RpcResponse {

    @JsonPropertyDescription("A String specifying the version of the JSON-RPC protocol. MUST be exactly \"2.0\".")
    @JsonProperty(required = true)
    public final String jsonrpc = "2.0";

    @JsonPropertyDescription("It MUST be the same as the value of the id member in the Request Object (String, Number or Null).\n"
            + "If there was an error in detecting the id in the Request object (e.g. Parse error/Invalid Request), it MUST be Null.")
    @JsonProperty(required = true)
    public final Object id = null;

    // Either `result` or `error` must be non-null and the other one must be null.
    @JsonPropertyDescription("This member is REQUIRED on success.\n"
            + "This member MUST NOT exist if there was an error invoking the method.\n"
            + "The value of this member is determined by the method invoked on the Server.")
    @JsonInclude(Include.NON_NULL)
    private Result result = null;

    @JsonPropertyDescription("This member is REQUIRED on error.\n"
            + "This member MUST NOT exist if there was no error triggered during invocation.")
    @JsonInclude(Include.NON_NULL)
    private RpcError error = null;

    public RpcResponse(Result result) {
        assert result != null;
        this.result = result;
    }

    public RpcResponse(RpcError error) {
        assert error != null;
        this.error = error;
    }

    public Result getResult() {
        return result;
    }

    public RpcError getError() {
        return error;
    }
}
