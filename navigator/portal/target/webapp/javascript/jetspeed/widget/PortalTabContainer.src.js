/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * author: Steve Milek
 */

dojo.provide("jetspeed.widget.PortalTabContainer");

dojo.require("jetspeed.desktop.core");
dojo.require("dojo.widget.*");
dojo.require("dojo.widget.TabContainer");

jetspeed.widget.PortalTabContainer = function()
{    
    this.widgetType = "PortalTabContainer";
    this.js_addingTab = false;
    this.doLayout = false;    // to keep base class from conducting certain layout behavior (not sure if needed?)
    this.selectedChildWidget = true;   // to keep base class code from setting first tab as selected
    this.tabsadded = 0;
    dojo.widget.TabContainer.call( this );
};

dojo.inherits(jetspeed.widget.PortalTabContainer, dojo.widget.TabContainer);

dojo.lang.extend( jetspeed.widget.PortalTabContainer,
{
    // dojo.widget.Widget create protocol
    postMixInProperties: function( args, fragment, parentComp )
    {
        this.templateCssPath = new dojo.uri.Uri( jetspeed.prefs.getLayoutRootUrl() + "/css/PortalTabContainer.css" ) ;
        jetspeed.widget.PortalTabContainer.superclass.postMixInProperties.call( this, args, fragment, parentComp );
    },
    // dojo.widget.Widget create protocol
    postCreate: function( args, fragment, parentComp )
    {
        jetspeed.widget.PortalTabContainer.superclass.postCreate.call( this, args, fragment, parentComp );
    },
    addTab: function( /* jetspeed.om.MenuOption */ menuOpt )
    {
        if ( ! menuOpt ) return;
        this.js_addingTab = true;
        var tabDomNode = document.createElement( "div" );
        var tab = new dojo.widget.HtmlWidget();   // create a fake widget so that widget.addedTo doesn't bomb when we call this.addChild() below
        tab.domNode = tabDomNode;
        tab.menuOption = menuOpt;
        tab.label = menuOpt.getShortTitle();
        tab.closable = false;
        tab.widgetId = this.widgetId + "-tab-" + this.tabsadded;   // to make toString of these widgets a useful hash key (this.tablist.pane2button)
        this.tabsadded++;
        this.addChild( tab );
        //dojo.debug( "PortalTabContainer.addTab" );
        if ( jetspeed.page.equalsPageUrl( menuOpt.getUrl() ) )
        {
            this.selectChild( tab );
        }
        this.js_addingTab = false;
    },
    _setupChild: function(page){
		// Summary: Add the given child to this page container

		//page.hide();

		// publish the addChild event for panes added via addChild(), and the original panes too
		dojo.event.topic.publish(this.widgetId+"-addChild", page);
	},
    selectChild: function( tab, _noRefresh )
    {
        //jetspeed.widget.PortalTabContainer.superclass.selectTab.call( this, tab );
        
        if(this.tablist._currentChild){
            var oldButton=this.tablist.pane2button[this.tablist._currentChild];
            oldButton.clearSelected();
        }
        var newButton=this.tablist.pane2button[tab];
        newButton.setSelected();
        this.tablist._currentChild=tab;

        if ( ! this.js_addingTab && ! _noRefresh )
        {
            tab.menuOption.navigateTo();
        }
	},

    _showChild: function(page) {
		// size the current page (in case this is the first time it's being shown, or I have been resized)
		//if(this.doLayout){
		//	var content = dojo.html.getContentBox(this.containerNode);
		//	page.resizeTo(content.width, content.height);
		//}

		page.selected=true;
		//page.show();
	},

	_hideChild: function(page) {
		page.selected=false;
		//page.hide();
	},

    createJetspeedMenu: function( /* jetspeed.om.Menu */ menuObj )
    {
        if ( ! menuObj ) return;
        if ( this.tabsadded > 0 && this.children && this.children.length > 0 )
        {
            for ( var i = (this.children.length -1) ; i >= 0 ; i-- )
            {
                this.removeChild( this.children[i] );
            }
            this.tabsadded = 0;
        }

        var menuOpts = menuObj.getOptions();
        for ( var i = 0 ; i < menuOpts.length ; i++ )
        {
            var menuOption = menuOpts[i];
            if ( menuOption.isLeaf() && menuOption.getUrl() && ! menuOption.isSeparator() )
            {
                this.addTab( menuOption );
            }
        }
    },
    onKey: function(e){}
});

