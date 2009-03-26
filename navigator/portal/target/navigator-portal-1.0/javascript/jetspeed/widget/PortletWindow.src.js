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

dojo.provide("jetspeed.widget.PortletWindow");
dojo.require("jetspeed.desktop.core");

jetspeed.widget.PortletWindow = function()
{
    this.windowInitialized = false;
    this.actionButtons = {};
    this.actionMenuWidget = null;
    this.tooltips = [];
    
    // content load vars
	this._onLoadStack = [];
	this._onUnloadStack = [];
	this._callOnUnload = false;
};

dojo.extend( jetspeed.widget.PortletWindow, {
    title: "",
    nextIndex: 1,

    resizable: true,
    moveable: true,
    moveAllowTilingChg: true,

    posStatic: false,
    heightToFit: false,

    decName: null,       // decoration name
    decConfig: null,     // decoration config
    titlebarEnabled: true,
    resizebarEnabled: true,
    editPageEnabled: false,

    iframeCoverContainerClass: "portletWindowIFrameClient",

    colWidth_pbE: 0,

    portlet: null,
    altInitParams: null,
    
    inContentChgd: false,

    exclPContent: false,

    minimizeTempRestore: null,

    // see setPortletContent for info on these ContentPane settings:
    executeScripts: false,
    scriptSeparation: false,
    adjustPaths: false,
    parseContent: true,

    childWidgets: null,

    dbProfile: (djConfig.isDebug && jetspeed.debug.profile),
    dbOn: djConfig.isDebug,
    dbMenuDims: "Dump Dimensions",

    /*  static  */
    altInitParamsDef: function( defineIn, params )
    {
        if ( ! defineIn )
        {
            defineIn = {
                            getProperty: function( propertyName )
                            {
                                if ( ! propertyName ) return null;
                                return this.altInitParams[ propertyName ];
                            },
                            retrieveContent: function( contentListener, bindArgs )
                            {
                                var contentRetriever = this.altInitParams[ jetspeed.id.PP_CONTENT_RETRIEVER ];
                                if ( contentRetriever )
                                {
                                    contentRetriever.getContent( bindArgs, contentListener, this, jetspeed.debugPortletDumpRawContent );
                                }
                                else
                                {
                                    jetspeed.url.retrieveContent( bindArgs, contentListener, this, jetspeed.debugPortletDumpRawContent );
                                }
                            }
                       };
        }
        if ( ! params )
            params = {};
        if ( params.altInitParams )
            defineIn.altInitParams = params.altInitParams;
        else
            defineIn.altInitParams = params;
        return defineIn;
    },

    build: function( createWinParams, winContainerNode )
    {
        var jsObj = jetspeed;
        var jsId = jsObj.id;
        var jsPrefs = jsObj.prefs;
        var jsPage = jsObj.page;
        var jsCss = jsObj.css;
        var jsUI = jsObj.ui;
        var doc = document;
        var docBody = jsObj.docBody;
        var djObj = dojo;

        var winIndex = jsObj.widget.PortletWindow.prototype.nextIndex;
        this.windowIndex = winIndex;
        var ie6 = jsObj.UAie6;
        this.ie6 = ie6;

        var printMode = false;
        if ( createWinParams )
        {
            if ( createWinParams.portlet )
                this.portlet = createWinParams.portlet;
            if ( createWinParams.altInitParams )
                this.altInitParams = createWinParams.altInitParams;
            if ( createWinParams.printMode )
                printMode = true;
        }
        var tPortlet = this.portlet;
        var iP = ( tPortlet ? tPortlet.getProperties() : ( this.altInitParams ? this.altInitParams : {} ) ) ;

        var initWidgetId = iP[ jsId.PP_WIDGET_ID ];
        if ( ! initWidgetId )
        {
            if ( tPortlet )
                djObj.raise( "PortletWindow is null for portlet: " + tPortlet.entityId );
            else
                initWidgetId = jsId.PW_ID_PREFIX + winIndex;
        }
        this.widgetId = initWidgetId;
        jsObj.widget.PortletWindow.prototype.nextIndex++;

        // ... initWindowDecoration
        var decNm = iP[ jsId.PP_WINDOW_DECORATION ];
        this.decName = decNm ;
        var wDC = jsObj.loadPortletDecorationStyles( decNm, jsPrefs );
        if ( wDC == null ) { djObj.raise( "No portlet decoration is available: " + this.widgetId ); }    // this should not occur
        this.decConfig = wDC;

        var dNodeClass = wDC.dNodeClass;
        var cNodeClass = wDC.cNodeClass;

        // ... create window nodes
        var dNode = doc.createElement( "div" );
        dNode.id = initWidgetId;
        dNode.className = dNodeClass;
        dNode.style.display = "none";

        var cNode = doc.createElement( "div" );
        cNode.className = cNodeClass;
        
        var tbNode = null, rbNode = null, tbIconNode = null, tbNodeCss = null;
        if ( ! printMode )
        {
            tbNode = doc.createElement( "div" );
            tbNode.className = "portletWindowTitleBar";
        
            tbIconNode = doc.createElement( "img" );
            tbIconNode.className = "portletWindowTitleBarIcon";
        
            var tbTextNode = doc.createElement( "div" );
            tbTextNode.className = "portletWindowTitleText";
    
            tbNode.appendChild( tbIconNode );
            tbNode.appendChild( tbTextNode );
    
            rbNode = doc.createElement( "div" );
            rbNode.className = "portletWindowResizebar";
    
            this.tbNode = tbNode;
            tbNodeCss = jsCss.cssBase.concat();
            this.tbNodeCss = tbNodeCss;
            this.tbIconNode = tbIconNode;
            this.tbTextNode = tbTextNode;
            this.rbNode = rbNode;
            this.rbNodeCss = jsCss.cssBase.concat();
        }
    
        if ( tbNode != null )
            dNode.appendChild( tbNode );
    
        dNode.appendChild( cNode );
    
        if ( rbNode != null )
            dNode.appendChild( rbNode );
        
        this.domNode = dNode;
        var dNodeCss = jsCss.cssPosition.concat();
        if ( jsPage.maximizedOnInit != null )
        {
            dNodeCss[ jsCss.cssNoSelNm ] = " visibility: ";
            dNodeCss[ jsCss.cssNoSel ] = "hidden";
            dNodeCss[ jsCss.cssNoSelEnd ] = ";";
        }
        this.dNodeCss = dNodeCss;
        this.containerNode = cNode;
        var cNodeCss = jsCss.cssOverflow.concat();
        this.cNodeCss = cNodeCss;

        // ... initWindowTitle
        this.setPortletTitle( iP[ jsId.PP_WINDOW_TITLE ] );

        // ... initWindowDimensions
        var posStatic = iP[ jsId.PP_WINDOW_POSITION_STATIC ];
        this.posStatic = this.preMaxPosStatic = posStatic;
        var heightToFit = iP[ jsId.PP_WINDOW_HEIGHT_TO_FIT ];
        this.heightToFit = this.preMaxHeightToFit = heightToFit;

        var wWidth = null, wHeight = null, wLeft = null, wTop = null;
        if ( tPortlet )
        {
            var pWDims = tPortlet.getInitialWinDims();
        	wWidth = pWDims.width;
            wHeight = pWDims.height;
            wLeft = pWDims.left;
            wTop = pWDims.top;
            // NOTE: pWDims.zIndex;  - should be dealt with in the creation order
        }
        else
        {
            wWidth = iP[ jsId.PP_WIDTH ];
            wHeight = iP[ jsId.PP_HEIGHT ];
            wLeft = iP[ jsId.PP_LEFT ];
            wTop = iP[ jsId.PP_TOP ];
        }
        
        var untiledDims = {};            // untiled
        var tiledDims = { w: null }; // tiled
        
        // to allow for an initial untiled placement based on tiled position,
        //   only record dimsUntiled when value is specified (not defaulted) or if window is already untiled
        if ( wWidth != null && wWidth > 0 )
            untiledDims.w = wWidth = Math.floor( wWidth );
        else
            untiledDims.w = wWidth = jsPrefs.windowWidth;
    
        if ( wHeight != null && wHeight > 0 )
            untiledDims.h = tiledDims.h = wHeight = Math.floor(wHeight);
        else
            untiledDims.h = tiledDims.h = wHeight = jsPrefs.windowHeight;
            
        if ( wLeft != null && wLeft >= 0 )
            untiledDims.l = Math.floor( wLeft );
        else if ( ! posStatic )
            untiledDims.l = (((winIndex -2) * 30 ) + 200);
    
        if ( wTop != null && wTop >= 0 )
            untiledDims.t = Math.floor(wTop);
        else if ( ! posStatic )
            untiledDims.t = (((winIndex -2) * 30 ) + 170);
        
        this.dimsUntiled = untiledDims;
        this.dimsTiled = tiledDims;

        this.exclPContent = iP[ jsId.PP_EXCLUDE_PCONTENT ];

        jsPage.putPWin( this );

		docBody.appendChild( dNode );   // necessary for safari, khtml (for computing width/height)

        // ... initWindowIcon
        if ( tbIconNode )
        {
            var tbIconSrc = null;
            if ( wDC.windowIconEnabled && wDC.windowIconPath != null )
            {
                var wI = iP[ jsId.PP_WINDOW_ICON ];
                if ( ! wI )
                    wI = "document.gif";
                tbIconSrc = new djObj.uri.Uri( jsObj.url.basePortalDesktopUrl() + wDC.windowIconPath + "/" + wI ) ;
                tbIconSrc = tbIconSrc.toString();
                if ( tbIconSrc.length == 0 )
                    tbIconSrc = null;
                this.iconSrc = tbIconSrc;
            }
            // <img src=""> can hang IE!  better get rid of it
		    if ( tbIconSrc )
                tbIconNode.src = tbIconSrc;
            else
            {
			    djObj.dom.removeNode( tbIconNode );
                this.tbIconNode = tbIconNode = null;
            }
		}

		if ( tbNode )
        {	
	        if ( jsObj.UAmoz || jsObj.UAsaf )
            {
                if ( jsObj.UAmoz )
                    tbNodeCss[ jsCss.cssNoSelNm ] = " -moz-user-select: ";
                else
                    tbNodeCss[ jsCss.cssNoSelNm ] = " -khtml-user-select: ";
                tbNodeCss[ jsCss.cssNoSel ] = "none";
                tbNodeCss[ jsCss.cssNoSelEnd ] = ";";
            }
            else if ( jsObj.UAie )
            {
                tbNode.unselectable = "on";
            }

            this._setupTitlebar( wDC, null, tPortlet, docBody, doc, jsObj, jsId, jsPrefs, jsUI, jsPage, djObj );
        }

        // ... init drag handle
        var isResizable = this.resizable;
        var rhWidget = null;
        if ( isResizable && rbNode )
        {
            var rhWidgetId = initWidgetId + "_resize";
            var rhWidget = jsObj.widget.CreatePortletWindowResizeHandler( this, jsObj );
            this.resizeHandle = rhWidget;
            if ( rhWidget )
            {
                rbNode.appendChild( rhWidget.domNode );
            }
		}
        else
        {
            this.resizable = false;
        }

		docBody.removeChild( dNode );   // counteract body.appendChild above

        if ( ! wDC.windowTitlebar || ! wDC.windowResizebar )
        {
            var disIdx = jsObj.css.cssDis;
            if ( ! wDC.windowTitlebar )
            {
                this.titlebarEnabled = false;
                if ( this.tbNodeCss )
                    this.tbNodeCss[ disIdx ] = "none";
            }

            if ( ! wDC.windowResizebar )
            {
                this.resizebarEnabled = false;
                if ( this.rbNodeCss )
                    this.rbNodeCss[ disIdx ] = "none";
            }
        }

        var nodeAdded = false;
        var winChildNodes = winContainerNode.childNodes;
        if ( posStatic && winChildNodes )
        {
            var rowProp = iP[ jsId.PP_ROW ];
            if ( rowProp != null  )
            {
                var rowInt = new Number(rowProp);
                if ( rowInt >= 0 )
                {
                    var winChildNodesLast = winChildNodes.length -1;
                    if ( winChildNodesLast >= rowInt )
                    {
                        var childAtRowInt = winChildNodes[rowInt];
                        if ( childAtRowInt )
                        {
                            winContainerNode.insertBefore( dNode, childAtRowInt );
                            nodeAdded = true;
                        } 
                    }
                }
            }
        }
        if ( ! nodeAdded )
            winContainerNode.appendChild( dNode );

        if ( ! wDC.layout )
        {
            var dimCss = "display: block; visibility: hidden; width: " + wWidth + "px" + ( ( wHeight != null && wHeight > 0 ) ? ( "; height: " + wHeight + "px" ) : "");
            dNode.style.cssText = dimCss;
            this._createLayoutInfo( wDC, false, dNode, cNode, tbNode, rbNode, djObj, jsObj, jsUI );
        }
        
        if ( tbNode )
        {
            this.drag = new djObj.dnd.Moveable( this, {handle: tbNode});
            this._setTitleBarDragging( true, jsCss );
        }

        if ( ie6 && posStatic )
            tiledDims.w = Math.max( 0, winContainerNode.offsetWidth - this.colWidth_pbE );

        this._setAsTopZIndex( jsPage, jsCss, dNodeCss, posStatic );
        this._alterCss( true, true );

        if ( ! posStatic )
            this._addUntiledEvents();

        if ( ie6 )   // prevent IE bleed-through problem
            this.bgIframe = new jsObj.widget.BackgroundIframe( dNode, null, djObj );

        this.windowInitialized = true;

        if ( jsObj.debug.createWindow )
            djObj.debug( "createdWindow [" + ( tPortlet ? tPortlet.entityId : initWidgetId ) + ( tPortlet ? (" / " + initWidgetId) : "" ) + "]" + " width=" + dNode.style.width + " height=" + dNode.style.height + " left=" + dNode.style.left + " top=" + dNode.style.top ) ;


        this.windowState = jsId.ACT_RESTORE;  // "normal"
        var iWS = null;
        if ( tPortlet )
            iWS = tPortlet.getCurrentActionState();
        else
            iWS = iP[ jsId.PP_WINDOW_STATE ];

        if ( iWS == jsId.ACT_MINIMIZE )
        {
            this.minimizeOnNextRender = true;
        }
        // jsId.ACT_MAXIMIZE is handled in jetspeed.page.loadPostRender

        if ( jsObj.widget.pwGhost == null && jsPage != null )
        {   // ... drag ghost
            var pwGhost = doc.createElement("div");
            pwGhost.id = "pwGhost";
            pwGhost.className = dNodeClass;
            pwGhost.style.position = "static";
            pwGhost.style.width = "";
            pwGhost.style.left = "auto";
            pwGhost.style.top = "auto";
            jsObj.widget.pwGhost = pwGhost;
        }

        if ( ie6 && jsObj.widget.ie6ZappedContentHelper == null )
        {
            var ie6Helper = doc.createElement("span");
            ie6Helper.id = "ie6ZappedContentHelper";
            jsObj.widget.ie6ZappedContentHelper = ie6Helper;
        }
    },  // build()


    // build functions

    /*  static  */
    _buildActionStructures: function( wDC, tPortlet, docBody, jsObj, jsId, jsPrefs, djObj )
    {   // should be called once for each used portlet decorator - twice if the decorator is used by a non-portlet window
        var btnActionNames = new Array();
        var aNm, incl, noMenuImg = false;
        var tMenuActionNames = new Array();
        var tBtnActionNameMap = new Object();
        var wDCBtnOrder = wDC.windowActionButtonOrder;
        var wDCMenuOrder = wDC.windowActionMenuOrder;
        var tInitialMenuOrderMap = new Object();
        var wDCNoImage = wDC.windowActionNoImage;
        var btnMax = wDC.windowActionButtonMax
        btnMax = ( btnMax == null ? -1 : btnMax );

        if ( wDCMenuOrder )
        {
            for ( var aI = 0 ; aI < wDCMenuOrder.length ; aI++ )
            {
                aNm = wDCMenuOrder[ aI ];
                if ( aNm )
                    tInitialMenuOrderMap[ aNm ] = true;
            }
        }

        if ( wDCBtnOrder != null )
        {   // all possible button actions must be added here (no support for adding action buttons after init)
            // this includes buttons for the current mode and state (which will be initially hidden)
            for ( var aI = (wDCBtnOrder.length-1) ; aI >= 0 ; aI-- )
            {
                aNm = wDCBtnOrder[ aI ];
                incl = false;
                if ( tPortlet )
                    incl = true;
                else
                {
                    if ( aNm == jsId.ACT_MINIMIZE || aNm == jsId.ACT_MAXIMIZE || aNm == jsId.ACT_RESTORE || aNm == jsId.ACT_MENU || jsPrefs.windowActionDesktop[ aNm ] != null )
                        incl = true;
                }
                if ( incl && wDCNoImage && wDCNoImage[ aNm ] )
                {
                    if ( ! tInitialMenuOrderMap[ aNm ] )
                        tMenuActionNames.push( aNm );
                    incl = false;
                }
                if ( incl )
                {
                    btnActionNames.push( aNm );
                    tBtnActionNameMap[ aNm ] = true;
                }
            }
            if ( ! tBtnActionNameMap[ jsId.ACT_MENU ] )
                noMenuImg = true;
   
            var defBtnNames = btnActionNames.length;
            if ( btnMax != -1 && defBtnNames > btnMax )
            {
                var removedBtns = 0;
                var mustRemoveBtns = defBtnNames - btnMax;
                for ( var j = 0 ; j < 2 && removedBtns < mustRemoveBtns ; j++ )
                {
                    for ( var i = (btnActionNames.length-1) ; i >= 0 && removedBtns < mustRemoveBtns ; i-- )
                    {
                        aNm = btnActionNames[i];
                        if ( aNm == null || aNm == jsId.ACT_MENU )
                            continue;
                        if ( j == 0 )
                        {
                            var aNmRE = new RegExp( "\b" + aNm + "\b" );
                            if ( aNmRE.test(jsPrefs.windowActionNotPortlet) || aNm == jsId.ACT_VIEW )
                                continue;
                        }
                        tMenuActionNames.push( aNm );
                        btnActionNames[i] = null;
                        delete tBtnActionNameMap[ aNm ];
                        removedBtns++;
                    }
                }
            }
        }   // if ( wDCBtnOrder != null )

        var menuActionNames = new Array();
        var tMenuAddedMap = new Object();

        var aNmChgPortletTheme = jsId.ACT_CHANGEPORTLETTHEME;
        var portletDecorationsAllowed = jsPrefs.portletDecorationsAllowed;
        if ( jsPrefs.pageEditorLabels && portletDecorationsAllowed && portletDecorationsAllowed.length > 1 )
        {
            aNm = aNmChgPortletTheme;
            var chgPortletThemeLabel = jsPrefs.pageEditorLabels[ aNm ];
            if ( chgPortletThemeLabel )
            {
                menuActionNames.push( aNm );
                tMenuAddedMap[ aNm ]
                this.actionLabels[ aNm ] = chgPortletThemeLabel;
            }
        }

        for ( var i = 0 ; i < tMenuActionNames.length ; i++ )
        {
            aNm = tMenuActionNames[i];
            if ( aNm != null && ! tMenuAddedMap[ aNm ] && ! tBtnActionNameMap[ aNm ] )
            {
                menuActionNames.push( aNm );
                tMenuAddedMap[ aNm ] = true;
            }
        }
        if ( wDCMenuOrder )
        {
            for ( var aI = 0 ; aI < wDCMenuOrder.length ; aI++ )
            {
                aNm = wDCMenuOrder[ aI ];
                if ( aNm != null && ! tMenuAddedMap[ aNm ] && ! tBtnActionNameMap[ aNm ] && ( tPortlet || jsPrefs.windowActionDesktop[ aNm ] ) )
                {
                    menuActionNames.push( aNm );
                    tMenuAddedMap[ aNm ] = true;
                }
            }
        }

        // 
        // jetspeed.prefs.pageEditorLabels.changeportlettheme
        // desktop.pageeditor.changeportlettheme            
        if ( this.dbOn )
        {
            menuActionNames.push( { aNm: this.dbMenuDims, dev: true } );
        }

        var actionMenuWidget = null;
        if ( menuActionNames.length > 0 )
        {
            var menuItemsByName = {};
            var aNm, menulabel, menuitem, submenuWidget, submenuWidgetId, isDev;
            var menuWidgetIdBase = wDC.name + "_menu" + ( ! tPortlet ? "Np" : "" );
            var actionMenuWidgetId = menuWidgetIdBase;
            actionMenuWidget = djObj.widget.createWidget( "PopupMenu2", { id: actionMenuWidgetId, contextMenuForWindow: false }, null );
            actionMenuWidget.onItemClick = function( mi )
            {
                var _aN = mi.jsActNm;
                var _pWin = this.pWin;
                if ( ! mi.jsActDev )
                    _pWin.actionProcess( _aN );
                else
                    _pWin.actionProcessDev( _aN );
            };

            for ( var i = 0 ; i < menuActionNames.length ; i++ )
            {
                aNm = menuActionNames[i];
                submenuWidgetId = null;
                isDev = false;
                if ( ! aNm.dev )
                {
                    menulabel = this.actionLabels[ aNm ];
                    if ( aNm == aNmChgPortletTheme )
                    {
                        submenuWidgetId = menuWidgetIdBase + "_sub_" + aNm;
                        submenuWidget = djObj.widget.createWidget( "PopupMenu2", { id: submenuWidgetId, contextMenuForWindow: false }, null );
                        submenuWidget.onItemClick = function( mi )
                        {
                            var _pDecNm = mi.jsPDecNm;
                            var _pWin = actionMenuWidget.pWin;
                            _pWin.changeDecorator( _pDecNm );
                        };
                        for ( var j = 0 ; j < portletDecorationsAllowed.length ; j++ )
                        {
                            var portletDecorationName = portletDecorationsAllowed[ j ];
                            var submenuitem = djObj.widget.createWidget( "MenuItem2", { caption: portletDecorationName, jsPDecNm: portletDecorationName } );
                            submenuWidget.addChild( submenuitem );
                        }
                        docBody.appendChild( submenuWidget.domNode );
                        jsObj.ui.addPopupMenuWidget( submenuWidget );
                    }
                }
                else
                {
                    isDev = true;
                    menulabel = aNm = aNm.aNm;
                }
                menuitem = djObj.widget.createWidget( "MenuItem2", { caption: menulabel, submenuId: submenuWidgetId, jsActNm: aNm, jsActDev: isDev } );
                menuItemsByName[ aNm ] = menuitem;
                actionMenuWidget.addChild( menuitem );
            }
            actionMenuWidget.menuItemsByName = menuItemsByName;
            docBody.appendChild( actionMenuWidget.domNode );
            jsObj.ui.addPopupMenuWidget( actionMenuWidget );
        }

        wDC.windowActionMenuHasNoImg = noMenuImg;
        if ( tPortlet )
        {
            wDC.windowActionButtonNames = btnActionNames;
            wDC.windowActionMenuNames = menuActionNames;
            wDC.windowActionMenuWidget = actionMenuWidget;
            //dojo.debug( "set portlet button names (" + this.widgetId + ") [" + btnActionNames.join( ", " ) + "]" );
            //dojo.debug( "portlet wDC.windowActionButtonOrder [" + wDCBtnOrder.join( ", " ) + "]" );
        }
        else
        {
            wDC.windowActionButtonNamesNp = btnActionNames;
            wDC.windowActionMenuNamesNp = menuActionNames;
            wDC.windowActionMenuWidgetNp = actionMenuWidget;
            //dojo.debug( "set non-portlet button names (" + this.widgetId + ") [" + btnActionNames.join( ", " ) + "]" );
        }
        return btnActionNames;
    },

    _setupTitlebar: function( wDC, wDCRemove, tPortlet, docBody, doc, jsObj, jsId, jsPrefs, jsUI, jsPage, djObj )
    {
        var djEvtObj = djObj.event;
        var aNm;
        var tooltipMgr = jsPage.tooltipMgr;
        var tbNode = this.tbNode;
        var wDCChg = ( wDCRemove && wDC );
        if ( wDCRemove )
        {
            if ( this.actionMenuWidget && wDCRemove.windowActionMenuHasNoImg )
            {
                jsUI.evtDisconnect( "after", tbNode, "oncontextmenu", this, "actionMenuOpen", djEvtObj );
            }
        
            jsPage.tooltipMgr.removeNodes( this.tooltips );
            this.tooltips = ttps = [];

            var aBtns = this.actionButtons;
            if ( aBtns )
            {
                var hasTooltip = ( wDCRemove && wDCRemove.windowActionButtonTooltip );
                for ( aNm in aBtns )
                {
                    var aBtn = aBtns[ aNm ];
                    if ( aBtn )
                    {
                        jsUI.evtDisconnect( "after", aBtn, "onclick", this, "actionBtnClick", djEvtObj );
                        if ( ! hasTooltip )
                            jsUI.evtDisconnect( "after", aBtn, "onmousedown", jsObj, "_stopEvent", djEvtObj );
                        if ( wDCChg )
                            djObj.dom.removeNode( aBtn );
                    }
                }
                this.actionButtons = aBtns = {};
            }
        }

        if ( wDC )
        {
            if ( wDC.windowActionButtonTooltip )
            {
                if ( this.actionLabels[ jsId.ACT_DESKTOP_MOVE_TILED ] != null && this.actionLabels[ jsId.ACT_DESKTOP_MOVE_UNTILED ] != null )
                    this.tooltips.push( tooltipMgr.addNode( tbNode, null, true, 1200, this, "getTitleBarTooltip", jsObj, jsUI, djEvtObj ) );
            }
    
            var btnActionNames = ( tPortlet ) ? wDC.windowActionButtonNames : wDC.windowActionButtonNamesNp;
            if ( btnActionNames == null )
            {
                btnActionNames = this._buildActionStructures( wDC, tPortlet, docBody, jsObj, jsId, jsPrefs, djObj );
            }
            for ( var i = 0 ; i < btnActionNames.length ; i++ )
            {
                aNm = btnActionNames[i];
                if ( aNm != null )
                {
                    if ( ! tPortlet || ( aNm == jsId.ACT_RESTORE || aNm == jsId.ACT_MENU || tPortlet.getAction( aNm ) != null || jsPrefs.windowActionDesktop[ aNm ] != null ) )
                    {
                        this._createActionButtonNode( aNm, doc, docBody, tooltipMgr, wDC, jsObj, jsPrefs, jsUI, djObj, djEvtObj );
                    }
                }
            }
            this.actionMenuWidget = ( tPortlet ) ? wDC.windowActionMenuWidget : wDC.windowActionMenuWidgetNp;
    
            if ( this.actionMenuWidget && wDC.windowActionMenuHasNoImg )
            {
                jsUI.evtConnect( "after", tbNode, "oncontextmenu", this, "actionMenuOpen", djEvtObj );
            }

            if ( this.ie6 && ! wDC._ie6used )
            {
                wDC._ie6used = true;
                this.actionBtnSyncDefer( false, jsObj, djObj );
            }
            else
            {
                this.actionBtnSync( jsObj, jsId );
            }
    
            if ( wDC.windowDisableResize )
                this.resizable =  false;
            if ( wDC.windowDisableMove )
                this.moveable =  false;
        }
    },

    _createActionButtonNode: function( aNm, doc, docBody, tooltipMgr, wDC, jsObj, jsPrefs, jsUI, djObj, djEvtObj )
    {
        if ( aNm != null )
        {
            var aBtn = doc.createElement( "div" );
            aBtn.className = "portletWindowActionButton";
            aBtn.style.backgroundImage = "url(" + jsPrefs.getPortletDecorationBaseUrl( this.decName ) + "/images/desktop/" + aNm + ".gif)";
            aBtn.actionName = aNm;

            this.actionButtons[ aNm ] = aBtn;
            this.tbNode.appendChild( aBtn );

            jsUI.evtConnect( "after", aBtn, "onclick", this, "actionBtnClick", djEvtObj );
            if ( wDC.windowActionButtonTooltip )
            {
                var actionLabel = this.actionLabels[ aNm ];
                this.tooltips.push( tooltipMgr.addNode( aBtn, actionLabel, true, null, null, null, jsObj, jsUI, djEvtObj ) );
            }
            else
            {
                jsUI.evtConnect( "after", aBtn, "onmousedown", jsObj, "_stopEvent", djEvtObj );
            }
        }
    },
    getTitleBarTooltip: function()
    {
        if ( ! this.getLayoutActionsEnabled() ) return null;
        if ( this.posStatic )
            return this.actionLabels[ jetspeed.id.ACT_DESKTOP_MOVE_TILED ];
        else
            return this.actionLabels[ jetspeed.id.ACT_DESKTOP_MOVE_UNTILED ];
    },

    // layout extents static methods - used for defining cached portlet decorator layout object

    /*  static  */
    _createLayoutInfo: function( decorationConfig, forIFrameStyles, dNode, cNode, tbNode, rbNode, djObj, jsObj, jsUI )
    {   // should be called once for each used portlet decorator
        var dNodeCompStyle = djObj.gcs( dNode );
        var cNodeCompStyle = djObj.gcs( cNode );

        var dLayoutInfo = jsUI.getLayoutExtents( dNode, dNodeCompStyle, djObj, jsObj );
        var cLayoutInfo = jsUI.getLayoutExtents( cNode, cNodeCompStyle, djObj, jsObj );
        var layoutInfo = { dNode: dLayoutInfo,
                           cNode: cLayoutInfo };

        var cMarginTop = Math.max( 0, cLayoutInfo.mE.t );
        var cMarginBottom = Math.max( 0, cLayoutInfo.mE.h - cLayoutInfo.mE.t );
        var cNode_mBh_adj_tb_mBh = 0;
        var cNode_mBh_adj_rb_mBh = 0;

        var tbLayoutInfo = null;
        if ( tbNode )
        {
            var tbNodeCompStyle = djObj.gcs( tbNode );
            tbLayoutInfo = jsUI.getLayoutExtents( tbNode, tbNodeCompStyle, djObj, jsObj );
            if ( ! decorationConfig.dragCursor )
            {   // we want to catch this the first time - otherwise, a recall with forIFrameStyles can occur with the tbNode styles already sync'd
                var dragCursor = tbNodeCompStyle.cursor;
                if ( dragCursor == null || dragCursor.length == 0 )
                    dragCursor = "move";
                decorationConfig.dragCursor = dragCursor;
            }
            tbLayoutInfo.mBh = djObj.getMarginBox( tbNode, tbNodeCompStyle, jsObj ).h;
            var tbMarginBottom = Math.max( 0, tbLayoutInfo.mE.h - tbLayoutInfo.mE.t );
            cNode_mBh_adj_tb_mBh = ( tbLayoutInfo.mBh - tbMarginBottom ) + Math.max( 0, (tbMarginBottom - cMarginTop) );
            layoutInfo.tbNode = tbLayoutInfo;
        }

        var rbLayoutInfo = null;
        if ( rbNode )
        {
            var rbNodeCompStyle = djObj.gcs( rbNode );
            rbLayoutInfo = jsUI.getLayoutExtents( rbNode, rbNodeCompStyle, djObj, jsObj );
            rbLayoutInfo.mBh = djObj.getMarginBox( rbNode, rbNodeCompStyle, jsObj ).h;
            var rbMarginTop = Math.max( 0, rbLayoutInfo.mE.t );
            cNode_mBh_adj_rb_mBh = ( rbLayoutInfo.mBh - rbMarginTop ) + Math.max( 0, (rbMarginTop - cMarginBottom) );
            layoutInfo.rbNode = rbLayoutInfo;
        }

        layoutInfo.cNode_mBh_LessBars = cNode_mBh_adj_tb_mBh + cNode_mBh_adj_rb_mBh;

        if ( ! forIFrameStyles )
            decorationConfig.layout = layoutInfo;
        else
            decorationConfig.layoutIFrame = layoutInfo;
    },  // _createLayoutInfo


    // action functions

    actionBtnClick: function( evt )
    {
        if ( evt == null || evt.target == null ) return;
        this.actionProcess( evt.target.actionName, evt );
    },
    actionMenuOpen: function( evt )
    {
        var jsObj = jetspeed;
        var jsId = jsObj.id;

        var menuWidget = this.actionMenuWidget;
        if ( ! menuWidget ) return;
        if ( menuWidget.isShowingNow )
            menuWidget.close();

        var aState = null;
        var aMode = null;
        if ( this.portlet )
        {
            aState = this.portlet.getCurrentActionState();
            aMode = this.portlet.getCurrentActionMode();
        }
        
        var menuItemsByName = menuWidget.menuItemsByName;
        for ( var aNm in menuItemsByName )
        {
            var menuItem = menuItemsByName[ aNm ];
            var miDisplay = ( this._isActionEnabled( aNm, aState, aMode, jsObj, jsId ) ) ? "" : "none";
            menuItem.domNode.style.display = miDisplay;   // instead of menuItem.enable()/disable()
        }
        menuWidget.pWin = this;
        menuWidget.onOpen( evt );
    },
    actionProcessDev: function( /* String */ aNm, evt )
    {
        if ( aNm == this.dbMenuDims && jetspeed.debugPWinPos )
        {
            jetspeed.debugPWinPos( this );
        }
    },
    actionProcess: function( /* String */ aNm, evt )
    {   // evt arg is needed only for opening action menu
        //dojo.debug( "actionProcess [" + ( this.portlet ? this.portlet.entityId : this.widgetId ) + ( this.portlet ? (" / " + this.widgetId) : "" ) + "]" + " actionName=" + aNm );
        var jsObj = jetspeed;
        var jsId = jsObj.id;
        if ( aNm == null ) return;
        if ( jsObj.prefs.windowActionDesktop[ aNm ] != null )
        {
            if ( aNm == jsId.ACT_DESKTOP_TILE )
            {
                this.makeTiled();
            }
            else if ( aNm == jsId.ACT_DESKTOP_UNTILE )
            {
                this.makeUntiled();
            }
            else if ( aNm == jsId.ACT_DESKTOP_HEIGHT_EXPAND )
            {
                this.makeHeightToFit( false );
            }
            else if ( aNm == jsId.ACT_DESKTOP_HEIGHT_NORMAL )
            {
                this.makeHeightVariable( false, false );
            }
        }
        else if ( aNm == jsId.ACT_MENU )
        {
            this.actionMenuOpen( evt );
        }
        else if ( aNm == jsId.ACT_MINIMIZE )
        {   // make no associated content request - just notify server of change
            if ( this.portlet && this.windowState == jsId.ACT_MAXIMIZE )
            {
                this.needsRenderOnRestore = true;
            }
            this.minimizeWindow();
            if ( this.portlet )
            {
                jsObj.changeActionForPortlet( this.portlet.getId(), jsId.ACT_MINIMIZE, null );
            }
            if ( ! this.portlet )
            {
                this.actionBtnSyncDefer( false, jsObj, dojo );
            }
        }
        else if ( aNm == jsId.ACT_RESTORE )
        {   // if minimized, make no associated content request - just notify server of change
            var deferRestoreWindow = false;
            if ( this.portlet )
            {
                if ( this.windowState == jsId.ACT_MAXIMIZE || this.needsRenderOnRestore )
                {
                    if ( this.needsRenderOnRestore )
                    {
                        deferRestoreWindow = true;
                        this.restoreOnNextRender = true;
                        this.needsRenderOnRestore = false;
                    }
                    this.portlet.renderAction( aNm );
                }
                else
                {
                    jsObj.changeActionForPortlet( this.portlet.getId(), jsId.ACT_RESTORE, null );
                }
            }
            if ( ! deferRestoreWindow )
            {
                this.restoreWindow();
            }
            if ( ! this.portlet )
            {
                this.actionBtnSyncDefer( false, jsObj, dojo );
            }
        }
        else if ( aNm == jsId.ACT_MAXIMIZE )
        {
            this.maximizeWindow();

            if ( this.portlet )
            {
                this.portlet.renderAction( aNm );
            }
            else
            {
                this.actionBtnSync( jsObj, jsId );
            }
        }
        else if ( aNm == jsId.ACT_REMOVEPORTLET )
        {
            if ( this.portlet )
            {
                var pageEditorWidget = dojo.widget.byId( jsId.PG_ED_WID );
                if ( pageEditorWidget != null )
                {
                    pageEditorWidget.deletePortlet( this.portlet.entityId, this.title );
                }
            }
        }
        else
        {
            if ( this.portlet )
                this.portlet.renderAction( aNm );
        }
    },  // actionProcess

    _isActionEnabled: function( aNm, aState, aMode, jsObj, jsId )
    {
        var jsObj = jetspeed;
        var jsId = jsObj.id;
        var enabled = false;
        var winState = this.windowState;
        
        if ( aNm == jsId.ACT_MENU )
        {
            if ( ! this._actionMenuIsEmpty( jsObj, jsId ) )
                enabled = true;
        }
        else if ( jsObj.prefs.windowActionDesktop[ aNm ] != null )
        {
            if ( this.getLayoutActionsEnabled() )
            {
                var ie6Minimized = ( this.ie6 && winState == jsId.ACT_MINIMIZE );
                if ( aNm == jsId.ACT_DESKTOP_HEIGHT_EXPAND )
                {
                    if ( ! this.heightToFit && ! ie6Minimized )
                        enabled = true;
                }
                else if ( aNm == jsId.ACT_DESKTOP_HEIGHT_NORMAL )
                {
                    if ( this.heightToFit && ! ie6Minimized )
                        enabled = true;
                }
                else if ( aNm == jsId.ACT_DESKTOP_TILE && jsObj.prefs.windowTiling )
                {
                    if ( ! this.posStatic )
                        enabled = true;
                }
                else if ( aNm == jsId.ACT_DESKTOP_UNTILE )
                {
                    if ( this.posStatic )
                        enabled = true;
                }
            }
        }
        else if ( aNm == jsId.ACT_CHANGEPORTLETTHEME )
        {
            if ( this.cP_D && this.editPageEnabled && this.getLayoutActionsEnabled() )
                enabled = true;
        }
        else if ( aNm == this.dbMenuDims )
        {
                enabled = true;
        }
        else if ( this.minimizeTempRestore != null )
        {
            if ( this.portlet )
            {
                var actionDef = this.portlet.getAction( aNm );
                if ( actionDef != null )
                {
                    if ( actionDef.id == jsId.ACT_REMOVEPORTLET )
                    {
                        if ( jsObj.page.editMode && this.getLayoutActionsEnabled() )
                            enabled = true;
                    }
                }
            }
        }
        else if ( this.portlet )
        {
            var actionDef = this.portlet.getAction( aNm );
            if ( actionDef != null )
            {
                if ( actionDef.id == jsId.ACT_REMOVEPORTLET )
                {
                    if ( jsObj.page.editMode && this.getLayoutActionsEnabled() )
                        enabled = true;
                }
                else if ( actionDef.type == jsId.PORTLET_ACTION_TYPE_MODE )
                {
                    if ( aNm != aMode )
                    {
                        enabled = true; 
                    }
                }
                else
                {   // assume actionDef.type == jsId.PORTLET_ACTION_TYPE_STATE
                    if ( aNm != aState )
                    {
                        enabled = true;
                    }
                }
            }
        }
        else
        {   // adjust visible action buttons - BOZO:NOW: this non-portlet case needs more attention
            if ( aNm == jsId.ACT_MAXIMIZE )
            {
                if ( aNm != winState && this.minimizeTempRestore == null )
                {
                    enabled = true;
                }
            }
            else if ( aNm == jsId.ACT_MINIMIZE )
            {
                if ( aNm != winState )
                {
                    enabled = true;
                }
            }
            else if ( aNm == jsId.ACT_RESTORE )
            {
                if ( winState == jsId.ACT_MAXIMIZE || winState == jsId.ACT_MINIMIZE )
                {
                    enabled = true;
                }
            }
            else if ( aNm == this.dbMenuDims )
            {
                enabled = true;
            }
        }
        return enabled;
    },  // _isActionEnabled

    _actionMenuIsEmpty: function( jsObj, jsId )
    {   // meant to be called from within _isActionEnabled call for ACT_MENU
        var actionMenuIsEmpty = true;
        var menuWidget = this.actionMenuWidget;
        if ( menuWidget )
        {
            var aState = null;
            var aMode = null;
            if ( this.portlet )
            {
                aState = this.portlet.getCurrentActionState();
                aMode = this.portlet.getCurrentActionMode();
            }

            for ( var aNm in menuWidget.menuItemsByName )
            {
                if ( aNm != jsId.ACT_MENU && this._isActionEnabled( aNm, aState, aMode, jsObj, jsId ) )
                {
                    actionMenuIsEmpty = false;
                    break;
                }
            }
        }
        return actionMenuIsEmpty ;
    },

    actionBtnSyncDefer: function( forceRepaint, jsObj, djObj )
    {   // delay helps mozilla update btn visibility on minimize and restore-from-minimized
        if ( forceRepaint && jsObj.UAie )
            forceRepaint = false;
        if ( forceRepaint )
        {
            var currentOpacity = djObj.gcs( this.domNode ).opacity;
            if ( typeof currentOpacity == "undefined" || currentOpacity == null )
                forceRepaint = false;
            else
            {   // firefox has repaint issues with (at least) window resize, where images (e.g. resize handle) can
                //         leave trails or not be displayed at the end of resize
                //    here we attempt to coerce the browser into repainting the PortletWindow by adjusting the opacity style setting
                currentOpacity = Number(currentOpacity);
                this._savedOpacity = currentOpacity;
                var adjOpacity = currentOpacity - 0.005;
                adjOpacity = ( (adjOpacity <= 0.1) ? (currentOpacity + 0.005) : adjOpacity );
                this.domNode.style.opacity = adjOpacity;
                djObj.lang.setTimeout( this, this._actionBtnSyncRepaint, 20 );
            }
        }
        if ( ! forceRepaint )
            djObj.lang.setTimeout( this, this.actionBtnSync, 10 );
    },

    _actionBtnSyncRepaint: function( jsObj, jsId )
    {
        this.actionBtnSync( jsObj, jsId );
        if ( this._savedOpacity != null )
        {
            this.domNode.style.opacity = this._savedOpacity;
            delete this._savedOpacity;
        }
    },
    actionBtnSync: function( jsObj, jsId )
    {
        if ( ! jsObj )
        {
            jsObj = jetspeed; jsId = jsObj.id;
        }
        var aState = null;
        var aMode = null;
        if ( this.portlet )
        {
            aState = this.portlet.getCurrentActionState();
            aMode = this.portlet.getCurrentActionMode();
        }
        for ( var aNm in this.actionButtons )
        {
            var showBtn = this._isActionEnabled( aNm, aState, aMode, jsObj, jsId );
            var buttonNode = this.actionButtons[ aNm ];
            buttonNode.style.display = ( showBtn ) ? "block" : "none";
        }
    },

    _postCreateMaximizeWindow: function()
    {
        var jsObj = jetspeed;
        var jsId = jsObj.id;
        this.maximizeWindow();
        if ( this.portlet )
        {
            this.portlet.renderAction( jsId.ACT_MAXIMIZE );
        }
        else
        {
            this.actionBtnSync( jsObj, jsId );
        }
    },

    minimizeWindowTemporarily: function( setNeedsRenderOnRestore )
    {
        var jsObj = jetspeed;
        var jsId = jsObj.id;
        if ( setNeedsRenderOnRestore )
            this.needsRenderOnRestore = true;
        if ( ! this.minimizeTempRestore )
        {
            this.minimizeTempRestore = this.windowState;
            if ( this.windowState != jsId.ACT_MINIMIZE )
            {
                this.minimizeWindow( false );
            }
            this.actionBtnSync( jsObj, jsId );
        }
    },

    /*  static  */
    restoreAllFromMinimizeWindowTemporarily: function()
    {
        var jsObj = jetspeed;
        var jsId = jsObj.id;
        var idMin = jsId.ACT_MINIMIZE, idMax = jsId.ACT_MAXIMIZE;
        var pWin;
        var colNodes = [];
        var winToMaximize = null;
        var pWins = jsObj.page.getPWins();
        for ( var i = 0; i < pWins.length; i++ )
        {
            pWin = pWins[i];
            var restoreToWindowState = pWin.minimizeTempRestore;
            delete pWin.minimizeTempRestore;
            if ( restoreToWindowState )
            {

                if ( restoreToWindowState == idMax )
                    winToMaximize = pWin;
                if ( restoreToWindowState == idMin )
                {
                    // nothing to do
                }
                else if ( pWin.needsRenderOnRestore && pWin.portlet )
                {
                    deferRestoreWindow = true;
                    if ( restoreToWindowState != idMax )
                        pWin.restoreOnNextRender = true;
                    delete pWin.needsRenderOnRestore;
                    pWin.portlet.renderAction( restoreToWindowState );
                }
                else 
                {
                    pWin.restoreWindow();
                    if ( ! pWin.portlet )
                    {
                        pWin.actionBtnSyncDefer( false, jsObj, dojo );
                    }
                }
                pWin.actionBtnSync( jsObj, jsId );
            }

            if ( pWin.ie6 && pWin.posStatic )
            {
                var colDomNode = pWin.domNode.parentNode;
                var added = false;
                for ( var j = 0 ; j < colNodes.length ; j++ )
                {
                    if ( colNodes[j] == colDomNode )
                    {
                        added = true;
                        break;
                    }
                }
                if ( ! added )
                    colNodes.push( colDomNode );
            }
        }

        jsObj.widget.showAllPortletWindows();

        if ( winToMaximize != null )
        {
            winToMaximize.maximizeWindow();
        }

        if ( jsObj.UAie6 )
        {
            // jsObj.page.displayAllPWins();   // line is from when this code was in PageEditor.editMoveExit
            if ( colNodes.length > 0 )
            {
                var zappedContentRestorer = new jetspeed.widget.IE6ZappedContentRestorer( colNodes );
                dojo.lang.setTimeout( zappedContentRestorer, zappedContentRestorer.showNext, 20 );
            }
        }
    },

    minimizeWindow: function( minimizeOnLoad )
    {
        if ( ! this.tbNode )
            return;

        var jsObj = jetspeed;
        if ( this.windowState == jetspeed.id.ACT_MAXIMIZE )
        {
            jsObj.widget.showAllPortletWindows() ;
            this.restoreWindow();
        }
        else if ( ! minimizeOnLoad )
        {
            this._updtDimsObj( false, false );
        }

        var disIdx = jsObj.css.cssDis;
        this.cNodeCss[ disIdx ] = "none";
        if ( this.rbNodeCss )
            this.rbNodeCss[ disIdx ] = "none";

        this.windowState = jsObj.id.ACT_MINIMIZE;
        if ( this.ie6 )
            this.containerNode.style.display = "none";   // in ie6, this needs to happen before bulk changes in alterCss
        this._alterCss( true, true );
    },  // minimizeWindow

    maximizeWindow: function()
    {
        var jsObj = jetspeed;
        var jsId = jsObj.id;
        var dNode = this.domNode;
        var hideAllBut = [ this.widgetId ];
        //if ( this.dbOn )   // show debug window when other window is maximized
        //    hideAllBut.push( jetspeed.debugWindowId() );
        jsObj.widget.hideAllPortletWindows( hideAllBut ) ;
        if ( this.windowState == jsId.ACT_MINIMIZE )
        {
            this.restoreWindow();
        }
        var preMaxPosStatic = this.posStatic;
        this.preMaxPosStatic = preMaxPosStatic;
        this.preMaxHeightToFit = this.heightToFit;
        var tiledStateWillChange = preMaxPosStatic;

        this._updtDimsObj( false, tiledStateWillChange );

        this._setTitleBarDragging( true, jsObj.css, false );

        this.posStatic = false;
        this.heightToFit = false;

        this._setMaximizeSize( true, true, jsObj );    

        this._alterCss( true, true );

        if ( preMaxPosStatic )
            jetspeedDesktop.appendChild( dNode );

        window.scrollTo(0, 0);

		this.windowState = jsId.ACT_MAXIMIZE;
	},  // maximizeWindow

    _setMaximizeSize: function( addDeferredScrollBarSync, suppressAlterCss, jsObj )
    {
        if ( jsObj == null ) jsObj = jetspeed;
        var adjH = 0, adjW = 0;
        if ( addDeferredScrollBarSync )
        {
            var scrollWidth = jsObj.ui.scrollWidth;
            if ( scrollWidth == null )
                scrollWidth = jsObj.ui.getScrollbar( jsObj );

            adjW = adjH = ((scrollWidth + 5) * -1);
        }
        
        var jetspeedDesktop = document.getElementById( jsObj.id.DESKTOP );
        
        var djObj = dojo;
        var djH = djObj.html;
        var yPos = djH.getAbsolutePosition( jetspeedDesktop, true ).y;    // passing true to fix position at top (so not affected by vertically scrolled window)
        var viewport = djH.getViewport();
        var docPadding = djH.getPadding( jsObj.docBody );        
    
        // hardcoded to fill document.body width leaving 1px on each side
        var dimsUntiled = { w: ( viewport.width + adjW ) - docPadding.width - 2,
                            h: ( viewport.height + adjH ) - docPadding.height - yPos,
                            l: 1,
                            t: yPos };

        this.dimsUntiledTemp = dimsUntiled;
        
        if ( ! suppressAlterCss )
        {
            this._alterCss( false, false, true );
        }

        if ( addDeferredScrollBarSync )
        {
            djObj.lang.setTimeout( this, this._setMaximizeSize, 40, false, false, jsObj );   // to allow the scroll bars to go away
        }
        return dimsUntiled;
    },

	restoreWindow: function()
    {
        var jsObj = jetspeed;
        var jsId = jsObj.id;
        var jsCss = jsObj.css;
        var dNode = this.domNode;
        var currentlyAbsolute = false;
        if ( dNode.style.position == "absolute" )
        {
            currentlyAbsolute = true;
        }

        var lastPI = null;
        var fromMax = false;
        if ( this.windowState == jsId.ACT_MAXIMIZE )
        {
            jsObj.widget.showAllPortletWindows() ;
            this.posStatic = this.preMaxPosStatic;
            this.heightToFit = this.preMaxHeightToFit;
            this.dimsUntiledTemp = null;
            fromMax = true;
        }
        
        var disIdx = jsCss.cssDis;
        this.cNodeCss[ disIdx ] = "block";
        if ( this.rbNodeCss && this.resizebarEnabled )
            this.rbNodeCss[ disIdx ] = "block";
        
        this.windowState = jsId.ACT_RESTORE;  // "normal"

        this._setTitleBarDragging( true, jsObj.css );

        var iNodeCssTemp = null;
        var ie6 = this.ie6;
        if ( ! ie6 )
        {
            this._alterCss( true, true );
        }
        else
        {
            if ( this.heightToFit )
            {
                iNodeCssTemp = this.iNodeCss;
                // blank out height so that it can be recalculated based on actual content height
                this.iNodeCss = null;   // if not null, _alterCss will not set content relative height
            }

            this._alterCss( true, true );
        }

        if ( this.posStatic && currentlyAbsolute )
        {   // tiled window in maximized or window just set from untiled to tiled - needs to be placed back in previous column/row
            this._tileWindow( jsObj );
        }

        if ( ie6 )
        {
            this._updtDimsObj( false, false, true, false, true );  // force update width and height
            if ( fromMax )
                this._resetIE6TiledSize( false, true );
            if ( iNodeCssTemp != null )
                this.iNodeCss = iNodeCssTemp;
            this._alterCss( false, false, true );   // resize    // xxxx
        }
    },  // restoreWindow

    _tileWindow: function( jsObj )
    {
        if ( ! this.posStatic ) return;
        var dNode = this.domNode;
        var dimsPrevious = this.getDimsObj( this.posStatic );
        var cannotPlace = true;
        if ( dimsPrevious != null )
        {
            var colInfo = dimsPrevious.colInfo;
            if ( colInfo != null && colInfo.colI != null )
            {
                var colObj = jsObj.page.columns[ colInfo.colI ];
                var colNode = ( (colObj != null) ? colObj.domNode : null);
                if ( colNode != null )
                {
                    var colChildAtIndex = null;
                    var colNodeChildLen = colNode.childNodes.length;
                    if ( colNodeChildLen == 0 )
                    {
                        colNode.appendChild( dNode );
                        cannotPlace = false;
                    }
                    else
                    {
                        var colChild, colChildId, colChildIndex = 0;
                        if ( colInfo.pSibId != null || colInfo.nSibId != null )
                        {
                            colChild = colNode.firstChild;
                            do
                            {
                                colChildId = colChild.id;
                                if ( colChildId == null ) continue;
                                if ( colChildId == colInfo.pSibId )
                                {
                                    dojo.dom.insertAfter( dNode, colChild );
                                    cannotPlace = false;
                                }
                                else if ( colChildId == colInfo.nSibId )
                                {
                                    dojo.dom.insertBefore( dNode, colChild );
                                    cannotPlace = false;
                                }
                                else if ( colChildIndex == colInfo.elmtI )
                                {
                                    colChildAtIndex = colChild;
                                }
                                colChild = colChild.nextSibling;
                                colChildIndex++;
                            } while ( cannotPlace && colChild != null )
                        }
                    }
                    if ( cannotPlace )
                    {
                        if ( colChildAtIndex != null )
                        {
                            dojo.dom.insertBefore( dNode, colChildAtIndex );
                        }
                        else
                        {
                            dojo.dom.prependChild( dNode, colNode );
                        }
                        cannotPlace = false;
                    }
                }
            }
        }
        if ( cannotPlace )
        {
            var defaultColumn = jsObj.page.getColumnDefault();
            if ( defaultColumn != null )
                dojo.dom.prependChild( dNode, defaultColumn.domNode );
        }
	},  // _tileWindow

    getDimsObj: function( posStatic, doNotReturnTemp )
    {
        return ( posStatic ? ( (this.dimsTiledTemp != null && ! doNotReturnTemp) ? this.dimsTiledTemp : this.dimsTiled ) : ( (this.dimsUntiledTemp != null && ! doNotReturnTemp) ? this.dimsUntiledTemp : this.dimsUntiled ) );
    },
    _updtDimsObj: function( updtOnlyIfPropIsUndefined, tiledStateWillChange, ltNoTouch, whNoTouch, whForce, copyToAndSetTemp )
    {
        var jsObj = jetspeed;
        var djObj = dojo;
        var dNode = this.domNode;
        var posStatic = this.posStatic;

        var dimsCurrent = this.getDimsObj( posStatic, copyToAndSetTemp );

        var lORtIsUndef = ( ! ltNoTouch && ! posStatic && ( ! updtOnlyIfPropIsUndefined || dimsCurrent.l == null || dimsCurrent.t == null ) );
        var wORhIsUndef = ( ! whNoTouch && ( ! updtOnlyIfPropIsUndefined || lORtIsUndef || whForce || dimsCurrent.w == null || dimsCurrent.h == null ) );
        
        if ( wORhIsUndef || lORtIsUndef )
        {
            var dNodeLayoutInfo = this._getLayoutInfo().dNode;
            if ( wORhIsUndef )
            {
                var dNodeMarginSize = jsObj.ui.getMarginBoxSize( dNode, dNodeLayoutInfo );
                dimsCurrent.w = dNodeMarginSize.w;
                dimsCurrent.h = dNodeMarginSize.h;
                if ( ! posStatic ) lORtIsUndef = true;
            }
            if ( lORtIsUndef )
            {
                var winAbsPos = djObj.html.getAbsolutePosition( dNode, true );
                dimsCurrent.l = winAbsPos.x - dNodeLayoutInfo.mE.l - dNodeLayoutInfo.pbE.l;
                dimsCurrent.t = winAbsPos.y - dNodeLayoutInfo.mE.t - dNodeLayoutInfo.pbE.t;
            }
        }
        if ( posStatic )
        {
            if ( tiledStateWillChange || copyToAndSetTemp && dimsCurrent.colInfo == null )
            {   // record col/row location
                var nodeIndex = 0, backNode = dNode.previousSibling, nextNode = dNode.nextSibling;
                var prevId = ( backNode != null ? backNode.id : null ), nextId = ( nextNode != null ? nextNode.id : null );
                if ( backNode != null ) prevId = backNode.id;
                while ( backNode != null )
                {
                    nodeIndex++;
                    backNode = backNode.previousSibling;
                }
                dimsCurrent.colInfo = { elmtI: nodeIndex, pSibId: prevId, nSibId: nextId, colI: this.getPageColumnIndex() };
            }
            
            if ( copyToAndSetTemp )
            {
                this.dimsTiledTemp = { w: dimsCurrent.w,
                                       h: dimsCurrent.h,
                                       colInfo: dimsCurrent.colInfo };
                dimsCurrent = this.dimsTiledTemp;
            }
        }
        else
        {
            if ( copyToAndSetTemp )
            {
                this.dimsUntiledTemp = { w: dimsCurrent.w,
                                         h: dimsCurrent.h,
                                         l: dimsCurrent.l,
                                         t: dimsCurrent.t };
                dimsCurrent = this.dimsUntiledTemp;
            }
        }
        return dimsCurrent;
    },  // _updtDimsObj

    getLayoutActionsEnabled: function()
    {
        return ( this.windowState != jetspeed.id.ACT_MAXIMIZE && ( this.portlet == null || ( ! this.portlet.layoutActionsDisabled || (this.cL_NA_ED == true) ) ) );
    },
    _setTitleBarDragging: function( suppressStyleUpdate, jsCss, enableDrag )
    {
        var tbNode = this.tbNode;
        if ( ! tbNode )
            return;

        if ( typeof enableDrag == "undefined" )
            enableDrag = this.getLayoutActionsEnabled();
        
        var resizeHandle = this.resizeHandle;
        var cursorVal = null;
        var wDC = this.decConfig;

        var enableResize = enableDrag;
        if ( enableResize && ! this.resizebarEnabled )
            enableResize = false;
        if ( enableDrag && ! this.titlebarEnabled )
            enableDrag = false;

        if ( enableDrag )
        {
            cursorVal = wDC.dragCursor;
            if ( this.drag )
                this.drag.enable();
        }
        else
        {
            cursorVal = "default";            
            if ( this.drag )
                this.drag.disable();
        }

        if ( enableResize )
        {
            if ( resizeHandle )
                resizeHandle.domNode.style.display = "";
        }
        else
        {
            if ( resizeHandle )
                resizeHandle.domNode.style.display = "none";
        }

        this.tbNodeCss[ jsCss.cssCur ] = cursorVal;
        if ( ! suppressStyleUpdate )
            tbNode.style.cursor = cursorVal;
    },  // _setTitleBarDragging

    onMouseDown: function( /*Event*/ evt )
    {   // summary: callback for click anywhere in window
        this.bringToTop( evt, false, false, jetspeed );
    },

    bringToTop: function( evt, inclStatic, suppressSubmitChange, jsObj )
    {
        if ( ! this.posStatic )
        {   // bring-to-front
            var jsPage = jsObj.page;
            var jsCss = jsObj.css;
            var dNodeCss = this.dNodeCss;
            var zHigh = jsPage.getPWinHighZIndex();
            var zCur = dNodeCss[ jsCss.cssZIndex ];
            if ( zHigh != zCur )
            {
                var zTop = this._setAsTopZIndex( jsPage, jsCss, dNodeCss, false );
                if ( this.windowInitialized )
                {
                    this.domNode.style.zIndex = zTop;
                    if ( ! suppressSubmitChange && this.portlet && this.windowState != jetspeed.id.ACT_MAXIMIZE )
                        this.portlet.submitWinState();
                }
                //dojo.debug( "bringToTop [" + this.widgetId + "] zIndex   before=" + zCur + " after=" + zTop );
            }
        }
        else if ( inclStatic )
        {
            var zTop = this._setAsTopZIndex( jsPage, jsCss, dNodeCss, true );
            if ( this.windowInitialized )
            {
                this.domNode.style.zIndex = zTop;
            }
        }
    },  // bringToTop

    _setAsTopZIndex: function( jsPage, jsCss, dNodeCss, posStatic )
    {
        var zTop = String( jsPage.getPWinTopZIndex( posStatic ) );
        dNodeCss[ jsCss.cssZIndex ] = zTop;
        return zTop;
    },

    makeUntiled: function()
    {
        var jsObj = jetspeed;

        this._updtDimsObj( false, true );
        
        this.posStatic = false;
        this._updtDimsObj( true, false );

        this._setAsTopZIndex( jsObj.page, jsObj.css, this.dNodeCss, false );

        this._alterCss( true, true );
        
        var prevParentNode = this.domNode.parentNode;

        var addToElmt = document.getElementById( jetspeed.id.DESKTOP );
        addToElmt.appendChild( this.domNode );

        jsObj.page.columnEmptyCheck( prevParentNode );

        if ( this.windowState == jsObj.id.ACT_MINIMIZE )
            this.minimizeWindow();

        if ( this.portlet )
            this.portlet.submitWinState();

        this._addUntiledEvents();
    },  // makeUntiled

    makeTiled: function()
    {
        this.posStatic = true;

        var jsObj = jetspeed;
        this._setAsTopZIndex( jsObj.page, jsObj.css, this.dNodeCss, true );
        
        this._alterCss( true, true );

        this._tileWindow( jsObj );

        jsObj.page.columnEmptyCheck( this.domNode.parentNode );

        if ( this.portlet )
            this.portlet.submitWinState();

        this._removeUntiledEvents();
    },  // makeTiled

    _addUntiledEvents: function()
    {
        if ( this._untiledEvts == null )
        {
            this._untiledEvts = [ jetspeed.ui.evtConnect( "after", this.domNode, "onmousedown", this, "onMouseDown" ) ];
        }
    },
    _removeUntiledEvents: function()
    {
        if ( this._untiledEvts != null )
        {
            jetspeed.ui.evtDisconnectWObjAry( this._untiledEvts );
            delete this._untiledEvts;
        }
    },

    makeHeightToFit: function( suppressSubmitChange )
    {
        var domNodePrevMarginBox = dojo.html.getMarginBox( this.domNode ) ;

        this.heightToFit = true;

        if ( this.ie6 )
        {
            var iNodeCss = this.iNodeCss;
            // blank out height so that it can be recalculated based on actual content height
            this.iNodeCss = null;   // if not null, _alterCss will not set content relative height
            this._alterCss( false, true );
            this._updtDimsObj( false, false, true, false, true );  // force update width and height
            this.iNodeCss = iNodeCss;
        }

        this._alterCss( false, true );

        if ( ! suppressSubmitChange && this.portlet )
            this.portlet.submitWinState();
    },  // makeHeightToFit

    makeHeightVariable: function( suppressSubmitChange, isResizing )
    {
        var dimsCurrent = this.getDimsObj( this.posStatic );

        var dNodeLayoutInfo = this._getLayoutInfo().dNode;
        var dNodeMarginBox = jetspeed.ui.getMarginBoxSize( this.domNode, dNodeLayoutInfo );
        dimsCurrent.w = dNodeMarginBox.w;
        dimsCurrent.h = dNodeMarginBox.h;

        this.heightToFit = false;

        this._alterCss( false, true );
    
        //dojo.debug( "makeHeightVariable [" + this.widgetId + "] containerNode NEW style.width=" + this.containerNode.style.width + " style.height=" + this.containerNode.style.height );

        if ( ! isResizing && this.iframesInfo )
            dojo.lang.setTimeout( this, this._forceRefreshZIndex, 70 );   // needs a jolt to make iframe adjust

        if ( ! suppressSubmitChange && this.portlet )
            this.portlet.submitWinState();
    },  // makeHeightVariable

    editPageInitiate: function( cP_D, cL_NA_ED, jsObj, jsCss, suppressAlterCss )
    {
        this.editPageEnabled = true;
        this.cP_D = cP_D;
        this.cL_NA_ED = cL_NA_ED;
        var wDC = this.decConfig;
        if ( ! wDC.windowTitlebar || ! wDC.windowResizebar )
        {
            var disIdx = jsCss.cssDis;
            if ( ! wDC.windowTitlebar )
            {
                this.titlebarEnabled = true;
                if ( this.tbNodeCss )
                    this.tbNodeCss[ disIdx ] = "block";
            }
            if ( ! wDC.windowResizebar )
            {
                this.resizebarEnabled = true;
                if ( this.rbNodeCss && this.windowState != jsObj.id.ACT_MINIMIZE )
                    this.rbNodeCss[ disIdx ] = "block";
            }
            this._setTitleBarDragging( false, jsCss );
            if ( ! suppressAlterCss )
                this._alterCss( true, true );
        }
        else
        {
            this._setTitleBarDragging( false, jsCss );
        }
    },

    editPageTerminate: function( jsCss, suppressAlterCss )
    {
        this.editPageEnabled = false;
        delete this.cP_D;
        delete this.cL_NA_ED;
        var wDC = this.decConfig;
        if ( ! wDC.windowTitlebar || ! wDC.windowResizebar )
        {
            var disIdx = jsCss.cssDis;
            if ( ! wDC.windowTitlebar )
            {
                this.titlebarEnabled = false;
                if ( this.tbNodeCss )
                    this.tbNodeCss[ disIdx ] = "none";
            }
            if ( ! wDC.windowResizebar )
            {
                this.resizebarEnabled = false;
                if ( this.rbNodeCss )
                    this.rbNodeCss[ disIdx ] = "none";
            }
            this._setTitleBarDragging( false, jsCss );
            if ( ! suppressAlterCss )
                this._alterCss( true, true );
        }
        else
        {
            this._setTitleBarDragging( false, jsCss );
        }
    },

    changeDecorator: function( portletDecorationName )
    {
        var jsObj = jetspeed;
        var jsCss = jsObj.css;
        var jsId = jsObj.id;
        var jsUI = jsObj.ui;
        var jsPrefs = jsObj.prefs;
        var djObj = dojo;

        var wDCRemove = this.decConfig;
        if ( wDCRemove && wDCRemove.name == portletDecorationName ) return;
        
        var wDC = jsObj.loadPortletDecorationStyles( portletDecorationName, jsPrefs );
        if ( ! wDC ) return;

        var tPortlet = this.portlet;
        if ( tPortlet )
            tPortlet._submitAjaxApi( "updatepage", "&method=update-portlet-decorator&portlet-decorator=" + portletDecorationName );
        this.decConfig = wDC;
        this.decName = wDC.name;

        var dNode = this.domNode;
        var cNode = this.containerNode;

        var iframesInfoCur = this.iframesInfo;
        var iframeLayout = ( iframesInfoCur && iframesInfoCur.layout );
        var layoutInfo = ( ! iframeLayout ? wDC.layout : wDC.layoutIFrame );
        if ( ! layoutInfo )
        {
            if ( ! iframeLayout )
                this._createLayoutInfo( wDC, false, dNode, cNode, this.tbNode, this.rbNode, djObj, jsObj, jsUI );
            else
                this._createLayoutInfo( wDC, true, dNode, cNode, this.tbNode, this.rbNode, djObj, jsObj, jsUI );
        }

        this._setupTitlebar( wDC, wDCRemove, this.portlet, jsObj.docBody, document, jsObj, jsObj.id, jsPrefs, jsUI, jsObj.page, djObj );

        dNode.className = wDC.dNodeClass;
        if ( iframeLayout )
            cNode.className = wDC.cNodeClass + " " + this.iframeCoverContainerClass;
        else
            cNode.className = wDC.cNodeClass;
        
        var disIdx = jsCss.cssDis;
        this.titlebarEnabled = true;
        if ( this.tbNodeCss )
            this.tbNodeCss[ disIdx ] = "block";
        this.resizebarEnabled = true;
        if ( this.rbNodeCss && this.windowState != jsId.ACT_MINIMIZE )
            this.rbNodeCss[ disIdx ] = "block";
        
        if ( this.editPageEnabled )
        {
            this.editPageInitiate( this.cP_D, this.cL_NA_ED, jsObj, jsCss, true );
        }
        else
        {
            this.editPageTerminate( jsCss, true );
        }
        this._setTitleBarDragging( true, jsCss );
        this._alterCss( true, true );
    },
    
    resizeTo: function( w, h, force )
    {
        var dimsCurrent = this.getDimsObj( this.posStatic );

        dimsCurrent.w = w;
        dimsCurrent.h = h;

        this._alterCss( false, false, true );

        if ( ! this.windowIsSizing )
        {
            var resizeHandle = this.resizeHandle;
            if ( resizeHandle != null && resizeHandle._isSizing )
            {
                jetspeed.ui.evtConnect( "after", resizeHandle, "_endSizing", this, "endSizing" );
                // NOTE: connecting directly to document.body onmouseup results in notification for second and subsequent onmouseup
                this.windowIsSizing = true;
            }
        }
        this.resizeNotifyChildWidgets();
    },  // resizeTo

    resizeNotifyChildWidgets: function()
    {
        if ( this.childWidgets )
        {
            var childWidgets = this.childWidgets;
            var childWidgetsLen = childWidgets.length, childWidget;
            for ( var i = 0 ; i < childWidgetsLen ; i++ )
            {
                try
                {
                    childWidget = childWidgets[i];
                    if ( childWidget )
                        childWidget.checkSize();
                }
                catch(e)
                {
                }
            }
        }
    },

    _getLayoutInfo: function()
    {
        var iframesInfoCur = this.iframesInfo;
        return ( ( ! ( iframesInfoCur && iframesInfoCur.layout ) ) ? this.decConfig.layout : this.decConfig.layoutIFrame );
    },
    _getLayoutInfoMoveable: function()
    {
        return this._getLayoutInfo().dNode;
    },

    onBrowserWindowResize: function()
    {
        var jsObj = jetspeed;
        if ( this.ie6 )
        {
            this._resetIE6TiledSize( false );
        }
        if ( this.windowState == jsObj.id.ACT_MAXIMIZE )
        {
            this._setMaximizeSize( true, false, jsObj );
        }
    },

    _resetIE6TiledSize: function( changeTiledState, suppressAlterCss )
    {
        var posStatic = this.posStatic;
        if ( posStatic )
        {
            var dNode = this.domNode;
            var dimsCurrent = this.getDimsObj( posStatic );
            dimsCurrent.w = Math.max( 0, this.domNode.parentNode.offsetWidth - this.colWidth_pbE );
            if ( ! suppressAlterCss )
                this._alterCss( changeTiledState, false, false, false, true );    // changeWidth
        }
    },

    _alterCss: function( changeTiledState, changeHeightToFit, changeResize, changePosition, changeWidth, suppressStyleUpdate )
    {
        var jsObj = jetspeed;
        var jsCss = jsObj.css;
        var iframesInfoCur = this.iframesInfo;
        var iframeLayout = ( iframesInfoCur && iframesInfoCur.layout );
        var layoutInfo = ( ! iframeLayout ? this.decConfig.layout : this.decConfig.layoutIFrame );

        var dNodeCss = this.dNodeCss, cNodeCss = null, tbNodeCss = null, rbNodeCss = null, iNodeCssSet = false, iNodeCss = this.iNodeCss, iCvrIE6Css = null;
        if ( iNodeCss && iframeLayout )
            iCvrIE6Css = iframesInfoCur.iframeCoverIE6Css;

        var posStatic = this.posStatic;
        var effectivePosStatic = ( posStatic && iNodeCss == null );
        var heightToFit = this.heightToFit;

        var setWidth = ( changeTiledState || changeWidth || ( changeResize && ! effectivePosStatic ) );
        var setHeight = ( changeHeightToFit || changeResize );
        var setPosition = ( changeTiledState || changePosition );
        var setOverflow = ( changeHeightToFit || ( changeResize && iframeLayout ) );

        var dimsCurrent = this.getDimsObj( posStatic );

        if ( changeTiledState )
        {
            dNodeCss[ jsCss.cssPos ] = ( posStatic ? "relative" : "absolute" );
        }

        var setIFrame = null, setIFrameH = null;
        if ( changeHeightToFit )
        {
            if ( iframeLayout )
            {
                var ifrmInfo = this.getIFramesAndObjects( false, true );
                if ( ifrmInfo && ifrmInfo.iframes && ifrmInfo.iframes.length == 1 && iframesInfoCur.iframesSize && iframesInfoCur.iframesSize.length == 1 )
                {
                    var ifrmH = iframesInfoCur.iframesSize[0].h;
                    if ( ifrmH != null )
                    {
                        setIFrame = ifrmInfo.iframes[0];
                        setIFrameH = ( heightToFit ? ifrmH : ( ! jsObj.UAie ? "100%" : "99%" ) );
                        suppressStyleUpdate = false;
                    }
                }
            }
        }

        if ( setOverflow )
        {
            cNodeCss = this.cNodeCss;
            var ofXIdx = jsCss.cssOx, ofYIdx = jsCss.cssOy;
            if ( heightToFit && ! iframeLayout )
            {
                dNodeCss[ ofYIdx ] = "hidden";   // BOZO:NOW: was "visible" prior to 2007-11-05
                cNodeCss[ ofYIdx ] = "visible";
            }
            else
            {
                dNodeCss[ ofYIdx ] = "hidden";
                cNodeCss[ ofYIdx ] = ( ! iframeLayout ? "auto" : "hidden" );;
            }            
        }

        if ( setPosition )
        {
            var lIdx = jsCss.cssL, luIdx = jsCss.cssLU;
            var tIdx = jsCss.cssT, tuIdx = jsCss.cssTU;
            if ( posStatic )
            {
                dNodeCss[ lIdx ] = "auto";
                dNodeCss[ luIdx ] = "";
                dNodeCss[ tIdx ] = "auto";
                dNodeCss[ tuIdx ] = "";
            }
            else
            {
                dNodeCss[ lIdx ] = dimsCurrent.l;
                dNodeCss[ luIdx ] = "px";
                dNodeCss[ tIdx ] = dimsCurrent.t;
                dNodeCss[ tuIdx ] = "px";
            }
        }

        if ( setHeight )
        {
            cNodeCss = this.cNodeCss;
            var hIdx = jsCss.cssH, huIdx = jsCss.cssHU;
            if ( heightToFit && iNodeCss == null )
            {
                dNodeCss[ hIdx ] = "";
                dNodeCss[ huIdx ] = "";
                cNodeCss[ hIdx ] = "";
                cNodeCss[ huIdx ] = "";
            }
            else
            {
                var h = dimsCurrent.h;
                var disIdx = jsObj.css.cssDis;
                var dNodeCBHeight;
                var cNodeCBHeight;
                if ( cNodeCss[ disIdx ] == "none" )
                {
                    dNodeCBHeight = layoutInfo.tbNode.mBh;
                    cNodeCBHeight = "";
                    cNodeCss[ huIdx ] = "";
                }
                else
                {
                    dNodeCBHeight = (h - layoutInfo.dNode.lessH);
                    cNodeCBHeight = dNodeCBHeight - layoutInfo.cNode.lessH - layoutInfo.cNode_mBh_LessBars;
                    cNodeCss[ huIdx ] = "px";
                }
                dNodeCss[ hIdx ] = dNodeCBHeight;
                dNodeCss[ huIdx ] = "px";
                cNodeCss[ hIdx ] = cNodeCBHeight;
                if ( iNodeCss )
                {
                    iNodeCss[ hIdx ] = dNodeCBHeight;
                    iNodeCss[ huIdx ] = "px";
                    iNodeCssSet = true;
                    if ( iCvrIE6Css )
                    {
                        iCvrIE6Css[ hIdx ] = cNodeCBHeight;
                        iCvrIE6Css[ huIdx ] = cNodeCss[ huIdx ];
                    }
                }
            }
        }

        if ( setWidth )
        {
            var w = dimsCurrent.w;
            cNodeCss = this.cNodeCss;
            tbNodeCss = this.tbNodeCss;
            rbNodeCss = this.rbNodeCss;
            var wIdx = jsCss.cssW, wuIdx = jsCss.cssWU;
            if ( effectivePosStatic && ( ! this.ie6 || ! w ) )
            {
                dNodeCss[ wIdx ] = "";
                dNodeCss[ wuIdx ] = "";
                cNodeCss[ wIdx ] = "";
                cNodeCss[ wuIdx ] = "";
                if ( tbNodeCss )
                {
                    tbNodeCss[ wIdx ] = "";
                    tbNodeCss[ wuIdx ] = "";
                }
                if ( rbNodeCss )
                {
                    rbNodeCss[ wIdx ] = "";
                    rbNodeCss[ wuIdx ] = "";
                }
            }
            else
            {
                var dNodeCBWidth = (w - layoutInfo.dNode.lessW);
                dNodeCss[ wIdx ] = dNodeCBWidth;
                dNodeCss[ wuIdx ] = "px";
                cNodeCss[ wIdx ] = dNodeCBWidth - layoutInfo.cNode.lessW;
                cNodeCss[ wuIdx ] = "px";
                if ( tbNodeCss )
                {
                    tbNodeCss[ wIdx ] = dNodeCBWidth - layoutInfo.tbNode.lessW;
                    tbNodeCss[ wuIdx ] = "px";
                }
                if ( rbNodeCss )
                {
                    rbNodeCss[ wIdx ] = dNodeCBWidth- layoutInfo.rbNode.lessW;
                    rbNodeCss[ wuIdx ] = "px";
                }
                if ( iNodeCss )
                {
                    iNodeCss[ wIdx ] = dNodeCBWidth;
                    iNodeCss[ wuIdx ] = "px";
                    iNodeCssSet = true;
                    if ( iCvrIE6Css )
                    {
                        iCvrIE6Css[ wIdx ] = cNodeCss[ wIdx ];
                        iCvrIE6Css[ wuIdx ] = cNodeCss[ wuIdx ];
                    }
                }
            }
        }

        if ( ! suppressStyleUpdate )
        {
            this.domNode.style.cssText = dNodeCss.join( "" );
            if ( cNodeCss )
                this.containerNode.style.cssText = cNodeCss.join( "" );
            if ( tbNodeCss )
                this.tbNode.style.cssText = tbNodeCss.join( "" );
            if ( rbNodeCss )
                this.rbNode.style.cssText = rbNodeCss.join( "" );
            if ( iNodeCssSet )
            {
                this.bgIframe.iframe.style.cssText = iNodeCss.join( "" );
                if ( iCvrIE6Css )
                    iframesInfoCur.iframeCover.style.cssText = iCvrIE6Css.join( "" );
            }
        }
        if ( setIFrame && setIFrameH )
            this._deferSetIFrameH( setIFrame, setIFrameH, false, 50 );
    },  // _alterCss

    _deferSetIFrameH: function( setIFrame, setIFrameH, forceRefresh, waitFor, forceRefreshWaitFor )
    {
        if ( ! waitFor ) waitFor = 100;
        var pWin = this;
        window.setTimeout( function()
        {
            //dojo.debug( "_deferSetIFrameH set iframe height to " + setIFrameH + " (current=" + setIFrame.height + ")" );
            setIFrame.height = setIFrameH;
            if ( forceRefresh )
            {
                if ( forceRefreshWaitFor == null ) forceRefreshWaitFor = 50;
                if ( forceRefreshWaitFor == 0 )
                    pWin._forceRefreshZIndexAndForget();
                else
                    dojo.lang.setTimeout( pWin, pWin._forceRefreshZIndexAndForget, forceRefreshWaitFor );
            }
        }, waitFor ) ;
    },

    _getWindowMarginBox: function( dNodeLayoutInfo, jsObj )
    {
        var dNode = this.domNode;
        if ( dNodeLayoutInfo == null )
            dNodeLayoutInfo = this._getLayoutInfo().dNode;
        var parentLayoutInfo = null;
        if ( jsObj.UAope )  // needs parentNode layout-info 
            parentLayoutInfo = ( this.posStatic ? jsObj.page.layoutInfo.column : jsObj.page.layoutInfo.desktop );
        return jsObj.ui.getMarginBox( dNode, dNodeLayoutInfo, parentLayoutInfo, jsObj );
    },

    _forceRefreshZIndex: function()
    {   // attempts to force a refresh with a zIndex change
        var jsObj = jetspeed;
        var zTop = this._setAsTopZIndex( jsObj.page, jsObj.css, this.dNodeCss, this.posStatic );
        this.domNode.style.zIndex = zTop;
    },
    _forceRefreshZIndexAndForget: function()
    {   // attempts to force a refresh with a zIndex change - does not record new zIndex value in dNodeCss
        var zTop = jetspeed.page.getPWinTopZIndex( this.posStatic );
        this.domNode.style.zIndex = String( zTop );
    },

    getIFramesAndObjects: function( includeSize, excludeObjects )
    {
        var cNode = this.containerNode;
        var result = {};
        var notNull = false;
        if ( ! excludeObjects )
        {
            var objElmts = cNode.getElementsByTagName( "object" );
            if ( objElmts && objElmts.length > 0 )
            {
                result.objects = objElmts;
                notNull = true;
            }
        }
        var ifrms = cNode.getElementsByTagName( "iframe" );
        if ( ifrms && ifrms.length > 0 )
        {
            result.iframes = ifrms;
            if ( ! includeSize ) return result;
            notNull = true;
            var ifrmsSize = [];
            for ( var i = 0 ; i < ifrms.length ; i++ )
            {
                var ifrm = ifrms[i];

                var w = new Number( String( ifrm.width ) );
                w = ( isNaN( w ) ? null : String( ifrm.width ) );

                var h = new Number( String( ifrm.height ) );
                h = ( isNaN( h ) ? null : String( ifrm.height ) );

                ifrmsSize.push( { w: w, h: h } );
            }
            result.iframesSize = ifrmsSize;
        }
        if ( ! notNull )
            return null;
        return result;
    },

    contentChanged: function( evt )
    {   // currently used for dojo-debug window only
        if ( this.inContentChgd == false )
        {
            this.inContentChgd = true;
            if ( this.heightToFit )
            {
                this.makeHeightToFit( true );
            }
            this.inContentChgd = false;
        }
    },
 
    closeWindow: function()
    {
        var jsObj = jetspeed;
        var jsUI = jsObj.ui;
        var jsPage = jsObj.page;
        var djObj = dojo;
        var djEvtObj = djObj.event;
        var wDC = this.decConfig;

        if ( this.iframesInfo )
            jsPage.unregPWinIFrameCover( this );

        this._setupTitlebar( null, wDC, this.portlet, jsObj.docBody, document, jsObj, jsObj.id, jsObj.prefs, jsUI, jsPage, djObj );

        if ( this.drag )
        {
            this.drag.destroy( djObj, djEvtObj, jsObj, jsUI );
            this.drag = null;
        }
    
        if ( this.resizeHandle )
        {
            this.resizeHandle.destroy( djEvtObj, jsObj, jsUI );
            this.resizeHandle = null;
        }

        this._destroyChildWidgets( djObj );

        this._removeUntiledEvents();

        // BOZO:WIDGET: destroy script content

        var dNode = this.domNode;
        if ( dNode && dNode.parentNode )
            dNode.parentNode.removeChild( dNode );

        this.domNode = null;
        this.containerNode = null;
        this.tbNode = null;
        this.rbNode = null;
    },  // closeWindow

    _destroyChildWidgets: function( djObj )
    {
        if ( this.childWidgets )
        {
            var childWidgets = this.childWidgets;
            var childWidgetsLen = childWidgets.length, childWidget, swT, swI;
            djObj.debug( "PortletWindow [" + this.widgetId + "] destroy child widgets (" + childWidgetsLen + ")" );

            for ( var i = (childWidgetsLen -1) ; i >= 0 ; i-- )
            {
                try
                {
                    childWidget = childWidgets[i];
                    if ( childWidget )
                    {
                        swT = childWidget.widgetType;
                        swI = childWidget.widgetId;
                        childWidget.destroy();
                        djObj.debug( "destroyed child widget[" + i + "]: " + swT + " " + swI ) ;
                    }
                    childWidgets[i] = null;
		        }
                catch(e){ }
            }
            this.childWidgets = null;
        }
    },
    
    getPageColumnIndex: function()
    {
        return jetspeed.page.getColIndexForNode( this.domNode );
    },
    endSizing: function(e)
    {
        jetspeed.ui.evtDisconnect( "after", this.resizeHandle, "_endSizing", this, "endSizing" );
        this.windowIsSizing = false;
        if ( this.portlet && this.windowState != jetspeed.id.ACT_MAXIMIZE )
            this.portlet.submitWinState();
    },
    endDragging: function( posObj, changeToUntiled, changeToTiled )
    {
        var jsObj = jetspeed;
        var ie6 = this.ie6;
        if ( changeToUntiled )
            this.posStatic = false;
        else if ( changeToTiled )
            this.posStatic = true;
        var posStatic = this.posStatic;
        if ( ! posStatic )
        {
            var dimsCurrent = this.getDimsObj( posStatic );
            if ( posObj && posObj.left != null && posObj.top != null )
            {
                dimsCurrent.l = posObj.left; 
                dimsCurrent.t = posObj.top;
                if ( ! changeToUntiled )
                    this._alterCss( false, false, false, true, false, true );
            }
            if ( changeToUntiled )
            {
                this._updtDimsObj( false, false, true );
                this._alterCss( true, true, false, true );
                this._addUntiledEvents();
            }
        }
        else
        {
            if ( changeToTiled )
            {
                this._setAsTopZIndex( jsObj.page, jsObj.css, this.dNodeCss, posStatic );
                this._updtDimsObj( false, false );
            }
            if ( ! ie6)
            {
                this._alterCss( true );
                this.resizeNotifyChildWidgets();
            }
            else
            {
                this._resetIE6TiledSize( changeToTiled );
            }
        }
        if ( this.portlet && this.windowState != jsObj.id.ACT_MAXIMIZE )
            this.portlet.submitWinState();
        if ( ie6 )
            dojo.lang.setTimeout( this, this._IEPostDrag, jsObj.widget.ie6PostDragAddDelay );
    },

    getCurWinState: function( volatileOnly )
    {
        var dNode = this.domNode;
        var posStatic = this.posStatic;
        if ( ! dNode ) return null;
        var dNodeStyle = dNode.style;
        var cWinState = {};
        if ( ! posStatic )
            cWinState.zIndex = dNodeStyle.zIndex;
        if ( volatileOnly )
            return cWinState;

        var dimsCurrent = this.getDimsObj( posStatic );
        cWinState.width = (dimsCurrent.w ? String( dimsCurrent.w ) : "");
        cWinState.height = (dimsCurrent.h ? String( dimsCurrent.h ) : "");

        cWinState[ jetspeed.id.PP_WINDOW_POSITION_STATIC ] = posStatic;
        cWinState[ jetspeed.id.PP_WINDOW_HEIGHT_TO_FIT ] = this.heightToFit;

        if ( ! posStatic )
        {
            cWinState.left = (dimsCurrent.l != null ? String( dimsCurrent.l ) : "");
            cWinState.top = (dimsCurrent.t != null ? String( dimsCurrent.t ) : "");
        }
        else
        {
            var columnRowResult = jetspeed.page.getPortletCurColRow( dNode );
            if ( columnRowResult != null )
            {
                //dojo.hostenv.println( "move-window[" + this.widgetId + "] col=" + columnRowResult.column + " row=" + columnRowResult.row + " layout=" + columnRowResult.layout );
                cWinState.column = columnRowResult.column;
                cWinState.row = columnRowResult.row;
                cWinState.layout = columnRowResult.layout;
            }
            else
            {
                throw new Error( "Can't find row/col/layout for window: " + this.widgetId );
                // BOZO:NOW: test this with maximize/minimize
            }
        }
        //dojo.debug( "getCurWinState: {" + jetspeed.printobj( cWinState ) + "} - {" + jetspeed.printobj( dimsCurrent ) + "}" );
        return cWinState;
    },
    getCurWinStateForPersist: function( volatileOnly )
    {
        var currentState = this.getCurWinState( volatileOnly );
        // get rid of units text
        this._mkNumProp( null, currentState, "left" );
        this._mkNumProp( null, currentState, "top" );
        this._mkNumProp( null, currentState, "width" );
        this._mkNumProp( null, currentState, "height" );
        return currentState;
    },
    _mkNumProp: function( propVal, propsObj, propName )
    {
        var setPropVal = ( propsObj != null && propName != null );
        if ( propVal == null && setPropVal )
            propVal = propsObj[ propName ];
        if ( propVal == null || propVal.length == 0 )
            propVal = 0;
        else
        {
            var sourceNum = "";
            for ( var i = 0 ; i < propVal.length ; i++ )
            {
                var sourceCh = propVal.charAt(i);
                if ( ( sourceCh >= "0" && sourceCh <= "9" ) || sourceCh == "." )
                    sourceNum += sourceCh.toString();
            }
            if ( sourceNum == null || sourceNum.length == 0 )
                sourceNum = "0";
            if ( setPropVal )
                propsObj[ propName ] = sourceNum;
            propVal = new Number( sourceNum );
        }
        return propVal;
    },

    /* IMPORTANT setContent notes:
          We are avoiding a call to ContentPane.splitAndFixPaths for these reasons:
              - it does more than we need (wasting time)
              - we want to use its script processing but we don't need executeScripts to be set to false

          So we have copied the script processing code from ContentPane.splitAndFixPaths (0.4.0), and we call our copy here.

          We set executeScripts=false to delay script execution until after call to dojo.widget.getParser().createSubComponents
              - this allows dojo.addOnLoad to work normally
              - we call ContentPane._executeScripts after calling ContentPane.setContent (which calls createSubComponents)

          We set scriptSeparation=false to opt-out of support for scoping scripts to ContentPane widget instance
              - this feature, while cool, requires script modification in most cases (e.g. if one of your scripts calls another)
              
          Although we don't call ContentPane.splitAndFixPaths, it is notable that adjustPaths=false is likely correct for portlet content
              - e.g. when set to true, security-permissions.css is still fetched but not used (not affected by scriptSeparation)

          A better use of ContentPane features, particularly, scriptSeparation=true, can be accomplished as follows:
              - code: 
                      // this.executeScripts = true;
                      // this.scriptSeparation = true;
                      // this.adjustPaths = false;
                      // this.setContent( initialHtmlStr );

              - this requires script content to follow the conventions shown in: security/permissions/view-dojo-scriptScope.vm,
                which works in both portal and desktop, should allow (at least with scripts name collisions), coexistence
                of multiple instances of same portlet (with permissions there would be id collisions among widgets)
    */

    setPortletContent: function( html, url )
    {
        var jsObj = jetspeed;
        var djObj = dojo;
        var ie6 = this.ie6;
        var iNodeCss = null;
        var cNode = this.containerNode;
        if ( ie6 )
        {
            iNodeCss = this.iNodeCss;
            if ( this.heightToFit )
            {   // blank out height while loading new content - so that it can be recalculated based on actual content height
                this.iNodeCss = null;   // if not null, _alterCss will not set content relative height
                this._alterCss( false, true );
            }
        }

        var initialHtmlStr = html.toString();
        
        if ( ! this.exclPContent )
        {
            initialHtmlStr = '<div class="PContent" >' + initialHtmlStr + '</div>';
        }

        var setContentObj = this._splitAndFixPaths_scriptsonly( initialHtmlStr, url, jsObj );
        var doc = cNode.ownerDocument;


        var childWidgets = this.setContent( setContentObj, doc, djObj );
        this.childWidgets = ( ( childWidgets && childWidgets.length > 0 ) ? childWidgets : null );

        var deferCompletion = false;
        if ( setContentObj.scripts != null && setContentObj.scripts.length != null && setContentObj.scripts.length > 0 )
        {
            //djObj.debug( "Executing " + setContentObj.scripts.length + " script/s for " + this.widgetId + "  dojo.hostenv.post_load_=" + dojo.hostenv.post_load_ ) ;
            jsObj.page.win_onload = false;
            this._executeScripts( setContentObj.scripts, djObj );
            this.onLoad();
            if ( jsObj.page.win_onload && ( typeof setTimeout == "object" ) )
            {   // some kind of detector for lame browsers used by dojo.addOnLoad to decide that addOnLoad fncs will be called via setTimeout
                deferCompletion = true;
            }
        }
        if ( deferCompletion )
        {
            djObj.lang.setTimeout( this, this._setPortletContentScriptsDone, 20, jsObj, djObj, iNodeCss );
        }
        else
        {
            this._setPortletContentScriptsDone( jsObj, djObj, iNodeCss );
        }
    },

    _setPortletContentScriptsDone: function( jsObj, djObj, iNodeCss, deferred )
    {
        jsObj = ( jsObj != null ? jsObj : jetspeed );
        djObj = ( djObj != null ? djObj : dojo );
        var cNode = this.containerNode;
        var doc = cNode.ownerDocument;
        var ie6 = this.ie6;
    
        //if ( jsObj.debug.setPortletContent )
        //    djObj.debug( "setPortletContent [" + ( this.portlet ? this.portlet.entityId : this.widgetId ) + "]" );
        
        if ( this.portlet )   // BOZO: ( 2007-12-17 ) should this happen before the scripts are executed? seems more appropriate
            this.portlet.postParseAnnotateHtml( cNode );
        
        
        var iframesInfoCur = this.iframesInfo;
        var iframesInfoNew = this.getIFramesAndObjects( true, false );
        var setIFrame100P = null, iframeLayoutChg = false;
        if ( iframesInfoNew != null )
        {
            if ( iframesInfoCur == null )
            {
                this.iframesInfo = iframesInfoCur = { layout: false };
                var iframeCoverDiv = doc.createElement( "div" );
                var coverCl = "portletWindowIFrameCover";
                iframeCoverDiv.className = coverCl;
                cNode.appendChild( iframeCoverDiv );
                if ( jsObj.UAie )
                {   // transparent background node will not block events to iframe in ie6 or ie7 - extra style class sets opacity filter
                    iframeCoverDiv.className = (coverCl + "IE") + " " + coverCl;
                    if ( ie6 )   // as usual, in ie6 we need to set the width and height of this absolute positioned node
                        iframesInfoCur.iframeCoverIE6Css = jsObj.css.cssWidthHeight.concat();
                }
                iframesInfoCur.iframeCover = iframeCoverDiv;
                jsObj.page.regPWinIFrameCover( this );
            }
            var iframesSize = iframesInfoCur.iframesSize = iframesInfoNew.iframesSize;
            var iframes = iframesInfoNew.iframes;
            var iframesCurLayout = iframesInfoCur.layout;
            var iframesLayout = iframesInfoCur.layout = ( iframes && iframes.length == 1 && iframesSize[0].h != null );
            if ( iframesCurLayout != iframesLayout )
                iframeLayoutChg = true;
            if ( iframesLayout )
            {
                if ( ! this.heightToFit )
                    setIFrame100P = iframes[0];

                var wDC = this.decConfig;
                var cNode = this.containerNode;
                cNode.firstChild.className = "PContent portletIFramePContent";
                cNode.className = wDC.cNodeClass + " " + this.iframeCoverContainerClass;
                if ( ! wDC.layoutIFrame )
                {
                    this._createLayoutInfo( wDC, true, this.domNode, cNode, this.tbNode, this.rbNode, djObj, jsObj, jsObj.ui );
                }
            }
            
            var swfInfo = null;
            var objNodes = iframesInfoNew.objects;
            if ( objNodes )
            {
                var allSwfInfo = jsObj.page.swfInfo;
                if ( allSwfInfo )
                {
                    for ( var i = 0; i < objNodes.length ; i++ )
                    {
                        var objNode = objNodes[i];
                        var objNodeId = objNode.id;
                        if ( objNodeId )
                        {
                            var swfI = allSwfInfo[ objNodeId ];
                            if ( swfI )
                            {
                                if ( swfInfo == null ) swfInfo = {};
                                swfInfo[ objNodeId ] = swfI;
                            }
                        }
                    }
                }
            }
            if ( swfInfo )
                iframesInfoCur.swfInfo = swfInfo;
            else
                delete iframesInfoCur.swfInfo;
        }
        else if ( iframesInfoCur != null )
        {
            if ( iframesInfoCur.layout )
            {
                this.containerNode.className = this.decConfig.cNodeClass;
                iframeLayoutChg = true;
            }
            this.iframesInfo = null;
            jsObj.page.unregPWinIFrameCover( this );
        }
        if ( iframeLayoutChg )
        {
            this._alterCss( false, false, true );   // resize
        }

        if ( this.restoreOnNextRender )
        {
            this.restoreOnNextRender = false;
            this.restoreWindow();
        }

        if ( ie6 )
        {   // now that we have content, we finally can know the height of heightToFit windows
            this._updtDimsObj( false, false, true, false, true );  // force update width and height
            if ( iNodeCss == null )
            {
                var jsCss = jsObj.css;
                iNodeCss = jsCss.cssHeight.concat();
                iNodeCss[ jsCss.cssDis ] = "inline";
            }
            this.iNodeCss = iNodeCss;
            this._alterCss( false, false, true );   // resize
        }

        if ( this.minimizeOnNextRender )
        {
            this.minimizeOnNextRender = false;
            this.minimizeWindow( true );
            this.actionBtnSync( jsObj, jsObj.id );
            this.needsRenderOnRestore = true;
        }

        if ( setIFrame100P )
        {
            this._deferSetIFrameH( setIFrame100P, ( ! jsObj.UAie ? "100%" : "99%" ), true );
        }
    },  // setPortletContentScriptsDone

    _setContentObjects: function()
    {
        delete this._objectsInfo;
    },

    setContent: function(data, doc, djObj)
    {   // summary:
        //      Replaces old content with data content, include style classes from old content
        //  data String||DomNode:   new content, be it Document fragment or a DomNode chain
        //          If data contains style tags, link rel=stylesheet it inserts those styles into DOM
        var components = null;
        var step = 1;
        try
        {
            if(this._callOnUnload){ this.onUnload(); }   // this tells a remote script clean up after itself
            this._callOnUnload = true;
        
            step = 2;
            this._setContent(data.xml, djObj);
        
            step = 3;
            if ( this.parseContent )
            {
                var node = this.containerNode;
                var parser = new djObj.xml.Parse();
                var frag = parser.parseElement(node, null, true);
                // createSubComponents not createComponents because frag has already been created
                components = djObj.widget.getParser().createSubComponents( frag, null );
            }

            //this.onLoad();   // BOZO:WIDGET: why is this disabled?
        }
        catch(e)
        {
            dojo.hostenv.println( "ERROR in PortletWindow [" + this.widgetId + "] setContent while " + ( step == 1 ? "running onUnload" : ( step ==2 ? "setting innerHTML" : "creating dojo widgets" ) ) + " - " + jetspeed.formatError( e ) );
        }
        return components;
    },
    _setContent: function( cont, djObj )
    {
        this._destroyChildWidgets( djObj );
        try
        {
            var node = this.containerNode;
            while(node.firstChild)
            {
                djObj.html.destroyNode(node.firstChild);
            }
            node.innerHTML = cont;
        }
        catch(e)
        {
            e.text = "Couldn't load content:"+e.description;
            this._handleDefaults(e, "onContentError");
        }
    },

    _splitAndFixPaths_scriptsonly: function( /* String */ s, /* String */ url, jsObj )
    {
        var forcingExecuteScripts = true;
        var match, attr;
        var scripts = [] ;
        // deal with embedded script tags 
        // /=/=/=/=/=  begin  ContentPane.splitAndFixPaths   code  =/=/=/=/=/
        //   - only modification is: replacement of "this.executeScripts" with "forcingExecuteScripts"
        //
				var regex = /<script([^>]*)>([\s\S]*?)<\/script>/i;
				var regexSrc = /src=(['"]?)([^"']*)\1/i;
				//var regexDojoJs = /.*(\bdojo\b\.js(?:\.uncompressed\.js)?)$/;
				//var regexInvalid = /(?:var )?\bdjConfig\b(?:[\s]*=[\s]*\{[^}]+\}|\.[\w]*[\s]*=[\s]*[^;\n]*)?;?|dojo\.hostenv\.writeIncludes\(\s*\);?/g;
                //var regexDojoLoadUnload = /dojo\.(addOn(?:Un)?[lL]oad)/g;
				//var regexRequires = /dojo\.(?:(?:require(?:After)?(?:If)?)|(?:widget\.(?:manager\.)?registerWidgetPackage)|(?:(?:hostenv\.)?setModulePrefix|registerModulePath)|defineNamespace)\((['"]).*?\1\)\s*;?/;

                // " - trick emacs here after regex
				while(match = regex.exec(s)){
					if(forcingExecuteScripts && match[1]){
						if(attr = regexSrc.exec(match[1])){
							// remove a dojo.js or dojo.js.uncompressed.js from remoteScripts
							// we declare all files named dojo.js as bad, regardless of path
							//if(regexDojoJs.exec(attr[2])){
							//	dojo.debug("Security note! inhibit:"+attr[2]+" from  being loaded again.");
							//}else{
								scripts.push({path: attr[2]});
							//}
						}
					}
					if(match[2]){
						// remove all invalid variables etc like djConfig and dojo.hostenv.writeIncludes()
						var sc = match[2];//.replace(regexInvalid, "");
    						if(!sc){ continue; }
		
						// cut out all dojo.require (...) calls, if we have execute 
						// scripts false widgets dont get there require calls
						// takes out possible widgetpackage registration as well
						
                        //while(tmp = regexRequires.exec(sc)){
						//	requires.push(tmp[0]);
						//	sc = sc.substring(0, tmp.index) + sc.substr(tmp.index + tmp[0].length);
						//}
                        
                        //sc = sc.replace( regexDojoLoadUnload, "dojo.widget.byId('" + this.widgetId + "').$1" );

						if(forcingExecuteScripts){
							scripts.push(sc);
						}
					}
					s = s.substr(0, match.index) + s.substr(match.index + match[0].length);
				}


        // /=/=/=/=/=  end  ContentPane.splitAndFixPaths   code  =/=/=/=/=/

        //dojo.debug( "= = = = = =  annotated content for: " + ( url ? url : "unknown url" ) );
        //dojo.debug( initialHtmlStr );
        //if ( scripts.length > 0 )
        //{
        //    dojo.debug( "      = = =  script content for: " + ( url ? url : "unknown url" ) );
        //    for ( var i = 0 ; i < scripts.length; i++ )
        //        dojo.debug( "      =[" + (i+1) + "]:" + scripts[i] );
        //}
        //dojo.debug( "preParse  scripts: " + ( scripts ? scripts.length : "0" ) + " remoteScripts: " + ( remoteScripts ? remoteScripts.length : "0" ) );
        return {"xml": 		    s, // Object
				"styles":		[],
				"titles": 		[],
				"requires": 	[],
				"scripts": 		scripts,
				"url": 			url };
    },

    onLoad: function(e){
        // summary:
        //      Event hook, is called after everything is loaded and widgetified 
        this._runStack("_onLoadStack");
        this.isLoaded=true;
    },

    onUnload: function(e){
        // summary:
        //      Event hook, is called before old content is cleared
        this._runStack("_onUnloadStack");
        delete this.scriptScope;
    },

    _runStack: function(stName){
        var st = this[stName]; var err = "";
        var scope = this.scriptScope || window;
        for(var i = 0;i < st.length; i++){
            try{
                //alert( "exec onload " + i + " - " + this.widgetId ) ;
                st[i].call(scope);
            }catch(e){ 
                err += "\n"+st[i]+" failed: "+e.description;
            }
        }
        this[stName] = [];

        if(err.length){
            var name = (stName== "_onLoadStack") ? "addOnLoad" : "addOnUnLoad";
            this._handleDefaults(name+" failure\n "+err, "onExecError", "debug");
        }
    },

    _executeScripts: function( scripts, djObj )
    {
        var jsObj = jetspeed;
        var djHostEnv = djObj.hostenv;
        var jsPage = jsObj.page;
        var headNode = document.getElementsByTagName("head")[0];
        var tmp, uri, code = "";
        for( var i = 0; i < scripts.length; i++ )
        {
            if ( ! scripts[i].path )
            {
                tmp = this._fixScripts( scripts[i], true );
                if ( tmp )
                    code += ( (code.length > 0) ? ";" : "" ) + tmp;

                continue;
            }

            var uri = scripts[i].path;
            //if ( ! uri || djHostEnv.loadedUris[uri] ) continue;

            var contents = null;
            try
            {
                contents = djHostEnv.getText( uri, null, false );
    	        if ( contents )
                {
                    //djHostEnv.loadedUris[uri] = true;
                    contents = this._fixScripts( contents, false );
                    code += ( (code.length > 0) ? ";" : "" ) + contents;
                }
            }
            catch ( ex )
            {
                djObj.debug( "Error loading script for portlet [" + this.widgetId + "] url=" + uri + " - " + jsObj.formatError( ex ) );
            }

            try
            {
                if ( contents && ! jsObj.containsElement( "script", "src", uri, headNode ) )
                    jsObj.addDummyScriptToHead( uri );
            }
            catch ( ex )
            {
                djObj.debug( "Error added fake script element to head for portlet [" + this.widgetId + "] url=" + uri + " - " + jsObj.formatError( ex ) );
            }
        }
        
        try
        {
            // exec in global, lose the _container_ feature
            var djg = djObj.global();
            if ( djg.execScript )
            {
                djg.execScript(code);
            }
            else
            {
                //djObj.debug( "a d d i n g   p o r t l e t   s c r i p t :" );
                //djObj.hostenv.println( code );
                var djd = djObj.doc();
                var sc = djd.createElement("script");
                sc.appendChild(djd.createTextNode(code));
                (this.containerNode||this.domNode).appendChild(sc);
            }
        }
        catch (e)
        {
            var errorMsg = "Error running scripts for portlet [" + this.widgetId + "] - " + jsObj.formatError( e );
            e.text = errorMsg;
            djObj.hostenv.println( errorMsg );
            djObj.hostenv.println( code );
            //this._handleDefaults(e, "onExecError", "debug");
        }
    },

    _fixScripts: function( /* String */ script, inline )
    {
        var addEventRegex = /\b([a-z_A-Z$]\w*)\s*\.\s*(addEventListener|attachEvent)\s*\(/
        var match, nodeRef, methodNm;
        while ( match = addEventRegex.exec( script ) )
        {
            nodeRef = match[1];
            methodNm = match[2];
            script = script.substr(0, match.index) + "jetspeed.postload_" + methodNm + "(" + nodeRef + "," + script.substr(match.index + match[0].length);
        }
        var docWriteRegex = /\b(document\s*.\s*write(ln)?)\s*\(/
        while ( match = docWriteRegex.exec( script ) )
        {
            script = script.substr(0, match.index) + "jetspeed.postload_docwrite(" + script.substr(match.index + match[0].length);
        }
        var locationRegex = /(;\s|\s+)([a-z_A-Z$][\w.]*)\s*\.\s*(URL\s*|(location\s*(\.\s*href\s*){0,1}))=\s*(("[^"]*"|'[^']*'|[^;])[^;]*)/
        // " - trick emacs here after regex    
        while ( match = locationRegex.exec( script ) )
        {
            var memberExpr = match[3];
            memberExpr = memberExpr.replace(/^\s+|\s+$/g, "");
            script = script.substr(0, match.index) + match[1] + "jetspeed.setdoclocation(" + match[2] + ', "' + memberExpr + '", (' + match[6] + '))' + script.substr(match.index + match[0].length);
        }

        if ( inline )
        {
            script = script.replace(/<!--|-->/g, "");
        }
        return script;
    },

    _cacheSetting: function(bindObj, useCache){
        var djLang = dojo.lang;
        for(var x in this.bindArgs){
            if(djLang.isUndefined(bindObj[x])){
                bindObj[x] = this.bindArgs[x];
            }
        }

        if(djLang.isUndefined(bindObj.useCache)){ bindObj.useCache = useCache; }
        if(djLang.isUndefined(bindObj.preventCache)){ bindObj.preventCache = !useCache; }
        if(djLang.isUndefined(bindObj.mimetype)){ bindObj.mimetype = "text/html"; }
        return bindObj;
    },

    _handleDefaults: function(e, handler, messType){
        var djObj = dojo;
        if(!handler){ handler = "onContentError"; }

        if(djObj.lang.isString(e)){ e = {text: e}; }

        if(!e.text){ e.text = e.toString(); }

        e.toString = function(){ return this.text; };

        if(typeof e.returnValue != "boolean"){
            e.returnValue = true; 
        }
        if(typeof e.preventDefault != "function"){
            e.preventDefault = function(){ this.returnValue = false; };
        }
        // call our handler
        this[handler](e);
        if(e.returnValue){
            switch(messType){
                case true: // fallthrough, old compat
                case "alert":
                    alert(e.toString()); break;
                case "debug":
                    djObj.debug(e.toString()); break;
                default:
                // makes sure scripts can clean up after themselves, before we setContent
                if(this._callOnUnload){ this.onUnload(); } 
                // makes sure we dont try to call onUnLoad again on this event,
                // ie onUnLoad before 'Loading...' but not before clearing 'Loading...'
                this._callOnUnload = false;

                // we might end up in a endless recursion here if domNode cant append content
                if(arguments.callee._loopStop){
                    djObj.debug(e.toString());
                }else{
                    arguments.callee._loopStop = true;
                    this._setContent(e.toString(), djObj);
                }
            }
        }
        arguments.callee._loopStop = false;
    },

    onExecError: function(/*Object*/e){
    },

    onContentError: function(/*Object*/e){
    },

    setPortletTitle: function( newPortletTitle )
    {
        if ( newPortletTitle )
            this.title = newPortletTitle;
        else
            this.title = "";
        //if ( this.portlet ) this.title = this.portlet.entityId;
        if ( this.windowInitialized && this.tbTextNode )
        {
            this.tbTextNode.innerHTML = this.title;
        }
    },
    getPortletTitle: function()
    {
        return this.title;
    },

    _IEPostDrag: function()
    {
        if ( ! this.posStatic ) return ;
        var colDomNode = this.domNode.parentNode;
        dojo.dom.insertAtIndex( jetspeed.widget.ie6ZappedContentHelper, colDomNode, 0 );
        dojo.lang.setTimeout( this, this._IERemoveHelper, jetspeed.widget.ie6PostDragRmDelay );
    },
    _IERemoveHelper: function()
    {
        dojo.dom.removeNode( jetspeed.widget.ie6ZappedContentHelper );
    }
});  // jetspeed.widget.PortletWindow

jetspeed.widget.showAllPortletWindows = function()
{
    var jsObj = jetspeed;
    var jsCss = jsObj.css;
    var disIdx = jsCss.cssDis, noSelNmIdx = jsCss.cssNoSelNm, noSelIdx = jsCss.cssNoSel, noSelEndIdx = jsCss.cssNoSelEnd;
    var allPWwidgets = jsObj.page.getPWins( false );
    var showPWwidget, dNodeCss;
    for ( var i = 0 ; i < allPWwidgets.length ; i++ )
    {
        showPWwidget = allPWwidgets[i] ;
        if ( showPWwidget )
        {
            dNodeCss = showPWwidget.dNodeCss;
            dNodeCss[ noSelNmIdx ] = "";
            dNodeCss[ noSelIdx ] = "";
            dNodeCss[ noSelEndIdx ] = "";
            dNodeCss[ disIdx ] = "block";
            showPWwidget.domNode.style.display = "block";
            showPWwidget.domNode.style.visibility = "visible";
        }
    }
};  // showAllPortletWindows

jetspeed.widget.hideAllPortletWindows = function( excludeWidgetIds )
{
    var jsObj = jetspeed;
    var jsCss = jsObj.css;
    var disIdx = jsCss.cssDis, noSelNmIdx = jsCss.cssNoSelNm, noSelIdx = jsCss.cssNoSel, noSelEndIdx = jsCss.cssNoSelEnd;
    var allPWwidgets = jsObj.page.getPWins( false );
    var hidePWwidget, thisPWwidget, dNodeCss;
    for ( var i = 0 ; i < allPWwidgets.length ; i++ )
    {
        thisPWwidget = allPWwidgets[i];
        hidePWwidget = true;
        if ( thisPWwidget && excludeWidgetIds && excludeWidgetIds.length > 0 )
        {
            for ( var exclI = 0 ; exclI < excludeWidgetIds.length ; exclI++ )
            {
                if ( thisPWwidget.widgetId == excludeWidgetIds[exclI] )
                {
                    hidePWwidget = false;
                    break;
                }
            }
        }
        if ( thisPWwidget )
        {
            dNodeCss = thisPWwidget.dNodeCss;
            dNodeCss[ noSelNmIdx ] = "";
            dNodeCss[ noSelIdx ] = "";
            dNodeCss[ noSelEndIdx ] = "";
            if ( hidePWwidget )
            {
                dNodeCss[ disIdx ] = "none";
                thisPWwidget.domNode.style.display = "none";
            }
            else
            {
                dNodeCss[ disIdx ] = "block";
                thisPWwidget.domNode.style.display = "block";
            }
            thisPWwidget.domNode.style.visibility = "visible";
        }
    }
};  // hideAllPortletWindows


jetspeed.widget.WinScroller = function()
{
    var jsObj = this.jsObj;
    this.UAmoz = jsObj.UAmoz;
    this.UAope = jsObj.UAope;
};
dojo.extend( jetspeed.widget.WinScroller, {
    jsObj: jetspeed,
    djObj: dojo,
    typeNm: "WinScroller",
    V_AS_T: 32,                 // dojo.dnd.V_TRIGGER_AUTOSCROLL
    V_AS_V: 16,                 // dojo.dnd.V_AUTOSCROLL_VALUE
    autoScroll: function( e )   // dojo.dnd.autoScroll
    {
        try{   // IE can choke on accessing event properties, apparently
            var w = window;
            var dy = 0;
            if( e.clientY < this.V_AS_T )
            {
                dy = -this.V_AS_V;
            }
            else
            {   // dojo.dnd.getViewport
                var vpHeight = null;
                if ( this.UAmoz )
                    vpHeight = w.innerHeight;
                else
                {
                    var doc = document, dd = doc.documentElement;
                    if ( ! this.UAope && w.innerWidth )
    		            vpHeight = w.innerHeight;
                    else if ( ! this.UAope && dd && dd.clientWidth )
                        vpHeight = dd.clientHeight;
                    else
                    {
                        var b = jetspeed.docBody;
                        if ( b.clientWidth )
                            vpHeight = b.clientHeight;
                    }
                }
    
                if( vpHeight != null && e.clientY > vpHeight - this.V_AS_T )
                {
    		        dy = this.V_AS_V;
                }
            }
            w.scrollBy( 0, dy );
        }catch(ex){
        }
    },
    _getErrMsg: function( ex, msg, wndORlayout, prevErrMsg )
    {
        return ( ( prevErrMsg != null ? (prevErrMsg + "; ") : "" ) + this.typeNm + " " + ( wndORlayout == null ? "<unknown>" : wndORlayout.widgetId ) + " " + msg + " (" + ex.toString() + ")" );
    }
});  // jetspeed.widget.WinScroller

jetspeed.widget.CreatePortletWindowResizeHandler = function( portletWindow, jsObj )
{
    var resizeHandler = new jetspeed.widget.PortletWindowResizeHandle( portletWindow, jsObj );
    var doc = document;
    var rDivHndl = doc.createElement( "div" );
    rDivHndl.className = resizeHandler.rhClass;
    var rDivHndlInner = doc.createElement( "div" );
    rDivHndl.appendChild( rDivHndlInner );
    portletWindow.rbNode.appendChild( rDivHndl );
    resizeHandler.domNode = rDivHndl;    
    resizeHandler.build();
    return resizeHandler;
};

jetspeed.widget.PortletWindowResizeHandle = function( portletWindow, jsObj )
{
    this.pWin = portletWindow;
    jsObj.widget.WinScroller.call(this);
};
dojo.inherits( jetspeed.widget.PortletWindowResizeHandle, jetspeed.widget.WinScroller );
dojo.extend( jetspeed.widget.PortletWindowResizeHandle, {
    typeNm: "Resize",
    rhClass: "portletWindowResizeHandle",
    build: function()
    {
        this.events = [ jetspeed.ui.evtConnect( "after", this.domNode, "onmousedown", this, "_beginSizing" ) ];
    },
    destroy: function( djEvtObj, jsObj, jsUI )
    {
        this._cleanUpLastEvt( djEvtObj, jsObj, jsUI );
        jsUI.evtDisconnectWObjAry( this.events, djEvtObj );
        this.events = this.pWin = null;
    },
    _cleanUpLastEvt: function( djEvtObj, jsObj, jsUI )
    {
        var errMsg = null;
        try
        {
            jsUI.evtDisconnectWObjAry( this.tempEvents, djEvtObj );
            this.tempEvents = null;
        }
        catch(ex)
        {
            errMsg = this._getErrMsg( ex, "event clean-up error", this.pWin, errMsg );
        }

        try
        {
            jsObj.page.displayAllPWinIFrameCovers( true );
        }
        catch(ex)
        {
            errMsg = this._getErrMsg( ex, "clean-up error", this.pWin, errMsg );
        }

        if ( errMsg != null )
            dojo.raise( errMsg );
    },
	_beginSizing: function(e){
		if ( this._isSizing ) { return false; }
        var pWin = this.pWin;
        var node = pWin.domNode;
		if ( ! node ) { return false; }
        this.targetDomNode = node;

        var jsObj = jetspeed;
        var jsUI = jsObj.ui;
        var djObj = dojo;
        var djEvtObj = djObj.event;
        var docBody = jsObj.docBody;

        if ( this.tempEvents != null )
            this._cleanUpLastEvt( djEvtObj, jsObj, jsUI );
		
		this._isSizing = true;

		this.startPoint = { x: e.pageX, y: e.pageY };
		var mb = djObj.html.getMarginBox( node );
		this.startSize = { w: mb.width, h: mb.height };

        var d = node.ownerDocument;
        var resizeTempEvts = [];
        resizeTempEvts.push( jsUI.evtConnect( "after", docBody, "onmousemove", this, "_changeSizing", djEvtObj, 25 ) );
        resizeTempEvts.push( jsUI.evtConnect( "after", docBody, "onmouseup", this, "_endSizing", djEvtObj ) );
        // cancel text selection and text dragging
        resizeTempEvts.push( jsUI.evtConnect( "after", d, "ondragstart", jsObj, "_stopEvent", djEvtObj ) );
        resizeTempEvts.push( jsUI.evtConnect( "after", d, "onselectstart", jsObj, "_stopEvent", djEvtObj ) );

        jsObj.page.displayAllPWinIFrameCovers( false );

        this.tempEvents = resizeTempEvts;
        
        try
        {        
            e.preventDefault();
        }
        catch(ex){
        }
	},
    _changeSizing: function(e)
    {
        var pWin = this.pWin;
        if ( pWin.heightToFit )
        {
            pWin.makeHeightVariable( true, true );
        }

        // On IE, if you move the mouse above/to the left of the object being resized,
		// sometimes pageX/Y aren't set, apparently.  Just ignore the event.
		try{
			if(!e.pageX  || !e.pageY){ return; }
		}catch(ex){
			// sometimes you get an exception accessing above fields...
			return;
		}

        this.autoScroll( e );

		var dx = this.startPoint.x - e.pageX;
		var dy = this.startPoint.y - e.pageY;

		var newW = this.startSize.w - dx;
		var newH = this.startSize.h - dy;

        var posStatic = pWin.posStatic;
        if ( posStatic )
        {
            newW = this.startSize.w;
        }

		// minimum size check
		if (this.minSize) {
			var mb = dojo.html.getMarginBox( this.targetDomNode );
			if (newW < this.minSize.w) {
				newW = mb.width;
			}
			if (newH < this.minSize.h) {
				newH = mb.height;
			}
		}

        //dojo.debug( "rsh._changeSizing -  w=" + newW + "  h=" + newH + "  tbNode.width=" + pWin.tbNode.style.width );

		pWin.resizeTo( newW, newH );

        try
        {        
            e.preventDefault();
        }
        catch(ex){
        }
	},
	_endSizing: function(e)
    {
        var jsObj = jetspeed;
        var djObj = dojo;
        this._cleanUpLastEvt( djObj.event, jsObj, jsObj.ui );

        this.pWin.actionBtnSyncDefer( true, jsObj, djObj );

		this._isSizing = false;
	}
});  // jetspeed.widget.PortletWindowResizeHandle

jetspeed.widget.ie6PostDragAddDelay = 60; jetspeed.widget.ie6PostDragRmDelay = 120;

jetspeed.widget.BackgroundIframe = function( node, iframeClass, djObj ) {
    //  see dojo.html.BackgroundIframe
    if ( ! iframeClass )
        iframeClass = this.defaultStyleClass;
    var html="<iframe src='' frameborder='0' scrolling='no' class='" + iframeClass + "'>";
    this.iframe = djObj.doc().createElement(html);
    this.iframe.tabIndex = -1; // Magic to prevent iframe from getting focus on tab keypress - as style didnt work.
    node.appendChild(this.iframe);
}
dojo.lang.extend(jetspeed.widget.BackgroundIframe, {
    defaultStyleClass: "ie6BackgroundIFrame",
	iframe: null
});

if ( ! dojo.dnd )
    dojo.dnd = {};

dojo.dnd.Mover = function(windowOrLayoutWidget, dragNode, dragLayoutColumn, cL_NA_ED, moveableObj, e, notifyOnAbsolute, djObj, jsObj){
	// summary: an object, which makes a node follow the mouse, 
	//	used as a default mover, and as a base class for custom movers
	// node: Node: a node (or node's id) to be moved
	// e: Event: a mouse event, which started the move;
	//	only pageX and pageY properties are used
    var jsUI = jsObj.ui;
    var djEvtObj = djObj.event;

    jsObj.widget.WinScroller.call(this);

    if ( jsObj.widget._movingInProgress )
    {
        if ( djConfig.isDebug )
            jsObj.debugAlert( "ERROR - Mover initiation before previous Mover was destroyed" );
    }
    jsObj.widget._movingInProgress = true;
    this.moveInitiated = false;
    this.moveableObj = moveableObj;
    this.windowOrLayoutWidget = windowOrLayoutWidget;
	this.node = dragNode;
    this.dragLayoutColumn = dragLayoutColumn;
    this.cL_NA_ED = cL_NA_ED;
    this.posStatic = windowOrLayoutWidget.posStatic;
    this.notifyOnAbsolute = notifyOnAbsolute;
    if ( e.ctrlKey && windowOrLayoutWidget.moveAllowTilingChg )
    {
        if ( this.posStatic )
            this.changeToUntiled = true ;
        else if ( jsObj.prefs.windowTiling )
        {
            this.changeToTiled = true ;
            this.changeToTiledStarted = false;
        }
    }
    this.posRecord = {};
    this.disqualifiedColumnIndexes = {};
    if ( dragLayoutColumn != null )
    {
        this.disqualifiedColumnIndexes = dragLayoutColumn.col.getDescendantCols();
        
    }

    this.marginBox = {l: e.pageX, t: e.pageY};

	var doc = this.node.ownerDocument;
    var moverEvts = [];
    var firstEvt = jsUI.evtConnect( "after", doc, "onmousemove", this, "onFirstMove", djEvtObj );
    moverEvts.push( jsUI.evtConnect( "after", doc, "onmousemove", this, "onMouseMove", djEvtObj ) );
    moverEvts.push( jsUI.evtConnect( "after", doc, "onmouseup", this, "mouseUpDestroy", djEvtObj ) );

    // cancel text selection and text dragging
    moverEvts.push( jsUI.evtConnect( "after", doc, "ondragstart", jsObj, "_stopEvent", djEvtObj ) );
    moverEvts.push( jsUI.evtConnect( "after", doc, "onselectstart", jsObj, "_stopEvent", djEvtObj ) );
    if ( jsObj.UAie6 )
    {
        moverEvts.push( jsUI.evtConnect( "before", doc, "onmousedown", this, "mouseDownDestroy", djEvtObj ) );
        moverEvts.push( jsUI.evtConnect( "before", moveableObj.handle, "onmouseup", moveableObj, "onMouseUp", djEvtObj ) );
    }

    jsObj.page.displayAllPWinIFrameCovers( false );

    moverEvts.push( firstEvt );  // disconnected with pop() in onFirstMove
    this.events = moverEvts;

    this.pSLastColChgIdx = null;    // pS: posStatic
    this.pSLastColChgTime = null;
    this.pSLastNaturalColChgYTest = null;
    this.pSLastNaturalColChgHistory = null;
    this.pSLastNaturalColChgChoiceMap = null;
    
    this.isDebug = false;
    if ( jsObj.debug.dragWindow )
    {
        this.isDebug = true;
        this.devKeepLastMsg = null;
        this.devKeepLastCount = 0;
        this.devLastX = null; this.devLastY = null; this.devLastTime = null, this.devLastColI = null;
        this.devChgTh = 30;       // Th: Threshold
        this.devLrgTh = 200;
        this.devChgSubsqTh = 10;
        this.devTimeTh = 6000;
        this.devI = jsObj.debugindent; this.devIH = jsObj.debugindentH; this.devIT = jsObj.debugindentT; this.devI3 = jsObj.debugindent3; this.devICH = jsObj.debugindentch;
    }
};
dojo.inherits( dojo.dnd.Mover, jetspeed.widget.WinScroller );
dojo.extend(dojo.dnd.Mover, {
    typeNm: "Mover",
    pSColChgTimeTh: 3000,   // pS: posStatic; Th: Threshold
	// mouse event processors
	onMouseMove: function( e ){
		// summary: event processor for onmousemove
		// e: Event: mouse event
        var jsObj = this.jsObj;
        var djObj = this.djObj;
        var isMoz = this.UAmoz;
		this.autoScroll( e );
		var m = this.marginBox;
        var noMove = false;
        var x = m.l + e.pageX;
        var y = m.t + e.pageY;
        var debugEnabled = this.isDebug;
        var debugOn = false;
        var debugTime = null, debugExcl = null, indent, indentH, indent3, indentCH, indentT;

        if ( debugEnabled )
        {
            indent = this.devI; indentH = this.devIH; indent3 = this.devI3; indentCH = this.devICH, indentT = this.devIT;
            debugTime = (new Date().getTime());
            if ( this.devLastX == null || this.devLastY == null )
            {
                this.devLastX = x;
                this.devLastY = y;
            }
            else
            {
                var pastLgThreshold = ( Math.abs( x - this.devLastX ) > this.devLrgTh ) || ( Math.abs( y - this.devLastY ) > this.devLrgTh );
                if ( ! pastLgThreshold && this.devLastTime != null && ( (this.devLastTime + this.devTimeTh) > debugTime ) )
                {   // too soon
                }
                else
                {
                    if ( Math.abs( x - this.devLastX ) > this.devChgTh )
                    {
                        this.devLastX = x;
                        debugOn = true;
                    }
                    if ( Math.abs( y - this.devLastY ) > this.devChgTh )
                    {
                        this.devLastY = y;
                        debugOn = true;
                    }
                }
            }
        }

        if ( isMoz && this.firstEvtAdjustXY != null )
        {   // initial event pageX and pageY seem to be relative to container when window is static
            //m = this.firstEvtAdjustXY;
            x = x + this.firstEvtAdjustXY.l;
            y = y + this.firstEvtAdjustXY.t;
            this.firstEvtAdjustXY = null;
            noMove = true;
        }
        
        jsObj.ui.setMarginBox( this.node, x, y, null, null, this.nodeLayoutInfo, jsObj, djObj );
        
        var posRecord = this.posRecord;
        posRecord.left = x;
        posRecord.top = y;

        var changeToTiledStart = false;
        var posStatic = this.posStatic;
        if ( ! posStatic )
        {
            if ( ! noMove && this.changeToTiled && ! this.changeToTiledStarted )
            {
                changeToTiledStart = true;
                posStatic = true;  // will change this.posStatic after we start successfully
            }
        }
        if ( posStatic && ! noMove )
        {
            var colInfoArray = this.columnInfoArray;
            var colObjArray = jsObj.page.columns;
            var heightHalf = this.heightHalf;
            var noOfCols = colObjArray.length;
            var xTest = e.pageX;
            var yTest = y + heightHalf;
            var lastColIdx = this.pSLastColChgIdx;
            var lastColChoiceMap = this.pSLastNaturalColChgChoiceMap;
            var lowEntryI = null, candidates = [], lastColCurrentLowY = null;
            var colInfo, lowY1, lowY2, lowY3, lowY, candidateI, prevLowEntry, nextLowEntry, nextLowEntryI;
            for ( var i = 0 ; i < noOfCols ; i++ )
            {
                colInfo = colInfoArray[ i ];
                if ( colInfo != null )
                {
                    if ( xTest >= colInfo.left && xTest <= colInfo.right )
                    {
                        if ( yTest >= (colInfo.top - 30) || ( lastColChoiceMap != null && lastColChoiceMap[ i ] != null ) )
                        {
                            lowY1 = Math.min( Math.abs( yTest - ( colInfo.top ) ), Math.abs( e.pageY - ( colInfo.top ) ) );
                            lowY2 = Math.min( Math.abs( yTest - ( colInfo.yhalf ) ), Math.abs( e.pageY - ( colInfo.yhalf ) ) );
                            lowY3 = Math.min( Math.abs( yTest - colInfo.bottom ), Math.abs( e.pageY - colInfo.bottom ) );
                            lowY = Math.min( lowY1, lowY2 );
                            lowY = Math.min( lowY, lowY3 );
                            prevLowEntry = null;
                            nextLowEntryI = lowEntryI;
                            while ( nextLowEntryI != null )
                            {
                                nextLowEntry = candidates[nextLowEntryI];
                                if ( lowY < nextLowEntry.lowY )
                                {
                                    break;
                                }
                                else
                                {
                                    prevLowEntry = nextLowEntry;
                                    nextLowEntryI = nextLowEntry.nextIndex;
                                }
                            }
                            candidates.push( { index: i, lowY: lowY, nextIndex: nextLowEntryI, lowYAlign: ( (! debugEnabled) ? null : ( lowY == lowY1 ? "^" : ( lowY == lowY2 ? "~" : "_" ) ) ) } );
                            candidateI = (candidates.length -1);
                            if ( prevLowEntry != null )
                                prevLowEntry.nextIndex = candidateI;
                            else
                                lowEntryI = candidateI;

                            if ( i == lastColIdx )
                                lastColCurrentLowY = lowY;
                        }
                        else if ( debugEnabled )
                        {
                            if ( debugExcl == null ) debugExcl = [];
                            var offBy = (colInfo.top - 30) - yTest;
                            debugExcl.push( djObj.string.padRight( String(i), 2, indentCH ) + " y! " + djObj.string.padRight( String(offBy), 4, indentCH ) );
                        }
                    }
                    else if ( debugEnabled && xTest > colInfo.width )
                    {
                        if ( debugExcl == null ) debugExcl = [];
                        var offBy = xTest - colInfo.width;
                        debugExcl.push( djObj.string.padRight( String(i), 2, indentCH ) + " x! " + djObj.string.padRight( String(offBy), 4, indentCH ) );
                    }
                }
            }   // for ( var i = 0 ; i < noOfCols ; i++ )

            var colIndex = -1;
            var col2ndIndex = -1, col3rdIndex = -1;
            var col1stLowY = null, col2ndLowY = null, col3rdLowY = null, col2ndDiff = null, col3rdDiff = null;
            if ( lowEntryI != null )
            {
                nextLowEntry = candidates[ lowEntryI ];
                colIndex = nextLowEntry.index;
                col1stLowY = nextLowEntry.lowY;
                if ( nextLowEntry.nextIndex != null )
                {
                    nextLowEntry = candidates[ nextLowEntry.nextIndex ];
                    col2ndIndex = nextLowEntry.index;
                    col2ndLowY = nextLowEntry.lowY;
                    col2ndDiff = col2ndLowY - col1stLowY;
                    if ( nextLowEntry.nextIndex != null )
                    {
                        nextLowEntry = candidates[ nextLowEntry.nextIndex ];
                        col3rdIndex = nextLowEntry.index;
                        col3rdLowY = nextLowEntry.lowY;
                        col3rdDiff = col3rdLowY - col1stLowY;;
                    }
                }
            }

            var debugNewColMsg = null;
            var currentTime = (new Date().getTime());
            var lastNaturalColChgYTest = this.pSLastNaturalColChgYTest;
            if ( lastColCurrentLowY == null || ( lastNaturalColChgYTest != null && Math.abs( yTest - lastNaturalColChgYTest ) >= Math.max( ( heightHalf - Math.floor( heightHalf * 0.3 ) ), Math.min( heightHalf, 21 ) ) ) )
            {   // lastCol is excluded from candidates (reset) OR first column selection OR mouse is beyond threshold of last column selection
                if ( colIndex >= 0 )
                {
                    this.pSLastNaturalColChgYTest = yTest;
                    this.pSLastNaturalColChgHistory = [ colIndex ];
                    lastColChoiceMap = {};
                    lastColChoiceMap[ colIndex ] = true ;
                    this.pSLastNaturalColChgChoiceMap = lastColChoiceMap;
                }
            }
            else if ( lastNaturalColChgYTest == null )
            {
                this.pSLastNaturalColChgYTest = yTest;
                colIndex = lastColIdx;
                this.pSLastNaturalColChgHistory = [ colIndex ];
                lastColChoiceMap = {};
                lastColChoiceMap[ colIndex ] = true ;
                this.pSLastNaturalColChgChoiceMap = lastColChoiceMap;
            }
            else
            {
                var leastRecentColIndex = null;
                var expireTime = this.pSLastColChgTime + this.pSColChgTimeTh;
                if ( expireTime < currentTime )   // BOZO:NOW: a change in placement within current column should reset this timer
                {   // time has expired since the last change in column selection
                    // alternate through column candidates
                    // - choose the column that has either not yet been selected or the one that was selected furthest in the past
                    var lastChgHistory = this.pSLastNaturalColChgHistory;
                    var lastChgHistoryLen = ( lastChgHistory == null ? 0 : lastChgHistory.length );
                    var leastRecentSelectionIndex = null, currentHasBeenSelected;
                    
                    nextLowEntryI = lowEntryI;
                    while ( nextLowEntryI != null )
                    {
                        nextLowEntry = candidates[nextLowEntryI];
                        colI = nextLowEntry.index;
                        if ( lastChgHistoryLen == 0 )
                        {
                            leastRecentColIndex = colI;
                            break;
                        }
                        else
                        {
                            currentHasBeenSelected = false;
                            for ( var i = (lastChgHistoryLen -1) ; i >= 0 ; i-- )
                            {
                                if ( lastChgHistory[i] == colI )
                                {
                                    if ( leastRecentSelectionIndex == null || leastRecentSelectionIndex > i )
                                    {
                                        leastRecentSelectionIndex = i;
                                        leastRecentColIndex = colI;
                                    }
                                    currentHasBeenSelected = true;
                                    break;
                                }
                            }
                            if ( ! currentHasBeenSelected )
                            {
                                leastRecentColIndex = colI;
                                break;
                            }
                        }
                        nextLowEntryI = nextLowEntry.nextIndex;
                    }

                    if ( leastRecentColIndex != null )
                    {
                        colIndex = leastRecentColIndex;
                        lastColChoiceMap[ colIndex ] = true ;
                        if ( lastChgHistoryLen == 0 || lastChgHistory[ (lastChgHistoryLen -1) ] != colIndex )
                            lastChgHistory.push( colIndex );
                    }
                }
                else
                {
                    colIndex = lastColIdx;
                }
                if ( debugEnabled && leastRecentColIndex != null )
                {
                    djObj.hostenv.println( indent + "ColChg YTest=" + lastNaturalColChgYTest + " LeastRecentColI=" + leastRecentColIndex + " History=[" + ( this.pSLastNaturalColChgHistory ? this.pSLastNaturalColChgHistory.join( ", " ) : "" ) + "] Map={" + jsObj.printobj( this.pSLastNaturalColChgChoiceMap ) + "} expire=" + (currentTime - expireTime) + "}" );
                }
            }

            if ( debugEnabled && debugNewColMsg != null )
            {
                if ( this.devKeepLastMsg != null )
                {
                    djObj.hostenv.println( this.devKeepLastMsg );
                    this.devKeepLastMsg = null;
                    this.devKeepLastCount = 0;
                }
                djObj.hostenv.println( debugNewColMsg );
            }

            var col = ( colIndex >= 0 ? colObjArray[ colIndex ] : null );
            
            if ( debugEnabled )
            {
                if ( this.devLastColI != colIndex )
                    debugOn = true;
                this.devLastColI = colIndex;
            }
            
            var pwGhost = jsObj.widget.pwGhost;
            if ( changeToTiledStart )
            {
                if ( col != null )
                {
                    jsObj.ui.setMarginBox( pwGhost, null, null, null, m.h, this.nodeLayoutInfo, jsObj, djObj );
                    pwGhost.col = null;
                    this.changeToTiledStarted = true;
                    this.posStatic = true;
                }
            }

            var beforeNode = null, appended = false, appendedDefault = false;
            if ( pwGhost.col != col && col != null )
            {
                this.pSLastColChgTime = currentTime;
                this.pSLastColChgIdx = colIndex;
                var pwGhostLastCol = pwGhost.col;
                if ( pwGhostLastCol != null )
                    djObj.dom.removeNode( pwGhost );
				pwGhost.col = col;
                var newColInfo = colInfoArray[ colIndex ];
                var newColChildCount = newColInfo.childCount + 1;
                newColInfo.childCount = newColChildCount;
                if ( newColChildCount == 1 )
                    colObjArray[ colIndex ].domNode.style.height = "";
				col.domNode.appendChild( pwGhost );
                appendedDefault = true;
                var lastColInfo = ( lastColIdx != null ? ( (lastColIdx != colIndex) ? colInfoArray[ lastColIdx ] : null ) : ( pwGhostLastCol != null ? colInfoArray[ pwGhostLastCol.getPageColumnIndex() ] : null ) );
                if ( lastColInfo != null )
                {   // check if lastColIdx is now empty
                    var lastColChildCount = lastColInfo.childCount -1;
                    if ( lastColChildCount < 0 ) lastColChildCount = 0;
                    lastColInfo.childCount = lastColChildCount;
                    if ( lastColChildCount == 0 )
                        colObjArray[ lastColInfo.pageColIndex ].domNode.style.height = "1px";
                }
			}
            var pWinsAndColsResult = null, colPWinsAndCols = null;
            if ( col != null )
            {
                pWinsAndColsResult = jsObj.ui.getPWinAndColChildren( col.domNode, pwGhost, true, false, true, false );
                colPWinsAndCols = pWinsAndColsResult.matchingNodes;
            }
            if ( colPWinsAndCols != null && colPWinsAndCols.length > 1 )
            {
                var ghostIndex = pWinsAndColsResult.matchNodeIndexInMatchingNodes;
                var yAboveWindow = -1;
                var yBelowWindow = -1;
                if ( ghostIndex > 0 )
                {
                    var yAboveWindow = djObj.html.getAbsolutePosition( colPWinsAndCols[ ghostIndex -1 ], true ).y;
                    if ( (y - 25) <= yAboveWindow )
                    {
                        djObj.dom.removeNode( pwGhost );
                        beforeNode = colPWinsAndCols[ ghostIndex -1 ];
                        djObj.dom.insertBefore( pwGhost, beforeNode, true );
                    }
                }
                if ( ghostIndex != (colPWinsAndCols.length -1) )
                {
                    var yBelowWindow = djObj.html.getAbsolutePosition( colPWinsAndCols[ ghostIndex +1 ], true ).y;
                    if ( (y + 10) >= yBelowWindow )
                    {
                        if ( ghostIndex + 2 < colPWinsAndCols.length )
                        {
                            beforeNode = colPWinsAndCols[ ghostIndex +2 ];
                            djObj.dom.insertBefore( pwGhost, beforeNode, true );
                        }
                        else
                        {
                            col.domNode.appendChild( pwGhost );
                            appended = true;
                        }
                    }
                }
            }

            if ( debugOn )
            {
                //djObj.debug( "eX=" + e.pageX + " eY=" + e.pageY + " mB: " + jsObj.printobj( this.marginBox ) + " nB: " + jsObj.printobj( djObj.getMarginBox( this.node, null, jsObj ) ) );
                var placement = "";
                if ( beforeNode != null || appended || appendedDefault )
                {
                    placement = "put=";
                    if ( beforeNode != null )
                        placement += "before(" + beforeNode.id + ")";
                    else if ( appended )
                        placement += "end";
                    else if ( appendedDefault )
                        placement += "end-default";
                }
                djObj.hostenv.println( indent + "col=" + colIndex + indentH + placement + indentH + "x=" + x + indentH + "y=" + y + indentH + "ePGx=" + e.pageX + indentH + "ePGy=" + e.pageY + indentH + "yTest=" + yTest );
                var candMsg = "", colI, colInfo;
                nextLowEntryI = lowEntryI;
                while ( nextLowEntryI != null )
                {
                    nextLowEntry = candidates[nextLowEntryI];
                    colI = nextLowEntry.index;
                    colInfo = colInfoArray[ nextLowEntry.index ];
                    
                    candMsg += ( candMsg.length > 0 ? indentT : "" ) + colI + nextLowEntry.lowYAlign + ( colI < 10 ? indentCH : "" ) + " -> " + djObj.string.padRight( String(nextLowEntry.lowY), 4, indentCH );
                    nextLowEntryI = nextLowEntry.nextIndex;
                }
                djObj.hostenv.println( indent3 + candMsg );
                if ( debugExcl != null )
                {
                    var exclMsg = "";
                    for ( i = 0 ; i < debugExcl.length ; i++ )
                    {
                        exclMsg += ( i > 0 ? indentT : "" ) + debugExcl[i];
                    }
                    djObj.hostenv.println( indent3 + exclMsg );
                }
                this.devLastTime = debugTime;
                this.devChgTh = this.devChgSubsqTh;
            }

        }
	},   // Mover.onMouseMove

	onFirstMove: function(){
		// summary: makes the node absolute; it is meant to be called only once
        var jsObj = this.jsObj;
        var jsUI = jsObj.ui;
        var djObj = this.djObj;
        var wndORlayout = this.windowOrLayoutWidget;
        var node = this.node;
        var nodeLayoutInfo = wndORlayout._getLayoutInfoMoveable();
        this.nodeLayoutInfo = nodeLayoutInfo;
        var mP = wndORlayout._getWindowMarginBox( nodeLayoutInfo, jsObj );
        this.staticWidth = null;
        var pwGhost = jsObj.widget.pwGhost;
        var isMoz = this.UAmoz;
        var changeToUntiled = this.changeToUntiled;
        var changeToTiled = this.changeToTiled;
        var m = null;

        if ( this.posStatic )
        {
            if ( ! changeToUntiled )
            {
                var inColIndex = wndORlayout.getPageColumnIndex();
                var inCol = ( inColIndex >= 0 ? jsObj.page.columns[ inColIndex ] : null );
                pwGhost.col = inCol;
                this.pSLastColChgTime = new Date().getTime();
                this.pSLastColChgIdx = inColIndex;
            }

            m = { w: mP.w, h: mP.h };
            var colDomNode = node.parentNode;
            var jsDNode = document.getElementById( jsObj.id.DESKTOP );
            var nodeStyle = node.style;
            this.staticWidth = nodeStyle.width;
            var nodeAbsPos = djObj.html.getAbsolutePosition( node, true );
            //dojo.debug( "start node abs: " + jsObj.printobj( nodeAbsPos ) );
            var nodeMargExt = nodeLayoutInfo.mE;
            //dojo.debug( "start node me: " + jsObj.printobj( nodeMargExt ) );
            m.l = nodeAbsPos.left - nodeMargExt.l;    // calculate manually to avoid calling getMarginBox during node insertion (mozilla is too fast to update)
            m.t = nodeAbsPos.top - nodeMargExt.t;
            if ( isMoz )
            {   // set early to avoid fast reaction that causes below content to shift for a split second
                if ( ! changeToUntiled )
                    jsUI.setMarginBox( pwGhost, null, null, null, mP.h, nodeLayoutInfo, jsObj, djObj );
                this.firstEvtAdjustXY = { l: m.l, t: m.t };
            }
            nodeStyle.position = "absolute";
            if ( ! changeToUntiled )
                nodeStyle.zIndex = jsObj.page.getPWinHighZIndex() + 1;
            else
                nodeStyle.zIndex = ( wndORlayout._setAsTopZIndex( jsObj.page, jsObj.css, wndORlayout.dNodeCss, false ) );

            if ( ! changeToUntiled )
            {
                colDomNode.insertBefore( pwGhost, node );
                if ( ! isMoz )   // some browsers cannot set this until node is in document
                    jsUI.setMarginBox( pwGhost, null, null, null, mP.h, nodeLayoutInfo, jsObj, djObj );

                jsDNode.appendChild( node );

                var pWinsAndColsResult = jsUI.getPWinAndColChildren( colDomNode, pwGhost, true, false, true );
                this.prevColumnNode = colDomNode;
                this.prevIndexInCol = pWinsAndColsResult.matchNodeIndexInMatchingNodes;
            }
            else
            {
                wndORlayout._updtDimsObj( false, true );
                jsDNode.appendChild( node );
            }
        }
        else
        {
            m = mP;
        }
        this.moveInitiated = true;
        //djObj.debug( "initial  marginBox: " + jsObj.printobj( this.marginBox ) );
        m.l -= this.marginBox.l;
		m.t -= this.marginBox.t;
		this.marginBox = m;
        //djObj.debug( "adjusted marginBox: " + jsObj.printobj( this.marginBox ) );

        jsUI.evtDisconnectWObj( this.events.pop(), djObj.event );

        var dqCols = this.disqualifiedColumnIndexes;
        var debugEnabled = ( this.isDebug || jsObj.debug.dragWindowStart ), indentT;
        if ( debugEnabled )
        {
            indentT = jsObj.debugindentT;
            var indentH = jsObj.debugindentH;
            var dqColsStr = "";
            if ( dqCols != null )
                dqColsStr = indentH + "dqCols=[" + jsObj.objectKeys( dqCols ).join( ", " ) + "]";
            var title = wndORlayout.title;
            if ( title == null ) title = node.id;
            djObj.hostenv.println( 'DRAG "' + title + '"' + indentH + ( ( this.posStatic && ! changeToUntiled ) ? ( "col=" + ( pwGhost.col ? pwGhost.col.getPageColumnIndex() : "null" ) + indentH ) : "" ) + "m.l = " + m.l + indentH + "m.t = " + m.t + dqColsStr );
        }

        if ( this.posStatic || changeToTiled )
        {
            this.heightHalf = mP.h / 2;
            var dragLayoutColumn = this.dragLayoutColumn || {};
            var columnInfoArray = jsUI.updateChildColInfo( node, dqCols, dragLayoutColumn.maxdepth, this.cL_NA_ED, ( debugEnabled ? 1 : null ), indentT );
            if ( debugEnabled )
                djObj.hostenv.println( indentT + "--------------------" );
            this.columnInfoArray = columnInfoArray;
        }

        if ( this.posStatic )
        {
            jsUI.setMarginBox( node, m.l, m.t, mP.w, null, nodeLayoutInfo, jsObj, djObj );  // djObj.setMarginBox( node, m.l, m.t, mP.w, null, null, jsObj );
            //djObj.debug( "initial node set-dims  l=" + node.style.left + "  t=" + node.style.top + "  w=" + node.style.width + "  h=" + node.style.height );

            if ( this.notifyOnAbsolute )
            {   // BOZO:NOW: consider making width smaller during drag  // this.dragLayoutColumn
                wndORlayout.dragChangeToAbsolute( this, node, this.marginBox, djObj, jsObj );
                //this.heightHalf = mP.h / 2;
            }

            if ( changeToUntiled )
            {
                this.posStatic = false;
            }
            //djObj.debug( "initial position: " + jsObj.printobj( djObj.getMarginBox( node, null, jsObj ) ) );
        }
	},   // Mover.onFirstMove
    
    mouseDownDestroy: function( e )
    {
        var jsObj = this.jsObj;
        //jsObj.debugCache( "mover mouseDownDestroy [" + this.windowOrLayoutWidget.widgetId + "]" );
        jsObj.stopEvent( e );
        this.mouseUpDestroy();
    },
    mouseUpDestroy: function()
    {
        var djObj = this.djObj;
        var jsObj = this.jsObj;
        //jsObj.debugCache( "mover mouseUpDestroy [" + this.windowOrLayoutWidget.widgetId + "]" );
        this.destroy( djObj, djObj.event, jsObj, jsObj.ui );
    },
	destroy: function( djObj, djEvtObj, jsObj, jsUI ){
		// summary: stops the move, deletes all references, so the object can be garbage-collected
        var wndORlayout = this.windowOrLayoutWidget;
        //dojo.debug( "mover destroy [" + wndORlayout.widgetId + "]" );
        var node = this.node;
        var errMsg = null;
        if ( this.moveInitiated && wndORlayout && node )
        {
            this.moveInitiated = false;
            try
            {
                if ( this.posStatic )
                {
                    var pwGhost = jsObj.widget.pwGhost;
                    var nStyle = node.style;
                    if ( pwGhost && pwGhost.col )
                    {
                        wndORlayout.column = 0;
                        djObj.dom.insertBefore( node, pwGhost, true );
                        //dojo.debug( "moved into column: " + pwGhost.col.toString() );
                    }
                    else
                    {
                        if ( this.prevColumnNode != null && this.prevIndexInCol != null )
                            djObj.dom.insertAtIndex( node, this.prevColumnNode, this.prevIndexInCol );
                        else
                        {
                            var defaultColumn = jsObj.page.getColumnDefault();
                            if ( defaultColumn != null )
                                djObj.dom.prependChild( node, defaultColumn.domNode );
                        }
                    }
                    if ( pwGhost )
                        djObj.dom.removeNode( pwGhost );
                }
                wndORlayout.endDragging( this.posRecord, this.changeToUntiled, this.changeToTiled );
            }
            catch(ex)
            {
                errMsg = this._getErrMsg( ex, "destroy reset-window error", wndORlayout, errMsg );
            }
        }

        try
        {
            jsUI.evtDisconnectWObjAry( this.events, djEvtObj );
            if ( this.moveableObj != null )
                this.moveableObj.mover = null;
            this.events = this.node = this.windowOrLayoutWidget = this.moveableObj = this.prevColumnNode = this.prevIndexInCol = null;
        }
        catch(ex)
        {
            errMsg = this._getErrMsg( ex, "destroy event clean-up error", wndORlayout, errMsg );
            if ( this.moveableObj != null )
                this.moveableObj.mover = null;
        }

        try
        {
            jsObj.page.displayAllPWinIFrameCovers( true );
        }
        catch(ex)
        {
            errMsg = this._getErrMsg( ex, "destroy clean-up error", wndORlayout, errMsg );
        }
        jsObj.widget._movingInProgress = false;
        if ( errMsg != null )
            djObj.raise( errMsg );
	}   // Mover.destroy
});  // dojo.dnd.Mover

dojo.dnd.Moveable = function( windowOrLayoutWidget, opt ){
	// summary: an object, which makes a node moveable
	// node: Node: a node (or node's id) to be moved
	// opt: Object: an optional object with additional parameters;
	//	following parameters are recognized:
	//		handle: Node: a node (or node's id), which is used as a mouse handle
	//			if omitted, the node itself is used as a handle
	//		delay: Number: delay move by this number of pixels
	//		skip: Boolean: skip move of form elements
	//		mover: Object: a constructor of custom Mover
    var jsObj = jetspeed;
    var jsUI = jsObj.ui;
    var djObj = dojo;
    var djEvtObj = djObj.event;
    this.windowOrLayoutWidget = windowOrLayoutWidget;
	this.handle = opt.handle;
    var moveableEvts = [];
    moveableEvts.push( jsUI.evtConnect( "after", this.handle, "onmousedown", this, "onMouseDown", djEvtObj ) );

    // cancel text selection and text dragging
    moveableEvts.push( jsUI.evtConnect( "after", this.handle, "ondragstart", jsObj, "_stopEvent", djEvtObj ) );
    moveableEvts.push( jsUI.evtConnect( "after", this.handle, "onselectstart", jsObj, "_stopEvent", djEvtObj ) );
	this.events = moveableEvts;
};

dojo.extend(dojo.dnd.Moveable, {
    minMove: 5,
    enabled: true,
    mover: null,
	// mouse event processors
	onMouseDown: function( e ){
		// summary: event processor for onmousedown, creates a Mover for the node
		// e: Event: mouse event
        if ( e && e.button == 2 ) return ;
        var djObj = dojo;
        var djEvtObj = djObj.event;
        var jsObj = jetspeed;
        var jsUI = jetspeed.ui;
        //jsObj.debugCache( "moveable onmousedown [" + this.windowOrLayoutWidget.widgetId + "] - mover=" + this.mover + " tempEvents=" + this.tempEvents );
        if ( this.mover != null || this.tempEvents != null )
        {
            this._cleanUpLastEvt( djObj, djEvtObj, jsObj, jsUI );
            jsObj.stopEvent( e );
        }
        else if ( this.enabled )
        {
            if ( this.tempEvents != null )
            {
                if ( djConfig.isDebug )
                    jsObj.debugAlert( "ERROR: Moveable onmousedown tempEvent already defined" );
            }
            else
            {
                var moveableTempEvts = [];
                var doc = this.handle.ownerDocument;
                moveableTempEvts.push( jsUI.evtConnect( "after", doc, "onmousemove", this, "onMouseMove", djEvtObj ) );
                this.tempEvents = moveableTempEvts;
            }
            if ( ! this.windowOrLayoutWidget.posStatic )
                this.windowOrLayoutWidget.bringToTop( e, false, true, jsObj );
            this._lastX = e.pageX;
            this._lastY = e.pageY;
            this._mDownEvt = e;
        }
        jsObj.stopEvent( e );
	},

    onMouseMove: function( e, force )
    {
        var jsObj = jetspeed;
        var djObj = dojo;
        var djEvtObj = djObj.event;
        if ( force || Math.abs(e.pageX - this._lastX) > this.minMove || Math.abs(e.pageY - this._lastY) > this.minMove )
        {
            this._cleanUpLastEvt( djObj, djEvtObj, jsObj, jsObj.ui );

            var wndORlayout = this.windowOrLayoutWidget;
            this.beforeDragColRowInfo = null;

            if ( ! wndORlayout.isLayoutPane )
            {
                var dragNode = wndORlayout.domNode;
                if ( dragNode != null )
                {
                    this.node = dragNode;
		            this.mover = new djObj.dnd.Mover( wndORlayout, dragNode, null, wndORlayout.cL_NA_ED, this, e, false, djObj, jsObj );
                }
            }
            else
            {
                wndORlayout.startDragging( e, this, djObj, jsObj );
            }
        }
        jsObj.stopEvent( e );
    },
    onMouseUp: function( e, suppressErrors )
    {
        var djObj = dojo;
        var jsObj = jetspeed;
        //jsObj.debugCache( "moveable onmouseup [" + this.windowOrLayoutWidget.widgetId + "]" );
        this._cleanUpLastEvt( djObj, djObj.event, jsObj, jsObj.ui, suppressErrors );
    },
    _cleanUpLastEvt: function( djObj, djEvtObj, jsObj, jsUI, suppressErrors )
    {
        //jsObj.debugCache( "moveable _cleanUpLastEvt [" + this.windowOrLayoutWidget.widgetId + "]" );
        if ( this._mDownEvt != null )
        {
            jsObj.stopEvent( this._mDownEvt, suppressErrors );
            this._mDownEvt = null;
        }
        if ( this.mover != null )
        {
            this.mover.destroy( djObj, djEvtObj, jsObj, jsUI );
            this.mover = null;
        }
        // disconnect temp event handlers which were added in onMouseDown
        jsUI.evtDisconnectWObjAry( this.tempEvents, djEvtObj );
        this.tempEvents = null;
    },
	destroy: function( djObj, djEvtObj, jsObj, jsUI )
    {
        this._cleanUpLastEvt( djObj, djEvtObj, jsObj, jsUI );
        jsUI.evtDisconnectWObjAry( this.events, djEvtObj );
		this.events = this.node = this.handle = this.windowOrLayoutWidget = this.beforeDragColRowInfo = null;
	},
    enable: function() { this.enabled = true; },
    disable: function() { this.enabled = false; }
});  // dojo.dnd.Moveable

dojo.getMarginBox = function(node, computedStyle, jsObj){
    var s = computedStyle||dojo.gcs(node), me = dojo._getMarginExtents(node, s, jsObj);
    var l = node.offsetLeft - me.l, t = node.offsetTop - me.t; 
    if(jsObj.UAmoz){
        // Mozilla:
        // If offsetParent has a computed overflow != visible, the offsetLeft is decreased
        // by the parent's border.
        // We don't want to compute the parent's style, so instead we examine node's
        // computed left/top which is more stable.
        var sl = parseFloat(s.left), st = parseFloat(s.top);
        if (!isNaN(sl) && !isNaN(st)) {
            l = sl, t = st;
        } else {
            // If child's computed left/top are not parseable as a number (e.g. "auto"), we
            // have no choice but to examine the parent's computed style.
            var p = node.parentNode;
            if (p) {
                var pcs = dojo.gcs(p);
                if (pcs.overflow != "visible"){
                    var be = dojo._getBorderExtents(p, pcs);
                    l += be.l, t += be.t;
                }
            }
        }
    }
    // On Opera, offsetLeft includes the parent's border
    else if(jsObj.UAope){
        var p = node.parentNode;
        if(p){
            var be = dojo._getBorderExtents(p);
            l -= be.l, t -= be.t;
        }
    }
    return { 
        l: l, 
        t: t, 
        w: node.offsetWidth + me.w, 
        h: node.offsetHeight + me.h 
    };
};

dojo.getContentBox = function(node, computedStyle, jsObj){
    // clientWidth/Height are important since the automatically account for scrollbars
    // fallback to offsetWidth/Height for special cases (see #3378)
    var s=computedStyle||dojo.gcs(node), pe=dojo._getPadExtents(node, s), be=dojo._getBorderExtents(node, s), w=node.clientWidth, h;
    if (!w) {
        w=node.offsetWidth, h=node.offsetHeight;
    } else {
        h=node.clientHeight, be.w = be.h = 0; 
    }
    // On Opera, offsetLeft includes the parent's border
    if(jsObj.UAope){ pe.l += be.l; pe.t += be.t; };
    return { 
        l: pe.l, 
        t: pe.t, 
        w: w - pe.w - be.w, 
        h: h - pe.h - be.h
    };
};

dojo.setMarginBox = function(node, leftPx, topPx, widthPx, heightPx, computedStyle, jsObj){
    var s = computedStyle || dojo.gcs(node);
    // Some elements have special padding, margin, and box-model settings. 
    // To use box functions you may need to set padding, margin explicitly.
    // Controlling box-model is harder, in a pinch you might set dojo.boxModel.
    var bb=dojo._usesBorderBox(node), pb=bb ? { l:0, t:0, w:0, h:0 } : dojo._getPadBorderExtents(node, s), mb=dojo._getMarginExtents(node, s, jsObj);
    if(widthPx != null && widthPx>=0){ widthPx = Math.max(widthPx - pb.w - mb.w, 0); }
    if(heightPx != null && heightPx>=0){ heightPx = Math.max(heightPx - pb.h - mb.h, 0); }
    dojo._setBox(node, leftPx, topPx, widthPx, heightPx);
};

dojo._setBox = function(node, l, t, w, h, u){
    u = u || "px";
    with(node.style){
        if(l != null && !isNaN(l)){ left = l+u; }
        if(t != null && !isNaN(t)){ top = t+u; }
        if(w != null && w >=0){ width = w+u; }
        if(h != null && h >=0){ height = h+u; }
    }
};

dojo._usesBorderBox = function(node){
    // We could test the computed style of node to see if a particular box
    // has been specified, but there are details and we choose not to bother.
    var n = node.tagName;
    // For whatever reason, TABLE and BUTTON are always border-box by default.
    // If you have assigned a different box to either one via CSS then
    // box functions will break.
    return false; // (dojo.boxModel=="border-box")||(n=="TABLE")||(n=="BUTTON");
};

dojo._getPadExtents = function(n, computedStyle){
    // Returns special values specifically useful 
    // for node fitting.
        // l/t = left/top padding (respectively)
    // w = the total of the left and right padding 
    // h = the total of the top and bottom padding
    // If 'node' has position, l/t forms the origin for child nodes. 
    // The w/h are used for calculating boxes.
    // Normally application code will not need to invoke this directly,
    // and will use the ...box... functions instead.
    var s=computedStyle||dojo.gcs(n), px=dojo._toPixelValue, l=px(n, s.paddingLeft), t=px(n, s.paddingTop);
    return { 
        l: l,
        t: t,
        w: l+px(n, s.paddingRight),
        h: t+px(n, s.paddingBottom)
    };
};

dojo._getPadBorderExtents = function(n, computedStyle){
    // l/t = the sum of left/top padding and left/top border (respectively)
    // w = the sum of the left and right padding and border
    // h = the sum of the top and bottom padding and border
    // The w/h are used for calculating boxes.
    // Normally application code will not need to invoke this directly,
    // and will use the ...box... functions instead.
    var s=computedStyle||dojo.gcs(n), p=dojo._getPadExtents(n, s), b=dojo._getBorderExtents(n, s);
    return { 
        l: p.l + b.l,
        t: p.t + b.t,
        w: p.w + b.w,
        h: p.h + b.h
    };
};

dojo._getMarginExtents = function(n, computedStyle, jsObj){
    var 
        s=computedStyle||dojo.gcs(n), 
        px=dojo._toPixelValue,
        l=px(n, s.marginLeft),
        t=px(n, s.marginTop),
        r=px(n, s.marginRight),
        b=px(n, s.marginBottom);
    if (jsObj.UAsaf && (s.position != "absolute")){
        // FIXME: Safari's version of the computed right margin
        // is the space between our right edge and the right edge 
        // of our offsetParent. 
        // What we are looking for is the actual margin value as 
        // determined by CSS.
        // Hack solution is to assume left/right margins are the same.
        r = l;
    }
    return { 
        l: l,
        t: t,
        w: l+r,
        h: t+b
    };
};


dojo._getBorderExtents = function(n, computedStyle){
    // l/t = the sum of left/top border (respectively)
    // w = the sum of the left and right border
    // h = the sum of the top and bottom border
    // The w/h are used for calculating boxes.
    // Normally application code will not need to invoke this directly,
    // and will use the ...box... functions instead.
    var 
        ne='none',
        px=dojo._toPixelValue, 
        s=computedStyle||dojo.gcs(n), 
        bl=(s.borderLeftStyle!=ne ? px(n, s.borderLeftWidth) : 0),
        bt=(s.borderTopStyle!=ne ? px(n, s.borderTopWidth) : 0);
    return { 
        l: bl,
        t: bt,
        w: bl + (s.borderRightStyle!=ne ? px(n, s.borderRightWidth) : 0),
        h: bt + (s.borderBottomStyle!=ne ? px(n, s.borderBottomWidth) : 0)
    };
};

if(!jetspeed.UAie){
    // non-IE branch
    var dv = document.defaultView;
    dojo.getComputedStyle = ((jetspeed.UAsaf) ? function(node){
            var s = dv.getComputedStyle(node, null);
            if(!s && node.style){ 
                node.style.display = ""; 
                s = dv.getComputedStyle(node, null);
            }
            return s || {};
        } : function(node){
            return dv.getComputedStyle(node, null);
        }
    );

    dojo._toPixelValue = function(element, value){
        // style values can be floats, client code may want
        // to round for integer pixels.
        return (parseFloat(value) || 0); 
    };
}else{
    // IE branch
    dojo.getComputedStyle = function(node){
        return node.currentStyle;
    };

    dojo._toPixelValue = function(element, avalue){
        if(!avalue){return 0;}
        // style values can be floats, client code may
        // want to round this value for integer pixels.
        if(avalue.slice&&(avalue.slice(-2)=='px')){ return parseFloat(avalue); }
        with(element){
            var sLeft = style.left;
            var rsLeft = runtimeStyle.left;
            runtimeStyle.left = currentStyle.left;
            try{
                // 'avalue' may be incompatible with style.left, which can cause IE to throw
                // this has been observed for border widths using "thin", "medium", "thick" constants
                // those particular constants could be trapped by a lookup
                // but perhaps there are more
                style.left = avalue;
                avalue = style.pixelLeft;
            }catch(e){
                avalue = 0;
            }
            style.left = sLeft;
            runtimeStyle.left = rsLeft;
        }
        return avalue;
    };
}

dojo.gcs = dojo.getComputedStyle;
