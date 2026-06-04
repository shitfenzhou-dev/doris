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
import org.apache.doris.common.ErrorCode;
import org.apache.doris.common.ErrorReport;
import org.apache.doris.thrift.TRuntimeFilterType;

import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Used for encoding and decoding of session variable runtime_filter_type
 */
public class RuntimeFilterTypeHelper {
    private static final Logger LOG = LogManager.getLogger(RuntimeFilterTypeHelper.class);

    public static final long ALLOWED_MASK = (TRuntimeFilterType.IN.getValue()
            | TRuntimeFilterType.BLOOM.getValue()
            | TRuntimeFilterType.MIN_MAX.getValue()
            | TRuntimeFilterType.IN_OR_BLOOM.getValue()
            | TRuntimeFilterType.BITMAP.getValue());

    private static final Map<String, Long> varValueSet = Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER);

    static {
        varValueSet.put("IN", (long) TRuntimeFilterType.IN.getValue());
        varValueSet.put("BLOOM_FILTER", (long) TRuntimeFilterType.BLOOM.getValue());
        varValueSet.put("MIN_MAX", (long) TRuntimeFilterType.MIN_MAX.getValue());
        varValueSet.put("IN_OR_BLOOM_FILTER", (long) TRuntimeFilterType.IN_OR_BLOOM.getValue());
        varValueSet.put("BITMAP_FILTER", (long) TRuntimeFilterType.BITMAP.getValue());
    }

    public static boolean allowedRuntimeFilterType(long runtimeFilterType, TRuntimeFilterType type) {
        return (runtimeFilterType & type.getValue()) != 0;
    }

    // convert long type variable value to string type that user can read
    public static String decode(Long varValue) throws DdlException {
        return VariableVarConverters.decodeBitFieldVar(SessionVariable.RUNTIME_FILTER_TYPE, varValue,
                ALLOWED_MASK, getSupportedVarValue(), 0L);
    }

    // convert string type variable value to long type that session can store
    public static Long encode(String varValue) throws DdlException {
        long resultCode = VariableVarConverters.encodeBitFieldVar(
                SessionVariable.RUNTIME_FILTER_TYPE, varValue, ALLOWED_MASK, getSupportedVarValue(), null);

        int count = 0;
        if (allowedRuntimeFilterType(resultCode, TRuntimeFilterType.IN_OR_BLOOM)) {
            count++;
        }
        if (allowedRuntimeFilterType(resultCode, TRuntimeFilterType.BLOOM)) {
            count++;
        }
        if (allowedRuntimeFilterType(resultCode, TRuntimeFilterType.IN)) {
            count++;
        }
        if (count > 1) {
            ErrorReport.reportDdlException("IN, BLOOM, IN_OR_BLOOM can not be enabled at the same time");
        }
        return resultCode;
    }

    public static Map<String, Long> getSupportedVarValue() {
        return varValueSet;
    }
}
