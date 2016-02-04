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

        Operation operation = operations.get(operationName);
        assert operation != null; // Required

        // If `operation` throws an `OperationException`, we pass it on.
        operation.execute(namespace);
    }

    @Override
    public void setWritingMapper(WritingMapper wm) {
        super.setWritingMapper(wm);
        for (Operation operation : operations.values()) {
            operation.setWritingMapper(wm);
        }
    }

    @Override
    public void setTextOutput(TextOutput to) {
        super.setTextOutput(to);
        for (Operation operation : operations.values()) {
            operation.setTextOutput(to);
        }
    }

}
