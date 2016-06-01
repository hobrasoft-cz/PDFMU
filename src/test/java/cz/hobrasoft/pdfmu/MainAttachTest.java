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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;

/**
 * Since {@link cz.hobrasoft.pdfmu.operation.OperationInspect} does not inspect
 * the attachments, we cannot test the {@code attach} operation properly. We
 * cannot compare the result bit-by-bit either because it differs in ModDate. We
 * only test whether a single basic call succeeds.
 *
 * @author Filip Bartek
 */
public class MainAttachTest extends MainTest {

    @Test
    public void testAttach() throws IOException {
        List<String> argsList = new ArrayList<>();
        argsList.add("attach");
        File inputFile = BLANK_12_PDF.getFile(folder);
        argsList.add(inputFile.getAbsolutePath());
        FileResource attachmentFileResource = new FileResource("blank.txt");
        File attachmentFile = attachmentFileResource.getFile(folder);
        argsList.add(attachmentFile.getAbsolutePath());
        argsList.add("--out");
        final File outFile = newFile("out.pdf", false);
        argsList.add(outFile.getAbsolutePath());
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() {
                Assert.assertTrue(outFile.exists());
            }
        });
        Main.main(argsList.toArray(new String[]{}));
        assert false;
    }
}
