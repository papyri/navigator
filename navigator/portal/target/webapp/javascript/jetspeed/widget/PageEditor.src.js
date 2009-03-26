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

dojo.provide("jetspeed.widget.PageEditor");

dojo.require("dojo.widget.*");
dojo.require("dojo.io.*");
dojo.require("dojo.event.*");
dojo.require("dojo.string.extras");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.widget.Dialog");
dojo.require("dojo.widget.Select");
dojo.require("dojo.widget.Button");
dojo.require("dojo.widget.Spinner");

dojo.require("dojo.html.common");
dojo.require("dojo.html.display");
dojo.require("jetspeed.widget.PageEditPane");
dojo.require("jetspeed.widget.LayoutEditPane");

jetspeed.widget.PageEditor = function()
{
}

dojo.widget.defineWidget(
	"jetspeed.widget.PageEditor",
	dojo.widget.HtmlWidget,
	{
        // template parameters
        deletePortletDialog: null,
		deletePortletDialogBg: null,
		deletePortletDialogFg: null,

        deleteLayoutDialog: null,
		deleteLayoutDialogBg: null,
		deleteLayoutDialogFg: null,

        columnSizeDialog: null,
		columnSizeDialogBg: null,
		columnSizeDialogFg: null,

        detail: null,
        
        // fields
        editorInitiatedFromDesktop: false,

		isContainer: true,
        widgetsInTemplate: true,

        loadTimeDistribute: jetspeed.UAie,

        dbOn: djConfig.isDebug,

        // style classes
        styleBase: "pageEditorPaneContainer",
        styleBaseAdd: ( jetspeed.UAie ? "pageEditorPaneContainerIE" : "pageEditorPaneContainerNotIE" ),
        styleDetail: "pageEditorDetailContainer",
        styleDetailAdd: ( jetspeed.UAie ? "pageEditorDetailContainerIE" : "pageEditorDetailContainerNotIE" ),

        // protocol - dojo.widget.Widget create

        postMixInProperties: function( args, fragment, parent )
        {
            var jsObj = jetspeed;
            jsObj.widget.PageEditor.superclass.postMixInProperties.apply( this, arguments );
    
            this.layoutImagesRoot = jsObj.prefs.getLayoutRootUrl() + "/images/desktop/";
            this.labels = jsObj.prefs.pageEditorLabels;
            this.dialogLabels = jsObj.prefs.pageEditorDialogLabels;

            this.templateCssPath = new dojo.uri.Uri( jsObj.url.basePortalDesktopUrl() + "/javascript/jetspeed/widget/PageEditor.css" ) ;
            this.templatePath = new dojo.uri.Uri( jsObj.url.basePortalDesktopUrl() + "/javascript/jetspeed/widget/PageEditor.html" ) ;
        },

        fillInTemplate: function( args, fragment )
        {
            var jsObj = jetspeed;
            var djObj = dojo;
            var self = this;

            this.deletePortletDialog = djObj.widget.createWidget( "dialog", { widgetsInTemplate: true, deletePortletConfirmed: function() { this.hide(); self.deletePortletConfirmed( this.portletEntityId ); } }, this.deletePortletDialog );
			this.deletePortletDialog.setCloseControl( this.deletePortletDialog.deletePortletCancel.domNode );

            this.deleteLayoutDialog = djObj.widget.createWidget( "dialog", { widgetsInTemplate: true, deleteLayoutConfirmed: function() { this.hide(); self.deleteLayoutConfirmed( this.portletEntityId ); } }, this.deleteLayoutDialog );
			this.deleteLayoutDialog.setCloseControl( this.deleteLayoutDialog.deleteLayoutCancel.domNode );

            var columnSizeParams = {};
            columnSizeParams.widgetsInTemplate = true;
            columnSizeParams.columnSizeConfirmed = function()
            {
                var columnSizesSum = 0;
                var columnSizes = new Array();
                for ( var i = 0 ; i < this.columnCount; i++ )
                {
                    var spinnerWidget = this[ "spinner" + i ];
                    var colSize = new Number( spinnerWidget.getValue() );
                    columnSizes.push( colSize );
                    columnSizesSum += colSize;
                }

                if ( columnSizesSum > 100 )
                {
                    alert( "Sum of column sizes cannot exceed 100." );
                }
                else
                {
                    this.hide();
                    self.columnSizeConfirmed( this.layoutId, columnSizes );
                }
            };

            this.columnSizeDialog = djObj.widget.createWidget( "dialog", columnSizeParams, this.columnSizeDialog );
            this.columnSizeDialog.setCloseControl( this.columnSizeDialog.columnSizeCancel.domNode );

            jsObj.widget.PageEditor.superclass.fillInTemplate.call( this );
		},

        postCreate: function( args, fragment, parent )
        {
            var startInEditModeMoveExecuteHere = false;
            var startInEditModeMove = null;
            //if ( this.editModeMove )
            //    startInEditModeMove = {};
            //if ( startInEditModeMove == null && this.checkPerm( this.PM_MZ_P, jetspeed ) )
            //    startInEditModeMove = { execEditMode: true };
            if ( this.editModeMove || this.checkPerm( this.PM_MZ_P, jetspeed ) )
                startInEditModeMove = { execEditMode: true };

            this.editPageInitiate( startInEditModeMove );
        },

        // initialization
        editPageInitiate: function( startInEditModeMove )
        {
            var themesContentManager = null;
            if ( this.editorInitiatedFromDesktop )
                themesContentManager = new jetspeed.widget.EditPageGetThemesContentManager( this, false, false, true, true, true, startInEditModeMove );
            else
                themesContentManager = new jetspeed.widget.EditPageGetThemesContentManager( this, true, true, true, false, false, startInEditModeMove );
            themesContentManager.getContent();
        },
        editPageBuild: function( startInEditModeMove )
        {
            var jsObj = jetspeed;
            var jsPage = jsObj.page;
            var djObj = dojo;
            
            this.pageEditorWidgets = new Array();
            this.layoutEditPaneWidgets = new Array();
            var pageEditPaneWidget = djObj.widget.createWidget( "jetspeed:PageEditPane", { layoutDecoratorDefinitions: jsPage.themeDefinitions.pageDecorations, portletDecoratorDefinitions: jsPage.themeDefinitions.portletDecorations, layoutImagesRoot: this.layoutImagesRoot, labels: this.labels, dialogLabels: this.dialogLabels } );
            pageEditPaneWidget.pageEditorWidget = this;
            var dNodeStyle = pageEditPaneWidget.domNode.style;
            dNodeStyle.display = "none";
            dNodeStyle.visibility = "hidden";
            djObj.dom.insertAfter( pageEditPaneWidget.domNode, this.domNode );
            this.pageEditorWidgets.push( pageEditPaneWidget );
            this.pageEditPaneWidget = pageEditPaneWidget;
            
            jsObj.url.loadingIndicatorStep( jsObj );
            this._buildDepth = 0;
            this._buildRootPane( startInEditModeMove );
            
            if ( startInEditModeMove != null && startInEditModeMove.execEditMode )
            {
                djObj.lang.setTimeout( this, this.editMoveModeStart, 100 );
            }
        },
        
        _buildRootPane: function( startInEditModeMove )
        {
            var jsObj = jetspeed;
            var jsPage = jsObj.page;
            var djObj = dojo;

            var editModeMove = ( startInEditModeMove != null );
            var rootLayoutEditPaneWidget = djObj.widget.createWidget( "jetspeed:LayoutEditPane", { widgetId: "layoutEdit_root", layoutId: jsPage.rootFragmentId, isRootLayout: true, depth: 0, layoutDefinitions: jsPage.themeDefinitions.layouts, layoutImagesRoot: this.layoutImagesRoot, labels: this.labels, dialogLabels: this.dialogLabels, startInEditModeMove: editModeMove } );
            rootLayoutEditPaneWidget.pageEditorWidget = this;
            var dNodeStyle = rootLayoutEditPaneWidget.domNode.style;
            dNodeStyle.display = "none";
            dNodeStyle.visibility = "hidden";
            djObj.dom.insertAfter( rootLayoutEditPaneWidget.domNode, this.pageEditPaneWidget.domNode );
            this.pageEditorWidgets.push( rootLayoutEditPaneWidget );
            this.layoutEditPaneWidgets.push( rootLayoutEditPaneWidget );

            this._buildNextColI = 0;
            this._buildColLen = ( jsObj.prefs.windowTiling ? jsPage.columns.length : 0 );
            
            if ( ! this.loadTimeDistribute )
            {
                jsObj.url.loadingIndicatorStep( jsObj );
                this._buildNextPane();
            }
            else
            {
                djObj.lang.setTimeout( this, this._buildNextPane, 10 );
                jsObj.url.loadingIndicatorStep( jsObj );
            }
        },

        _buildNextPane: function()
        {
            var jsObj = jetspeed;
            var jsPage = jsObj.page;
            var djObj = dojo;

            var i = this._buildNextColI;
            var colLen = this._buildColLen;
            if ( i < colLen )
            {
                var col, layoutEditPaneWidget = null;
                while ( i < colLen && layoutEditPaneWidget == null )
                {
                    col = jsPage.columns[i];
                    if ( col.layoutHeader )
                    {
                        layoutEditPaneWidget = djObj.widget.createWidget( "jetspeed:LayoutEditPane", { widgetId: "layoutEdit_" + i, layoutColumn: col, layoutId: col.layoutId, depth: col.layoutDepth, layoutInfo: jsPage.layoutInfo.columnLayoutHeader, layoutDefinitions: jsPage.themeDefinitions.layouts, layoutImagesRoot: this.layoutImagesRoot, labels: this.labels, dialogLabels: this.dialogLabels } );
                        layoutEditPaneWidget.pageEditorWidget = this;
                        var dNodeStyle = layoutEditPaneWidget.domNode.style;
                        dNodeStyle.display = "none";
                        dNodeStyle.visibility = "hidden";
                        if ( col.domNode.firstChild != null )
                            col.domNode.insertBefore( layoutEditPaneWidget.domNode, col.domNode.firstChild );
                        else
                            col.domNode.appendChild( layoutEditPaneWidget.domNode );
                        layoutEditPaneWidget.initializeDrag();
                        this.pageEditorWidgets.push( layoutEditPaneWidget );
                        this.layoutEditPaneWidgets.push( layoutEditPaneWidget );
                    }
                    i++;
                }
            }

            if ( i < colLen )
            {
                this._buildNextColI = i;
                if ( ! this.loadTimeDistribute )
                {
                    jsObj.url.loadingIndicatorStep( jsObj );
                    this._buildNextPane();
                }
                else
                {
                    djObj.lang.setTimeout( this, this._buildNextPane, 10 );
                    jsObj.url.loadingIndicatorStep( jsObj );
                }
            }
            else
            {
                djObj.lang.setTimeout( this, this._buildFinished, 10 );
            }
        },

        _buildFinished: function()
        {
            var jsObj = jetspeed;
            
            if ( jsObj.UAie )   // provide background when prevent IE bleed-through problem
            {
                this.bgIframe = new jsObj.widget.BackgroundIframe( this.domNode, "ieLayoutBackgroundIFrame", dojo );
            }

            var pageEditorWidgets = this.pageEditorWidgets;
            if ( pageEditorWidgets != null )
            {
                for ( var i = 0 ; i < pageEditorWidgets.length ; i++ )
                {
                    var dNodeStyle = pageEditorWidgets[i].domNode.style;
                    dNodeStyle.display = "block";
                    dNodeStyle.visibility = "visible";
                }
            }

            this.editPageSyncPortletActions( true, jsObj );

            jsObj.url.loadingIndicatorHide();
            if ( jsObj.UAie6 )
            {
                //jsObj.url.loadingIndicatorHide();
                jsObj.page.displayAllPWins();
            }
        },

        editPageSyncPortletActions: function( showing, jsObj )
        {
            var jsPage = jsObj.page;
            var jsCss = jsObj.css;
            if ( showing )
                jsObj.ui.updateChildColInfo();

            var portlets = jsPage.getPortletArray()
            if ( portlets != null )
            {
                for ( var i = 0 ; i < portlets.length ; i++ )
                {
                    portlets[i].syncActions();
                }
            }

            var peProto = jsObj.widget.PageEditor.prototype;
            var cP_D = this.checkPerm(this.PM_P_D,jsObj,peProto);
            var cL_NA_ED = this.canL_NA_ED(jsObj,peProto);

            var pWins = jsPage.portlet_windows;
            for ( var windowId in pWins )
            {
                var pWin = pWins[ windowId ];
                if ( ! pWin ) continue;
                if ( showing )
                    pWin.editPageInitiate( cP_D, cL_NA_ED, jsObj, jsCss );
                else
                    pWin.editPageTerminate( jsObj, jsCss );
            }
        },

        editPageHide: function()
        {
            var pageEditorWidgets = this.pageEditorWidgets;
            if ( pageEditorWidgets != null )
            {
                for ( var i = 0 ; i < pageEditorWidgets.length ; i++ )
                {
                    pageEditorWidgets[i].hide();
                }
            }

            this.hide();

            this.editPageSyncPortletActions( false, jetspeed );            
        },
        editPageShow: function()
        {
            var jsObj = jetspeed;
            var pageEditorWidgets = this.pageEditorWidgets;
            var moveModeIsEnabled = this.editModeMove;
            if ( pageEditorWidgets != null )
            {
                for ( var i = 0 ; i < pageEditorWidgets.length ; i++ )
                {
                    pageEditorWidgets[i].editModeRedisplay( moveModeIsEnabled );
                }
            }

            this.show();

            this.editPageSyncPortletActions( true, jsObj );

            if ( moveModeIsEnabled )
            {
                this.editMoveModeStart();
            }
            if ( jsObj.UAie6 )
                jsObj.page.displayAllPWins();
        },
        editPageDestroy: function()
        {
            var pageEditorWidgets = this.pageEditorWidgets;
            if ( pageEditorWidgets != null )
            {
                for ( var i = 0 ; i < pageEditorWidgets.length ; i++ )
                {
                    pageEditorWidgets[i].destroy();
                    pageEditorWidgets[i] = null;
                }
            }

            this.pageEditorWidgets = null;
            this.layoutEditPaneWidgets = null;
            this.pageEditPaneWidget = null;

            if ( this.deletePortletDialog != null )
                this.deletePortletDialog.destroy();
            if ( this.deleteLayoutDialog != null )
                this.deleteLayoutDialog.destroy();
            if ( this.columnSizeDialog != null )
                this.columnSizeDialog.destroy();

            this.destroy();
        },

        // methods

        deletePortlet: function( portletEntityId, portletTitle )
        {
            this.deletePortletDialog.portletEntityId = portletEntityId;
            this.deletePortletDialog.portletTitle = portletTitle;
            this.deletePortletTitle.innerHTML = portletTitle;
            this._openDialog( this.deletePortletDialog );
        },
        deletePortletConfirmed: function( portletEntityId )
        {
            var removePortletContentManager = new jetspeed.widget.RemovePortletContentManager( portletEntityId, this );
            removePortletContentManager.getContent();
        },
        deleteLayout: function( layoutId )
        {
            this.deleteLayoutDialog.layoutId = layoutId;
            this.deleteLayoutDialog.layoutTitle = layoutId;
            this.deleteLayoutTitle.innerHTML = layoutId;
            this._openDialog( this.deleteLayoutDialog );
        },
        deleteLayoutConfirmed: function()
        {
            var removePortletContentManager = new jetspeed.widget.RemoveLayoutContentManager( this.deleteLayoutDialog.layoutId, this );
            removePortletContentManager.getContent();
        },
        openColumnSizesEditor: function( layoutId )
        {
            var currentLayout = null;
            if ( layoutId != null )
                currentLayout = jetspeed.page.layouts[ layoutId ];

            if ( currentLayout != null && currentLayout.columnSizes != null && currentLayout.columnSizes.length > 0 )
            {
                var spinnerMax = 5;   // 5 is current max
                var spinnerCount = 0;
                for ( var i = 0 ; i < spinnerMax; i++ )
                {
                    var spinnerWidget = this.columnSizeDialog[ "spinner" + i ];
                    var spinnerFieldDiv = this[ "spinner" + i + "Field" ];
                    if ( i < currentLayout.columnSizes.length )
                    {
                        spinnerWidget.setValue( currentLayout.columnSizes[i] );
                        spinnerFieldDiv.style.display = "block";
                        spinnerWidget.show();
                        spinnerCount++;
                    }
                    else
                    {
                        spinnerFieldDiv.style.display = "none";
                        spinnerWidget.hide();
                    }
                }
                this.columnSizeDialog.layoutId = layoutId;
                this.columnSizeDialog.columnCount = spinnerCount;
                this._openDialog( this.columnSizeDialog );
            }
        },
        columnSizeConfirmed: function( layoutId, columnSizes )
        {
            if ( layoutId != null && columnSizes != null && columnSizes.length > 0 )
            {   // layout name is currently required by updatepage/update-fragment
                var currentLayout = jetspeed.page.layouts[ layoutId ];
    
                var currentLayoutName = null;
                if ( currentLayout != null )
                    currentLayoutName = currentLayout.name;

                if ( currentLayoutName != null )
                {
                    var colSizesStr = "";
                    for ( var i = 0 ; i < columnSizes.length ; i++ )
                    {
                        if ( i > 0 )
                            colSizesStr += ",";
                        colSizesStr += columnSizes[i] + "%";
                    }
                    var updateFragmentContentManager = new jetspeed.widget.UpdateFragmentContentManager( layoutId, currentLayoutName, colSizesStr, this );
                    updateFragmentContentManager.getContent();
                }
            }
        },
        checkPerm: function(p,jsObj,proto)
        {
            var peProto = proto || jsObj.widget.PageEditor.prototype;
            var perms = peProto.perms;
            if ( perms == null )
                perms = peProto.perms = jsObj.page._perms(jsObj.prefs,-1,String.fromCharCode);
            if ( perms == null ) return false;
            if ( p )
                return ((perms[0] & p) > 0);
            return perms;
        },
        getLDepthPerm: function(jsObj,proto)
        {
            var perms = this.checkPerm(null,jsObj,proto);
            if ( perms && perms.length >= 2 )
                return perms[1];
            return -1;
        },
        canL_NA_ED: function(jsObj,proto)
        {
            var peProto = proto || jsObj.widget.PageEditor.prototype;
            var cL_NA_ED = peProto.checkPerm( peProto.PM_L_NA_ED, jsObj, peProto );
            if ( ! cL_NA_ED )
                cL_NA_ED = peProto.hasRPerm( jsObj, peProto );
            return cL_NA_ED;
        },
        hasRPerm: function(jsObj,proto)
        {
            var peProto = proto || jsObj.widget.PageEditor.prototype;
            var rperm = false;
            if ( typeof proto.permR != "undefined" )
                rperm = proto.permR;
            else
            {
                var perms = this.checkPerm(null,jsObj,proto);
                if ( perms && perms.length >= 3 && perms[2] && perms[2].length > 0 )
                {
                    var u = jsObj.page._getU();
                    if ( u && u.r && u.r[ perms[2] ] )
                    {
                        rperm = true;
                    }
                }
                proto.permR = rperm;
            }
            return rperm;
        },
        refreshPage: function()
        {
            dojo.lang.setTimeout( this, this._doRefreshPage, 10 );
        },
        _doRefreshPage: function()
        {
            var jsObj = jetspeed;
            var pageUrl = jsObj.page.getPageUrl();

            //pageUrl = jsObj.url.addQueryParameter( pageUrl, jsObj.id.PG_ED_PARAM, "true", true );  // BOZO:NOW: is this ok when ajax-pagenavigation is off???? 
            if ( ! jsObj.prefs.ajaxPageNavigation )
            {
                var stateVal = 0;
                var wt = null;
                    
                if ( this.editModeMove )
                {
                    if ( ! this.checkPerm( this.PM_MZ_P, jetspeed ) )
                        stateVal |= this.PM_MZ_P;
                    var identifierCheck = /\b([a-z_A-Z$]\w*)\b(?!-)/;
                    var pWins = jsObj.page.getPWins();
                    var windowTitlesAdded = 0;
                    var windowTitlesJSON = [];
                    for ( var i = 0; i < pWins.length; i++ )
                    {
                        pWin = pWins[i];
                        if ( pWin && pWin.portlet )
                        {
                            var pTitle = pWin.getPortletTitle();
                            if ( pTitle != null && pTitle.length > 0 )
                            {
                                var pWinId = pWin.portlet.entityId;
                                if ( ! identifierCheck.test( pWinId ) )
                                    pWinId = "\"" +  pWinId + "\"";

                                if ( windowTitlesAdded > 0 )
                                    windowTitlesJSON.push( "," );
                                windowTitlesJSON.push( pWinId+":\"" + pTitle + "\"" );
                                windowTitlesAdded++;
                                if ( windowTitlesJSON.length > 1024 )
                                {
                                    windowTitlesJSON = null;
                                    break;
                                }
                            }
                        }
                    }
                    if ( windowTitlesAdded > 0 && windowTitlesJSON != null && windowTitlesJSON.length > 0 )
                    {
                        wt = "";
                        var wtRaw = windowTitlesJSON.join("");
                        var wtRawLen = wtRaw.length;
                        var wtCh, wtHCh, wtHChLen;
                        for ( var wtRawI = 0 ; wtRawI < wtRawLen ; wtRawI++ )
                        {
                            wtCh = wtRaw.charCodeAt( wtRawI );
                            wtHCh = (wtCh).toString(16);
                            wtHChLen = wtHCh.length;
                            if ( wtHChLen < 1 || wtHChLen > 2 )
                            {
                                wt = null;
                                windowTitlesJSON = null;
                                break;
                            }
                            else if ( wtHChLen == 1 )
                            {
                                wtHCh += "0";
                            }
                            wt += wtHCh;
                        }
                        if ( wt == null || wt.length == 0 )
                            wt = null;
                        else
                            stateVal |= this.PM_MZ_P;
                    }
                }
                var modPageUrl = pageUrl;
                if ( wt != null || stateVal > 0 )
                {
                    var jsUrl = jsObj.url;
                    modPageUrl = jsUrl.parse( pageUrl.toString() );
                    if ( stateVal > 0 )
                        modPageUrl = jsUrl.addQueryParameter( modPageUrl, jsObj.id.PG_ED_STATE_PARAM, (stateVal).toString(16), true );
                    if ( wt != null && wt.length > 0 )
                        modPageUrl = jsUrl.addQueryParameter( modPageUrl, jsObj.id.PG_ED_TITLES_PARAM, wt, true );
                }

                jsObj.pageNavigate( modPageUrl.toString(), null, true );
            }
            else
            {
                jsObj.updatePage( pageUrl.toString(), false, true, { editModeMove: this.editModeMove } );
            }
        },

        editMoveModeExit: function( sysInitiated )
        {
            var jsObj = jetspeed;
            var isIE6 = jsObj.UAie6;
            if ( isIE6 )
                jsObj.page.displayAllPWins( true );

            // restore all windows (that were not already minimized prior to move-mode)
            jsObj.widget.PortletWindow.prototype.restoreAllFromMinimizeWindowTemporarily();

            var lepWidgets = this.layoutEditPaneWidgets;
            if ( lepWidgets != null )
            {
                for ( var i = 0 ; i < lepWidgets.length ; i++ )
                {
                    lepWidgets[i]._disableMoveMode();
                }
            }

            if ( ! sysInitiated )
                delete this.editModeMove;
        },

        editMoveModeStart: function()
        {
            var jsObj = jetspeed;
            var hideTiledWins = false;

            if ( jsObj.UAie6 )
                jsObj.page.displayAllPWins( true );

            var pWinObjsToRemainVisible = [];
            var pWinIdsToRemainVisible = [];
            if ( this.dbOn )   // keep showing debug window if appropriate
            {
                var pWinDebug = jsObj.debugWindow();
                if ( pWinDebug && ( ! hideTiledWins || ! pWinDebug.posStatic || jsObj.debug.dragWindow ) )
                {
                    pWinObjsToRemainVisible.push( pWinDebug );
                    pWinIdsToRemainVisible.push( pWinDebug.widgetId );
                }
            }        

            // minimize or hide all windows
            if ( ! hideTiledWins )
            {
                var pWin;
                var pWins = jsObj.page.getPWins();
                for ( var i = 0; i < pWins.length; i++ )
                {
                    pWin = pWins[i];
                    if ( pWin.posStatic )
                    {
                        pWinObjsToRemainVisible.push( pWin );
                        pWinIdsToRemainVisible.push( pWin.widgetId );
                        pWin.minimizeWindowTemporarily();
                    }
                }
            }
            jsObj.widget.hideAllPortletWindows( pWinIdsToRemainVisible );

            var lepWidgets = this.layoutEditPaneWidgets;
            if ( lepWidgets != null )
            {
                for ( var i = 0 ; i < lepWidgets.length ; i++ )
                {
                    lepWidgets[i]._enableMoveMode();
                }
            }

            if ( jsObj.UAie6 )
            {
                setTimeout(function() {
                    jsObj.page.displayAllPWins( false, pWinObjsToRemainVisible );
                }, 20);
            }
            this.editModeMove = true;
        },
        onBrowserWindowResize: function()
        {   // called after ie6 resize window
            var deletePDialog = this.deletePortletDialog;
            var deleteLDialog = this.deleteLayoutDialog;
            var colSizeDialog = this.columnSizeDialog;
            if ( deletePDialog && deletePDialog.isShowing() )
            {
                deletePDialog.domNode.style.display = "none";
                deletePDialog.domNode.style.display = "block";
            }
            if ( deleteLDialog && deleteLDialog.isShowing() )
            {
                deleteLDialog.domNode.style.display = "none";
                deleteLDialog.domNode.style.display = "block";
            }
            if ( colSizeDialog && colSizeDialog.isShowing() )
            {
                colSizeDialog.domNode.style.display = "none";
                colSizeDialog.domNode.style.display = "block";
            }

            var pageEditorWidgets = this.pageEditorWidgets;
            if ( pageEditorWidgets != null )
            {
                for ( var i = 0 ; i < pageEditorWidgets.length ; i++ )
                {
                    pageEditorWidgets[i].onBrowserWindowResize();
                }
            }
        },
        PM_PG_L_D: 16, PM_L_N: 32, PM_L_CS: 64, PM_PG_AD: 128, PM_P_AD: 256, PM_PG_P_D: 512, PM_P_D: 1024, PM_MZ_P: 2048, PM_L_NA_ED: 4096, PM_L_NA_TLMV: 8192, PM_L_NA_CS: 16384,
        
        _openDialog: function( dialogWidget )
        {   // this is to address a mozilla bug where insertion point is always invisible in text boxes
            var isMoz = jetspeed.UAmoz;
            if ( isMoz )
            {
                dialogWidget.domNode.style.position = "fixed";  // this fix involves setting position to fixed instead of absolute,
                if ( ! dialogWidget._fixedIPtBug )              // and the change to var x and var y initialization in placeModalDialog
                {
                    var _dialog = dialogWidget;
                    _dialog.placeModalDialog = function() {
			            // summary: position modal dialog in center of screen

			            var scroll_offset = dojo.html.getScroll().offset;
			            var viewport_size = dojo.html.getViewport();
			
			            // find the size of the dialog (dialog needs to be showing to get the size)
			            var mb;
			            if(_dialog.isShowing()){
				            mb = dojo.html.getMarginBox(_dialog.domNode);
			            }else{
				            dojo.html.setVisibility(_dialog.domNode, false);
				            dojo.html.show(_dialog.domNode);
				            mb = dojo.html.getMarginBox(_dialog.domNode);
				            dojo.html.hide(_dialog.domNode);
				            dojo.html.setVisibility(_dialog.domNode, true);
                        }
                        //var x = scroll_offset.x + (viewport_size.width - mb.width)/2;
			            //var y = scroll_offset.y + (viewport_size.height - mb.height)/2;
                        var x = (viewport_size.width - mb.width)/2;
			            var y = (viewport_size.height - mb.height)/2;
			            with(_dialog.domNode.style){
				            left = x + "px";
				            top = y + "px";
			            }
                    };
                    _dialog._fixedIPtBug = true;
                }
		    }
            dialogWidget.show();
        }
	}
);


// ... jetspeed.widget.EditPageGetThemesContentManager
jetspeed.widget.EditPageGetThemesContentManager = function( pageEditorWidget, pageDecorations, portletDecorations, layouts, desktopPageDecorations, desktopPortletDecorations, startInEditModeMove )
{
    this.pageEditorWidget = pageEditorWidget;
    var getThemeTypes = new Array();
    if ( pageDecorations )
        getThemeTypes.push( [ "pageDecorations" ] );
    if ( portletDecorations )
        getThemeTypes.push( [ "portletDecorations" ] );
    if ( layouts )
        getThemeTypes.push( [ "layouts" ] );
    if ( desktopPageDecorations )
        getThemeTypes.push( [ "desktopPageDecorations", "pageDecorations" ] );
    if ( desktopPortletDecorations )
        getThemeTypes.push( [ "desktopPortletDecorations", "portletDecorations" ] );
    this.getThemeTypes = getThemeTypes;
    this.getThemeTypeNextIndex = 0;
    this.startInEditModeMove = startInEditModeMove;
};
jetspeed.widget.EditPageGetThemesContentManager.prototype =
{
    getContent: function()
    {
        if ( this.getThemeTypes != null && this.getThemeTypes.length > this.getThemeTypeNextIndex )
        {
            var queryString = "?action=getthemes&type=" + this.getThemeTypes[ this.getThemeTypeNextIndex ][0] + "&format=json";
            var getThemesUrl = jetspeed.url.basePortalUrl() + jetspeed.url.path.AJAX_API + queryString ;
            var ajaxApiContext = new jetspeed.om.Id( "getthemes", { } );
            var bindArgs = {};
            bindArgs.url = getThemesUrl;
            bindArgs.mimetype = "text/json";
            jetspeed.url.retrieveContent( bindArgs, this, ajaxApiContext, jetspeed.debugContentDumpIds );
        }
        else
        {
            this.pageEditorWidget.editPageBuild( this.startInEditModeMove );
        }
    },
    notifySuccess: function( /* JSON */ getThemesData, /* String */ requestUrl, domainModelObject )
    {
        if ( jetspeed.page.themeDefinitions == null )
            jetspeed.page.themeDefinitions = {};
        var themeDefKey = ( ( this.getThemeTypes[ this.getThemeTypeNextIndex ].length > 1 ) ? this.getThemeTypes[ this.getThemeTypeNextIndex ][1] : this.getThemeTypes[ this.getThemeTypeNextIndex ][0]);
        jetspeed.page.themeDefinitions[ themeDefKey ] = getThemesData;
        this.getThemeTypeNextIndex++;
        this.getContent();
    },
    notifyFailure: function( /* String */ type, /* Object */ error, /* String */ requestUrl, domainModelObject )
    {
        dojo.raise( "EditPageGetThemesContentManager notifyFailure url: " + requestUrl + " type: " + type + jetspeed.formatError( error ) );
    }
};

// ... jetspeed.widget.RemovePageContentManager
jetspeed.widget.RemovePageContentManager = function( pageEditorWidget )
{
    this.pageEditorWidget = pageEditorWidget;    
};
jetspeed.widget.RemovePageContentManager.prototype =
{
    getContent: function()
    {
        var queryString = "?action=updatepage&method=remove";
        var removePageUrl = jetspeed.url.basePortalUrl() + jetspeed.url.path.AJAX_API + jetspeed.page.getPath() + queryString ;
        var ajaxApiContext = new jetspeed.om.Id( "updatepage-remove-page", { } );
        var bindArgs = {};
        bindArgs.url = removePageUrl;
        bindArgs.mimetype = "text/xml";
        jetspeed.url.retrieveContent( bindArgs, this, ajaxApiContext, jetspeed.debugContentDumpIds );
    },
    notifySuccess: function( /* XMLDocument */ data, /* String */ requestUrl, /* Portlet */ portlet )
    {
        if ( jetspeed.url.checkAjaxApiResponse( requestUrl, data, null, true, "updatepage-remove-page" ) )
        {
            var pageUrl = jetspeed.page.makePageUrl( "/" );
            pageUrl += "?" + jetspeed.id.PG_ED_PARAM + "=true";
            window.location.href = pageUrl;
        }
    },
    notifyFailure: function( /* String */ type, /* Object */ error, /* String */ requestUrl, /* Portlet */ portlet )
    {
        dojo.raise( "RemovePageContentManager notifyFailure url: " + requestUrl + " type: " + type + jetspeed.formatError( error ) );
    }
};

jetspeed.widget.IE6ZappedContentRestorer = function( colNodes )
{
    this.colNodes = colNodes;
    this.nextColNodeIndex = 0;
};
jetspeed.widget.IE6ZappedContentRestorer.prototype =
{
    showNext: function()
    {
        if ( this.colNodes && this.colNodes.length > this.nextColNodeIndex )
        {
            dojo.dom.insertAtIndex( jetspeed.widget.ie6ZappedContentHelper, this.colNodes[this.nextColNodeIndex], 0 );
            dojo.lang.setTimeout( this, this.removeAndShowNext, 20 );
        }
    },
    removeAndShowNext: function()
    {
        dojo.dom.removeNode( jetspeed.widget.ie6ZappedContentHelper );
        this.nextColNodeIndex++;
        if ( this.colNodes && this.colNodes.length > this.nextColNodeIndex )
            dojo.lang.setTimeout( this, this.showNext, 20 );
    }
};

// ... jetspeed.widget.AddPageContentManager
jetspeed.widget.AddPageContentManager = function( pageRealPath, pagePath, pageName, layoutName, pageTitle, pageShortTitle, pageEditorWidget )
{
    this.pageRealPath = pageRealPath;
    this.pagePath = pagePath;
    this.pageName = pageName;
    if ( layoutName == null )
    {
        if ( jetspeed.page.themeDefinitions != null && jetspeed.page.themeDefinitions.layouts != null && jetspeed.page.themeDefinitions.layouts.length > 0 && jetspeed.page.themeDefinitions.layouts[0] != null && jetspeed.page.themeDefinitions.layouts[0].length == 2 )
            layoutName = jetspeed.page.themeDefinitions.layouts[0][1];
    }
    this.layoutName = layoutName;
    this.pageTitle = pageTitle;
    this.pageShortTitle = pageShortTitle;
    this.pageEditorWidget = pageEditorWidget;    
};
jetspeed.widget.AddPageContentManager.prototype =
{
    getContent: function()
    {
        if ( this.pageRealPath != null && this.pageName != null )
        {
            var queryString = "?action=updatepage&method=add&path=" + escape( this.pageRealPath ) + "&name=" + escape( this.pageName );
            if ( this.layoutName != null )
                queryString += "&defaultLayout=" + escape( this.layoutName );
            if ( this.pageTitle != null )
                queryString += "&title=" + escape( this.pageTitle );
            if ( this.pageShortTitle != null )
                queryString += "&short-title=" + escape( this.pageShortTitle );
            var addPageUrl = jetspeed.url.basePortalUrl() + jetspeed.url.path.AJAX_API + queryString ;
            var ajaxApiContext = new jetspeed.om.Id( "updatepage-add-page", { } );
            var bindArgs = {};
            bindArgs.url = addPageUrl;
            bindArgs.mimetype = "text/xml";
            jetspeed.url.retrieveContent( bindArgs, this, ajaxApiContext, jetspeed.debugContentDumpIds );
        }
    },
    notifySuccess: function( /* XMLDocument */ data, /* String */ requestUrl, /* Portlet */ portlet )
    {
        if ( jetspeed.url.checkAjaxApiResponse( requestUrl, data, null, true, "updatepage-add-page" ) )
        {
            var pageUrl = jetspeed.page.makePageUrl( this.pagePath );
            if ( ! dojo.string.endsWith( pageUrl, ".psml", true ) )
                pageUrl += ".psml";
            pageUrl += "?" + jetspeed.id.PG_ED_PARAM + "=true";
            window.location.href = pageUrl;
        }
    },
    notifyFailure: function( /* String */ type, /* Object */ error, /* String */ requestUrl, /* Portlet */ portlet )
    {
        dojo.raise( "AddPageContentManager notifyFailure url: " + requestUrl + " type: " + type + jetspeed.formatError( error ) );
    }
};


// ... jetspeed.widget.MoveLayoutContentManager
jetspeed.widget.MoveLayoutContentManager = function( layoutId, moveToLayoutId, column, row, pageEditorWidget )
{
    this.layoutId = layoutId;
    this.moveToLayoutId = moveToLayoutId;
    this.column = column;
    this.row = row;
    this.pageEditorWidget = pageEditorWidget;
};
jetspeed.widget.MoveLayoutContentManager.prototype =
{
    getContent: function()
    {
        if ( this.layoutId != null && this.moveToLayoutId != null )
        {
            var queryString = "?action=moveabs&id=" + this.layoutId + "&layoutid=" + this.moveToLayoutId;
            if ( this.column != null )
                queryString += "&col=" + this.column;
            if ( this.row != null )
                queryString += "&row=" + this.row;
            var psmlMoveActionUrl = jetspeed.url.basePortalUrl() + jetspeed.url.path.AJAX_API + jetspeed.page.getPath() + queryString ;
            var ajaxApiContext = new jetspeed.om.Id( "moveabs-layout", this.layoutId );
            var bindArgs = {};
            bindArgs.url = psmlMoveActionUrl;
            bindArgs.mimetype = "text/xml";
            jetspeed.url.retrieveContent( bindArgs, this, ajaxApiContext, jetspeed.debugContentDumpIds );
        }
    },
    notifySuccess: function( /* XMLDocument */ data, /* String */ requestUrl, /* Portlet */ portlet )
    {
        if ( jetspeed.url.checkAjaxApiResponse( requestUrl, data, null, true, "moveabs-layout" ) )
        {
            
        }
    },
    notifyFailure: function( /* String */ type, /* Object */ error, /* String */ requestUrl, /* Portlet */ portlet )
    {
        dojo.raise( "MoveLayoutContentManager notifyFailure url: " + requestUrl + " type: " + type + jetspeed.formatError( error ) );
    }
};

// ... jetspeed.widget.UpdateFragmentContentManager
jetspeed.widget.UpdateFragmentContentManager = function( layoutId, layoutName, layoutSizes, pageEditorWidget )
{
    this.layoutId = layoutId;
    this.layoutName = layoutName;
    this.layoutSizes = layoutSizes;
    this.pageEditorWidget = pageEditorWidget;
};
jetspeed.widget.UpdateFragmentContentManager.prototype =
{
    getContent: function()
    {
        if ( this.layoutId != null )
        {
            var queryString = "?action=updatepage&method=update-fragment&id=" + this.layoutId;
            if ( this.layoutName != null )
                queryString += "&layout=" + escape( this.layoutName );
            if ( this.layoutSizes != null )
                queryString += "&sizes=" + escape( this.layoutSizes );
            var updatePageUrl = jetspeed.url.basePortalUrl() + jetspeed.url.path.AJAX_API + jetspeed.page.getPath() + queryString ;
            var ajaxApiContext = new jetspeed.om.Id( "updatepage-update-fragment", { } );
            var bindArgs = {};
            bindArgs.url = updatePageUrl;
            bindArgs.mimetype = "text/xml";
            jetspeed.url.retrieveContent( bindArgs, this, ajaxApiContext, jetspeed.debugContentDumpIds );
        }
    },
    notifySuccess: function( /* XMLDocument */ data, /* String */ requestUrl, /* Portlet */ portlet )
    {
        if ( jetspeed.url.checkAjaxApiResponse( requestUrl, data, null, true, "updatepage-update-fragment" ) )
        {
            this.pageEditorWidget.refreshPage();
        }
    },
    notifyFailure: function( /* String */ type, /* Object */ error, /* String */ requestUrl, /* Portlet */ portlet )
    {
        dojo.raise( "UpdateFragmentContentManager notifyFailure url: " + requestUrl + " type: " + type + jetspeed.formatError( error ) );
    }
};

// ... jetspeed.widget.UpdateFragmentContentManager
jetspeed.widget.UpdatePageInfoContentManager = function( layoutDecorator, portletDecorator, pageEditorWidget )
{
    this.refreshPage = ( ( pageEditorWidget.editorInitiatedFromDesktop ) ? true : false ) ;
    this.layoutDecorator = layoutDecorator;
    this.portletDecorator = portletDecorator;
    this.pageEditorWidget = pageEditorWidget;
};
jetspeed.widget.UpdatePageInfoContentManager.prototype =
{
    getContent: function()
    {
        var queryString = "?action=updatepage&method=info";
        if ( this.layoutDecorator != null )
            queryString += "&layout-decorator=" + escape( this.layoutDecorator );
        if ( this.portletDecorator != null )
            queryString += "&portlet-decorator=" + escape( this.portletDecorator );
        var updatePageUrl = jetspeed.url.basePortalUrl() + jetspeed.url.path.AJAX_API + jetspeed.page.getPath() + queryString ;
        var ajaxApiContext = new jetspeed.om.Id( "updatepage-info", { } );
        var bindArgs = {};
        bindArgs.url = updatePageUrl;
        bindArgs.mimetype = "text/xml";
        jetspeed.url.retrieveContent( bindArgs, this, ajaxApiContext, jetspeed.debugContentDumpIds );
    },
    notifySuccess: function( /* XMLDocument */ data, /* String */ requestUrl, /* Portlet */ portlet )
    {
        if ( jetspeed.url.checkAjaxApiResponse( requestUrl, data, null, true, "updatepage-info" ) )
        {
            if ( this.refreshPage )
                this.pageEditorWidget.refreshPage();
        }
    },
    notifyFailure: function( /* String */ type, /* Object */ error, /* String */ requestUrl, /* Portlet */ portlet )
    {
        dojo.raise( "UpdatePageInfoContentManager notifyFailure url: " + requestUrl + " type: " + type + jetspeed.formatError( error ) );
    }
};

// ... jetspeed.widget.RemovePortletContentManager
jetspeed.widget.RemovePortletContentManager = function( portletEntityId, pageEditorWidget )
{
    this.portletEntityId = portletEntityId;
    this.pageEditorWidget = pageEditorWidget;
};
jetspeed.widget.RemovePortletContentManager.prototype =
{
    getContent: function()
    {
        if ( this.portletEntityId != null )
        {
            var queryString = "?action=remove&id=" + this.portletEntityId;
            var removePortletUrl = jetspeed.url.basePortalUrl() + jetspeed.url.path.AJAX_API + jetspeed.page.getPath() + queryString ;
            var ajaxApiContext = new jetspeed.om.Id( "removeportlet", { } );
            var bindArgs = {};
            bindArgs.url = removePortletUrl;
            bindArgs.mimetype = "text/xml";
            jetspeed.url.retrieveContent( bindArgs, this, ajaxApiContext, jetspeed.debugContentDumpIds );
        }
    },
    notifySuccess: function( /* XMLDocument */ data, /* String */ requestUrl, /* Portlet */ portlet )
    {
        if ( jetspeed.url.checkAjaxApiResponse( requestUrl, data, null, true, "removeportlet" ) )
        {
            this.pageEditorWidget.refreshPage();
        }
    },
    notifyFailure: function( /* String */ type, /* Object */ error, /* String */ requestUrl, /* Portlet */ portlet )
    {
        dojo.raise( "RemovePortletContentManager notifyFailure url: " + requestUrl + " type: " + type + jetspeed.formatError( error ) );
    }
};

// ... jetspeed.widget.RemoveLayoutContentManager
jetspeed.widget.RemoveLayoutContentManager = function( layoutId, pageEditorWidget )
{
    this.layoutId = layoutId;
    this.pageEditorWidget = pageEditorWidget;
};
jetspeed.widget.RemoveLayoutContentManager.prototype =
{
    getContent: function()
    {
        if ( this.layoutId != null )
        {
            var queryString = "?action=updatepage&method=remove-fragment&id=" + this.layoutId;
            var removeLayoutUrl = jetspeed.url.basePortalUrl() + jetspeed.url.path.AJAX_API + jetspeed.page.getPath() + queryString ;
            var ajaxApiContext = new jetspeed.om.Id( "removelayout", { } );
            var bindArgs = {};
            bindArgs.url = removeLayoutUrl;
            bindArgs.mimetype = "text/xml";
            jetspeed.url.retrieveContent( bindArgs, this, ajaxApiContext, jetspeed.debugContentDumpIds );
        }
    },
    notifySuccess: function( /* XMLDocument */ data, /* String */ requestUrl, /* Portlet */ portlet )
    {
        if ( jetspeed.url.checkAjaxApiResponse( requestUrl, data, null, true, "removeportlet" ) )
        {
            this.pageEditorWidget.refreshPage();
        }
    },
    notifyFailure: function( /* String */ type, /* Object */ error, /* String */ requestUrl, /* Portlet */ portlet )
    {
        dojo.raise( "RemoveLayoutContentManager notifyFailure url: " + requestUrl + " type: " + type + jetspeed.formatError( error ) );
    }
};

// ... jetspeed.widget.AddLayoutContentManager
jetspeed.widget.AddLayoutContentManager = function( parentLayoutId, layoutName, pageEditorWidget )
{
    this.parentLayoutId = parentLayoutId;
    this.layoutName = layoutName;
    this.pageEditorWidget = pageEditorWidget;
};
jetspeed.widget.AddLayoutContentManager.prototype =
{
    getContent: function()
    {
        if ( this.parentLayoutId != null )
        {
            var queryString = "?action=updatepage&method=add-fragment&layoutid=" + this.parentLayoutId + ( this.layoutName != null ? ( "&layout=" + this.layoutName ) : "" );
            var addLayoutUrl = jetspeed.url.basePortalUrl() + jetspeed.url.path.AJAX_API + jetspeed.page.getPath() + queryString ;
            var ajaxApiContext = new jetspeed.om.Id( "addlayout", { } );
            var bindArgs = {};
            bindArgs.url = addLayoutUrl;
            bindArgs.mimetype = "text/xml";
            jetspeed.url.retrieveContent( bindArgs, this, ajaxApiContext, jetspeed.debugContentDumpIds );
        }
    },
    notifySuccess: function( /* XMLDocument */ data, /* String */ requestUrl, /* Portlet */ portlet )
    {
        if ( jetspeed.url.checkAjaxApiResponse( requestUrl, data, null, true, "addlayout" ) )
        {
            this.pageEditorWidget.refreshPage();
        }
    },
    notifyFailure: function( /* String */ type, /* Object */ error, /* String */ requestUrl, /* Portlet */ portlet )
    {
        dojo.raise( "AddLayoutContentManager notifyFailure url: " + requestUrl + " type: " + type + jetspeed.formatError( error ) );
    }
};
