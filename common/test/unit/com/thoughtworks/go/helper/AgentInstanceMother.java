/*
 * Copyright 2016 ThoughtWorks, Inc.
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

package com.thoughtworks.go.helper;

import com.thoughtworks.go.config.AgentConfig;
import com.thoughtworks.go.config.Resource;
import com.thoughtworks.go.config.Resources;
import com.thoughtworks.go.domain.AgentInstance;
import com.thoughtworks.go.domain.AgentRuntimeStatus;
import com.thoughtworks.go.domain.AgentStatus;
import com.thoughtworks.go.domain.EnvironmentPipelineMatcher;
import com.thoughtworks.go.remote.AgentIdentifier;
import com.thoughtworks.go.server.service.AgentBuildingInfo;
import com.thoughtworks.go.server.service.AgentRuntimeInfo;
import com.thoughtworks.go.util.ReflectionUtil;
import com.thoughtworks.go.util.SystemEnvironment;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static com.thoughtworks.go.util.SystemUtil.currentWorkingDirectory;

public class AgentInstanceMother {
    private static final Set<EnvironmentPipelineMatcher> NO_ENVIRONMENTS = new HashSet<EnvironmentPipelineMatcher>();

    public static AgentInstance virtual() {
        AgentConfig virtualAgentConfig = new AgentConfig("uuid1", "ec2", "10.18.8.10");
        AgentInstance instance = AgentInstance.create(virtualAgentConfig, true, new SystemEnvironment()
        );
        instance.update(new AgentRuntimeInfo(virtualAgentConfig.getAgentIdentifier(), AgentRuntimeStatus.Idle, currentWorkingDirectory(), "cookie", null));
        return instance;
    }

    public static AgentInstance local() {
        return AgentInstance.createFromConfig(new AgentConfig("uuid-local", "localhost", "127.0.0.1"), new SystemEnvironment()
        );
    }

    public static AgentInstance idle()  {
        return idle(new Date(), "CCeDev01");
    }

    public static AgentInstance idle(final Date lastHeardAt, final String hostname)  {
        AgentConfig idleAgentConfig = new AgentConfig("uuid2", hostname, "10.18.5.1");
        AgentRuntimeInfo agentRuntimeInfo = new AgentRuntimeInfo(idleAgentConfig.getAgentIdentifier(), AgentRuntimeStatus.Idle, currentWorkingDirectory(), "cookie", null);
        agentRuntimeInfo.setLocation("/var/lib/foo");
        agentRuntimeInfo.idle();
        agentRuntimeInfo.setUsableSpace(10*1024l);
        AgentInstance agentInstance = AgentInstance.createFromLiveAgent(agentRuntimeInfo, new SystemEnvironment());
        agentInstance.idle();
        agentInstance.update(agentRuntimeInfo);
        ReflectionUtil.setField(agentInstance, "lastHeardTime", lastHeardAt);
        return agentInstance;
    }

    public static AgentInstance building() {
        return building("buildLocator");
    }

    public static AgentInstance building(String buildLocator) {
        AgentConfig buildingAgentConfig = new AgentConfig("uuid3", "CCeDev01", "10.18.5.1", new Resources("java"));
        AgentRuntimeInfo agentRuntimeInfo = new AgentRuntimeInfo(buildingAgentConfig.getAgentIdentifier(), AgentRuntimeStatus.Idle, currentWorkingDirectory(), "cookie", null);
        agentRuntimeInfo.busy(new AgentBuildingInfo("pipeline", buildLocator));
        AgentInstance building = AgentInstance.createFromConfig(buildingAgentConfig, new SystemEnvironment());
        building.update(agentRuntimeInfo);
        return building;
    }

    public static AgentInstance pending() {
        AgentRuntimeInfo runtimeInfo = AgentRuntimeInfo.fromServer(new AgentConfig("uuid4", "CCeDev03", "10.18.5.3", new Resources(new Resource("db"),new Resource("web"))), false,
                "/var/lib", 0L, "linux");
        AgentInstance pending = AgentInstance.createFromLiveAgent(runtimeInfo, new SystemEnvironment()
        );
        pending.pending();
        pending.update(runtimeInfo);
        pending.pending();
        return pending;
    }

    public static AgentInstance pendingInstance() {
        AgentRuntimeInfo runtimeInfo = new AgentRuntimeInfo(new AgentIdentifier("CCeDev03", "10.18.5.3", "uuid4"), AgentRuntimeStatus.Idle, currentWorkingDirectory(), null, null);
        return AgentInstance.createFromLiveAgent(runtimeInfo, new SystemEnvironment());
    }

    public static AgentInstance updateUuid(AgentInstance agent,String uuid){
        agent.syncConfig(new AgentConfig(uuid, agent.getHostname(), agent.getIpAddress()));
        return agent;
    }


    public static AgentInstance updateResources(AgentInstance agentInstance, String resources) {
        agentInstance.agentConfig().setResources(new Resources(resources));
        return agentInstance;
    }

    public static AgentInstance updateUsableSpace(AgentInstance agentInstance, Long freespace) {
        AgentConfig agentConfig = agentInstance.agentConfig();
        agentInstance.update(AgentRuntimeInfo.fromServer(agentConfig, true, agentInstance.getLocation(), freespace, "linux"));
        return agentInstance;
    }


    public static AgentInstance updateOS(AgentInstance agentInstance, String operatingSystem) {
        AgentConfig agentConfig = agentInstance.agentConfig();
        AgentRuntimeInfo newRuntimeInfo = AgentRuntimeInfo.fromServer(agentConfig, true, agentInstance.getLocation(), agentInstance.getUsableSpace(), operatingSystem);
        newRuntimeInfo.setStatus(agentInstance.getStatus());
        agentInstance.update(newRuntimeInfo);
        return agentInstance;
    }

    public static AgentInstance updateIpAddress(AgentInstance agentInstance, String ip) {
        agentInstance.agentConfig().setIpAddress(ip);
        return agentInstance;
    }


    public static AgentInstance updateLocation(AgentInstance agentInstance, String location) {
        AgentConfig agentConfig = agentInstance.agentConfig();
        agentInstance.update(AgentRuntimeInfo.fromServer(agentConfig, true, location, agentInstance.getUsableSpace(), "linux"));
        return agentInstance;
    }

    public static AgentInstance updateHostname(AgentInstance agentInstance, String hostname) {
        AgentConfig original = agentInstance.agentConfig();
        agentInstance.syncConfig(new AgentConfig(original.getUuid(), hostname, original.getIpAddress(), original.getResources()));
        return agentInstance;
    }

    public static AgentInstance updateRuntimeStatus(AgentInstance agentInstance, AgentRuntimeStatus status) {
        AgentConfig agentConfig = agentInstance.agentConfig();
        AgentRuntimeInfo newRuntimeInfo = AgentRuntimeInfo.fromServer(agentConfig, true, agentInstance.getLocation(), agentInstance.getUsableSpace(), "linux");
        newRuntimeInfo.setRuntimeStatus(status, null);
        agentInstance.update(newRuntimeInfo);
        return agentInstance;
    }

    public static AgentInstance updateAgentLauncherVersion(AgentInstance agentInstance, String agentLauncherVersion) {
        AgentRuntimeInfo newRuntimeInfo = AgentRuntimeInfo.fromServer(agentInstance.agentConfig(), agentInstance.isRegistered(), agentInstance.getLocation(), agentInstance.getUsableSpace(),
                agentInstance.getOperatingSystem());
        newRuntimeInfo.setAgentLauncherVersion(agentLauncherVersion);
        agentInstance.update(newRuntimeInfo);
        return agentInstance;
    }


    public static AgentInstance disabled()  {
        return disabled("10.18.5.4");
    }

    public static AgentInstance disabled(String ip) {
        AgentInstance denied = AgentInstance.createFromConfig(new AgentConfig("uuid5", "CCeDev04", ip), new SystemEnvironment()
        );
        denied.enable();
        denied.deny();
        return denied;
    }


    public static AgentInstance cancelled() {
        return cancel(building());
    }

    public static AgentInstance cancelled(String buildLocator) {
        return cancel(building(buildLocator));
    }

    public static AgentInstance cancel(AgentInstance building) {
        building.cancel();
        return building;
    }

    public static AgentInstance missing() {
        AgentConfig agentConfig = new AgentConfig("1234", "localhost", "192.168.0.1");
        AgentInstance instance = AgentInstance.createFromConfig(agentConfig, new SystemEnvironment());
        AgentRuntimeInfo newRuntimeInfo = AgentRuntimeInfo.initialState(agentConfig);
        newRuntimeInfo.setStatus(AgentStatus.Missing);
        instance.update(newRuntimeInfo);
        return instance;
    }

    public static AgentInstance lostContact() {
        return lostContact("buildLocator");
    }

    public static AgentInstance lostContact(String buildLocator) {
        AgentConfig agentConfig = new AgentConfig("1234", "localhost", "192.168.0.1");
        AgentInstance instance = AgentInstance.createFromConfig(agentConfig, new SystemEnvironment());
        AgentRuntimeInfo newRuntimeInfo = AgentRuntimeInfo.initialState(agentConfig);
        newRuntimeInfo.setStatus(AgentStatus.LostContact);
        newRuntimeInfo.setUsableSpace(1000L);
        newRuntimeInfo.setBuildingInfo(new AgentBuildingInfo("", buildLocator));
        instance.update(newRuntimeInfo);
        return instance;
    }


    public static AgentInstance idle(String hostname) {
        return updateHostname(idle(new Date(), "CCeDev01"), hostname);
    }
}
