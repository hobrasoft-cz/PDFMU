package cz.hobrasoft.pdfmu.metadata;

import cz.hobrasoft.pdfmu.Operation;
import cz.hobrasoft.pdfmu.OperationException;
import java.util.SortedMap;
import java.util.TreeMap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

public class OperationMetadata implements Operation {

    //private final Operation[] operations = {};
    private final SortedMap<String, Operation> operations = new TreeMap<>();

    public OperationMetadata() {
        { // set
            Operation operationSet = new OperationMetadataSet();
            operations.put(operationSet.getCommandName(), operationSet);
        }
        { // get
            Operation operationGet = new OperationMetadataGet();
            operations.put(operationGet.getCommandName(), operationGet);
        }
    }

    @Override
    public String getCommandName() {
        return "metadata";
    }

    @Override
    public Subparser configureSubparser(Subparser subparser) {
        String help = "Set or display PDF document metadata";

        // Configure the subparser
        subparser.help(help)
                .description(help)
                .defaultHelp(true)
                .setDefault("command", OperationMetadata.class);

        // Add subparsers group
        Subparsers subparsersSignature = subparser.addSubparsers()
                .help("metadata operation to execute")
                .metavar("OPERATION")
                .dest("metadataOperation");

        // Configure the subparsers
        for (Operation operation : operations.values()) {
            operation.configureSubparser(subparsersSignature.addParser(operation.getCommandName()));
        }

        return subparser;
    }

    @Override
    public void execute(Namespace namespace) throws OperationException {
        assert namespace.getString("metadataOperation") != null;
        Operation operation = operations.get(namespace.getString("metadataOperation"));
        assert operation != null;

        // If `operation` throws an `OperationException`, we pass it on.
        operation.execute(namespace);
    }

}
