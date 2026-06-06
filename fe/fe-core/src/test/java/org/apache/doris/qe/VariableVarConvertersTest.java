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

public class VariableVarConvertersTest {

    @Test
    public void testSqlMode() throws DdlException {
        // Valid string
        Assert.assertEquals(Long.valueOf(2L), VariableVarConverters.encode(SessionVariable.SQL_MODE, "PIPES_AS_CONCAT"));
        // Valid number
        Assert.assertEquals(Long.valueOf(2L), VariableVarConverters.encode(SessionVariable.SQL_MODE, "2"));
        // Entrance decode
        Assert.assertEquals("PIPES_AS_CONCAT", VariableVarConverters.decode(SessionVariable.SQL_MODE, 2L));
    }

    @Test(expected = DdlException.class)
    public void testSqlModeInvalidString() throws DdlException {
        VariableVarConverters.encode(SessionVariable.SQL_MODE, "INVALID_MODE_XXX");
    }

    @Test(expected = DdlException.class)
    public void testSqlModeHugeNumber() throws DdlException {
        VariableVarConverters.encode(SessionVariable.SQL_MODE, "999999999999999999999999999999");
    }

    @Test(expected = DdlException.class)
    public void testSqlModeNegativeNumber() throws DdlException {
        VariableVarConverters.encode(SessionVariable.SQL_MODE, "-1");
    }

    @Test
    public void testRuntimeFilterType() throws DdlException {
        Assert.assertEquals(Long.valueOf(1L), VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "IN"));
        Assert.assertEquals(Long.valueOf(1L), VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "1"));
        Assert.assertEquals("IN", VariableVarConverters.decode(SessionVariable.RUNTIME_FILTER_TYPE, 1L));
    }

    @Test(expected = DdlException.class)
    public void testRuntimeFilterTypeInvalidString() throws DdlException {
        VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "INVALID_FILTER");
    }

    @Test(expected = DdlException.class)
    public void testRuntimeFilterTypeHugeNumber() throws DdlException {
        VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "999999999999999999999999999999");
    }

    @Test(expected = DdlException.class)
    public void testRuntimeFilterTypeNegativeNumber() throws DdlException {
        VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "-1");
    }

    @Test
    public void testSqlSelectLimit() throws DdlException {
        Assert.assertEquals(Long.valueOf(Long.MAX_VALUE), VariableVarConverters.encode(SessionVariable.SQL_SELECT_LIMIT, "DEFAULT"));
        Assert.assertEquals(Long.valueOf(100L), VariableVarConverters.encode(SessionVariable.SQL_SELECT_LIMIT, "100"));
        Assert.assertEquals(Long.valueOf(-1L), VariableVarConverters.encode(SessionVariable.SQL_SELECT_LIMIT, "-1"));
    }

    @Test(expected = DdlException.class)
    public void testSqlSelectLimitInvalidString() throws DdlException {
        VariableVarConverters.encode(SessionVariable.SQL_SELECT_LIMIT, "NOT_A_NUMBER");
    }

    @Test(expected = DdlException.class)
    public void testSqlSelectLimitHugeNumber() throws DdlException {
        VariableVarConverters.encode(SessionVariable.SQL_SELECT_LIMIT, "999999999999999999999999999999");
    }

    @Test
    public void testValidatePasswordPolicy() throws DdlException {
        Assert.assertEquals(Long.valueOf(0L), VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "NONE"));
        Assert.assertEquals(Long.valueOf(2L), VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "STRONG"));
        Assert.assertEquals(Long.valueOf(0L), VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "0"));
        Assert.assertEquals(Long.valueOf(2L), VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "2"));
        Assert.assertEquals("NONE", VariableVarConverters.decode(GlobalVariable.VALIDATE_PASSWORD_POLICY, 0L));
        Assert.assertEquals("STRONG", VariableVarConverters.decode(GlobalVariable.VALIDATE_PASSWORD_POLICY, 2L));
    }

    @Test(expected = DdlException.class)
    public void testValidatePasswordPolicyInvalidString() throws DdlException {
        VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "INVALID_POLICY");
    }

    @Test(expected = DdlException.class)
    public void testValidatePasswordPolicyInvalidNumber() throws DdlException {
        VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "1");
    }

    @Test(expected = DdlException.class)
    public void testValidatePasswordPolicyHugeNumber() throws DdlException {
        VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "999999999999999999999999999999");
    }
}
