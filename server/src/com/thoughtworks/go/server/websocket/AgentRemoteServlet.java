/*
 * Copyright 2015 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thoughtworks.go.server.websocket;

import org.eclipse.jetty.websocket.api.extensions.ExtensionConfig;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;

public class AgentRemoteServlet extends WebSocketServlet {

    private WebApplicationContext wac;

    @Override
    public void init() throws ServletException {
        this.wac = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        super.init();
    }

    @Override
    public void configure(WebSocketServletFactory factory) {
        AgentRemoteSocketCreator bean = wac.getBean(AgentRemoteSocketCreator.class);
        bean.addExtensionConfig(ExtensionConfig.parse("fragment;maxLength=" + factory.getPolicy().getMaxBinaryMessageBufferSize()));
        factory.setCreator(bean);
    }
}
