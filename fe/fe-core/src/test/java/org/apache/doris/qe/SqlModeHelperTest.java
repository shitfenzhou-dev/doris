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
    public void testCaseInsensitive() throws DdlException {
        String sqlMode = "pipes_as_concat";
        Assert.assertEquals(new Long(2L), SqlModeHelper.encode(sqlMode));

        sqlMode = "ANSI";
        long expected = SqlModeHelper.MODE_ANSI | SqlModeHelper.MODE_REAL_AS_FLOAT 
                | SqlModeHelper.MODE_PIPES_AS_CONCAT | SqlModeHelper.MODE_ANSI_QUOTES 
                | SqlModeHelper.MODE_IGNORE_SPACE | SqlModeHelper.MODE_ONLY_FULL_GROUP_BY;
        Assert.assertEquals(new Long(expected), SqlModeHelper.encode(sqlMode));
    }

    @Test
    public void testCombineMode() throws DdlException {
        String sqlMode = "ANSI";
        long expected = SqlModeHelper.MODE_ANSI | SqlModeHelper.MODE_REAL_AS_FLOAT 
                | SqlModeHelper.MODE_PIPES_AS_CONCAT | SqlModeHelper.MODE_ANSI_QUOTES 
                | SqlModeHelper.MODE_IGNORE_SPACE | SqlModeHelper.MODE_ONLY_FULL_GROUP_BY;
        Assert.assertEquals(new Long(expected), SqlModeHelper.encode(sqlMode));

        sqlMode = "TRADITIONAL";
        expected = SqlModeHelper.MODE_TRADITIONAL | SqlModeHelper.MODE_STRICT_TRANS_TABLES 
                | SqlModeHelper.MODE_STRICT_ALL_TABLES | SqlModeHelper.MODE_NO_ZERO_IN_DATE 
                | SqlModeHelper.MODE_NO_ZERO_DATE | SqlModeHelper.MODE_ERROR_FOR_DIVISION_BY_ZERO 
                | SqlModeHelper.MODE_NO_ENGINE_SUBSTITUTION;
        Assert.assertEquals(new Long(expected), SqlModeHelper.encode(sqlMode));

        long expanded = SqlModeHelper.expand(SqlModeHelper.MODE_ANSI);
        Assert.assertEquals(expected, SqlModeHelper.encode("ANSI"));
    }

    @Test
    public void testMultipleModes() throws DdlException {
        String sqlMode = "PIPES_AS_CONCAT,ANSI_QUOTES,IGNORE_SPACE";
        long expected = SqlModeHelper.MODE_PIPES_AS_CONCAT | SqlModeHelper.MODE_ANSI_QUOTES | SqlModeHelper.MODE_IGNORE_SPACE;
        Assert.assertEquals(new Long(expected), SqlModeHelper.encode(sqlMode));

        String decoded = SqlModeHelper.decode(expected);
        Assert.assertTrue(decoded.contains("PIPES_AS_CONCAT"));
        Assert.assertTrue(decoded.contains("ANSI_QUOTES"));
        Assert.assertTrue(decoded.contains("IGNORE_SPACE"));
    }

    @Test
    public void testNumericValue() throws DdlException {
        String sqlMode = "2"; // PIPES_AS_CONCAT
        Assert.assertEquals(new Long(2L), SqlModeHelper.encode(sqlMode));

        sqlMode = "1,2,4"; // DEFAULT, PIPES_AS_CONCAT, ANSI_QUOTES
        long expected = SqlModeHelper.MODE_DEFAULT | SqlModeHelper.MODE_PIPES_AS_CONCAT | SqlModeHelper.MODE_ANSI_QUOTES;
        Assert.assertEquals(new Long(expected), SqlModeHelper.encode(sqlMode));
    }

    @Test
    public void testSupportedMode() {
        Assert.assertTrue(SqlModeHelper.isSupportedSqlMode("PIPES_AS_CONCAT"));
        Assert.assertTrue(SqlModeHelper.isSupportedSqlMode("ANSI"));
        Assert.assertFalse(SqlModeHelper.isSupportedSqlMode("INVALID_MODE"));
        Assert.assertFalse(SqlModeHelper.isSupportedSqlMode(null));
    }

    @Test
    public void testCombineModeCheck() {
        Assert.assertTrue(SqlModeHelper.isCombineMode("ANSI"));
        Assert.assertTrue(SqlModeHelper.isCombineMode("TRADITIONAL"));
        Assert.assertFalse(SqlModeHelper.isCombineMode("PIPES_AS_CONCAT"));
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

    @Test(expected = DdlException.class)
    public void testInvalidMask() throws DdlException {
        // This should fail because 1L << 40 is beyond allowed mask
        String sqlMode = String.valueOf(1L << 40);
        SqlModeHelper.encode(sqlMode);
        Assert.fail("No exception throws");
    }
}
