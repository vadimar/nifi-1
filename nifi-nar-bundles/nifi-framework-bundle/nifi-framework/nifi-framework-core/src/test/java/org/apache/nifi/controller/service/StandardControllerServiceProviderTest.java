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
package org.apache.nifi.controller.service;

import org.apache.nifi.bundle.Bundle;
import org.apache.nifi.components.state.StateManager;
import org.apache.nifi.components.state.StateManagerProvider;
import org.apache.nifi.controller.ControllerService;
import org.apache.nifi.controller.FlowController;
import org.apache.nifi.nar.ExtensionDiscoveringManager;
import org.apache.nifi.nar.StandardExtensionDiscoveringManager;
import org.apache.nifi.nar.SystemBundle;
import org.apache.nifi.registry.VariableRegistry;
import org.apache.nifi.registry.variable.FileBasedVariableRegistry;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.util.NiFiProperties;
import org.apache.nifi.util.SynchronousValidationTrigger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;


public class StandardControllerServiceProviderTest {

    private ControllerService proxied;
    private ControllerService implementation;
    private static VariableRegistry variableRegistry;
    private static NiFiProperties nifiProperties;
    private static ExtensionDiscoveringManager extensionManager;
    private static Bundle systemBundle;
    private static FlowController flowController;

    @BeforeClass
    public static void setupSuite() throws Exception {
        System.setProperty(NiFiProperties.PROPERTIES_FILE_PATH, StandardControllerServiceProviderTest.class.getResource("/conf/nifi.properties").getFile());
        nifiProperties = NiFiProperties.createBasicNiFiProperties(null, null);

        // load the system bundle
        systemBundle = SystemBundle.create(nifiProperties);
        extensionManager = new StandardExtensionDiscoveringManager();
        extensionManager.discoverExtensions(systemBundle, Collections.emptySet());

        variableRegistry = new FileBasedVariableRegistry(nifiProperties.getVariableRegistryPropertiesPaths());

        flowController = Mockito.mock(FlowController.class);
        Mockito.when(flowController.getExtensionManager()).thenReturn(extensionManager);
    }

    @Before
    public void setup() throws Exception {
        String id = "id";
        String clazz = "org.apache.nifi.controller.service.util.TestControllerService";
        ControllerServiceProvider provider = new StandardControllerServiceProvider(flowController, null, null, new StateManagerProvider() {
            @Override
            public StateManager getStateManager(final String componentId) {
                return Mockito.mock(StateManager.class);
            }

            @Override
            public void shutdown() {
            }

            @Override
            public void enableClusterProvider() {
            }

            @Override
            public void disableClusterProvider() {
            }

            @Override
            public void onComponentRemoved(String componentId) {
            }
        }, variableRegistry, nifiProperties, new SynchronousValidationTrigger());
        ControllerServiceNode node = provider.createControllerService(clazz, id, systemBundle.getBundleDetails().getCoordinate(), null, true);
        proxied = node.getProxiedControllerService();
        implementation = node.getControllerServiceImplementation();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCallProxiedOnPropertyModified() {
        proxied.onPropertyModified(null, "oldValue", "newValue");
    }

    @Test
    public void testCallImplementationOnPropertyModified() {
        implementation.onPropertyModified(null, "oldValue", "newValue");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCallProxiedInitialized() throws InitializationException {
        proxied.initialize(null);
    }

    @Test
    public void testCallImplementationInitialized() throws InitializationException {
        implementation.initialize(null);
    }
}
