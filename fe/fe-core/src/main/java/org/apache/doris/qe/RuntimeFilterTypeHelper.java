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
import org.apache.doris.common.ErrorReport;
import org.apache.doris.thrift.TRuntimeFilterType;

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

    private static final SessionVarFlagHelper ENCODER = new SessionVarFlagHelper(
            SessionVariable.RUNTIME_FILTER_TYPE, ALLOWED_MASK) {
        {
            registerFlag("IN", (long) TRuntimeFilterType.IN.getValue());
            registerFlag("BLOOM_FILTER", (long) TRuntimeFilterType.BLOOM.getValue());
            registerFlag("MIN_MAX", (long) TRuntimeFilterType.MIN_MAX.getValue());
            registerFlag("IN_OR_BLOOM_FILTER", (long) TRuntimeFilterType.IN_OR_BLOOM.getValue());
            registerFlag("BITMAP_FILTER", (long) TRuntimeFilterType.BITMAP.getValue());
        }

        @Override
        protected void validateAfterEncode(long resultCode) throws DdlException {
            int count = 0;
            if ((resultCode & TRuntimeFilterType.IN_OR_BLOOM.getValue()) != 0) {
                count++;
            }
            if ((resultCode & TRuntimeFilterType.BLOOM.getValue()) != 0) {
                count++;
            }
            if ((resultCode & TRuntimeFilterType.IN.getValue()) != 0) {
                count++;
            }
            if (count > 1) {
                ErrorReport.reportDdlException("IN, BLOOM, IN_OR_BLOOM can not be enabled at the same time");
            }
        }
    };

    private static final Map<String, Long> varValueSet = ENCODER.getFlagNameMap();

    public static boolean allowedRuntimeFilterType(long runtimeFilterType, TRuntimeFilterType type) {
        return (runtimeFilterType & type.getValue()) != 0;
    }

    // convert long type variable value to string type that user can read
    public static String decode(Long varValue) throws DdlException {
        return ENCODER.decode(varValue);
    }

    // convert string type variable value to long type that session can store
    public static Long encode(String varValue) throws DdlException {
        return ENCODER.encode(varValue);
    }

    // check if this variable value is supported
    public static boolean isSupportedVarValue(String varValue) {
        return ENCODER.isSupported(varValue);
    }

    public static Map<String, Long> getSupportedVarValue() {
        return varValueSet;
    }
}