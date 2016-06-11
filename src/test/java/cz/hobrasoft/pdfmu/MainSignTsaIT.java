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
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author Filip Bártek
 */
@RunWith(DataProviderRunner.class)
public class MainSignTsaIT extends MainTest {

    public interface Online {
        /* category marker */ }

    /**
     * The SSL configuration must be shared among the tests, because it cannot
     * be re-configured once it is used for the first time. The first test that
     * sets and uses SSL determines the SSL configuration for the remainder of
     * the tests. The truststore is stored internally in the JVM so the
     * truststore file need only be present during the first use.
     */
    private List<String> sslArgs() throws IOException {
        List<String> argsList = new ArrayList<>();

        File sslTruststoreFile = new FileResource("cacerts.jks").getFile(folder);
        argsList.add("--ssl-truststore");
        argsList.add(sslTruststoreFile.getAbsolutePath());
        //argsList.add("--ssl-truststore-type");
        //argsList.add("jks");
        argsList.add("--ssl-truststore-password");
        argsList.add("changeit");

        File sslKeystoreFile = new FileResource("auth-changeit.pfx").getFile(folder);
        argsList.add("--ssl-keystore");
        argsList.add(sslKeystoreFile.getAbsolutePath());
        //argsList.add("--ssl-keystore-type");
        //argsList.add("pkcs12");
        argsList.add("--ssl-keystore-password");
        argsList.add("changeit");

        return argsList;
    }

    @Test
    public void testNoUrl() throws IOException {
        final PdfFileResource inFileResource = BLANK_12_PDF;
        File inFile = inFileResource.getFile(folder);
        File keystoreFile = new FileResource("1.p12").getFile(folder);
        final File outFile = newFile("out.pdf", false);

        List<String> argsList = new ArrayList<>();
        argsList.add("sign");
        argsList.add(inFile.getAbsolutePath());
        argsList.add("--out");
        argsList.add(outFile.getAbsolutePath());
        argsList.add("--keystore");
        argsList.add(keystoreFile.getAbsolutePath());
        argsList.add("--tsa-url");

        exit.expectSystemExitWithStatus(15);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() {
                Assert.assertFalse(outFile.exists());
            }
        });
        Main.main(argsList.toArray(new String[]{}));
        assert false;
    }

    @Test
    public void testInvalidUrl() throws IOException {
        final PdfFileResource inFileResource = BLANK_12_PDF;
        File inFile = inFileResource.getFile(folder);
        File keystoreFile = new FileResource("1.p12").getFile(folder);
        final File outFile = newFile("out.pdf", false);

        List<String> argsList = new ArrayList<>();
        argsList.add("sign");
        argsList.add(inFile.getAbsolutePath());
        argsList.add("--out");
        argsList.add(outFile.getAbsolutePath());
        argsList.add("--keystore");
        argsList.add(keystoreFile.getAbsolutePath());
        argsList.add("--tsa-url");
        argsList.add("");

        exit.expectSystemExitWithStatus(91);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() {
                Assert.assertFalse(outFile.exists());
            }
        });
        Main.main(argsList.toArray(new String[]{}));
        assert false;
    }

    @Test
    @Category(Online.class)
    public void testIncorrectUrl() throws IOException {
        final PdfFileResource inFileResource = BLANK_12_PDF;
        File inFile = inFileResource.getFile(folder);
        File keystoreFile = new FileResource("1.p12").getFile(folder);
        final File outFile = newFile("out.pdf", false);

        List<String> argsList = new ArrayList<>();
        argsList.add("--output-format");
        argsList.add("json");
        argsList.add("sign");
        argsList.add(inFile.getAbsolutePath());
        argsList.add("--out");
        argsList.add(outFile.getAbsolutePath());
        argsList.add("--keystore");
        argsList.add(keystoreFile.getAbsolutePath());
        argsList.add("--tsa-url");
        argsList.add("http://example.com/");

        exit.expectSystemExitWithStatus(61);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() {
                Assert.assertFalse(outFile.exists());
                // TODO?: Check the JSON output
            }
        });
        Main.main(argsList.toArray(new String[]{}));
        assert false;
    }

    @Test
    @Category(Online.class)
    public void testNoUsername() throws IOException {
        final PdfFileResource inFileResource = BLANK_12_PDF;
        File inFile = inFileResource.getFile(folder);
        File keystoreFile = new FileResource("1.p12").getFile(folder);
        final File outFile = newFile("out.pdf", false);

        List<String> argsList = new ArrayList<>();
        argsList.add("sign");
        argsList.add(inFile.getAbsolutePath());
        argsList.add("--out");
        argsList.add(outFile.getAbsolutePath());
        argsList.add("--keystore");
        argsList.add(keystoreFile.getAbsolutePath());
        argsList.add("--tsa-url");
        argsList.add("https://www3.postsignum.cz/DEMOTSA/TSS_user/");
        argsList.addAll(sslArgs());

        exit.expectSystemExitWithStatus(63);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() {
                Assert.assertFalse(outFile.exists());
            }
        });
        Main.main(argsList.toArray(new String[]{}));
        assert false;
    }

    @Test
    @Category(Online.class)
    public void testNoPassword() throws IOException {
        final PdfFileResource inFileResource = BLANK_12_PDF;
        File inFile = inFileResource.getFile(folder);
        File keystoreFile = new FileResource("1.p12").getFile(folder);
        final File outFile = newFile("out.pdf", false);

        List<String> argsList = new ArrayList<>();
        argsList.add("sign");
        argsList.add(inFile.getAbsolutePath());
        argsList.add("--out");
        argsList.add(outFile.getAbsolutePath());
        argsList.add("--keystore");
        argsList.add(keystoreFile.getAbsolutePath());
        argsList.add("--tsa-url");
        argsList.add("https://www3.postsignum.cz/DEMOTSA/TSS_user/");
        argsList.add("--tsa-username");
        argsList.add("demoTSA");
        argsList.addAll(sslArgs());

        exit.expectSystemExitWithStatus(64);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() {
                Assert.assertFalse(outFile.exists());
            }
        });
        Main.main(argsList.toArray(new String[]{}));
        assert false;
    }

    @Test
    @Category(Online.class)
    public void testIncorrectPassword() throws IOException {
        final PdfFileResource inFileResource = BLANK_12_PDF;
        File inFile = inFileResource.getFile(folder);
        File keystoreFile = new FileResource("1.p12").getFile(folder);
        final File outFile = newFile("out.pdf", false);

        List<String> argsList = new ArrayList<>();
        argsList.add("sign");
        argsList.add(inFile.getAbsolutePath());
        argsList.add("--out");
        argsList.add(outFile.getAbsolutePath());
        argsList.add("--keystore");
        argsList.add(keystoreFile.getAbsolutePath());
        argsList.add("--tsa-url");
        argsList.add("https://www3.postsignum.cz/DEMOTSA/TSS_user/");
        argsList.add("--tsa-username");
        argsList.add("demoTSA");
        argsList.add("--tsa-password");
        argsList.add("incorrect-password");
        argsList.addAll(sslArgs());

        exit.expectSystemExitWithStatus(64);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() {
                Assert.assertFalse(outFile.exists());
            }
        });
        Main.main(argsList.toArray(new String[]{}));
        assert false;
    }

    @DataProvider
    public static Object[][] dataProviderUsernamePasswordSuccess() {
        return new Object[][]{
            new Object[]{"https://www3.postsignum.cz/DEMOTSA/TSS_user/", "demoTSA", "demoTSA2010"},
            new Object[]{"https://bteszt.e-szigno.hu/tsa", "teszt", "teszt"}
        };
    }

    @Test
    @Category(Online.class)
    @UseDataProvider
    public void testUsernamePasswordSuccess(String url, String username, String password) throws IOException {
        assert url != null;
        assert username != null;
        assert password != null;

        final PdfFileResource inFileResource = BLANK_12_PDF;
        File inFile = inFileResource.getFile(folder);
        File keystoreFile = new FileResource("1.p12").getFile(folder);
        final File outFile = newFile("out.pdf", false);

        List<String> argsList = new ArrayList<>();
        argsList.add("sign");
        argsList.add(inFile.getAbsolutePath());
        argsList.add("--out");
        argsList.add(outFile.getAbsolutePath());
        argsList.add("--keystore");
        argsList.add(keystoreFile.getAbsolutePath());
        argsList.add("--tsa-url");
        argsList.add(url);
        argsList.add("--tsa-username");
        argsList.add(username);
        argsList.add("--tsa-password");
        argsList.add(password);
        argsList.addAll(sslArgs());

        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() {
                Assert.assertTrue(outFile.exists());
                // TODO?: Inspect output PDF file
            }
        });
        Main.main(argsList.toArray(new String[]{}));
        assert false;
    }

    @Test
    @Category(Online.class)
    public void testCertificateSuccess() throws IOException {
        final PdfFileResource inFileResource = BLANK_12_PDF;
        File inFile = inFileResource.getFile(folder);
        File keystoreFile = new FileResource("1.p12").getFile(folder);
        final File outFile = newFile("out.pdf", false);

        List<String> argsList = new ArrayList<>();
        argsList.add("sign");
        argsList.add(inFile.getAbsolutePath());
        argsList.add("--out");
        argsList.add(outFile.getAbsolutePath());
        argsList.add("--keystore");
        argsList.add(keystoreFile.getAbsolutePath());
        argsList.add("--tsa-url");
        argsList.add("https://teszt.e-szigno.hu/tsa");
        argsList.addAll(sslArgs());

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
