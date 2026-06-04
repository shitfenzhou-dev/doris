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
#include <vector>

namespace doris {

TEST(UrlCodingTest, UrlDecodeValidPercentEncoding) {
    std::string out;

    EXPECT_TRUE(url_decode("%2F", &out));
    EXPECT_EQ("/", out);

    EXPECT_TRUE(url_decode("%2f", &out));
    EXPECT_EQ("/", out);

    EXPECT_TRUE(url_decode("%20+", &out));
    EXPECT_EQ("  ", out);

    EXPECT_TRUE(url_decode("A%42%43", &out));
    EXPECT_EQ("ABC", out);

    EXPECT_TRUE(url_decode("hello%20world%21", &out));
    EXPECT_EQ("hello world!", out);

    EXPECT_TRUE(url_decode("%E4%B8%AD", &out));
    EXPECT_EQ("中", out);

    EXPECT_TRUE(url_decode("%E4%B8%AD%E6%96%87", &out));
    EXPECT_EQ("中文", out);
}

TEST(UrlCodingTest, UrlDecodeRejectsInvalidPercentEncoding) {
    const std::vector<std::string> invalid_inputs = {
            "%", "%1", "%1G", "%G1", "%XX", "abc%2", "%2F%1G", "%2Fabc%ZZdef"};
    std::string out;

    for (const auto& input : invalid_inputs) {
        out = "previous-success";
        EXPECT_FALSE(url_decode(input, &out)) << input;
        EXPECT_TRUE(out.empty()) << input;
    }
}

TEST(UrlCodingTest, UrlDecodeEncodeRoundTrip) {
    const std::string input = "path / 中文 + space";
    std::string encoded;
    std::string decoded;

    url_encode(input, &encoded);
    EXPECT_EQ("path+%2F+%E4%B8%AD%E6%96%87+%2B+space", encoded);

    EXPECT_TRUE(url_decode(encoded, &decoded));
    EXPECT_EQ(input, decoded);
}

TEST(Base64Test, Basic) {
    std::string encoded;
    std::string decoded;

    base64_encode("a", &encoded);
    EXPECT_EQ("YQ==", encoded);
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
}

TEST(HtmlEscapingTest, Basic) {
    std::stringstream out;
    escape_for_html("<html><body>&amp", &out);
    EXPECT_EQ("&lt;html&gt;&lt;body&gt;&amp;amp", out.str());
}

} // namespace doris
