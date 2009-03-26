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

dojo.provide( "jetspeed.desktop.debug" );
dojo.require( "jetspeed.debug" );
dojo.require( "dojo.profile" );


// jetspeed base objects

if ( ! window.jetspeed )
    jetspeed = {};
if ( ! jetspeed.om )
    jetspeed.om = {};


// debug options

jetspeed.debug =
{
    pageLoad: false,
    retrievePsml: false,
    setPortletContent: false,
    doRenderDoAction: false,
    postParseAnnotateHtml: false,
    postParseAnnotateHtmlDisableAnchors: false,
    confirmOnSubmit: false,
    createWindow: false,
    initWinState: false,
    submitWinState: false,
    ajaxPageNav: false,
    dragWindow: false,
    dragWindowStart: false,

    profile: false,

    windowDecorationRandom: false,

    debugInPortletWindow: true,

    debugContainerId: ( djConfig.debugContainerId ? djConfig.debugContainerId : dojo.hostenv.defaultDebugContainerId )
};
//jetspeed.debugPortletEntityIdFilter = [ "dp-7", "dp-3" ];       // load listed portlets only
//jetspeed.debugPortletEntityIdFilter = [];                       // disable all portlets
//jetspeed.debugContentDumpIds = [ ".*" ];                        // dump all responses
//jetspeed.debugContentDumpIds = [ "getmenus", "getmenu-.*" ];    // dump getmenus response and all getmenu responses
//jetspeed.debugContentDumpIds = [ "page-.*" ];                   // dump page psml response
//jetspeed.debugContentDumpIds = [ "js-cp-selector.2" ];          // dump portlet selector content
//jetspeed.debugContentDumpIds = [ "moveabs-layout", "moveabs", "move", "addportlet", "getuserinfo" ];   // dump move and addportlet responses
//jetspeed.debugContentDumpIds = [ "js-cp-selector.*" ];          // dump portlet selector


jetspeed.debugAlert = function( msg )
{
    if ( msg )
        alert( msg );
};

// debug window

jetspeed.debugWindowLoad = function()
{
    var jsObj = jetspeed;
    var jsId = jsObj.id;
    var djObj = dojo;
    if ( djConfig.isDebug && jsObj.debug.debugInPortletWindow && djObj.byId( jsObj.debug.debugContainerId ) == null )
    {
        var dbWSt = jsObj.debugWindowReadCookie( true );
        var wP = {};
        var dbWId = jsId.PW_ID_PREFIX + jsId.DEBUG_WINDOW_TAG;
        wP[ jsId.PP_WINDOW_POSITION_STATIC ] = false;
        wP[ jsId.PP_WINDOW_HEIGHT_TO_FIT ] = false;
        wP[ jsId.PP_WINDOW_DECORATION ] = jsObj.prefs.windowDecoration;
        wP[ jsId.PP_WINDOW_TITLE ] = "Dojo Debug";
        wP[ jsId.PP_WINDOW_ICON ] = "text-x-script.png";
        wP[ jsId.PP_WIDGET_ID ] = dbWId;
        wP[ jsId.PP_WIDTH ] = dbWSt.width;
        wP[ jsId.PP_HEIGHT ] = dbWSt.height;
        wP[ jsId.PP_LEFT ] = dbWSt.left;
        wP[ jsId.PP_TOP ] = dbWSt.top;
        wP[ jsId.PP_EXCLUDE_PCONTENT ] = false;
        wP[ jsId.PP_CONTENT_RETRIEVER ] = new jsObj.om.DojoDebugContentRetriever();
        wP[ jsId.PP_WINDOW_STATE ] = dbWSt.windowState;
        if ( dbWSt.windowState == jsId.ACT_MAXIMIZE )
            jsObj.page.maximizedOnInit = dbWId;
        var pwP = jsObj.widget.PortletWindow.prototype.altInitParamsDef( null, wP );
        jsObj.ui.createPortletWindow( pwP, null, jsObj );
        pwP.retrieveContent( null, null );
        var dbWW = jsObj.page.getPWin( dbWId );

        dbWW.dbContentAdded = function( evt )
        {
            this.contentChanged( evt );
            var clr = document.getElementById("_dbclrspan");
            if ( clr )
            {
                clr.style.visibility = "visible";
            }
        };

        djObj.event.connect( "after", djObj.hostenv, "println", dbWW, "dbContentAdded" );
    
        djObj.event.connect( dbWW, "actionBtnSync", jsObj, "debugWindowSave" );
        djObj.event.connect( dbWW, "endSizing", jsObj, "debugWindowSave" );
        djObj.event.connect( dbWW, "endDragging", jsObj, "debugWindowSave" );
    }
};
jetspeed.debugWindowReadCookie = function( useDefaults )
{
    var debugState = {};
    if ( useDefaults )
        debugState = { width: "400", height: "400", left: "320", top: "0", windowState: jetspeed.id.ACT_MINIMIZE };
    var stateCookieVal = dojo.io.cookie.getCookie( jetspeed.id.DEBUG_WINDOW_TAG );
    if ( stateCookieVal != null && stateCookieVal.length > 0 )
    {
        var debugStateRaw = stateCookieVal.split( "|" );
        if ( debugStateRaw && debugStateRaw.length >= 4 )
        {
            debugState.width = debugStateRaw[0]; debugState.height = debugStateRaw[1]; debugState.top = debugStateRaw[2]; debugState.left = debugStateRaw[3];
            if ( debugStateRaw.length > 4 && debugStateRaw[4] != null && debugStateRaw[4].length > 0 )
                debugState.windowState=debugStateRaw[4];
        }
    }
    return debugState;
};
jetspeed.debugWindowRestore = function()
{
    var debugWindowWidget = jetspeed.debugWindow();
    if ( ! debugWindowWidget ) return;
    debugWindowWidget.restoreWindow();
};
jetspeed.debugWindow = function()
{
    var debugWindowWidgetId = jetspeed.id.PW_ID_PREFIX + jetspeed.id.DEBUG_WINDOW_TAG;
    return jetspeed.page.getPWin( debugWindowWidgetId );
};
jetspeed.debugWindowId = function()
{
    return jetspeed.id.PW_ID_PREFIX + jetspeed.id.DEBUG_WINDOW_TAG;
};
jetspeed.debugWindowSave = function()
{
    var debugWindowWidget = jetspeed.debugWindow();
    if ( ! debugWindowWidget ) return null;
    if ( ! debugWindowWidget.posStatic )
    {
        var currentState = debugWindowWidget.getCurWinStateForPersist( false );
        var cWidth = currentState.width, cHeight = currentState.height, cTop = currentState.top, cLeft = currentState.left;
        var cWinState = debugWindowWidget.windowState;
        if ( ! cWinState ) cWinState = jetspeed.id.ACT_RESTORE;
        var stateCookieVal = cWidth + "|" + cHeight + "|" + cTop + "|" + cLeft + "|" + cWinState;
        dojo.io.cookie.setCookie( jetspeed.id.DEBUG_WINDOW_TAG, stateCookieVal, 30, "/" );
        var readstateCookieVal = dojo.io.cookie.getCookie( jetspeed.id.DEBUG_WINDOW_TAG );
    }
};

jetspeed.debugDumpForm = function( formNode )
{
    if ( ! formNode ) return null ;
    var formDump = formNode.toString() ;
    if ( formNode.name )
        formDump += " name=" + formNode.name;
    if ( formNode.id )
        formDump += " id=" + formNode.id;
    var queryString = dojo.io.encodeForm( formNode );
    formDump += " data=" + queryString; 
    return formDump;
};

// ... jetspeed.om.DojoDebugContentRetriever
jetspeed.om.DojoDebugContentRetriever = function()
{
    this.initialized = false;
};
jetspeed.om.DojoDebugContentRetriever.prototype =
{
    getContent: function( bindArgs, contentListener, domainModelObject, /* String[] */ debugContentDumpIds )
    {
        if ( ! bindArgs )
            bindArgs = {};
        if ( ! this.initialized )
        {
            var jsObj = jetspeed;
            var content = "";
            var dbNodeId = jsObj.debug.debugContainerId;
            var dbWindow = jsObj.debugWindow();

            if ( jsObj.altDebugWindowContent )
                content = jsObj.altDebugWindowContent();
            else
                content += '<div id="' + dbNodeId + '"></div>';

            if ( contentListener )
                contentListener.notifySuccess( content, bindArgs.url, domainModelObject );
            else if ( dbWindow )
                dbWindow.setPortletContent( content, bindArgs.url );

            this.initialized = true;

            if ( dbWindow )
            {
                var clearJS = "javascript: void(jetspeed.debugWindowClear())";
                var indent = "";
                for ( var i = 0 ; i < 20 ; i++ )
                    indent += "&nbsp;";
                var titleWithClearAnchor = dbWindow.title + indent + '<a href="' + clearJS + '"><span id="_dbclrspan" style="visibility: hidden; font-size: xx-small; font-weight: normal; color: blue">Clear</span></a>';
                dbWindow.tbTextNode.innerHTML = titleWithClearAnchor;
            }
        }
    }
};

jetspeed.debugWindowClear = function()
{
    var jsObj = jetspeed;
    var dbNodeId = jsObj.debug.debugContainerId;
    var dbWindow = jsObj.debugWindow();
    document.getElementById(dbNodeId).innerHTML='';
    if ( dbWindow && dbWindow.drag )
        dbWindow.drag.onMouseUp( null, true );
    var clr = document.getElementById("_dbclrspan");
    if ( clr )
    {
        clr.style.visibility = "hidden";
    }
};

// debug info functions

jetspeed.debugDumpColWidths = function()
{
    for ( var i = 0 ; i < jetspeed.page.columns.length ; i++ )
    {
        var columnElmt = jetspeed.page.columns[i];
        dojo.debug( "jetspeed.page.columns[" + i + "] outer-width: " + dojo.html.getMarginBox( columnElmt.domNode ).width );
    }
};
jetspeed.debugDumpWindowsPerCol = function()
{
    for ( var i = 0 ; i < jetspeed.page.columns.length ; i++ )
    {
        var columnElmt = jetspeed.page.columns[i];
        var windowNodesInColumn = jetspeed.ui.getPWinAndColChildren( columnElmt.domNode, null );
        var portletWindowsInColumn = jetspeed.ui.getPWinsFromNodes( windowNodesInColumn.matchingNodes );
        var dumpClosure = { dumpMsg: "" };
        if ( portletWindowsInColumn != null )
        {
            dojo.lang.forEach( portletWindowsInColumn,
                                    function(portletWindow) { dumpClosure.dumpMsg = dumpClosure.dumpMsg + ( dumpClosure.dumpMsg.length > 0 ? ", " : "" ) + portletWindow.portlet.entityId; } );
        }
        dumpClosure.dumpMsg = "column " + i + ": " + dumpClosure.dumpMsg;
        dojo.debug( dumpClosure.dumpMsg );
    }
};

jetspeed.debugDumpWindows = function()
{
    var portletWindows = jetspeed.page.getPWins();
    var pwOut = "";
    for ( var i = 0 ; i < portletWindows.length; i++ )
    {
        if ( i > 0 )
            pwOut += ", ";
        pwOut += portletWindows[i].widgetId;
    }
    dojo.debug( "PortletWindows: " + pwOut );
};

jetspeed.debugLayoutInfo = function()
{
    var jsPage = jetspeed.page;
    var dumpMsg = "";
    var i = 0;
    for ( var layoutId in jsPage.layouts )
    {
        if ( i > 0 ) dumpMsg += "\r\n";
        dumpMsg += "layout[" + layoutId + "]: " + jetspeed.printobj( jsPage.layouts[ layoutId ], true, true, true );
        i++;
    }
    return dumpMsg;
};

jetspeed.debugColumns = function( includePWins, includeDims )
{
    var jsObj = jetspeed;
    var jsPage = jsObj.page;
    var suppressDims = (! includeDims);
    var allCols = jsPage.columns, col;
    if ( ! allCols ) return null;
    var columnContainerNode = dojo.byId( jsObj.id.COLUMNS );
    var buff = "";
    var exclPWins = ! includePWins;
    return jsObj._debugColumnTree( suppressDims, columnContainerNode, buff, "\r\n", jsObj.debugindentT, exclPWins, jsObj, jsPage );
};
jetspeed._debugColumnTree = function( suppressDims, parentNode, buff, indent, indentAddNextLevel, exclPWins, jsObj, jsPage )
{
    var childrenResult = jsObj.ui.getPWinAndColChildren( parentNode, null, false, true, true, exclPWins );
    var childNodes = childrenResult.matchingNodes;
    if ( ! childNodes || childNodes.length == 0 ) return buff;
    var child, col, pWin, pWinTitle, nextIndent = (indent + indentAddNextLevel);
    for ( var i = 0 ; i < childNodes.length ; i++ )
    {
        child = childNodes[i];
        col = jsPage.getColFromColNode( child );
        pWin = null;
        if ( ! col )
            pWin = jsPage.getPWinFromNode( child );
        buff += indent;
        if ( col )
        {
            buff += jsObj.debugColumn( col, suppressDims );
            buff = jsObj._debugColumnTree( suppressDims, child, buff, nextIndent, indentAddNextLevel, exclPWins, jsObj, jsPage );
        }
        else if ( pWin )
        {
            pWinTitle = pWin.title;
            buff += pWin.widgetId + ( ( pWinTitle && pWinTitle.length > 0 ) ? ( " - " + pWinTitle ) : "" );
        }
        else
        {
            buff += jsObj.debugNode( child );
        }
    }
    return buff;
};
jetspeed.debugColumn = function( col, suppressDims )
{
    if ( ! col ) return null;
    var dNodeCol = col.domNode;
    var out = "column[" + dojo.string.padLeft( String(col.pageColumnIndex), 2, " " ) + "]";
    out += " layoutHeader=" + ( col.layoutHeader ? "T" : "F" ) + " id=" + ( dNodeCol != null ? dNodeCol.id : "null" ) + " layoutCol=" + col.layoutColumnIndex + " layoutId=" + col.layoutId + " size=" + col.size + ( col.layoutDepth != null ? ( " depth=" + col.layoutDepth ) : "" ) + ( col.layoutMaxChildDepth > 0 ? ( " childDepth=" + col.layoutMaxChildDepth ) : "" ) + ( col.layoutActionsDisabled ? " noLayout=true" : "" );
    if ( dNodeCol != null && ! suppressDims ) // layoutActionsDisabled
    {
        //var colCompStyle = dojo.gcs( dNodeCol );
        var colAbsPos = dojo.html.getAbsolutePosition( dNodeCol, true );
        var marginBox = dojo.html.getMarginBox( dNodeCol );
        out += " dims={" + "l=" + (colAbsPos.x) + " t=" + (colAbsPos.y) + " r=" + (colAbsPos.x + marginBox.width) + " b=" + (colAbsPos.y + marginBox.height) + " wOff=" + dNodeCol.offsetWidth + " hOff=" + dNodeCol.offsetHeight + " wCl=" + dNodeCol.clientWidth + " hCl=" + dNodeCol.clientHeight + "}";
    }
    return out;
};
jetspeed.debugSavedWinState = function()
{
    return jetspeed.debugWinStateAll( true );
};
jetspeed.debugWinState = function()
{
    return jetspeed.debugWinStateAll( false );
};
jetspeed.debugPortletActions = function()
{
    var portletArray = jetspeed.page.getPortletArray();
    var dumpMsg = "";
    for ( var i = 0; i < portletArray.length; i++ )
    {
        var portlet = portletArray[i];
        if ( i > 0 ) dumpMsg += "\r\n";
        dumpMsg += "portlet [" + portlet.name + "] actions: {";
        for ( var actionKey in portlet.actions )
            dumpMsg += actionKey + "={" + jetspeed.printobj( portlet.actions[actionKey], true ) + "} ";
        dumpMsg += "}";
    }
    return dumpMsg;
};
jetspeed.debugWinStateAll = function( useLastSaved )
{
    var portletArray = jetspeed.page.getPortletArray();
    var dumpMsg = "";
    for ( var i = 0; i < portletArray.length; i++ )
    {
        var portlet = portletArray[i];
        if ( i > 0 ) dumpMsg += "\r\n";
        var windowState = null;
        try
        {
            if ( useLastSaved )
                windowState = portlet.getSavedWinState();
            else
                windowState = portlet.getCurWinState();
        }
        catch (e) { }
        dumpMsg += "[" + portlet.name + "] " + ( (windowState == null) ? "null" : jetspeed.printobj( windowState, true ) );
    }
    return dumpMsg;
};

// Portlet Window debug info functions

jetspeed.debugPWinPos = function( pWin )
{
    var jsObj = jetspeed;
    var djObj = dojo;
    var isIE = jsObj.UAie;
    var djH = djObj.html;
    var dNode = pWin.domNode;
    var cNode = pWin.containerNode;
    var tbNode = pWin.tbNode;
    var rbNode = pWin.rbNode;
    var dAbsPos = djH.getAbsolutePosition( dNode, true );
    var cAbsPos = djH.getAbsolutePosition( cNode, true );
    var tAbsPos = djH.getAbsolutePosition( tbNode, true );
    var rAbsPos = djH.getAbsolutePosition( rbNode, true );
    var dCompStyle = djObj.gcs( dNode ), cCompStyle = djObj.gcs( cNode ), tCompStyle = djObj.gcs( tbNode ), rCompStyle = djObj.gcs( rbNode );
    var bgIfrmAbsPos = null;
    if ( jsObj.UAie6 )
        bgIfrmAbsPos = djH.getAbsolutePosition( pWin.bgIframe.iframe, true );
    var coverIfrm = null;
    var coverIfrmAbsPos = null;
    var coverCompStyle = null;
    if ( pWin.iframesInfo != null && pWin.iframesInfo.iframeCover != null )
    {
        coverIfrm = pWin.iframesInfo.iframeCover;
        coverIfrmAbsPos = djH.getAbsolutePosition( coverIfrm, true );
        coverCompStyle = djObj.gcs( coverIfrm );
    }
    var layoutInfo = pWin._getLayoutInfo();
    var ind = jsObj.debugindent;
    var indH = jsObj.debugindentH;
    djObj.hostenv.println( "wnd-dims [" + pWin.widgetId + "  " + pWin.title + "]" + "  z=" + dNode.style.zIndex + " hfit=" + pWin.heightToFit );
    djObj.hostenv.println( ind + "d.abs {x=" + dAbsPos.x + " y=" + dAbsPos.y + "}" + ( isIE ? ( "  hasLayout=" + dNode.currentStyle.hasLayout ) : "" ) );
    djObj.hostenv.println( ind + "c.abs {x=" + cAbsPos.x + " y=" + cAbsPos.y + "}" + ( isIE ? ( "  hasLayout=" + cNode.currentStyle.hasLayout ) : "" ) );
    djObj.hostenv.println( ind + "t.abs {x=" + tAbsPos.x + " y=" + tAbsPos.y + "}" + ( isIE ? ( "  hasLayout=" + tbNode.currentStyle.hasLayout ) : "" ) );
    djObj.hostenv.println( ind + "r.abs {x=" + rAbsPos.x + " y=" + rAbsPos.y + "}" + ( isIE ? ( "  hasLayout=" + rbNode.currentStyle.hasLayout ) : "" ) );
    if ( bgIfrmAbsPos != null )
        djObj.hostenv.println( ind + "ibg.abs {x=" + bgIfrmAbsPos.x + " y=" + bgIfrmAbsPos.y + "}" + indH + " z=" + pWin.bgIframe.iframe.currentStyle.zIndex + ( isIE ? ( " hasLayout=" + pWin.bgIframe.iframe.currentStyle.hasLayout ) : "" ) );
    if ( coverIfrmAbsPos != null )
        djObj.hostenv.println( ind + "icv.abs {x=" + coverIfrmAbsPos.x + " y=" + coverIfrmAbsPos.y + "}" + indH + " z=" + coverCompStyle.zIndex + ( isIE ? ( " hasLayout=" + coverIfrm.currentStyle.hasLayout ) : "" ) );
    djObj.hostenv.println( ind + "d.mb " + jsObj.debugDims( djObj.getMarginBox( dNode, dCompStyle, jsObj ) ) + indH + " d.offset {w=" + dNode.offsetWidth + " h=" + dNode.offsetHeight + "}" );
    djObj.hostenv.println( ind + "d.cb " + jsObj.debugDims( djObj.getContentBox( dNode, dCompStyle, jsObj ) ) + indH + " d.client {w=" + dNode.clientWidth + " h=" + dNode.clientHeight + "}" );
    djObj.hostenv.println( ind + "d.style {" + jsObj._debugPWinStyle( dNode, dCompStyle, "width", true ) + jsObj._debugPWinStyle( dNode, dCompStyle, "height" ) + indH + jsObj._debugPWinStyle( dNode, dCompStyle, "left" ) + jsObj._debugPWinStyle( dNode, dCompStyle, "top" ) + indH + " pos=" + dCompStyle.position.substring(0,1) + " ofx=" + dCompStyle.overflowX.substring(0,1) + " ofy=" + dCompStyle.overflowY.substring(0,1) + "}" );
    djObj.hostenv.println( ind + "c.mb " + jsObj.debugDims( djObj.getMarginBox( cNode, cCompStyle, jsObj ) ) + indH + " c.offset {w=" + cNode.offsetWidth + " h=" + cNode.offsetHeight + "}" );
    djObj.hostenv.println( ind + "c.cb " + jsObj.debugDims( djObj.getContentBox( cNode, cCompStyle, jsObj ) ) + indH + " c.client {w=" + cNode.clientWidth + " h=" + cNode.clientHeight + "}" );
    djObj.hostenv.println( ind + "c.style {" + jsObj._debugPWinStyle( cNode, cCompStyle, "width", true ) + jsObj._debugPWinStyle( cNode, cCompStyle, "height" ) + indH + jsObj._debugPWinStyle( cNode, cCompStyle, "left" ) + jsObj._debugPWinStyle( cNode, cCompStyle, "top" ) + indH + " ofx=" + cCompStyle.overflowX.substring(0,1) + " ofy=" + cCompStyle.overflowY.substring(0,1) + " d=" + cCompStyle.display.substring(0,1) + "}" );
    djObj.hostenv.println( ind + "t.mb " + jsObj.debugDims( djObj.getMarginBox( tbNode, tCompStyle, jsObj ) ) + indH + " t.offset {w=" + tbNode.offsetWidth + " h=" + tbNode.offsetHeight + "}" );
    djObj.hostenv.println( ind + "t.cb " + jsObj.debugDims( djObj.getContentBox( tbNode, tCompStyle, jsObj ) ) + indH + " t.client {w=" + tbNode.clientWidth + " h=" + tbNode.clientHeight + "}" );
    djObj.hostenv.println( ind + "t.style {" + jsObj._debugPWinStyle( tbNode, tCompStyle, "width", true ) + jsObj._debugPWinStyle( tbNode, tCompStyle, "height" ) + indH + jsObj._debugPWinStyle( tbNode, tCompStyle, "left" ) + jsObj._debugPWinStyle( tbNode, tCompStyle, "top" ) + "}" );
    djObj.hostenv.println( ind + "r.mb " + jsObj.debugDims( djObj.getMarginBox( rbNode, rCompStyle, jsObj ) ) + indH + " r.offset {w=" + rbNode.offsetWidth + " h=" + rbNode.offsetHeight + "}" );
    djObj.hostenv.println( ind + "r.cb " + jsObj.debugDims( djObj.getContentBox( rbNode, rCompStyle, jsObj ) ) + indH + " r.client {w=" + rbNode.clientWidth + " h=" + rbNode.clientHeight + "}" );
    djObj.hostenv.println( ind + "r.style {" + jsObj._debugPWinStyle( rbNode, rCompStyle, "width", true ) + jsObj._debugPWinStyle( rbNode, rCompStyle, "height" ) + indH + jsObj._debugPWinStyle( rbNode, rCompStyle, "left" ) + jsObj._debugPWinStyle( rbNode, rCompStyle, "top" ) + "}" );
    if ( bgIfrmAbsPos != null )
    {
        var iNode = pWin.bgIframe.iframe;
        var iCompStyle = djObj.gcs( iNode );
        djObj.hostenv.println( ind + "ibg.mb " + jsObj.debugDims( djObj.getMarginBox( iNode, iCompStyle, jsObj ) ) );
        djObj.hostenv.println( ind + "ibg.cb " + jsObj.debugDims( djObj.getContentBox( iNode, iCompStyle, jsObj ) ) );
        djObj.hostenv.println( ind + "ibg.style {" + jsObj._debugPWinStyle( iNode, iCompStyle, "width", true ) + jsObj._debugPWinStyle( iNode, iCompStyle, "height" ) + indH + jsObj._debugPWinStyle( iNode, iCompStyle, "left" ) + jsObj._debugPWinStyle( iNode, iCompStyle, "top" ) + indH + " pos=" + iCompStyle.position.substring(0,1) + " ofx=" + iCompStyle.overflowX.substring(0,1) + " ofy=" + iCompStyle.overflowY.substring(0,1) + " d=" + iCompStyle.display.substring(0,1) + "}" );
    }
    if ( coverIfrm )
    {
        djObj.hostenv.println( ind + "icv.mb " + jsObj.debugDims( djObj.getMarginBox( coverIfrm, coverCompStyle, jsObj ) ) );
        djObj.hostenv.println( ind + "icv.cb " + jsObj.debugDims( djObj.getContentBox( coverIfrm, coverCompStyle, jsObj ) ) );
        djObj.hostenv.println( ind + "icv.style {" + jsObj._debugPWinStyle( coverIfrm, coverCompStyle, "width", true ) + jsObj._debugPWinStyle( coverIfrm, coverCompStyle, "height" ) + indH + jsObj._debugPWinStyle( coverIfrm, coverCompStyle, "left" ) + jsObj._debugPWinStyle( coverIfrm, coverCompStyle, "top" ) + indH + " pos=" + coverCompStyle.position.substring(0,1) + " ofx=" + coverCompStyle.overflowX.substring(0,1) + " ofy=" + coverCompStyle.overflowY.substring(0,1) + " d=" + coverCompStyle.display.substring(0,1) + "}" );
    }
    //djObj.hostenv.println( ind + "dNodeCss=" + pWin.dNodeCss.join("") );
    //djObj.hostenv.println( ind + "cNodeCss=" + pWin.cNodeCss.join("") );
    // + " ieHasLayout=" + layoutInfo.dNode.ieHasLayout
    var leN = layoutInfo.dNode;
    djObj.hostenv.println( ind + "dLE {" + "-w=" + leN.lessW + " -h=" + leN.lessH + " mw=" + leN.mE.w + " mh=" + leN.mE.h + " bw=" + leN.bE.w + " bh=" + leN.bE.h + " pw=" + leN.pE.w + " ph=" + leN.pE.h + "}" );
    leN = layoutInfo.cNode;
    djObj.hostenv.println( ind + "cLE {" + "-w=" + leN.lessW + " -h=" + leN.lessH + " mw=" + leN.mE.w + " mh=" + leN.mE.h + " bw=" + leN.bE.w + " bh=" + leN.bE.h + " pw=" + leN.pE.w + " ph=" + leN.pE.h + "}" );
    leN = layoutInfo.tbNode;
    djObj.hostenv.println( ind + "tLE {" + "-w=" + leN.lessW + " -h=" + leN.lessH + " mw=" + leN.mE.w + " mh=" + leN.mE.h + " bw=" + leN.bE.w + " bh=" + leN.bE.h + " pw=" + leN.pE.w + " ph=" + leN.pE.h + "}" );
    leN = layoutInfo.rbNode;
    djObj.hostenv.println( ind + "rLE {" + "-w=" + leN.lessW + " -h=" + leN.lessH + " mw=" + leN.mE.w + " mh=" + leN.mE.h + " bw=" + leN.bE.w + " bh=" + leN.bE.h + " pw=" + leN.pE.w + " ph=" + leN.pE.h + "}" );

    djObj.hostenv.println( ind + "cNode_mBh_LessBars=" + layoutInfo.cNode_mBh_LessBars );
    djObj.hostenv.println( ind + "dimsTiled " + jsObj.debugDims( pWin.dimsTiled ) );
    djObj.hostenv.println( ind + "dimsUntiled " + jsObj.debugDims( pWin.dimsUntiled ) );
    if ( pWin.dimsTiledTemp != null )
        djObj.hostenv.println( ind + "dimsTiledTemp " + jsObj.debugDims( pWin.dimsTiledTemp ) );
    if ( pWin.dimsUntiledTemp != null )
        djObj.hostenv.println( ind + "dimsUntiledTemp=" + jsObj.debugDims( pWin.dimsUntiledTemp ) );
    djObj.hostenv.println( ind + "--------------------" );
    //" document-width=" + dojo.html.getMarginBox( document[ "body" ] ).width + " document-height=" + dojo.html.getMarginBox( document[ "body" ] ).height
},
jetspeed.debugDims = function( box, suppressEnd )
{
    return ( "{w=" + ( box.w == undefined ? ( box.width == undefined ? "null" : box.width ) : box.w ) + " h=" + ( box.h == undefined ? ( box.height == undefined ? "null" : box.height ) : box.h ) + ( box.l != undefined ? (" l=" + box.l) : ( box.left == undefined ? "" : (" l=" + box.left) ) ) + ( box.t != undefined ? (" t=" + box.t) : ( box.top == undefined ? "" : (" t=" + box.top) ) ) + ( box.right != undefined ? (" r=" + box.right) : "" ) + ( box.bottom != undefined ? (" b=" + box.bottom) : "" ) + ( ! suppressEnd ? "}" : "" ) ) ;
};
jetspeed._debugPWinStyle = function( node, compStyle, propName, omitLeadingSpace )
{
    var sStyle = node.style[ propName ];
    var cStyle = compStyle[ propName ];
    if ( sStyle == "auto" ) sStyle = "a";
    if ( cStyle == "auto" ) cStyle = "a";
    var showVal = null;
    if ( sStyle == cStyle )
        showVal = ('"' + cStyle + '"');
    else
        showVal = ('"' + sStyle + '"/' + cStyle);
    return ( (omitLeadingSpace ? "" : " ") + propName.substring( 0, 1 ) + '=' + showVal);
};


// profile functions

if ( jetspeed.debug.profile )
{
    dojo.profile.clearItem = function(name) {
    	// summary:	clear the profile times for a particular entry
    	return (this._profiles[name] = {iters: 0, total: 0});
    }
    dojo.profile.debugItem = function(name,clear) {
    	// summary:	write profile information for a particular entry to the debug console
    	var profile = this._profiles[name];
    	if (profile == null) return null;
    	
    	if (profile.iters == 0) {
    		return [name, " not profiled."].join("");
    	}
    	var output = [name, " took ", profile.total, " msec for ", profile.iters, " iteration"];
    	if (profile.iters > 1) {
    		output.push("s (", (Math.round(profile.total/profile.iters*100)/100), " msec each)");
    	}
    
    	// summary: print profile information for a single item out to the debug log
    	dojo.debug(output.join(""));
        if ( clear )
            this.clearItem( name );
    }
    dojo.profile.debugAllItems = function(clear) {
        for(var x=0; x < this._pns.length; x++){
            this.debugItem( this._pns[x], clear );
        }
    }        
}

window.getPWin = function( portletWindowId )
{
    return jetspeed.page.getPWin( portletWindowId );
};
