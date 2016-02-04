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
