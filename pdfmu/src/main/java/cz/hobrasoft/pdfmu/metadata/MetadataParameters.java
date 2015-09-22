package cz.hobrasoft.pdfmu.metadata;

import cz.hobrasoft.pdfmu.ArgsConfiguration;
import cz.hobrasoft.pdfmu.PreferenceListComparator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
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
                .help("set the property K to the value V")
                .nargs(2)
                .type(String.class)
                .action(Arguments.append())
                .metavar("K", "V");
    }

    @Override
    public void setFromNamespace(Namespace namespace) {
        List<List<String>> elements = namespace.getList("keyvalue");
        if (elements != null) {
            for (List<String> element : elements) {
                String key = element.get(0);
                String value = element.get(1);
                info.put(key, value);
            }
        }
        }
    }

    public void setFromInfo(Map<String, String> info) {
        this.info = info;
    }

    private static final Comparator<String> propertyComparator
            = new PreferenceListComparator<>(new String[]{
        "Title", "Subject", "Author", "Keywords", "Creator", "Producer",
        "CreationDate", "ModDate", "Trapped"});

    public SortedMap<String, String> getSorted() {
        SortedMap<String, String> infoSorted = new TreeMap<>(propertyComparator);
        infoSorted.putAll(info);
        return infoSorted;
    }

}
