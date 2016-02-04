/* 
 * Copyright (C) 2016 Hobrasoft s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
