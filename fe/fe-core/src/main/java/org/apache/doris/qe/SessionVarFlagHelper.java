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
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Base helper for session variables that encode as comma-separated flag names
 * where each flag maps to a unique bit position in a long value.
 *
 * Provides common encode/decode logic shared by SqlMode and RuntimeFilterType.
 * Subclasses override hooks to inject domain-specific behavior such as
 * combine-mode expansion or mutual-exclusion validation.
 */
public class SessionVarFlagHelper {

    protected final String varName;
    protected final long allowedMask;
    protected final Map<String, Long> flagNameMap;

    protected SessionVarFlagHelper(String varName, long allowedMask) {
        this.varName = varName;
        this.allowedMask = allowedMask;
        this.flagNameMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    protected void registerFlag(String name, long value) {
        flagNameMap.put(name, value);
    }

    protected long getCodeFromString(String name) {
        Long code = flagNameMap.get(name);
        return code != null ? code : 0L;
    }

    protected long expandNumeric(long value) throws DdlException {
        return value;
    }

    protected void validateAfterEncode(long resultCode) throws DdlException {
    }

    protected boolean isSpecialZeroCase(long varValue) {
        return varValue == 0;
    }

    public String decode(Long varValue) throws DdlException {
        if (isSpecialZeroCase(varValue)) {
            return "";
        }
        if ((varValue & ~allowedMask) != 0) {
            ErrorReport.reportDdlException(ErrorCode.ERR_WRONG_VALUE_FOR_VAR, varName, varValue);
        }

        List<String> names = new ArrayList<>();
        for (Map.Entry<String, Long> entry : flagNameMap.entrySet()) {
            if ((varValue & entry.getValue()) != 0) {
                names.add(entry.getKey());
            }
        }
        return Joiner.on(',').join(names);
    }

    public Long encode(String varValue) throws DdlException {
        List<String> names = Splitter.on(',').trimResults().omitEmptyStrings().splitToList(varValue);

        long resultCode = 0L;
        for (String key : names) {
            long code = 0L;
            if (StringUtils.isNumeric(key)) {
                code = expandNumeric(Long.parseLong(key));
            } else {
                code = getCodeFromString(key);
                if (code == 0) {
                    ErrorReport.reportDdlException(ErrorCode.ERR_WRONG_VALUE_FOR_VAR, varName, key);
                }
            }
            resultCode |= code;
            if ((resultCode & ~allowedMask) != 0) {
                ErrorReport.reportDdlException(ErrorCode.ERR_WRONG_VALUE_FOR_VAR, varName, key);
            }
        }

        validateAfterEncode(resultCode);
        return resultCode;
    }

    public boolean isSupported(String name) {
        return name != null && flagNameMap.containsKey(name);
    }

    public Map<String, Long> getFlagNameMap() {
        return flagNameMap;
    }
}