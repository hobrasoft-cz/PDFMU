package cz.hobrasoft.pdfmu.operation;

import cz.hobrasoft.pdfmu.JSONWriterEx;
import cz.hobrasoft.pdfmu.JsonWriting;

public abstract class OperationCommon implements Operation, JsonWriting {

    private JSONWriterEx json;

    @Override
    public void setJsonWriter(JSONWriterEx json) {
        this.json = json;
    }
}
