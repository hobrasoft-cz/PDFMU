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
     * Adds arguments that configure this {@link ArgsConfiguration} to an
     * {@link ArgumentParser}
     *
     * @param parser argument parser to add arguments to
     */
    public void addArguments(ArgumentParser parser);

    /**
     * Sets configuration values from a parsed {@link Namespace}
     *
     * @param namespace the namespace with the argument values
     * @throws OperationException when an exception occurs
     */
    public void setFromNamespace(Namespace namespace) throws OperationException;
}
