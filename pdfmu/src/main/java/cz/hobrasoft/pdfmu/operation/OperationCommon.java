package cz.hobrasoft.pdfmu.operation;

import cz.hobrasoft.pdfmu.JSONWriterEx;

public abstract class OperationCommon implements Operation {

    private JSONWriterEx json;

    @Override
    public void setJsonWriter(JSONWriterEx json) {
        this.json = json;
    }
}
