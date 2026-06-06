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
    private static final String TOO_LARGE_LONG = "9223372036854775808";

    @Test
    public void testNormal() throws DdlException {
        Assert.assertEquals(Long.valueOf(2L), SqlModeHelper.encode("PIPES_AS_CONCAT"));
        Assert.assertEquals(Long.valueOf(0L), SqlModeHelper.encode(""));
        Assert.assertEquals(Long.valueOf(3L), SqlModeHelper.encode("0,1,PIPES_AS_CONCAT"));
        Assert.assertEquals("PIPES_AS_CONCAT", SqlModeHelper.decode(2L));
        Assert.assertEquals("", SqlModeHelper.decode(0L));
    }

    @Test
    public void testEncodeCombineModeFromStringAndNumber() throws DdlException {
        long expandedAnsiMode = expandedAnsiMode();
        Assert.assertEquals(Long.valueOf(expandedAnsiMode), SqlModeHelper.encode("ANSI"));
        Assert.assertEquals(Long.valueOf(expandedAnsiMode),
                SqlModeHelper.encode(String.valueOf(SqlModeHelper.MODE_ANSI)));
    }

    @Test
    public void testInvalidSqlMode() {
        Assert.assertThrows(DdlException.class, () -> SqlModeHelper.encode("PIPES_AS_CONCAT,WRONG_MODE"));
    }

    @Test
    public void testEncodeTooLargeNumber() {
        Assert.assertThrows(DdlException.class, () -> SqlModeHelper.encode(TOO_LARGE_LONG));
    }

    @Test
    public void testEncodeNegativeNumber() {
        Assert.assertThrows(DdlException.class, () -> SqlModeHelper.encode("-1"));
    }

    @Test
    public void testEncodeInvalidMask() {
        Assert.assertThrows(DdlException.class,
                () -> SqlModeHelper.encode(String.valueOf(SqlModeHelper.MODE_LAST)));
    }

    @Test
    public void testInvalidDecode() {
        Assert.assertThrows(DdlException.class, () -> SqlModeHelper.decode(SqlModeHelper.MODE_LAST));
    }

    @Test
    public void testVariableVarConvertersEntryNormal() throws DdlException {
        long expandedAnsiMode = expandedAnsiMode();
        Assert.assertEquals(Long.valueOf(expandedAnsiMode),
                VariableVarConverters.encode(SessionVariable.SQL_MODE, "ANSI"));
        Assert.assertEquals(Long.valueOf(expandedAnsiMode),
                VariableVarConverters.encode(SessionVariable.SQL_MODE, String.valueOf(SqlModeHelper.MODE_ANSI)));
        Assert.assertEquals(Long.valueOf(Long.MAX_VALUE),
                VariableVarConverters.encode(SessionVariable.SQL_SELECT_LIMIT, "DEFAULT"));
        Assert.assertEquals(Long.valueOf(-1L),
                VariableVarConverters.encode(SessionVariable.SQL_SELECT_LIMIT, "-1"));
        Assert.assertEquals(Long.valueOf(2L),
                VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "STRONG"));
        Assert.assertEquals(Long.valueOf(2L),
                VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "2"));
    }

    @Test
    public void testVariableVarConvertersEntryInvalidValues() {
        Assert.assertThrows(DdlException.class,
                () -> VariableVarConverters.encode(SessionVariable.SQL_MODE, TOO_LARGE_LONG));
        Assert.assertThrows(DdlException.class,
                () -> VariableVarConverters.encode(SessionVariable.SQL_MODE, "WRONG_MODE"));
        Assert.assertThrows(DdlException.class,
                () -> VariableVarConverters.encode(SessionVariable.SQL_SELECT_LIMIT, TOO_LARGE_LONG));
        Assert.assertThrows(DdlException.class,
                () -> VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, TOO_LARGE_LONG));
        Assert.assertThrows(DdlException.class,
                () -> VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "-1"));
        Assert.assertThrows(DdlException.class,
                () -> VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "WEAK"));
    }

    private long expandedAnsiMode() {
        return SqlModeHelper.MODE_ANSI
                | SqlModeHelper.MODE_REAL_AS_FLOAT
                | SqlModeHelper.MODE_PIPES_AS_CONCAT
                | SqlModeHelper.MODE_ANSI_QUOTES
                | SqlModeHelper.MODE_IGNORE_SPACE
                | SqlModeHelper.MODE_ONLY_FULL_GROUP_BY;
    }
}
