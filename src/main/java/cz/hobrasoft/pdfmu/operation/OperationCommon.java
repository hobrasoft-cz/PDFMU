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
package cz.hobrasoft.pdfmu.operation;

import cz.hobrasoft.pdfmu.TextOutput;
import cz.hobrasoft.pdfmu.WritingMapper;
import cz.hobrasoft.pdfmu.jackson.Result;
import cz.hobrasoft.pdfmu.jackson.RpcResponse;
import java.io.IOException;
import java.util.logging.Logger;

public abstract class OperationCommon implements Operation {

    private WritingMapper wm = null;
    protected TextOutput to = new TextOutput(); // Discard messages by default
    private static final Logger logger = Logger.getLogger(OperationCommon.class.getName());

    @Override
    public void setWritingMapper(WritingMapper wm) {
        this.wm = wm;
    }

    protected void writeResult(Result result) {
        // Discard value if mapper was not set
        if (wm != null) {
            RpcResponse response = new RpcResponse(result);
            try {
                wm.writeValue(response);
            } catch (IOException ex) {
                logger.severe(String.format("Cannot write JSON document: %s", ex));
            }
        }
    }

    @Override
    public void setTextOutput(TextOutput to) {
        this.to = to;
    }
}
