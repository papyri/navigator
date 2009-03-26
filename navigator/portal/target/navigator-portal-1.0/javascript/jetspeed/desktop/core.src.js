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
 * author: David Sean Taylor
 */

/**
 * jetspeed desktop core javascript objects and types
 *
 * 2007-02-20: this file desperately needs to be broken up once deployment support
 *             for javascript compression and aggregation is implemented
 */

dojo.provide( "jetspeed.desktop.core" );

dojo.require( "dojo.lang.*" );
dojo.require( "dojo.event.*" );
dojo.require( "dojo.io.*" );
dojo.require( "dojo.uri.Uri" );
dojo.require( "dojo.widget.*" );
dojo.require( "jetspeed.common" );


// jetspeed base objects

if ( ! window.jetspeed )
    jetspeed = {};
if ( ! jetspeed.om )
    jetspeed.om = {};
if ( ! jetspeed.debug )
    jetspeed.debug = {};


// jetspeed.id

jetspeed.id =
{
    PAGE: "jetspeedPage",
    DESKTOP_CELL: "jetspeedDesktopCell",
    DESKTOP: "jetspeedDesktop",
    COLUMNS: "jetspeedColumns",
    PAGE_CONTROLS: "jetspeedPageControls",
    
    P_CLASS: "portlet",
    PWIN_CLASS: "portletWindow",
    PWIN_CLIENT_CLASS: "portletWindowClient",
    PWIN_GHOST_CLASS: "ghostPane",
    PW_ID_PREFIX: "pw_",
    COL_CLASS: "desktopColumn",
    COL_LAYOUTHEADER_CLASS: "desktopLayoutHeader",

    // ... pp - portlet props
    PP_WIDGET_ID: "widgetId",
    PP_CONTENT_RETRIEVER: "contentRetriever",
    PP_DESKTOP_EXTENDED: "jsdesktop",
    PP_WINDOW_POSITION_STATIC: "windowPositionStatic",
    PP_WINDOW_HEIGHT_TO_FIT: "windowHeightToFit",
    PP_WINDOW_DECORATION: "windowDecoration",
    PP_WINDOW_TITLE: "title",
    PP_WINDOW_ICON: "windowIcon",
    PP_WIDTH: "width",
    PP_HEIGHT: "height",
    PP_LEFT: "left",
    PP_TOP: "top",
    PP_COLUMN: "column",
    PP_ROW: "row",
    PP_EXCLUDE_PCONTENT: "excludePContent",
    PP_WINDOW_STATE: "windowState",

    PP_STATICPOS: "staticpos",
    PP_FITHEIGHT: "fitheight",
    PP_PROP_SEPARATOR: "=",
    PP_PAIR_SEPARATOR: ";",

    // these constants for action names are defined because they have special meaning to desktop (ie. this is not a list of all supported actions)
    ACT_MENU: "menu",
    ACT_MINIMIZE: "minimized",
    ACT_MAXIMIZE: "maximized",
    ACT_RESTORE: "normal",
    ACT_PRINT: "print",
    ACT_EDIT: "edit",
    ACT_VIEW: "view",
    ACT_HELP: "help",
    ACT_ADDPORTLET: "addportlet",
    ACT_REMOVEPORTLET: "removeportlet",
    ACT_CHANGEPORTLETTHEME: "changeportlettheme",

    ACT_DESKTOP_TILE: "tile",
    ACT_DESKTOP_UNTILE: "untile",
    ACT_DESKTOP_HEIGHT_EXPAND: "heightexpand",
    ACT_DESKTOP_HEIGHT_NORMAL: "heightnormal",

    ACT_DESKTOP_MOVE_TILED: "movetiled",
    ACT_DESKTOP_MOVE_UNTILED: "moveuntiled",

    ACT_LOAD_RENDER: "loadportletrender",
    ACT_LOAD_ACTION: "loadportletaction",
    ACT_LOAD_UPDATE: "loadportletupdate",

    PORTLET_ACTION_TYPE_MODE: "mode",
    PORTLET_ACTION_TYPE_STATE: "state",

    MENU_WIDGET_ID_PREFIX: "jetspeed-menu-",

    PG_ED_WID: "jetspeed-page-editor",
    PG_ED_PARAM: "editPage",

    ADDP_RFRAG: "aR",
    
    PG_ED_STATE_PARAM: "epst",
    PG_ED_TITLES_PARAM: "wintitles",
    PORTAL_ORIGINATE_PARAMETER: "portal",
    PM_P_AD: 256, PM_P_D: 1024, PM_MZ_P: 2048,

    DEBUG_WINDOW_TAG: "js-db"
};

// jetspeed desktop preferences - defaults

jetspeed.prefs = 
{
    windowTiling: true,                 // false indicates no-columns, free-floating windows
    windowHeightExpand: false,          // only meaningful when windowTiling == true

    ajaxPageNavigation: false,
    
    windowWidth: null,                  // last-ditch defaults for these defined in initializeDesktop
    windowHeight: null,

    layoutName: null,                   // do not access directly - use getLayoutName()
    layoutRootUrl: null,                // do not access directly - use getLayoutRootUrl()
    getLayoutName: function()
    {
        if ( jetspeed.prefs.layoutName == null && djConfig.jetspeed != null )
            jetspeed.prefs.layoutName = djConfig.jetspeed.layoutName;
        return jetspeed.prefs.layoutName;
    },
    getLayoutRootUrl: function()
    {
        if ( jetspeed.prefs.layoutRootUrl == null && djConfig.jetspeed != null )
            jetspeed.prefs.layoutRootUrl = jetspeed.url.basePortalDesktopUrl() + djConfig.jetspeed.layoutDecorationPath;
        return jetspeed.prefs.layoutRootUrl;
    },
    getPortletDecorationsRootUrl: function()
    {
        if ( jetspeed.prefs.portletDecorationsRootUrl == null && djConfig.jetspeed != null )
            jetspeed.prefs.portletDecorationsRootUrl = jetspeed.url.basePortalDesktopUrl() + djConfig.jetspeed.portletDecorationsPath;
        return jetspeed.prefs.portletDecorationsRootUrl;
    },

    portletSelectorWindowTitle: "Portlet Selector",
    portletSelectorWindowIcon: "text-x-script.png",
    portletSelectorBounds: { x: 20, y: 20, width: 400, height: 600 },

    
    windowActionButtonMax: 5,
    windowActionButtonTooltip: true,
    //windowActionButtonOrder, windowActionNotPortlet, windowActionMenuOrder - see jetspeed.initializeDesktop
    
    windowIconEnabled: true,
    windowIconPath: "/images/portlets/small",

    windowTitlebar: true,
    windowResizebar: true,

    windowDecoration: "tigris",

    pageActionButtonTooltip: true,

    getPortletDecorationBaseUrl: function( portletDecorationName )
    {
        return jetspeed.prefs.getPortletDecorationsRootUrl() + "/" + portletDecorationName;
    },

    getActionLabel: function( actionName, nullIfNotSpecified, jsPrefs, djObj )
    {
        if ( actionName == null ) return null;
        var actionlabel = null;
        var actionLabelPrefs = jsPrefs.desktopActionLabels;
        if ( actionLabelPrefs != null )
            actionlabel = actionLabelPrefs[ actionName ];
        if ( actionlabel == null || actionlabel.length == 0 )
        {
            actionlabel = null;
            if ( ! nullIfNotSpecified )
                actionlabel = djObj.string.capitalize( actionName );
        }
        return actionlabel;
    }
};


// load page /portlets

jetspeed.page = null ;
jetspeed.initializeDesktop = function()
{
    var jsObj = jetspeed;
    var jsId = jsObj.id;
    var jsPrefs = jsObj.prefs;
    var jsDebug = jsObj.debug;
    var djObj = dojo;

    jsObj.getBody();   // sets jetspeed.docBody

    jsObj.ui.initCssObj();
    
    jsPrefs.windowActionButtonOrder = [ jsId.ACT_MENU, "edit", "view", "help", jsId.ACT_MINIMIZE, jsId.ACT_RESTORE, jsId.ACT_MAXIMIZE ];
    jsPrefs.windowActionNotPortlet = [ jsId.ACT_MENU, jsId.ACT_MINIMIZE, jsId.ACT_RESTORE, jsId.ACT_MAXIMIZE ];
    jsPrefs.windowActionMenuOrder = [ jsId.ACT_DESKTOP_HEIGHT_EXPAND, jsId.ACT_DESKTOP_HEIGHT_NORMAL, jsId.ACT_DESKTOP_TILE, jsId.ACT_DESKTOP_UNTILE ];

    jsObj.url.pathInitialize();

    var djConfJetspeed = djConfig.jetspeed;
    if ( djConfJetspeed != null )
    {
        for ( var prefKey in djConfJetspeed )
        {
            var prefOverrideVal = djConfJetspeed[ prefKey ];
            if ( prefOverrideVal != null )
            {
                if ( jsDebug[ prefKey ] != null )
                    jsDebug[ prefKey ] = prefOverrideVal;
                else
                    jsPrefs[ prefKey ] = prefOverrideVal;
            }
        }
        if ( jsPrefs.windowWidth == null || isNaN( jsPrefs.windowWidth ) )
            jsPrefs.windowWidth = "280";
        if ( jsPrefs.windowHeight == null || isNaN( jsPrefs.windowHeight ) )
            jsPrefs.windowHeight = "200";
        
        var windowActionDesktopAll = [ jsId.ACT_DESKTOP_HEIGHT_EXPAND, jsId.ACT_DESKTOP_HEIGHT_NORMAL, jsId.ACT_DESKTOP_TILE, jsId.ACT_DESKTOP_UNTILE ];
        var windowActionDesktop = {};
        for ( var i = 0 ; i < windowActionDesktopAll.length ; i++ )
            windowActionDesktop[ windowActionDesktopAll[i] ] = true;
        windowActionDesktopAll.push( jsId.ACT_DESKTOP_MOVE_TILED );
        windowActionDesktopAll.push( jsId.ACT_DESKTOP_MOVE_UNTILED );
        jsPrefs.windowActionDesktopAll = windowActionDesktopAll;
        jsPrefs.windowActionDesktop = windowActionDesktop;
    }
    var defaultPortletWindowCSSUrl = new djObj.uri.Uri( jetspeed.url.basePortalDesktopUrl() + "/javascript/jetspeed/widget/PortletWindow.css" );
    djObj.html.insertCssFile( defaultPortletWindowCSSUrl, document, true );

    if ( jsPrefs.portletDecorationsAllowed == null || jsPrefs.portletDecorationsAllowed.length == 0 )
    {
        if ( jsPrefs.windowDecoration != null )
            jsPrefs.portletDecorationsAllowed = [ jsPrefs.windowDecoration ];
    }
    else if ( jsPrefs.windowDecoration == null )
    {
        jsPrefs.windowDecoration = jsPrefs.portletDecorationsAllowed[0];
    }
    if ( jsPrefs.windowDecoration == null || jsPrefs.portletDecorationsAllowed == null )
    {
        djObj.raise( "No portlet decorations" );
        return;
    }

    if ( jsPrefs.windowActionNoImage != null )
    {
        var noImageMap = {};
        for ( var i = 0 ; i < jsPrefs.windowActionNoImage.length; i++ )
        {
            noImageMap[ jsPrefs.windowActionNoImage[ i ] ] = true;
        }
        jsPrefs.windowActionNoImage = noImageMap;
    }

    var docUrlObj = jsObj.url.parse( window.location.href );
    var printModeOnly = jsObj.url.getQueryParameter( docUrlObj, "jsprintmode" ) == "true";
    if ( printModeOnly )
    {
        printModeOnly = {};
        printModeOnly.action = jsObj.url.getQueryParameter( docUrlObj, "jsaction" );
        printModeOnly.entity = jsObj.url.getQueryParameter( docUrlObj, "jsentity" );
        printModeOnly.layout = jsObj.url.getQueryParameter( docUrlObj, "jslayoutid" );
        jsPrefs.printModeOnly = printModeOnly;
        jsPrefs.windowTiling = true;
        jsPrefs.windowHeightExpand = true;
        jsPrefs.ajaxPageNavigation = false;
    }

    jsPrefs.portletDecorationsConfig = {};
    for ( var i = 0 ; i < jsPrefs.portletDecorationsAllowed.length ; i++ )
    {
        jsObj.loadPortletDecorationConfig( jsPrefs.portletDecorationsAllowed[ i ], jsPrefs, jsId );
    }

    if ( jsObj.UAie6 )
    {
        jsPrefs.ajaxPageNavigation = false;
        // ie6 with ajax page navigation is not advisable
        // memeory management is bad - page loads get gradually worse
    }

    if ( printModeOnly )
    {
        for ( var portletDecorationName in jsPrefs.portletDecorationsConfig )
        {
            var pdConfig = jsPrefs.portletDecorationsConfig[ portletDecorationName ];
            if ( pdConfig != null )
            {
                pdConfig.windowActionButtonOrder = null;
                pdConfig.windowActionMenuOrder = null;
                pdConfig.windowDisableResize = true;
                pdConfig.windowDisableMove = true;
            }
        }
    }
    jsObj.url.loadingIndicatorShow();

    var windowActionLabels = {};
    if ( jsPrefs.windowActionButtonOrder )
    {   
        var actionName, actionLabel, actArray;
        var actArrays = [ jsPrefs.windowActionButtonOrder, jsPrefs.windowActionMenuOrder, jsPrefs.windowActionDesktopAll ];
        for ( var actAryI = 0 ; actAryI < actArrays.length ; actAryI++ )
        {
            var actArray = actArrays[actAryI];
            if ( ! actArray ) continue;
            for ( var aI = 0 ; aI < actArray.length ; aI++ )
            {
                actionName = actArray[ aI ];
                if ( actionName != null && ! windowActionLabels[ actionName ] )
                    windowActionLabels[ actionName ] = jsPrefs.getActionLabel( actionName, false, jsPrefs, djObj );
            }
        }
    }
    jsObj.widget.PortletWindow.prototype.actionLabels = windowActionLabels;

    jsObj.page = new jsObj.om.Page();

    if ( ! printModeOnly && djConfig.isDebug )
    {
        if ( jsObj.debugWindowLoad )
            jsObj.debugWindowLoad();

        if ( jsObj.debug.profile && djObj.profile )
            djObj.profile.start( "initializeDesktop" );
        else
            jsObj.debug.profile = false;
    }
    else
    {
        jsObj.debug.profile = false;
    }

    jsObj.page.retrievePsml();

    //if ( jsObj.UAie6 )
    {
        jsObj.ui.windowResizeMgr.init( window, jsObj.docBody );
    }
};

jetspeed.updatePage = function( navToPageUrl, backOrForwardPressed, force, initEditModeConf )
{
    var jsObj = jetspeed;
    
    var dbProfile = false;
    if ( djConfig.isDebug && jsObj.debug.profile )
    {
        dbProfile = true;
        dojo.profile.start( "updatePage" );
    }

    var currentPage = jsObj.page;
    if ( ! navToPageUrl || ! currentPage || jsObj.pageNavigateSuppress ) return;
    if ( ! force && currentPage.equalsPageUrl( navToPageUrl ) )
        return ;
    navToPageUrl = currentPage.makePageUrl( navToPageUrl );
    if ( navToPageUrl != null )
    {
        jsObj.updatePageBegin();
        
        if ( initEditModeConf != null && initEditModeConf.editModeMove )
        {
            var windowTitles = {};
            var pWins = currentPage.getPWins();
            for ( var i = 0; i < pWins.length; i++ )
            {
                pWin = pWins[i];
                if ( pWin && pWin.portlet )
                    windowTitles[ pWin.portlet.entityId ] = pWin.getPortletTitle();
            }
            initEditModeConf.windowTitles = windowTitles;
        }

        var currentLayoutDecorator = currentPage.layoutDecorator;
        var currentEditMode = currentPage.editMode;
        if ( dbProfile )
            dojo.profile.start( "destroyPage" );
        currentPage.destroy();
        if ( dbProfile )
            dojo.profile.end( "destroyPage" );
        
        var retainedWindows = currentPage.portlet_windows;        
        var retainedWindowCount = currentPage.portlet_window_count;

        var newJSPage = new jsObj.om.Page( currentLayoutDecorator, navToPageUrl, (! djConfig.preventBackButtonFix && ! backOrForwardPressed), currentPage.tooltipMgr, currentPage.iframeCoverByWinId );
        jsObj.page = newJSPage;

        var pWin;
        if ( retainedWindowCount > 0 )
        {
            for ( var windowId in retainedWindows )
            {
                pWin = retainedWindows[ windowId ];
                pWin.bringToTop( null, true, false, jsObj );
            }
        }
    
        newJSPage.retrievePsml( new jsObj.om.PageCLCreateWidget( true, initEditModeConf ) );
        
        if ( retainedWindowCount > 0 )
        {
            for ( var windowId in retainedWindows )
            {
                pWin = retainedWindows[ windowId ];
                newJSPage.putPWin( pWin );
            }
        }

        window.focus();   // to prevent IE from sending alt-arrow to tab container
    }
};

jetspeed.updatePageBegin = function()
{
    var jsObj = jetspeed;
    if ( jsObj.UAie6 )
    {
        jsObj.docBody.attachEvent( "onclick", jsObj.ie6StopMouseEvts );
        jsObj.docBody.setCapture();
    }
}
jetspeed.ie6StopMouseEvts = function( e )
{
    if ( e )
    {
        e.cancelBubble = true;
        e.returnValue = false;
    }
}
jetspeed.updatePageEnd = function()
{
    var jsObj = jetspeed;
    if ( jsObj.UAie6 )
    {
        jsObj.docBody.releaseCapture();
        jsObj.docBody.detachEvent( "onclick", jsObj.ie6StopMouseEvts );
        jsObj.docBody.releaseCapture();
    }
}


// jetspeed.doRender

jetspeed.doRender = function( bindArgs, portletEntityId )
{
    if ( ! bindArgs )
    {
        bindArgs = {};
    }
    else if ( ( typeof bindArgs == "string" || bindArgs instanceof String ) )
    {
        bindArgs = { url: bindArgs };
    }
    var targetPortlet = jetspeed.page.getPortlet( portletEntityId );
    if ( targetPortlet )
    {
        if ( jetspeed.debug.doRenderDoAction )
            dojo.debug( "doRender [" + portletEntityId + "] url: " + bindArgs.url );
        targetPortlet.retrieveContent( null, bindArgs );
    }
};


// jetspeed.doAction

jetspeed.doAction = function( bindArgs, portletEntityId )
{
    if ( ! bindArgs )
    {
        bindArgs = {};
    }
    else if ( ( typeof bindArgs == "string" || bindArgs instanceof String ) )
    {
        bindArgs = { url: bindArgs };
    }
    var targetPortlet = jetspeed.page.getPortlet( portletEntityId );
    if ( targetPortlet )
    {
        if ( jetspeed.debug.doRenderDoAction )
        {
            if ( ! bindArgs.formNode )
                dojo.debug( "doAction [" + portletEntityId + "] url: " + bindArgs.url + " form: null" );
            else
                dojo.debug( "doAction [" + portletEntityId + "] url: " + bindArgs.url + " form: " + jetspeed.debugDumpForm( bindArgs.formNode ) );
        }
        targetPortlet.retrieveContent( new jetspeed.om.PortletActionCL( targetPortlet, bindArgs ), bindArgs );
    }
};


// jetspeed.PortletRenderer

jetspeed.PortletRenderer = function( createWindows, isPageLoad, isPageUpdate, renderUrl, suppressGetActions, initEditModeConf )
{
    var jsObj = jetspeed;
    var jsPage = jsObj.page;
    var djObj = dojo;
    this._jsObj = jsObj;

    this.mkWins = createWindows;
    this.initEdit = initEditModeConf;
    this.minimizeTemp = ( initEditModeConf != null && initEditModeConf.editModeMove );
    this.noRender = ( this.minimizeTemp && initEditModeConf.windowTitles != null );
    this.isPgLd = isPageLoad;
    this.isPgUp = isPageUpdate;
    this.renderUrl = renderUrl;
    this.suppressGetActions = suppressGetActions;

    this._colLen = jsPage.columns.length;
    this._colIndex = 0;
    this._portletIndex = 0;
    this._renderCount = 0;
    this.psByCol = jsPage.portletsByPageColumn;
    this.pageLoadUrl = null;
    if ( isPageLoad )
    {
        this.pageLoadUrl = jsObj.url.parse( jsPage.getPageUrl() );
        jsObj.ui.evtConnect( "before", djObj, "addOnLoad", jsPage, "_beforeAddOnLoad", djObj.event );
    }

    this.dbgPgLd = jsObj.debug.pageLoad && isPageLoad;
    this.dbgMsg = null;
    if ( jsObj.debug.doRenderDoAction || this.dbgPgLd )
        this.dbgMsg = "";
};
dojo.lang.extend( jetspeed.PortletRenderer,
{
    renderAll: function()
    {
        do
        {
            this._renderCurrent();
        } while ( this._evalNext() )

        this._finished();
    },
    renderAllTimeDistribute: function()
    {
        this._renderCurrent();
        if ( this._evalNext() )
        {
            dojo.lang.setTimeout( this, this.renderAllTimeDistribute, 10 );
        }
        else
        {
            this._finished();
        }
    },
    _finished: function()
    {
        var jsObj = this._jsObj;

        var debugMsg = this.dbgMsg;
        if ( debugMsg != null )
        {
            if ( this.dbgPgLd )
                dojo.debug( "portlet-renderer page-url: " + jsObj.page.getPsmlUrl() + " portlets: [" + renderMsg + "]" + ( url ? ( " url: " + url ) : "" ) );
            else
                dojo.debug( "portlet-renderer [" + renderMsg + "] url: " + url );
        }
        
        if ( this.isPgLd )
        {
            jsObj.page.loadPostRender( this.isPgUp, this.initEdit );
        }
    },
    _renderCurrent: function()
    {
        var jsObj = this._jsObj;
        
        var colLen = this._colLen;
        var colIndex = this._colIndex;
        var portletIndex = this._portletIndex;
        
        if ( colIndex <= colLen )
        {
            var portletArray;
            if ( colIndex < colLen )
                portletArray = this.psByCol[ colIndex.toString() ];
            else
            {
                portletArray = this.psByCol[ "z" ];
                colIndex = null;
            }
            var portletLen = (portletArray != null ? portletArray.length : 0);
            if ( portletLen > 0 )
            {
                var pAryElmt = portletArray[portletIndex];
                if ( pAryElmt )
                {
                    var renderObj = pAryElmt.portlet;
                    var pWin = null;
                    if ( this.mkWins )
                    {
                        pWin = jsObj.ui.createPortletWindow( renderObj, colIndex, jsObj );
                        if ( this.minimizeTemp )
                            pWin.minimizeWindowTemporarily( this.noRender );
                    }
                    
                    var debugMsg = this.dbgMsg;
                    if ( debugMsg != null )
                    {
                        if ( debugMsg.length > 0 )
                            debugMsg = debugMsg + ", ";
                        var widgetId = null;
                        if ( renderObj.getProperty != null )
                            widgetId = renderObj.getProperty( jsObj.id.PP_WIDGET_ID );
                        if ( ! widgetId )
                            widgetId = renderObj.widgetId;
                        if ( ! widgetId )
                            widgetId = renderObj.toString();
                        if ( renderObj.entityId )
                        {
                            debugMsg = debugMsg + renderObj.entityId + "(" + widgetId + ")";
                            if ( this._dbPgLd && renderObj.getProperty( jsObj.id.PP_WINDOW_TITLE ) )
                                debugMsg = debugMsg + " " + renderObj.getProperty( jsObj.id.PP_WINDOW_TITLE );
                        }
                        else
                        {
                            debugMsg = debugMsg + widgetId;
                        }
                    }
                    if ( ! this.noRender )
                        renderObj.retrieveContent( null, { url: this.renderUrl, jsPageUrl: this.pageLoadUrl }, this.suppressGetActions );
                    else if ( pWin && pWin.portlet )
                    {
                        var pWinTitle = this.initEdit.windowTitles[ pWin.portlet.entityId ];
                        if ( pWinTitle != null )
                            pWin.setPortletTitle( pWinTitle );
                    }
                    if ( (this._renderCount % 3) == 0 )
                        jsObj.url.loadingIndicatorStep( jsObj );
                    this._renderCount++;
                }
            }
        }
    },
    _evalNext: function()
    {
        var nextFound = false;
        var colLen = this._colLen;
        var colIndex = this._colIndex;
        var portletIndex = this._portletIndex;

        var curColIndex = colIndex;
        var portletArray;

        // check if there's any portlet window in the next columns.
        for ( ++colIndex; colIndex <= colLen; colIndex++ )
        {
            portletArray = this.psByCol[ colIndex == colLen ? "z" : colIndex.toString() ];
            if ( portletIndex < (portletArray != null ? portletArray.length : 0) )
            {
                nextFound = true;
                this._colIndex = colIndex;
                break;
            }
        }
        
        // check if there's any portlet window in the previous columns.
        if ( ! nextFound )
        {
            ++portletIndex;
            for ( colIndex = 0; colIndex <= curColIndex; colIndex++ )
            {
                portletArray = this.psByCol[ colIndex == colLen ? "z" : colIndex.toString() ];
                if ( portletIndex < (portletArray != null ? portletArray.length : 0) )
                {
                    nextFound = true;
                    this._colIndex = colIndex;
                    this._portletIndex = portletIndex;
                    break;
                }
            }
        }
        return nextFound;
    }
});

jetspeed.portleturl =
{
    DESKTOP_ACTION_PREFIX_URL: null,
    DESKTOP_RENDER_PREFIX_URL: null,
    JAVASCRIPT_ARG_QUOTE: "&" + "quot;",
    PORTLET_REQUEST_ACTION: "action",
    PORTLET_REQUEST_RENDER: "render",
    JETSPEED_DO_NOTHING_ACTION: "javascript:jetspeed.doNothingNav()",

    parseContentUrl: function( /* String */ contentUrl )   // parseContentUrlForDesktopActionRender
    {
        if ( this.DESKTOP_ACTION_PREFIX_URL == null )
            this.DESKTOP_ACTION_PREFIX_URL = jetspeed.url.basePortalUrl() + jetspeed.url.path.ACTION ;
        if ( this.DESKTOP_RENDER_PREFIX_URL == null )
            this.DESKTOP_RENDER_PREFIX_URL = jetspeed.url.basePortalUrl() + jetspeed.url.path.RENDER ;
        var op = null;
        var justTheUrl = contentUrl;
        var entityId = null;
        if ( contentUrl && contentUrl.length > this.DESKTOP_ACTION_PREFIX_URL.length && contentUrl.indexOf( this.DESKTOP_ACTION_PREFIX_URL ) == 0 )
        {   // annotate away javascript invocation in form action
            op = jetspeed.portleturl.PORTLET_REQUEST_ACTION;
        }
        else if ( contentUrl && contentUrl.length > this.DESKTOP_RENDER_PREFIX_URL.length && contentUrl.indexOf( this.DESKTOP_RENDER_PREFIX_URL ) == 0 )
        {
            op = jetspeed.portleturl.PORTLET_REQUEST_RENDER;
        }
        if ( op != null )
        {
            entityId = jetspeed.url.getQueryParameter( contentUrl, "entity" );
            //dojo.debug( "portlet-url op=" + op  + " entity=" + entityId + " url=" + contentUrl );  
        }
        
        if ( ! jetspeed.url.urlStartsWithHttp( justTheUrl ) )
            justTheUrl = null;

        return { url: justTheUrl, operation: op, portletEntityId: entityId };
    },

    genPseudoUrl: function( parsedPseudoUrl, makeDummy )   // generateJSPseudoUrlActionRender
    {   // NOTE: no form can be passed in one of these
        if ( ! parsedPseudoUrl || ! parsedPseudoUrl.url || ! parsedPseudoUrl.portletEntityId ) return null;
        var hrefJScolon = null;
        if ( makeDummy )
        {
            hrefJScolon = jetspeed.portleturl.JETSPEED_DO_NOTHING_ACTION;
        }
        else
        {
            hrefJScolon = "javascript:";
            var badnews = false;
            if ( parsedPseudoUrl.operation == jetspeed.portleturl.PORTLET_REQUEST_ACTION )
                hrefJScolon += "doAction(\"";
            else if ( parsedPseudoUrl.operation == jetspeed.portleturl.PORTLET_REQUEST_RENDER )
                hrefJScolon += "doRender(\"";
            else badnews = true;
            if ( badnews ) return null;
            hrefJScolon += parsedPseudoUrl.url + "\",\"" + parsedPseudoUrl.portletEntityId + "\"";
            hrefJScolon += ")";
        }
        return hrefJScolon;
    }

};

jetspeed.doNothingNav = function()
{   // replacing form actions with javascript: doNothingNav() is 
    // useful for preventing form submission in cases like: <a onclick="form.submit(); return false;" >
    // JSF h:commandLink uses the above anchor onclick practice
    false;
};

jetspeed.loadPortletDecorationStyles = function( portletDecorationName, jsPrefs, suppressFailover )
{
    var portletDecorationConfig = null;
    var portletDecorations = jsPrefs.portletDecorationsConfig;
    if ( portletDecorationName && portletDecorations )
        portletDecorationConfig = portletDecorations[ portletDecorationName ];
    
    if ( portletDecorationConfig == null && ! suppressFailover )
    {
        var portletDecorationsAllowed = jsPrefs.portletDecorationsAllowed;
        for ( var i = 0 ; i < portletDecorationsAllowed.length ; i++ )
        {
            portletDecorationName = portletDecorationsAllowed[ i ];
            portletDecorationConfig = portletDecorations[ portletDecorationName ];
            if ( portletDecorationConfig != null )
                break;
        }
    }

    if ( portletDecorationConfig != null && ! portletDecorationConfig._initialized )
    {
        var pdBaseUrl = jetspeed.prefs.getPortletDecorationBaseUrl( portletDecorationName );
        portletDecorationConfig._initialized = true;
        portletDecorationConfig.cssPathCommon = new dojo.uri.Uri( pdBaseUrl + "/css/styles.css" );
        portletDecorationConfig.cssPathDesktop = new dojo.uri.Uri( pdBaseUrl + "/css/desktop.css" );
        
        dojo.html.insertCssFile( portletDecorationConfig.cssPathCommon, null, true );
        dojo.html.insertCssFile( portletDecorationConfig.cssPathDesktop, null, true );
    }
    return portletDecorationConfig;
};
jetspeed.loadPortletDecorationConfig = function( portletDecorationName, jsPrefs, jsId )
{   // setup default portlet decoration config
    var pdConfig = {};
    jsPrefs.portletDecorationsConfig[ portletDecorationName ] = pdConfig;
    pdConfig.name = portletDecorationName;
    pdConfig.windowActionButtonOrder = jsPrefs.windowActionButtonOrder;
    pdConfig.windowActionNotPortlet = jsPrefs.windowActionNotPortlet;
    pdConfig.windowActionButtonMax = jsPrefs.windowActionButtonMax;
    pdConfig.windowActionButtonTooltip = jsPrefs.windowActionButtonTooltip;
    pdConfig.windowActionMenuOrder = jsPrefs.windowActionMenuOrder;
    pdConfig.windowActionNoImage = jsPrefs.windowActionNoImage;
    pdConfig.windowIconEnabled = jsPrefs.windowIconEnabled;
    pdConfig.windowIconPath = jsPrefs.windowIconPath;
    pdConfig.windowTitlebar = jsPrefs.windowTitlebar;
    pdConfig.windowResizebar = jsPrefs.windowResizebar;

    pdConfig.dNodeClass = jsId.P_CLASS + " " + portletDecorationName + " " + jsId.PWIN_CLASS + " " + jsId.PWIN_CLASS + "-" + portletDecorationName;
    pdConfig.cNodeClass = jsId.P_CLASS + " " + portletDecorationName + " " + jsId.PWIN_CLIENT_CLASS;

    if ( jsPrefs.portletDecorationsProperties )
    {
        var pdProps = jsPrefs.portletDecorationsProperties[ portletDecorationName ];
        if ( pdProps )
        {
            for ( var pDecNm in pdProps )
            {
                pdConfig[ pDecNm ] = pdProps[ pDecNm ];
            }
            if ( pdProps.windowActionNoImage != null )
            {
                var noImageMap = {};
                for ( var i = 0 ; i < pdProps.windowActionNoImage.length; i++ )
                {
                    noImageMap[ pdProps.windowActionNoImage[ i ] ] = true;
                }
                pdConfig.windowActionNoImage = noImageMap;
            }
            if ( pdProps.windowIconPath != null )
            {
                pdConfig.windowIconPath = dojo.string.trim( pdProps.windowIconPath );
                if ( pdConfig.windowIconPath == null || pdConfig.windowIconPath.length == 0 )
                    pdConfig.windowIconPath = null;
                else
                {
                    var winIconsPath = pdConfig.windowIconPath;
                    var firstCh = winIconsPath.charAt(0);
                    if ( firstCh != "/" )
                        winIconsPath = "/" + winIconsPath;
                    var lastCh = winIconsPath.charAt( winIconsPath.length -1 );
                    if ( lastCh != "/" )
                        winIconsPath = winIconsPath + "/";
                    pdConfig.windowIconPath = winIconsPath;
                }
            }
        }
    }
};

jetspeed.notifyRetrieveAllMenusFinished = function( isPageUpdate, initEditModeConf )
{   // dojo.event.connect to this or add to your page content, one of the functions that it invokes ( doMenuBuildAll() or doMenuBuild() )
    var jsObj = jetspeed;
    jsObj.pageNavigateSuppress = true;

    if ( dojo.lang.isFunction( window.doMenuBuildAll ) )
    {   
        window.doMenuBuildAll();
    }
    
    var menuNames = jsObj.page.getMenuNames();
    for ( var i = 0 ; i < menuNames.length; i++ )
    {
        var menuNm = menuNames[i];
        var menuWidget = dojo.widget.byId( jsObj.id.MENU_WIDGET_ID_PREFIX + menuNm );
        if ( menuWidget )
        {
            menuWidget.createJetspeedMenu( jsObj.page.getMenu( menuNm ) );
        }
    }
    if ( ! initEditModeConf )
        jsObj.url.loadingIndicatorHide();
    jsObj.pageNavigateSuppress = false;
};

jetspeed.notifyRetrieveMenuFinished = function( /* jetspeed.om.Menu */ menuObj )
{   // dojo.event.connect to this or add to your page content the function that it invokes ( doMenuBuild() )
    if ( dojo.lang.isFunction( window.doMenuBuild ) )
    {
        window.doMenuBuild( menuObj );
    }
};

jetspeed.menuNavClickWidget = function( /* Tab widget || Tab widgetId */ tabWidget, /* int || String */ selectedTab )
{
    //dojo.debug( "jetspeed.menuNavClick" );
    if ( ! tabWidget ) return;
    if ( dojo.lang.isString( tabWidget ) )
    {
        var tabWidgetId = tabWidget;
        tabWidget = dojo.widget.byId( tabWidgetId );
        if ( ! tabWidget )
            dojo.raise( "Tab widget not found: " + tabWidgetId );
    }
    if ( tabWidget )
    {
        var jetspeedMenuName = tabWidget.jetspeedmenuname;
        if ( ! jetspeedMenuName && tabWidget.extraArgs )
            jetspeedMenuName = tabWidget.extraArgs.jetspeedmenuname;
        if ( ! jetspeedMenuName )
            dojo.raise( "Tab widget is invalid: " + tabWidget.widgetId );
        var menuObj = jetspeed.page.getMenu( jetspeedMenuName );
        if ( ! menuObj )
            dojo.raise( "Tab widget " + tabWidget.widgetId + " no menu: " + jetspeedMenuName );
        var menuOpt = menuObj.getOptionByIndex( selectedTab );
        
        jetspeed.menuNavClick( menuOpt );
    }
};

jetspeed.pageNavigateSuppress = false;
jetspeed.pageNavigate = function( navUrl, navTarget, force )
{
    var jsObj = jetspeed;
    if ( ! navUrl || jsObj.pageNavigateSuppress ) return;

    if ( typeof force == "undefined" )
        force = false;

    if ( ! force && jsObj.page && jsObj.page.equalsPageUrl( navUrl ) )
        return ;

    navUrl = jsObj.page.makePageUrl( navUrl );
    
    if ( navTarget == "top" )
        top.location.href = navUrl;
    else if ( navTarget == "parent" )
        parent.location.href = navUrl;
    else
        window.location.href = navUrl;  // BOZO:NOW: popups
};

jetspeed.getActionsForPortlet = function( /* String */ portletEntityId )
{
    if ( portletEntityId == null ) return;
    jetspeed.getActionsForPortlets( [ portletEntityId ] );
};
jetspeed.getActionsForPortlets = function( /* Array */ portletEntityIds )
{
    var jsObj = jetspeed;
    if ( portletEntityIds == null )
        portletEntityIds = jsObj.page.getPortletIds();
    var contentListener = new jsObj.om.PortletActionsCL( portletEntityIds );
    var queryString = "?action=getactions";
    for ( var i = 0 ; i < portletEntityIds.length ; i++ )
    {
        queryString += "&id=" + portletEntityIds[i];
    }
    var getActionsUrl = jsObj.url.basePortalUrl() + jsObj.url.path.AJAX_API + jsObj.page.getPath() + queryString;
    var mimetype = "text/xml";
    var ajaxApiContext = new jsObj.om.Id( "getactions", { } );
    jsObj.url.retrieveContent( { url: getActionsUrl, mimetype: mimetype }, contentListener, ajaxApiContext, jsObj.debugContentDumpIds );
};
jetspeed.changeActionForPortlet = function( /* String */ portletEntityId, /* String */ changeActionState, /* String */ changeActionMode, contentListener, pagePathOverride )
{
    var jsObj = jetspeed;
    if ( portletEntityId == null ) return;
    if ( contentListener == null )
        contentListener = new jsObj.om.PortletChangeActionCL( portletEntityId );
    var queryString = "?action=window&id=" + ( portletEntityId != null ? portletEntityId : "" );
    if ( changeActionState != null )
        queryString += "&state=" + changeActionState;
    if ( changeActionMode != null )
        queryString += "&mode=" + changeActionMode;
    var pagePath = pagePathOverride ;
    if ( ! pagePath )
        pagePath = jsObj.page.getPath();
    var changeActionUrl = jsObj.url.basePortalUrl() + jsObj.url.path.AJAX_API + pagePath + queryString ;
    var mimetype = "text/xml";
    var ajaxApiContext = new jsObj.om.Id( "changeaction", { } );
    jsObj.url.retrieveContent( { url: changeActionUrl, mimetype: mimetype }, contentListener, ajaxApiContext, jsObj.debugContentDumpIds );
};

jetspeed.getUserInfo = function( sync )
{
    var jsObj = jetspeed;
    var contentListener = new jsObj.om.UserInfoCL();
    var queryString = "?action=getuserinfo";
    var getActionsUrl = jsObj.url.basePortalUrl() + jsObj.url.path.AJAX_API + jsObj.page.getPath() + queryString;
    var mimetype = "text/xml";
    var ajaxApiContext = new jsObj.om.Id( "getuserinfo", { } );
    jsObj.url.retrieveContent( { url: getActionsUrl, mimetype: mimetype, sync: sync }, contentListener, ajaxApiContext, jsObj.debugContentDumpIds );
};

jetspeed.editPageInitiate = function( jsObj, initEditModeConf )
{
    var jsPage = jsObj.page;
    if ( ! jsPage.editMode )
    {
        var jsCss = jsObj.css;
        var fromDesktop = true;
        var fromPortal = jsObj.url.getQueryParameter( window.location.href, jsObj.id.PORTAL_ORIGINATE_PARAMETER );
        if ( fromPortal != null && fromPortal == "true" )
            fromDesktop = false;
        jsPage.editMode = true;
        var pageEditorWidget = dojo.widget.byId( jsObj.id.PG_ED_WID );
        if ( jsObj.UAie6 )
            jsPage.displayAllPWins( true );
        var editModeMove = ( (initEditModeConf != null && initEditModeConf.editModeMove) ? true : false );
        var pperms = jsPage._perms(jsObj.prefs,-1,String.fromCharCode);
        if ( pperms && pperms[2] && pperms[2].length > 0 )
        {
            if ( ! jsObj.page._getU() )
            {
                jsObj.getUserInfo( true );
            }
        }
        if ( pageEditorWidget == null )
        {
            try
            {
                jsObj.url.loadingIndicatorShow( "loadpageeditor", true );
                pageEditorWidget = dojo.widget.createWidget( "jetspeed:PageEditor", { widgetId: jsObj.id.PG_ED_WID, editorInitiatedFromDesktop: fromDesktop, editModeMove: editModeMove } );
                var allColumnsContainer = document.getElementById( jsObj.id.COLUMNS );
                allColumnsContainer.insertBefore( pageEditorWidget.domNode, allColumnsContainer.firstChild );
            }
            catch (e)
            {
                jsObj.url.loadingIndicatorHide();
                if ( jsObj.UAie6 )
                    jsPage.displayAllPWins();
            }
        }
        else
        {
            pageEditorWidget.editPageShow();
        }
        jsPage.syncPageControls( jsObj );
    }
};
jetspeed.editPageTerminate = function( jsObj, changeActionToView )
{
    var jsPage = jsObj.page;
    if ( jsPage.editMode )
    {
        var mustNavUrl = null;
        var jsCss = jsObj.css;
        var pageEditorWidget = dojo.widget.byId( jsObj.id.PG_ED_WID );
        if ( pageEditorWidget != null && ! pageEditorWidget.editorInitiatedFromDesktop )
        {
            var portalPageUrl = jsPage.getPageUrl( true );
            portalPageUrl = jsObj.url.removeQueryParameter( portalPageUrl, jsObj.id.PG_ED_PARAM );
            portalPageUrl = jsObj.url.removeQueryParameter( portalPageUrl, jsObj.id.PORTAL_ORIGINATE_PARAMETER );
            mustNavUrl = portalPageUrl;
        }
        else
        {
            var pageEditorInititate = jsObj.url.getQueryParameter( window.location.href, jsObj.id.PG_ED_PARAM );
            if ( pageEditorInititate != null && pageEditorInititate == "true" )
            {   // because of parameter, we must navigate
                var dtPageUrl = window.location.href; // jsPage.getPageUrl( false );
                dtPageUrl = jsObj.url.removeQueryParameter( dtPageUrl, jsObj.id.PG_ED_PARAM );
                mustNavUrl = dtPageUrl;
            }
        }
        if ( mustNavUrl != null )
            mustNavUrl = mustNavUrl.toString();
        jsPage.editMode = false;
        jsObj.changeActionForPortlet( jsPage.rootFragmentId, null, jsObj.id.ACT_VIEW, new jsObj.om.PageChangeActionCL( mustNavUrl ) );
        if ( mustNavUrl == null )
        {
            if ( pageEditorWidget != null )
            {
                pageEditorWidget.editMoveModeExit( true );  // in case we're in move-mode
                pageEditorWidget.editPageHide();
            }
            jsPage.syncPageControls( jsObj );
        }
    }
};

// ... jetspeed.om.PortletContentRetriever
jetspeed.om.PortletContentRetriever = function()
{
};
jetspeed.om.PortletContentRetriever.prototype =
{   // /* Portlet */ portlet, /* String */ requestUrl, /* PortletCL */ portletCL
    getContent: function( bindArgs, contentListener, domainModelObject, /* String[] */ debugContentDumpIds )
    {
        if ( ! bindArgs )
            bindArgs = {};
        jetspeed.url.retrieveContent( bindArgs, contentListener, domainModelObject, debugContentDumpIds );
    }
};

// ... jetspeed.om.PageCLCreateWidget
jetspeed.om.PageCLCreateWidget = function( isPageUpdate, initEditModeConf )
{
    if ( typeof isPageUpdate == "undefined" )
        isPageUpdate = false ;
    this.isPageUpdate = isPageUpdate ;
    this.initEditModeConf = initEditModeConf;
};
jetspeed.om.PageCLCreateWidget.prototype =
{
    notifySuccess: function( /* XMLDocument */ data, /* String */ requestUrl, /* Page */ page )
    {
        page.loadFromPSML( data, this.isPageUpdate, this.initEditModeConf );
    },
    notifyFailure: function( /* String */ type, /* Object */ error, /* String */ requestUrl, /* Page */ page )
    {
        dojo.raise( "PageCLCreateWidget error url: " + requestUrl + " type: " + type + jetspeed.formatError( error ) );
    }
};

// ... jetspeed.om.Page
jetspeed.om.Page = function( requiredLayoutDecorator, navToPageUrl, addToHistory, tooltipMgr, iframeCoverByWinId )
{
    if ( requiredLayoutDecorator != null && navToPageUrl != null )
    {
        this.requiredLayoutDecorator = requiredLayoutDecorator;
        this.setPsmlPathFromDocumentUrl( navToPageUrl );
        this.pageUrlFallback = navToPageUrl;
    }
    else
    {
        this.setPsmlPathFromDocumentUrl();
    }
    if ( typeof addToHistory != "undefined" )
        this.addToHistory = addToHistory;
    this.layouts = {};
    this.columns = [];
    this.colFirstNormI = -1;
    this.portlets = {};
    this.portlet_count = 0;
    this.portlet_windows = {};
    this.portlet_window_count = 0;
    if ( iframeCoverByWinId != null )
        this.iframeCoverByWinId = iframeCoverByWinId;
    else
        this.iframeCoverByWinId = {};
    this.portlet_tiled_high_z = 10;
    this.portlet_untiled_high_z = -1;
    this.menus = [];
    
    if ( tooltipMgr != null )
        this.tooltipMgr = tooltipMgr;
    else
    {
        this.tooltipMgr = dojo.widget.createWidget( "jetspeed:PortalTooltipManager", { isContainer: false, fastMixIn: true } );
        // setting isContainer=false and fastMixIn=true to avoid recursion hell when connectId is a node (could give each an id instead)
        jetspeed.docBody.appendChild( this.tooltipMgr.domNode );
    }
};
dojo.lang.extend( jetspeed.om.Page,
{
    psmlPath: null,
    name: null,
    path: null,
    pageUrl: null,
    pagePathAndQuery: null,
    title: null,
    shortTitle: null,
    layoutDecorator: null,
    portletDecorator: null,
    uIA: true,   // userIsAnonymous

    requiredLayoutDecorator: null,
    pageUrlFallback: null,
    addToHistory: false,

    layouts: null,
    columns: null,
    portlets: null,
    portletsByPageColumn: null,

    editMode: false,
    themeDefinitions: null,

    menus: null,

    getId: function()  // jetspeed.om.Id protocol
    {
        var idsuffix = ( this.name != null && this.name.length > 0 ? this.name : null );
        if ( ! idsuffix )
        {
            this.getPsmlUrl();
            idsuffix = this.psmlPath;
        }
        return "page-" + idsuffix;
    },
    
    setPsmlPathFromDocumentUrl: function( navToPageUrl )
    {
        var jsObj = jetspeed;
        var psmlPath = jsObj.url.path.AJAX_API;
        var docPath = null;
        if ( navToPageUrl == null )
        {
            docPath = window.location.pathname;
            if ( ! djConfig.preventBackButtonFix && jsObj.prefs.ajaxPageNavigation )
            {
                var hash = window.location.hash;
                if ( hash != null && hash.length > 0 )
                {
                    if ( hash.indexOf( "#" ) == 0 )
                    {
                        hash = ( hash.length > 1 ? hash.substring(1) : "" );
                    }
                    if ( hash != null && hash.length > 1 && hash.indexOf( "/" ) == 0 )
                    {
                        this.psmlPath = jsObj.url.path.AJAX_API + hash;
                        return;
                    }
                }
            }
        }
        else
        {
            var uObj = jsObj.url.parse( navToPageUrl );
            docPath = uObj.path;
        }

        var contextAndServletPath = jsObj.url.path.DESKTOP;
        var contextAndServletPathPos = docPath.indexOf( contextAndServletPath );
        if ( contextAndServletPathPos != -1 && docPath.length > ( contextAndServletPathPos + contextAndServletPath.length ) )
        {
            psmlPath = psmlPath + docPath.substring( contextAndServletPathPos + contextAndServletPath.length );
        }
        this.psmlPath = psmlPath;
    },
    
    getPsmlUrl: function()
    {
        var jsObj = jetspeed;
        if ( this.psmlPath == null )
            this.setPsmlPathFromDocumentUrl();

        var psmlUrl = jsObj.url.basePortalUrl() + this.psmlPath;
        if ( jsObj.prefs.printModeOnly != null )
        {
            psmlUrl = jsObj.url.addQueryParameter( psmlUrl, "layoutid", jsObj.prefs.printModeOnly.layout );
            psmlUrl = jsObj.url.addQueryParameter( psmlUrl, "entity", jsObj.prefs.printModeOnly.entity ).toString();
        }
        return psmlUrl;
    },
    _setU: function( u ) { this._u = u; },
    _getU: function() { return this._u; },
    
    retrievePsml: function( pageContentListener )
    {
        var jsObj = jetspeed;
        if ( pageContentListener == null )
            pageContentListener = new jsObj.om.PageCLCreateWidget();

        var psmlUrl = this.getPsmlUrl() ;
        var mimetype = "text/xml";

        if ( jsObj.debug.retrievePsml )
            dojo.debug( "retrievePsml url: " + psmlUrl ) ;

        jsObj.url.retrieveContent( { url: psmlUrl, mimetype: mimetype }, pageContentListener, this, jsObj.debugContentDumpIds );
    },

    loadFromPSML: function( psml, isPageUpdate, initEditModeConf )
    {
        var jsObj = jetspeed;
        var jsPrefs = jsObj.prefs;
        var djObj = dojo;
        var printModeOnly = jsPrefs.printModeOnly ;
        if ( djConfig.isDebug && jsObj.debug.profile && printModeOnly == null )
        {
            djObj.profile.start( "loadFromPSML" );
        }

        // parse psml
        var parsedRootLayoutFragment = this._parsePSML( psml );
        jetspeed.rootfrag = parsedRootLayoutFragment;
        if ( parsedRootLayoutFragment == null ) return;

        // create layout model
        this.portletsByPageColumn = {};
        var portletDecorationsUsed = {};
        if ( this.portletDecorator )
            portletDecorationsUsed[ this.portletDecorator ] = true;
        this.columnsStructure = this._layoutCreateModel( parsedRootLayoutFragment, 0, null, this.portletsByPageColumn, true, portletDecorationsUsed, djObj, jsObj );

        this.rootFragmentId = parsedRootLayoutFragment.id ;

        this.editMode = false;

        // load portlet decorator css
        for ( var pDecNm in portletDecorationsUsed )
        {
            jsObj.loadPortletDecorationStyles( pDecNm, jsPrefs, true );
        }

        // create columns
        if ( jsPrefs.windowTiling )
        {
            this._createColsStart( document.getElementById( jsObj.id.DESKTOP ), jsObj.id.COLUMNS );
        }
        this.createLayoutInfo( jsObj );
        
        var portletArray = this.portletsByPageColumn[ "z" ];
        if ( portletArray )
        {
            portletArray.sort( this._loadPortletZIndexCompare );
        }

        if ( typeof initEditModeConf == "undefined" ) initEditModeConf = null;
        //var pageEditorInititate = null;   // ( pageEditorInititate != null && pageEditorInititate == "true" )
        if ( initEditModeConf != null || ( this.actions != null && this.actions[ jsObj.id.ACT_VIEW ] != null ) )
        {
            if ( ! this.isUA() && this.actions != null && ( this.actions[ jsObj.id.ACT_EDIT ] != null || this.actions[ jsObj.id.ACT_VIEW ] != null ) )
            {
                if ( initEditModeConf == null )
                    initEditModeConf = {};

                if ( (typeof initEditModeConf.editModeMove == "undefined") && this._perms(jsPrefs,jsObj.id.PM_MZ_P,String.fromCharCode) )
                    initEditModeConf.editModeMove = true;

                var winUrl = jsObj.url.parse( window.location.href );                
                if ( ! initEditModeConf.editModeMove )
                {
                    var peState = jsObj.url.getQueryParameter( winUrl, jsObj.id.PG_ED_STATE_PARAM );
                    if ( peState != null )
                    {
                        peState = "0x" + peState;
                        if ( (peState & jsObj.id.PM_MZ_P) > 0 )
                            initEditModeConf.editModeMove = true;
                    }
                }
                if ( initEditModeConf.editModeMove && ! initEditModeConf.windowTitles )
                {
                    var winTitles = jsObj.url.getQueryParameter( winUrl, jsObj.id.PG_ED_TITLES_PARAM );
                    if ( winTitles != null )
                    {
                        var winTitlesLen = winTitles.length;
                        var winTitlesChars = new Array( winTitlesLen / 2 );
                        var sfcc = String.fromCharCode;
                        var wtChI = 0, chI = 0;
                        while ( chI < (winTitlesLen-1) )
                        {
                            winTitlesChars[wtChI] = sfcc( Number("0x" + winTitles.substring( chI, (chI +2) ) ) );
                            wtChI++;
                            chI += 2;
                        }
                        var winTitlesObj = null;
                        try
                        {
                            winTitlesObj = eval( "({" + winTitlesChars.join("") + "})" );
                        }
                        catch(e)
                        {
                            if ( djConfig.isDebug )
                                dojo.debug( "cannot parse json: " + winTitlesChars.join("") );
                        }
                        if ( winTitlesObj != null )
                        {
                            var missingTitle = false;
                            for ( var portletIndex in this.portlets )
                            {
                                var portlet = this.portlets[portletIndex];
                                if ( portlet != null && ! winTitlesObj[ portlet.entityId ] )
                                {
                                    missingTitle = true;
                                    break;
                                }
                            }
                            if ( ! missingTitle )
                                initEditModeConf.windowTitles = winTitlesObj;
                        }
                    }
                }
            }
            else
            {
                initEditModeConf = null;
            }
        }
        if ( initEditModeConf != null )
        {   // bring up early
            jsObj.url.loadingIndicatorShow( "loadpageeditor", true );
        }

        var renderer = new jsObj.PortletRenderer( true, true, isPageUpdate, null, true, initEditModeConf );
        renderer.renderAllTimeDistribute();
    },

    loadPostRender: function( isPageUpdate, initEditModeConf )
    {
        var jsObj = jetspeed;
        var printModeOnly = jsObj.prefs.printModeOnly ;
        if ( printModeOnly == null )
        {
            this._portletsInitWinState( this.portletsByPageColumn[ "z" ] );
    
            // load menus
            this.retrieveMenuDeclarations( true, isPageUpdate, initEditModeConf );
        }
        else
        {
            for ( var portletIndex in this.portlets )
            {
                var portlet = this.portlets[portletIndex];
                if ( portlet != null )
                    portlet.renderAction( null, printModeOnly.action );
                break;
            }
            if ( isPageUpdate )
                jsObj.updatePageEnd() ;
        }

        // window resize
        jsObj.ui.evtConnect( "after", window, "onresize", jsObj.ui.windowResizeMgr, "onResize", dojo.event );
        jsObj.ui.windowResizeMgr.onResizeDelayedCompare();   // in case resize occurred while loading

        var colNode, columnObjArray = this.columns;
        if ( columnObjArray )
        {   // find empty columns and set their height to 1px
            for ( var i = 0 ; i < columnObjArray.length ; i++ )
            {
                colNode = columnObjArray[i].domNode;
                if ( ! colNode.childNodes || colNode.childNodes.length == 0 )
                {
                    colNode.style.height = "1px";
                }
            }
        }

        var maxOnInitId = this.maximizedOnInit;
        if ( maxOnInitId != null )
        {
            var pWinToMax = this.getPWin( maxOnInitId );
            if ( pWinToMax == null )
                dojo.raise( "no pWin to max" );
            else
                dojo.lang.setTimeout( pWinToMax, pWinToMax._postCreateMaximizeWindow, 500 );
            this.maximizedOnInit = null;
        }

        dojo.lang.setTimeout( jsObj.url, jsObj.url.loadingIndicatorStepPreload, 1800 );
    },

    loadPostRetrieveMenus: function( isPageUpdate, initEditModeConf )
    {
        var jsObj = jetspeed;
        this.renderPageControls( jsObj );

        if ( initEditModeConf )
            jsObj.editPageInitiate( jsObj, initEditModeConf );

        if ( isPageUpdate )
            jsObj.updatePageEnd();

        this.syncPageControls( jsObj );
    },
    
    _parsePSML: function( psml )
    {
        var jsObj = jetspeed;
        var djObj = dojo;
        var pageElements = psml.getElementsByTagName( "page" );
        if ( ! pageElements || pageElements.length > 1 || pageElements[0] == null )
            djObj.raise( "<page>" );
        var pageElement = pageElements[0];
        var children = pageElement.childNodes;
        var simpleValueLNames = new RegExp( "(name|path|profiledPath|title|short-title|uIA|npe)" );
        var rootFragment = null;
        var rootFragmentActions = {};
        for ( var i = 0 ; i < children.length ; i++ )
        {
            var child = children[i];
            if ( child.nodeType != 1 )   // djObj.dom.ELEMENT_NODE
                continue;
            var childLName = child.nodeName;
            if ( childLName == "fragment" )
            {
                rootFragment = child;
            }
            else if ( childLName == "defaults" )
            {
                this.layoutDecorator = child.getAttribute( "layout-decorator" );
                var defaultpd = child.getAttribute( "portlet-decorator" );
                var pdsAllowed = jsObj.prefs.portletDecorationsAllowed;
                if ( ! pdsAllowed || djObj.lang.indexOf( pdsAllowed, defaultpd ) == -1 )
                    defaultpd = jsObj.prefs.windowDecoration;
                this.portletDecorator = defaultpd;
            }
            else if ( childLName && childLName.match( simpleValueLNames  ) )
            {
                if ( childLName == "short-title" )
                    childLName = "shortTitle";
                this[ childLName ] = ( ( child && child.firstChild ) ? child.firstChild.nodeValue : null );
            }
            else if ( childLName == "action" )
            {
                this._parsePSMLAction( child, rootFragmentActions ) ;
            }
        }
        this.actions = rootFragmentActions;

        if ( rootFragment == null )
        {
            djObj.raise( "root frag" );
            return null;
        }
        if ( this.requiredLayoutDecorator != null && this.pageUrlFallback != null )
        {
            if ( this.layoutDecorator != this.requiredLayoutDecorator )
            {
                if ( jsObj.debug.ajaxPageNav ) 
                    djObj.debug( "ajaxPageNavigation _parsePSML different layout decorator (" + this.requiredLayoutDecorator + " != " + this.layoutDecorator + ") - fallback to normal page navigation - " + this.pageUrlFallback );
                jsObj.pageNavigate( this.pageUrlFallback, null, true );
                return null;
            }
            else if ( this.addToHistory )
            {
                var currentPageUrl = this.getPageUrl();
                djObj.undo.browser.addToHistory({
	    	        back: function() { if ( jsObj.debug.ajaxPageNav ) dojo.debug( "back-nav-button: " + currentPageUrl ); jsObj.updatePage( currentPageUrl, true ); },
		            forward: function() { if ( jsObj.debug.ajaxPageNav ) dojo.debug( "forward-nav-button: " + currentPageUrl ); jsObj.updatePage( currentPageUrl, true ); },
		            changeUrl: escape( this.getPath() )
		        });
            }
        }
        else if ( ! djConfig.preventBackButtonFix && jsObj.prefs.ajaxPageNavigation )
        {
            var currentPageUrl = this.getPageUrl();
            djObj.undo.browser.setInitialState({
                back: function() { if ( jsObj.debug.ajaxPageNav ) dojo.debug( "back-nav-button initial: " + currentPageUrl ); jsObj.updatePage( currentPageUrl, true ); },
                forward: function() { if ( jsObj.debug.ajaxPageNav ) dojo.debug( "forward-nav-button initial: " + currentPageUrl ); jsObj.updatePage( currentPageUrl, true ); },
                changeUrl: escape( this.getPath() )
            });
        }

        var parsedRootLayoutFragment = this._parsePSMLFrag( rootFragment, 0, false );    // rootFragment must be a layout fragment - /portal requires this as well
        return parsedRootLayoutFragment;
    },
    _parsePSMLFrag: function( layoutNode, layoutNodeDocumentOrderIndex, layoutActionsDisabled )
    {
        var jsObj = jetspeed;
        var fragChildren = new Array();
        var layoutFragType = ( (layoutNode != null) ? layoutNode.getAttribute( "type" ) : null );
        if ( layoutFragType != "layout" )
        {
            dojo.raise( "!layout frag=" + layoutNode );
            return null;
        }
        
        if ( ! layoutActionsDisabled )
        {
            var layoutFragNameAttr = layoutNode.getAttribute( "name" );
            if ( layoutFragNameAttr != null )
            {
                layoutFragNameAttr = layoutFragNameAttr.toLowerCase();
                if ( layoutFragNameAttr.indexOf( "noactions" ) != -1 )
                {
                    layoutActionsDisabled = true;
                }
            }
        }

        var sizes = null, sizesSum = 0;
        var propertiesMap = {};
        var children = layoutNode.childNodes;
        var child, childLName, propName, propVal, fragType;
        for ( var i = 0 ; i < children.length ; i++ )
        {
            child = children[i];
            if ( child.nodeType != 1 )   // dojo.dom.ELEMENT_NODE
                continue;
            childLName = child.nodeName;
            if ( childLName == "fragment" )
            {
                fragType = child.getAttribute( "type" );
                if ( fragType == "layout" )
                {
                    var parsedLayoutChildFragment = this._parsePSMLFrag( child, i, layoutActionsDisabled );
                    if ( parsedLayoutChildFragment != null )
                    {
                        fragChildren.push( parsedLayoutChildFragment ) ;
                    }
                }
                else
                {
                    var portletProps = this._parsePSMLProps( child, null );
                    var portletIcon = portletProps[ jsObj.id.PP_WINDOW_ICON ];
                    if ( portletIcon == null || portletIcon.length == 0 )
                    {
                        portletIcon = this._parsePSMLChildOrAttr( child, "icon" );
                        if ( portletIcon != null && portletIcon.length > 0 )
                        {
                            portletProps[ jsObj.id.PP_WINDOW_ICON ] = portletIcon;
                        }
                    }
                    fragChildren.push( { id: child.getAttribute( "id" ), type: fragType, name: child.getAttribute( "name" ), properties: portletProps, actions: this._parsePSMLActions( child, null ), currentActionState: this._parsePSMLChildOrAttr( child, "state" ), currentActionMode: this._parsePSMLChildOrAttr( child, "mode" ), decorator: child.getAttribute( "decorator" ), layoutActionsDisabled: layoutActionsDisabled, documentOrderIndex: i } );
                }
            }
            else if ( childLName == "property" )
            {
                if ( this._parsePSMLProp( child, propertiesMap ) == "sizes" )
                {
                    if ( sizes != null )
                    {
                        dojo.raise( "<sizes>: " + layoutNode );
                        return null;
                    }
                    if ( jsObj.prefs.printModeOnly != null )
                    {
                        sizes = [ "100" ];
                        sizesSum = 100;
                    }
                    else
                    {
                        propVal = child.getAttribute( "value" );
                        if ( propVal != null && propVal.length > 0 )
                        {
                            sizes = propVal.split( "," );
                            for ( var j = 0 ; j < sizes.length ; j++ )
                            {
                                var re = /^[^0-9]*([0-9]+)[^0-9]*$/;
                                sizes[j] = sizes[j].replace( re, "$1" );
                                sizesSum += new Number( sizes[j] );
                            }
                        }
                    }
                }
            }
        }
        
        if ( sizes == null )
        {
            sizes = [ "100" ];
            sizesSum = 100;
        }

        var colCount = sizes.length;
        var fragChildCount = fragChildren.length;
        var pCi = jsObj.id.PP_COLUMN;
        var pRi = jsObj.id.PP_ROW;
        var colLinkedLists = new Array( colCount );
        var colLinkedListsInfo = new Array( colCount );
        for ( var cI = 0 ; cI < colCount ; cI++ )
        {
            colLinkedLists[cI] = [];
            colLinkedListsInfo[cI] = { head: -1, tail: -1, high: -1 };
        }
        for ( var fragChildIndex = 0 ; fragChildIndex < fragChildCount ; fragChildIndex++ )
        {
            var frag = fragChildren[fragChildIndex];
            var fragProps = frag.properties;
            var col = fragProps[pCi];
            var row = fragProps[pRi];
            //jsObj.println( "  [" + fragChildIndex + "] col=" + col + " row=" + row + " doc=" + frag.documentOrderIndex + " " + frag.type + "/" + frag.id );
            var setCol = null;
            if ( col == null || col >= colCount )
                setCol = colCount - 1;
            else if ( col < 0 )
                setCol = 0;
            if ( setCol != null )
                col = fragProps[pCi] = String(setCol);

            var ll = colLinkedLists[col];
            var llLen = ll.length;
            var llInfo = colLinkedListsInfo[col];
            if ( row < 0 )
                row = fragProps[pRi] = 0;
            else if ( row == null )
                row = llInfo.high + 1;

            var fragLLentry = { i: fragChildIndex, row: row, next: -1 };
            ll.push( fragLLentry );
            if ( llLen == 0 )
            {
                llInfo.head = llInfo.tail = 0;
                llInfo.high = row;
            }
            else
            {
                if ( row > llInfo.high )
                {
                    ll[llInfo.tail].next = llLen;
                    llInfo.high = row;
                    llInfo.tail = llLen;
                }
                else
                {
                    var llEntryIndex = llInfo.head;
                    var llPrevEntryIndex = -1;
                    while ( ll[llEntryIndex].row < row )
                    {
                        llPrevEntryIndex = llEntryIndex;
                        llEntryIndex = ll[llEntryIndex].next;
                    }
                    if ( ll[llEntryIndex].row == row )
                    {
                        var incrementedRow = new Number( row ) + 1;
                        ll[llEntryIndex].row = incrementedRow;
                        if ( llInfo.tail == llEntryIndex )
                            llInfo.high = incrementedRow;
                    }
                    fragLLentry.next = llEntryIndex;
                    if ( llPrevEntryIndex == -1 )
                        llInfo.head = llLen;
                    else
                        ll[llPrevEntryIndex].next = llLen;
                }
            }
        }

        var sortedFragChildren = new Array( fragChildCount );
        var nextFragIndex = 0;
        for ( var cI = 0 ; cI < colCount ; cI++ )
        {
            var ll = colLinkedLists[cI];
            var llInfo = colLinkedListsInfo[cI];
            
            var nextRow = 0;
            var nextEntryIndex = llInfo.head;
            while ( nextEntryIndex != -1 )
            {
                var fragLLentry = ll[nextEntryIndex];
                var frag = fragChildren[fragLLentry.i];
                sortedFragChildren[nextFragIndex] = frag;
                frag.properties[pRi] = nextRow;

                //jsObj.println( "  [" + nextFragIndex + "] col=" + cI + " row=" + nextRow + " doc=" + frag.documentOrderIndex + " " + frag.type + "/" + frag.id );
                nextFragIndex++;
                nextRow++;
                nextEntryIndex = fragLLentry.next;
            }
        }

        return { id: layoutNode.getAttribute( "id" ), type: layoutFragType, name: layoutNode.getAttribute( "name" ), decorator: layoutNode.getAttribute( "decorator" ), columnSizes: sizes, columnSizesSum: sizesSum, properties: propertiesMap, fragments: sortedFragChildren, layoutActionsDisabled: layoutActionsDisabled, documentOrderIndex: layoutNodeDocumentOrderIndex };
    },
    _parsePSMLActions: function( fragmentNode, actionsMap )
    {
        if ( actionsMap == null )
            actionsMap = {};
        var actionChildren = fragmentNode.getElementsByTagName( "action" );
        for( var actionsIdx=0; actionsIdx < actionChildren.length; actionsIdx++ )
        {
            var actionNode = actionChildren[actionsIdx];
            this._parsePSMLAction( actionNode, actionsMap );
        }
        return actionsMap;
    },
    _parsePSMLAction: function( actionNode, actionsMap )
    {
        var actionName = actionNode.getAttribute( "id" );
        if ( actionName != null )
        {
            var actionType = actionNode.getAttribute( "type" );
            var actionLabel = actionNode.getAttribute( "name" );
            var actionUrl = actionNode.getAttribute( "url" );
            var actionAlt = actionNode.getAttribute( "alt" );
            actionsMap[ actionName.toLowerCase() ] = { id: actionName, type: actionType, label: actionLabel, url: actionUrl, alt: actionAlt };
        }
    },
    _parsePSMLChildOrAttr: function( fragmentNode, propName )
    {
        var propVal = null;
        var nodes = fragmentNode.getElementsByTagName( propName );
        if ( nodes != null && nodes.length == 1 && nodes[0].firstChild != null )
            propVal = nodes[0].firstChild.nodeValue;
        if ( ! propVal )
            propVal = fragmentNode.getAttribute( propName );
        if ( propVal == null || propVal.length == 0 )
            propVal = null;
        return propVal;
    },
    _parsePSMLProps: function( fragmentNode, propertiesMap )
    {
        if ( propertiesMap == null )
            propertiesMap = {};
        var props = fragmentNode.getElementsByTagName( "property" );
        for( var propsIdx=0; propsIdx < props.length; propsIdx++ )
        {
            this._parsePSMLProp( props[propsIdx], propertiesMap );
        }
        return propertiesMap;
    },
    _parsePSMLProp: function( propertyNode, propertiesMap )
    {
        var propName = propertyNode.getAttribute( "name" );
        var propValue = propertyNode.getAttribute( "value" );
        propertiesMap[ propName ] = propValue;
        return propName;
    },

    _layoutCreateModel: function( layoutFragment, depth, parentColumn, portletsByPageColumn, omitLayoutHeader, portletDecorationsUsed, djObj, jsObj )
    {
        var jsId = jsObj.id;
        var allColumnsStartIndex = this.columns.length;
        var colModelResult = this._layoutCreateColsModel( layoutFragment, depth, parentColumn, omitLayoutHeader );
        var columnsInLayout = colModelResult.columnsInLayout;
        if ( colModelResult.addedLayoutHeaderColumn )
            allColumnsStartIndex++;
        var columnsInLayoutLen = ( columnsInLayout == null ? 0 : columnsInLayout.length ) ;

        var portletsByLayoutColumn = new Array(columnsInLayoutLen);
        var columnsInLayoutPopulated = new Array(columnsInLayoutLen);
        for ( var i = 0 ; i < layoutFragment.fragments.length ; i++ )
        {
            var childFrag = layoutFragment.fragments[ i ];

            //dojo.debug( "layout-create[" + layoutFragment.id + "][" + i + "] - " + childFrag.type + " id=" + childFrag.id + " row=" + childFrag.properties[ "row" ] + " col=" + childFrag.properties["column"]);

            if ( childFrag.type == "layout" )
            {
                var childFragInColIndex = i;
                var childFragInColIndex = ( childFrag.properties ? childFrag.properties[ jsObj.id.PP_COLUMN ] : i );
                if ( childFragInColIndex == null || childFragInColIndex < 0 || childFragInColIndex >= columnsInLayoutLen )
                    childFragInColIndex = ( columnsInLayoutLen > 0 ? ( columnsInLayoutLen -1 ) : 0 );
                columnsInLayoutPopulated[ childFragInColIndex ] = true;
                this._layoutCreateModel( childFrag, (depth + 1), columnsInLayout[childFragInColIndex], portletsByPageColumn, false, portletDecorationsUsed, djObj, jsObj ) ;
            }
            else
            {
                this._layoutCreatePortlet( childFrag, layoutFragment, columnsInLayout, allColumnsStartIndex, portletsByPageColumn, portletsByLayoutColumn, portletDecorationsUsed, djObj, jsObj ) ;
            }
        }
        return columnsInLayout;
    },  // _layoutCreateModel

    _layoutCreatePortlet: function( pFrag, layoutFragment, columnsInLayout, pageColumnStartIndex, portletsByPageColumn, portletsByLayoutColumn, portletDecorationsUsed, djObj, jsObj )
    {
        if ( pFrag && jsObj.debugPortletEntityIdFilter )
        {
            if ( ! djObj.lang.inArray( jsObj.debugPortletEntityIdFilter, pFrag.id ) )
                pFrag = null;
        }
        if ( pFrag )
        {
            var pageColumnKey = "z";
            var extendedProperty = pFrag.properties[ jsObj.id.PP_DESKTOP_EXTENDED ];
            
            var tilingEnabled = jsObj.prefs.windowTiling;
            var posStatic = tilingEnabled;
            var heightToFit = jsObj.prefs.windowHeightExpand;
            if ( extendedProperty != null && tilingEnabled && jsObj.prefs.printModeOnly == null )
            {
                var extPropData = extendedProperty.split( jsObj.id.PP_PAIR_SEPARATOR );
                var extProp = null, extPropLen = 0, extPropName = null, extPropValue = null, extPropFlag = false;
                if ( extPropData != null && extPropData.length > 0 )
                {
                    var propSeparator = jsObj.id.PP_PROP_SEPARATOR;
                    for ( var extPropIndex = 0 ; extPropIndex < extPropData.length ; extPropIndex++ )
                    {
                        extProp = extPropData[extPropIndex];
                        extPropLen = ( ( extProp != null ) ? extProp.length : 0 );
                        if ( extPropLen > 0 )
                        {
                            var eqPos = extProp.indexOf( propSeparator );
                            if ( eqPos > 0 && eqPos < (extPropLen-1) )
                            {
                                extPropName = extProp.substring( 0, eqPos );
                                extPropValue = extProp.substring( eqPos +1 );
                                extPropFlag = ( ( extPropValue == "true" ) ? true : false );
                                if ( extPropName == jsObj.id.PP_STATICPOS )
                                    posStatic = extPropFlag;
                                else if ( extPropName == jsObj.id.PP_FITHEIGHT )
                                    heightToFit = extPropFlag;
                            }
                        }
                    }
                }
            }
            else if ( ! tilingEnabled )
            {
                posStatic = false;
            }
            pFrag.properties[ jsObj.id.PP_WINDOW_POSITION_STATIC ] = posStatic;
            pFrag.properties[ jsObj.id.PP_WINDOW_HEIGHT_TO_FIT ] = heightToFit;
            
            if ( posStatic && tilingEnabled )
            {
                var colCount = columnsInLayout.length;
                var portletColumnIndex = pFrag.properties[ jsObj.id.PP_COLUMN ];
                if ( portletColumnIndex == null || portletColumnIndex >= colCount )
                    portletColumnIndex = colCount -1;
                else if ( portletColumnIndex < 0 )
                    portletColumnIndex = 0;
                if ( portletsByLayoutColumn[portletColumnIndex] == null )
                    portletsByLayoutColumn[portletColumnIndex] = new Array();
                portletsByLayoutColumn[portletColumnIndex].push( pFrag.id );
                var portletPageColumnIndex = pageColumnStartIndex + new Number( portletColumnIndex );
                pageColumnKey = portletPageColumnIndex.toString();
            }
            if ( pFrag.currentActionState == jsObj.id.ACT_MAXIMIZE )
            {
                this.maximizedOnInit = pFrag.id;
            }
            var pDecNm = pFrag.decorator;
            if ( pDecNm != null && pDecNm.length > 0 )
            {
                if ( djObj.lang.indexOf( jsObj.prefs.portletDecorationsAllowed, pDecNm ) == -1 )
                    pDecNm = null;
            }
            if ( pDecNm == null || pDecNm.length == 0 )
            {
                if ( djConfig.isDebug && jsObj.debug.windowDecorationRandom )
                    pDecNm = jsObj.prefs.portletDecorationsAllowed[ Math.floor( Math.random() * jsObj.prefs.portletDecorationsAllowed.length ) ];
                else
                    pDecNm = this.portletDecorator;
            }
            var pProps = pFrag.properties || {};
            pProps[ jsObj.id.PP_WINDOW_DECORATION ] = pDecNm;
            portletDecorationsUsed[ pDecNm ] = true;
            var pActions = pFrag.actions || {};

            var portlet = new jsObj.om.Portlet( pFrag.name, pFrag.id, null, pProps, pActions, pFrag.currentActionState, pFrag.currentActionMode, pFrag.layoutActionsDisabled );
            portlet.initialize();

            this.putPortlet( portlet ) ;

            if ( portletsByPageColumn[ pageColumnKey ] == null )
            {
                portletsByPageColumn[ pageColumnKey ] = new Array();
            }
            portletsByPageColumn[ pageColumnKey ].push( { portlet: portlet, layout: layoutFragment.id } );
        }
    },  // _layoutCreatePortlet

    _layoutCreateColsModel: function( layoutFragment, depth, parentColumn, omitLayoutHeader )
    {
        var jsObj = jetspeed;
        this.layouts[ layoutFragment.id ] = layoutFragment;
        var addedLayoutHeaderColumn = false;
        var columnsInLayout = new Array();
        if ( jsObj.prefs.windowTiling && layoutFragment.columnSizes.length > 0 )
        {
            var subOneLast = false;
            if ( jsObj.UAie ) // IE can't deal with 100% here on any nested column - so subtract 0.1% - bug not fixed in IE7
                subOneLast = true;
            
            if ( parentColumn != null && ! omitLayoutHeader )
            {
                var layoutHeaderColModelObj = new jsObj.om.Column( 0, layoutFragment.id, ( subOneLast ? layoutFragment.columnSizesSum-0.1 : layoutFragment.columnSizesSum ), this.columns.length, layoutFragment.layoutActionsDisabled, depth );
                layoutHeaderColModelObj.layoutHeader = true;
                this.columns.push( layoutHeaderColModelObj );
                if ( parentColumn.buildColChildren == null )
                    parentColumn.buildColChildren = new Array();
                parentColumn.buildColChildren.push( layoutHeaderColModelObj );
                parentColumn = layoutHeaderColModelObj;
                addedLayoutHeaderColumn = true;
            }
            
            for ( var i = 0 ; i < layoutFragment.columnSizes.length ; i++ )
            {
                var size = layoutFragment.columnSizes[i];
                if ( subOneLast && i == (layoutFragment.columnSizes.length - 1) )
                    size = size - 0.1;
                var colModelObj = new jsObj.om.Column( i, layoutFragment.id, size, this.columns.length, layoutFragment.layoutActionsDisabled );
                this.columns.push( colModelObj );
                if ( parentColumn != null )
                {
                    if ( parentColumn.buildColChildren == null )
                        parentColumn.buildColChildren = new Array();
                    parentColumn.buildColChildren.push( colModelObj );
                }
                columnsInLayout.push( colModelObj );
            }
        }
        return { columnsInLayout: columnsInLayout, addedLayoutHeaderColumn: addedLayoutHeaderColumn };
    },  // _layoutCreateColsModel

    _portletsInitWinState: function( /* Array */ portletsByPageColumnZ )
    {
        var initialColumnRowAllPortlets = {};
        this.getPortletCurColRow( null, false, initialColumnRowAllPortlets );
        for ( var portletIndex in this.portlets )
        {
            var portlet = this.portlets[portletIndex];
            var portletInitialColRow = initialColumnRowAllPortlets[ portlet.getId() ];
            if ( portletInitialColRow == null && portletsByPageColumnZ )
            {
                for ( var i = 0 ; i < portletsByPageColumnZ.length ; i++ )
                {
                    if ( portletsByPageColumnZ[i].portlet.getId() == portlet.getId() )
                    {
                        portletInitialColRow = { layout: portletsByPageColumnZ[i].layout };
                        // NOTE: if portlet is put in tiling mode it should be placed in the bottom row of column 0 of layout
                        break;
                    }
                }
            }
            if ( portletInitialColRow != null )
                portlet._initWinState( portletInitialColRow, false );
            else
                dojo.raise( "Window state data not found for portlet: " + portlet.getId() );
        }
    },

    _loadPortletZIndexCompare: function( portletA, portletB )
    {
        var aZIndex = null;
        var bZIndex = null;
        var windowState = null;
        aZIndex = portletA.portlet._getInitialZIndex();
        bZIndex = portletB.portlet._getInitialZIndex();
        if ( aZIndex && ! bZIndex )
            return -1;
        else if ( bZIndex && ! aZIndex )
            return 1;
        else if ( aZIndex == bZIndex )
            return 0;
        return ( aZIndex - bZIndex );
    },

    _createColsStart: function( allColumnsParent, colContainerNodeId )
    {
        if ( ! this.columnsStructure || this.columnsStructure.length == 0 ) return;
        var columnContainerNode = document.createElement( "div" );
        columnContainerNode.id = colContainerNodeId;
        columnContainerNode.setAttribute( "id", colContainerNodeId );
        for ( var colIndex = 0 ; colIndex < this.columnsStructure.length ; colIndex++ )
        {
            var colObj = this.columnsStructure[colIndex];
            this._createCols( colObj, columnContainerNode ) ;
        }
        allColumnsParent.appendChild( columnContainerNode );
    },

    _createCols: function( column, columnContainerNode )
    {
        column.createColumn() ;
        if ( this.colFirstNormI == -1 && ! column.columnContainer && ! column.layoutHeader )
            this.colFirstNormI = column.getPageColumnIndex();
        var buildColChildren = column.buildColChildren;
        if ( buildColChildren != null && buildColChildren.length > 0 )
        {
            for ( var colIndex = 0 ; colIndex < buildColChildren.length ; colIndex++ )
            {
                this._createCols( buildColChildren[ colIndex ], column.domNode ) ;
            }
        }
        delete column.buildColChildren;
        columnContainerNode.appendChild( column.domNode );
    },
    _removeCols: function( /* DOM Node */ preserveWindowNodesInNode )
    {
        if ( ! this.columns || this.columns.length == 0 ) return;
        for ( var i = 0 ; i < this.columns.length ; i++ )
        {
            if ( this.columns[i] )
            {
                if ( preserveWindowNodesInNode )
                {
                    var windowNodesInColumn = jetspeed.ui.getPWinAndColChildren( this.columns[i].domNode, null );
                    dojo.lang.forEach( windowNodesInColumn,
                        function( windowNode ) { preserveWindowNodesInNode.appendChild( windowNode ); } );
                }
                dojo.dom.removeNode( this.columns[i] );
                this.columns[i] = null;
            }
        }
        var columnContainerNode = dojo.byId( jetspeed.id.COLUMNS );
        if ( columnContainerNode )
            dojo.dom.removeNode( columnContainerNode );
        this.columns = [];
    },
    getColumnDefault: function()
    {
        if ( this.colFirstNormI != -1 )
            return this.columns[ this.colFirstNormI ];
        return null;
    },
    columnsEmptyCheck: function( /* DOM node */ parentNode )
    {
        var isEmpty = null;
        if ( parentNode == null ) return isEmpty;
        var parentChildren = parentNode.childNodes, child;
        if ( parentChildren )
        {
            for ( var i = 0 ; i < parentChildren.length ; i++ )
            {
                child = parentChildren[i];
                var colEmptyCheck = this.columnEmptyCheck( child, true );
                if ( colEmptyCheck != null )
                {
                    isEmpty = colEmptyCheck;
                    if ( isEmpty == false )
                        break;
                }
            }
        }
        return isEmpty;
    },
    columnEmptyCheck: function( /* DOM node */ colDomNode, suppressStyleChange )
    {
        var isEmpty = null;
        if ( ! colDomNode || ! colDomNode.getAttribute ) return isEmpty;
        var pageColIndexStr = colDomNode.getAttribute( "columnindex" );
        if ( ! pageColIndexStr || pageColIndexStr.length == 0 ) return isEmpty;
        var layoutId = colDomNode.getAttribute( "layoutid" );
        if ( layoutId == null || layoutId.length == 0 )
        {   // colDomNode has been verified to be a column
            //   verification is done to allow this method to be a no-op if node arg is not a true column
            var colChildren = colDomNode.childNodes;
            isEmpty = ( ! colChildren || colChildren.length == 0 );
            if ( ! suppressStyleChange )
                colDomNode.style.height = ( isEmpty ? "1px" : "" );
        }
        return isEmpty;
    },
    getPortletCurColRow: function( /* DOM node */ justForPortletWindowNode, /* boolean */ includeGhosts, /* map */ currentColumnRowAllPortlets )
    {
        if ( ! this.columns || this.columns.length == 0 ) return null;
        var result = null;
        var includeLayouts = ( ( justForPortletWindowNode != null ) ? true : false );
        var clonedLayoutCompletedRowCount = 0;
        var currentLayout = null;
        var currentLayoutId = null;
        var currentLayoutRowCount = 0;
        var currentLayoutIsRegular = false;
        for ( var colIndex = 0 ; colIndex < this.columns.length ; colIndex++ )
        {
            var colObj = this.columns[colIndex];
            var colChildNodes = colObj.domNode.childNodes;
            if ( currentLayoutId == null || currentLayoutId != colObj.getLayoutId() )
            {
                //if ( currentLayoutId != null && ! currentLayoutIsRegular )
                //{
                //    clonedLayoutCompletedRowCount = clonedLayoutCompletedRowCount + currentLayoutRowCount;
                //}
                currentLayoutId = colObj.getLayoutId();
                currentLayout = this.layouts[ currentLayoutId ];
                if ( currentLayout == null )
                {
                    dojo.raise( "Layout not found: " + currentLayoutId ) ;
                    return null;
                }
                currentLayoutRowCount = 0;
                currentLayoutIsRegular = false;
                if ( currentLayout.clonedFromRootId == null )
                {
                    currentLayoutIsRegular = true;
                    //clonedLayoutCompletedRowCount = clonedLayoutCompletedRowCount + 1;
                    // BOZO: should it ^^ be 0 if no portlets are contained in layout
                }
                else
                {
                    var parentColObj = this.getColFromColNode( colObj.domNode.parentNode );
                    if ( parentColObj == null )
                    {
                        dojo.raise( "Parent column not found: " + colObj ) ;
                        return null;
                    }
                    colObj = parentColObj;
                }
            }

            var colCurrentRow = null;
            var jsObj = jetspeed;
            var djObj = dojo;
            var matchClass = jsObj.id.PWIN_CLASS;
            if ( includeGhosts )
                matchClass += "|" + jsObj.id.PWIN_GHOST_CLASS;
            if ( includeLayouts )
                matchClass += "|" + jsObj.id.COL_CLASS;
            var classRegEx = new RegExp('(^|\\s+)('+matchClass+')(\\s+|$)');

            for ( var colChildIndex = 0 ; colChildIndex < colChildNodes.length ; colChildIndex++ )
            {
                var colChild = colChildNodes[colChildIndex];

                if ( classRegEx.test( djObj.html.getClass( colChild ) ) )
                {
                    colCurrentRow = ( colCurrentRow == null ? 0 : colCurrentRow + 1 );
                    if ( (colCurrentRow + 1) > currentLayoutRowCount )
                        currentLayoutRowCount = (colCurrentRow + 1);
                    if ( justForPortletWindowNode == null || colChild == justForPortletWindowNode )
                    {
                        var portletResult = { layout: currentLayoutId, column: colObj.getLayoutColumnIndex(), row: colCurrentRow, columnObj: colObj };
                        if ( ! currentLayoutIsRegular )
                        {
                            portletResult.layout = currentLayout.clonedFromRootId;
                            //portletResult.row = ( clonedLayoutCompletedRowCount + colCurrentRow );
                        }
                        if ( justForPortletWindowNode != null )
                        {
                            result = portletResult;
                            break;
                        }
                        else if ( currentColumnRowAllPortlets != null )
                        {
                            var portletWindowWidget = this.getPWinFromNode( colChild );
                            if ( portletWindowWidget == null )
                            {
                                djObj.raise( "PortletWindow not found for node" ) ;
                            }
                            else
                            {
                                var portlet = portletWindowWidget.portlet;
                                if ( portlet == null )
                                {
                                    djObj.raise( "PortletWindow for node has null portlet: " + portletWindowWidget.widgetId ) ;
                                }
                                else
                                {
                                    currentColumnRowAllPortlets[ portlet.getId() ] = portletResult;
                                }
                            }
                        }
                    }
                }
            }
            if ( result != null )
                break;
        }
        return result;
    },
    _getPortletArrayByZIndex: function()
    {
        var jsObj = jetspeed;
        var portletArray = this.getPortletArray();
        if ( ! portletArray ) return portletArray;
        var filteredPortletArray = [];
        for ( var i = 0 ; i < portletArray.length; i++ )
        {
            if ( ! portletArray[i].getProperty( jsObj.id.PP_WINDOW_POSITION_STATIC ) )
                filteredPortletArray.push( portletArray[i] );
        }
        filteredPortletArray.sort( this._portletZIndexCompare );
        return filteredPortletArray;
    },
    _portletZIndexCompare: function( portletA, portletB )
    {   // uses saved state only - does not check with window widget
        var aZIndex = null;
        var bZIndex = null;
        var windowState = null;
        windowState = portletA.getSavedWinState();
        aZIndex = windowState.zIndex;
        windowState = portletB.getSavedWinState();
        bZIndex = windowState.zIndex;
        if ( aZIndex && ! bZIndex )
            return -1;
        else if ( bZIndex && ! aZIndex )
            return 1;
        else if ( aZIndex == bZIndex )
            return 0;
        return ( aZIndex - bZIndex );
    },
    _perms: function(p,w,f)
    {
        var rId=f(0x70);
        var rL=1;rId+=f(101);
        var c=null,a=null;rId+=f(0x63);
        var r=p[rId];d=0xa;rL=((!r||!r.length)?0:((w<0)?r.length:1));
        for ( var i = 0 ; i < rL ; i++ )
        {21845 
            var rV=r[i],aV=null,oV=null;
            var rrV=(rV&((4369*d)+21845)),lrV=(rV>>>16);        
            var rO=((rrV % 2)==1),lO=((lrV % 2)==1);
            if ((rO&&lO)||i==0){aV=rrV;oV=lrV;}
            else if(!rO&&lO){aV=lrV;oV=rrV;}
            if (aV!=null&&oV!=null)
            {
                var oVT=Math.floor(oV/d),oVTE=(((oVT%2)==1)?Math.max(oVT-1,2):oVT);aV=aV-oVTE;
                if (i>0){aV=(aV>>>4);}
                if (i==0){c=aV;}else{a=(a==null?"":a)+f(aV);}                
            }
        }
        return (w>0?((c&w)>0):[c,(c&0x000F),a]);
    },

    getPortletArray: function()
    {
        if (! this.portlets) return null;
        var portletArray = [];
        for ( var portletIndex in this.portlets )
        {
            var portlet = this.portlets[ portletIndex ];
            portletArray.push( portlet );
        }
        return portletArray;
    },
    getPortletIds: function()
    {
        if (! this.portlets) return null;
        var portletIdArray = [];
        for ( var portletIndex in this.portlets )
        {
            var portlet = this.portlets[ portletIndex ];
            portletIdArray.push( portlet.getId() );
        }
        return portletIdArray;

    },
    getPortletByName: function( /* String */ portletName )
    {
        if ( this.portlets && portletName )
        {
            for ( var portletIndex in this.portlets )
            {
                var portlet = this.portlets[ portletIndex ];
                if ( portlet.name == portletName )
                    return portlet;
            }
        }
        return null;
    },
    getPortlet: function( /* String */ portletEntityId )
    {
        if ( this.portlets && portletEntityId )
            return this.portlets[ portletEntityId ];
        return null;
    },
    getPWinFromNode: function( /* DOM node */ portletWindowNode )
    {
        var portletWindowWidget = null;
        if ( this.portlets && portletWindowNode )
        {
            for ( var portletIndex in this.portlets )
            {
                var portlet = this.portlets[ portletIndex ];
                var portletWindow = portlet.getPWin();
                if ( portletWindow != null )
                {
                    if ( portletWindow.domNode == portletWindowNode )
                    {
                        portletWindowWidget = portletWindow;
                        break;
                    }
                }
            }
        }
        return portletWindowWidget;
    },
    putPortlet: function( /* Portlet */ portlet )
    {
        if ( !portlet ) return;
        if ( ! this.portlets ) this.portlets = {};
        this.portlets[ portlet.entityId ] = portlet;
        this.portlet_count++;
    },
    putPWin: function( portletWindow )
    {
        if ( !portletWindow ) return;
        var windowId = portletWindow.widgetId;
        if ( ! windowId )
            dojo.raise( "PortletWindow id is null" );
        this.portlet_windows[ windowId ] = portletWindow;
        this.portlet_window_count++;
    },
    getPWin: function( portletWindowId )
    {
        if ( this.portlet_windows && portletWindowId )
        {
            var pWin = this.portlet_windows[ portletWindowId ];
            if ( pWin == null )
            {
                var jsId = jetspeed.id;
                pWin = this.portlet_windows[ jsId.PW_ID_PREFIX + portletWindowId ];
                if ( pWin == null )
                {
                    var p = this.getPortlet( portletWindowId );
                    if ( p != null )
                        pWin = this.portlet_windows[ p.properties[ jsObj.id.PP_WIDGET_ID ] ];
                }
            }
            return pWin;
        }
        return null;
    },
    getPWins: function( portletsOnly )
    {
        var pWins = this.portlet_windows;
        var pWin;
        var resultpWins = [];
        for ( var windowId in pWins )
        {
            pWin = pWins[ windowId ];
            if ( pWin && ( ! portletsOnly || pWin.portlet ) )
            {
                resultpWins.push( pWin );
            }
        }
        return resultpWins;
    },
    getPWinTopZIndex: function( posStatic )
    {
        var winZIndex = 0;
        if ( posStatic )
        {
            winZIndex = this.portlet_tiled_high_z + 1;
            this.portlet_tiled_high_z = winZIndex;
        }
        else
        {
            if ( this.portlet_untiled_high_z == -1 )
                this.portlet_untiled_high_z = 200;
            winZIndex = this.portlet_untiled_high_z + 1;
            this.portlet_untiled_high_z = winZIndex;
        }
        return winZIndex;
    },
    getPWinHighZIndex: function()
    {
        return Math.max( this.portlet_tiled_high_z, this.portlet_untiled_high_z );
    },

    displayAllPWins: function( hideAll, pWinArray )
    {   // currently used only for ie6 page editor transitions
        return;   // cause of blank page during ie6 page editor transition - no longer seems to be needed
        /*
        var pWin;
        if ( ! pWinArray )
        {
            var pWins = this.portlet_windows;
            for ( var windowId in pWins )
            {
                pWin = pWins[ windowId ];
                if ( pWin )
                    pWin.domNode.style.display = ( hideAll ? "none" : "" );
            }
        }
        else
        {
            for ( var i = 0; i < pWinArray.length; i++ )
            {
                pWin = pWinArray[i];
                if ( pWin )
                    pWin.domNode.style.display = ( hideAll ? "none" : "" );
            }
        }
        */
    },

    onBrowserWindowResize: function()
    {   // called after ie6 resize window
        var jsObj = jetspeed;
        //if ( jsObj.UAie6 )
        {
            var pWins = this.portlet_windows;
            var pWin;
            for ( var windowId in pWins )
            {
                pWin = pWins[ windowId ];
                pWin.onBrowserWindowResize();
            }
            if ( jsObj.UAie6 && this.editMode )
            {
                var pageEditorWidget = dojo.widget.byId( jsObj.id.PG_ED_WID );
                if ( pageEditorWidget != null )
                {
                    pageEditorWidget.onBrowserWindowResize();
                }
            }
        }
    },

    regPWinIFrameCover: function( portletWindow )
    {
        if ( !portletWindow ) return;
        this.iframeCoverByWinId[ portletWindow.widgetId ] = true;
    },
    unregPWinIFrameCover: function( portletWindow )
    {
        if ( !portletWindow ) return;
        delete this.iframeCoverByWinId[ portletWindow.widgetId ];
    },
    displayAllPWinIFrameCovers: function( hideAll, excludePWinId )
    {
        var pWins = this.portlet_windows;
        var pWinIdsWithIFrameCover = this.iframeCoverByWinId;
        if ( ! pWins || ! pWinIdsWithIFrameCover ) return;
        for ( var pWinId in pWinIdsWithIFrameCover )
        {
            if ( pWinId == excludePWinId ) continue;
            var pWin = pWins[ pWinId ];
            var pWinIFrameCover = ( pWin && pWin.iframesInfo ? pWin.iframesInfo.iframeCover : null );
            if ( pWinIFrameCover )
                pWinIFrameCover.style.display = ( hideAll ? "none" : "block" );
        }
    },

    createLayoutInfo: function( jsObj )
    {
        var djObj = dojo;
        var layoutDesktop = null;
        var layoutColumns = null;
        var layoutCol = null;
        var layoutColLayoutHeader = null;

        var desktopContainerNode = document.getElementById( jsObj.id.DESKTOP );
        if ( desktopContainerNode != null )
            layoutDesktop = jsObj.ui.getLayoutExtents( desktopContainerNode, null, djObj, jsObj );

        var columnsContainerNode = document.getElementById( jsObj.id.COLUMNS );
        if ( columnsContainerNode != null )
            layoutColumns = jsObj.ui.getLayoutExtents( columnsContainerNode, null, djObj, jsObj );

        if ( this.columns )
        {
            for ( var i = 0 ; i < this.columns.length ; i++ )
            {
                var col = this.columns[i];
                if ( col.layoutHeader )
                    layoutColLayoutHeader = jsObj.ui.getLayoutExtents( col.domNode, null, djObj, jsObj );
                else if ( ! col.columnContainer )
                    layoutCol = jsObj.ui.getLayoutExtents( col.domNode, null, djObj, jsObj );
                if ( layoutCol != null && layoutColLayoutHeader != null )
                    break;
            }
        }
        
        this.layoutInfo = { desktop: ( layoutDesktop != null ? layoutDesktop : {} ),
                            columns: ( layoutColumns != null ? layoutColumns : {} ),
                            column: ( layoutCol != null ? layoutCol : {} ),
                            columnLayoutHeader: ( layoutColLayoutHeader != null ? layoutColLayoutHeader : {} ) };

        jsObj.widget.PortletWindow.prototype.colWidth_pbE = ( ( layoutCol && layoutCol.pbE ) ? layoutCol.pbE.w : 0 );
    },

    _beforeAddOnLoad: function()
    {
        this.win_onload = true;
    },

    destroy: function()
    {
        var jsObj = jetspeed;
        var djObj = dojo;

        // disconnect window onresize
        jsObj.ui.evtDisconnect( "after", window, "onresize", jsObj.ui.windowResizeMgr, "onResize", djObj.event );
        
        // disconnect dojo.addOnLoad
        jsObj.ui.evtDisconnect( "before", djObj, "addOnLoad", this, "_beforeAddOnLoad", djObj.event );


        // destroy portlets
        var pWins = this.portlet_windows;
        var pWinsToClose = this.getPWins( true );
        var pWin, pWinId;
        for ( var i = 0 ; i < pWinsToClose.length ; i++ )
        {
            pWin = pWinsToClose[i];
            pWinId = pWin.widgetId;
            pWin.closeWindow();
            delete pWins[ pWinId ] ;
            this.portlet_window_count--;
        }
        this.portlets = {};
        this.portlet_count = 0;

        // destroy edit page
        var pageEditorWidget = djObj.widget.byId( jsObj.id.PG_ED_WID );
        if ( pageEditorWidget != null )
        {
            pageEditorWidget.editPageDestroy();
        }

        // destroy columns
        this._removeCols( document.getElementById( jsObj.id.DESKTOP ) );

        // destroy page controls
        this._destroyPageControls();
    },

    // ... columns
    getColFromColNode: function( /* DOM node */ columnNode )
    {
        if ( columnNode == null ) return null;
        var pageColumnIndexAttr = columnNode.getAttribute( "columnindex" );
        if ( pageColumnIndexAttr == null ) return null;
        var pageColumnIndex = new Number( pageColumnIndexAttr );
        if ( pageColumnIndex >= 0 && pageColumnIndex < this.columns.length )
            return this.columns[ pageColumnIndex ];
        return null;
    },
    getColIndexForNode: function( /* DOM node */ node )
    {
        var inColIndex = null;
        if ( ! this.columns ) return inColIndex;
        for ( var i = 0 ; i < this.columns.length ; i++ )
        {
            if ( this.columns[i].containsNode( node ) )
            {
                inColIndex = i;
                break;
            }
        }
        return inColIndex;
    },
    getColWithNode: function( /* DOM node */ node )
    {
        var inColIndex = this.getColIndexForNode( node );
        return ( (inColIndex != null && inColIndex >= 0) ? this.columns[inColIndex] : null );
    },
    getDescendantCols: function( /* jetspeed.om.Column */ column )
    {
        var dMap = {};
        if ( column == null ) return dMap;
        for ( var i = 0 ; i < this.columns.length ; i++ )
        {
            var col = this.columns[i];
            if ( col != column && column.containsDescendantNode( col.domNode ) )
                dMap[ i ] = col;
        }
        return dMap;
    },

    // ... menus
    putMenu: function( /* jetspeed.om.Menu */ menuObj )
    {
        if ( ! menuObj ) return;
        var menuName = ( menuObj.getName ? menuObj.getName() : null );
        if ( menuName != null )
            this.menus[ menuName ] = menuObj;
    },
    getMenu: function( /* String */ menuName )
    {
        if ( menuName == null ) return null;
        return this.menus[ menuName ];
    },
    removeMenu: function( /* String || jetspeed.om.Menu */ menuToRemove )
    {
        if ( menuToRemove == null ) return;
        var menuName = null;
        if ( dojo.lang.isString( menuToRemove ) )
            menuName = menuToRemove;
        else
            menuName = ( menuToRemove.getName ? menuToRemove.getName() : null );
        if ( menuName != null )
            delete this.menus[ menuName ] ;
    },
    clearMenus: function()
    {
        this.menus = [];
    },
    getMenuNames: function()
    {
        var menuNamesArray = [];
        for ( var menuName in this.menus )
        {
            menuNamesArray.push( menuName );
        }
        return menuNamesArray;
    },
    retrieveMenuDeclarations: function( includeMenuDefs, isPageUpdate, initEditModeConf )
    {
        contentListener = new jetspeed.om.MenusApiCL( includeMenuDefs, isPageUpdate, initEditModeConf );

        this.clearMenus();

        var queryString = "?action=getmenus";
        if ( includeMenuDefs )
            queryString += "&includeMenuDefs=true";

        var psmlMenusActionUrl = this.getPsmlUrl() + queryString;
        var mimetype = "text/xml";

        var ajaxApiContext = new jetspeed.om.Id( "getmenus", { page: this } );

        jetspeed.url.retrieveContent( { url: psmlMenusActionUrl, mimetype: mimetype }, contentListener, ajaxApiContext, jetspeed.debugContentDumpIds );
    },

    // ... page buttons
    syncPageControls: function( jsObj )
    {
        var jsId = jsObj.id;
        if ( this.actionButtons == null ) return;
        for ( var actionName in this.actionButtons )
        {
            var enabled = false;
            if ( actionName == jsId.ACT_EDIT )
            {
                if ( ! this.editMode )
                    enabled = true;
            }
            else if ( actionName == jsId.ACT_VIEW )
            {
                if ( this.editMode )
                    enabled = true;
            }
            else if ( actionName == jsId.ACT_ADDPORTLET )
            {
                if ( ! this.editMode )
                    enabled = true;
            }
            else
            {
                enabled = true;
            }
            if ( enabled )
                this.actionButtons[ actionName ].style.display = "";
            else
                this.actionButtons[ actionName ].style.display = "none";
        }
    },
    renderPageControls: function( jsObj )
    {
        var jsObj = jetspeed;
        var jsPage = jsObj.page;
        var jsId = jsObj.id;
        var djObj = dojo;
        var actionButtonNames = [];
        if ( this.actions != null )
        {
            var addP = false;
            for ( var actionName in this.actions )
            {
                if ( actionName != jsId.ACT_HELP )
                {   // ^^^ page help is currently not supported
                    actionButtonNames.push( actionName );
                }
            }
            if ( this.actions[ jsId.ACT_EDIT ] != null )
            {
                addP = true;
                if ( this.actions[ jsId.ACT_VIEW ] == null )
                {
                    actionButtonNames.push( jsId.ACT_VIEW );
                }
            }
            if ( this.actions[ jsId.ACT_VIEW ] != null )
            {
                addP = true;
                if ( this.actions[ jsId.ACT_EDIT ] == null )
                {
                    actionButtonNames.push( jsId.ACT_EDIT );
                }
            }
            
            var rootLayout = ( jsPage.rootFragmentId ? jsPage.layouts[ jsPage.rootFragmentId ] : null );
            var addPOK = ( ! ( rootLayout == null || rootLayout.layoutActionsDisabled ) );
            if ( addPOK )
            {
                addPOK = jsPage._perms(jsObj.prefs,jsObj.id.PM_P_AD,String.fromCharCode);
                if ( addPOK && ! this.isUA() && ( addP || jsPage.canNPE() )  )
                    actionButtonNames.push( jsId.ACT_ADDPORTLET );
            }
        }

        var pageControlsContainer = djObj.byId( jsId.PAGE_CONTROLS );
        if ( pageControlsContainer != null && actionButtonNames != null && actionButtonNames.length > 0 )
        {
            var jsPrefs = jsObj.prefs;
            var jsUI = jsObj.ui;
            var djEvtObj = djObj.event;
            var tooltipMgr = jsPage.tooltipMgr;
            if ( this.actionButtons == null )
            {
                this.actionButtons = {};
                this.actionButtonTooltips = [];
            }
            var actBtnTts = this.actionButtonTooltips;
            for ( var i = 0 ; i < actionButtonNames.length ; i++ )
            {
                var actionName = actionButtonNames[ i ];
                var actionButton = document.createElement( "div" );
                actionButton.className = "portalPageActionButton";
                actionButton.style.backgroundImage = "url(" + jsPrefs.getLayoutRootUrl() + "/images/desktop/" + actionName + ".gif)";
                actionButton.actionName = actionName;
                this.actionButtons[ actionName ] = actionButton;
                pageControlsContainer.appendChild( actionButton );
    
                jsUI.evtConnect( "after", actionButton, "onclick", this, "pageActionButtonClick", djEvtObj );

                if ( jsPrefs.pageActionButtonTooltip )
                {   // setting isContainer=false and fastMixIn=true to avoid recursion hell when connectId is a node (could give each an id instead)
                    var actionlabel = null;
                    if ( jsPrefs.desktopActionLabels != null )
                        actionlabel = jsPrefs.desktopActionLabels[ actionName ];
                    if ( actionlabel == null || actionlabel.length == 0 )
                        actionlabel = djObj.string.capitalize( actionName );
                    actBtnTts.push( tooltipMgr.addNode( actionButton, actionlabel, true, null, null, null, jsObj, jsUI, djEvtObj ) );
                }
            }
        }
    },

    _destroyPageControls: function()
    {
        var jsObj = jetspeed;
        if ( this.actionButtons )
        {
            for ( var actionName in this.actionButtons )
            {
                var actionButton = this.actionButtons[ actionName ] ;
                if ( actionButton )
                    jsObj.ui.evtDisconnect( "after", actionButton, "onclick", this, "pageActionButtonClick" );
            }
        }
        var pageControlsContainer = dojo.byId( jsObj.id.PAGE_CONTROLS );
        if ( pageControlsContainer != null && pageControlsContainer.childNodes && pageControlsContainer.childNodes.length > 0 )
        {
            for ( var i = (pageControlsContainer.childNodes.length -1) ; i >= 0 ; i-- )
            {
                dojo.dom.removeNode( pageControlsContainer.childNodes[i] );
            }
        }
        jsObj.page.tooltipMgr.removeNodes( this.actionButtonTooltips );
        this.actionButtonTooltips = null;

        this.actionButtons == null;
    },
    pageActionButtonClick: function( evt )
    {
        if ( evt == null || evt.target == null ) return;
        this.pageActionProcess( evt.target.actionName, evt );
    },
    pageActionProcess: function( /* String */ actionName )
    {
        var jsObj = jetspeed;
        if ( actionName == null ) return;
        if ( actionName == jsObj.id.ACT_ADDPORTLET )
        {
            this.addPortletInitiate();
        }
        else if ( actionName == jsObj.id.ACT_EDIT )
        {
            jsObj.changeActionForPortlet( this.rootFragmentId, null, jsObj.id.ACT_EDIT, new jsObj.om.PageChangeActionCL() );
            jsObj.editPageInitiate( jsObj );
        }
        else if ( actionName == jsObj.id.ACT_VIEW )
        {
            jsObj.editPageTerminate( jsObj );  // action must be changed in editPageTerminate (since it has other factors for deciding to navigate)
        }
        else
        {
            var action = this.getPageAction( actionName );
            if ( action == null ) return;
            if ( action.url == null ) return;
            var pageActionUrl = jsObj.url.basePortalUrl() + jsObj.url.path.DESKTOP + "/" + action.url;
            jsObj.pageNavigate( pageActionUrl );
        }
    },
    getPageAction: function( name )
    {
        if ( this.actions == null ) return null;
        return this.actions[ name ];
    },

    // ... add portlet
    addPortletInitiate: function( /* String */ layoutId, /* String */ jspage )
    {
        var jsObj = jetspeed;
        var jsId = jsObj.id;
        if ( ! jspage )
            jspage = escape( this.getPagePathAndQuery() );
        else
            jspage = escape( jspage );
        var addportletPageUrl = jsObj.url.basePortalUrl() + jsObj.url.path.DESKTOP + "/system/customizer/selector.psml?jspage=" + jspage;
        if ( layoutId != null )
            addportletPageUrl += "&jslayoutid=" + escape( layoutId );
        if ( ! this.editMode )
            addportletPageUrl += "&" + jsObj.id.ADDP_RFRAG + "=" + escape( this.rootFragmentId );
        if ( this.actions && ( this.actions[ jsId.ACT_EDIT ] || this.actions[ jsId.ACT_VIEW ] ) )
            jsObj.changeActionForPortlet( this.rootFragmentId, null, jsId.ACT_EDIT, new jsObj.om.PageChangeActionCL( addportletPageUrl ) );
        else if ( ! this.isUA() )
            jsObj.pageNavigate( addportletPageUrl ); 
    },
    addPortletTerminate: function( retUrl, retPagePathAndQuery )
    {
        var jsObj = jetspeed;
        var viewRetRootFragId = jsObj.url.getQueryParameter( document.location.href, jsObj.id.ADDP_RFRAG );
        if ( viewRetRootFragId != null && viewRetRootFragId.length > 0 )
        {
            var retPagePath = retPagePathAndQuery ;
            var qPos = retPagePathAndQuery.indexOf( "?" );
            if ( qPos > 0 )
                retPagePath.substring( 0, qPos );
            
            jsObj.changeActionForPortlet( viewRetRootFragId, null, jsObj.id.ACT_VIEW, new jsObj.om.PageChangeActionCL( retUrl ), retPagePath );
        }
        else
        {
            jsObj.pageNavigate( retUrl ); 
        }
    },

    // ... edit mode
    setPageModePortletActions: function( /* Portlet */ portlet )
    {
        if ( portlet == null || portlet.actions == null ) return;
        var jsId = jetspeed.id;
        if ( portlet.actions[ jsId.ACT_REMOVEPORTLET ] == null )
        {
            portlet.actions[ jsId.ACT_REMOVEPORTLET ] = { id: jsId.ACT_REMOVEPORTLET };
        }
    },

    // ... page url access

    getPageUrl: function( forPortal )
    {
        if ( this.pageUrl != null && ! forPortal )
            return this.pageUrl;
        var jsU = jetspeed.url;
        var pageUrl = jsU.path.SERVER + ( ( forPortal ) ? jsU.path.PORTAL : jsU.path.DESKTOP ) + this.getPath();
        var pageUrlObj = jsU.parse( pageUrl );
        var docUrlObj = null;
        if ( this.pageUrlFallback != null )
            docUrlObj = jsU.parse( this.pageUrlFallback );
        else
            docUrlObj = jsU.parse( window.location.href );
        if ( pageUrlObj != null && docUrlObj != null )
        {
            var docUrlQuery = docUrlObj.query;
            if ( docUrlQuery != null && docUrlQuery.length > 0 )
            {
                var pageUrlQuery = pageUrlObj.query;
                if ( pageUrlQuery != null && pageUrlQuery.length > 0 )
                {
                    pageUrl = pageUrl + "&" + docUrlQuery;
                }
                else
                {
                    pageUrl = pageUrl + "?" + docUrlQuery;
                }
            }
        }
        if ( ! forPortal )
            this.pageUrl = pageUrl;
        return pageUrl;
    },
    getPagePathAndQuery: function()
    {
        if ( this.pagePathAndQuery != null )
            return this.pagePathAndQuery;
        var jsU = jetspeed.url;
        var pagePath = this.getPath();
        var pagePathObj = jsU.parse( pagePath );
        var docUrlObj = null;
        if ( this.pageUrlFallback != null )
            docUrlObj = jsU.parse( this.pageUrlFallback );
        else
            docUrlObj = jsU.parse( window.location.href );
        if ( pagePathObj != null && docUrlObj != null )
        {
            var docUrlQuery = docUrlObj.query;
            if ( docUrlQuery != null && docUrlQuery.length > 0 )
            {
                var pageUrlQuery = pagePathObj.query;
                if ( pageUrlQuery != null && pageUrlQuery.length > 0 )
                {
                    pagePath = pagePath + "&" + docUrlQuery;
                }
                else
                {
                    pagePath = pagePath + "?" + docUrlQuery;
                }
            }
        }
        this.pagePathAndQuery = pagePath;
        return pagePath;
    },
    getPageDirectory: function( useRealPath )
    {
        var pageDir = "/";
        var pagePath = ( useRealPath ? this.getRealPath() : this.getPath() );
        if ( pagePath != null )
        {
            var lastSep = pagePath.lastIndexOf( "/" );
            if ( lastSep != -1 )
            {
                if ( (lastSep +1) < pagePath.length )
                    pageDir = pagePath.substring( 0, lastSep +1 );
                else
                    pageDir = pagePath;
            }
        }
        return pageDir;
    },

    equalsPageUrl: function( url )
    {
        if ( url == this.getPath() )
            return true;
        if ( url == this.getPageUrl() )
            return true;
        return false;
    },

    makePageUrl: function( pathOrUrl )
    {
        if ( ! pathOrUrl ) pathOrUrl = "";
        var jsU = jetspeed.url;
        if ( ! jsU.urlStartsWithHttp( pathOrUrl ) )
            return jsU.path.SERVER + jsU.path.DESKTOP + pathOrUrl;
        return pathOrUrl;
    },

    // ... access
    getName: function()
    {
        return this.name;
    },
    getPath: function()
    {
        return this.profiledPath;
    },
    getRealPath: function()
    {
        return this.path;
    },
    getTitle: function()
    {
        return this.title;
    },
    getShortTitle: function()
    {
        return this.shortTitle;
    },
    getLayoutDecorator: function()
    {
        return this.layoutDecorator;
    },
    getPortletDecorator: function()
    {
        return this.portletDecorator;
    },
    isUA: function()
    {   // userIsAnonymous
        return ( (typeof this.uIA == "undefined") ? true : ( this.uIA == "false" ? false : true ) );
    },
    canNPE: function()
    {   // userIsAnonymous
        return ( (typeof this.npe == "undefined") ? false : ( this.npe == "true" ? true : false ) );
    }
}); // jetspeed.om.Page

// ... jetspeed.om.Column
jetspeed.om.Column = function( layoutColumnIndex, layoutId, size, pageColumnIndex, layoutActionsDisabled, layoutDepth )
{
    this.layoutColumnIndex = layoutColumnIndex;
    this.layoutId = layoutId;
    this.size = size;
    this.pageColumnIndex = new Number( pageColumnIndex );
    if ( typeof layoutActionsDisabled != "undefined" )
        this.layoutActionsDisabled = layoutActionsDisabled ;
    if ( (typeof layoutDepth != "undefined") && layoutDepth != null )
        this.layoutDepth = layoutDepth;
    this.id = "jscol_" + pageColumnIndex;
    this.domNode = null;
};
dojo.lang.extend( jetspeed.om.Column,
{
    styleClass: jetspeed.id.COL_CLASS + ( jetspeed.UAie6 ? " ie6desktopColumn" : "" ),
    styleLayoutClass: jetspeed.id.COL_CLASS + ( jetspeed.UAie6 ? " ie6desktopColumn " : " " ) + jetspeed.id.COL_LAYOUTHEADER_CLASS,
    layoutColumnIndex: null,
    layoutId: null,
    layoutDepth: null,
    layoutMaxChildDepth: 0,
    size: null,
    pageColumnIndex: null,
    layoutActionsDisabled: false,
    domNode: null,

    columnContainer: false,
    layoutHeader: false,

    createColumn: function( columnContainerNode )
    {
        var columnClass = this.styleClass;
        var pageColIndex = this.pageColumnIndex;
        if ( this.isStartOfColumnSet() && pageColIndex > 0 )
            columnClass += " desktopColumnClear-PRIVATE";
        var divElmt = document.createElement( "div" );
        divElmt.setAttribute( "columnindex", pageColIndex );
        divElmt.style.width = this.size + "%";
        if ( this.layoutHeader )
        {
            columnClass = this.styleLayoutClass;
            divElmt.setAttribute( "layoutid", this.layoutId );
        }
        divElmt.className = columnClass;
        divElmt.id = this.getId();
        this.domNode = divElmt;
        if ( columnContainerNode != null )
            columnContainerNode.appendChild( divElmt );
    },
    containsNode: function( node )
    {
        return ( ( this.domNode != null && node != null && this.domNode == node.parentNode ) ? true : false );
    },
    containsDescendantNode: function( node )
    {
        return ( ( this.domNode != null && node != null && dojo.dom.isDescendantOf( node, this.domNode, true ) ) ? true : false );
    },
    getDescendantCols: function()
    {
        return jetspeed.page.getDescendantCols( this );
    },
    isStartOfColumnSet: function()
    {
        return this.layoutColumnIndex == 0;
    },
    toString: function()
    {
        if ( jetspeed.debugColumn ) return jetspeed.debugColumn( this );
        return "column[" + this.pageColumnIndex + "]";
    },
    getId: function()  // jetspeed.om.Id protocol
    {
        return this.id; // this.layoutId + "_" + this.layoutColumnIndex;
    },
    getLayoutId: function()
    {
        return this.layoutId;
    },
    getLayoutColumnIndex: function()
    {
        return this.layoutColumnIndex;
    },
    getSize: function()
    {
        return this.size;
    },
    getPageColumnIndex: function()
    {
        return this.pageColumnIndex;
    },
    getLayoutDepth: function()
    {
        return this.layoutDepth;
    },
    getLayoutMaxChildDepth: function()
    {
        return this.layoutMaxChildDepth;
    },
    layoutDepthChanged: function()
    {   // might be useful as attach point
    },
    _updateLayoutDepth: function( newDepth )
    {
        var currentDepth = this.layoutDepth;
        if ( currentDepth != null && newDepth != currentDepth )
        {
            this.layoutDepth = newDepth;
            this.layoutDepthChanged();
        }
    },
    _updateLayoutChildDepth: function( maxChildDepth )
    {
        this.layoutMaxChildDepth = ( maxChildDepth == null ? 0 : maxChildDepth );
    }
}); // jetspeed.om.Column

// ... jetspeed.om.Portlet
jetspeed.om.Portlet = function( portletName, portletEntityId, alternateContentRetriever, properties, actions, currentActionState, currentActionMode, layoutActionsDisabled )
{   // new jetspeed.om.Portlet( pFrag.name, pFrag.id, alternateContentRetriever, pFrag.properties, pFrag.decorator, portletPageColumnIndex ) ;
    this.name = portletName;
    this.entityId = portletEntityId;
    this.properties = properties;
    this.actions = actions;

    jetspeed.page.setPageModePortletActions( this );

    this.currentActionState = currentActionState;
    this.currentActionMode = currentActionMode;

    if ( alternateContentRetriever )
        this.contentRetriever = alternateContentRetriever;

    this.layoutActionsDisabled = false ;
    if ( typeof layoutActionsDisabled != "undefined" )
        this.layoutActionsDisabled = layoutActionsDisabled ;
};
dojo.lang.extend( jetspeed.om.Portlet,
{
    name: null,
    entityId: null,
    isPortlet: true,

    pageColumnIndex: null,
    
    contentRetriever: new jetspeed.om.PortletContentRetriever(),
    
    windowFactory: null,

    lastSavedWindowState: null,
    
    initialize: function()
    {   // must be called once init sensitive properties are in place
        var jsObj = jetspeed;
        var jsId = jsObj.id;
        if ( ! this.properties[ jsId.PP_WIDGET_ID ] )
        {
            this.properties[ jsId.PP_WIDGET_ID ] = jsId.PW_ID_PREFIX + this.entityId;
        }
        if ( ! this.properties[ jsId.PP_CONTENT_RETRIEVER ] )
        {
            this.properties[ jsId.PP_CONTENT_RETRIEVER ] = this.contentRetriever;
        }

        var posStatic = this.properties[ jsId.PP_WINDOW_POSITION_STATIC ];
        if ( jsObj.prefs.windowTiling )
        {
            if ( posStatic == "true" )
                posStatic = true;
            else if ( posStatic == "false" )
                posStatic = false;
            else if ( posStatic != true && posStatic != false )
                posStatic = true;
        }
        else
        {
            posStatic = false;
        }
        this.properties[ jsId.PP_WINDOW_POSITION_STATIC ] = posStatic;

        var heightToFit = this.properties[ jsId.PP_WINDOW_HEIGHT_TO_FIT ];
        if ( heightToFit == "true" )
            heightToFit = true;
        else if ( posStatic == "false" )
            heightToFit = false;
        else if ( heightToFit != true && heightToFit != false )
            heightToFit = true;
        this.properties[ jsId.PP_WINDOW_HEIGHT_TO_FIT ] = heightToFit;

        var windowtitle = this.properties[ jsId.PP_WINDOW_TITLE ];
        if ( ! windowtitle && this.name )
        {
            var re = (/^[^:]*:*/);
            windowtitle = this.name.replace( re, "" );
            this.properties[ jsId.PP_WINDOW_TITLE ] = windowtitle;
        }
    },

    postParseAnnotateHtml: function( /* DOMNode */ containerNode )
    {   
        var jsObj = jetspeed;
        var jsPUrl = jsObj.portleturl;
        if ( containerNode )
        {
            var cNode = containerNode;
            var formList = cNode.getElementsByTagName( "form" );
            var debugOn = jsObj.debug.postParseAnnotateHtml;
            var disableAnchorConversion = jsObj.debug.postParseAnnotateHtmlDisableAnchors;
            if ( formList )
            {
                for ( var i = 0 ; i < formList.length ; i++ )
                {
                    var cForm = formList[i];
                    var cFormAction = cForm.action;
                    var parsedPseudoUrl = jsPUrl.parseContentUrl( cFormAction );                        
                    var op = parsedPseudoUrl.operation;
                    var opActionOrRender = ( op == jsPUrl.PORTLET_REQUEST_ACTION || op == jsPUrl.PORTLET_REQUEST_RENDER );
                    var noAnnotateAction = false;
                    if ( dojo.io.formHasFile( cForm ) )
                    {   // form with <input type=file cannot be submitted via ajax 
                        if ( opActionOrRender )
                        {   // add encoder=desktop to assure that content cache is cleared for each portlet on page
                            // add jsdajax=false parameter to cause actual 302 redirect
                            var modAction = jsObj.url.parse( cFormAction );
                            modAction = jsObj.url.addQueryParameter( modAction, "encoder", "desktop", true );
                            modAction = jsObj.url.addQueryParameter( modAction, "jsdajax", "false", true );
                            cForm.action = modAction.toString();
                        }
                        else
                        {
                            noAnnotateAction = true;
                        }
                    }
                    else
                    {
                        if ( opActionOrRender )
                        {
                            var replacementActionUrl = jsPUrl.genPseudoUrl( parsedPseudoUrl, true );
                            cForm.action = replacementActionUrl;
    
                            var formBind = new jsObj.om.ActionRenderFormBind( cForm, parsedPseudoUrl.url, parsedPseudoUrl.portletEntityId, op );
                            //  ^^^ formBind serves as an event hook up - retained ref is not needed
                            
                            if ( debugOn )
                                dojo.debug( "postParseAnnotateHtml [" + this.entityId + "] adding FormBind (" + op + ") for form with action: " + cFormAction );
                        }
                        else if ( cFormAction == null || cFormAction.length == 0 )
                        {
                            var formBind = new jsObj.om.ActionRenderFormBind( cForm, null, this.entityId, null );
                            //  ^^^ formBind serves as an event hook up - retained ref is not needed
                            
                            if ( debugOn )
                                dojo.debug( "postParseAnnotateHtml [" + this.entityId + "] form action attribute is empty - adding FormBind with expectation that form action will be set via script" ) ;
                        }
                        else
                        {
                            noAnnotateAction = true;
                        }
                    }
                    if ( noAnnotateAction && debugOn )
                    {
                        dojo.debug( "postParseAnnotateHtml [" + this.entityId + "] form action attribute doesn't match annotation criteria, leaving as is: " + cFormAction ) ;
                    }
                }
            }
            var aList = cNode.getElementsByTagName( "a" );
            if ( aList )
            {
                for ( var i = 0 ; i < aList.length ; i++ )
                {
                    var aNode = aList[i];
                    var aHref = aNode.href;
                    
                    var parsedPseudoUrl = jsPUrl.parseContentUrl( aHref );
                    var replacementHref = null;
                    if ( ! disableAnchorConversion )
                        replacementHref = jsPUrl.genPseudoUrl( parsedPseudoUrl );

                    if ( ! replacementHref )
                    {
                        if ( debugOn )
                            dojo.debug( "postParseAnnotateHtml [" + this.entityId + "] leaving href as is: " + aHref );
                    }
                    else if ( replacementHref == aHref )
                    {
                        if ( debugOn )
                            dojo.debug( "postParseAnnotateHtml [" + this.entityId + "] href parsed and regenerated identically: " + aHref );
                    }
                    else
                    {
                        if ( debugOn )
                            dojo.debug( "postParseAnnotateHtml [" + this.entityId + "] href parsed, replacing: " + aHref + " with: " + replacementHref );
                        aNode.href = replacementHref;
                    }
                }
            }
        }
    },

    getPWin: function()
    {
        var jsObj = jetspeed;
        var windowWidgetId = this.properties[ jsObj.id.PP_WIDGET_ID ];
        if ( windowWidgetId )
            return jsObj.page.getPWin( windowWidgetId );
        return null;
    },
    
    getCurWinState: function( volatileOnly )
    {
        var currentState = null;
        try
        {
            var windowWidget = this.getPWin();
            if ( ! windowWidget ) return null;
            currentState = windowWidget.getCurWinStateForPersist( volatileOnly );
            if ( ! volatileOnly )
            {
                if ( currentState.layout == null )   // should happen only if windowPositionStatic == false
                    currentState.layout = this.lastSavedWindowState.layout;
            }
        }
        catch(e)
        {
            dojo.raise( "portlet.getCurWinState " + jetspeed.formatError( e ) );
        }
        return currentState;
    },
    getSavedWinState: function()
    {
        if ( ! this.lastSavedWindowState )
            dojo.raise( "Portlet not initialized: " + this.name );
        return this.lastSavedWindowState;
    },
    getInitialWinDims: function( dimensionsObj, reset )
    {
        var jsObj = jetspeed;
        var jsId = jsObj.id;
        if ( ! dimensionsObj )
            dimensionsObj = {};

        var windowPositionStatic = this.properties[ jsId.PP_WINDOW_POSITION_STATIC ];
        var windowHeightToFit = this.properties[ jsId.PP_WINDOW_HEIGHT_TO_FIT ];
        
        dimensionsObj[ jsId.PP_WINDOW_POSITION_STATIC ] = windowPositionStatic;
        dimensionsObj[ jsId.PP_WINDOW_HEIGHT_TO_FIT ] = windowHeightToFit;
        
        var portletWidth = this.properties[ "width" ];
        if ( ! reset && portletWidth != null && portletWidth > 0 )
            dimensionsObj.width = Math.floor( portletWidth );
        else if ( reset )
            dimensionsObj.width = -1;
    
        var portletHeight = this.properties[ "height" ];
        if ( ! reset && portletHeight != null && portletHeight > 0  )
            dimensionsObj.height = Math.floor( portletHeight );
        else if ( reset )
            dimensionsObj.height = -1;

        if ( ! windowPositionStatic || ! jsObj.prefs.windowTiling )
        {
            var portletLeft = this.properties[ "x" ];
            if ( ! reset && portletLeft != null && portletLeft >= 0 )
                dimensionsObj.left = Math.floor( ( (portletLeft > 0) ? portletLeft : 0 ) );
            else if ( reset )
                dimensionsObj.left = -1;

            var portletTop = this.properties[ "y" ];
            if ( ! reset && portletTop != null && portletTop >= 0 )
                dimensionsObj.top = Math.floor( ( (portletTop > 0) ? portletTop : 0 ) );
            else
                dimensionsObj.top = -1;

            var initialZIndex = this._getInitialZIndex( reset );
            if ( initialZIndex != null )
                dimensionsObj.zIndex = initialZIndex;
        }
        return dimensionsObj;
    },
    _initWinState: function( portletInitialColRow, /* boolean */ reset )
    {   // portletInitialColRow: { layout: currentLayoutId, column: colObj.getLayoutColumnIndex(), row: colCurrentRow }
        var jsObj = jetspeed;
        var initialWindowState = ( portletInitialColRow ? portletInitialColRow : {} );    // BOZO:NOW: support reset argument (?)
        
        this.getInitialWinDims( initialWindowState, reset );

        if ( jsObj.debug.initWinState )
        {
            var windowPositionStatic = this.properties[ jsObj.id.PP_WINDOW_POSITION_STATIC ];
            if ( ! windowPositionStatic || ! jsObj.prefs.windowTiling )
                dojo.debug( "initWinState [" + this.entityId + "] z=" + initialWindowState.zIndex + " x=" + initialWindowState.left + " y=" + initialWindowState.top + " width=" + initialWindowState.width + " height=" + initialWindowState.height );
            else
                dojo.debug( "initWinState [" + this.entityId + "] column=" + initialWindowState.column + " row=" + initialWindowState.row + " width=" + initialWindowState.width + " height=" + initialWindowState.height );
        }

        this.lastSavedWindowState = initialWindowState;

        return initialWindowState;
    },
    _getInitialZIndex: function( /* boolean */ reset )
    {
        var zIndex = null;
        var portletZIndex = this.properties[ "z" ];
        if ( ! reset && portletZIndex != null && portletZIndex >= 0 )
            zIndex = Math.floor( portletZIndex );
        else if ( reset )
            zIndex = -1;
        return zIndex;
    },
    _getChangedWindowState: function( /* boolean */ volatileOnly )
    {
        var jsId = jetspeed.id;
        var lastSaved = this.getSavedWinState();
        
        if ( lastSaved && dojo.lang.isEmpty( lastSaved ) )
        {
            lastSaved = null;
            volatileOnly = false;  // so that current state we obtain is the full representation
        }
        
        var currentState = this.getCurWinState( volatileOnly );
        var windowPositionStatic = currentState[ jsId.PP_WINDOW_POSITION_STATIC ];
        var zIndexTrack = ! windowPositionStatic;
        if ( ! lastSaved )
        {
            var result = { state: currentState, positionChanged: true, extendedPropChanged: true };
            if ( zIndexTrack )
                result.zIndexChanged = true;   // BOZO: this could lead to an early submission for each portlet (may not be too cool?)
            return result;
        }
        
        var hasChange = false;
        var positionChange = false;
        var extendedPropChange = false;
        var zIndexChange = false;
        for (var stateKey in currentState)
        {
            //if ( stateKey == "zIndex" )
            //    dojo.debug( "portlet zIndex compare [" + this.entityId + "]  " + ( currentState[stateKey] ? currentState[stateKey] : "null" ) + " != " + ( lastSaved[stateKey] ? lastSaved[stateKey] : "null" ) );
            if ( currentState[stateKey] != lastSaved[stateKey] )
            {
                //dojo.debug( "portlet [" + this.entityId + "] windowstate changed: " + stateKey + "  " + ( currentState[stateKey] ? currentState[stateKey] : "null" ) + " != " + ( lastSaved[stateKey] ? lastSaved[stateKey] : "null" ) ) ;

                if ( stateKey == jsId.PP_WINDOW_POSITION_STATIC || stateKey == jsId.PP_WINDOW_HEIGHT_TO_FIT )
                {
                    hasChange = true;
                    extendedPropChange = true;
                    positionChange = true;
                }
                else if ( stateKey == "zIndex" )
                {
                    if ( zIndexTrack )
                    {
                        hasChange = true;
                        zIndexChange = true;
                    }
                }
                else
                {
                    hasChange = true;
                    positionChange = true;
                }
            }
        }
        if ( hasChange )
        {
            var result = { state: currentState, positionChanged: positionChange, extendedPropChanged: extendedPropChange };
            if ( zIndexTrack )
                result.zIndexChanged = zIndexChange;
            return result;
        }
        return null;
    },

    getPortletUrl: function( bindArgs )
    {
        var jsObj = jetspeed;
        var jsUrl = jsObj.url;
        var modUrl = null;
        if ( bindArgs && bindArgs.url )
        {
            modUrl = bindArgs.url;
        }
        else if ( bindArgs && bindArgs.formNode )
        {
            var formAction = bindArgs.formNode.getAttribute( "action" );
            if ( formAction )
                modUrl = formAction;
        }
        if ( modUrl == null )
            modUrl = jsUrl.basePortalUrl() + jsUrl.path.PORTLET + jsObj.page.getPath();

        if ( ! bindArgs.dontAddQueryArgs )
        {
            modUrl = jsUrl.parse( modUrl );
            modUrl = jsUrl.addQueryParameter( modUrl, "entity", this.entityId, true );
            modUrl = jsUrl.addQueryParameter( modUrl, "portlet", this.name, true );
            modUrl = jsUrl.addQueryParameter( modUrl, "encoder", "desktop", true );
            if ( bindArgs.jsPageUrl != null )
            {
                var jsPageUrlQuery = bindArgs.jsPageUrl.query;
                if ( jsPageUrlQuery != null && jsPageUrlQuery.length > 0 )
                {
                    modUrl = modUrl.toString() + "&" + jsPageUrlQuery;
                }
            }
        }
        
        if ( bindArgs )
            bindArgs.url = modUrl.toString();
        return modUrl;
    },

    _submitAjaxApi: function( /* String */ action, /* String */ queryStringFragment, contentListener )
    {
        var jsObj = jetspeed;
        var queryString = "?action=" + action + "&id=" + this.entityId + queryStringFragment;

        var psmlActionUrl = jsObj.url.basePortalUrl() + jsObj.url.path.AJAX_API + jsObj.page.getPath() + queryString;
        var mimetype = "text/xml";

        var ajaxApiContext = new jsObj.om.Id( action, this.entityId );
        ajaxApiContext.portlet = this;

        jsObj.url.retrieveContent( { url: psmlActionUrl, mimetype: mimetype }, contentListener, ajaxApiContext, jsObj.debugContentDumpIds );
    },

    submitWinState: function( /* boolean */ volatileOnly, /* boolean */ reset )
    {
        var jsObj = jetspeed;
        var jsId = jsObj.id;
        if ( jsObj.page.isUA() || ( ! ( jsObj.page.getPageAction( jsId.ACT_EDIT ) || jsObj.page.getPageAction( jsId.ACT_VIEW ) || jsObj.page.canNPE() ) ) )
            return;
        var changedStateResult = null;
        if ( reset )
            changedStateResult = { state: this._initWinState( null, true ) };
        else
            changedStateResult = this._getChangedWindowState( volatileOnly );
        if ( changedStateResult )
        {
            var changedState = changedStateResult.state;
            
            var windowPositionStatic = changedState[ jsId.PP_WINDOW_POSITION_STATIC ];
            var windowHeightToFit = changedState[ jsId.PP_WINDOW_HEIGHT_TO_FIT ];

            var windowExtendedProperty = null;
            if ( changedStateResult.extendedPropChanged )
            {
                var propSep = jsId.PP_PROP_SEPARATOR;
                var pairSep = jsId.PP_PAIR_SEPARATOR;
                windowExtendedProperty = jsId.PP_STATICPOS + propSep + windowPositionStatic.toString();
                windowExtendedProperty += pairSep + jsId.PP_FITHEIGHT + propSep + windowHeightToFit.toString();
                windowExtendedProperty = escape( windowExtendedProperty );
            }

            var queryStringFragment = "";
            var action = null;
            if ( windowPositionStatic )
            {
                action = "moveabs";
                if ( changedState.column != null )
                    queryStringFragment += "&col=" + changedState.column;
                if ( changedState.row != null )
                    queryStringFragment += "&row=" + changedState.row;
                if ( changedState.layout != null )
                    queryStringFragment += "&layoutid=" + changedState.layout;
                if ( changedState.height != null )
                    queryStringFragment += "&height=" + changedState.height;
            }
            else
            {
                action = "move";
                if ( changedState.zIndex != null )
                    queryStringFragment += "&z=" + changedState.zIndex;
                if ( changedState.width != null )
                    queryStringFragment += "&width=" + changedState.width;
                if ( changedState.height != null )
                    queryStringFragment += "&height=" + changedState.height;
                if ( changedState.left != null )
                    queryStringFragment += "&x=" + changedState.left;
                if ( changedState.top != null )
                    queryStringFragment += "&y=" + changedState.top;
            }
            if ( windowExtendedProperty != null )
                queryStringFragment += "&" + jsId.PP_DESKTOP_EXTENDED + "=" + windowExtendedProperty;

            this._submitAjaxApi( action, queryStringFragment, new jsObj.om.MoveApiCL( this, changedState ) );

            if ( ! volatileOnly && ! reset )
            {
                if ( ! windowPositionStatic && changedStateResult.zIndexChanged )  // current condition for whether 
                {                                                                  // volatile (zIndex) changes are possible
                    var portletArray = jsObj.page.getPortletArray();
                    if ( portletArray && ( portletArray.length -1 ) > 0 )
                    {
                        for ( var i = 0 ; i < portletArray.length ; i++ )
                        {
                            var tPortlet = portletArray[i];
                            if ( tPortlet && tPortlet.entityId != this.entityId )
                            {
                                if ( ! tPortlet.properties[ jsObj.id.PP_WINDOW_POSITION_STATIC ] )
                                    tPortlet.submitWinState( true );
                            }
                        }
                    }
                }
                else if ( windowPositionStatic )
                {
                    // moveapi submission adjusts other portlets that have had their row changed because of portlet inserted or removed from higher row
                }
            }
        }
    },
    retrieveContent: function( contentListener, bindArgs, suppressGetActions )
    {
        if ( contentListener == null )
            contentListener = new jetspeed.om.PortletCL( this, suppressGetActions, bindArgs );

        if ( ! bindArgs )
            bindArgs = {};
        
        var portlet = this ;
        portlet.getPortletUrl( bindArgs ) ;
        
        this.contentRetriever.getContent( bindArgs, contentListener, portlet, jetspeed.debugContentDumpIds );
    },
    setPortletContent: function( portletContent, renderUrl, portletTitle )
    {
        var windowWidget = this.getPWin();
        if ( portletTitle != null && portletTitle.length > 0 )
        {
            this.properties[ jetspeed.id.PP_WINDOW_TITLE ] = portletTitle;
            if ( windowWidget && ! this.loadingIndicatorIsShown() )
                windowWidget.setPortletTitle( portletTitle );
        }
        if ( windowWidget )
        {
            windowWidget.setPortletContent( portletContent, renderUrl );
        }
    },
    loadingIndicatorIsShown: function()
    {
        var jsId = jetspeed.id;
        var actionlabel1 = this._getLoadingActionLabel( jsId.ACT_LOAD_RENDER );
        var actionlabel2 = this._getLoadingActionLabel( jsId.ACT_LOAD_ACTION );
        var actionlabel3 = this._getLoadingActionLabel( jsId.ACT_LOAD_UPDATE );
        var windowWidget = this.getPWin();
        if ( windowWidget && ( actionlabel1 || actionlabel2 ) )
        {
            var windowTitle = windowWidget.getPortletTitle();
            if ( windowTitle && ( windowTitle == actionlabel1 || windowTitle == actionlabel2 ) )
                return true;
        }
        return false;
    },
    _getLoadingActionLabel: function( actionName )
    {
        var actionlabel = null;
        if ( jetspeed.prefs != null && jetspeed.prefs.desktopActionLabels != null )
        {
            actionlabel = jetspeed.prefs.desktopActionLabels[ actionName ];
            if ( actionlabel != null && actionlabel.length == 0 )
                actionlabel = null;
        }
        return actionlabel;
    },  
    loadingIndicatorShow: function( actionName )
    {
        if ( actionName && ! this.loadingIndicatorIsShown() )
        {
            var actionlabel = this._getLoadingActionLabel( actionName );
            var windowWidget = this.getPWin();
            if ( windowWidget && actionlabel )
            {
                windowWidget.setPortletTitle( actionlabel );
            }
        }
    },
    loadingIndicatorHide: function()
    {
        var windowWidget = this.getPWin();
        if ( windowWidget )
        {
            windowWidget.setPortletTitle( this.properties[ jetspeed.id.PP_WINDOW_TITLE ] );
        }
    },

    getId: function()  // jetspeed.om.Id protocol
    {
        return this.entityId;
    },

    getProperty: function( name )
    {
        return this.properties[ name ];
    },
    getProperties: function()
    {
        return this.properties;
    },

    renderAction: function( actionName, actionUrlOverride )
    {
        var jsObj = jetspeed;
        var jsUrl = jsObj.url;
        var action = null;
        if ( actionName != null )
            action = this.getAction( actionName );
        var actionUrl = actionUrlOverride;
        if ( actionUrl == null && action != null )
            actionUrl = action.url;
        if ( actionUrl == null ) return;
        var renderActionUrl = jsUrl.basePortalUrl() + jsUrl.path.PORTLET + "/" + actionUrl + jsObj.page.getPath();
        if ( actionName != jsObj.id.ACT_PRINT )
            this.retrieveContent( null, { url: renderActionUrl } );
        else
        {
            var printmodeUrl = jsObj.page.getPageUrl();
            printmodeUrl = jsUrl.addQueryParameter( printmodeUrl, "jsprintmode", "true" );
            printmodeUrl = jsUrl.addQueryParameter( printmodeUrl, "jsaction", escape( action.url ) );
            printmodeUrl = jsUrl.addQueryParameter( printmodeUrl, "jsentity", this.entityId );
            printmodeUrl = jsUrl.addQueryParameter( printmodeUrl, "jslayoutid", this.lastSavedWindowState.layout );
            window.open( printmodeUrl.toString(), "jsportlet_print", "status,scrollbars,resizable,menubar" );
        }
    },
    getAction: function( name )
    {
        if ( this.actions == null ) return null;
        return this.actions[ name ];
    },
    getCurrentActionState: function()
    {
        return this.currentActionState;
    },
    getCurrentActionMode: function()
    {
        return this.currentActionMode;
    },
    updateActions: function( actions, currentActionState, currentActionMode )
    {
        if ( actions )
            this.actions = actions;
        else
            this.actions = {};

        this.currentActionState = currentActionState;
        this.currentActionMode = currentActionMode;

        this.syncActions();
    },
    syncActions: function()
    {
        var jsObj = jetspeed;
        jsObj.page.setPageModePortletActions( this );
        var windowWidget = this.getPWin();
        if ( windowWidget )
        {
            windowWidget.actionBtnSync( jsObj, jsObj.id );
        }
    }
}); // jetspeed.om.Portlet

jetspeed.om.ActionRenderFormBind = function( /* HtmlForm */ form, /* String */ url, /* String */ portletEntityId, /* String */ submitOperation )
{
    dojo.io.FormBind.call( this, { url: url, formNode: form } );

    this.entityId = portletEntityId;
    this.submitOperation = submitOperation;
    this.formSubmitInProgress = false;
};
dojo.inherits( jetspeed.om.ActionRenderFormBind, dojo.io.FormBind );
dojo.lang.extend( jetspeed.om.ActionRenderFormBind,
{
    init: function(args)
    {
        var form = dojo.byId( args.formNode );

        if(!form || !form.tagName || form.tagName.toLowerCase() != "form") {
            throw new Error("FormBind: Couldn't apply, invalid form");
        } else if(this.form == form) {
            return;
        } else if(this.form) {
            throw new Error("FormBind: Already applied to a form");
        }

        dojo.lang.mixin(this.bindArgs, args);
        this.form = form;

        this.eventConfMgr( false );

        form.oldSubmit = form.submit;  // Isn't really used anymore, but cache it
        form.submit = function()
        {
            form.onsubmit();
        };
    },

    eventConfMgr: function( disconnect )
    {
        var fn = (disconnect) ? "disconnect" : "connect";
        var djEvt = dojo.event;
        var form = this.form;
        djEvt[ fn ]( "after", form, "onsubmit", this, "submit", null );

        for(var i = 0; i < form.elements.length; i++) {
            var node = form.elements[i];
            if(node && node.type && dojo.lang.inArray(["submit", "button"], node.type.toLowerCase())) {
                djEvt[ fn ]( "after", node, "onclick", this, "click", null );
            }
        }

        var inputs = form.getElementsByTagName("input");
        for(var i = 0; i < inputs.length; i++) {
            var input = inputs[i];
            if(input.type.toLowerCase() == "image" && input.form == form) {
                djEvt[ fn ]( "after", input, "onclick", this, "click", null );
            }
        }

        var as = form.getElementsByTagName("a");
        for(var i = 0; i < as.length; i++) {
            djEvt[ fn ]( "before", as[i], "onclick", this, "click", null );
        }
    },

    onSubmit: function( cForm )
    {
        var proceed = true;
        if ( this.isFormSubmitInProgress() )
            proceed = false;
        else if ( jetspeed.debug.confirmOnSubmit )
        {
            if ( ! confirm( "Click OK to submit." ) )
            {
                proceed = false;
            }
        }
        return proceed;
    },

    submit: function( e )
    {
        if ( e )
		    e.preventDefault();
        if ( this.isFormSubmitInProgress() )
        {
            // do nothing
        }
		else if( this.onSubmit( this.form ) )
        {
            var parsedPseudoUrl = jetspeed.portleturl.parseContentUrl( this.form.action );

            var mixInBindArgs = {};
            if ( parsedPseudoUrl.operation == jetspeed.portleturl.PORTLET_REQUEST_ACTION || parsedPseudoUrl.operation == jetspeed.portleturl.PORTLET_REQUEST_RENDER )
            {   // form action set via script
                var replacementActionUrl = jetspeed.portleturl.genPseudoUrl( parsedPseudoUrl, true );
                this.form.action = replacementActionUrl;
                this.submitOperation = parsedPseudoUrl.operation;
                this.entityId = parsedPseudoUrl.portletEntityId;
                mixInBindArgs.url = parsedPseudoUrl.url;
            }

            if ( this.submitOperation == jetspeed.portleturl.PORTLET_REQUEST_RENDER || this.submitOperation == jetspeed.portleturl.PORTLET_REQUEST_ACTION )
            {
                this.isFormSubmitInProgress( true );
                mixInBindArgs.formFilter = dojo.lang.hitch( this, "formFilter" );
                mixInBindArgs.submitFormBindObject = this;
                if ( this.submitOperation == jetspeed.portleturl.PORTLET_REQUEST_RENDER )
                {
                    jetspeed.doRender( dojo.lang.mixin(this.bindArgs, mixInBindArgs ), this.entityId );
                }
                else
                {
                    jetspeed.doAction( dojo.lang.mixin(this.bindArgs, mixInBindArgs ), this.entityId );
                }
            }
            else
            {
                //var errMsg = "ActionRenderFormBind.submit cannot process form submit with action:" + this.form.action;
                //alert( errMsg );
            }
		}
	},
    isFormSubmitInProgress: function( setVal )
    {
        if ( setVal != undefined )
        {
            this.formSubmitInProgress = setVal;
        }
        return this.formSubmitInProgress;
    }
});


// ... jetspeed.om.PortletCL
jetspeed.om.PortletCL = function( /* Portlet */ portlet, suppressGetActions, bindArgs )
{
    this.portlet = portlet;
    this.suppressGetActions = suppressGetActions;
    this.formbind = null;
    if ( bindArgs != null && bindArgs.submitFormBindObject != null )
    {
        this.formbind = bindArgs.submitFormBindObject;
    }
    this._loading( true );
};
jetspeed.om.PortletCL.prototype =
{
    _loading: function( /* boolean */ showLoading )
    {
        if ( this.portlet == null ) return;
        if ( showLoading )
            this.portlet.loadingIndicatorShow( jetspeed.id.ACT_LOAD_RENDER );
        else
            this.portlet.loadingIndicatorHide();
    },
    notifySuccess: function( /* String */ portletContent, /* String */ requestUrl, /* Portlet */ portlet, http )
    {
        var portletTitle = null;
        if ( http != null )
        {
            try {
                portletTitle = http.getResponseHeader("JS_PORTLET_TITLE");
            } catch (ignore) { // might happen in older mozilla
            }
            if ( portletTitle != null )
                portletTitle = unescape( portletTitle );
        }
        portlet.setPortletContent( portletContent, requestUrl, portletTitle );
        if ( this.suppressGetActions == null || this.suppressGetActions == false )
            jetspeed.getActionsForPortlet( portlet.getId() );
        else
            this._loading( false );
        if ( this.formbind != null )
        {
            this.formbind.isFormSubmitInProgress( false );
        }
    },
    notifyFailure: function( /* String */ type, /* Object */ error, /* String */ requestUrl, /* Portlet */ portlet )
    {
        this._loading( false );
        if ( this.formbind != null )
        {
            this.formbind.isFormSubmitInProgress( false );
        }
        dojo.raise( "PortletCL notifyFailure url: " + requestUrl + " type: " + type + jetspeed.formatError( error ) );
    }
};

// ... jetspeed.om.PortletActionCL
jetspeed.om.PortletActionCL = function( /* Portlet */ portlet, bindArgs )
{
    this.portlet = portlet;
    this.formbind = null;
    if ( bindArgs != null && bindArgs.submitFormBindObject != null )
    {
        this.formbind = bindArgs.submitFormBindObject;
    }
    this._loading( true );
};
jetspeed.om.PortletActionCL.prototype =
{
    _loading: function( /* boolean */ showLoading )
    {
        if ( this.portlet == null ) return;
        if ( showLoading )
            this.portlet.loadingIndicatorShow( jetspeed.id.ACT_LOAD_ACTION );
        else
            this.portlet.loadingIndicatorHide();
    },
    notifySuccess: function( /* String */ portletContent, /* String */ requestUrl, /* Portlet */ portlet, http )
    {
        var jsObj = jetspeed;
        var renderUrl = null;
        var navigatedPage = false;
        var parsedPseudoUrl = jsObj.portleturl.parseContentUrl( portletContent );
        if ( parsedPseudoUrl.operation == jsObj.portleturl.PORTLET_REQUEST_ACTION || parsedPseudoUrl.operation == jsObj.portleturl.PORTLET_REQUEST_RENDER )
        {
            if ( jsObj.debug.doRenderDoAction )
                dojo.debug( "PortletActionCL " + parsedPseudoUrl.operation + "-url in response body: " + portletContent + "  url: " + parsedPseudoUrl.url + " entity-id: " + parsedPseudoUrl.portletEntityId ) ;
            renderUrl = parsedPseudoUrl.url;
        }
        else
        {
            if ( jsObj.debug.doRenderDoAction )
                dojo.debug( "PortletActionCL other-url in response body: " + portletContent )
            renderUrl = portletContent;
            if ( renderUrl )
            {
                var portletUrlPos = renderUrl.indexOf( jsObj.url.basePortalUrl() + jsObj.url.path.PORTLET );
                if ( portletUrlPos == -1 )
                {
                    //dojo.debug( "PortletActionCL window.location.href navigation=" + renderUrl );
                    navigatedPage = true;
                    window.location.href = renderUrl;
                    renderUrl = null;
                }
                else if ( portletUrlPos > 0 )
                {
                    this._loading( false );
                    dojo.raise( "Cannot interpret portlet url in action response: " + portletContent );
                    renderUrl = null;
                }
            }
        }
        if ( renderUrl != null && ! jsObj.noActionRender )
        {
            if ( jsObj.debug.doRenderDoAction )
                dojo.debug( "PortletActionCL starting portlet-renderer with renderUrl=" + renderUrl );
            var renderer = new jetspeed.PortletRenderer( false, false, false, renderUrl, true );
            renderer.renderAll();
        }
        else
        {
            this._loading( false );
        }
        if ( ! navigatedPage && this.portlet )
            jsObj.getActionsForPortlet( this.portlet.entityId );
        if ( this.formbind != null )
        {
            this.formbind.isFormSubmitInProgress( false );
        }
    },
    notifyFailure: function( /* String */ type, /* Object */ error, /* String */ requestUrl, /* Portlet */ portlet )
    {
        this._loading( false );
        if ( this.formbind != null )
        {
            this.formbind.isFormSubmitInProgress( false );
        }
        dojo.raise( "PortletActionCL notifyFailure type: " + type + jetspeed.formatError( error ) );
    }
};

// ... jetspeed.om.MenuOption
jetspeed.om.MenuOption = function()
{
};
dojo.lang.extend( jetspeed.om.MenuOption,
{
    // operations
    navigateTo: function()
    {
        if ( this.isLeaf() )
        {
            var navUrl = this.getUrl();
            if ( navUrl )
            {
                var jsObj = jetspeed;
                if ( ! jsObj.prefs.ajaxPageNavigation || jsObj.url.urlStartsWithHttp( navUrl ) )
                {
                    jsObj.pageNavigate( navUrl, this.getTarget() );
                }
                else
                {
                    jsObj.updatePage( navUrl );
                }
            }
        }
    },
    navigateUrl: function()
    {
        return jetspeed.page.makePageUrl( this.getUrl() );
    },

    // data
    getType: function()
    {
        return this.type;
    },
    getTitle: function()
    {
        return this.title;
    },
    getShortTitle: function()
    {
        return this[ "short-title" ];
    },
    getSkin: function()
    {
        return this.skin;
    },
    getUrl: function()
    {
        return this.url;
    },
    getTarget: function()
    {
        return this.target;
    },
    getHidden: function()
    {
        return this.hidden;
    },
    getSelected: function()
    {
        return this.selected;
    },
    getText: function()
    {
        return this.text;
    },
    isLeaf: function()
    {
        return true;
    },
    isMenu: function()
    {
        return false;
    },
    isSeparator: function()
    {
        return false;
    }
});
// ... jetspeed.om.MenuOptionSeparator
jetspeed.om.MenuOptionSeparator = function()
{
};
dojo.inherits( jetspeed.om.MenuOptionSeparator, jetspeed.om.MenuOption);
dojo.lang.extend( jetspeed.om.MenuOptionSeparator,
{
    isSeparator: function()
    {
        return true;
    }
});
// ... jetspeed.om.Menu
jetspeed.om.Menu = function( /* String */ menuName, /* String */ menuType )
{
    this._is_parsed = false;
    this.name = menuName;
    this.type = menuType;
};
dojo.inherits( jetspeed.om.Menu, jetspeed.om.MenuOption);
dojo.lang.extend( jetspeed.om.Menu,
{
    setParsed: function()
    {
        this._is_parsed = true;
    },
    isParsed: function()
    {
        return this._is_parsed;
    },
    getName: function()
    {
        return this.name;
    },
    addOption: function( /* MenuOption */ menuOptionObj )
    {
        if ( ! menuOptionObj ) return;
        if ( ! this.options )
            this.options = new Array();
        this.options.push( menuOptionObj );
    },
    getOptions: function()
    {
        var tAry = new Array();
        return ( this.options ? tAry.concat( this.options ) : tAry );
    },
    getOptionByIndex: function( optionIndex )
    {
        if ( ! this.hasOptions() ) return null;
        if ( optionIndex == 0 || optionIndex > 0 )
        {
            if ( optionIndex >= this.options.length )
                dojo.raise( "Menu.getOptionByIndex index out of bounds" );
            else
                return this.options[ optionIndex ];
        }
    },
    hasOptions: function()
    {
        return ( ( this.options && this.options.length > 0 ) ? true : false );
    },
    isMenu: function()
    {
        return true;
    },
    isLeaf: function()
    {
        return false;
    },
    hasNestedMenus: function()
    {
        if ( ! this.options ) return false;
        for ( var i = 0; i < this.options.length ; i++ )
        {
            var mOptObj = this.options[i];
            if ( mOptObj instanceof jetspeed.om.Menu )
                return true;
        }
        return false;
    }
    
});

// ... jetspeed.om.MenuApiCL
jetspeed.om.MenuApiCL = function()
{
};
dojo.lang.extend( jetspeed.om.MenuApiCL,
{
    notifySuccess: function( /* XMLDocument */ data, /* String */ requestUrl, domainModelObject )
    {
        var menuObj = this.parseMenu( data, domainModelObject.menuName, domainModelObject.menuType );
        domainModelObject.page.putMenu( menuObj );
    },
    notifyFailure: function( /* String */ type, /* Object */ error, /* String */ requestUrl, domainModelObject )
    {
        this.notifyCount++;
        dojo.raise( "MenuApiCL error [" + domainModelObject.toString() + "] url: " + requestUrl + " type: " + type + jetspeed.formatError( error ) );
    },

    parseMenu: function( /* XMLNode */ node, /* String */ menuName, /* String */ menuType )
    {
        var menu = null;
        var jsElements = node.getElementsByTagName( "js" );
        if ( ! jsElements || jsElements.length > 1 )
            dojo.raise( "Expected one <js> in menu xml" );
        var children = jsElements[0].childNodes;
        
        for ( var i = 0 ; i < children.length ; i++ )
        {
            var child = children[i];
            if ( child.nodeType != 1 )   // dojo.dom.ELEMENT_NODE
                continue;
            var childLName = child.nodeName;
            if ( childLName == "menu" )
            {
                if ( menu != null )
                    dojo.raise( "Expected one root <menu> in menu xml" );
                menu = this.parseMenuObject( child, new jetspeed.om.Menu() );
            }
        }
        if ( menu != null )
        {
            if ( menu.name == null )
                menu.name == menuName;
            if ( menu.type == null )
                menu.type = menuType;
        }
        return menu;
    },
    parseMenuObject: function( /* XMLNode */ node, /* MenuOption */ mObj )
    {
        var constructObj = null;
        var children = node.childNodes;
        for ( var i = 0 ; i < children.length ; i++ )
        {
            var child = children[i];
            if ( child.nodeType != 1 )   // dojo.dom.ELEMENT_NODE
                continue;
            var childLName = child.nodeName;
            if ( childLName == "menu" )
            {
                if ( mObj.isLeaf() )
                    dojo.raise( "Unexpected nested <menu>" );
                else
                    mObj.addOption( this.parseMenuObject( child, new jetspeed.om.Menu() ) );
            }
            else if ( childLName == "option" )
            {
                if ( mObj.isLeaf() )
                    dojo.raise( "Unexpected nested <option>" );
                else
                    mObj.addOption( this.parseMenuObject( child, new jetspeed.om.MenuOption() ) );
            }
            else if ( childLName == "separator" )
            {
                if ( mObj.isLeaf() )
                    dojo.raise( "Unexpected nested <separator>" );
                else
                    mObj.addOption( this.parseMenuObject( child, new jetspeed.om.MenuOptionSeparator() ) );
            }
            else if ( childLName )
                mObj[ childLName ] = ( ( child && child.firstChild ) ? child.firstChild.nodeValue : null );
        }
        if ( mObj.setParsed )
            mObj.setParsed();
        return mObj;
    }
});

// ... jetspeed.om.MenusApiCL
jetspeed.om.MenusApiCL = function( /* boolean */ includeMenuDefs, /* boolean */ isPageUpdate, /* Object */ initEditModeConf )
{
    this.includeMenuDefs = includeMenuDefs;
    this.isPageUpdate = isPageUpdate;
    this.initEditModeConf = initEditModeConf;
};
dojo.inherits( jetspeed.om.MenusApiCL, jetspeed.om.MenuApiCL);
dojo.lang.extend( jetspeed.om.MenusApiCL,
{
    notifySuccess: function( /* XMLDocument */ data, /* String */ requestUrl, domainModelObject )
    {
        var menuDefs = this.getMenuDefs( data, requestUrl, domainModelObject );
        for ( var i = 0 ; i < menuDefs.length; i++ )
        {
            var mObj = menuDefs[i];
            domainModelObject.page.putMenu( mObj );
        }
        this.notifyFinished( domainModelObject );
    },
    getMenuDefs: function( /* XMLDocument */ data, /* String */ requestUrl, domainModelObject )
    {
        var menuDefs = [];
        var menuDefElements = data.getElementsByTagName( "menu" );
        for( var i = 0; i < menuDefElements.length; i++ )
        {
            var menuType = menuDefElements[i].getAttribute( "type" );
            if ( this.includeMenuDefs )
                menuDefs.push( this.parseMenuObject( menuDefElements[i], new jetspeed.om.Menu( null, menuType ) ) );
            else
            {
                var menuName = menuDefElements[i].firstChild.nodeValue;
                menuDefs.push( new jetspeed.om.Menu( menuName, menuType ) );
            }
        }
        return menuDefs;
    },
    
    notifyFailure: function( /* String */ type, /* Object */ error, /* String */ requestUrl, domainModelObject )
    {
        dojo.raise( "MenusApiCL error [" + domainModelObject.toString() + "] url: " + requestUrl + " type: " + type + jetspeed.formatError( error ) );
    },

    notifyFinished: function( domainModelObject )
    {
        var jsObj = jetspeed;
        if ( this.includeMenuDefs )
            jsObj.notifyRetrieveAllMenusFinished( this.isPageUpdate, this.initEditModeConf );

        jsObj.page.loadPostRetrieveMenus( this.isPageUpdate, this.initEditModeConf );

        if ( djConfig.isDebug && jsObj.debug.profile )
        {
            dojo.profile.end( "loadFromPSML" );
            if ( ! this.isPageUpdate )
                dojo.profile.end( "initializeDesktop" );
            else
                dojo.profile.end( "updatePage" );
            dojo.profile.debugAllItems( true );
            dojo.debug( "-------------------------" );
        }
    }
});

// ... jetspeed.om.PortletChangeActionCL
jetspeed.om.PortletChangeActionCL = function( /* String */ portletEntityId )
{
    this.portletEntityId = portletEntityId;
    this._loading( true );
};
dojo.lang.extend( jetspeed.om.PortletChangeActionCL,
{
    notifySuccess: function( /* XMLDocument */ data, /* String */ requestUrl, domainModelObject )
    {
        if ( jetspeed.url.checkAjaxApiResponse( requestUrl, data, null, true, "portlet-change-action" ) )
            jetspeed.getActionsForPortlet( this.portletEntityId );
        else
            this._loading( false );
    },
    _loading: function( /* boolean */ showLoading )
    {
        var portlet = jetspeed.page.getPortlet( this.portletEntityId ) ;
        if ( portlet )
        {
            if ( showLoading )
                portlet.loadingIndicatorShow( jetspeed.id.ACT_LOAD_UPDATE );
            else
                portlet.loadingIndicatorHide();
        }
    },
    notifyFailure: function( /* String */ type, /* Object */ error, /* String */ requestUrl, domainModelObject )
    {
        this._loading( false );
        dojo.raise( "PortletChangeActionCL error [" + domainModelObject.toString() + "] url: " + requestUrl + " type: " + type + jetspeed.formatError( error ) );
    }
});

// ... jetspeed.om.PageChangeActionCL
jetspeed.om.PageChangeActionCL = function( /* String */ pageActionUrl )
{
    this.pageActionUrl = pageActionUrl;
};
dojo.lang.extend( jetspeed.om.PageChangeActionCL,
{
    notifySuccess: function( /* XMLDocument */ data, /* String */ requestUrl, domainModelObject )
    {
        if ( jetspeed.url.checkAjaxApiResponse( requestUrl, data, null, true, "page-change-action" ) )
        {
            if ( this.pageActionUrl != null && this.pageActionUrl.length > 0 )
                jetspeed.pageNavigate( this.pageActionUrl ); 
        }
    },
    notifyFailure: function( /* String */ type, /* Object */ error, /* String */ requestUrl, domainModelObject )
    {
        dojo.raise( "PageChangeActionCL error [" + domainModelObject.toString() + "] url: " + requestUrl + " type: " + type + jetspeed.formatError( error ) );
    }
});

// ... jetspeed.om.UserInfoCL
jetspeed.om.UserInfoCL = function()
{
};
dojo.lang.extend( jetspeed.om.UserInfoCL,
{
    notifySuccess: function( /* XMLDocument */ data, /* String */ requestUrl, domainModelObject )
    {
        var jsObj = jetspeed;
        if ( jsObj.url.checkAjaxApiResponse( requestUrl, data, null, false, "user-info" ) )
        {
            var jsElements = data.getElementsByTagName( "js" );
            if ( jsElements && jsElements.length == 1 )
            {
                var root = jsElements[0];
                var un = jsObj.page._parsePSMLChildOrAttr( root, "username" );
                var rMap = {};
                var roleNodes = root.getElementsByTagName( "role" );
                if ( roleNodes != null )
                {
                    for ( var i = 0 ; i < roleNodes.length ; i++ )
                    {
                        var role = ( roleNodes[i].firstChild ? roleNodes[i].firstChild.nodeValue : null );
                        if ( role )
                            rMap[ role ] = role;
                    }
                }
                jsObj.page._setU( { un: un, r: rMap } );
                //dojo.debug( "user-info name=" + un  + " roles=" + jetspeed.printobj( rMap ) );
            }
        }
    },
    notifyFailure: function( /* String */ type, /* Object */ error, /* String */ requestUrl, domainModelObject )
    {
        dojo.raise( "UserInfoCL error [" + domainModelObject.toString() + "] url: " + requestUrl + " type: " + type + jetspeed.formatError( error ) );
    }
});

// ... jetspeed.om.PortletActionsCL
jetspeed.om.PortletActionsCL = function( /* String[] */ portletEntityIds )
{
    this.portletEntityIds = portletEntityIds;
    this._loading( true );
};
dojo.lang.extend( jetspeed.om.PortletActionsCL,
{
    _loading: function( /* boolean */ showLoading )
    {
        if ( this.portletEntityIds == null || this.portletEntityIds.length == 0 ) return ;
        for ( var i = 0 ; i < this.portletEntityIds.length ; i++ )
        {
            var portlet = jetspeed.page.getPortlet( this.portletEntityIds[i] ) ;
            if ( portlet )
            {
                if ( showLoading )
                    portlet.loadingIndicatorShow( jetspeed.id.ACT_LOAD_UPDATE );
                else
                    portlet.loadingIndicatorHide();
            }
        }
    },
    notifySuccess: function( /* XMLDocument */ data, /* String */ requestUrl, domainModelObject )
    {
        var jsObj = jetspeed;
        this._loading( false );
        if ( jsObj.url.checkAjaxApiResponse( requestUrl, data, null, true, "portlet-actions" ) )
        {
            this.processPortletActionsResponse( data, jsObj.page );
        }
    },
    processPortletActionsResponse: function( /* XMLNode */ node, jsPage )
    {   // derived class should override this method
        var results = this.parsePortletActionsResponse( node, jsPage );
        for ( var i = 0 ; i < results.length ; i++ )
        {
            var resultsObj = results[i];
            var entityId = resultsObj.id;
            var portlet = jsPage.getPortlet( entityId );
            if ( portlet != null )
                portlet.updateActions( resultsObj.actions, resultsObj.currentActionState, resultsObj.currentActionMode );
        }
    },

    parsePortletActionsResponse: function( /* XMLNode */ node, jsPage )
    {
        var results = new Array();
        var jsElements = node.getElementsByTagName( "js" );
        if ( ! jsElements || jsElements.length > 1 )
        {
            dojo.raise( "Expected one <js> in portlet selector xml" );
            return results;
        }
        var children = jsElements[0].childNodes;
        for ( var i = 0 ; i < children.length ; i++ )
        {
            var child = children[i];
            if ( child.nodeType != 1 )   // dojo.dom.ELEMENT_NODE
                continue;
            var childLName = child.nodeName;
            if ( childLName == "portlets" )
            {
                var portletsNode = child ;
                var portletChildren = portletsNode.childNodes ;
                for ( var pI = 0 ; pI < portletChildren.length ; pI++ )
                {
                    var pChild = portletChildren[pI];
                    if ( pChild.nodeType != 1 )   // dojo.dom.ELEMENT_NODE
                        continue;
                    var pChildLName = pChild.nodeName;
                    if ( pChildLName == "portlet" )
                    {
                        var portletResult = this.parsePortletElement( pChild, jsPage );
                        if ( portletResult != null )
                            results.push( portletResult );
                    }
                }
            }
        }
        return results;
    },
    parsePortletElement: function( /* XMLNode */ node, jsPage )
    {
        var portletId = node.getAttribute( "id" );
        if ( portletId != null )
        {
            var actions = jsPage._parsePSMLActions( node, null );
            var currentActionState = jsPage._parsePSMLChildOrAttr( node, "state" );
            var currentActionMode = jsPage._parsePSMLChildOrAttr( node, "mode" );
            return { id: portletId, actions: actions, currentActionState: currentActionState, currentActionMode: currentActionMode };
        }
        return null;
    },
    notifyFailure: function( /* String */ type, /* Object */ error, /* String */ requestUrl, domainModelObject )
    {
        this._loading( false );
        dojo.raise( "PortletActionsCL error [" + domainModelObject.toString() + "] url: " + requestUrl + " type: " + type + jetspeed.formatError( error ) );
    }
});

// ... jetspeed.om.MoveApiCL
jetspeed.om.MoveApiCL = function( /* Portlet */ portlet, changedState )
{
    this.portlet = portlet;
    this.changedState = changedState;
    this._loading( true );
};
jetspeed.om.MoveApiCL.prototype =
{
    _loading: function( /* boolean */ showLoading )
    {
        if ( this.portlet == null ) return;
        if ( showLoading )
            this.portlet.loadingIndicatorShow( jetspeed.id.ACT_LOAD_UPDATE );
        else
            this.portlet.loadingIndicatorHide();
    },
    notifySuccess: function( /* String */ data, /* String */ requestUrl, domainModelObject )
    {
        var jsObj = jetspeed;
        this._loading( false );
        dojo.lang.mixin( domainModelObject.portlet.lastSavedWindowState, this.changedState );
        var reportError = true;    // BOZO:NOW:   set back to false!!!!!!
        if ( djConfig.isDebug && jsObj.debug.submitWinState )
            reportError = true;
        var successIndicator = jsObj.url.checkAjaxApiResponse( requestUrl, data, [ "refresh" ], reportError, ("move-portlet [" + domainModelObject.portlet.entityId + "]"), jsObj.debug.submitWinState );
        if ( successIndicator == "refresh" )
        {
            var navUrl = jsObj.page.getPageUrl();
            if ( ! jsObj.prefs.ajaxPageNavigation )
            {
                jsObj.pageNavigate( navUrl, null, true );
            }
            else
            {
                jsObj.updatePage( navUrl, false, true );
            }
        }
    },
    notifyFailure: function( /* String */ type, /* Object */ error, /* String */ requestUrl, domainModelObject )
    {
        this._loading( false );
        dojo.debug( "submitWinState error [" + domainModelObject.entityId + "] url: " + requestUrl + " type: " + type + jetspeed.formatError( error ) );
    }
};

// ... script content annotation methods

jetspeed.postload_addEventListener = function( node, eventType, fnc, useCapture )
{
    if ( ( eventType == "load" || eventType == "DOMContentLoaded" || eventType == "domready" ) && ( node == window || node == document || node == document.body ) )
        fnc();
    else
        node.addEventListener( eventType, fnc, useCapture );
};

jetspeed.postload_attachEvent = function( node, eventType, fnc )
{
    if ( eventType == "onload" && ( node == window || node == document || node == document.body ) )
        fnc();
    else
        node.attachEvent( eventType, fnc );
};

jetspeed.postload_docwrite = function( content )
{
    if ( ! content ) return;
    content = content.replace(/^\s+|\s+$/g, "");
    
    var scriptTagRegex = /^<script\b([^>]*)>.*?<\/script>/i
    var scriptMatch = scriptTagRegex.exec( content );
    if ( scriptMatch )
    {   // only know purpose of script in document.write is for IE DOMContentLoaded detection
        content = null;
        var scriptAttrs = scriptMatch[1];
        if ( scriptAttrs )
        {
            var idRegex = /\bid\s*=\s*([^\s]+)/i
            var idMatch = idRegex.exec( scriptAttrs );
            if ( idMatch )
            {   // if used for DOMContentLoaded detection, it must have id for event listener attachment
                var idVal = idMatch[1];
                content = '<img id=' + idVal + ' src="' + jetspeed.url.basePortalDesktopUrl() + '/javascript/jetspeed/desktop/pixel.gif' + '"/>';
                // " - trick emacs here after regex
            }
        }
    }
    var tn = null;
    if ( content )
    {
        var djObj = dojo;
        tn = djObj.doc().createElement("div");
        tn.style.visibility = "hidden";
        djObj.body().appendChild(tn);
        tn.innerHTML = content;
        tn.style.display = "none";
    }
    return tn;
};

jetspeed.setdoclocation = function( target, memberExpr, value )
{
    if ( target == document || target == window )
    {
        if ( value && value.length > 0 )
        {   // suppress navigation if url is action/render (which should never be used to navigate the window)
            //    wicket toolkit uses render/action for ajax requests - when this is combined with setTimeout and ajaxPageNavigation,
            //    the wicket toolkit can initiate a redirect (by setting document.location)
            var jsPUrl = jetspeed.portleturl;
            if ( value.indexOf( jsPUrl.DESKTOP_ACTION_PREFIX_URL ) != 0 && value.indexOf( jsPUrl.DESKTOP_RENDER_PREFIX_URL ) != 0 )
                target.location = value;
        }
    }
    else if ( target != null )
    {
        var dotPos = memberExpr.indexOf( "." );
        if ( dotPos == -1 )
            target[ memberExpr ] = value;
        else
        {
            var scope1NM = memberExpr.substring( 0, dotPos );
            var scope1 = target[ scope1NM ];
            if ( scope1 )
            {
                var scope2NM = memberExpr.substring( dotPos+1 );
                if ( scope2NM )
                    scope1[ scope2NM ] = value;
            }
        }
    }
};

jetspeed.addDummyScriptToHead = function( src )
{   // add script tag with src to head element for benefit of ajax toolkits which search
    //    script elements in head to determine if a library has been loaded (e.g. wicket)
    var script = document.createElement( "script" );
    script.setAttribute( "type", "text/plain" );
    script.setAttribute( "language", "ignore" );
    script.setAttribute( "src", src );
    document.getElementsByTagName( "head" )[0].appendChild( script );
    return script;
};

jetspeed.containsElement = function( nodeName, attrName, attrValue, container )
{
    if ( ! nodeName || ! attrName || ! attrValue ) return false;
    if ( ! container ) container = document;
	var nodes = container.getElementsByTagName( nodeName );
    if ( ! nodes ) return false;
	for ( var i = 0; i < nodes.length; ++i )
    {
		var nodeAttr = nodes[i].getAttribute( attrName );
        if ( nodeAttr == attrValue )
        {
            return true;
        }
	}
	return false;
};

// ... jetspeed.ui methods

jetspeed.ui = {
    initCssObj: function()
    {
        var cssBase = [ "display: ", "block", ";",
                        " cursor: ", "default", ";",
                        " width: ", "", "", ";",
                        "", "", "" ];

        var cssHeight = cssBase.concat( [ " height: ", "", "", ";" ] );

        var cssWidthHeight = [ "", "", "",
                               "", "", "",
                               "width: ", "", "", ";",
                               "", "", "",
                               " height: ", "", "", ";" ];
    
        var cssOverflow = cssHeight.concat( [ " overflow-y: ", "", ";",
                                              " overflow-x: ", "hidden", ";" ] );
    
        var cssPosition = cssOverflow.concat( [ " position: ", "relative", ";",
                                                " top: ", "auto", "", ";",
                                                " left: ", "auto", "", ";",
                                                " z-index: ", "", ";" ] );
    
        jetspeed.css = { cssBase: cssBase,
                         cssHeight: cssHeight,
                         cssWidthHeight: cssWidthHeight,
                         cssOverflow: cssOverflow,
                         cssPosition: cssPosition,
                         cssDis: 1,
                         cssCur: 4,
                         cssW: 7,
                         cssWU: 8,
                         cssNoSelNm: 10,
                         cssNoSel: 11,
                         cssNoSelEnd: 12,
                         cssH: 14,
                         cssHU: 15,
                         cssOy: 18,
                         cssOx: 21,
                         cssPos: 24,
                         cssT: 27,
                         cssTU: 28,
                         cssL: 31,
                         cssLU: 32,
                         cssZIndex: 35 };
    },
    
    getPWinAndColChildren: function( parentNode, matchNode, includeGhosts, includeColumns, includeLayouts, excludeWindows )
    {
        var djH = dojo.html;
        var jsId = jetspeed.id;
        var matchingNodes = null;
        var nodeMatchIndex = -1;
        var nodeMatchIndexInMatchingNodes = -1;
        var nodeTotal = -1;
        if ( parentNode )
        {
            var children = parentNode.childNodes;
            if ( children )
                nodeTotal = children.length;
            matchingNodes = [];
            
            if ( nodeTotal > 0 )
            {
                var includeClasses = "", excludeClasses = "";
                if ( ! excludeWindows )
                    includeClasses = jsId.PWIN_CLASS;
                if ( includeGhosts )
                    includeClasses += ( (includeClasses.length > 0) ? "|" : "" ) + jsId.PWIN_GHOST_CLASS;
                if ( includeColumns )
                    includeClasses += ( (includeClasses.length > 0) ? "|" : "" ) + jsId.COL_CLASS;
                if ( includeLayouts && ! includeColumns )  // layout node always has COL_CLASS
                    includeClasses += ( (includeClasses.length > 0) ? "|" : "" ) + jsId.COL_LAYOUTHEADER_CLASS;
                if ( includeColumns && ! includeLayouts )
                    excludeClasses = ( (excludeClasses.length > 0) ? "|" : "" ) + jsId.COL_LAYOUTHEADER_CLASS;

                if ( includeClasses.length > 0 )
                {
                    var inclRegEx = new RegExp('(^|\\s+)('+includeClasses+')(\\s+|$)');
                    var exclRegEx = null;
                    if ( excludeClasses.length > 0 )
                        exclRegEx = new RegExp('(^|\\s+)('+excludeClasses+')(\\s+|$)');
                    var child, childAdded, childClass;
                    for ( var i = 0 ; i < nodeTotal ; i++ )
                    {
                        child = children[i];
                        childAdded = false;
                        childClass = djH.getClass( child );
                        if ( inclRegEx.test( childClass ) && ( exclRegEx == null || ! exclRegEx.test( childClass ) ) )
                        {
                            matchingNodes.push( child );
                            childAdded = true;
                        }
                        
                        if ( matchNode && child == matchNode )
                        {
                            if ( ! childAdded )
                                matchingNodes.push( child );
                            nodeMatchIndex = i;
                            nodeMatchIndexInMatchingNodes = matchingNodes.length -1;
                        }
                    }
                }
            }
        }
        return { matchingNodes: matchingNodes, totalNodes: nodeTotal, matchNodeIndex: nodeMatchIndex, matchNodeIndexInMatchingNodes: nodeMatchIndexInMatchingNodes };
    },
    
    getPWinsFromNodes: function( /* DOM node [] */ portletWindowNodes )
    {
        var jsPage = jetspeed.page;
        var portletWindows = null;
        if ( portletWindowNodes )
        {
            portletWindows = new Array();
            for ( var i = 0 ; i < portletWindowNodes.length ; i++ )
            {
                var widget = jsPage.getPWin( portletWindowNodes[ i ].id );
                if ( widget )
                    portletWindows.push( widget ) ;
            }
        }
        return portletWindows;
    },
    
    createPortletWindow: function( windowConfigObject, columnIndex, jsObj )
    {
        var dbProfile = false;
        if ( djConfig.isDebug && jsObj.debug.profile )
        {
            dbProfile = true;
    	    dojo.profile.start( "createPortletWindow" );
        }
    
        var winPositionStatic = ( columnIndex != null );    
        var windowMakeAbsolute = false;
        var windowContainerNode = null;
        if ( winPositionStatic && columnIndex < jsObj.page.columns.length && columnIndex >= 0 )
            windowContainerNode = jsObj.page.columns[ columnIndex ].domNode;
        if ( windowContainerNode == null )
        {
            windowMakeAbsolute = true;
            windowContainerNode = document.getElementById( jsObj.id.DESKTOP );
        }
        if ( windowContainerNode == null ) return;
    
        var createWindowParams = {};
        if ( windowConfigObject.isPortlet )
        {
            createWindowParams.portlet = windowConfigObject;
            if ( jsObj.prefs.printModeOnly != null )
                createWindowParams.printMode = true;
            if ( windowMakeAbsolute )
                windowConfigObject.properties[ jsObj.id.PP_WINDOW_POSITION_STATIC ] = false ;
        }
        else
        {
            var pwP = jsObj.widget.PortletWindow.prototype.altInitParamsDef( createWindowParams, windowConfigObject );
            if ( windowMakeAbsolute )
                pwP.altInitParams[ jsObj.id.PP_WINDOW_POSITION_STATIC ] = false ;
        }
    
        var wndObj = new jsObj.widget.PortletWindow();
        wndObj.build( createWindowParams, windowContainerNode );    
    
        if ( dbProfile )
            dojo.profile.end( "createPortletWindow" );

        return wndObj;
    },

    getLayoutExtents: function( node, nodeCompStyle, djObj, jsObj )
    {
        if ( ! nodeCompStyle ) nodeCompStyle = djObj.gcs( node );
        var pad = djObj._getPadExtents( node, nodeCompStyle );
        var border = djObj._getBorderExtents( node, nodeCompStyle );
        var padborder = { l: ( pad.l + border.l ), t: ( pad.t + border.t ), w: ( pad.w + border.w ), h: ( pad.h + border.h ) };
        var margin = djObj._getMarginExtents( node, nodeCompStyle, jsObj);
        return { bE: border,
                 pE: pad,
                 pbE: padborder,
                 mE: margin,
                 lessW: ( padborder.w + margin.w ),
                 lessH: ( padborder.h + margin.h ) };
                 //ieHasLayout: ( jetspeed.UAie ? node.currentStyle.hasLayout : "N/A" ) };
    },

    getContentBoxSize: function( node, layoutExtents )
    {
        var w = node.clientWidth, h, useLE;
        if ( !w )
        {
            w = node.offsetWidth, h = node.offsetHeight;
            useLE = layoutExtents.pbE;
        }
        else
        {
            h = node.clientHeight;
            useLE = layoutExtents.pE;
        }
        return { w: ( w - useLE.w ), h: ( h - useLE.h ) };
    },

    getMarginBoxSize: function( node, layoutExtents )
    {
        return { w: ( node.offsetWidth + layoutExtents.mE.w ), h: ( node.offsetHeight + layoutExtents.mE.h ) };
    },

    getMarginBox: function( node, layoutExtents, parentLayoutExtents, jsObj )
    {   // NOTE: assumes that parent node has overflow set to visible
        var	l = node.offsetLeft - layoutExtents.mE.l, t = node.offsetTop - layoutExtents.mE.t;
        // mozilla - left and top will be wrong if parent overflow is not visible
		if ( parentLayoutExtents && jsObj.UAope )
        {   // opera offsetLeft/offsetTop includes the parent's border
            l -= parentLayoutExtents.bE.l;
            t -= parentLayoutExtents.bE.t;
		}
		return { 
			l: l, 
			t: t, 
			w: ( node.offsetWidth + layoutExtents.mE.w ),
			h: ( node.offsetHeight + layoutExtents.mE.h ) };
	},

    setMarginBox: function( node, leftPx, topPx, widthPx, heightPx, layoutExtents, jsObj, djObj )
    {   // NOT: will not work if dojo._usesBorderBox(node) == true
		var pb=layoutExtents.pbE, mb=layoutExtents.mE;
		if(widthPx != null && widthPx>=0){ widthPx = Math.max(widthPx - pb.w - mb.w, 0); }
		if(heightPx != null && heightPx>=0){ heightPx = Math.max(heightPx - pb.h - mb.h, 0); }
		djObj._setBox( node, leftPx, topPx, widthPx, heightPx );
	},
    
    evtConnect: function( adviceType, srcObj, srcFuncName, adviceObj, adviceFuncName, djEvtObj, rate )
    {   // if arg check is needed, use dojo.event.connect()
        if ( ! rate ) rate = 0;
        var cParams = { adviceType: adviceType, srcObj: srcObj, srcFunc: srcFuncName, adviceObj: adviceObj, adviceFunc: adviceFuncName, rate: rate };
        if ( djEvtObj == null ) djEvtObj = dojo.event;
        djEvtObj.connect( cParams );
        return cParams;
    },
    
    evtDisconnect: function( adviceType, srcObj, srcFuncName, adviceObj, adviceFuncName, djEvtObj )
    {   // if arg check is needed, use dojo.event.disconnect()
        if ( djEvtObj == null ) djEvtObj = dojo.event;
        djEvtObj.disconnect( { adviceType: adviceType, srcObj: srcObj, srcFunc: srcFuncName, adviceObj: adviceObj, adviceFunc: adviceFuncName } );
    },
    
    evtDisconnectWObj: function( kwArgs, djEvtObj )
    {
        if ( djEvtObj == null ) djEvtObj = dojo.event;
        //jetspeed.println( "evtD-single: " + jetspeed.printobj( kwArgs ) );
        djEvtObj.disconnect( kwArgs );
    },
    
    evtDisconnectWObjAry: function( kwArgsArray, djEvtObj )
    {
        if ( kwArgsArray && kwArgsArray.length > 0 )
        {
            //jetspeed.dumpary( kwArgsArray, "evtD-multi" );
            if ( djEvtObj == null ) djEvtObj = dojo.event;
            for ( var i = 0 ; i < kwArgsArray.length ; i++ )
            {
                djEvtObj.disconnect( kwArgsArray[i] );
            }
        }
    },

    _popupMenuWidgets: [],
    isWindowActionMenuOpen: function()
    {
        var oneIsOpen = false;
        var popupMenus = this._popupMenuWidgets;
        for ( var i = 0 ; i < popupMenus.length ; i++ )
        {
            var popupMenu = popupMenus[i];
            if ( popupMenu && popupMenu.isShowingNow )
            {
                oneIsOpen = true;
                break;
            }
        }
        return oneIsOpen;
    },
    addPopupMenuWidget: function( popupMenuWidget )
    {
        if ( popupMenuWidget )
            this._popupMenuWidgets.push( popupMenuWidget );
    },
    removePopupMenuWidget: function( popupMenuWidget )
    {
        if ( ! popupMenuWidget ) return;
        var popupMenus = this._popupMenuWidgets;
        for ( var i = 0 ; i < popupMenus.length ; i++ )
        {
            if ( popupMenus[i] === popupMenuWidget )
                popupMenus[i] = null;
        }
    },

    updateChildColInfo: function( dragNode, disqualifiedColIndexes, disqualifyDepth, cL_NA_ED, debugDepth, debugIndent )
    {
        var jsObj = jetspeed;
        var djObj = dojo;
        var columnContainerNode = djObj.byId( jsObj.id.COLUMNS );
        if ( ! columnContainerNode )
            return;
        var dragNodeIsLayout = false;
        if ( dragNode != null )
        {
            var nodeColIndexStr = dragNode.getAttribute( "columnindex" );
            var nodeLayoutId = dragNode.getAttribute( "layoutid" );
            var pageColIndex = ( nodeColIndexStr == null ? -1 : new Number( nodeColIndexStr ) );
            
            if ( pageColIndex >= 0 && nodeLayoutId != null && nodeLayoutId.length > 0 )
                dragNodeIsLayout = true;
        }
        var columnObjArray = jsObj.page.columns || [];
        var columnInfoArray = new Array( columnObjArray.length );
        var pageLayoutInfo = jsObj.page.layoutInfo;
        var fnc = jsObj.ui._updateChildColInfo;
        fnc( fnc, columnContainerNode, 1, columnInfoArray, columnObjArray, disqualifiedColIndexes, disqualifyDepth, cL_NA_ED, pageLayoutInfo, pageLayoutInfo.columns, pageLayoutInfo.desktop, dragNode, dragNodeIsLayout, debugDepth, debugIndent, djObj, jsObj );
        return columnInfoArray;
    },
    _updateChildColInfo: function( fnc, parentNode, depth, columnInfoArray, columnObjArray, disqualifiedColIndexes, disqualifyDepth, cL_NA_ED, pageLayoutInfo, parentNodeLayoutInfo, parentNodeParentLayoutInfo, dragNode, dragNodeIsLayout, debugDepth, debugIndent, djObj, jsObj )
    {
        var childNodes = parentNode.childNodes;
        var childNodesLen = ( childNodes ? childNodes.length : 0 );
        if ( childNodesLen == 0 ) return;
        var absPosParent = djObj.html.getAbsolutePosition( parentNode, true );
        var mbParent = jsObj.ui.getMarginBox( parentNode, parentNodeLayoutInfo, parentNodeParentLayoutInfo, jsObj );
        var colLayoutInfo = pageLayoutInfo.column;
        var child, col, colLad, pageColIndexStr, pageColIndex, layoutId, isLayout, mbCol, absLeftCol, absTopCol, heightCol, colInfo, highHeightCol, colGetChildren;
        var colTopOffset = null, debugNextDepth = ( debugDepth != null ? (debugDepth + 1) : null ), tDebugNextDepth, debugMsg;
        var currentMaxChildDepth = null;
        for ( var i = 0 ; i < childNodesLen ; i++ )
        {
            child = childNodes[i];
            pageColIndexStr = child.getAttribute( "columnindex" );
            pageColIndex = ( pageColIndexStr == null ? -1 : new Number( pageColIndexStr ) );
            if ( pageColIndex >= 0 )
            {
                col = columnObjArray[pageColIndex];
                colGetChildren = true;
                colLad = ( col ? col.layoutActionsDisabled : false );
                layoutId = child.getAttribute( "layoutid" );
                isLayout = ( layoutId != null && layoutId.length > 0 );
                tDebugNextDepth = debugNextDepth;
                debugMsg = null;
                colLad = ((! cL_NA_ED) && colLad);
                var nextDepth = depth;
                var childIsDragNode = ( child === dragNode );
                if ( isLayout )
                {
                    if ( currentMaxChildDepth == null )
                        currentMaxChildDepth = depth;
                    if ( col )
                        col._updateLayoutDepth( depth );
                    nextDepth++;
                }
                else if ( ! childIsDragNode )
                {
                    if ( col && ( ! colLad || cL_NA_ED ) && ( disqualifiedColIndexes == null || disqualifiedColIndexes[ pageColIndex ] == null ) && ( disqualifyDepth == null || depth <= disqualifyDepth ) )
                    {
                        mbCol = jsObj.ui.getMarginBox( child, colLayoutInfo, parentNodeLayoutInfo, jsObj );
                        if ( colTopOffset == null )
                        {
                            colTopOffset = mbCol.t - mbParent.t;
                            highHeightCol = mbParent.h - colTopOffset;
                        }
                        absLeftCol = absPosParent.left + ( mbCol.l - mbParent.l );
                        absTopCol = absPosParent.top + colTopOffset;
                        heightCol = mbCol.h;
                        if ( heightCol < highHeightCol )
                            heightCol = highHeightCol; // heightCol + Math.floor( ( highHeightCol - heightCol ) / 2 );
                        if ( heightCol < 40 )
                            heightCol = 40;
                            
                        var colChildNodes = child.childNodes;
                        colInfo = { left: absLeftCol, top: absTopCol, right: (absLeftCol + mbCol.w), bottom: (absTopCol + heightCol), childCount: ( colChildNodes ? colChildNodes.length : 0 ), pageColIndex: pageColIndex };
                        colInfo.height = colInfo.bottom - colInfo.top;
                        colInfo.width = colInfo.right - colInfo.left;
                        colInfo.yhalf = colInfo.top + ( colInfo.height / 2 );
                        columnInfoArray[ pageColIndex ] = colInfo;
                        colGetChildren = ( colInfo.childCount > 0 );
                        if ( colGetChildren )
                            child.style.height = "";
                        else
                            child.style.height = "1px";
                        if ( debugDepth != null )
                            debugMsg = ( jsObj.debugDims( colInfo, true ) + " yhalf=" + colInfo.yhalf + ( mbCol.h != heightCol ? ( " hreal=" + mbCol.h ) : "" ) + " childC=" + colInfo.childCount + "}" );
                    }
                }
                if ( debugDepth != null )
                {
                    if ( isLayout )
                        tDebugNextDepth = debugNextDepth + 1;
                    if ( debugMsg == null )
                        debugMsg = "---";
                    djObj.hostenv.println( djObj.string.repeat( debugIndent, debugDepth ) + "["+( ( pageColIndex < 10 ? " " : "" ) + pageColIndexStr )+"] " + debugMsg );
                }
                if ( colGetChildren )
                {
                    var childHighDepth = fnc( fnc, child, nextDepth, columnInfoArray, columnObjArray, disqualifiedColIndexes, disqualifyDepth, cL_NA_ED, pageLayoutInfo, ( isLayout ? pageLayoutInfo.columnLayoutHeader : colLayoutInfo ), parentNodeLayoutInfo, dragNode, dragNodeIsLayout, tDebugNextDepth, debugIndent, djObj, jsObj );
                    if ( childHighDepth != null && ( currentMaxChildDepth == null || childHighDepth > currentMaxChildDepth ) )
                        currentMaxChildDepth = childHighDepth;
                }
            }
        }
        pageColIndexStr = parentNode.getAttribute( "columnindex" );
        layoutId = parentNode.getAttribute( "layoutid" );
        pageColIndex = ( pageColIndexStr == null ? -1 : new Number( pageColIndexStr ) );
        if ( pageColIndex >= 0 && layoutId != null && layoutId.length > 0 )
        {
            col = columnObjArray[pageColIndex];
            col._updateLayoutChildDepth( currentMaxChildDepth );
        }

        return currentMaxChildDepth;
    },  // _updateChildColInfo

    getScrollbar: function( jsObj )
    {    //	returns the width of a scrollbar.
        var scrollWidth = jsObj.ui.scrollWidth;
        if ( scrollWidth == null )
        {
        	var scroll = document.createElement( "div" );
            var scrollCss = "width: 100px; height: 100px; top: -300px; left: 0px; overflow: scroll; position: absolute";
            scroll.style.cssText = scrollCss;
            	
        	var test = document.createElement( "div" );
            scroll.style.cssText = "width: 400px; height: 400px";
        
            scroll.appendChild( test );
        
            var docBod = jsObj.docBody;
    
        	docBod.appendChild( scroll );
        
        	scrollWidth = scroll.offsetWidth - scroll.clientWidth;
        
        	docBod.removeChild( scroll );
        	scroll.removeChild( test );
        	scroll = test = null;
            
            jsObj.ui.scrollWidth = scrollWidth;
        }
    	return scrollWidth;
    }
};

//if ( jetspeed.UAie6 )
{
    // object to bundle resize processing:
    jetspeed.ui.windowResizeMgr = 
    {
        checkTime: 500,
        timerId: 0,
        resizing: false,

        init: function( win, docBody )
        {
            this.oldXY = this.getWinDims( win, win.document, docBody );
        },

        getWinDims: function( win, doc, docBody )
        {   // get window dimensions and scroll amount
            var b, x, y, sx, sy, v;
            x = y = sx = sy = 0;
            if ( win.innerWidth && win.innerHeight )
            {
                x = win.innerWidth;
                v = docBody.offsetWidth;
                if ( v && ( 1 < v ) && ! ( x < v ) ) // scrollbar width problem
                    x = v-1;
                y = win.innerHeight;
                sx = win.pageXOffset || 0;
                sy = win.pageYOffset || 0;
            }
            else
            {
                b = doc.documentElement.clientWidth ? doc.documentElement : docBody; // IE 6 strict dtd
                if ( b )
                {
                    x = b.clientWidth || 0;
                    y = b.clientHeight || 0;
                    sx = b.scrollLeft || 0;
                    sy = b.scrollTop || 0;
                }
            }
            return { x: x, y: y, sx: sx, sy: sy };
        },

        onResize: function()
        {
            if ( this.timerId )
                window.clearTimeout( this.timerId );
            this.timerId = dojo.lang.setTimeout( this, this.onResizeDelayedCompare, this.checkTime );
        },

        onResizeDelayedCompare: function()
        {
            var jsObj = jetspeed;
            var newXY = this.getWinDims( window, window.document, jsObj.docBody );
            this.timerId = 0;
            if ( (newXY.x != this.oldXY.x) || (newXY.y != this.oldXY.y) )
            {
                this.oldXY = newXY;
                if ( jsObj.page )
                {
                    if ( ! this.resizing )  // duplicate event
                    {
                        try
                        {
                            this.resizing = true;
                            jsObj.page.onBrowserWindowResize();
                        }
                        catch(e)
                        {
                        }
                        finally
                        {
                            this.resizing = false;
                        }
                    }
                }
            }
        }
    };
}

/*  jetspeed.ui.swfobject is based on a condensed and modified copy of the SWFObject library */
/*     this library appears here due to problems with loading the library for each portlet needing it */

/*	SWFObject v2.0 beta5 <http://code.google.com/p/swfobject/>
	Copyright (c) 2007 Geoff Stearns, Michael Williams, and Bobby van der Sluis
	This software is released under the MIT License <http://www.opensource.org/licenses/mit-license.php>
*/
jetspeed.ui.swfobject = function() {
    
    var jsObj = jetspeed;
    var storedAltContent = null;
    var isExpressInstallActive = false;
    
    var ua = function() {
        var playerVersion = [0,0,0];
		var d = null;
		if (typeof navigator.plugins != "undefined" && typeof navigator.plugins["Shockwave Flash"] == "object") {
			d = navigator.plugins["Shockwave Flash"].description;
			if (d) {
				d = d.replace(/^.*\s+(\S+\s+\S+$)/, "$1");
				playerVersion[0] = parseInt(d.replace(/^(.*)\..*$/, "$1"), 10);
				playerVersion[1] = parseInt(d.replace(/^.*\.(.*)\s.*$/, "$1"), 10);
				playerVersion[2] = /r/.test(d) ? parseInt(d.replace(/^.*r(.*)$/, "$1"), 10) : 0;
			}
		}
		else if (typeof window.ActiveXObject != "undefined") {
			var a = null;
			var fp6Crash = false;
			try {
				a = new ActiveXObject("ShockwaveFlash.ShockwaveFlash.7");
			}
			catch(e) {
				try { 
					a = new ActiveXObject("ShockwaveFlash.ShockwaveFlash.6");
					playerVersion = [6,0,21];
					a.AllowScriptAccess = "always";  // Introduced in fp6.0.47
				}
				catch(e) {
					if (playerVersion[0] == 6) {
						fp6Crash = true;
					}
				}
				if (!fp6Crash) {
					try {
						a = new ActiveXObject("ShockwaveFlash.ShockwaveFlash");
					}
					catch(e) {}
				}
			}
			if (!fp6Crash && typeof a == "object") { // When ActiveX is disbled in IE, then "typeof a" returns "object", however it is "null" in reality, so something like "typeof a.GetVariable" will crash the script
				try {
					d = a.GetVariable("$version");  // Will crash fp6.0.21/23/29
					if (d) {
						d = d.split(" ")[1].split(",");
						playerVersion = [parseInt(d[0], 10), parseInt(d[1], 10), parseInt(d[2], 10)];
					}
				}
				catch(e) {}
			}
		}
        var djR = dojo.render;
        var djRH = djR.html;

        return { w3cdom: true, playerVersion: playerVersion, ie: djRH.ie, win: djR.os.win, mac: djR.os.mac };
    }();

    /* Fix hanging audio/video threads
		- Occurs when unloading a web page in IE using fp8+ and innerHTML/outerHTML
		- Dynamic publishing only
	*/
	function fixObjectLeaks() {
		if (ua.ie && ua.win && hasPlayerVersion([8,0,0])) {
			window.attachEvent("onunload", function () {
				var o = document.getElementsByTagName("object");
				if (o) {
					var ol = o.length;
					for (var i = 0; i < ol; i++) {
						o[i].style.display = "none";
						for (var x in o[i]) {
							if (typeof o[i][x] == "function") {
								o[i][x] = function() {};
							}
						}
					}
				}
			});
		}
	}

    /* Show the Adobe Express Install dialog
		- Reference: http://www.adobe.com/cfusion/knowledgebase/index.cfm?id=6a253b75
	*/
	function showExpressInstall(regObj) {
		isExpressInstallActive = true;
		var obj = document.getElementById(regObj.id);
		if (obj) {
		    var ac = document.getElementById(regObj.altContentId);
		 	if (ac) {
	    		storedAltContent = ac;
	   		}
			var w = regObj.width ? regObj.width : (obj.getAttribute("width") ? obj.getAttribute("width") : 0);
			if (parseInt(w, 10) < 310) {
				w = "310";
			}
			var h = regObj.height ? regObj.height : (obj.getAttribute("height") ? obj.getAttribute("height") : 0);
			if (parseInt(h, 10) < 137) {
				h = "137";
			}
			var pt = ua.ie && ua.win ? "ActiveX" : "PlugIn";
			//document.title = document.title.slice(0, 47) + " - Flash Player Installation";
			var dt = document.title;
			var fv = "MMredirectURL=" + window.location + "&MMplayerType=" + pt + "&MMdoctitle=" + dt;
			var el = obj;
			createSWF({ data:regObj.expressInstall, id:"SWFObjectExprInst", width:w, height:h }, { flashvars:fv }, el);
		}
	}

	/* Cross-browser dynamic SWF creation
	*/
	function createSWF(attObj, parObj, el) {
        parObj.wmode = "transparent";   // allows other content (e.g. another portlet window) to be in front of flash content
		if (ua.ie && ua.win) { // IE, the object element and W3C DOM methods do not combine: fall back to outerHTML
			var att = "";
			for (var i in attObj) {
				if (typeof attObj[i] == "string") { // Filter out prototype additions from other potential libraries, like Object.prototype.toJSONString = function() {}
					if (i == "data") {
						parObj.movie = attObj[i];
					}
					else if (i.toLowerCase() == "styleclass") { // 'class' is an ECMA4 reserved keyword
						att += ' class="' + attObj[i] + '"';
					}
					else if (i != "classid") {
						att += ' ' + i + '="' + attObj[i] + '"';
					}
				}
			}
			var par = "";
			for (var j in parObj) {
				if (typeof parObj[j] == "string") { // Filter out prototype additions from other potential libraries
					par += '<param name="' + j + '" value="' + parObj[j] + '" />';
				}
			}
			el.outerHTML = '<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"' + att + '>' + par + '</object>';
			fixObjectLeaks(); // This bug affects dynamic publishing only	
		}
		else { // Well-behaving browsers
			var o = document.createElement("object");
			o.setAttribute("type", "application/x-shockwave-flash");
			for (var m in attObj) {
				if (typeof attObj[m] == "string") {  // Filter out prototype additions from other potential libraries
					if (m.toLowerCase() == "styleclass") { // 'class' is an ECMA4 reserved keyword
						o.setAttribute("class", attObj[m]);
					}
					else if (m != "classid") { // Filter out IE specific attribute
						o.setAttribute(m, attObj[m]);
					}
				}
			}
			for (var n in parObj) {
				if (typeof parObj[n] == "string" && n != "movie") { // Filter out prototype additions from other potential libraries and IE specific param element
					createObjParam(o, n, parObj[n]);
				}
			}
			el.parentNode.replaceChild(o, el);
		}
	}

	function createObjParam(el, pName, pValue) {
		var p = document.createElement("param");
		p.setAttribute("name", pName);	
		p.setAttribute("value", pValue);
		el.appendChild(p);
	}

	function hasPlayerVersion(rv) {
		return (ua.playerVersion[0] > rv[0] || (ua.playerVersion[0] == rv[0] && ua.playerVersion[1] > rv[1]) || (ua.playerVersion[0] == rv[0] && ua.playerVersion[1] == rv[1] && ua.playerVersion[2] >= rv[2])) ? true : false;
	}
	
	/* Cross-browser dynamic CSS creation
		- Based on Bobby van der Sluis' solution: http://www.bobbyvandersluis.com/articles/dynamicCSS.php
	*/	
	function createCSS(sel, decl) {
		if (ua.ie && ua.mac) {
			return;
		}
		var h = document.getElementsByTagName("head")[0]; 
		var s = document.createElement("style");
		s.setAttribute("type", "text/css");
		s.setAttribute("media", "screen");
		if (!(ua.ie && ua.win) && typeof document.createTextNode != "undefined") {
			s.appendChild(document.createTextNode(sel + " {" + decl + "}"));
		}
		h.appendChild(s);
		if (ua.ie && ua.win && typeof document.styleSheets != "undefined" && document.styleSheets.length > 0) {
			var ls = document.styleSheets[document.styleSheets.length - 1];
			if (typeof ls.addRule == "object") {
				ls.addRule(sel, decl);
			}
		}
	}

	return {
		/* Public API
			- Reference: http://code.google.com/p/swfobject/wiki/SWFObject_2_0_documentation
		*/

		embedSWF: function(swfUrlStr, replaceElemIdStr, widthStr, heightStr, swfVersionStr, xiSwfUrlStr, flashvarsObj, parObj, attObj, sizeInfo ) {
			if (!ua.w3cdom || !swfUrlStr || !replaceElemIdStr || !widthStr || !heightStr || !swfVersionStr) {
				return;
			}
			if (hasPlayerVersion(swfVersionStr.split("."))) {
                var objNodeId = ( attObj ? attObj.id : null );
				createCSS("#" + replaceElemIdStr, "visibility:hidden");
				var att = (typeof attObj == "object") ? attObj : {};
				att.data = swfUrlStr;
				att.width = widthStr;
				att.height = heightStr;
				var par = (typeof parObj == "object") ? parObj : {};
				if (typeof flashvarsObj == "object") {
					for (var i in flashvarsObj) {
						if (typeof flashvarsObj[i] == "string") { // Filter out prototype additions from other potential libraries
							if (typeof par.flashvars != "undefined") {
								par.flashvars += "&" + i + "=" + flashvarsObj[i];
							}
							else {
								par.flashvars = i + "=" + flashvarsObj[i];
							}
						}
					}
				}
                createSWF(att, par, document.getElementById(replaceElemIdStr));
                createCSS("#" + replaceElemIdStr, "visibility:visible");
                if ( objNodeId )
                {
                    var swfInfo = jsObj.page.swfInfo;
                    if ( swfInfo == null )
                    {
                        swfInfo = jsObj.page.swfInfo = {};
                    }
                    swfInfo[ objNodeId ] = sizeInfo;
                }
			}
			else if (xiSwfUrlStr && !isExpressInstallActive && hasPlayerVersion([6,0,65]) && (ua.win || ua.mac)) {
				createCSS("#" + replaceElemIdStr, "visibility:hidden");
                var regObj = {};
			 	regObj.id = regObj.altContentId = replaceElemIdStr;
			   	regObj.width = widthStr;
			   	regObj.height = heightStr;
			   	regObj.expressInstall = xiSwfUrlStr;
			   	showExpressInstall(regObj);
			   	createCSS("#" + replaceElemIdStr, "visibility:visible");
			}
		}

		// For internal usage only
        /*  BOZO: callback from expressInstall.swf - broken cause it refers to SWFFix.expressInstallCallback
		expressInstallCallback: function() {
			if (isExpressInstallActive && storedAltContent) {
				var obj = document.getElementById("SWFObjectExprInst");
				if (obj) {
					obj.parentNode.replaceChild(storedAltContent, obj);
					storedAltContent = null;
					isExpressInstallActive = false;
				}
			} 
		}
        */
    };
}();

