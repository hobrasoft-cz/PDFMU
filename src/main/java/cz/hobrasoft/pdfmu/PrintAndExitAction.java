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
package cz.hobrasoft.pdfmu;

import java.util.Map;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;

/**
 * Argument action to print a message and exit program. Inspired by
 * {@link net.sourceforge.argparse4j.impl.action.VersionArgumentAction}.
 *
 * @author Filip Bartek
 */
public class PrintAndExitAction implements ArgumentAction {

    private final String message;

    public PrintAndExitAction(String message) {
        this.message = message;
    }

    /**
     * Prints the message to {@link System#out} and terminates the program with
     * the exit code 0. All the parameters are ignored.
     */
    @Override
    public void run(ArgumentParser parser, Argument arg,
            Map<String, Object> attrs, String flag, Object value) {
        System.out.print(message);
        System.exit(0);
    }

    @Override
    public void onAttach(Argument arg) {
        // Do nothing
    }

    /**
     * @return always false because this action does not consume and argument
     */
    @Override
    public boolean consumeArgument() {
        return false;
    }
}
