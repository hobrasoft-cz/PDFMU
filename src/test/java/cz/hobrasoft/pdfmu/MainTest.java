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

import cz.hobrasoft.pdfmu.jackson.Inspect;
import cz.hobrasoft.pdfmu.jackson.SignatureDisplay;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.rules.TemporaryFolder;

/**
 * @author Filip Bartek
 */
abstract public class MainTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().mute().enableLog();

    /**
     * Ensures that the directory is deleted after it has been used.
     */
    private static class StrictTemporaryFolder extends TemporaryFolder {

        @Override
        protected void after() {
            super.after();
            assert !getRoot().exists();
        }
    }

    @Rule
    public final TemporaryFolder folder = new StrictTemporaryFolder();

    protected File newFile(String fileName, boolean exists) throws IOException {
        return newFile(folder, fileName, exists);
    }

    public static File newFile(TemporaryFolder folder, String fileName,
            boolean exists) throws IOException {
        // TODO: Check whether this works if fileName is null
        final File file = folder.newFile(fileName);
        assert file != null;
        assert file.exists();
        if (!exists) {
            final boolean success = file.delete();
            assert success;
        }
        assert file.exists() == exists;
        return file;
    }

    private static final List<String> IGNORED_PROPERTIES
            = Arrays.asList(new String[]{"Producer", "ModDate", "CreationDate"});

    protected static Inspect newInspect(String version) {
        Inspect inspect = new Inspect();
        inspect.version = version;
        inspect.properties = new HashMap<>();
        for (String property : IGNORED_PROPERTIES) {
            inspect.properties.put(property, null);
        }
        inspect.signatures = new SignatureDisplay();
        inspect.signatures.nRevisions = 0;
        inspect.signatures.signatures = new ArrayList<>();
        return inspect;
    }

    private static void assertEqualsProperties(
            final Map<String, String> expected,
            final Map<String, String> actual) {
        Assert.assertNotNull(expected);
        Assert.assertNotNull(actual);
        Assert.assertEquals(expected.keySet(), actual.keySet());
        Set<String> keySet = new HashSet<>(expected.keySet());
        keySet.removeAll(IGNORED_PROPERTIES);
        for (String key : keySet) {
            Assert.assertEquals(expected.get(key), actual.get(key));
        }
    }

    protected static void assertEquals(final Inspect expected, final Inspect actual) {
        Assert.assertEquals(expected.version, actual.version);
        assertEqualsProperties(expected.properties, actual.properties);
        Assert.assertEquals(expected.signatures, actual.signatures);
    }

    protected static final PdfFileResource BLANK_12_PDF
            = new PdfFileResource("blank-12.pdf", "1.2", "D:20160525204745+02'00'");
}
