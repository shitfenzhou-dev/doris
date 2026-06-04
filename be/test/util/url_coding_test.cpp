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
#include <sstream>
#include <string>

namespace doris {

// Tests URL encoding/decoding
void test_url(const std::string& input, const std::string& expected_encoded = "") {
    std::string intermediate;
    url_encode(input, &intermediate);
    std::string output;

    if (!expected_encoded.empty()) {
        EXPECT_EQ(intermediate, expected_encoded);
    }

    EXPECT_TRUE(url_decode(intermediate, &output));
    EXPECT_EQ(input, output);
}

// Tests base64 encoding/decoding
void test_base64(const std::string& input, const std::string& expected_encoded = "") {
    std::string intermediate;
    base64_encode(input, &intermediate);
    std::string output;

    if (!expected_encoded.empty()) {
        EXPECT_EQ(intermediate, expected_encoded);
    }

    EXPECT_TRUE(base64_decode(intermediate, &output));
    EXPECT_EQ(input, output);
}

// Test URL encoding basic functionality
TEST(UrlCodingTest, Basic) {
    std::string input = "ABCDEFGHIJKLMNOPQRSTUWXYZ1234567890~!@#$%^&*()<>?,./:\";'{}|[]\\_+-=";
    test_url(input);
}

// Test URL encoding with spaces (should be encoded as '+')
TEST(UrlCodingTest, SpaceEncoding) {
    test_url("hello world", "hello+world");
    test_url("  space  ", "++space++");
    test_url("a b c", "a+b+c");
}

// Test URL encoding with empty string
TEST(UrlCodingTest, EmptyString) {
    test_url("", "");
}

// Test URL encoding with path separators
TEST(UrlCodingTest, PathSeparators) {
    test_url("/home/doris/directory/", "%2Fhome%2Fdoris%2Fdirectory%2F");
    test_url("path/to/file.txt", "path%2Fto%2Ffile.txt");
}

// Test URL encoding with UTF-8 bytes
TEST(UrlCodingTest, Utf8Encoding) {
    test_url("中文测试", "%E4%B8%AD%E6%96%87%E6%B5%8B%E8%AF%95");
    test_url("ñáéíóú", "%C3%B1%C3%A1%C3%A9%C3%AD%C3%B3%C3%BA");
    test_url("😊", "%F0%9F%98%8A");
}

// Test URL encode/decode consistency after repeated operations
TEST(UrlCodingTest, RepeatedEncodeDecode) {
    std::string input = "test with spaces & special chars!@#";
    std::string encoded1, encoded2;
    std::string decoded1, decoded2;
    
    url_encode(input, &encoded1);
    url_decode(encoded1, &decoded1);
    EXPECT_EQ(input, decoded1);
    
    url_encode(decoded1, &encoded2);
    url_decode(encoded2, &decoded2);
    EXPECT_EQ(input, decoded2);
    EXPECT_EQ(encoded1, encoded2);
}

// Test URL decoding failure paths
TEST(UrlCodingTest, DecodeFailure) {
    std::string output;
    
    // Truncated '%'
    EXPECT_FALSE(url_decode("test%", &output));
    
    // Invalid hex character
    EXPECT_FALSE(url_decode("test%XX", &output));
    
    // Incomplete percent encoding
    EXPECT_FALSE(url_decode("test%a", &output));
    
    // Invalid hex digits
    EXPECT_FALSE(url_decode("test%GG", &output));
}

// Test base64 encoding
TEST(Base64Test, Basic) {
    test_base64("a", "YQ==");
    test_base64("ab", "YWI=");
    test_base64("abc", "YWJj");
    test_base64("abcd", "YWJjZA==");
    test_base64("abcde", "YWJjZGU=");
    test_base64("abcdef", "YWJjZGVm");
}

// Test base64 encoding with empty string
TEST(Base64Test, EmptyString) {
    test_base64("", "");
}

// Test base64 encoding with binary data
TEST(Base64Test, BinaryData) {
    std::string input("\x00\x01\x02\x03\x04\x05", 6);
    test_base64(input);
}

// Test base64 decode failure paths
TEST(Base64Test, DecodeFailure) {
    std::string output;
    
    // Invalid character in base64
    EXPECT_FALSE(base64_decode("invalid!", &output));
    
    // Invalid padding
    EXPECT_FALSE(base64_decode("YQ===", &output));
    
    // Empty string (should succeed)
    EXPECT_TRUE(base64_decode("", &output));
}

// Test HTML escaping
TEST(HtmlEscapingTest, Basic) {
    std::string before = "<html><body>&amp";
    std::stringstream after;
    escape_for_html(before, &after);
    EXPECT_EQ(after.str(), "&lt;html&gt;&lt;body&gt;&amp;amp");
}

// Test HTML escaping with empty string
TEST(HtmlEscapingTest, EmptyString) {
    std::string before = "";
    std::stringstream after;
    escape_for_html(before, &after);
    EXPECT_EQ(after.str(), "");
    
    EXPECT_EQ(escape_for_html_to_string(before), "");
}

// Test HTML escaping with various special characters
TEST(HtmlEscapingTest, SpecialChars) {
    std::string before = "<>&";
    EXPECT_EQ(escape_for_html_to_string(before), "&lt;&gt;&amp;");
    
    std::string before2 = "a<b>c&d";
    EXPECT_EQ(escape_for_html_to_string(before2), "a&lt;b&gt;c&amp;d");
}

// Test HTML escaping with normal characters (no escaping needed)
TEST(HtmlEscapingTest, NormalChars) {
    std::string before = "hello world 123!@#$";
    EXPECT_EQ(escape_for_html_to_string(before), before);
}

} // namespace doris

