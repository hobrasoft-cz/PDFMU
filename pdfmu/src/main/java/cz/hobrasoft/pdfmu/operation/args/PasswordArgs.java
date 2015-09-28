package cz.hobrasoft.pdfmu.operation.args;

import cz.hobrasoft.pdfmu.Console;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.lang3.StringUtils;

public class PasswordArgs implements ArgsConfiguration {

    private final String title;
    private final String passArgNameShort;
    private final String passArgNameLong;
    private final String passArgHelp;
    private final String envvarArgNameShort;
    private final String envvarArgNameLong;
    private final String envvarArgHelp;
    private final String envvarArgDefault;

    private char[] password = null;

    public char[] getPassword() {
        return password;
    }

    public PasswordArgs(String title,
            String passArgNameShort,
            String passArgNameLong,
            String passArgHelp,
            String envvarArgNameShort,
            String envvarArgNameLong,
            String envvarArgHelp,
            String envvarArgDefault) {
        this.title = title;
        this.passArgNameShort = passArgNameShort;
        this.passArgNameLong = passArgNameLong;
        this.passArgHelp = passArgHelp;
        this.envvarArgNameShort = envvarArgNameShort;
        this.envvarArgNameLong = envvarArgNameLong;
        this.envvarArgHelp = envvarArgHelp;
        this.envvarArgDefault = envvarArgDefault;
    }

    @Override
    public void addArguments(ArgumentParser parser) {
        MutuallyExclusiveGroup keypassGroup = parser.addMutuallyExclusiveGroup(title);
        // TODO?: Add description that states that the arguments are mutually exclusive
        // and that `passArg` takes precedence to `envvarArg`.
        keypassGroup.addArgument("-" + passArgNameShort, "--" + passArgNameLong)
                .help(passArgHelp)
                .type(String.class);
        keypassGroup.addArgument("-" + envvarArgNameShort, "--" + envvarArgNameLong)
                .help(envvarArgHelp)
                .type(String.class)
                .setDefault(envvarArgDefault);
    }

    @Override
    public void setFromNamespace(Namespace namespace) {
        String passwordString = namespace.getString(passArgNameLong);
        if (passwordString == null) {
            // Load the password from an environment variable
            String envVar = namespace.getString(envvarArgNameLong);
            assert envVar != null; // The argument has a default value
            Console.println(String.format("%s environment variable: %s", StringUtils.capitalize(title), envVar));
            passwordString = System.getenv(envVar);
            if (passwordString != null) {
                Console.println(String.format("%s loaded from the environment variable %s.", StringUtils.capitalize(title), envVar));
            } else {
                Console.println(String.format("%s was not set; using empty password.", StringUtils.capitalize(title)));
            }
        } else {
            Console.println(String.format("%s loaded from the command line option --%s.", StringUtils.capitalize(title), passArgNameLong));
        }
        if (passwordString != null) {
            password = passwordString.toCharArray();
        } else {
            password = null;
        }
    }

}
