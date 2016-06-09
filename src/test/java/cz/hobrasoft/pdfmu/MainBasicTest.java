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
import cz.hobrasoft.pdfmu.error.ErrorType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.runner.RunWith;

/**
 * @author Filip Bartek
 */
@RunWith(DataProviderRunner.class)
public class MainBasicTest extends MainTest {

    @DataProvider
    public static Object[][] dataProviderParserError() {
        return new Object[][]{
            new Object[]{new String[]{"--unrecognized-argument"}, ErrorType.PARSER_UNRECOGNIZED_ARGUMENT.getCode()},
            new Object[]{new String[]{"--output-format", "invalid-format"}, ErrorType.PARSER_INVALID_CHOICE.getCode()},
            new Object[]{new String[]{"unrecognized-command"}, ErrorType.PARSER_UNRECOGNIZED_COMMAND.getCode()},
            new Object[]{new String[]{}, ErrorType.PARSER_TOO_FEW_ARGUMENTS.getCode()},
            new Object[]{new String[]{"--output-format"}, ErrorType.PARSER_EXPECTED_ONE_ARGUMENT.getCode()}
        };
    }

    @Test
    @UseDataProvider
    public void testParserError(String[] args, int exitStatus) {
        exit.expectSystemExitWithStatus(exitStatus);
        Main.main(args);
        assert false;
    }

    @Test
    public void testVersion() {
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() {
                // The terminating line break is introduced by Argparse4j
                Assert.assertEquals(String.format("%1$s\n", Main.getProjectVersion()),
                        systemOutRule.getLogWithNormalizedLineSeparator());
            }
        });
        Main.main(new String[]{"--version"});
        assert false;
    }

    @Test
    public void testHelp() {
        exit.expectSystemExitWithStatus(0);
        Main.main(new String[]{"--help"});
        assert false;
    }

    @Test
    public void testLegalNotice() {
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() {
                Assert.assertEquals(Main.getLegalNotice(),
                        systemOutRule.getLogWithNormalizedLineSeparator());
            }
        });
        Main.main(new String[]{"--legal-notice"});
        assert false;
    }

}
