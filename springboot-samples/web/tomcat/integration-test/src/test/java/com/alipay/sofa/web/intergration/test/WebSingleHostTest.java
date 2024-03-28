/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.web.intergration.test;

import com.alipay.sofa.ark.container.service.classloader.BizClassLoader;
import com.alipay.sofa.koupleless.common.BizRuntimeContextRegistry;
import com.alipay.sofa.koupleless.test.suite.biz.SOFAArkTestBootstrap;
import com.alipay.sofa.koupleless.test.suite.spring.model.KouplelessBaseSpringTestConfig;
import com.alipay.sofa.koupleless.test.suite.spring.model.KouplelessBizSpringTestConfig;
import com.alipay.sofa.koupleless.test.suite.spring.model.KouplelessMultiSpringTestConfig;
import com.alipay.sofa.koupleless.test.suite.spring.multi.KouplelessTestMultiSpringApplication;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author CodeNoobKing
 * @date 2024/3/11
 */
public class WebSingleHostTest {

    private static KouplelessTestMultiSpringApplication multiApp;

    private static final OkHttpClient                   client = new OkHttpClient();

    @SneakyThrows
    @BeforeClass
    public static void setUpMultiApplication() {
        multiApp = new KouplelessTestMultiSpringApplication(KouplelessMultiSpringTestConfig
            .builder()
            .baseConfig(
                KouplelessBaseSpringTestConfig.builder().packageName("com.alipay.sofa.web.base")
                    .mainClass("com.alipay.sofa.web.base.BaseApplication")
                    .artifactId("base-web-single-host").build())
            .bizConfigs(
                Lists.newArrayList(
                    KouplelessBizSpringTestConfig.builder().packageName("com.alipay.sofa.web.biz1")
                        .bizName("biz1").mainClass("com.alipay.sofa.web.biz1.Biz1Application")
                        .artifactId("biz1-web-single-host").build(),
                    KouplelessBizSpringTestConfig.builder().packageName("com.alipay.sofa.web.biz2")
                        .bizName("biz2").mainClass("com.alipay.sofa.web.biz2.Biz2Application")
                        .artifactId("biz2-web-single-host").build())).build());
        multiApp.run();
        Thread.sleep(1000);
    }

    @Test
    public void testContextWebhookPathPrefixIsAdded() throws Throwable {
        Response resp = client.newCall(
            new Request.Builder().url("http://localhost:8080/").get().build()).execute();
        Assert.assertEquals("hello to base deploy", resp.body().string());

        resp = client.newCall(
            new Request.Builder().url("http://localhost:8080/biz1/").get().build()).execute();
        Assert.assertEquals("hello to biz1 deploy", resp.body().string());

        resp = client.newCall(
            new Request.Builder().url("http://localhost:8080/biz2/").get().build()).execute();
        Assert.assertEquals("hello to biz2 deploy", resp.body().string());

    }
}
