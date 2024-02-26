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
package com.alipay.sofa.koupleless.ext.web.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@EnableConfigurationProperties(GatewayProperties.class)
@ComponentScan(basePackages = "com.alipay.sofa.koupleless.ext.web")
public class ForwardAutoConfiguration {
    private static final String EMPTY               = "";
    private static final String CONTEXT_PATH_PREFIX = "/";

    private static final String PATH_PREFIX         = "/";
    @Autowired
    private GatewayProperties   gatewayProperties;

    @Bean
    public Forwards forwards() {
        //load conf
        List<Forward> forwards = gatewayProperties.getForwards();
        List<ForwardItem> items = toForwardItems(forwards);
        items.sort(ForwardItemComparator.getInstance());
        //make forwards bean and set conf
        Forwards bean = new Forwards();
        bean.setItems(items);
        return bean;
    }

    private List<ForwardItem> toForwardItems(List<Forward> forwards) {
        if (CollectionUtils.isEmpty(forwards)) {
            return Collections.emptyList();
        }
        List<ForwardItem> items = new LinkedList<>();
        for (Forward forward : forwards) {
            items.addAll(toForwardItems(forward, items.size()));
        }
        return items;
    }

    private List<ForwardItem> toForwardItems(Forward forward, int startIndex) {
        Set<String> hosts = forward.getHosts();
        if (CollectionUtils.isEmpty(hosts)) {
            hosts = Collections.singleton(EMPTY);
        }
        String contextPath = forward.getContextPath();
        if (!contextPath.startsWith(CONTEXT_PATH_PREFIX)) {
            contextPath = CONTEXT_PATH_PREFIX + contextPath;
        }
        Set<ForwardPath> paths = forward.getPaths();
        if (CollectionUtils.isEmpty(paths)) {
            ForwardPath path = new ForwardPath();
            path.setFrom(PATH_PREFIX);
            path.setTo(PATH_PREFIX);
            paths = Collections.singleton(path);
        } else {
            for (ForwardPath path : paths) {
                String from = path.getFrom();
                if (!from.startsWith(CONTEXT_PATH_PREFIX)) {
                    path.setFrom(CONTEXT_PATH_PREFIX + from);
                }
                String to = path.getTo();
                if (to == null) {
                    path.setTo(path.getFrom());
                    continue;
                }
                if (!to.startsWith(CONTEXT_PATH_PREFIX)) {
                    path.setTo(CONTEXT_PATH_PREFIX + to);
                }
            }
        }
        List<ForwardItem> items = new LinkedList<>();
        for (String host : hosts) {
            for (ForwardPath path : paths) {
                ForwardItem item = new ForwardItem(contextPath, host, path.getFrom(), path.getTo());
                items.add(item);
            }
        }
        return items;
    }
}
