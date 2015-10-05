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
    private Error error = null;

    public RpcResponse(Result result) {
        assert result != null;
        this.result = result;
    }

    public RpcResponse(Error error) {
        assert error != null;
        this.error = error;
    }

    public Result getResult() {
        return result;
    }

    public Error getError() {
        return error;
    }
}
