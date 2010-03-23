/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: jasonlee
 * Date: Mar 23, 2010
 * Time: 4:31:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class LifecycleModulesTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_LIFECYCLE_MODULES = "A lifecycle module performs tasks when it is triggered by one or more events in the server's lifecycle. Possible trigger server events are: initialization, startup, ready to service requests, and shutdown. Lifecycle modules are not part of the Java specification, but are an enhancement to the Enterprise Server.";
    private static final String TRIGGER_EDIT_LIFECYCLE_MODULE = "Edit Lifecycle Module";
    private static final String TRIGGER_NEW_LIFECYCLE_MODULE = "New Lifecycle Module";

    @Test
    public void testLifecycleModules() {
        final String lifecycleName = "TestLifecycle"+generateRandomString();
        final String lifecycleClassname = "org.foo.nonexistent.Lifecyclemodule";
        final String property = "property";
        final String value = "value";
        final String description = "description";

        clickAndWait("treeForm:tree:lifecycle:lifecycle_link", TRIGGER_LIFECYCLE_MODULES);
        clickAndWait("propertyForm:deployTable:topActionsGroup1:deployButton", TRIGGER_NEW_LIFECYCLE_MODULE);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:IdTextProp:IdText", lifecycleName);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:classNameProp:classname", lifecycleClassname);

        int count = addTableRow("propertyForm:basicTable", "propertyForm:basicTable:topActionsGroup1:addSharedTableButton");

        selenium.type("propertyForm:basicTable:rowGroup1:0:col2:col1St", property);
        selenium.type("propertyForm:basicTable:rowGroup1:0:col3:col1St", value);
        selenium.type("propertyForm:basicTable:rowGroup1:0:col4:col1St", description);
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_LIFECYCLE_MODULES);
        assertTrue(selenium.isTextPresent(lifecycleName));

        testDisableButton(lifecycleName, "propertyForm:deployTable", "propertyForm:deployTable:topActionsGroup1:button3",
                "propertyForm:propertySheet:propertSectionTextField:statusProp:status",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_LIFECYCLE_MODULES,
                TRIGGER_EDIT_LIFECYCLE_MODULE);

        testEnableButton(lifecycleName, "propertyForm:deployTable", "propertyForm:deployTable:topActionsGroup1:button2",
                "propertyForm:propertySheet:propertSectionTextField:statusProp:status",
                "propertyForm:propertyContentPage:topButtons:cancelButton",
                TRIGGER_LIFECYCLE_MODULES,
                TRIGGER_EDIT_LIFECYCLE_MODULE);

        deleteRow("propertyForm:deployTable:topActionsGroup1:button1", "propertyForm:deployTable", lifecycleName);
    }
}
