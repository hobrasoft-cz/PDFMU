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

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.runner.RunWith;

/**
 * @author Filip Bártek
 */
@RunWith(DataProviderRunner.class)
public class MainSignTest extends MainTest {

    @Test
    public void testNoInput() throws IOException {
        String[] args = new String[]{
            "sign"
        };
        exit.expectSystemExitWithStatus(14);
        Main.main(args);
        assert false;
    }

    @Test
    public void testNoKeystore() throws IOException {
        File inFile = BLANK_12_PDF.getFile(folder);
        final File outFile = newFile("out.pdf", false);

        String[] args = new String[]{
            "sign",
            inFile.getAbsolutePath(),
            "--out",
            outFile.getAbsolutePath()
        };

        exit.expectSystemExitWithStatus(41);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() {
                Assert.assertFalse(outFile.exists());
            }
        });
        Main.main(args);
        assert false;
    }

    @DataProvider
    public static Object[][] dataProviderEmpty() {
        return new Object[][]{
            new Object[]{"empty.p12", null},
            new Object[]{"empty.pfx", null},
            new Object[]{"empty.jks", null},
            new Object[]{"empty.jceks", "jceks"}
        };
    }

    @Test
    @UseDataProvider
    public void testEmpty(String keystoreFileName, String keystoreType)
            throws IOException {
        File inFile = BLANK_12_PDF.getFile(folder);
        File keystoreFile = new FileResource(keystoreFileName).getFile(folder);
        final File outFile = newFile("out.pdf", false);

        List<String> argsList = new ArrayList<>();
        argsList.add("sign");
        argsList.add(inFile.getAbsolutePath());
        argsList.add("--out");
        argsList.add(outFile.getAbsolutePath());
        argsList.add("--keystore");
        argsList.add(keystoreFile.getAbsolutePath());
        if (keystoreType != null) {
            argsList.add("--keystore-type");
            argsList.add(keystoreType);
        }

        exit.expectSystemExitWithStatus(51);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() {
                Assert.assertFalse(outFile.exists());
            }
        });
        Main.main(argsList.toArray(new String[]{}));
        assert false;
    }
}
