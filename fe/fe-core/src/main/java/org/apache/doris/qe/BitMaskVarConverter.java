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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for encoding/decoding bitmask-based session variables.
 * Provides shared logic for comma-separated string parsing, case-insensitive name mapping,
 * numeric value parsing, allowed mask validation, and decode output.
 */
public class BitMaskVarConverter {

    private BitMaskVarConverter() {
    }

    /**
     * Encode a comma-separated string into a long bitmask.
     * Each token is converted to a long value via the provided tokenToValue function.
     *
     * @param input        the comma-separated string (e.g. "MODE_A,MODE_B")
     * @param allowedMask  mask of all allowed bits; result must not exceed this
     * @param varName      variable name for error reporting
     * @param tokenToValue converts a single token to its long value;
     *                     should throw DdlException for invalid tokens
     * @return the encoded long value
     */
    public static long encode(String input, long allowedMask, String varName,
            TokenToValue tokenToValue) throws DdlException {
        List<String> names = Splitter.on(',').trimResults().omitEmptyStrings().splitToList(input);

        long resultCode = 0L;
        for (String key : names) {
            long code = tokenToValue.convert(key);
            resultCode |= code;
            if ((resultCode & ~allowedMask) != 0) {
                ErrorReport.reportDdlException(ErrorCode.ERR_WRONG_VALUE_FOR_VAR, varName, key);
            }
        }
        return resultCode;
    }

    /**
     * Encode with a default lookup map for non-numeric tokens.
     * Numeric tokens are parsed directly; non-numeric tokens are looked up in the map.
     *
     * @param input        the comma-separated string
     * @param nameToValue  case-insensitive map from name to bitmask value
     * @param allowedMask  mask of all allowed bits
     * @param varName      variable name for error reporting
     * @return the encoded long value
     */
    public static long encode(String input, Map<String, Long> nameToValue, long allowedMask, String varName)
            throws DdlException {
        return encode(input, allowedMask, varName, token -> {
            if (isNumeric(token)) {
                return Long.parseLong(token);
            }
            Long mapped = nameToValue.get(token);
            if (mapped == null) {
                ErrorReport.reportDdlException(ErrorCode.ERR_WRONG_VALUE_FOR_VAR, varName, token);
            }
            return mapped;
        });
    }

    /**
     * Decode a long bitmask into a comma-separated string of names.
     *
     * @param value        the bitmask value
     * @param nameToValue  case-insensitive map from name to bitmask value (iteration order preserved)
     * @param allowedMask  mask of all allowed bits; value must not exceed this
     * @param varName      variable name for error reporting
     * @return the decoded comma-separated string
     */
    public static String decode(long value, Map<String, Long> nameToValue, long allowedMask, String varName)
            throws DdlException {
        if ((value & ~allowedMask) != 0) {
            ErrorReport.reportDdlException(ErrorCode.ERR_WRONG_VALUE_FOR_VAR, varName, value);
        }

        List<String> names = new ArrayList<>();
        for (Map.Entry<String, Long> entry : nameToValue.entrySet()) {
            if ((value & entry.getValue()) != 0) {
                names.add(entry.getKey());
            }
        }
        return Joiner.on(',').join(names);
    }

    /**
     * Create a case-insensitive TreeMap for name-to-value mappings.
     */
    public static Map<String, Long> newCaseInsensitiveMap() {
        return Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER);
    }

    /**
     * Check if a string represents a numeric value.
     * Handles optional leading minus sign followed by digits.
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        int len = str.length();
        int start = 0;
        if (str.charAt(0) == '-') {
            if (len == 1) {
                return false;
            }
            start = 1;
        }
        for (int i = start; i < len; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Functional interface for converting a single token to a long value.
     */
    @FunctionalInterface
    public interface TokenToValue {
        long convert(String token) throws DdlException;
    }
}
