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
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractFlagConverter {

    private final String variableName;
    private final long allowedMask;
    private final Map<String, Long> nameToValueMap;

    protected AbstractFlagConverter(String variableName, long allowedMask, Map<String, Long> nameToValueMap) {
        this.variableName = variableName;
        this.allowedMask = allowedMask;
        this.nameToValueMap = Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER);
        this.nameToValueMap.putAll(nameToValueMap);
    }

    protected String getVariableName() {
        return variableName;
    }

    protected long getAllowedMask() {
        return allowedMask;
    }

    protected Map<String, Long> getNameToValueMap() {
        return nameToValueMap;
    }

    public String decode(Long value) throws DdlException {
        if (value == 0L) {
            return "";
        }
        if ((value & ~allowedMask) != 0) {
            ErrorReport.reportDdlException(ErrorCode.ERR_WRONG_VALUE_FOR_VAR, variableName, value);
        }

        List<String> names = new ArrayList<>();
        for (Map.Entry<String, Long> entry : nameToValueMap.entrySet()) {
            if ((value & entry.getValue()) != 0) {
                names.add(entry.getKey());
            }
        }

        return Joiner.on(',').join(names);
    }

    public Long encode(String value) throws DdlException {
        List<String> names = Splitter.on(',').trimResults().omitEmptyStrings().splitToList(value);

        long resultCode = 0L;
        for (String key : names) {
            long code = 0;
            if (StringUtils.isNumeric(key)) {
                code |= Long.parseLong(key);
            } else {
                code = getCodeFromString(key);
                if (code == 0) {
                    ErrorReport.reportDdlException(ErrorCode.ERR_WRONG_VALUE_FOR_VAR, variableName, key);
                }
            }
            resultCode |= code;
            if ((resultCode & ~allowedMask) != 0) {
                ErrorReport.reportDdlException(ErrorCode.ERR_WRONG_VALUE_FOR_VAR, variableName, key);
            }
        }

        resultCode = postProcess(resultCode);
        return resultCode;
    }

    protected long postProcess(long value) throws DdlException {
        return value;
    }

    protected long expand(long value) throws DdlException {
        return value;
    }

    protected long getCodeFromString(String key) {
        if (key != null && nameToValueMap.containsKey(key)) {
            return nameToValueMap.get(key);
        }
        return 0;
    }

    public boolean isSupported(String key) {
        return key != null && nameToValueMap.containsKey(key);
    }
}
