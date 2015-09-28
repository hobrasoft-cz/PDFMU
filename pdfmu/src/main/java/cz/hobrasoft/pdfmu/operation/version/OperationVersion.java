package cz.hobrasoft.pdfmu.operation.version;

import cz.hobrasoft.pdfmu.operation.Operation;
import cz.hobrasoft.pdfmu.operation.OperationFork;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Gets or sets the version of a PDF file
 *
 * <p>
 * Usage:
 * <ul>
 * <li>{@code pdfmu version set in.pdf --out out.pdf --version 1.6}</li>
 * <li>{@code pdfmu version get in.pdf}</li>
 * <li>{@code pdfmu version set inout.pdf --force}</li>
 * </ul>
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class OperationVersion {

    private static final String commandName = "version";
    private static final String help = "Set or display PDF version of a PDF document";
    private static final String dest = "operation_version";

    private static SortedMap<String, Operation> getOperations() {
        SortedMap<String, Operation> operations = new TreeMap<>();
        operations.put("set", OperationVersionSet.getInstance());
        operations.put("get", OperationVersionGet.getInstance());
        return operations;
    }

    private static Operation instance = null;

    public static Operation getInstance() {
        if (instance == null) {
            instance = new OperationFork(commandName, help, dest, getOperations());
        }
        return instance;
    }

    private OperationVersion() {
        // Disabled
    }

}
