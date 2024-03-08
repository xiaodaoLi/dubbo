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
package org.apache.dubbo.rpc.cluster;

import org.apache.dubbo.common.Node;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;

import java.util.List;

/**
 * Directory. (SPI, Prototype, ThreadSafe)
 * <p>
 *     在一个服务集群中，服务提供者数量并不是一成不变的，如果集群中新增了一台机器，相应地在服务目录中就要新增一条服务提供者记录。
 *     或者，如果服务提供者的配置修改了，服务目录中的记录也要做相应的更新。如果这样说，服务目录和注册中心的功能不就雷同了吗？
 *     确实如此，这里这么说是为了方便大家理解。<br/>
 *     <b>实际上服务目录在获取注册中心的服务配置信息后，会为每条配置信息生成一个 Invoker 对象，并把这个 Invoker 对象存储起来，
 *     这个 Invoker 才是服务目录最终持有的对象。<b/><br/><br/>
 *     <b>Invoker 有什么用呢？看名字就知道了，这是一个具有远程调用功能的对象。讲到这大家应该知道了什么是服务目录了，它可以看做是 Invoker 集合，
 *     且这个集合中的元素会随注册中心的变化而进行动态调整。<b/>
 * </p>
 * <a href="http://en.wikipedia.org/wiki/Directory_service">Directory Service</a>
 * <a href="https://cn.dubbo.apache.org/zh-cn/docsv2.7/dev/source/directory/">Dubbo 官网对于服务目录的解释</a>
 *
 * @see org.apache.dubbo.rpc.cluster.Cluster#join(Directory)
 */
public interface Directory<T> extends Node {

    /**
     * get service type.
     *
     * @return service type.
     */
    Class<T> getInterface();

    /**
     * list invokers.
     * filtered by invocation
     *
     * @return invokers
     */
    List<Invoker<T>> list(Invocation invocation) throws RpcException;

    /**
     * list invokers
     * include all invokers from registry
     */
    List<Invoker<T>> getAllInvokers();

    URL getConsumerUrl();

    boolean isDestroyed();

    default boolean isEmpty() {
        return CollectionUtils.isEmpty(getAllInvokers());
    }

    default boolean isServiceDiscovery() {
        return false;
    }

    void discordAddresses();

    RouterChain<T> getRouterChain();

    /**
     * invalidate an invoker, add it into reconnect task, remove from list next time
     * will be recovered by address refresh notification or reconnect success notification
     *
     * @param invoker invoker to invalidate
     */
    void addInvalidateInvoker(Invoker<T> invoker);

    /**
     * disable an invoker, remove from list next time
     * will be removed when invoker is removed by address refresh notification
     * using in service offline notification
     *
     * @param invoker invoker to invalidate
     */
    void addDisabledInvoker(Invoker<T> invoker);

    /**
     * recover a disabled invoker
     *
     * @param invoker invoker to invalidate
     */
    void recoverDisabledInvoker(Invoker<T> invoker);

    default boolean isNotificationReceived() {
        return false;
    }
}
