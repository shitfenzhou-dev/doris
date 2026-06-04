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
        Assert.assertEquals(new Long(1L), RuntimeFilterTypeHelper.encode("In"));
        Assert.assertEquals(new Long(1L), RuntimeFilterTypeHelper.encode("IN"));

        Assert.assertEquals(new Long(2L), RuntimeFilterTypeHelper.encode("bloom_filter"));
        Assert.assertEquals(new Long(2L), RuntimeFilterTypeHelper.encode("Bloom_Filter"));
        Assert.assertEquals(new Long(2L), RuntimeFilterTypeHelper.encode("BLOOM_FILTER"));
    }

    @Test
    public void testNumericValue() throws DdlException {
        Assert.assertEquals(new Long(1L), RuntimeFilterTypeHelper.encode("1"));
        Assert.assertEquals(new Long(5L), RuntimeFilterTypeHelper.encode("1,4"));
        Assert.assertEquals(new Long(6L), RuntimeFilterTypeHelper.encode("2,4"));
    }

    @Test
    public void testBitmapFilter() throws DdlException {
        Assert.assertEquals(new Long(16L), RuntimeFilterTypeHelper.encode("BITMAP_FILTER"));
        Assert.assertEquals(new Long(20L), RuntimeFilterTypeHelper.encode("BITMAP_FILTER,MIN_MAX"));
    }

    @Test
    public void testDecodeWithMultipleBits() throws DdlException {
        long value = 5L;
        Assert.assertEquals("IN,MIN_MAX", RuntimeFilterTypeHelper.decode(value));

        value = 6L;
        Assert.assertEquals("BLOOM_FILTER,MIN_MAX", RuntimeFilterTypeHelper.decode(value));
    }

    @Test(expected = DdlException.class)
    public void testInvalidMask() throws DdlException {
        long invalidMask = RuntimeFilterTypeHelper.ALLOWED_MASK << 1;
        RuntimeFilterTypeHelper.decode(invalidMask);
        Assert.fail("No exception throws");
    }

    @Test
    public void testLargeNumericValue() throws DdlException {
        long validValue = 16L;
        Assert.assertEquals(new Long(validValue), RuntimeFilterTypeHelper.encode(String.valueOf(validValue)));
    }

    @Test(expected = DdlException.class)
    public void testInvalidName() throws DdlException {
        RuntimeFilterTypeHelper.encode("INVALID_TYPE");
        Assert.fail("No exception throws");
    }

    @Test
    public void testIsSupportedVarValue() {
        Assert.assertTrue(RuntimeFilterTypeHelper.isSupportedVarValue("IN"));
        Assert.assertTrue(RuntimeFilterTypeHelper.isSupportedVarValue("in"));
        Assert.assertTrue(RuntimeFilterTypeHelper.isSupportedVarValue("BLOOM_FILTER"));
        Assert.assertFalse(RuntimeFilterTypeHelper.isSupportedVarValue("INVALID"));
        Assert.assertFalse(RuntimeFilterTypeHelper.isSupportedVarValue(null));
    }

    @Test
    public void testAllowedRuntimeFilterType() {
        Assert.assertTrue(RuntimeFilterTypeHelper.allowedRuntimeFilterType(1L,
                org.apache.doris.thrift.TRuntimeFilterType.IN));
        Assert.assertTrue(RuntimeFilterTypeHelper.allowedRuntimeFilterType(2L,
                org.apache.doris.thrift.TRuntimeFilterType.BLOOM));
        Assert.assertTrue(RuntimeFilterTypeHelper.allowedRuntimeFilterType(4L,
                org.apache.doris.thrift.TRuntimeFilterType.MIN_MAX));
        Assert.assertFalse(RuntimeFilterTypeHelper.allowedRuntimeFilterType(1L,
                org.apache.doris.thrift.TRuntimeFilterType.BLOOM));
    }

    @Test
    public void testMutualExclusion() throws DdlException {
        RuntimeFilterTypeHelper.encode("MIN_MAX,BITMAP_FILTER");

        try {
            RuntimeFilterTypeHelper.encode("IN,BLOOM_FILTER");
            Assert.fail("Should throw exception for IN and BLOOM_FILTER together");
        } catch (DdlException e) {
            Assert.assertTrue(e.getMessage().contains("can not be enabled at the same time"));
        }

        try {
            RuntimeFilterTypeHelper.encode("IN,IN_OR_BLOOM_FILTER");
            Assert.fail("Should throw exception for IN and IN_OR_BLOOM_FILTER together");
        } catch (DdlException e) {
            Assert.assertTrue(e.getMessage().contains("can not be enabled at the same time"));
        }

        try {
            RuntimeFilterTypeHelper.encode("BLOOM_FILTER,IN_OR_BLOOM_FILTER");
            Assert.fail("Should throw exception for BLOOM_FILTER and IN_OR_BLOOM_FILTER together");
        } catch (DdlException e) {
            Assert.assertTrue(e.getMessage().contains("can not be enabled at the same time"));
        }
    }
}
