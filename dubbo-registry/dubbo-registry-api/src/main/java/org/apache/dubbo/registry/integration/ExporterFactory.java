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
package org.apache.dubbo.registry.integration;

import org.apache.dubbo.common.logger.ErrorTypeAwareLogger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.Exporter;
import org.apache.dubbo.rpc.protocol.ProtocolSerializationWrapper;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class ExporterFactory {
    private final Map<String, ReferenceCountExporter<?>> exporters = new ConcurrentHashMap<>();
    private static final ErrorTypeAwareLogger logger = LoggerFactory.getErrorTypeAwareLogger(ExporterFactory.class);

    protected ReferenceCountExporter<?> createExporter(String providerKey, Callable<Exporter<?>> exporterProducer) {

        return exporters.computeIfAbsent(providerKey,
            key -> {
                try {
                    logger.info("hgb,ExporterFactory.createExporter");
                    //exporterProducer.call() 真正触发Exporter里面的protocol的export()方法
                    // 直接调用Callable的call方法并不会开启新的线程
                    return new ReferenceCountExporter<>(exporterProducer.call(), key, this);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
    }

    protected void remove(String key, ReferenceCountExporter<?> exporter) {
        exporters.remove(key, exporter);
    }
}
