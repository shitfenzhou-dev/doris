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
import org.apache.doris.thrift.TRuntimeFilterType;

import org.junit.Assert;
import org.junit.Test;

public class RuntimeFilterTypeHelperTest {
    private static final String TOO_LARGE_LONG = "9223372036854775808";

    @Test
    public void testNormal() throws DdlException {
        Assert.assertEquals(Long.valueOf(0L), RuntimeFilterTypeHelper.encode(""));
        Assert.assertEquals(Long.valueOf(1L), RuntimeFilterTypeHelper.encode("IN"));
        Assert.assertEquals(Long.valueOf(2L), RuntimeFilterTypeHelper.encode("BLOOM_FILTER"));
        Assert.assertEquals(Long.valueOf(4L), RuntimeFilterTypeHelper.encode("MIN_MAX"));
        Assert.assertEquals(Long.valueOf(5L), RuntimeFilterTypeHelper.encode("IN,MIN_MAX"));
        Assert.assertEquals(Long.valueOf(6L), RuntimeFilterTypeHelper.encode("MIN_MAX,BLOOM_FILTER"));
        Assert.assertEquals(Long.valueOf(8L), RuntimeFilterTypeHelper.encode("IN_OR_BLOOM_FILTER"));
        Assert.assertEquals(Long.valueOf(12L), RuntimeFilterTypeHelper.encode("MIN_MAX,IN_OR_BLOOM_FILTER"));
        Assert.assertEquals(Long.valueOf((long) TRuntimeFilterType.MIN_MAX.getValue()),
                RuntimeFilterTypeHelper.encode(String.valueOf(TRuntimeFilterType.MIN_MAX.getValue())));
        Assert.assertEquals("", RuntimeFilterTypeHelper.decode(0L));
        Assert.assertEquals("IN", RuntimeFilterTypeHelper.decode(1L));
    }

    @Test
    public void testInvalidSqlMode() {
        Assert.assertThrows(DdlException.class, () -> RuntimeFilterTypeHelper.encode("BLOOM,IN"));
    }

    @Test
    public void testEncodeTooLargeNumber() {
        Assert.assertThrows(DdlException.class, () -> RuntimeFilterTypeHelper.encode(TOO_LARGE_LONG));
    }

    @Test
    public void testEncodeNegativeNumber() {
        Assert.assertThrows(DdlException.class, () -> RuntimeFilterTypeHelper.encode("-1"));
    }

    @Test
    public void testEncodeInvalidMask() {
        Assert.assertThrows(DdlException.class, () -> RuntimeFilterTypeHelper.encode("32"));
    }

    @Test
    public void testInvalidDecode() {
        Assert.assertThrows(DdlException.class, () -> RuntimeFilterTypeHelper.decode(32L));
    }

    @Test
    public void testInvalidSqlMode2() {
        Assert.assertThrows(DdlException.class, () -> RuntimeFilterTypeHelper.encode("BLOOM_FILTER,IN"));
    }

    @Test
    public void testInvalidSqlMode3() {
        Assert.assertThrows(DdlException.class,
                () -> RuntimeFilterTypeHelper.encode("BLOOM_FILTER,IN_OR_BLOOM_FILTER"));
    }

    @Test
    public void testInvalidSqlMode4() {
        Assert.assertThrows(DdlException.class, () -> RuntimeFilterTypeHelper.encode("IN,IN_OR_BLOOM_FILTER"));
    }

    @Test
    public void testVariableVarConvertersEntryNormal() throws DdlException {
        Assert.assertEquals(Long.valueOf((long) TRuntimeFilterType.MIN_MAX.getValue()),
                VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE,
                        String.valueOf(TRuntimeFilterType.MIN_MAX.getValue())));
        Assert.assertEquals(Long.valueOf(12L),
                VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "MIN_MAX,IN_OR_BLOOM_FILTER"));
    }

    @Test
    public void testVariableVarConvertersEntryInvalidValues() {
        Assert.assertThrows(DdlException.class,
                () -> VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, TOO_LARGE_LONG));
        Assert.assertThrows(DdlException.class,
                () -> VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "-1"));
        Assert.assertThrows(DdlException.class,
                () -> VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "32"));
        Assert.assertThrows(DdlException.class,
                () -> VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "BLOOM"));
        Assert.assertThrows(DdlException.class,
                () -> VariableVarConverters.encode(SessionVariable.RUNTIME_FILTER_TYPE, "BLOOM_FILTER,IN"));
    }
}
