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
    public void testEncodeStringValue() throws DdlException {
        Assert.assertEquals(Long.valueOf(0L), RuntimeFilterTypeHelper.encode(""));
        Assert.assertEquals(Long.valueOf(1L), RuntimeFilterTypeHelper.encode("IN"));
        Assert.assertEquals(Long.valueOf(1L), RuntimeFilterTypeHelper.encode("in"));
        Assert.assertEquals(Long.valueOf(2L), RuntimeFilterTypeHelper.encode("BLOOM_FILTER"));
        Assert.assertEquals(Long.valueOf(4L), RuntimeFilterTypeHelper.encode("MIN_MAX"));
        Assert.assertEquals(Long.valueOf(5L), RuntimeFilterTypeHelper.encode("IN,MIN_MAX"));
        Assert.assertEquals(Long.valueOf(6L), RuntimeFilterTypeHelper.encode("MIN_MAX, BLOOM_FILTER"));
        Assert.assertEquals(Long.valueOf(8L), RuntimeFilterTypeHelper.encode("IN_OR_BLOOM_FILTER"));
        Assert.assertEquals(Long.valueOf(12L), RuntimeFilterTypeHelper.encode("MIN_MAX,IN_OR_BLOOM_FILTER"));
    }

    @Test
    public void testEncodeNumericValue() throws DdlException {
        Assert.assertEquals(Long.valueOf(5L), RuntimeFilterTypeHelper.encode("1,4"));
        Assert.assertEquals(Long.valueOf(12L), RuntimeFilterTypeHelper.encode("8,4"));
    }

    @Test
    public void testDecodeValue() throws DdlException {
        Assert.assertEquals("", RuntimeFilterTypeHelper.decode(0L));
        Assert.assertEquals("IN", RuntimeFilterTypeHelper.decode(1L));
        Assert.assertEquals("BLOOM_FILTER,MIN_MAX", RuntimeFilterTypeHelper.decode(6L));
        Assert.assertEquals("IN_OR_BLOOM_FILTER,MIN_MAX", RuntimeFilterTypeHelper.decode(12L));
    }

    @Test
    public void testInvalidName() {
        Assert.assertThrows(DdlException.class, () -> RuntimeFilterTypeHelper.encode("BLOOM,IN"));
    }

    @Test
    public void testInvalidDecodeMask() {
        Assert.assertThrows(DdlException.class, () -> RuntimeFilterTypeHelper.decode(32L));
    }

    @Test
    public void testInvalidEncodeMask() {
        Assert.assertThrows(DdlException.class, () -> RuntimeFilterTypeHelper.encode("32"));
    }

    @Test
    public void testMutuallyExclusiveFilterTypes() {
        Assert.assertThrows(DdlException.class, () -> RuntimeFilterTypeHelper.encode("BLOOM_FILTER,IN"));
        Assert.assertThrows(DdlException.class,
                () -> RuntimeFilterTypeHelper.encode("BLOOM_FILTER,IN_OR_BLOOM_FILTER"));
        Assert.assertThrows(DdlException.class, () -> RuntimeFilterTypeHelper.encode("IN,IN_OR_BLOOM_FILTER"));
    }

    @Test
    public void testOverflowNumber() {
        Assert.assertThrows(NumberFormatException.class,
                () -> RuntimeFilterTypeHelper.encode("18446744073709551616"));
    }
}
