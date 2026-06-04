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
#include <string>
#include <string_view>

namespace doris {
namespace {

void test_url_round_trip(const std::string& input, const std::string_view* expected_encoded) {
    std::string encoded;
    url_encode(input, &encoded);

    if (expected_encoded != nullptr) {
        EXPECT_EQ(*expected_encoded, encoded);
    }

    std::string decoded;
    EXPECT_TRUE(url_decode(encoded, &decoded));
    EXPECT_EQ(input, decoded);
}

void test_base64_round_trip(const std::string& input, const std::string_view* expected_encoded) {
    std::string encoded;
    base64_encode(input, &encoded);

    if (expected_encoded != nullptr) {
        EXPECT_EQ(*expected_encoded, encoded);
    }

    std::string decoded;
    EXPECT_TRUE(base64_decode(encoded, &decoded));
    EXPECT_EQ(input, decoded);

    std::string encoded_from_bytes(encoded.size(), '\0');
    size_t encoded_len = base64_encode(reinterpret_cast<const unsigned char*>(input.data()),
                                       input.size(),
                                       reinterpret_cast<unsigned char*>(encoded_from_bytes.data()));
    encoded_from_bytes.resize(encoded_len);
    EXPECT_EQ(encoded, encoded_from_bytes);

    std::string decoded_from_bytes(input.size(), '\0');
    int64_t decoded_len =
            base64_decode(encoded.data(), encoded.size(), decoded_from_bytes.data());
    ASSERT_GE(decoded_len, 0);
    decoded_from_bytes.resize(decoded_len);
    EXPECT_EQ(input, decoded_from_bytes);
}

} // namespace

TEST(UrlCodingTest, Basic) {
    std::string input = "ABCDEFGHIJKLMNOPQRSTUWXYZ1234567890~!@#$%^&*()<>?,./:\";'{}|[]\\_+-=";
    test_url_round_trip(input, nullptr);
}

TEST(UrlCodingTest, BlankString) {
    test_url_round_trip("", nullptr);
}

TEST(UrlCodingTest, PathSeparators) {
    const std::string_view expected = "%2Fhome%2Fdoris%2Fdirectory%2F";
    test_url_round_trip("/home/doris/directory/", &expected);
}

TEST(UrlCodingTest, SpaceAndPlusEncoding) {
    const std::string_view expected = "+%2B";
    test_url_round_trip(" +", &expected);
}

TEST(UrlCodingTest, DecodeInvalidInput) {
    std::string decoded;
    EXPECT_FALSE(url_decode("%", &decoded));
    EXPECT_FALSE(url_decode("%2", &decoded));
    EXPECT_FALSE(url_decode("%GG", &decoded));
}

TEST(Base64Test, Basic) {
    const std::string_view encoded_a = "YQ==";
    const std::string_view encoded_ab = "YWI=";
    const std::string_view encoded_abc = "YWJj";
    const std::string_view encoded_abcd = "YWJjZA==";
    const std::string_view encoded_abcde = "YWJjZGU=";
    const std::string_view encoded_abcdef = "YWJjZGVm";

    test_base64_round_trip("a", &encoded_a);
    test_base64_round_trip("ab", &encoded_ab);
    test_base64_round_trip("abc", &encoded_abc);
    test_base64_round_trip("abcd", &encoded_abcd);
    test_base64_round_trip("abcde", &encoded_abcde);
    test_base64_round_trip("abcdef", &encoded_abcdef);
}

TEST(Base64Test, DecodeInvalidInput) {
    std::string decoded;
    EXPECT_FALSE(base64_decode("YWJj!", &decoded));
    EXPECT_FALSE(base64_decode("a", &decoded));

    char raw_decoded[8];
    EXPECT_LT(base64_decode("YWJj!", 5, raw_decoded), 0);
    EXPECT_LT(base64_decode("a", 1, raw_decoded), 0);
}

TEST(HtmlEscapingTest, Basic) {
    std::string before = "<html><body>&amp";
    std::stringstream after;
    escape_for_html(before, &after);
    EXPECT_EQ(after.str(), "&lt;html&gt;&lt;body&gt;&amp;amp");
    EXPECT_EQ(escape_for_html_to_string(before), "&lt;html&gt;&lt;body&gt;&amp;amp");
}

} // namespace doris
