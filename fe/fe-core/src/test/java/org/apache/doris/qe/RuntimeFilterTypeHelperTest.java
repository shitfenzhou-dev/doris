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

    @Test
    public void testCaseInsensitive() throws DdlException {
        String runtimeFilterType = "in";
        Assert.assertEquals(new Long(1L), RuntimeFilterTypeHelper.encode(runtimeFilterType));

        runtimeFilterType = "bloom_filter";
        Assert.assertEquals(new Long(2L), RuntimeFilterTypeHelper.encode(runtimeFilterType));

        runtimeFilterType = "min_max";
        Assert.assertEquals(new Long(4L), RuntimeFilterTypeHelper.encode(runtimeFilterType));
    }

    @Test
    public void testNumericValue() throws DdlException {
        String runtimeFilterType = "1"; // IN
        Assert.assertEquals(new Long(1L), RuntimeFilterTypeHelper.encode(runtimeFilterType));

        runtimeFilterType = "2"; // BLOOM_FILTER
        Assert.assertEquals(new Long(2L), RuntimeFilterTypeHelper.encode(runtimeFilterType));

        runtimeFilterType = "1,4"; // IN, MIN_MAX
        Assert.assertEquals(new Long(5L), RuntimeFilterTypeHelper.encode(runtimeFilterType));
    }

    @Test
    public void testSupportedType() {
        Assert.assertTrue(RuntimeFilterTypeHelper.isSupportedVarValue("IN"));
        Assert.assertTrue(RuntimeFilterTypeHelper.isSupportedVarValue("BLOOM_FILTER"));
        Assert.assertTrue(RuntimeFilterTypeHelper.isSupportedVarValue("MIN_MAX"));
        Assert.assertTrue(RuntimeFilterTypeHelper.isSupportedVarValue("IN_OR_BLOOM_FILTER"));
        Assert.assertTrue(RuntimeFilterTypeHelper.isSupportedVarValue("BITMAP_FILTER"));
        Assert.assertFalse(RuntimeFilterTypeHelper.isSupportedVarValue("INVALID_TYPE"));
        Assert.assertFalse(RuntimeFilterTypeHelper.isSupportedVarValue(null));
    }

    @Test
    public void testAllowedRuntimeFilterType() {
        long value = 1L; // IN
        Assert.assertTrue(RuntimeFilterTypeHelper.allowedRuntimeFilterType(value, org.apache.doris.thrift.TRuntimeFilterType.IN));
        Assert.assertFalse(RuntimeFilterTypeHelper.allowedRuntimeFilterType(value, org.apache.doris.thrift.TRuntimeFilterType.BLOOM));

        value = 12L; // MIN_MAX (4) + IN_OR_BLOOM (8)
        Assert.assertTrue(RuntimeFilterTypeHelper.allowedRuntimeFilterType(value, org.apache.doris.thrift.TRuntimeFilterType.MIN_MAX));
        Assert.assertTrue(RuntimeFilterTypeHelper.allowedRuntimeFilterType(value, org.apache.doris.thrift.TRuntimeFilterType.IN_OR_BLOOM));
    }

    @Test
    public void testDecodeMultiple() throws DdlException {
        long value = 6L; // MIN_MAX (4) + BLOOM_FILTER (2)
        String decoded = RuntimeFilterTypeHelper.decode(value);
        Assert.assertTrue(decoded.contains("MIN_MAX"));
        Assert.assertTrue(decoded.contains("BLOOM_FILTER"));

        value = 12L; // MIN_MAX (4) + IN_OR_BLOOM_FILTER (8)
        decoded = RuntimeFilterTypeHelper.decode(value);
        Assert.assertTrue(decoded.contains("MIN_MAX"));
        Assert.assertTrue(decoded.contains("IN_OR_BLOOM_FILTER"));
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

    @Test(expected = DdlException.class)
    public void testInvalidTypeName() throws DdlException {
        RuntimeFilterTypeHelper.encode("INVALID_TYPE");
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testInvalidLargeMask() throws DdlException {
        // This should fail because it's beyond allowed mask
        RuntimeFilterTypeHelper.decode(1L << 10);
        Assert.fail("No exception throws");
    }

    @Test(expected = DdlException.class)
    public void testInvalidLargeNumericValue() throws DdlException {
        // This should fail because it's beyond allowed mask
        RuntimeFilterTypeHelper.encode(String.valueOf(1L << 10));
        Assert.fail("No exception throws");
    }
}
