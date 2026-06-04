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

#include "util/url_coding.h"

#include <gtest/gtest.h>

#include <sstream>

namespace doris {

void test_url(const std::string& input, const std::string& expected_encoded) {
    std::string intermediate;
    url_encode(input, &intermediate);
    std::string output;

    if (!expected_encoded.empty()) {
        EXPECT_EQ(intermediate, expected_encoded);
    }

    EXPECT_TRUE(url_decode(intermediate, &output));
    EXPECT_EQ(input, output);
}

void test_base64(const std::string& input, const std::string& expected_encoded) {
    std::string intermediate;
    base64_encode(input, &intermediate);
    std::string output;

    if (!expected_encoded.empty()) {
        EXPECT_EQ(intermediate, expected_encoded);
    }

    EXPECT_TRUE(base64_decode(intermediate, &output));
    EXPECT_EQ(input, output);
}

TEST(UrlCodingTest, Basic) {
    std::string input = "ABCDEFGHIJKLMNOPQRSTUWXYZ1234567890~!@#$%^&*()<>?,./:\";'{}|[]\\_+-=";
    test_url(input, "");
}

TEST(UrlCodingTest, SpaceAndPlus) {
    std::string input = " +";
    std::string encoded;
    url_encode(input, &encoded);
    std::string decoded;
    EXPECT_TRUE(url_decode(encoded, &decoded));
    EXPECT_EQ(input, decoded);
}

TEST(UrlCodingTest, BlankString) {
    test_url("", "");
}

TEST(UrlCodingTest, PathSeparators) {
    test_url("/home/doris/directory/", "%2Fhome%2Fdoris%2Fdirectory%2F");
}

TEST(UrlCodingTest, InvalidDecodeTruncatedPercent) {
    std::string output;
    EXPECT_FALSE(url_decode("%", &output));
    EXPECT_FALSE(url_decode("%2", &output));
}

TEST(UrlCodingTest, InvalidDecodeBadHex) {
    std::string output;
    EXPECT_FALSE(url_decode("%ZZ", &output));
    EXPECT_FALSE(url_decode("%2G", &output));
}

TEST(UrlCodingTest, ValidDecodePercent) {
    std::string output;
    EXPECT_TRUE(url_decode("%41", &output));
    EXPECT_EQ(output, "A");
}

TEST(Base64Test, Basic) {
    test_base64("a", "YQ==");
    test_base64("ab", "YWI=");
    test_base64("abc", "YWJj");
    test_base64("abcd", "YWJjZA==");
    test_base64("abcde", "YWJjZGU=");
    test_base64("abcdef", "YWJjZGVm");
}

TEST(Base64Test, EmptyString) {
    test_base64("", "");
}

TEST(Base64Test, InvalidDecode) {
    std::string output;
    EXPECT_FALSE(base64_decode("!!!", &output));
}

TEST(HtmlEscapingTest, Basic) {
    std::string before = "<html><body>&amp";
    std::stringstream after;
    escape_for_html(before, &after);
    EXPECT_EQ(after.str(), "&lt;html&gt;&lt;body&gt;&amp;amp");
}

TEST(HtmlEscapingTest, EmptyString) {
    std::string before;
    std::stringstream after;
    escape_for_html(before, &after);
    EXPECT_EQ(after.str(), "");
}

TEST(HtmlEscapingTest, NoSpecialChars) {
    std::string before = "hello world 123";
    std::stringstream after;
    escape_for_html(before, &after);
    EXPECT_EQ(after.str(), "hello world 123");
}

} // namespace doris