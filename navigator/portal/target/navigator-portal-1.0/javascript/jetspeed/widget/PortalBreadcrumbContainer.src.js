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

dojo.provide("jetspeed.widget.PortalBreadcrumbContainer");
dojo.provide("jetspeed.widget.PortalBreadcrumbLink");
dojo.provide("jetspeed.widget.PortalBreadcrumbLinkSeparator");

dojo.require("jetspeed.desktop.core");
dojo.require("dojo.widget.*");
dojo.require("dojo.widget.TabContainer");

jetspeed.widget.PortalBreadcrumbContainer = function()
{    
    this.widgetType = "PortalBreadcrumbContainer";
    this.isContainer = true;

    //this.templateString = '<div id="breadcrumbs"><div dojoAttachPoint="containerNode" class="FolderList"></div><div id="jetspeedPageControls"></div></div>';
    //this.templateString = '<div dojoAttachPoint="containerNode" class="toolgroup"></div>';
    dojo.widget.HtmlWidget.call(this);
};

dojo.inherits(jetspeed.widget.PortalBreadcrumbContainer, dojo.widget.HtmlWidget);

dojo.lang.extend( jetspeed.widget.PortalBreadcrumbContainer,
{
    // dojo.widget.Widget create protocol
    postMixInProperties: function( args, fragment, parentComp )
    {
        this.templateCssPath = new dojo.uri.Uri( jetspeed.prefs.getLayoutRootUrl() + "/css/PortalBreadcrumbContainer.css" ) ;
        this.templatePath = new dojo.uri.dojoUri( jetspeed.prefs.getLayoutRootUrl() + "/templates/PortalBreadcrumbContainer.html");
        jetspeed.widget.PortalBreadcrumbContainer.superclass.postMixInProperties.call( this, args, fragment, parentComp );
    },

    // dojo.widget.Widget create protocol
    fillInTemplate: function( args, frag )
    {
		// Copy style info from input node to output node
		var source = this.getFragNodeRef( frag );
		dojo.html.copyStyle( this.domNode, source );
		jetspeed.widget.PortalBreadcrumbContainer.superclass.fillInTemplate.apply( this, arguments );
	},

    createJetspeedMenu: function( /* jetspeed.om.Menu */ menuObj )
    {
        if ( ! menuObj ) return;
        if ( this.containerNode.childNodes && this.containerNode.childNodes.length > 0 )
        {
            for ( var i = (this.containerNode.childNodes.length -1) ; i >= 0 ; i-- )
            {
                dojo.dom.removeNode( this.containerNode.childNodes[i] );
            }
        }
        var menuOpts = menuObj.getOptions();
        var breadcrumbLinks = [], menuOption = null;
        for ( var i = 0 ; i < menuOpts.length ; i++ )
        {
            menuOption = menuOpts[ i ];
            if ( menuOption != null && ! menuOption.isSeparator() )
            {
                breadcrumbLinks.push( menuOption );
            }
        }
        if ( breadcrumbLinks != null && breadcrumbLinks.length > 0 )
        {
            var linkWidget, linkSepWidget;
            var bcLen = breadcrumbLinks.length;
            for ( var i = 0 ; i < bcLen ; i++ )
            {
                if ( i > 0 )
                {
                    linkSepWidget = dojo.widget.createWidget( "jetspeed:PortalBreadcrumbLinkSeparator" );
                    this.containerNode.appendChild( linkSepWidget.domNode );
                }
                if ( i == (bcLen -1) )
                {
                    var currentLinkNode = document.createElement( "span" );
                    currentLinkNode.appendChild( document.createTextNode( breadcrumbLinks[i].getShortTitle() ) );
                    this.containerNode.appendChild( currentLinkNode );
                }
                else
                {
                    linkWidget = dojo.widget.createWidget( "jetspeed:PortalBreadcrumbLink", { menuOption: breadcrumbLinks[i] } );
                    this.containerNode.appendChild( linkWidget.domNode );
                }
            }
        }        
    }
});

jetspeed.widget.PortalBreadcrumbLink = function()
{    
	dojo.widget.HtmlWidget.call(this);
    
    this.widgetType = "PortalBreadcrumbLink";
    this.templateString = '<span dojoAttachPoint="containerNode"><a href="" dojoAttachPoint="menuOptionLinkNode" dojoAttachEvent="onClick" class="Link"></a></span>';
};
dojo.inherits(jetspeed.widget.PortalBreadcrumbLink, dojo.widget.HtmlWidget);

dojo.lang.extend(jetspeed.widget.PortalBreadcrumbLink, {
    fillInTemplate: function()
    {
        if ( this.menuOption.type == "page" )
            this.menuOptionLinkNode.className = "LinkPage";
        else if ( this.menuOption.type == "folder" )
            this.menuOptionLinkNode.className = "LinkFolder";
		if ( this.iconSrc )
        {
			var img = document.createElement("img");
			img.src = this.iconSrc;
            this.menuOptionLinkNode.appendChild( img );
		}
        this.menuOptionLinkNode.href = this.menuOption.navigateUrl();
		this.menuOptionLinkNode.appendChild( document.createTextNode( this.menuOption.getShortTitle() ) );
		dojo.html.disableSelection( this.domNode );
	},
    onClick: function( evt )
    {
        this.menuOption.navigateTo();
        dojo.event.browser.stopEvent( evt );
	}
});


jetspeed.widget.PortalBreadcrumbLinkSeparator = function()
{    
	dojo.widget.HtmlWidget.call(this);
    
    this.widgetType = "PortalBreadcrumbLinkSeparator";
    this.templatePath = new dojo.uri.dojoUri( jetspeed.prefs.getLayoutRootUrl() + "/templates/PortalBreadcrumbLinkSeparator.html");
};
dojo.inherits(jetspeed.widget.PortalBreadcrumbLinkSeparator, dojo.widget.HtmlWidget);
