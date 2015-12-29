package cz.hobrasoft.pdfmu.operation.args;

import java.util.logging.Logger;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.lang3.StringUtils;

public class PasswordArgs implements ArgsConfiguration {

    private static final Logger logger = Logger.getLogger(PasswordArgs.class.getName());

    private final String title;
    private final String passArgNameShort;
    private final String passArgNameLong;
    private final String passArgHelp;
    private final String envvarArgNameShort;
    private final String envvarArgNameLong;
    private final String envvarArgHelp;
    private final String envvarArgDefault;

    private String password = null;

    public String getPassword() {
        return password;
    }

    public char[] getPasswordCharArray() {
        if (password == null) {
            return null;
        }
        return password.toCharArray();
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

    private static Argument addArgument(MutuallyExclusiveGroup group, String nameShort, String nameLong) {
        assert nameLong != null;
        if (nameShort != null) {
            return group.addArgument("-" + nameShort, "--" + nameLong);
        } else {
            return group.addArgument("--" + nameLong);
        }
    }

    @Override
    public void addArguments(ArgumentParser parser) {
        MutuallyExclusiveGroup keypassGroup = parser.addMutuallyExclusiveGroup(title);
        // TODO?: Add description that states that the arguments are mutually exclusive
        // and that `passArg` takes precedence to `envvarArg`.
        addArgument(keypassGroup, passArgNameShort, passArgNameLong)
                .help(passArgHelp)
                .type(String.class);
        addArgument(keypassGroup, envvarArgNameShort, envvarArgNameLong)
                .help(envvarArgHelp)
                .type(String.class)
                .setDefault(envvarArgDefault);
    }

    @Override
    public void setFromNamespace(Namespace namespace) {
        password = namespace.getString(passArgNameLong.replace('-', '_'));
        if (password == null) {
            // Load the password from an environment variable
            String envVar = namespace.getString(envvarArgNameLong.replace('-', '_'));
            assert envVar != null; // The argument has a default value
            logger.info(String.format("%s environment variable: %s", StringUtils.capitalize(title), envVar));
            password = System.getenv(envVar);
            if (password != null) {
                logger.info(String.format("%s loaded from the environment variable %s.", StringUtils.capitalize(title), envVar));
            } else {
                logger.info(String.format("%s was not set; using empty password.", StringUtils.capitalize(title)));
            }
        } else {
            logger.info(String.format("%s loaded from the command line option --%s.", StringUtils.capitalize(title), passArgNameLong));
        }
    }

}
