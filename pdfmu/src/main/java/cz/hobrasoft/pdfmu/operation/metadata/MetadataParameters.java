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
package cz.hobrasoft.pdfmu.operation.metadata;

import com.itextpdf.text.pdf.PdfReader;
import cz.hobrasoft.pdfmu.MapSorter;
import cz.hobrasoft.pdfmu.PreferenceListComparator;
import cz.hobrasoft.pdfmu.operation.args.ArgsConfiguration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
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
    // TODO: Check spelling of "settable"
    private static final List<String> standardSettableProperties = Arrays.asList(new String[]{
        "Title", "Subject", "Author", "Keywords", "Creator", "CreationDate", "Trapped"});

    @Override
    public void addArguments(ArgumentParser parser) {
        // Remove all properties
        parser.addArgument("--clear-all")
                .help("clear all properties")
                .type(boolean.class)
                .action(Arguments.storeTrue());

        parser.addArgument("-c", "--clear")
                .help("clear the property P")
                .metavar("P")
                .type(String.class)
                .action(Arguments.append());

        // Generic properties
        parser.addArgument("-s", "--set")
                .help("set the property P to the value V")
                .metavar("P", "V")
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
        clearall = namespace.getBoolean("clear_all");

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
        List<List<String>> elements = namespace.getList("set");
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

    private static final MapSorter<String> propertySorter
            = new PreferenceListComparator<>(standardProperties);

    public SortedMap<String, String> getSorted() {
        SortedMap<String, String> infoSorted = propertySorter.sort(info);
        return infoSorted;
    }

}
