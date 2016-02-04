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
package cz.hobrasoft.pdfmu.operation;

import cz.hobrasoft.pdfmu.TextOutput;
import cz.hobrasoft.pdfmu.WritingMapper;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

/**
 * Manipulates a PDF file
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public interface Operation {

    /**
     * Configure this operation's subparser
     *
     * <p>
     * The subparser handles the arguments accepted by this operation.
     *
     * @param subparser Subparser object to configure, especially adding
     * arguments
     * @return the subparser added to {@code subparsers}
     */
    public Subparser configureSubparser(Subparser subparser);

    /**
     * Executes the operation
     *
     * @param namespace parsed command line arguments
     * @throws OperationException if some condition prevents the operation from
     * finishing
     */
    public void execute(Namespace namespace) throws OperationException;

    public void setWritingMapper(WritingMapper wm);

    public void setTextOutput(TextOutput to);
}
