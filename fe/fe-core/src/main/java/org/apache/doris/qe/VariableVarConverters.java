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

/**
 * Helper class to drives the convert of session variables according to the converters.
 * You can define your converter that implements interface VariableVarConverterI in here.
 * Each converter should put in map (variable name -> converters) and only converts the variable with specified name.
 *
 * The converted session variable is a special kind of variable.
 * It's real type is Long or Int, for example, the variable `sql_mode` is Long, when querying `select @@sql_mode`,
 * the return column type is String.
 * But user usually set this variable by string, such as:
 * `set @@sql_mode = 'STRICT_TRANS_TABLES'`
 * or
 * `set @@sql_mode = concat(@@sql_mode, 'STRICT_TRANS_TABLES')'`
 */
public class VariableVarConverters {

    public static final Map<String, VariableVarConverterI> converters = Maps.newHashMap();

    static {
        SqlModeConverter sqlModeConverter = new SqlModeConverter();
        converters.put(SessionVariable.SQL_MODE, sqlModeConverter);
        RuntimeFilterTypeConverter runtimeFilterTypeConverter = new RuntimeFilterTypeConverter();
        converters.put(SessionVariable.RUNTIME_FILTER_TYPE, runtimeFilterTypeConverter);
        ValidatePasswordPolicyConverter validatePasswordPolicyConverter = new ValidatePasswordPolicyConverter();
        converters.put(GlobalVariable.VALIDATE_PASSWORD_POLICY, validatePasswordPolicyConverter);
        SqlSelectLimitConverter sqlSelectLimitConverter = new SqlSelectLimitConverter();
        converters.put(SessionVariable.SQL_SELECT_LIMIT, sqlSelectLimitConverter);
    }

    public static Boolean hasConverter(String varName) {
        return converters.containsKey(varName);
    }

    public static Long encode(String varName, String value) throws DdlException {
        if (converters.containsKey(varName)) {
            return converters.get(varName).encode(value);
        }
        return 0L;
    }

    public static String decode(String varName, Long value) throws DdlException {
        if (converters.containsKey(varName)) {
            return converters.get(varName).decode(value);
        }
        return "";
    }

    // Shared methods for parsing bit field variables like sql_mode and runtime_filter_type
    public static long encodeBitFieldVar(String varName, String varValue, long allowedMask,
                                         Map<String, Long> supportedMap,
                                         Map<String, Long> combineMap) throws DdlException {
        List<String> names = Splitter.on(',').trimResults().omitEmptyStrings().splitToList(varValue);

        long resultCode = 0L;
        for (String key : names) {
            long code = 0L;
            if (StringUtils.isNumeric(key)) {
                try {
                    code = Long.parseLong(key);
                    // expand numeric value if it is a combine mode
                    if (combineMap != null) {
                        for (Map.Entry<String, Long> entry : combineMap.entrySet()) {
                            if ((code & supportedMap.get(entry.getKey())) != 0) {
                                code |= entry.getValue();
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    ErrorReport.reportDdlException(ErrorCode.ERR_WRONG_VALUE_FOR_VAR, varName, key);
                }
            } else {
                if (supportedMap.containsKey(key)) {
                    code |= supportedMap.get(key);
                    if (combineMap != null && combineMap.containsKey(key)) {
                        code |= combineMap.get(key);
                    }
                } else {
                    ErrorReport.reportDdlException(ErrorCode.ERR_WRONG_VALUE_FOR_VAR, varName, key);
                }
            }
            resultCode |= code;
            if ((resultCode & ~allowedMask) != 0) {
                ErrorReport.reportDdlException(ErrorCode.ERR_WRONG_VALUE_FOR_VAR, varName, key);
            }
        }
        return resultCode;
    }

    public static String decodeBitFieldVar(String varName, Long varValue, long allowedMask,
                                           Map<String, Long> supportedMap, Long emptyValue) throws DdlException {
        if (varValue.equals(emptyValue)) {
            return "";
        }
        if ((varValue & ~allowedMask) != 0) {
            ErrorReport.reportDdlException(ErrorCode.ERR_WRONG_VALUE_FOR_VAR, varName, varValue);
        }

        List<String> names = new ArrayList<>();
        for (Map.Entry<String, Long> value : supportedMap.entrySet()) {
            if ((varValue & value.getValue()) != 0) {
                names.add(value.getKey());
            }
        }

        return Joiner.on(',').join(names);
    }

    /* Converters */

    // Converter to convert sql mode variable
    public static class SqlModeConverter implements VariableVarConverterI {
        @Override
        public Long encode(String value) throws DdlException {
            return SqlModeHelper.encode(value);
        }

        @Override
        public String decode(Long value) throws DdlException {
            return SqlModeHelper.decode(value);
        }
    }

    // Converter to convert runtime filter type variable
    public static class RuntimeFilterTypeConverter implements VariableVarConverterI {
        @Override
        public Long encode(String value) throws DdlException {
            return RuntimeFilterTypeHelper.encode(value);
        }

        @Override
        public String decode(Long value) throws DdlException {
            return RuntimeFilterTypeHelper.decode(value);
        }
    }

    // Converter to convert sql select limit variable
    public static class SqlSelectLimitConverter implements VariableVarConverterI {
        @Override
        public Long encode(String value) throws DdlException {
            if (value.equalsIgnoreCase("DEFAULT")) {
                return Long.MAX_VALUE;
            } else {
                try {
                    return Long.parseLong(value);
                } catch (NumberFormatException e) {
                    throw new DdlException("Invalid sql_select_limit value: " + value);
                }
            }
        }

        @Override
        public String decode(Long value) throws DdlException {
            return String.valueOf(value);
        }
    }

    public static class ValidatePasswordPolicyConverter implements VariableVarConverterI {
        @Override
        public Long encode(String value) throws DdlException {
            if (StringUtils.isNumeric(value)) {
                long val = Long.valueOf(value);
                if (val != GlobalVariable.VALIDATE_PASSWORD_POLICY_DISABLED
                        && val != GlobalVariable.VALIDATE_PASSWORD_POLICY_STRONG) {
                    throw new DdlException("Invalid validate_password_policy value: " + value);
                }
                return val;
            } else if (value.equalsIgnoreCase("NONE")) {
                return 0L;
            } else if (value.equalsIgnoreCase("STRONG")) {
                return 2L;
            } else {
                throw new DdlException("Invalid validate_password_policy value: " + value);
            }
        }

        @Override
        public String decode(Long value) throws DdlException {
            if (value == GlobalVariable.VALIDATE_PASSWORD_POLICY_DISABLED) {
                return "NONE";
            } else if (value == GlobalVariable.VALIDATE_PASSWORD_POLICY_STRONG) {
                return "STRONG";
            } else {
                throw new DdlException("Invalid validate_password_policy value: " + value);
            }
        }
    }
}
