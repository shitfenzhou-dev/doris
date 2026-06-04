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

TEST(UrlDecodeTest, ValidEncodings) {
    std::string out;

    // 简单的百分号编码
    EXPECT_TRUE(url_decode("%2F", &out));
    EXPECT_EQ(out, "/");

    // 空格编码
    EXPECT_TRUE(url_decode("a+b", &out));
    EXPECT_EQ(out, "a b");

    // 连续编码
    EXPECT_TRUE(url_decode("%20%20a%20b%20", &out));
    EXPECT_EQ(out, "  a b ");

    // 中文 UTF-8 编码
    EXPECT_TRUE(url_decode("%E4%B8%AD%E6%96%87", &out));
    EXPECT_EQ(out, "中文");

    // 大小写十六进制混合
    EXPECT_TRUE(url_decode("%2f%2F", &out));
    EXPECT_EQ(out, "//");

    // 普通字符与编码混合
    EXPECT_TRUE(url_decode("hello%20world%21", &out));
    EXPECT_EQ(out, "hello world!");
}

TEST(UrlDecodeTest, InvalidEncodings) {
    std::string out;

    // 只有一个百分号
    EXPECT_FALSE(url_decode("%", &out));

    // 百分号后只有一个字符
    EXPECT_FALSE(url_decode("%1", &out));

    // 一位合法 + 一位非法：%1G
    EXPECT_FALSE(url_decode("%1G", &out));

    // 一位非法 + 一位合法：%G1
    EXPECT_FALSE(url_decode("%G1", &out));

    // 两位都非法：%XX
    EXPECT_FALSE(url_decode("%XX", &out));

    // 部分非法的连续编码
    EXPECT_FALSE(url_decode("%2F%GG%2F", &out));

    // 带非法字符的混合
    EXPECT_FALSE(url_decode("%2Fabc%ZZdef", &out));

    // 末尾截断
    EXPECT_FALSE(url_decode("abc%2", &out));
}

TEST(UrlDecodeTest, EdgeCases) {
    std::string out;

    // 空字符串
    EXPECT_TRUE(url_decode("", &out));
    EXPECT_EQ(out, "");

    // 全是加号
    EXPECT_TRUE(url_decode("+++", &out));
    EXPECT_EQ(out, "   ");

    // 全是合法的编码
    EXPECT_TRUE(url_decode("%61%62%63", &out));
    EXPECT_EQ(out, "abc");

    // 长字符串，包含多种情况
    EXPECT_TRUE(url_decode("http%3A%2F%2Fexample.com%2Fpath%2F%3Fq%3D%E4%B8%AD%E6%96%87", &out));
    EXPECT_EQ(out, "http://example.com/path/?q=中文");
}

TEST(UrlEncodeTest, Basic) {
    std::string out;

    // 普通字符不应该编码
    url_encode("abcDEF123", &out);
    EXPECT_EQ(out, "abcDEF123");

    // 空格编码为 +
    url_encode("hello world", &out);
    EXPECT_EQ(out, "hello+world");

    // 特殊字符编码
    url_encode("/", &out);
    EXPECT_EQ(out, "%2F");

    // 编码解码往返测试
    std::string original = "测试 中文 !@#$%^&*()";
    std::string encoded;
    url_encode(original, &encoded);
    std::string decoded;
    EXPECT_TRUE(url_decode(encoded, &decoded));
    EXPECT_EQ(decoded, original);
}

} // namespace doris
