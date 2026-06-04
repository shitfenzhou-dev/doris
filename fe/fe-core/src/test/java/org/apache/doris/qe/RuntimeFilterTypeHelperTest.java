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
    public void testEncodeNumeric() throws DdlException {
        // Valid mask value
        Assert.assertEquals(new Long(2L), RuntimeFilterTypeHelper.encode("2"));
        
        // Mixed numeric and string
        Assert.assertEquals(new Long(6L), RuntimeFilterTypeHelper.encode("2, MIN_MAX"));
        
        // Out of allowed mask numeric value
        try {
            RuntimeFilterTypeHelper.encode("16"); // BITMAP is 16 now wait BITMAP_FILTER is 16? ALLOWED_MASK has BITMAP. Let's use a huge mask.
            // Wait, let's use 1024 which is clearly out of mask
            RuntimeFilterTypeHelper.encode("1024");
            Assert.fail("Expected DdlException");
        } catch (DdlException e) {
            // expected
        }

        // Negative numeric value
        try {
            RuntimeFilterTypeHelper.encode("-1");
            Assert.fail("Expected DdlException");
        } catch (DdlException e) {
            // expected
        }

        // Extremely large number that overflows long
        try {
            RuntimeFilterTypeHelper.encode("99999999999999999999");
            Assert.fail("Expected DdlException");
        } catch (DdlException e) {
            // expected
        }
    }

    @Test
    public void testConverterEntrance() throws DdlException {
        // Normal string combination
        Assert.assertEquals(new Long(5L), VariableVarConverters.encode("runtime_filter_type", "IN,MIN_MAX"));

        // Overflow value through converter
        try {
            VariableVarConverters.encode("runtime_filter_type", "99999999999999999999");
            Assert.fail("Expected DdlException");
        } catch (DdlException e) {
            // expected
        }
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
}
