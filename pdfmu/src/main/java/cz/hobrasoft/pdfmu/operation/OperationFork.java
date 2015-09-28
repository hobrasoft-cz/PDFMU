package cz.hobrasoft.pdfmu.operation;

import cz.hobrasoft.pdfmu.JSONWriterEx;
import cz.hobrasoft.pdfmu.JsonWriting;
import java.util.Map;
import java.util.SortedMap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

public class OperationFork extends OperationCommon {

    private final String commandName;
    private final String help;
    private final String dest;
    private final SortedMap<String, Operation> operations;

    // The user shall guarantee that `dest` is globally unique for this instance.
    // It is used later to extract the desired sub-operation from the global namespace.
    public OperationFork(String commandName, String help, String dest,
            SortedMap<String, Operation> operations) {
        this.commandName = commandName;
        this.help = help;
        this.dest = dest;
        this.operations = operations;
    }

    @Override
    public Subparser configureSubparser(Subparser subparser) {
        // Configure the subparser
        subparser.help(help)
                .description(help)
                .defaultHelp(true);

        Subparsers subparsers = subparser.addSubparsers()
                .help(String.format("%s operation to execute", commandName))
                .metavar("OPERATION")
                .dest(dest);

        // Configure the subparsers
        for (Map.Entry<String, Operation> e : operations.entrySet()) {
            String name = e.getKey();
            Operation operation = e.getValue();
            assert operation != null;
            // Here we appreciate that the keys (that is operation names)
            // are required to be unique by SortedMap,
            // so we do not add two subparsers of the same name.
            operation.configureSubparser(subparsers.addParser(name));
        }

        return subparser;
    }

    @Override
    public void execute(Namespace namespace) throws OperationException {
        assert namespace != null;

        String operationName = namespace.getString(dest);
        assert operationName != null; // Sub-command -> required
        json.write("operation", operationName);
        json.object("result");

        Operation operation = operations.get(operationName);
        assert operation != null; // Required

        // If `operation` throws an `OperationException`, we pass it on.
        operation.execute(namespace);

        json.endObject(); // result
    }

    /**
     * Delegates the JSON writer to the sub-operations.
     */
    @Override
    public void setJsonWriter(JSONWriterEx json) {
        super.setJsonWriter(json);
        for (JsonWriting operation : operations.values()) {
            operation.setJsonWriter(json);
        }
    }

}
