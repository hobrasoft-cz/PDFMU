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
import cz.hobrasoft.pdfmu.jackson.CertificateResult;
import cz.hobrasoft.pdfmu.jackson.Inspect;
import cz.hobrasoft.pdfmu.jackson.Signature;
import cz.hobrasoft.pdfmu.jackson.SignatureMetadata;
import cz.hobrasoft.pdfmu.operation.OperationException;
import cz.hobrasoft.pdfmu.operation.OperationInspect;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;

/**
 * @author Filip Bártek
 */
@RunWith(DataProviderRunner.class)
public class MainSignTest extends MainTest {

    @Rule
    public final EnvironmentVariables environmentVariables
            = new EnvironmentVariables();

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

    private static void assertDateBetween(Date actual, Date begin, Date end) {
        assert actual != null;
        assert begin != null;
        // The dates saved in PDF files only have second precision,
        // while new Date() has millisecond precision.
        // We compare with a 1 second delta to compensate
        Assert.assertTrue(actual.getTime() - begin.getTime()
                > TimeUnit.SECONDS.toMillis(-1));
        assert end != null;
        Assert.assertFalse(actual.after(end));
    }

    // Example content of the properties dictionary:
    // Producer => iText® 5.5.6 ©2000-2015 iText Group NV (AGPL-version); modified using iText® 5.5.6 ©2000-2015 iText Group NV (AGPL-version)
    // CreationDate => D:20160525204745+02'00'
    // ModDate => D:20160601102240+02'00'
    private static void assertDefaultPropertiesValid(
            Map<String, String> properties, String expectedCreationDate,
            Date modDateBegin, Date modDateEnd) throws ParseException {
        Assert.assertNotNull(properties);
        Assert.assertEquals(3, properties.size());
        Assert.assertTrue(properties.containsKey("Producer"));
        String expectedProducer = "iText® 5.5.6 ©2000-2015 iText Group NV (AGPL-version); modified using iText® 5.5.6 ©2000-2015 iText Group NV (AGPL-version)";
        Assert.assertEquals(expectedProducer, properties.get("Producer"));
        Assert.assertTrue(properties.containsKey("CreationDate"));
        assert expectedCreationDate != null;
        Assert.assertEquals(expectedCreationDate, properties.get("CreationDate"));
        Assert.assertTrue(properties.containsKey("ModDate"));
        DateFormat dateFormat = new SimpleDateFormat("'D:'yyyyMMddHHmmssX'''00'''");
        Date actualModDate = dateFormat.parse(properties.get("ModDate"));
        assertDateBetween(actualModDate, modDateBegin, modDateEnd);
    }

    @Test
    public void test1p12() throws IOException {
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

        final Date modDateBegin = new Date();

        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws OperationException, IOException, ParseException {
                Date modDateEnd = new Date();
                Assert.assertTrue(outFile.exists());
                Inspect inspect = OperationInspect.getInstance().execute(outFile);
                assert inspect != null;
                assert inFileResource.version != null;
                Assert.assertEquals(inFileResource.version, inspect.version);
                assertDefaultPropertiesValid(inspect.properties,
                        inFileResource.creationDate, modDateBegin, modDateEnd);
                Assert.assertNotNull(inspect.signatures);
                Assert.assertEquals(1, inspect.signatures.nRevisions.intValue());
                Assert.assertNotNull(inspect.signatures.signatures);
                Assert.assertEquals(1, inspect.signatures.signatures.size());
                {
                    Signature signature = inspect.signatures.signatures.get(0);
                    Assert.assertNotNull(signature);
                    Assert.assertEquals("Signature1", signature.id);
                    Assert.assertTrue(signature.coversWholeDocument);
                    Assert.assertEquals(1, signature.revision.intValue());
                    {
                        SignatureMetadata metadata = signature.metadata;
                        Assert.assertNotNull(metadata);
                        Assert.assertNull(metadata.name);
                        Assert.assertEquals("", metadata.reason);
                        Assert.assertEquals("", metadata.location);
                        Assert.assertNotNull(metadata.date);
                        DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
                        // Example: "Wed Jun 01 11:22:38 CEST 2016"
                        Date actualDate = dateFormat.parse(metadata.date);
                        assertDateBetween(actualDate, modDateBegin, modDateEnd);
                    }
                    Assert.assertNotNull(signature.certificates);
                    Assert.assertEquals(1, signature.certificates.size());
                    {
                        CertificateResult certificate = signature.certificates.get(0);
                        Assert.assertNotNull(certificate);
                        Assert.assertEquals("X.509", certificate.type);
                        Assert.assertTrue(certificate.selfSigned);
                        Map<String, List<String>> dn = new LinkedHashMap<>();
                        dn.put("CN", Arrays.asList(new String[]{"CN1"}));
                        dn.put("E", Arrays.asList(new String[]{"E1"}));
                        dn.put("OU", Arrays.asList(new String[]{"OU1"}));
                        dn.put("O", Arrays.asList(new String[]{"O1"}));
                        dn.put("L", Arrays.asList(new String[]{"L1"}));
                        dn.put("ST", Arrays.asList(new String[]{"ST1"}));
                        dn.put("C", Arrays.asList(new String[]{"C1"}));
                        Assert.assertEquals(dn, certificate.subject);
                        Assert.assertEquals(dn, certificate.issuer);
                    }
                }
            }
        });
        Main.main(argsList.toArray(new String[]{}));
        assert false;
    }

    @Test
    public void testKeyIncorrect() throws IOException {
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
        argsList.add("--key-alias");
        argsList.add("incorrect-alias");

        exit.expectSystemExitWithStatus(52);
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
    public void testKeyCorrect() throws IOException {
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
        argsList.add("--key-alias");
        argsList.add("cn1");

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

    @Test
    public void testPasswordMissing() throws IOException {
        final PdfFileResource inFileResource = BLANK_12_PDF;
        File inFile = inFileResource.getFile(folder);
        File keystoreFile = new FileResource("1-changeit.p12").getFile(folder);
        final File outFile = newFile("out.pdf", false);

        List<String> argsList = new ArrayList<>();
        argsList.add("sign");
        argsList.add(inFile.getAbsolutePath());
        argsList.add("--out");
        argsList.add(outFile.getAbsolutePath());
        argsList.add("--keystore");
        argsList.add(keystoreFile.getAbsolutePath());
        argsList.add("--keystore-type");
        argsList.add("pkcs12");

        exit.expectSystemExitWithStatus(43);
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
    public void testPasswordIncorrect() throws IOException {
        final PdfFileResource inFileResource = BLANK_12_PDF;
        File inFile = inFileResource.getFile(folder);
        File keystoreFile = new FileResource("1-changeit.p12").getFile(folder);
        final File outFile = newFile("out.pdf", false);

        List<String> argsList = new ArrayList<>();
        argsList.add("sign");
        argsList.add(inFile.getAbsolutePath());
        argsList.add("--out");
        argsList.add(outFile.getAbsolutePath());
        argsList.add("--keystore");
        argsList.add(keystoreFile.getAbsolutePath());
        argsList.add("--keystore-type");
        argsList.add("pkcs12");
        argsList.add("--keystore-password");
        argsList.add("incorrect-password");

        exit.expectSystemExitWithStatus(43);
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
    public void testPasswordCmdlineSuccess() throws IOException {
        final PdfFileResource inFileResource = BLANK_12_PDF;
        File inFile = inFileResource.getFile(folder);
        File keystoreFile = new FileResource("1-changeit.p12").getFile(folder);
        final File outFile = newFile("out.pdf", false);

        List<String> argsList = new ArrayList<>();
        argsList.add("sign");
        argsList.add(inFile.getAbsolutePath());
        argsList.add("--out");
        argsList.add(outFile.getAbsolutePath());
        argsList.add("--keystore");
        argsList.add(keystoreFile.getAbsolutePath());
        argsList.add("--keystore-type");
        argsList.add("pkcs12");
        argsList.add("--keystore-password");
        argsList.add("changeit");

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

    @Test
    public void testPasswordEnvvarDefaultSuccess() throws IOException {
        final PdfFileResource inFileResource = BLANK_12_PDF;
        File inFile = inFileResource.getFile(folder);
        File keystoreFile = new FileResource("1-changeit.p12").getFile(folder);
        final File outFile = newFile("out.pdf", false);

        List<String> argsList = new ArrayList<>();
        argsList.add("sign");
        argsList.add(inFile.getAbsolutePath());
        argsList.add("--out");
        argsList.add(outFile.getAbsolutePath());
        argsList.add("--keystore");
        argsList.add(keystoreFile.getAbsolutePath());
        argsList.add("--keystore-type");
        argsList.add("pkcs12");

        environmentVariables.set("PDFMU_STOREPASS", "changeit");

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

    @Test
    public void testPasswordEnvvarCustomSuccess() throws IOException {
        final PdfFileResource inFileResource = BLANK_12_PDF;
        File inFile = inFileResource.getFile(folder);
        File keystoreFile = new FileResource("1-changeit.p12").getFile(folder);
        final File outFile = newFile("out.pdf", false);

        List<String> argsList = new ArrayList<>();
        argsList.add("sign");
        argsList.add(inFile.getAbsolutePath());
        argsList.add("--out");
        argsList.add(outFile.getAbsolutePath());
        argsList.add("--keystore");
        argsList.add(keystoreFile.getAbsolutePath());
        argsList.add("--keystore-type");
        argsList.add("pkcs12");
        argsList.add("--keystore-password-envvar");
        argsList.add("PDFMU_STOREPASS_CUSTOM_ENVVAR");

        environmentVariables.set("PDFMU_STOREPASS_CUSTOM_ENVVAR", "changeit");

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
