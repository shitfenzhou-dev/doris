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
    public void testSqlModeConverterValidString() throws DdlException {
        Assert.assertEquals(new Long(2L),
                VariableVarConverters.encode(SessionVariable.SQL_MODE, "PIPES_AS_CONCAT"));
        Assert.assertEquals(new Long(0L),
                VariableVarConverters.encode(SessionVariable.SQL_MODE, ""));
        Assert.assertEquals(new Long(6L),
                VariableVarConverters.encode(SessionVariable.SQL_MODE, "2,4"));
    }

    @Test(expected = DdlException.class)
    public void testSqlModeConverterOverflow() throws DdlException {
        VariableVarConverters.encode(SessionVariable.SQL_MODE, "99999999999999999999");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testSqlModeConverterNegative() throws DdlException {
        VariableVarConverters.encode(SessionVariable.SQL_MODE, "-1");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testSqlModeConverterInvalidMask() throws DdlException {
        VariableVarConverters.encode(SessionVariable.SQL_MODE, "131072");
        Assert.fail("No exception throws");
    }

    @Test
    public void testSqlModeConverterDecode() throws DdlException {
        Assert.assertEquals("PIPES_AS_CONCAT",
                VariableVarConverters.decode(SessionVariable.SQL_MODE, 2L));
        Assert.assertEquals("",
                VariableVarConverters.decode(SessionVariable.SQL_MODE, 0L));
    }

    @Test
    public void testRuntimeFilterTypeConverterValidString() throws DdlException {
        Assert.assertEquals(new Long(1L),
                VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "IN"));
        Assert.assertEquals(new Long(6L),
                VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "2,4"));
    }

    @Test(expected = DdlException.class)
    public void testRuntimeFilterTypeConverterOverflow() throws DdlException {
        VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "99999999999999999999");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testRuntimeFilterTypeConverterNegative() throws DdlException {
        VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "-1");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testRuntimeFilterTypeConverterInvalidMask() throws DdlException {
        VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "32");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testRuntimeFilterTypeConverterMutualExclusive() throws DdlException {
        VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "BLOOM_FILTER,IN");
        Assert.fail("No exception throws");
    }

    @Test
    public void testRuntimeFilterTypeConverterDecode() throws DdlException {
        Assert.assertEquals("IN",
                VariableVarConverters.decode(SessionVariable.RUNTIME_FILTER_TYPE, 1L));
        Assert.assertEquals("",
                VariableVarConverters.decode(SessionVariable.RUNTIME_FILTER_TYPE, 0L));
    }

    @Test
    public void testSqlSelectLimitConverter() throws DdlException {
        Assert.assertEquals(new Long(100L),
                VariableVarConverters.encode(SessionVariable.SQL_SELECT_LIMIT, "100"));
        Assert.assertEquals(new Long(Long.MAX_VALUE),
                VariableVarConverters.encode(SessionVariable.SQL_SELECT_LIMIT, "DEFAULT"));
    }

    @Test(expected = DdlException.class)
    public void testSqlSelectLimitConverterOverflow() throws DdlException {
        VariableVarConverters.encode(SessionVariable.SQL_SELECT_LIMIT, "99999999999999999999");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testSqlSelectLimitConverterNegative() throws DdlException {
        VariableVarConverters.encode(SessionVariable.SQL_SELECT_LIMIT, "-1");
        Assert.fail("No exception throws");
    }

    @Test
    public void testValidatePasswordPolicyConverter() throws DdlException {
        Assert.assertEquals(new Long(0L),
                VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "NONE"));
        Assert.assertEquals(new Long(0L),
                VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "none"));
        Assert.assertEquals(new Long(2L),
                VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "STRONG"));
        Assert.assertEquals(new Long(0L),
                VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "0"));
        Assert.assertEquals(new Long(2L),
                VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "2"));
    }

    @Test(expected = DdlException.class)
    public void testValidatePasswordPolicyConverterOverflow() throws DdlException {
        VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "99999999999999999999");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testValidatePasswordPolicyConverterNegative() throws DdlException {
        VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "-1");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testValidatePasswordPolicyConverterInvalidNumber() throws DdlException {
        VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "1");
        Assert.fail("No exception throws");
    }

    @Test
    public void testValidatePasswordPolicyConverterDecode() throws DdlException {
        Assert.assertEquals("NONE",
                VariableVarConverters.decode(GlobalVariable.VALIDATE_PASSWORD_POLICY, 0L));
        Assert.assertEquals("STRONG",
                VariableVarConverters.decode(GlobalVariable.VALIDATE_PASSWORD_POLICY, 2L));
    }

    @Test
    public void testUnknownVariable() throws DdlException {
        Assert.assertEquals(new Long(0L),
                VariableVarConverters.encode("unknown_var", "anything"));
        Assert.assertEquals("",
                VariableVarConverters.decode("unknown_var", 123L));
    }
}