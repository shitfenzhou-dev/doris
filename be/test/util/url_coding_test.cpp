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

#include <cmath>
#include <iostream>

namespace doris {

// Tests encoding/decoding of input.  If expected_encoded is non-empty, the
// encoded string is validated against it.
void test_url(const string& input, const string& expected_encoded) {
    std::string intermediate;
    url_encode(input, &intermediate);
    std::string output;

    if (!expected_encoded.empty()) {
        EXPECT_EQ(intermediate, expected_encoded);
    }

    EXPECT_TRUE(url_decode(intermediate, &output));
    EXPECT_EQ(input, output);

    // Convert string to vector and try that also
    std::vector<uint8_t> input_vector;
    input_vector.resize(input.size());
    memcpy(&input_vector[0], input.c_str(), input.size());
    std::string intermediate2;
    std::string_view input_view(reinterpret_cast<const char*>(input_vector.data()), input_vector.size());
    url_encode(input_view, &intermediate2);
    EXPECT_EQ(intermediate, intermediate2);
}

void test_base64(const string& input, const string& expected_encoded) {
    std::string intermediate;
    base64_encode(input, &intermediate);
    std::string output;

    if (!expected_encoded.empty()) {
        EXPECT_EQ(intermediate, expected_encoded);
    }

    EXPECT_TRUE(base64_decode(intermediate, &output));
    EXPECT_EQ(input, output);

    // Convert string to vector and try that also
    std::vector<uint8_t> input_vector;
    input_vector.resize(input.size());
    memcpy(&input_vector[0], input.c_str(), input.size());
    std::string intermediate2;
    intermediate2.resize((size_t)(4.0 * std::ceil(input_vector.size() / 3.0)) + 4);
    size_t len = base64_encode(input_vector.data(), input_vector.size(), (unsigned char*)intermediate2.data());
    intermediate2.resize(len);
    EXPECT_EQ(intermediate, intermediate2);
}

// Test URL encoding. Check that the values that are put in are the
// same that come out.
TEST(UrlCodingTest, Basic) {
    std::string input = "ABCDEFGHIJKLMNOPQRSTUWXYZ1234567890~!@#$%^&*()<>?,./:\";'{}|[]\\_+-=";
    test_url(input, "");
}

TEST(UrlCodingTest, SpaceAndPlus) {
    // Current url_encode implementation encodes space as '+' and '+' as '%2B'
    test_url(" +", "+%2B");
}

TEST(UrlCodingTest, BlankString) {
    test_url("", "");
}

TEST(UrlCodingTest, PathSeparators) {
    test_url("/home/doris/directory/", "%2Fhome%2Fdoris%2Fdirectory%2F");
}

TEST(UrlCodingTest, InvalidDecode) {
    std::string output;
    EXPECT_FALSE(url_decode("%2", &output));
    EXPECT_FALSE(url_decode("%", &output));
    EXPECT_FALSE(url_decode("%ZZ", &output));
}

TEST(Base64Test, Basic) {
    test_base64("a", "YQ==");
    test_base64("ab", "YWI=");
    test_base64("abc", "YWJj");
    test_base64("abcd", "YWJjZA==");
    test_base64("abcde", "YWJjZGU=");
    test_base64("abcdef", "YWJjZGVm");
}

TEST(Base64Test, InvalidDecode) {
    std::string output;
    // Invalid characters
    EXPECT_FALSE(base64_decode("YWJjZA==^", &output));
    // Another invalid scenario if libbase64 rejects it
    EXPECT_FALSE(base64_decode("YWJjZGU==", &output)); // length is 9, invalid base64 length
}

TEST(HtmlEscapingTest, Basic) {
    std::string before = "<html><body>&amp";
    std::stringstream after;
    escape_for_html(before, &after);
    EXPECT_EQ(after.str(), "&lt;html&gt;&lt;body&gt;&amp;amp");
}

} // namespace doris
