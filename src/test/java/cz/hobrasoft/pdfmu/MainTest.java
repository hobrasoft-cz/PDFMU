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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TemporaryFolder;

/**
 * @author Filip Bartek
 */
abstract public class MainTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().mute().enableLog();

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

    protected static File newFile(TemporaryFolder folder, String fileName,
            boolean exists) throws IOException {
        // TODO: Check whether this works if fileName is null
        final File file = folder.newFile(fileName);
        assert file.exists();
        if (!exists) {
            final boolean success = file.delete();
            assert success;
        }
        assert file.exists() == exists;
        return file;
    }

    protected static class FileResource {

        private final String resourceName;
        private final String fileName;

        public FileResource(final String resourceName, final String fileName) {
            assert resourceName != null;
            this.resourceName = resourceName;
            this.fileName = fileName;
        }

        public FileResource(final String resourceName) {
            this(resourceName, resourceName);
        }

        public File getFile(TemporaryFolder folder) throws IOException {
            File file;
            try (InputStream in = getStream()) {
                assert in != null;
                file = newFile(folder, fileName, true);
                assert file.exists();
                try (OutputStream out = new FileOutputStream(file)) {
                    assert out != null;
                    IOUtils.copy(in, out);
                }
            }
            return file;
        }

        public InputStream getStream() {
            ClassLoader classLoader = this.getClass().getClassLoader();
            assert resourceName != null;
            InputStream in = classLoader.getResourceAsStream(resourceName);
            assert in != null;
            return in;
        }
    }

}
