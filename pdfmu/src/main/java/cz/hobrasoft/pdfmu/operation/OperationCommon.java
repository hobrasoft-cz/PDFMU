package cz.hobrasoft.pdfmu.operation;

import cz.hobrasoft.pdfmu.WritingMapper;
import cz.hobrasoft.pdfmu.TextOutput;
import cz.hobrasoft.pdfmu.Console;
import cz.hobrasoft.pdfmu.jackson.Result;
import cz.hobrasoft.pdfmu.jackson.RpcResponse;
import java.io.IOException;

public abstract class OperationCommon implements Operation {

    private WritingMapper wm = null;
    protected TextOutput to = new TextOutput(); // Discard messages by default

    @Override
    public void setWritingMapper(WritingMapper wm) {
        this.wm = wm;
    }

    protected void writeResult(Result result) {
        // Discard value if mapper was not set
        if (wm != null) {
            RpcResponse response = new RpcResponse(result);
            try {
                wm.writeValue(response);
            } catch (IOException ex) {
                Console.println(String.format("Error: Cannot write JSON document: %s", ex));
            }
        }
    }

    @Override
    public void setTextOutput(TextOutput to) {
        this.to = to;
    }
}
