package cz.hobrasoft.pdfmu;

import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Holds a set of named and type values
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public interface ArgsConfiguration {

    /**
     * Sets configuration values from parsed argument namespace
     *
     * @param namespace namespace with the argument values
     */
    public void setFromNamespace(Namespace namespace);
}
