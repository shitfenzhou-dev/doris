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
        Assert.assertEquals(Long.valueOf(0L), RuntimeFilterTypeHelper.encode(runtimeFilterType));

        runtimeFilterType = "IN";
        Assert.assertEquals(Long.valueOf(1L), RuntimeFilterTypeHelper.encode(runtimeFilterType));

        runtimeFilterType = "BLOOM_FILTER";
        Assert.assertEquals(Long.valueOf(2L), RuntimeFilterTypeHelper.encode(runtimeFilterType));

        runtimeFilterType = "MIN_MAX";
        Assert.assertEquals(Long.valueOf(4L), RuntimeFilterTypeHelper.encode(runtimeFilterType));

        runtimeFilterType = "IN,MIN_MAX";
        Assert.assertEquals(Long.valueOf(5L), RuntimeFilterTypeHelper.encode(runtimeFilterType));

        runtimeFilterType = "MIN_MAX, BLOOM_FILTER";
        Assert.assertEquals(Long.valueOf(6L), RuntimeFilterTypeHelper.encode(runtimeFilterType));

        runtimeFilterType = "IN_OR_BLOOM_FILTER";
        Assert.assertEquals(Long.valueOf(8L), RuntimeFilterTypeHelper.encode(runtimeFilterType));

        runtimeFilterType = "MIN_MAX,IN_OR_BLOOM_FILTER";
        Assert.assertEquals(Long.valueOf(12L), RuntimeFilterTypeHelper.encode(runtimeFilterType));

        long runtimeFilterTypeValue = 0L;
        Assert.assertEquals("", RuntimeFilterTypeHelper.decode(runtimeFilterTypeValue));

        runtimeFilterTypeValue = 1L;
        Assert.assertEquals("IN", RuntimeFilterTypeHelper.decode(runtimeFilterTypeValue));
    }

    @Test
    public void testHelperAndConverterKeepLegalBehavior() throws DdlException {
        Long minMaxCode = Long.valueOf(4L);
        Assert.assertEquals(minMaxCode, RuntimeFilterTypeHelper.encode("MIN_MAX"));
        Assert.assertEquals(minMaxCode, RuntimeFilterTypeHelper.encode("4"));
        Assert.assertEquals(minMaxCode,
                VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "MIN_MAX"));
        Assert.assertEquals(minMaxCode,
                VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "4"));

        Long mixedCode = Long.valueOf(20L);
        Assert.assertEquals(mixedCode, RuntimeFilterTypeHelper.encode("MIN_MAX,BITMAP_FILTER"));
        Assert.assertEquals(mixedCode,
                VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "MIN_MAX,BITMAP_FILTER"));
    }

    @Test
    public void testInvalidRuntimeFilterType() {
        assertRuntimeFilterTypeEncodeFails("BLOOM,IN");
        assertRuntimeFilterTypeEncodeFails("BLOOM_FILTER,IN");
        assertRuntimeFilterTypeEncodeFails("BLOOM_FILTER,IN_OR_BLOOM_FILTER");
        assertRuntimeFilterTypeEncodeFails("IN,IN_OR_BLOOM_FILTER");
    }

    @Test
    public void testInvalidNumericRuntimeFilterType() {
        assertRuntimeFilterTypeEncodeFails("9223372036854775808");
        assertRuntimeFilterTypeEncodeFails("-1");
        assertRuntimeFilterTypeEncodeFails("32");
    }

    @Test
    public void testInvalidDecode() {
        assertRuntimeFilterTypeDecodeFails(32L);
    }

    private void assertRuntimeFilterTypeEncodeFails(String value) {
        try {
            RuntimeFilterTypeHelper.encode(value);
            Assert.fail("No exception throws");
        } catch (DdlException e) {
        }
        assertVariableEncodeFails(SessionVariable.RUNTIME_FILTER_TYPE, value);
    }

    private void assertRuntimeFilterTypeDecodeFails(long value) {
        try {
            RuntimeFilterTypeHelper.decode(value);
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
