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
    public void testCaseInsensitive() throws DdlException {
        Assert.assertEquals(new Long(2L), SqlModeHelper.encode("pipes_as_concat"));
        Assert.assertEquals(new Long(2L), SqlModeHelper.encode("PIPES_AS_CONCAT"));
        Assert.assertEquals(new Long(2L), SqlModeHelper.encode("Pipes_As_Concat"));
    }

    @Test
    public void testCombineModeAnsi() throws DdlException {
        long result = SqlModeHelper.encode("ANSI");
        Assert.assertTrue((result & SqlModeHelper.MODE_ANSI) != 0);
        Assert.assertTrue((result & SqlModeHelper.MODE_REAL_AS_FLOAT) != 0);
        Assert.assertTrue((result & SqlModeHelper.MODE_PIPES_AS_CONCAT) != 0);
        Assert.assertTrue((result & SqlModeHelper.MODE_ANSI_QUOTES) != 0);
        Assert.assertTrue((result & SqlModeHelper.MODE_IGNORE_SPACE) != 0);
        Assert.assertTrue((result & SqlModeHelper.MODE_ONLY_FULL_GROUP_BY) != 0);
    }

    @Test
    public void testCombineModeTraditional() throws DdlException {
        long result = SqlModeHelper.encode("TRADITIONAL");
        Assert.assertTrue((result & SqlModeHelper.MODE_TRADITIONAL) != 0);
        Assert.assertTrue((result & SqlModeHelper.MODE_STRICT_TRANS_TABLES) != 0);
        Assert.assertTrue((result & SqlModeHelper.MODE_STRICT_ALL_TABLES) != 0);
        Assert.assertTrue((result & SqlModeHelper.MODE_NO_ZERO_IN_DATE) != 0);
        Assert.assertTrue((result & SqlModeHelper.MODE_NO_ZERO_DATE) != 0);
        Assert.assertTrue((result & SqlModeHelper.MODE_ERROR_FOR_DIVISION_BY_ZERO) != 0);
        Assert.assertTrue((result & SqlModeHelper.MODE_NO_ENGINE_SUBSTITUTION) != 0);
    }

    @Test
    public void testCombineModeNumeric() throws DdlException {
        long result = SqlModeHelper.encode(String.valueOf(SqlModeHelper.MODE_ANSI));
        Assert.assertTrue((result & SqlModeHelper.MODE_ANSI) != 0);
        Assert.assertTrue((result & SqlModeHelper.MODE_REAL_AS_FLOAT) != 0);
    }

    @Test
    public void testCombineModeWithOtherModes() throws DdlException {
        long result = SqlModeHelper.encode("ANSI,NO_BACKSLASH_ESCAPES");
        Assert.assertTrue((result & SqlModeHelper.MODE_ANSI) != 0);
        Assert.assertTrue((result & SqlModeHelper.MODE_REAL_AS_FLOAT) != 0);
        Assert.assertTrue((result & SqlModeHelper.MODE_NO_BACKSLASH_ESCAPES) != 0);
    }

    @Test
    public void testMultipleModes() throws DdlException {
        long result = SqlModeHelper.encode("PIPES_AS_CONCAT,ANSI_QUOTES,IGNORE_SPACE");
        Assert.assertEquals(SqlModeHelper.MODE_PIPES_AS_CONCAT | SqlModeHelper.MODE_ANSI_QUOTES
                | SqlModeHelper.MODE_IGNORE_SPACE, result);
    }

    @Test
    public void testDecodeDefault() throws DdlException {
        Assert.assertEquals("", SqlModeHelper.decode(SqlModeHelper.MODE_DEFAULT));
    }

    @Test
    public void testDecodeMultipleModes() throws DdlException {
        long value = SqlModeHelper.MODE_PIPES_AS_CONCAT | SqlModeHelper.MODE_ANSI_QUOTES;
        String decoded = SqlModeHelper.decode(value);
        Assert.assertTrue(decoded.contains("PIPES_AS_CONCAT"));
        Assert.assertTrue(decoded.contains("ANSI_QUOTES"));
    }

    @Test
    public void testDecodeOrder() throws DdlException {
        long value = SqlModeHelper.MODE_NO_BACKSLASH_ESCAPES | SqlModeHelper.MODE_ANSI_QUOTES;
        String decoded = SqlModeHelper.decode(value);
        Assert.assertEquals("ANSI_QUOTES,NO_BACKSLASH_ESCAPES", decoded);
    }

    @Test(expected = DdlException.class)
    public void testInvalidMaskInEncode() throws DdlException {
        SqlModeHelper.encode(String.valueOf(SqlModeHelper.MODE_LAST));
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testVeryLargeNumber() throws DdlException {
        SqlModeHelper.encode("9999999999999999999");
        Assert.fail("No exception throws");
    }

    @Test
    public void testExpand() throws DdlException {
        long expanded = SqlModeHelper.expand(SqlModeHelper.MODE_ANSI);
        Assert.assertTrue((expanded & SqlModeHelper.MODE_ANSI) != 0);
        Assert.assertTrue((expanded & SqlModeHelper.MODE_REAL_AS_FLOAT) != 0);
        Assert.assertTrue((expanded & SqlModeHelper.MODE_PIPES_AS_CONCAT) != 0);
    }

    @Test
    public void testExpandNonCombine() throws DdlException {
        long expanded = SqlModeHelper.expand(SqlModeHelper.MODE_PIPES_AS_CONCAT);
        Assert.assertEquals(SqlModeHelper.MODE_PIPES_AS_CONCAT, expanded);
    }

    @Test
    public void testIsSupportedSqlMode() {
        Assert.assertTrue(SqlModeHelper.isSupportedSqlMode("PIPES_AS_CONCAT"));
        Assert.assertTrue(SqlModeHelper.isSupportedSqlMode("ANSI"));
        Assert.assertTrue(SqlModeHelper.isSupportedSqlMode("TRADITIONAL"));
        Assert.assertFalse(SqlModeHelper.isSupportedSqlMode("NON_EXISTENT"));
        Assert.assertFalse(SqlModeHelper.isSupportedSqlMode(null));
    }

    @Test
    public void testIsCombineMode() {
        Assert.assertTrue(SqlModeHelper.isCombineMode("ANSI"));
        Assert.assertTrue(SqlModeHelper.isCombineMode("TRADITIONAL"));
        Assert.assertFalse(SqlModeHelper.isCombineMode("PIPES_AS_CONCAT"));
    }
}