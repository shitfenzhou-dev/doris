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

    std::vector<uint8_t> buffer(intermediate.size(), 0);
    int64_t decoded_len =
            base64_decode(intermediate.data(), intermediate.size(), reinterpret_cast<char*>(buffer.data()));
    EXPECT_GE(decoded_len, 0);
    EXPECT_EQ(std::string(reinterpret_cast<char*>(buffer.data()), decoded_len), input);
}

TEST(UrlCodingTest, Basic) {
    std::string input =
            "ABCDEFGHIJKLMNOPQRSTUWXYZ1234567890~!@#$%^&*()<>?,./:\";'{}|[]\\_+-=";
    test_url(input, "");
}

TEST(UrlCodingTest, BlankString) {
    test_url("", "");
}

TEST(UrlCodingTest, PathSeparators) {
    test_url("/home/doris/directory/", "%2Fhome%2Fdoris%2Fdirectory%2F");
}

TEST(UrlCodingTest, SpaceEncoding) {
    test_url("hello world", "hello+world");
    test_url(" +", "+%2B");
}

TEST(UrlCodingTest, UrlDecodeInvalid) {
    std::string output;

    EXPECT_FALSE(url_decode("%", &output));

    EXPECT_FALSE(url_decode("%1", &output));

    EXPECT_FALSE(url_decode("%GG", &output));

    EXPECT_TRUE(url_decode("hello", &output));
    EXPECT_EQ(output, "hello");

    EXPECT_TRUE(url_decode("%20", &output));
    EXPECT_EQ(output, " ");

    EXPECT_TRUE(url_decode("a+b", &output));
    EXPECT_EQ(output, "a b");
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
    std::string encoded;
    base64_encode("", &encoded);
    EXPECT_EQ(encoded, "");

    std::string decoded;
    EXPECT_TRUE(base64_decode("", &decoded));
    EXPECT_EQ(decoded, "");
}

TEST(Base64Test, RawBufferEncode) {
    const std::string input = "hello";
    std::string encoded;
    base64_encode(input, &encoded);

    std::vector<unsigned char> buffer(encoded.size() + 16, 0);
    size_t len = base64_encode(reinterpret_cast<const unsigned char*>(input.data()), input.size(),
                               buffer.data());
    EXPECT_EQ(std::string(reinterpret_cast<char*>(buffer.data()), len), encoded);
}

TEST(Base64Test, DecodeInvalid) {
    std::string output;

    EXPECT_FALSE(base64_decode("!!!invalid!!!", &output));

    EXPECT_FALSE(base64_decode("====", &output));
}

TEST(HtmlEscapingTest, Basic) {
    std::string before = "<html><body>&amp";
    std::stringstream after;
    escape_for_html(before, &after);
    EXPECT_EQ(after.str(), "&lt;html&gt;&lt;body&gt;&amp;amp");
}

TEST(HtmlEscapingTest, ToString) {
    std::string before = "<html><body>&amp";
    std::string result = escape_for_html_to_string(before);
    EXPECT_EQ(result, "&lt;html&gt;&lt;body&gt;&amp;amp");
}

} // namespace doris
