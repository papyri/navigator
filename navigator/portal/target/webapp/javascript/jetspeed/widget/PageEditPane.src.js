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

dojo.provide("jetspeed.widget.PageEditPane");

dojo.require("dojo.widget.*");
dojo.require("dojo.io.*");
dojo.require("dojo.event.*");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.widget.Dialog");
dojo.require("dojo.widget.Select");
dojo.require("dojo.widget.Button");

dojo.require("dojo.html.common");
dojo.require("dojo.html.display");

jetspeed.widget.PageEditPane = function()
{
}

dojo.widget.defineWidget(
	"jetspeed.widget.PageEditPane",
	dojo.widget.HtmlWidget,
	{
        // template parameters
        pageEditContainer: null,
        pageEditLDContainer: null,
        pageEditPDContainer: null,

        deletePageDialog: null,
		deletePageDialogBg: null,
		deletePageDialogFg: null,

        createPageDialog: null,
		createPageDialogBg: null,
		createPageDialogFg: null,

        layoutDecoratorSelect: null,
        portletDecoratorSelect: null,
        
        // fields
		isContainer: true,
        widgetsInTemplate: true,
        layoutDecoratorDefinitions: null,
        portletDecoratorDefinitions: null,


        // protocol - dojo.widget.Widget create

        postMixInProperties: function( args, fragment, parent )
        {
            jetspeed.widget.PageEditPane.superclass.postMixInProperties.apply( this, arguments );

            this.templateCssPath = new dojo.uri.Uri( jetspeed.url.basePortalDesktopUrl() + "/javascript/jetspeed/widget/PageEditPane.css" ) ;
            this.templatePath = new dojo.uri.Uri( jetspeed.url.basePortalDesktopUrl() + "/javascript/jetspeed/widget/PageEditPane.html" ) ;
        },

        fillInTemplate: function( args, fragment )
        {
            var self = this;
            this.deletePageDialog = dojo.widget.createWidget( "dialog", { widgetsInTemplate: true, deletePageConfirmed: function() { this.hide(); self.deletePageConfirmed(); } }, this.deletePageDialog );
			    this.deletePageDialog.setCloseControl( this.deletePageDialog.deletePageCancel.domNode );

            var createPageParams = {};
            createPageParams.widgetsInTemplate = true;
            createPageParams.createPageConfirmed = function()
            {
                var pageName = this.createPageNameTextbox.textbox.value;
                var pageTitle = this.createPageTitleTextbox.textbox.value;
                var pageShortTitle = this.createPageShortTitleTextbox.textbox.value;
                this.hide();
                self.createPageConfirmed( pageName, pageTitle, pageShortTitle );
            };
            this.createPageDialog = dojo.widget.createWidget( "dialog", createPageParams, this.createPageDialog );
			    this.createPageDialog.setCloseControl( this.createPageDialog.createPageCancel.domNode );
            
            jetspeed.widget.PageEditPane.superclass.fillInTemplate.call( this );
		},
        destroy: function()
        {
            if ( this.deletePageDialog != null )
                this.deletePageDialog.destroy();
            if ( this.createPageDialog != null )
                this.createPageDialog.destroy();
            jetspeed.widget.PageEditPane.superclass.destroy.apply( this, arguments );
        },

        postCreate: function( args, fragment, parent )
        {
            var jsObj = jetspeed;
            var djH = dojo.html;

            jsObj.widget.PageEditPane.superclass.postCreate.apply( this, arguments );
            
            var pageEditorProto = jsObj.widget.PageEditor.prototype;
            if ( this.pageEditContainer != null )
                djH.addClass( this.pageEditContainer, pageEditorProto.styleBaseAdd );
            if ( this.pageEditLDContainer != null )
                djH.addClass( this.pageEditLDContainer, pageEditorProto.styleDetailAdd );
            if ( this.pageEditPDContainer != null )
                djH.addClass( this.pageEditPDContainer, pageEditorProto.styleDetailAdd );

            if ( this.layoutDecoratorSelect != null )
            {
                if ( ! pageEditorProto.checkPerm( pageEditorProto.PM_PG_L_D, jsObj ) )
                {
                    if ( this.pageEditLDContainer )
                        this.pageEditLDContainer.style.display = "none";
                    else
                        this.layoutDecoratorSelect.domNode.style.display = "none";
                }
                else
                {
                    var currentLayoutDecorator = jsObj.page.layoutDecorator;
                    var layoutDecoratorData = [];
                    if ( this.layoutDecoratorDefinitions )
                    {
                        for ( var i = 0 ; i < this.layoutDecoratorDefinitions.length ; i++ )
                        {
                            var layoutDecoratorDef = this.layoutDecoratorDefinitions[i];
                            if ( layoutDecoratorDef && layoutDecoratorDef.length == 2 )
                            {
                                layoutDecoratorData.push( [layoutDecoratorDef[0], layoutDecoratorDef[1]] );
                                if ( currentLayoutDecorator == layoutDecoratorDef[1] )
                                {
                                    this.layoutDecoratorSelect.setAllValues( layoutDecoratorDef[0], layoutDecoratorDef[1] );
                                }
        					}
        				}
                    }
                    this.layoutDecoratorSelect.dataProvider.setData( layoutDecoratorData );
                }
            }

            if ( this.portletDecoratorSelect != null )
            {
                if ( ! pageEditorProto.checkPerm( pageEditorProto.PM_PG_P_D, jsObj ) )
                {
                    if ( this.pageEditPDContainer )
                        this.pageEditPDContainer.style.display = "none";
                    else
                        this.portletDecoratorSelect.domNode.style.display = "none";
                }
                else
                {
                    var currentPortletDecorator = jsObj.page.portletDecorator;
                    var portletDecoratorData = [];
                    if ( this.portletDecoratorDefinitions )
                    {
                        for ( var i = 0 ; i < this.portletDecoratorDefinitions.length ; i++ )
                        {
                            var portletDecoratorDef = this.portletDecoratorDefinitions[i];
                            if ( portletDecoratorDef && portletDecoratorDef.length == 2 )
                            {
                                portletDecoratorData.push( [portletDecoratorDef[0], portletDecoratorDef[1]] );
                                if ( currentPortletDecorator == portletDecoratorDef[1] )
                                {
                                    this.portletDecoratorSelect.setAllValues( portletDecoratorDef[0], portletDecoratorDef[1] );
                                }
        					}
        				}
                    }
                    this.portletDecoratorSelect.dataProvider.setData( portletDecoratorData );
                }
            }

            var addPgPerm = pageEditorProto.checkPerm( pageEditorProto.PM_PG_AD, jsObj );
            if ( ! addPgPerm )
            {
                this.createPageButton.domNode.style.display = "none";
                //this.deletePageButton.domNode.style.display = "none";
            }
        },


        // methods

        deletePage: function()
        {
            this.pageEditorWidget._openDialog( this.deletePageDialog );
        },
        deletePageConfirmed: function()
        {
            var removePageContentManager = new jetspeed.widget.RemovePageContentManager( this.pageEditorWidget );
            removePageContentManager.getContent();
        },
        createPage: function()
        {
            this.pageEditorWidget._openDialog( this.createPageDialog );
        },
        createPageConfirmed: function( pageName, pageTitle, pageShortTitle )
        {
            if ( pageName != null && pageName.length > 0 )
            {
                var pageRealPath = jetspeed.page.getPageDirectory( true ) + pageName;
                var pagePath = jetspeed.page.getPageDirectory() + pageName;
                var addPageContentManager = new jetspeed.widget.AddPageContentManager( pageRealPath, pagePath, pageName, null, pageTitle, pageShortTitle, this.pageEditorWidget );
                addPageContentManager.getContent();
            }
        },
        changeLayoutDecorator: function()
        {
            var updatePageInfoContentManager = new jetspeed.widget.UpdatePageInfoContentManager( this.layoutDecoratorSelect.getValue(), null, this.pageEditorWidget );
            updatePageInfoContentManager.getContent();
        },
        changePortletDecorator: function()
        {
            var updatePageInfoContentManager = new jetspeed.widget.UpdatePageInfoContentManager( null, this.portletDecoratorSelect.getValue(), this.pageEditorWidget );
            updatePageInfoContentManager.getContent();
        },
        editModeRedisplay: function()
        {
            this.show();
        },
        onBrowserWindowResize: function()
        {   // called after ie6 resize window
            var deletePageDialog = this.deletePageDialog;
            var createPageDialog = this.createPageDialog;
            if ( deletePageDialog && deletePageDialog.isShowing() )
            {
                deletePageDialog.domNode.style.display = "none";
                deletePageDialog.domNode.style.display = "block";
            }
            if ( createPageDialog && createPageDialog.isShowing() )
            {
                createPageDialog.domNode.style.display = "none";
                createPageDialog.domNode.style.display = "block";
            }
        }
	}
);
