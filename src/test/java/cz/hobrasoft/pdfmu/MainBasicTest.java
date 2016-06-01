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

import cz.hobrasoft.pdfmu.error.ErrorType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;

/**
 * @author Filip Bartek
 */
public class MainBasicTest extends MainTest {

    @Test
    public void testTooFewArguments() {
        exit.expectSystemExitWithStatus(ErrorType.PARSER_TOO_FEW_ARGUMENTS.getCode());
        Main.main(new String[]{});
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
