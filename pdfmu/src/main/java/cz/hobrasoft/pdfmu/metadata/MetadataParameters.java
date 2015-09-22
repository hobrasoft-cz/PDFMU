package cz.hobrasoft.pdfmu.metadata;

import cz.hobrasoft.pdfmu.ArgsConfiguration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

public class MetadataParameters implements ArgsConfiguration {

    private Map<String, String> info = new HashMap<>();

    public Map<String, String> getInfo() {
        return info;
    }

    @Override
    public void addArguments(ArgumentParser parser) {
        parser.addArgument("-kv", "--keyvalue")
                .help("generic metadata element")
                .nargs(2)
                .type(String.class)
                .action(Arguments.append())
                .metavar("K", "V");
    }

    @Override
    public void setFromNamespace(Namespace namespace) {
        List<List<String>> elements = namespace.getList("keyvalue");
        for (List<String> element : elements) {
            String key = element.get(0);
            String value = element.get(1);
            info.put(key, value);
        }
    }

}
