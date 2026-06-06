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
import org.apache.doris.common.GlobalVariable;

import org.junit.Assert;
import org.junit.Test;

public class VariableVarConvertersTest {

    @Test
    public void testSqlSelectLimitNormal() throws DdlException {
        VariableVarConverters.SqlSelectLimitConverter converter = new VariableVarConverters.SqlSelectLimitConverter();
        Assert.assertEquals(new Long(Long.MAX_VALUE), converter.encode("DEFAULT"));
        Assert.assertEquals(new Long(Long.MAX_VALUE), converter.encode("default"));
        Assert.assertEquals(new Long(100L), converter.encode("100"));
        Assert.assertEquals("100", converter.decode(100L));
    }

    @Test(expected = DdlException.class)
    public void testSqlSelectLimitOverflow() throws DdlException {
        VariableVarConverters.SqlSelectLimitConverter converter = new VariableVarConverters.SqlSelectLimitConverter();
        converter.encode("99999999999999999999");
        Assert.fail("No exception throws for overflow");
    }

    @Test(expected = DdlException.class)
    public void testSqlSelectLimitInvalidString() throws DdlException {
        VariableVarConverters.SqlSelectLimitConverter converter = new VariableVarConverters.SqlSelectLimitConverter();
        converter.encode("abc");
        Assert.fail("No exception throws for invalid string");
    }

    @Test
    public void testValidatePasswordPolicyNormal() throws DdlException {
        VariableVarConverters.ValidatePasswordPolicyConverter converter =
                new VariableVarConverters.ValidatePasswordPolicyConverter();
        Assert.assertEquals(new Long(0L), converter.encode("NONE"));
        Assert.assertEquals(new Long(2L), converter.encode("STRONG"));
        Assert.assertEquals(new Long(0L), converter.encode("0"));
        Assert.assertEquals(new Long(2L), converter.encode("2"));
        Assert.assertEquals("NONE", converter.decode(0L));
        Assert.assertEquals("STRONG", converter.decode(2L));
    }

    @Test(expected = DdlException.class)
    public void testValidatePasswordPolicyOverflow() throws DdlException {
        VariableVarConverters.ValidatePasswordPolicyConverter converter =
                new VariableVarConverters.ValidatePasswordPolicyConverter();
        converter.encode("99999999999999999999");
        Assert.fail("No exception throws for overflow");
    }

    @Test(expected = DdlException.class)
    public void testValidatePasswordPolicyInvalidNumeric() throws DdlException {
        VariableVarConverters.ValidatePasswordPolicyConverter converter =
                new VariableVarConverters.ValidatePasswordPolicyConverter();
        converter.encode("1");
        Assert.fail("No exception throws for invalid numeric value");
    }

    @Test(expected = DdlException.class)
    public void testValidatePasswordPolicyInvalidString() throws DdlException {
        VariableVarConverters.ValidatePasswordPolicyConverter converter =
                new VariableVarConverters.ValidatePasswordPolicyConverter();
        converter.encode("MEDIUM");
        Assert.fail("No exception throws for invalid string value");
    }

    @Test(expected = DdlException.class)
    public void testValidatePasswordPolicyDecodeInvalid() throws DdlException {
        VariableVarConverters.ValidatePasswordPolicyConverter converter =
                new VariableVarConverters.ValidatePasswordPolicyConverter();
        converter.decode(1L);
        Assert.fail("No exception throws for invalid decode value");
    }

    @Test
    public void testConverterViaStaticEncode() throws DdlException {
        Assert.assertEquals(new Long(Long.MAX_VALUE),
                VariableVarConverters.encode(SessionVariable.SQL_SELECT_LIMIT, "DEFAULT"));
        Assert.assertEquals(new Long(0L),
                VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "NONE"));
        Assert.assertEquals(new Long(2L),
                VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "STRONG"));
    }

    @Test(expected = DdlException.class)
    public void testSqlSelectLimitOverflowViaStatic() throws DdlException {
        VariableVarConverters.encode(SessionVariable.SQL_SELECT_LIMIT, "99999999999999999999");
        Assert.fail("No exception throws for overflow");
    }

    @Test(expected = DdlException.class)
    public void testValidatePasswordPolicyOverflowViaStatic() throws DdlException {
        VariableVarConverters.encode(GlobalVariable.VALIDATE_PASSWORD_POLICY, "99999999999999999999");
        Assert.fail("No exception throws for overflow");
    }
}
