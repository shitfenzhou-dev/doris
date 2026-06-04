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
void test_url(const string& input, const string& expected_encoded, bool hive_compat) {
    std::string intermediate;
    url_encode(input, &intermediate, hive_compat);
    std::string output;

    if (!expected_encoded.empty()) {
        EXPECT_EQ(intermediate, expected_encoded);
    }

    EXPECT_TRUE(UrlDecode(intermediate, &output, hive_compat));
    EXPECT_EQ(input, output);

    // Convert string to vector and try that also
    std::vector<uint8_t> input_vector;
    input_vector.resize(input.size());
    memcpy(&input_vector[0], input.c_str(), input.size());
    std::string intermediate2;
    url_encode(input_vector, &intermediate2, hive_compat);
    EXPECT_EQ(intermediate, intermediate2);
}

void test_base64(const string& input, const string& expected_encoded) {
    std::string intermediate;
    Base64Encode(input, &intermediate);
    std::string output;

    if (!expected_encoded.empty()) {
        EXPECT_EQ(intermediate, expected_encoded);
    }

    EXPECT_TRUE(Base64Decode(intermediate, &output));
    EXPECT_EQ(input, output);

    // Convert string to vector and try that also
    std::vector<uint8_t> input_vector;
    input_vector.resize(input.size());
    memcpy(&input_vector[0], input.c_str(), input.size());
    std::string intermediate2;
    Base64Encode(input_vector, &intermediate2);
    EXPECT_EQ(intermediate, intermediate2);
}

// Test URL encoding. Check that the values that are put in are the
// same that come out.
TEST(UrlCodingTest, Basic) {
    std::string input = "ABCDEFGHIJKLMNOPQRSTUWXYZ1234567890~!@#$%^&*()<>?,./:\";'{}|[]\\_+-=";
    test_url(input, "", false);
    test_url(input, "", true);
}

TEST(UrlCodingTest, HiveExceptions) {
    test_url(" +", " +", true);
}

TEST(UrlCodingTest, BlankString) {
    test_url("", "", false);
    test_url("", "", true);
}

TEST(UrlCodingTest, PathSeparators) {
    test_url("/home/doris/directory/", "%2Fhome%2Fdoris%2Fdirectory%2F", false);
    test_url("/home/doris/directory/", "%2Fhome%2Fdoris%2Fdirectory%2F", true);
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
    EscapeForHtml(before, &after);
    EXPECT_EQ(after.str(), "&lt;html&gt;&lt;body&gt;&amp;amp");
}

TEST(UrlDecodeTest, ValidPercentEncoding) {
    std::string out;

    EXPECT_TRUE(url_decode("%2F", &out));
    EXPECT_EQ(out, "/");

    EXPECT_TRUE(url_decode("%20", &out));
    EXPECT_EQ(out, " ");

    EXPECT_TRUE(url_decode("hello%20world", &out));
    EXPECT_EQ(out, "hello world");

    EXPECT_TRUE(url_decode("%41%42%43", &out));
    EXPECT_EQ(out, "ABC");

    EXPECT_TRUE(url_decode("%E4%B8%AD", &out));
    EXPECT_EQ(out, "\xE4\xB8\xAD");

    EXPECT_TRUE(url_decode("", &out));
    EXPECT_EQ(out, "");

    EXPECT_TRUE(url_decode("no_percent", &out));
    EXPECT_EQ(out, "no_percent");

    EXPECT_TRUE(url_decode("a+b", &out));
    EXPECT_EQ(out, "a b");

    EXPECT_TRUE(url_decode("%2f", &out));
    EXPECT_EQ(out, "/");

    EXPECT_TRUE(url_decode("%0A", &out));
    EXPECT_EQ(out, "\x0A");

    EXPECT_TRUE(url_decode("%ff", &out));
    EXPECT_EQ(out, "\xFF");
}

TEST(UrlDecodeTest, InvalidPercentEncoding) {
    std::string out;

    EXPECT_FALSE(url_decode("%1G", &out));

    EXPECT_FALSE(url_decode("%G1", &out));

    EXPECT_FALSE(url_decode("%XX", &out));

    EXPECT_FALSE(url_decode("%", &out));

    EXPECT_FALSE(url_decode("%1", &out));

    EXPECT_FALSE(url_decode("%ZZ", &out));

    EXPECT_FALSE(url_decode("abc%1G", &out));

    EXPECT_FALSE(url_decode("%AG", &out));

    EXPECT_FALSE(url_decode("test%xy", &out));
}

TEST(UrlDecodeTest, OutputBufferOnFailure) {
    std::string out;

    out = "previous_content";
    EXPECT_FALSE(url_decode("%1G", &out));
    EXPECT_TRUE(out.empty()) << "out buffer should be cleared before processing";

    out = "previous_content";
    EXPECT_FALSE(url_decode("abc%1G", &out));
    EXPECT_EQ(out, "abc") << "out buffer contains partially decoded content up to failure point, "
                             "caller must not rely on out value when url_decode returns false";
}

TEST(UrlDecodeTest, BoundaryCases) {
    std::string out;

    EXPECT_TRUE(url_decode("%00", &out));
    EXPECT_EQ(out, std::string("\x00", 1));

    EXPECT_TRUE(url_decode("%7F", &out));
    EXPECT_EQ(out, "\x7F");

    EXPECT_TRUE(url_decode("%FF", &out));
    EXPECT_EQ(out, "\xFF");

    EXPECT_TRUE(url_decode("100%25", &out));
    EXPECT_EQ(out, "100%");

    EXPECT_TRUE(url_decode("%25", &out));
    EXPECT_EQ(out, "%");

    EXPECT_FALSE(url_decode("%%", &out));

    EXPECT_FALSE(url_decode("100%", &out));

    EXPECT_FALSE(url_decode("100%1", &out));
}

} // namespace doris
