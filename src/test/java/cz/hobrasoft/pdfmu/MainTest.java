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

import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import java.io.File;
import java.io.IOException;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

/**
 * @author Filip Bartek
 */
@RunWith(DataProviderRunner.class)
abstract public class MainTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().mute().enableLog();

    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().mute().enableLog();

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

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

}
