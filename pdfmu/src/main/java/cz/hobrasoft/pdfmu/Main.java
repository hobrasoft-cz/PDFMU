package cz.hobrasoft.pdfmu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import cz.hobrasoft.pdfmu.operation.Operation;
import cz.hobrasoft.pdfmu.operation.OperationAttach;
import cz.hobrasoft.pdfmu.operation.OperationException;
import cz.hobrasoft.pdfmu.operation.metadata.OperationMetadata;
import cz.hobrasoft.pdfmu.operation.signature.OperationSignature;
import cz.hobrasoft.pdfmu.operation.version.OperationVersion;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * The main class of PDFMU
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class Main {

    private static void disableLoggers() {
        // http://stackoverflow.com/a/3363747
        LogManager.getLogManager().reset(); // Remove the handlers
    }

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    /**
     * The main entry point of PDFMU
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Configure log message format
        // Arguments:
        // http://docs.oracle.com/javase/7/docs/api/java/util/logging/SimpleFormatter.html#format%28java.util.logging.LogRecord%29
        // %4$s: level
        // %5$s: message
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s%n");

        // Create a command line argument parser
        ArgumentParser parser = ArgumentParsers.newArgumentParser("pdfmu")
                .description("Manipulate a PDF document")
                .defaultHelp(true);
        // TODO: Use an enum
        parser.addArgument("-of", "--output_format")
                .choices("text", "json")
                .setDefault("text")
                .type(String.class)
                .nargs("?")
                .help("format of stdout output");

        // TODO: Set pdfmu version in `parser`. For example:
        // parser.version("1.0");
        // http://argparse4j.sourceforge.net/usage.html#argumentparser-version
        // Try to extract the version from `pom.xml`.
        // Once the version has been set, enable a CL argument:
//        parser.addArgument("-v", "--version")
//                .help("print version of pdfmu")
//                .action(Arguments.version());
        // Create a Subparsers instance for operation subparsers
        Subparsers subparsers = parser.addSubparsers()
                .help("operation to execute")
                .metavar("OPERATION")
                .dest("operation");

        // Create a map of operations
        SortedMap<String, Operation> operations = new TreeMap<>();
        operations.put("version", OperationVersion.getInstance());
        operations.put("signature", OperationSignature.getInstance());
        operations.put("attach", OperationAttach.getInstance());
        operations.put("metadata", OperationMetadata.getInstance());

        // Configure operation subparsers
        for (Map.Entry<String, Operation> e : operations.entrySet()) {
            String name = e.getKey();
            Operation operation = e.getValue();
            operation.configureSubparser(subparsers.addParser(name));
        }

        // Parse command line arguments
        Namespace namespace = null;
        try {
            // If help is requested,
            // `parseArgs` prints help message and throws `ArgumentParserException`
            // (so `namespace` stays null).
            // If insufficient or invalid `args` are given,
            // `parseArgs` throws `ArgumentParserException`.
            namespace = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            // Prints information about the parsing error (missing argument etc.)
            parser.handleError(e);
        }

        // Handle command line arguments
        if (namespace != null) {
            // Extract operation name
            String operationName = namespace.getString("operation");
            assert operationName != null; // The argument "operation" is a sub-command, thus it is required

            // Select the operation from `operations`
            assert operations.containsKey(operationName); // Only supported operation names are allowed
            Operation operation = operations.get(operationName);
            assert operation != null;

            // Choose the output format
            String outputFormat = namespace.getString("output_format");
            switch (outputFormat) {
                case "json":
                    // Initialize the JSON serializer
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.enable(SerializationFeature.INDENT_OUTPUT); // Enable nice formatting
                    WritingMapper wm = new WritingMapper(mapper, System.err); // Bind to `System.err`
                    operation.setWritingMapper(wm); // Configure the operation
                    // Disable loggers
                    disableLoggers();
                    break;
                case "text":
                    // Initialize the text output
                    TextOutput to = new TextOutput(System.err); // Bind to `System.err`
                    operation.setTextOutput(to); // Configure the operation
                    break;
                default:
                    assert false; // The option has limited choices
            }

            // Execute the operation
            try {
                operation.execute(namespace);
            } catch (OperationException ex) {
                // Log the exception
                logger.severe(ex.getLocalizedMessage());
                Throwable cause = ex.getCause();
                if (cause != null && cause.getMessage() != null) {
                    logger.severe(cause.getLocalizedMessage());
                }
                // TODO: Output a JSON document if "-of=json"
            }
        }
        // TODO: Return an exit code
    }
}
