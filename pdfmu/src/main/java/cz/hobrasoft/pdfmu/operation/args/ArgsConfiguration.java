package cz.hobrasoft.pdfmu.operation.args;

import cz.hobrasoft.pdfmu.operation.OperationException;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Holds a set of named and type values
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public interface ArgsConfiguration {

    /**
     * Add arguments that configure this configuration to a parser
     *
     * @param parser argument parser to add arguments to
     */
    public void addArguments(ArgumentParser parser);

    /**
     * Sets configuration values from parsed argument namespace
     *
     * @param namespace namespace with the argument values
     * @throws cz.hobrasoft.pdfmu.operation.OperationException when an exception occurs
     */
    public void setFromNamespace(Namespace namespace) throws OperationException;
}
