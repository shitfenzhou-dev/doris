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
        Assert.assertEquals(Long.valueOf(2L), SqlModeHelper.encode(sqlMode));

        sqlMode = "";
        Assert.assertEquals(Long.valueOf(0L), SqlModeHelper.encode(sqlMode));

        sqlMode = "0,1, PIPES_AS_CONCAT";
        Assert.assertEquals(Long.valueOf(3L), SqlModeHelper.encode(sqlMode));

        long sqlModeValue = 2L;
        Assert.assertEquals("PIPES_AS_CONCAT", SqlModeHelper.decode(sqlModeValue));

        sqlModeValue = 0L;
        Assert.assertEquals("", SqlModeHelper.decode(sqlModeValue));
    }

    @Test
    public void testHelperAndConverterKeepLegalBehavior() throws DdlException {
        Long ansiMode = Long.valueOf(SqlModeHelper.MODE_ANSI
                | SqlModeHelper.MODE_REAL_AS_FLOAT
                | SqlModeHelper.MODE_PIPES_AS_CONCAT
                | SqlModeHelper.MODE_ANSI_QUOTES
                | SqlModeHelper.MODE_IGNORE_SPACE
                | SqlModeHelper.MODE_ONLY_FULL_GROUP_BY);
        Assert.assertEquals(ansiMode, SqlModeHelper.encode("ANSI"));
        Assert.assertEquals(ansiMode, SqlModeHelper.encode(String.valueOf(SqlModeHelper.MODE_ANSI)));
        Assert.assertEquals(ansiMode, VariableVarConverters.encode(SessionVariable.SQL_MODE, "ANSI"));
        Assert.assertEquals(ansiMode,
                VariableVarConverters.encode(SessionVariable.SQL_MODE, String.valueOf(SqlModeHelper.MODE_ANSI)));

        Long mixedMode = Long.valueOf(SqlModeHelper.MODE_PIPES_AS_CONCAT | SqlModeHelper.MODE_ANSI_QUOTES);
        Assert.assertEquals(mixedMode, SqlModeHelper.encode("PIPES_AS_CONCAT,ANSI_QUOTES"));
        Assert.assertEquals(mixedMode,
                VariableVarConverters.encode(SessionVariable.SQL_MODE, "PIPES_AS_CONCAT,ANSI_QUOTES"));
    }

    @Test
    public void testInvalidSqlMode() {
        assertSqlModeEncodeFails("PIPES_AS_CONCAT, WRONG_MODE");
    }

    @Test
    public void testInvalidNumericSqlMode() {
        assertSqlModeEncodeFails("9223372036854775808");
        assertSqlModeEncodeFails("-1");
        assertSqlModeEncodeFails(String.valueOf(SqlModeHelper.MODE_LAST));
    }

    @Test
    public void testInvalidDecode() {
        assertSqlModeDecodeFails(SqlModeHelper.MODE_LAST);
    }

    @Test
    public void testOtherVariableConverterSemantics() throws DdlException {
        Assert.assertEquals(Long.valueOf(Long.MAX_VALUE),
                VariableVarConverters.encode(SessionVariable.SQL_SELECT_LIMIT, "DEFAULT"));
        Assert.assertEquals(Long.valueOf(-1L),
                VariableVarConverters.encode(SessionVariable.SQL_SELECT_LIMIT, "-1"));
        Assert.assertEquals(Long.valueOf(GlobalVariable.VALIDATE_PASSWORD_POLICY_DISABLED),
                VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "NONE"));
        Assert.assertEquals(Long.valueOf(GlobalVariable.VALIDATE_PASSWORD_POLICY_STRONG),
                VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "2"));
        assertVariableEncodeFails(GlobalVariable.VALIDATE_PASSWORD_POLICY, "9223372036854775808");
        assertVariableEncodeFails(GlobalVariable.VALIDATE_PASSWORD_POLICY, "-1");
        assertVariableEncodeFails(SessionVariable.SQL_SELECT_LIMIT, "9223372036854775808");
    }

    private void assertSqlModeEncodeFails(String value) {
        try {
            SqlModeHelper.encode(value);
            Assert.fail("No exception throws");
        } catch (DdlException e) {
        }
        assertVariableEncodeFails(SessionVariable.SQL_MODE, value);
    }

    private void assertSqlModeDecodeFails(long value) {
        try {
            SqlModeHelper.decode(value);
            Assert.fail("No exception throws");
        } catch (DdlException e) {
        }
    }

    private void assertVariableEncodeFails(String variableName, String value) {
        try {
            VariableVarConverters.encode(variableName, value);
            Assert.fail("No exception throws");
        } catch (DdlException e) {
        }
    }
}
