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

dojo.provide("jetspeed.widget.PortalAccordionContainer");
dojo.provide("jetspeed.widget.PortalMenuOptionLink");

dojo.require("jetspeed.desktop.core");
dojo.require("dojo.widget.*");
dojo.require("dojo.widget.AccordionContainer");

jetspeed.widget.PortalAccordionContainer = function()
{    
    this.widgetType = "PortalAccordionContainer";
    this.isContainer = true;
    //this.templateString = '<div id="navcolumn"><table cellpadding="0" cellspacing="4" border="0" width="100%"><tr><td><div dojoAttachPoint="containerNode" class="toolgroup"></div></td></tr></table></div>';
    this.templateString = '<div dojoAttachPoint="containerNode" class="toolgroup"></div>';
    dojo.widget.HtmlWidget.call(this);
};

dojo.inherits(jetspeed.widget.PortalAccordionContainer, dojo.widget.HtmlWidget);

dojo.lang.extend( jetspeed.widget.PortalAccordionContainer,
{
    // dojo.widget.Widget create protocol
    postMixInProperties: function( args, fragment, parentComp )
    {
        this.templateCssPath = new dojo.uri.Uri( jetspeed.prefs.getLayoutRootUrl() + "/css/PortalAccordionContainer.css" ) ;
        jetspeed.widget.PortalAccordionContainer.superclass.postMixInProperties.call( this, args, fragment, parentComp );
    },
    createAndAddPane: function( /* jetspeed.om.MenuOption */ labelMenuOption, accordionPaneProps )
    {
        if ( ! accordionPaneProps )
            accordionPaneProps = {};
        if ( labelMenuOption )
        {
            accordionPaneProps.label = labelMenuOption.getText();
            if ( labelMenuOption.getHidden() )
                accordionPaneProps.open = false;
            else
                accordionPaneProps.open = true;
            
            accordionPaneProps.labelNodeClass = "label";
            accordionPaneProps.containerNodeClass = "FolderList";
            accordionPaneProps.templatePath = new dojo.uri.Uri( jetspeed.url.basePortalDesktopUrl() + "/javascript/jetspeed/widget/TitlePane.html" ) ;
            accordionPaneProps.allowCollapse = false;
        }
    
        var accordionPaneWidget = dojo.widget.createWidget( "AccordionPane", accordionPaneProps );
        this.addChild( accordionPaneWidget );
        return accordionPaneWidget;
    },
    addLinksToPane: function( accordionPaneWidget, /* Array */ menuOptions )
    {
        if ( ! menuOptions || ! accordionPaneWidget ) return;

        var linkWidget;
        for ( var i = 0; i < menuOptions.length; i++ )
        {
            linkWidget = dojo.widget.createWidget( "jetspeed:PortalMenuOptionLink", { menuOption: menuOptions[i] } );
            accordionPaneWidget.addChild( linkWidget );
        }
    },
    createJetspeedMenu: function( /* jetspeed.om.Menu */ menuObj )
    {
        if ( ! menuObj ) return;
        if ( this.children && this.children.length > 0 )
        {
            for ( var i = (this.children.length -1) ; i >= 0 ; i-- )
            {
                this.children[i].destroy();
            }
        }
        var menuOpts = menuObj.getOptions();
        var currentLinkGroup = [], currentLinkGroupOpt = null, menuOption = null, menuOptIndex = 0;
        while ( currentLinkGroup != null )
        {
            menuOption = null;
            if ( menuOptIndex < menuOpts.length )
            {   // another one
                menuOption = menuOpts[menuOptIndex];
                
                menuOptIndex++;
            }
            if ( menuOption == null || menuOption.isSeparator() )
            {
                if ( currentLinkGroup != null && currentLinkGroup.length > 0 )
                {   // add pane
                    var accordionPaneWidget = this.createAndAddPane( currentLinkGroupOpt );
                    this.addLinksToPane( accordionPaneWidget, currentLinkGroup );
                }
                currentLinkGroupOpt = null;
                currentLinkGroup = null;
                if ( menuOption != null )
                {
                    currentLinkGroupOpt = menuOption;
                    currentLinkGroup = [];
                }
            }
            else if ( menuOption.isLeaf() && menuOption.getUrl() )
            {
                currentLinkGroup.push( menuOption );
            }
        }        
    },
    selectChild: function(/*Widget*/ page)
    {
    }
});

jetspeed.widget.PortalMenuOptionLink = function()
{    
	dojo.widget.HtmlWidget.call(this);
    
    this.widgetType = "PortalMenuOptionLink";
    this.templateString = '<div dojoAttachPoint="containerNode"><a href="" dojoAttachPoint="menuOptionLinkNode" dojoAttachEvent="onClick" class="Link"></a></div>';
};
dojo.inherits(jetspeed.widget.PortalMenuOptionLink, dojo.widget.HtmlWidget);

dojo.lang.extend(jetspeed.widget.PortalMenuOptionLink, {
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
