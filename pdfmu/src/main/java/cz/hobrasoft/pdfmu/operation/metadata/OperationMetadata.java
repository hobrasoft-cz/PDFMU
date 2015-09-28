package cz.hobrasoft.pdfmu.operation.metadata;

import cz.hobrasoft.pdfmu.operation.Operation;
import cz.hobrasoft.pdfmu.operation.OperationFork;
import java.util.SortedMap;
import java.util.TreeMap;

public class OperationMetadata {

    private static final String commandName = "metadata";
    private static final String help = "Set or display PDF document metadata";
    private static final String dest = "operation_metadata";

    private static SortedMap<String, Operation> getOperations() {
        SortedMap<String, Operation> operations = new TreeMap<>();
        operations.put("set", OperationMetadataSet.getInstance());
        operations.put("get", OperationMetadataGet.getInstance());
        return operations;
    }

    private static Operation instance = null;

    public static Operation getInstance() {
        if (instance == null) {
            instance = new OperationFork(commandName, help, dest, getOperations());
        }
        return instance;
    }

    private OperationMetadata() {
        // Disabled
    }

}
