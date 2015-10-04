package cz.hobrasoft.pdfmu.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class RpcResponse {

    public final String jsonrpc = "2.0";
    public final Object id = null;

    // Either `result` or `error` must be non-null and the other one must be null.
    @JsonInclude(Include.NON_NULL)
    private Result result = null;

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
