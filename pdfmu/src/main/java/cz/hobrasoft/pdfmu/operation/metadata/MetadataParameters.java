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
import cz.hobrasoft.pdfmu.PdfmuUtils;
import cz.hobrasoft.pdfmu.PreferenceListComparator;
import cz.hobrasoft.pdfmu.operation.args.ArgsConfiguration;
import java.util.AbstractMap;
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

    // Table with the descriptions:
    // http://wwwimages.adobe.com/content/dam/Adobe/en/devnet/pdf/pdfs/PDF32000_2008.pdf
    // (table 317, section 14.3.3, page 550)
    private static final Map<String, String> standardProperties = PdfmuUtils.sortedMap(
            new AbstractMap.SimpleEntry<>("Title", "The document's title."),
            new AbstractMap.SimpleEntry<>("Subject", "The subject of the document."),
            new AbstractMap.SimpleEntry<>("Author", "The name of the person who created the document."),
            new AbstractMap.SimpleEntry<>("Keywords", "Keywords associated with the document."),
            new AbstractMap.SimpleEntry<>("Creator", "The name of the product that created the document in the original format."),
            new AbstractMap.SimpleEntry<>("Producer", "The name of the product that converted the document from the original format to PDF."),
            new AbstractMap.SimpleEntry<>("CreationDate", "The date and time the document was created, in human-readable form."),
            new AbstractMap.SimpleEntry<>("ModDate", "The date and time the document was most recently modified, in human-readable form."),
            new AbstractMap.SimpleEntry<>("Trapped", "Has the document been modified to include trapping information? (recommended values: True,False,Unknown)")
    );

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
            assert standardProperties.containsKey(property);
            String help = standardProperties.get(property);
            group.addArgument("--" + property)
                    .help(help)
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
                // TODO: Warn if key is Producer or ModDate
                String value = element.get(1);
                // TODO: Warn if key is Trapped and value is not one of True, False, Unknown
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
            = new PreferenceListComparator<>(standardProperties.keySet().iterator());

    public SortedMap<String, String> getSorted() {
        return propertySorter.sort(info);
    }

}
