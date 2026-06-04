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
#include <stdio.h>
#include <stdlib.h>

#include <iostream>

namespace doris {

// Tests encoding/decoding of input.  If expected_encoded is non-empty, the
// encoded string is validated against it.
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

// Test URL encoding. Check that the values that are put in are the
// same that come out.
TEST(UrlCodingTest, Basic) {
    std::string input = "ABCDEFGHIJKLMNOPQRSTUWXYZ1234567890~!@#$%^&*()<>?,./:\";'{}|[]\\_+-=";
    test_url(input, "");
}

TEST(UrlCodingTest, BlankString) {
    test_url("", "");
}

TEST(UrlCodingTest, PathSeparators) {
    test_url("/home/doris/directory/", "%2Fhome%2Fdoris%2Fdirectory%2F");
}

TEST(UrlCodingTest, InvalidUrlDecode) {
    std::string output;
    
    // One invalid hex char
    EXPECT_FALSE(url_decode("%1G", &output));
    EXPECT_TRUE(output.empty()); // Ensure buffer is cleared on failure

    EXPECT_FALSE(url_decode("%G1", &output));
    EXPECT_TRUE(output.empty());

    EXPECT_FALSE(url_decode("%XX", &output));
    EXPECT_TRUE(output.empty());
    
    // Truncated percent bytes
    EXPECT_FALSE(url_decode("%", &output));
    EXPECT_TRUE(output.empty());
    
    EXPECT_FALSE(url_decode("%1", &output));
    EXPECT_TRUE(output.empty());

    EXPECT_FALSE(url_decode("abc%", &output));
    EXPECT_TRUE(output.empty());

    EXPECT_FALSE(url_decode("abc%1", &output));
    EXPECT_TRUE(output.empty());

    // Mixed valid and invalid
    EXPECT_FALSE(url_decode("%2F%1G", &output));
    EXPECT_TRUE(output.empty());
}

TEST(UrlCodingTest, ValidUrlDecode) {
    std::string output;
    
    // valid URL decode
    EXPECT_TRUE(url_decode("%2F", &output));
    EXPECT_EQ(output, "/");

    // plus sign
    EXPECT_TRUE(url_decode("a+b", &output));
    EXPECT_EQ(output, "a b");

    // utf8 chinese
    EXPECT_TRUE(url_decode("%E4%B8%AD", &output));
    EXPECT_EQ(output, "中");

    // space
    EXPECT_TRUE(url_decode("%20", &output));
    EXPECT_EQ(output, " ");

    // lowercase hex
    EXPECT_TRUE(url_decode("%2f", &output));
    EXPECT_EQ(output, "/");
    
    EXPECT_TRUE(url_decode("%e4%b8%ad", &output));
    EXPECT_EQ(output, "中");
}

TEST(Base64Test, Basic) {
    test_base64("a", "YQ==");
    test_base64("ab", "YWI=");
    test_base64("abc", "YWJj");
    test_base64("abcd", "YWJjZA==");
    test_base64("abcde", "YWJjZGU=");
    test_base64("abcdef", "YWJjZGVm");
}

TEST(HtmlEscapingTest, Basic) {
    std::string before = "<html><body>&amp";
    std::stringstream after;
    escape_for_html(before, &after);
    EXPECT_EQ(after.str(), "&lt;html&gt;&lt;body&gt;&amp;amp");
}

} // namespace doris
