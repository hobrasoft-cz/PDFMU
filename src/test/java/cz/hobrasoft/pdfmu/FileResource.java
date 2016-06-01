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
import org.junit.rules.TemporaryFolder;

/**
 * @author Filip Bártek
 */
class FileResource {

    private final String resourceName;
    public final String fileName;

    public FileResource(String resourceName, String fileName) {
        assert resourceName != null;
        this.resourceName = resourceName;
        this.fileName = fileName;
    }

    public FileResource(String resourceName) {
        this(resourceName, resourceName);
    }

    public File getFile(TemporaryFolder folder) throws IOException {
        File file;
        try (InputStream in = getStream()) {
            assert in != null;
            file = MainTest.newFile(folder, fileName, true);
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
