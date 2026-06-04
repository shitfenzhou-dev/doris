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

public class RuntimeFilterTypeHelperTest {

    @Test
    public void testNormal() throws DdlException {
        String runtimeFilterType = "";
        Assert.assertEquals(new Long(0L), RuntimeFilterTypeHelper.encode(runtimeFilterType));

        runtimeFilterType = "IN";
        Assert.assertEquals(new Long(1L), RuntimeFilterTypeHelper.encode(runtimeFilterType));

        runtimeFilterType = "BLOOM_FILTER";
        Assert.assertEquals(new Long(2L), RuntimeFilterTypeHelper.encode(runtimeFilterType));

        runtimeFilterType = "MIN_MAX";
        Assert.assertEquals(new Long(4L), RuntimeFilterTypeHelper.encode(runtimeFilterType));

        runtimeFilterType = "IN,MIN_MAX";
        Assert.assertEquals(new Long(5L), RuntimeFilterTypeHelper.encode(runtimeFilterType));

        runtimeFilterType = "MIN_MAX, BLOOM_FILTER";
        Assert.assertEquals(new Long(6L), RuntimeFilterTypeHelper.encode(runtimeFilterType));

        runtimeFilterType = "IN_OR_BLOOM_FILTER";
        Assert.assertEquals(new Long(8L), RuntimeFilterTypeHelper.encode(runtimeFilterType));

        runtimeFilterType = "MIN_MAX,IN_OR_BLOOM_FILTER";
        Assert.assertEquals(new Long(12L), RuntimeFilterTypeHelper.encode(runtimeFilterType));

        long runtimeFilterTypeValue = 0L;
        Assert.assertEquals("", RuntimeFilterTypeHelper.decode(runtimeFilterTypeValue));

        runtimeFilterTypeValue = 1L;
        Assert.assertEquals("IN", RuntimeFilterTypeHelper.decode(runtimeFilterTypeValue));
    }

    @Test(expected = DdlException.class)
    public void testInvalidSqlMode() throws DdlException {
        RuntimeFilterTypeHelper.encode("BLOOM,IN");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testInvalidDecode() throws DdlException {
        RuntimeFilterTypeHelper.decode(32L);
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testInvalidSqlMode2() throws DdlException {
        RuntimeFilterTypeHelper.encode("BLOOM_FILTER,IN");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testInvalidSqlMode3() throws DdlException {
        RuntimeFilterTypeHelper.encode("BLOOM_FILTER,IN_OR_BLOOM_FILTER");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testInvalidSqlMode4() throws DdlException {
        RuntimeFilterTypeHelper.encode("IN,IN_OR_BLOOM_FILTER");
        Assert.fail("No exception throws");
    }

    @Test
    public void testCaseInsensitive() throws DdlException {
        Assert.assertEquals(new Long(1L), RuntimeFilterTypeHelper.encode("in"));
        Assert.assertEquals(new Long(1L), RuntimeFilterTypeHelper.encode("IN"));
        Assert.assertEquals(new Long(1L), RuntimeFilterTypeHelper.encode("In"));
        Assert.assertEquals(new Long(2L), RuntimeFilterTypeHelper.encode("bloom_filter"));
        Assert.assertEquals(new Long(2L), RuntimeFilterTypeHelper.encode("BLOOM_FILTER"));
    }

    @Test
    public void testNumericValue() throws DdlException {
        Assert.assertEquals(new Long(1L), RuntimeFilterTypeHelper.encode("1"));
        Assert.assertEquals(new Long(5L), RuntimeFilterTypeHelper.encode("5"));
        Assert.assertEquals(new Long(7L), RuntimeFilterTypeHelper.encode("1,6"));
    }

    @Test
    public void testDecodeCombined() throws DdlException {
        Assert.assertEquals("IN,MIN_MAX", RuntimeFilterTypeHelper.decode(5L));
        Assert.assertEquals("MIN_MAX", RuntimeFilterTypeHelper.decode(4L));
        Assert.assertEquals("BLOOM_FILTER,BITMAP_FILTER", RuntimeFilterTypeHelper.decode(18L));
    }

    @Test
    public void testDecodeOrder() throws DdlException {
        Assert.assertEquals("BLOOM_FILTER,MIN_MAX", RuntimeFilterTypeHelper.decode(6L));
        Assert.assertEquals("BITMAP_FILTER,IN", RuntimeFilterTypeHelper.decode(17L));
    }

    @Test(expected = DdlException.class)
    public void testInvalidName() throws DdlException {
        RuntimeFilterTypeHelper.encode("NON_EXISTENT");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testInvalidNumericMask() throws DdlException {
        RuntimeFilterTypeHelper.encode("16");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testVeryLargeNumber() throws DdlException {
        RuntimeFilterTypeHelper.encode("9999999999999999999");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testThreeMutualExclusive() throws DdlException {
        RuntimeFilterTypeHelper.encode("IN,BLOOM_FILTER,IN_OR_BLOOM_FILTER");
        Assert.fail("No exception throws");
    }

    @Test
    public void testIsSupportedVarValue() {
        Assert.assertTrue(RuntimeFilterTypeHelper.isSupportedVarValue("IN"));
        Assert.assertTrue(RuntimeFilterTypeHelper.isSupportedVarValue("BLOOM_FILTER"));
        Assert.assertTrue(RuntimeFilterTypeHelper.isSupportedVarValue("MIN_MAX"));
        Assert.assertTrue(RuntimeFilterTypeHelper.isSupportedVarValue("IN_OR_BLOOM_FILTER"));
        Assert.assertTrue(RuntimeFilterTypeHelper.isSupportedVarValue("BITMAP_FILTER"));
        Assert.assertFalse(RuntimeFilterTypeHelper.isSupportedVarValue("NON_EXISTENT"));
        Assert.assertFalse(RuntimeFilterTypeHelper.isSupportedVarValue(null));
    }
}