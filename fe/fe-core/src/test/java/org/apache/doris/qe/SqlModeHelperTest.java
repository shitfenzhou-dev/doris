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
        Assert.assertEquals(new Long(2L), SqlModeHelper.encode("Pipes_As_Concat"));
        Assert.assertEquals(new Long(2L), SqlModeHelper.encode("PIPES_AS_CONCAT"));
    }

    @Test
    public void testNumericValue() throws DdlException {
        Assert.assertEquals(new Long(2L), SqlModeHelper.encode("2"));
        Assert.assertEquals(new Long(6L), SqlModeHelper.encode("2,4"));
        Assert.assertEquals(new Long(10L), SqlModeHelper.encode("2,8"));
    }

    @Test
    public void testCombinedMode() throws DdlException {
        long ansiMode = SqlModeHelper.encode("ANSI");
        Assert.assertTrue((ansiMode & SqlModeHelper.MODE_ANSI) != 0);
        Assert.assertTrue((ansiMode & SqlModeHelper.MODE_REAL_AS_FLOAT) != 0);
        Assert.assertTrue((ansiMode & SqlModeHelper.MODE_PIPES_AS_CONCAT) != 0);
        Assert.assertTrue((ansiMode & SqlModeHelper.MODE_ANSI_QUOTES) != 0);
        Assert.assertTrue((ansiMode & SqlModeHelper.MODE_IGNORE_SPACE) != 0);
        Assert.assertTrue((ansiMode & SqlModeHelper.MODE_ONLY_FULL_GROUP_BY) != 0);

        long traditionalMode = SqlModeHelper.encode("TRADITIONAL");
        Assert.assertTrue((traditionalMode & SqlModeHelper.MODE_TRADITIONAL) != 0);
        Assert.assertTrue((traditionalMode & SqlModeHelper.MODE_STRICT_TRANS_TABLES) != 0);
        Assert.assertTrue((traditionalMode & SqlModeHelper.MODE_STRICT_ALL_TABLES) != 0);
    }

    @Test
    public void testNumericWithCombineModeExpansion() throws DdlException {
        long ansiNumeric = SqlModeHelper.encode(String.valueOf(SqlModeHelper.MODE_ANSI));
        Assert.assertTrue((ansiNumeric & SqlModeHelper.MODE_ANSI) != 0);
        Assert.assertTrue((ansiNumeric & SqlModeHelper.MODE_REAL_AS_FLOAT) != 0);
        Assert.assertTrue((ansiNumeric & SqlModeHelper.MODE_PIPES_AS_CONCAT) != 0);
    }

    @Test
    public void testMultipleModes() throws DdlException {
        String sqlMode = "PIPES_AS_CONCAT,ANSI_QUOTES,IGNORE_SPACE";
        long encoded = SqlModeHelper.encode(sqlMode);
        Assert.assertEquals(new Long(14L), encoded);
        Assert.assertEquals("PIPES_AS_CONCAT,ANSI_QUOTES,IGNORE_SPACE", SqlModeHelper.decode(encoded));
    }

    @Test
    public void testDecodeWithMultipleBits() throws DdlException {
        long value = SqlModeHelper.MODE_PIPES_AS_CONCAT | SqlModeHelper.MODE_ANSI_QUOTES;
        Assert.assertEquals("PIPES_AS_CONCAT,ANSI_QUOTES", SqlModeHelper.decode(value));
    }

    @Test(expected = DdlException.class)
    public void testInvalidMask() throws DdlException {
        long invalidMask = SqlModeHelper.MODE_ALLOWED_MASK << 1;
        SqlModeHelper.decode(invalidMask);
        Assert.fail("No exception throws");
    }

    @Test
    public void testLargeNumericValue() throws DdlException {
        long largeValue = 1L << 34;
        Assert.assertEquals(new Long(largeValue), SqlModeHelper.encode(String.valueOf(largeValue)));
    }

    @Test
    public void testDefaultModeDecode() throws DdlException {
        Assert.assertEquals("", SqlModeHelper.decode(SqlModeHelper.MODE_DEFAULT));
    }

    @Test
    public void testIsSupportedSqlMode() {
        Assert.assertTrue(SqlModeHelper.isSupportedSqlMode("PIPES_AS_CONCAT"));
        Assert.assertTrue(SqlModeHelper.isSupportedSqlMode("pipes_as_concat"));
        Assert.assertFalse(SqlModeHelper.isSupportedSqlMode("INVALID_MODE"));
        Assert.assertFalse(SqlModeHelper.isSupportedSqlMode(null));
    }

    @Test
    public void testIsCombineMode() {
        Assert.assertTrue(SqlModeHelper.isCombineMode("ANSI"));
        Assert.assertTrue(SqlModeHelper.isCombineMode("TRADITIONAL"));
        Assert.assertFalse(SqlModeHelper.isCombineMode("PIPES_AS_CONCAT"));
    }
}
