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

#include "util/timezone_utils.h"

#include <string>

#include "cctz/time_zone.h"
#include "gtest/gtest.h"

namespace doris {
namespace {

int lookup_offset_seconds(const cctz::time_zone& tz) {
    static const auto tp = cctz::civil_second(2011, 1, 1, 0, 0, 0);
    return tz.lookup(cctz::convert(tp, tz)).offset;
}

void expect_find_timezone_offset(const std::string& timezone, int expected_offset_seconds) {
    cctz::time_zone result;
    ASSERT_TRUE(TimezoneUtils::find_cctz_time_zone(timezone, result));
    EXPECT_EQ(expected_offset_seconds, lookup_offset_seconds(result));
}

void expect_fixed_offset_seconds(const std::string& timezone, int expected_offset_seconds) {
    cctz::time_zone result;
    ASSERT_TRUE(TimezoneUtils::find_cctz_time_zone(timezone, result));

    int32_t offset_seconds = 0;
    EXPECT_TRUE(TimezoneUtils::try_get_fixed_offset_seconds(result, &offset_seconds));
    EXPECT_EQ(expected_offset_seconds, offset_seconds);
}

} // namespace

TEST(TimezoneUtilsTest, ParseOffset) {
    cctz::time_zone result;

    EXPECT_TRUE(TimezoneUtils::parse_tz_offset_string("+14:00", result));
    EXPECT_EQ(14 * 3600, lookup_offset_seconds(result));

    EXPECT_TRUE(TimezoneUtils::parse_tz_offset_string("+00:00", result));
    EXPECT_EQ(0, lookup_offset_seconds(result));

    EXPECT_TRUE(TimezoneUtils::parse_tz_offset_string("+00:30", result));
    EXPECT_EQ(1800, lookup_offset_seconds(result));

    EXPECT_TRUE(TimezoneUtils::parse_tz_offset_string("+10:30", result));
    EXPECT_EQ(10 * 3600 + 1800, lookup_offset_seconds(result));

    EXPECT_TRUE(TimezoneUtils::parse_tz_offset_string("-12:00", result));
    EXPECT_EQ(-12 * 3600, lookup_offset_seconds(result));

    EXPECT_TRUE(TimezoneUtils::parse_tz_offset_string("-00:30", result));
    EXPECT_EQ(-1800, lookup_offset_seconds(result));

    EXPECT_TRUE(TimezoneUtils::parse_tz_offset_string("UTC", result));
    EXPECT_EQ(0, lookup_offset_seconds(result));

    EXPECT_TRUE(TimezoneUtils::parse_tz_offset_string("GMT", result));
    EXPECT_EQ(0, lookup_offset_seconds(result));

    EXPECT_TRUE(TimezoneUtils::parse_tz_offset_string("gmt", result));
    EXPECT_EQ(0, lookup_offset_seconds(result));

    EXPECT_TRUE(TimezoneUtils::parse_tz_offset_string("Etc/GMT", result));
    EXPECT_EQ(0, lookup_offset_seconds(result));

    EXPECT_TRUE(TimezoneUtils::parse_tz_offset_string("UTC+8", result));
    EXPECT_EQ(8 * 3600, lookup_offset_seconds(result));

    EXPECT_TRUE(TimezoneUtils::parse_tz_offset_string("GMT-06:30", result));
    EXPECT_EQ(-(6 * 3600 + 1800), lookup_offset_seconds(result));

    EXPECT_FALSE(TimezoneUtils::parse_tz_offset_string("+15:00", result));
    EXPECT_FALSE(TimezoneUtils::parse_tz_offset_string("-13:00", result));
    EXPECT_FALSE(TimezoneUtils::parse_tz_offset_string("UTC+", result));
    EXPECT_FALSE(TimezoneUtils::parse_tz_offset_string("GMT+8:75", result));
    EXPECT_FALSE(TimezoneUtils::parse_tz_offset_string("+800", result));
    EXPECT_FALSE(TimezoneUtils::parse_tz_offset_string("0800", result));
}

TEST(TimezoneUtilsTest, LoadOffsets) {
    TimezoneUtils::clear_timezone_caches();
    TimezoneUtils::load_offsets_to_cache();
    EXPECT_EQ((13 + 15) * 3, TimezoneUtils::cache_size());

    TimezoneUtils::load_timezones_to_cache();
    EXPECT_GE(TimezoneUtils::cache_size(), 100);
}

TEST(TimezoneUtilsTest, FindTimezoneWithoutCache) {
    TimezoneUtils::clear_timezone_caches();

    expect_find_timezone_offset("GMT", 0);
    expect_find_timezone_offset("gmt", 0);
    expect_find_timezone_offset("Etc/GMT", 0);
    expect_find_timezone_offset("UTC+8", 8 * 3600);
    expect_find_timezone_offset("GMT-06:30", -(6 * 3600 + 1800));
    expect_find_timezone_offset("+08:00", 8 * 3600);
    expect_find_timezone_offset("-00:30", -1800);

    cctz::time_zone result;
    EXPECT_FALSE(TimezoneUtils::find_cctz_time_zone("UTC+", result));
    EXPECT_FALSE(TimezoneUtils::find_cctz_time_zone("GMT+8:75", result));
    EXPECT_FALSE(TimezoneUtils::find_cctz_time_zone("+800", result));
}

TEST(TimezoneUtilsTest, FindTimezoneWithOffsetCacheOnly) {
    TimezoneUtils::clear_timezone_caches();
    TimezoneUtils::load_offsets_to_cache();

    expect_find_timezone_offset("GMT", 0);
    expect_find_timezone_offset("gmt", 0);
    expect_find_timezone_offset("Etc/GMT", 0);
    expect_find_timezone_offset("UTC", 0);
    expect_find_timezone_offset("+08:00", 8 * 3600);
    expect_find_timezone_offset("UTC+8", 8 * 3600);
}

TEST(TimezoneUtilsTest, FindTimezoneWithFullCache) {
    TimezoneUtils::clear_timezone_caches();
    TimezoneUtils::load_timezones_to_cache();

    expect_find_timezone_offset("GMT", 0);
    expect_find_timezone_offset("gmt", 0);
    expect_find_timezone_offset("Etc/GMT", 0);
    expect_find_timezone_offset("Etc/GMT-8", 8 * 3600);
    expect_find_timezone_offset("Asia/Shanghai", 8 * 3600);
}

TEST(TimezoneUtilsTest, TryGetFixedOffsetSeconds) {
    TimezoneUtils::clear_timezone_caches();
    TimezoneUtils::load_timezones_to_cache();

    expect_fixed_offset_seconds("UTC", 0);
    expect_fixed_offset_seconds("GMT", 0);
    expect_fixed_offset_seconds("gmt", 0);
    expect_fixed_offset_seconds("Etc/UTC", 0);
    expect_fixed_offset_seconds("Etc/GMT", 0);
    expect_fixed_offset_seconds("+08:00", 8 * 3600);
    expect_fixed_offset_seconds("Etc/GMT-8", 8 * 3600);
    expect_fixed_offset_seconds("-06:00", -6 * 3600);
    expect_fixed_offset_seconds("+05:45", 5 * 3600 + 45 * 60);

    cctz::time_zone result;
    ASSERT_TRUE(TimezoneUtils::find_cctz_time_zone("Asia/Shanghai", result));

    int32_t offset_seconds = 0;
    EXPECT_FALSE(TimezoneUtils::try_get_fixed_offset_seconds(result, &offset_seconds));
}

} // namespace doris
