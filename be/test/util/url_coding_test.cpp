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

#include <string>

namespace doris {

TEST(UrlCodingTest, BasicRoundTrip) {
    std::string input = "ABCDEFGHIJKLMNOPQRSTUWXYZ1234567890~!@#$%^&*()<>?,./:\";'{}|[]\\_+-=";
    std::string encoded;
    url_encode(input, &encoded);
    std::string decoded;
    EXPECT_TRUE(url_decode(encoded, &decoded));
    EXPECT_EQ(input, decoded);
}

TEST(UrlCodingTest, BlankString) {
    std::string encoded;
    url_encode("", &encoded);
    std::string decoded;
    EXPECT_TRUE(url_decode(encoded, &decoded));
    EXPECT_EQ("", decoded);
}

TEST(UrlCodingTest, PathSeparators) {
    std::string input = "/home/doris/directory/";
    std::string encoded;
    url_encode(input, &encoded);
    EXPECT_EQ("%2Fhome%2Fdoris%2Fdirectory%2F", encoded);
    std::string decoded;
    EXPECT_TRUE(url_decode(encoded, &decoded));
    EXPECT_EQ(input, decoded);
}

TEST(UrlCodingTest, PlusDecodesToSpace) {
    std::string decoded;
    EXPECT_TRUE(url_decode("hello+world", &decoded));
    EXPECT_EQ("hello world", decoded);
}

TEST(UrlCodingTest, ValidPercentEncoding) {
    std::string decoded;
    EXPECT_TRUE(url_decode("%20", &decoded));
    EXPECT_EQ(" ", decoded);

    EXPECT_TRUE(url_decode("%2F", &decoded));
    EXPECT_EQ("/", decoded);

    EXPECT_TRUE(url_decode("%2f", &decoded));
    EXPECT_EQ("/", decoded);

    EXPECT_TRUE(url_decode("%E4%B8%AD", &decoded));
    EXPECT_EQ("\xE4\xB8\xAD", decoded);
}

TEST(UrlCodingTest, ValidPercentEncodingUppercaseLowercase) {
    std::string decoded;
    EXPECT_TRUE(url_decode("%AB", &decoded));
    EXPECT_EQ("\xAB", decoded);

    EXPECT_TRUE(url_decode("%ab", &decoded));
    EXPECT_EQ("\xAB", decoded);

    EXPECT_TRUE(url_decode("%Ab", &decoded));
    EXPECT_EQ("\xAB", decoded);

    EXPECT_TRUE(url_decode("%aB", &decoded));
    EXPECT_EQ("\xAB", decoded);

    EXPECT_TRUE(url_decode("%00", &decoded));
    EXPECT_EQ(std::string(1, '\0'), decoded);

    EXPECT_TRUE(url_decode("%FF", &decoded));
    EXPECT_EQ("\xFF", decoded);

    EXPECT_TRUE(url_decode("%ff", &decoded));
    EXPECT_EQ("\xFF", decoded);
}

TEST(UrlCodingTest, ValidConsecutivePercentBytes) {
    std::string decoded;
    EXPECT_TRUE(url_decode("%E4%B8%AD%E6%96%87", &decoded));
    EXPECT_EQ("\xE4\xB8\xAD\xE6\x96\x87", decoded);
}

TEST(UrlCodingTest, InvalidPercentTruncated) {
    std::string decoded;
    EXPECT_FALSE(url_decode("%", &decoded));
    EXPECT_FALSE(url_decode("%1", &decoded));
}

TEST(UrlCodingTest, InvalidPercentNonHex) {
    std::string decoded;
    EXPECT_FALSE(url_decode("%1G", &decoded));
    EXPECT_FALSE(url_decode("%G1", &decoded));
    EXPECT_FALSE(url_decode("%XX", &decoded));
    EXPECT_FALSE(url_decode("%GG", &decoded));
    EXPECT_FALSE(url_decode("% 0", &decoded));
    EXPECT_FALSE(url_decode("%0 ", &decoded));
}

TEST(UrlCodingTest, InvalidPercentTruncatedInMiddle) {
    std::string decoded;
    EXPECT_FALSE(url_decode("abc%", &decoded));
    EXPECT_FALSE(url_decode("abc%1", &decoded));
    EXPECT_FALSE(url_decode("abc%G1", &decoded));
}

TEST(UrlCodingTest, MixedValidAndInvalid) {
    std::string decoded;
    EXPECT_TRUE(url_decode("hello%20world", &decoded));
    EXPECT_EQ("hello world", decoded);

    EXPECT_FALSE(url_decode("hello%2Gworld", &decoded));
    EXPECT_FALSE(url_decode("%20%GG", &decoded));
    EXPECT_FALSE(url_decode("%20%1", &decoded));
}

TEST(UrlCodingTest, DecodeOutputClearedOnFailure) {
    std::string decoded = "previous";
    EXPECT_FALSE(url_decode("%1G", &decoded));
    EXPECT_TRUE(decoded.empty());

    decoded = "previous";
    EXPECT_FALSE(url_decode("%", &decoded));
    EXPECT_TRUE(decoded.empty());
}

TEST(Base64Test, Basic) {
    std::string encoded;
    base64_encode("a", &encoded);
    EXPECT_EQ("YQ==", encoded);
    std::string decoded;
    EXPECT_TRUE(base64_decode(encoded, &decoded));
    EXPECT_EQ("a", decoded);

    base64_encode("ab", &encoded);
    EXPECT_EQ("YWI=", encoded);
    EXPECT_TRUE(base64_decode(encoded, &decoded));
    EXPECT_EQ("ab", decoded);

    base64_encode("abc", &encoded);
    EXPECT_EQ("YWJj", encoded);
    EXPECT_TRUE(base64_decode(encoded, &decoded));
    EXPECT_EQ("abc", decoded);

    base64_encode("abcdef", &encoded);
    EXPECT_EQ("YWJjZGVm", encoded);
    EXPECT_TRUE(base64_decode(encoded, &decoded));
    EXPECT_EQ("abcdef", decoded);
}

TEST(HtmlEscapingTest, Basic) {
    std::string before = "<html><body>&amp";
    std::stringstream after;
    escape_for_html(before, &after);
    EXPECT_EQ("&lt;html&gt;&lt;body&gt;&amp;amp", after.str());
}

} // namespace doris
