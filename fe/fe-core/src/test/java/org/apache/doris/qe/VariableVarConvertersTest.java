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
    public void testSqlModeValidString() throws DdlException {
        Assert.assertEquals(new Long(2L),
                VariableVarConverters.encode(SessionVariable.SQL_MODE, "PIPES_AS_CONCAT"));
        Assert.assertEquals(new Long(0L),
                VariableVarConverters.encode(SessionVariable.SQL_MODE, ""));
        Assert.assertEquals(new Long(3L),
                VariableVarConverters.encode(SessionVariable.SQL_MODE, "0,1,PIPES_AS_CONCAT"));
    }

    @Test
    public void testSqlModeValidNumeric() throws DdlException {
        Assert.assertEquals(new Long(2L),
                VariableVarConverters.encode(SessionVariable.SQL_MODE, "2"));
        Assert.assertEquals(new Long(0L),
                VariableVarConverters.encode(SessionVariable.SQL_MODE, "0"));
    }

    @Test
    public void testSqlModeDecode() throws DdlException {
        Assert.assertEquals("PIPES_AS_CONCAT",
                VariableVarConverters.decode(SessionVariable.SQL_MODE, 2L));
        Assert.assertEquals("",
                VariableVarConverters.decode(SessionVariable.SQL_MODE, 0L));
    }

    @Test(expected = DdlException.class)
    public void testSqlModeOverflowNumeric() throws DdlException {
        VariableVarConverters.encode(SessionVariable.SQL_MODE, "99999999999999999999");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testSqlModeNegativeNumeric() throws DdlException {
        VariableVarConverters.encode(SessionVariable.SQL_MODE, "-1");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testSqlModeInvalidMask() throws DdlException {
        VariableVarConverters.encode(SessionVariable.SQL_MODE,
                String.valueOf(SqlModeHelper.MODE_LAST));
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testSqlModeInvalidEnum() throws DdlException {
        VariableVarConverters.encode(SessionVariable.SQL_MODE, "WRONG_MODE");
        Assert.fail("No exception throws");
    }

    @Test
    public void testRuntimeFilterTypeValidString() throws DdlException {
        Assert.assertEquals(new Long(0L),
                VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, ""));
        Assert.assertEquals(new Long(1L),
                VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "IN"));
        Assert.assertEquals(new Long(2L),
                VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "BLOOM_FILTER"));
        Assert.assertEquals(new Long(4L),
                VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "MIN_MAX"));
        Assert.assertEquals(new Long(8L),
                VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "IN_OR_BLOOM_FILTER"));
        Assert.assertEquals(new Long(5L),
                VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "IN,MIN_MAX"));
    }

    @Test
    public void testRuntimeFilterTypeValidNumeric() throws DdlException {
        Assert.assertEquals(new Long(1L),
                VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "1"));
        Assert.assertEquals(new Long(0L),
                VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "0"));
        Assert.assertEquals(new Long(5L),
                VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "1,4"));
    }

    @Test
    public void testRuntimeFilterTypeDecode() throws DdlException {
        Assert.assertEquals("",
                VariableVarConverters.decode(SessionVariable.RUNTIME_FILTER_TYPE, 0L));
        Assert.assertEquals("IN",
                VariableVarConverters.decode(SessionVariable.RUNTIME_FILTER_TYPE, 1L));
    }

    @Test(expected = DdlException.class)
    public void testRuntimeFilterTypeOverflowNumeric() throws DdlException {
        VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "99999999999999999999");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testRuntimeFilterTypeNegativeNumeric() throws DdlException {
        VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "-1");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testRuntimeFilterTypeInvalidMask() throws DdlException {
        VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "32");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testRuntimeFilterTypeInvalidEnum() throws DdlException {
        VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "WRONG_TYPE");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testRuntimeFilterTypeConflict() throws DdlException {
        VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "IN,BLOOM_FILTER");
        Assert.fail("No exception throws");
    }

    @Test
    public void testValidatePasswordPolicyValid() throws DdlException {
        Assert.assertEquals(new Long(0L),
                VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "NONE"));
        Assert.assertEquals(new Long(2L),
                VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "STRONG"));
        Assert.assertEquals(new Long(0L),
                VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "0"));
        Assert.assertEquals(new Long(2L),
                VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "2"));
    }

    @Test
    public void testValidatePasswordPolicyDecode() throws DdlException {
        Assert.assertEquals("NONE",
                VariableVarConverters.decode(GlobalVariable.VALIDATE_PASSWORD_POLICY, 0L));
        Assert.assertEquals("STRONG",
                VariableVarConverters.decode(GlobalVariable.VALIDATE_PASSWORD_POLICY, 2L));
    }

    @Test(expected = DdlException.class)
    public void testValidatePasswordPolicyOverflow() throws DdlException {
        VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "99999999999999999999");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testValidatePasswordPolicyNegative() throws DdlException {
        VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "-1");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testValidatePasswordPolicyInvalidValue() throws DdlException {
        VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "1");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testValidatePasswordPolicyInvalidEnum() throws DdlException {
        VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "INVALID");
        Assert.fail("No exception throws");
    }

    @Test
    public void testSqlSelectLimitValid() throws DdlException {
        Assert.assertEquals(new Long(Long.MAX_VALUE),
                VariableVarConverters.encode(SessionVariable.SQL_SELECT_LIMIT, "DEFAULT"));
        Assert.assertEquals(new Long(Long.MAX_VALUE),
                VariableVarConverters.encode(SessionVariable.SQL_SELECT_LIMIT, "default"));
        Assert.assertEquals(new Long(100L),
                VariableVarConverters.encode(SessionVariable.SQL_SELECT_LIMIT, "100"));
        Assert.assertEquals(new Long(0L),
                VariableVarConverters.encode(SessionVariable.SQL_SELECT_LIMIT, "0"));
    }

    @Test
    public void testSqlSelectLimitDecode() throws DdlException {
        Assert.assertEquals("100",
                VariableVarConverters.decode(SessionVariable.SQL_SELECT_LIMIT, 100L));
    }

    @Test(expected = DdlException.class)
    public void testSqlSelectLimitOverflow() throws DdlException {
        VariableVarConverters.encode(SessionVariable.SQL_SELECT_LIMIT, "99999999999999999999");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testSqlSelectLimitNegative() throws DdlException {
        VariableVarConverters.encode(SessionVariable.SQL_SELECT_LIMIT, "-1");
        Assert.fail("No exception throws");
    }
}