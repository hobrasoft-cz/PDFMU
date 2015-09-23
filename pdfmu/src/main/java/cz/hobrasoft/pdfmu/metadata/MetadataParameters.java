package cz.hobrasoft.pdfmu.metadata;

import com.itextpdf.text.pdf.PdfReader;
import cz.hobrasoft.pdfmu.ArgsConfiguration;
import cz.hobrasoft.pdfmu.PreferenceListComparator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

public class MetadataParameters implements ArgsConfiguration {

    private Map<String, String> info = new HashMap<>();
    private boolean clearall = false;

    public Map<String, String> getInfo(PdfReader pdfReader) {
        if (clearall) {
            // We need the keys that are already set in the input file.
            Map<String, String> inInfo = pdfReader.getInfo();
            Map<String, String> res = new HashMap<>();
            for (String key : inInfo.keySet()) {
                // Unset the property `key`
                // TODO?: Do not unset the properties "Producer" and "ModDate"
                res.put(key, null);
            }
            // Set all the properties in `info`, possibly overwriting the unset properties
            res.putAll(info);
            return res;
        }
        return info;
    }

    private static final List<String> standardProperties = Arrays.asList(new String[]{
        "Title", "Subject", "Author", "Keywords", "Creator", "Producer",
        "CreationDate", "ModDate", "Trapped"});

    // iText does not let us set the Producer property.
    // The ModDate property also seems to be set automatically.
    private static final List<String> standardSettableProperties = Arrays.asList(new String[]{
        "Title", "Subject", "Author", "Keywords", "Creator", "CreationDate", "Trapped"});

    @Override
    public void addArguments(ArgumentParser parser) {
        // Remove all properties
        parser.addArgument("-ca", "--clearall")
                .help("clear all properties")
                .type(boolean.class)
                .action(Arguments.storeTrue());

        parser.addArgument("-c", "--clear")
                .help("clear the property P")
                .metavar("P")
                .nargs("?")
                .type(String.class)
                .action(Arguments.append());

        // Generic properties
        parser.addArgument("-kv", "--keyvalue")
                .help("set the property K to the value V")
                .metavar("K", "V")
                .nargs(2)
                .type(String.class)
                .action(Arguments.append());

        // Standard properties
        ArgumentGroup group = parser.addArgumentGroup("standard properties");
        for (String property : standardSettableProperties) {
            group.addArgument("--" + property)
                    .help(property) // TODO: Change to something sensible
                    .type(String.class);
        }
    }

    @Override
    public void setFromNamespace(Namespace namespace) {
        clearall = namespace.getBoolean("clearall");

        // Clear the selected properties
        { // clearedProperties
            List<String> clearedProperties = namespace.getList("clear");
            if (clearedProperties != null) {
                for (String p : clearedProperties) {
                    info.put(p, null);
                }
            }
        }

        // Generic properties
        List<List<String>> elements = namespace.getList("keyvalue");
        if (elements != null) {
            for (List<String> element : elements) {
                String key = element.get(0);
                String value = element.get(1);
                info.put(key, value);
            }
        }

        // Standard properties
        for (String property : standardSettableProperties) {
            String value = namespace.getString(property);
            if (value != null) {
                info.put(property, value);
            }
        }
    }

    public void setFromInfo(Map<String, String> info) {
        this.info = info;
    }

    private static final Comparator<String> propertyComparator
            = new PreferenceListComparator<>(standardProperties);

    public SortedMap<String, String> getSorted() {
        SortedMap<String, String> infoSorted = new TreeMap<>(propertyComparator);
        infoSorted.putAll(info);
        return infoSorted;
    }

}
