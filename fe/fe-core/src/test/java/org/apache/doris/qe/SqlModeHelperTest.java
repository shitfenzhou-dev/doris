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

    @Test
    public void testValidNumericInput() throws DdlException {
        Assert.assertEquals(new Long(0L), SqlModeHelper.encode("0"));
        Assert.assertEquals(new Long(1L), SqlModeHelper.encode("1"));
        Assert.assertEquals(new Long(2L), SqlModeHelper.encode("2"));
        Assert.assertEquals(new Long(32L), SqlModeHelper.encode("32"));
    }

    @Test
    public void testValidStringCombination() throws DdlException {
        long result = SqlModeHelper.encode("PIPES_AS_CONCAT,ANSI_QUOTES");
        Assert.assertEquals(new Long(6L), result);

        result = SqlModeHelper.encode("ANSI");
        Assert.assertTrue((result & SqlModeHelper.MODE_PIPES_AS_CONCAT) != 0);
        Assert.assertTrue((result & SqlModeHelper.MODE_ANSI_QUOTES) != 0);

        result = SqlModeHelper.encode("TRADITIONAL");
        Assert.assertTrue((result & SqlModeHelper.MODE_STRICT_TRANS_TABLES) != 0);
        Assert.assertTrue((result & SqlModeHelper.MODE_STRICT_ALL_TABLES) != 0);
    }

    @Test
    public void testMixedNumericAndString() throws DdlException {
        long result = SqlModeHelper.encode("2, ANSI_QUOTES");
        Assert.assertEquals(new Long(6L), result);
    }

    @Test(expected = DdlException.class)
    public void testOverflowNumericViaHelper() throws DdlException {
        SqlModeHelper.encode("99999999999999999999");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testNegativeNumericViaHelper() throws DdlException {
        SqlModeHelper.encode("-1");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testNegativeLargeNumericViaHelper() throws DdlException {
        SqlModeHelper.encode("-99999999999999999999");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testInvalidMaskViaHelper() throws DdlException {
        SqlModeHelper.encode(String.valueOf(SqlModeHelper.MODE_LAST));
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testOverflowNumericViaConverter() throws DdlException {
        VariableVarConverters.encode(SessionVariable.SQL_MODE, "99999999999999999999");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testNegativeNumericViaConverter() throws DdlException {
        VariableVarConverters.encode(SessionVariable.SQL_MODE, "-1");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testInvalidMaskViaConverter() throws DdlException {
        VariableVarConverters.encode(
                SessionVariable.SQL_MODE, String.valueOf(SqlModeHelper.MODE_LAST));
        Assert.fail("No exception throws");
    }

    @Test
    public void testValidNumericViaConverter() throws DdlException {
        Assert.assertEquals(new Long(2L),
                VariableVarConverters.encode(SessionVariable.SQL_MODE, "2"));
        Assert.assertEquals(new Long(0L),
                VariableVarConverters.encode(SessionVariable.SQL_MODE, "0"));
    }

    @Test
    public void testValidStringViaConverter() throws DdlException {
        Assert.assertEquals(new Long(2L),
                VariableVarConverters.encode(SessionVariable.SQL_MODE, "PIPES_AS_CONCAT"));
    }

    @Test
    public void testDecodeViaConverter() throws DdlException {
        Assert.assertEquals("PIPES_AS_CONCAT",
                VariableVarConverters.decode(SessionVariable.SQL_MODE, 2L));
        Assert.assertEquals("",
                VariableVarConverters.decode(SessionVariable.SQL_MODE, 0L));
    }
}
