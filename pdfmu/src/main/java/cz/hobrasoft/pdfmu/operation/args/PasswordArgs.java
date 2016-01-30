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

import java.util.logging.Logger;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.lang3.StringUtils;

public class PasswordArgs implements ArgsConfiguration {

    private static final Logger logger = Logger.getLogger(PasswordArgs.class.getName());

    private final String title;

    public Argument passwordArgument;
    public Argument environmentVariableArgument;

    private String password;

    public PasswordArgs(String title) {
        assert title != null;
        this.title = title;
    }

    public String getPassword() {
        return password;
    }

    public char[] getPasswordCharArray() {
        if (password == null) {
            return null;
        }
        return password.toCharArray();
    }

    @Deprecated
    @Override
    public void addArguments(ArgumentParser parser) {
        finalizeArguments();
    }

    public void finalizeArguments() {
        assert passwordArgument != null;
        passwordArgument.type(String.class);

        assert environmentVariableArgument != null;
        environmentVariableArgument.type(String.class);
    }

    @Override
    public void setFromNamespace(Namespace namespace) {
        assert title != null;
        assert passwordArgument != null;
        password = namespace.getString(passwordArgument.getDest());
        if (password == null) {
            // Load the password from an environment variable
            assert environmentVariableArgument != null;
            String envVar = namespace.getString(environmentVariableArgument.getDest());
            assert envVar != null; // The argument has a default value
            logger.info(String.format("%s environment variable: %s", StringUtils.capitalize(title), envVar));
            password = System.getenv(envVar);
            if (password != null) {
                logger.info(String.format("%s loaded from the environment variable %s.", StringUtils.capitalize(title), envVar));
            } else {
                logger.info(String.format("%s was not set.", StringUtils.capitalize(title)));
            }
        } else {
            logger.info(String.format("%s loaded from the command line.", StringUtils.capitalize(title)));
        }
    }

}
