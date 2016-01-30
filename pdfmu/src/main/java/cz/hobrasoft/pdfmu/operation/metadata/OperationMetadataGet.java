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
import cz.hobrasoft.pdfmu.jackson.MetadataGet;
import cz.hobrasoft.pdfmu.operation.Operation;
import cz.hobrasoft.pdfmu.operation.OperationCommon;
import cz.hobrasoft.pdfmu.operation.OperationException;
import cz.hobrasoft.pdfmu.operation.args.InPdfArgs;
import java.util.Map;
import java.util.SortedMap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class OperationMetadataGet extends OperationCommon {

    private final InPdfArgs in = new InPdfArgs();

    @Override
    public Subparser configureSubparser(Subparser subparser) {
        String help = "Display metadata in a PDF document";

        // Configure the subparser
        subparser.help(help)
                .description(help)
                .defaultHelp(true)
                .setDefault("command", OperationMetadataGet.class);

        in.addArguments(subparser);

        return subparser;
    }

    @Override
    public void execute(Namespace namespace) throws OperationException {
        in.setFromNamespace(namespace);

        in.open();
        SortedMap<String, String> properties = get(in.getPdfReader());
        in.close();

        writeResult(new MetadataGet(properties));
    }

    private SortedMap<String, String> get(PdfReader pdfReader) {
        Map<String, String> properties = pdfReader.getInfo();

        MetadataParameters mp = new MetadataParameters();
        mp.setFromInfo(properties);

        SortedMap<String, String> propertiesSorted = mp.getSorted();

        {
            to.indentMore("Properties:");
            for (Map.Entry<String, String> property : propertiesSorted.entrySet()) {
                String key = property.getKey();
                String value = property.getValue();
                to.println(String.format("%s: %s", key, value));
            }
            to.indentLess();
        }

        return propertiesSorted;
    }

    private static Operation instance = null;

    public static Operation getInstance() {
        if (instance == null) {
            instance = new OperationMetadataGet();
        }
        return instance;
    }

    private OperationMetadataGet() {
        // Singleton
    }

}
