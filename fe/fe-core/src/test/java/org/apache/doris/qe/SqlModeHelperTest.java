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

public class SqlModeHelperTest {

    @Test
    public void testEncodeStringValue() throws DdlException {
        Assert.assertEquals(Long.valueOf(2L), SqlModeHelper.encode("PIPES_AS_CONCAT"));
        Assert.assertEquals(Long.valueOf(2L), SqlModeHelper.encode("pipes_as_concat"));
        Assert.assertEquals(Long.valueOf(0L), SqlModeHelper.encode(""));
        Assert.assertEquals(Long.valueOf(3L), SqlModeHelper.encode("0,1, PIPES_AS_CONCAT"));
    }

    @Test
    public void testEncodeCombineMode() throws DdlException {
        long ansiMode = SqlModeHelper.MODE_ANSI
                | SqlModeHelper.MODE_REAL_AS_FLOAT
                | SqlModeHelper.MODE_PIPES_AS_CONCAT
                | SqlModeHelper.MODE_ANSI_QUOTES
                | SqlModeHelper.MODE_IGNORE_SPACE
                | SqlModeHelper.MODE_ONLY_FULL_GROUP_BY;
        Assert.assertEquals(Long.valueOf(ansiMode), SqlModeHelper.encode("ANSI"));
        Assert.assertEquals(Long.valueOf(ansiMode), SqlModeHelper.encode(String.valueOf(SqlModeHelper.MODE_ANSI)));
    }

    @Test
    public void testDecodeValue() throws DdlException {
        Assert.assertEquals("PIPES_AS_CONCAT", SqlModeHelper.decode(2L));
        Assert.assertEquals("", SqlModeHelper.decode(0L));
        Assert.assertEquals("", SqlModeHelper.decode(SqlModeHelper.MODE_DEFAULT));
        Assert.assertEquals("ANSI,ANSI_QUOTES,IGNORE_SPACE,ONLY_FULL_GROUP_BY,PIPES_AS_CONCAT,REAL_AS_FLOAT",
                SqlModeHelper.decode(SqlModeHelper.encode("ANSI")));
    }

    @Test
    public void testInvalidSqlModeName() {
        Assert.assertThrows(DdlException.class,
                () -> SqlModeHelper.encode("PIPES_AS_CONCAT, WRONG_MODE"));
    }

    @Test
    public void testInvalidDecodeMask() {
        Assert.assertThrows(DdlException.class, () -> SqlModeHelper.decode(SqlModeHelper.MODE_LAST));
    }

    @Test
    public void testInvalidEncodeMask() {
        Assert.assertThrows(DdlException.class, () -> SqlModeHelper.encode(String.valueOf(SqlModeHelper.MODE_LAST)));
    }

    @Test
    public void testOverflowNumber() {
        Assert.assertThrows(NumberFormatException.class, () -> SqlModeHelper.encode("18446744073709551616"));
    }
}
