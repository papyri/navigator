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
dojo.provide("jetspeed.manifest");
dojo.require("dojo.string.extras");
dojo.require("dojo.ns");

(function(){
    //mapping of all widget short names to their full package names
    // This is used for widget autoloading - no dojo.require() is necessary.
    // If you use a widget in markup or create one dynamically, then this
    // mapping is used to find and load any dependencies not already loaded.
    // You should use your own namespace for any custom widgets.
    // For extra widgets you use, dojo.declare() may be used to explicitly load them.
    var map = {
        html: {
            "editortable": "jetspeed.widget.EditorTable",
            "multirowheadertable": "jetspeed.widget.MultiRowHeaderTable",
            "portalaccordioncontainer": "jetspeed.widget.PortalAccordionContainer",
            "portalaccordionpane": "jetspeed.widget.PortalAccordionPane",
            "portalmenuoptionlink": "jetspeed.widget.PortalMenuOptionLink",
            "portaltabcontainer": "jetspeed.widget.PortalTabContainer",
            "portalbreadcrumbcontainer": "jetspeed.widget.PortalBreadcrumbContainer",
            "portalbreadcrumblink": "jetspeed.widget.PortalBreadcrumbLink",
            "portalbreadcrumblinkseparator": "jetspeed.widget.PortalBreadcrumbLinkSeparator",
            "portaltooltipmanager": "jetspeed.widget.PortalTooltipManager",
            "portletwindow": "jetspeed.widget.PortletWindow",
            "portletwindowresizehandle": "jetspeed.widget.PortletWindowResizeHandle",
            "portletdefcontaineritem": "jetspeed.widget.PortletDefContainerItem",
            "pageeditor": "jetspeed.widget.PageEditor",
            "pageeditpane": "jetspeed.widget.PageEditPane",
            "layouteditpane": "jetspeed.widget.LayoutEditPane",
            "layouteditpanemovebutton": "jetspeed.widget.LayoutEditPaneMoveHandle",
            "sitemanagertreerpccontroller": "jetspeed.widget.SiteManagerTreeRPCController"
        }
    };

    function jetspeedNamespaceResolver(name, domain){
        if(!domain){ domain="html"; }
        if(!map[domain]){ return null; }
        return map[domain][name];    
    }

    dojo.registerNamespaceResolver("jetspeed", jetspeedNamespaceResolver);
})();




// This is a full custom namespace example
// By convention, myns lives in <dojo root>/../myns/, 
// and myns widgets are in myns.widget
// Convention paths are autodiscovered, and all we would 
// have to do here is register a resolver with 
// dojo.registerNamespaceResolver("myns", <resolver>);
/*
dojo.registerNamespaceManifest("jetspeed", "desktop", "jetspeed", "myns.widget",
    function(name){
        var module = "myns.widget."+dojo.string.capitalize(name);
        dojo.debug("resolver returning '"+module+"' for '"+name+"'"); 
        return module;
    }
);

*/
