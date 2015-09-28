package cz.hobrasoft.pdfmu.operation.signature;

import cz.hobrasoft.pdfmu.operation.Operation;
import cz.hobrasoft.pdfmu.operation.OperationFork;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Manipulates signatures of a PDF document
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class OperationSignature {

    private static final String commandName = "signature";
    private static final String help = "Sign a PDF document or display signatures in a PDF document";
    private static final String dest = "operation_signature";

    private static SortedMap<String, Operation> getOperations() {
        SortedMap<String, Operation> operations = new TreeMap<>();
        operations.put("add", OperationSignatureAdd.getInstance());
        operations.put("display", OperationSignatureDisplay.getInstance());
        return operations;
    }

    private static Operation instance = null;

    public static Operation getInstance() {
        if (instance == null) {
            instance = new OperationFork(commandName, help, dest, getOperations());
        }
        return instance;
    }

    private OperationSignature() {
        // Disabled
    }

}
