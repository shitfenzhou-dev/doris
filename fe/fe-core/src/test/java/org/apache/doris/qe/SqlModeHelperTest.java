// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.qe;

import org.apache.doris.common.DdlException;

import org.junit.Assert;
import org.junit.Test;

public class SqlModeHelperTest {

    @Test
    public void testNormal() throws DdlException {
        String sqlMode = "PIPES_AS_CONCAT";
        Assert.assertEquals(new Long(2L), SqlModeHelper.encode(sqlMode));

        sqlMode = "";
        Assert.assertEquals(new Long(0L), SqlModeHelper.encode(sqlMode));

        sqlMode = "0,1, PIPES_AS_CONCAT";
        Assert.assertEquals(new Long(3L), SqlModeHelper.encode(sqlMode));

        long sqlModeValue = 2L;
        Assert.assertEquals("PIPES_AS_CONCAT", SqlModeHelper.decode(sqlModeValue));

        sqlModeValue = 0L;
        Assert.assertEquals("", SqlModeHelper.decode(sqlModeValue));
    }

    @Test
    public void testEncodeNumeric() throws DdlException {
        // Valid mask value
        Assert.assertEquals(new Long(2L), SqlModeHelper.encode("2"));
        
        // Mixed numeric and string
        Assert.assertEquals(new Long(6L), SqlModeHelper.encode("2, ANSI_QUOTES"));
        
        // Out of allowed mask numeric value
        try {
            SqlModeHelper.encode("34359738368"); // 1L << 35
            Assert.fail("Expected DdlException");
        } catch (DdlException e) {
            // expected
        }

        // Negative numeric value
        try {
            SqlModeHelper.encode("-1");
            Assert.fail("Expected DdlException");
        } catch (DdlException e) {
            // expected
        }

        // Extremely large number that overflows long
        try {
            SqlModeHelper.encode("99999999999999999999");
            Assert.fail("Expected DdlException");
        } catch (DdlException e) {
            // expected
        }
    }

    @Test
    public void testConverterEntrance() throws DdlException {
        // Normal string combination
        Assert.assertEquals(new Long(6L), VariableVarConverters.encode("sql_mode", "PIPES_AS_CONCAT,ANSI_QUOTES"));

        // Overflow value through converter
        try {
            VariableVarConverters.encode("sql_mode", "99999999999999999999");
            Assert.fail("Expected DdlException");
        } catch (DdlException e) {
            // expected
        }
    }

    @Test(expected = DdlException.class)
    public void testInvalidSqlMode() throws DdlException {
        String sqlMode = "PIPES_AS_CONCAT, WRONG_MODE";
        SqlModeHelper.encode(sqlMode);
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testInvalidDecode() throws DdlException {
        long sqlMode = SqlModeHelper.MODE_LAST;
        SqlModeHelper.decode(sqlMode);
        Assert.fail("No exception throws");
    }
}
