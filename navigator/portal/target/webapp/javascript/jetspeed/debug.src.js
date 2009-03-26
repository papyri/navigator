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

if ( window.dojo )
{
    dojo.provide( "jetspeed.debug" );
}


// jetspeed base objects

if ( ! window.jetspeed )
    jetspeed = {};


// common debugging functions

jetspeed.dumpary = function( ary, header )
{
    if ( ! ( ary && ary.length >= 0 ) ) return null;
    var jsObj = jetspeed;
    if ( header )
        jsObj.println( header + "  len=" + ary.length );
    for( var i = 0; i < ary.length; i++ )
    {
        jsObj.println( jsObj.debugindentH + "[" + i + "]: " + jsObj.printobj( ary[i] ) );
    }
};

jetspeed.printobj = function( obj, omitLineBreaks, omitEmptyValsProperties, arrayLengthsOnly, showFunctions, showFunctionCode )
{
    var props = [];
    for( var prop in obj )
    {
        try
        {
            var propVal = obj[prop];
            if ( arrayLengthsOnly )
            {
                if ( dojo.lang.isArray( propVal ) )
                {
                    propVal = "[" + propVal.length + "]";
                }
            }
            if ( dojo.lang.isFunction( propVal ) )
            {
                if ( ! showFunctions )
                    continue;
                if ( ! showFunctionCode )
                    propVal = "function";
            }
            propVal = propVal + "";
            if ( ! omitEmptyValsProperties || propVal.length > 0 )
                props.push( prop + ': ' + propVal );
        }
        catch(E)
        {
            props.push( prop + ': ERROR - ' + E.message );
        }
    }
    props.sort();
    var buff = "";
    for( var i = 0; i < props.length; i++ )
    {
        if ( buff.length > 0 )
            buff += ( omitLineBreaks ? ", " : "\r\n" );
        buff += props[i];
    }
    return buff;
};

jetspeed.println = function( line )
{
    try
    {
        var console = jetspeed.getDebugElement();
        var div = document.createElement( "div" );
        div.appendChild( document.createTextNode( line ) );
        console.appendChild( div );
    }
    catch (e)
    {
        try
        {   // safari needs the output wrapped in an element for some reason
            document.write("<div>" + line + "</div>");
        }
        catch(e2)
        {
            window.status = line;
        }
    }
};

jetspeed.objectKeys = function( obj )
{
    var keys = new Array();
    if ( obj != null )
    {
        for( var key in obj )
            keys.push( key );
    }
    return keys;
};

jetspeed.debugNodes = function( nodes )
{
    if ( ! nodes || nodes.length == null ) return null;
    var jsObj = jetspeed;
    var djObj = dojo;
    var out = "", node;
    var nodesLen = nodes.length;
    var leftPad = ( nodesLen >= 100 ? 3 : ( nodesLen >= 10 ? 2 : 1 ) );
    for ( var i = 0 ; i < nodesLen ; i++ )
    {
        node = nodes[i];
        out += "\r\n";
        out += "[" + djObj.string.padLeft( String(i), leftPad, "0" ) + "] ";
        if ( ! node )
            out += "null";
        else
            out += jsObj.debugNode( node );
    }
    return out;
}

jetspeed.debugNode = function( node )
{
    if ( ! node ) return null;
    return node.nodeName + " " + node.id + " " + node.className;
}

jetspeed.debugNodeTree = function( node, string )
{
    if ( ! node ) return ;
    
    if ( string )
    {
        if ( string.length > 0 )
            jetspeed.println( string );
    }
    else
    {
        jetspeed.println( 'node: ' );
    }
    if ( node.nodeType != 1 && node.nodeType != 3 )
    {
        if ( node.length && node.length > 0 && ( node[0].nodeType == 1 || node[0].nodeType == 3 ) )
        {
            for ( var i = 0 ; i < node.length ; i++ )
            {
                jetspeed.debugNodeTree( node[i], " [" + i + "]" )
            }
        }
        else
        {
            jetspeed.println( " node is not a node! " + node.length );
        }   
        return ;
    }
    if ( node.innerXML )
    {
        jetspeed.println( node.innerXML );
    }
    else if ( node.xml )
    {
        jetspeed.println( node.xml );
    }
    else if ( typeof XMLSerializer != "undefined" )
    {
        jetspeed.println( (new XMLSerializer()).serializeToString( node ) );
    }
    else
    {
        jetspeed.println( " node != null (IE no XMLSerializer)" );
    }
};
jetspeed.debugShallow = function( obj, string )
{
    if ( string )
        jetspeed.println( string );
    else
        jetspeed.println( 'Object: ' + obj );
    var props = [];
    for(var prop in obj){
        try {
            props.push(prop + ': ' + obj[prop]);
        } catch(E) {
            props.push(prop + ': ERROR - ' + E.message);
        }
    }
    props.sort();
    for(var i = 0; i < props.length; i++) {
        jetspeed.println( props[i] );
    }
};
jetspeed.debugCache = function( msg )
{
    var jsObj = jetspeed;
    var dbgCache = jsObj._debugCache;
    if ( ! dbgCache )
    {
        dbgCache = jsObj._debugCache = new Array(100);
        jsObj._debugCacheI = 0;
    }
    var dt = new Date();
    var dtH = dt.getHours(), dtM = dt.getMinutes(), dtS = dt.getSeconds(), dtMS = dt.getMilliseconds();
    dbgCache[ jsObj._debugCacheI ] = ( dtH < 10 ? ( "0" + dtH ) : dtH ) + ":" + ( dtM < 10 ? ( "0" + dtM ) : dtM ) + ":" + ( dtS < 10 ? ( "0" + dtS ) : dtS ) + ":" + ( dtMS >= 100 ? dtMS : ( dtMS >= 10 ? ( "0" + dtMS ) : ( "00" + dtMS ) ) ) + " - " + msg;
    jsObj._debugCacheI++;
};
jetspeed.debugCacheDump = function()
{
    var jsObj = jetspeed;
    var djObj = dojo;
    var dbgCache = jsObj._debugCache;
    if ( dbgCache )
    {
        var dbgCacheI = jsObj._debugCacheI;
        for ( var i = 0 ; i < dbgCacheI ; i++ )
        {
            djObj.hostenv.println( dbgCache[i] );
        }
        jsObj._debugCache = null;
    }
};
jetspeed.getDebugElement = function( clear )
{
    var docBody = null;
    var console = null;
    try {
        var consoleId = jetspeed.debug.debugContainerId;
        console = document.getElementById(consoleId);
        if(!console)
        {
            consoleId = "debug_container";
            console = document.getElementById(consoleId);
            if(!console)
            {
                docBody = jetspeed.docBody;
                if ( docBody == null )
                    docBody = jetspeed.getBody();
                console = document.createElement("div");
                console.setAttribute( "id", "debug_container" );
                docBody.appendChild(console);
            }
        }
        if ( console && clear )
        {
            console.innerHTML = "";
        }
    } catch (e) {
        try {
            if ( console == null ) console = jetspeed.getBody();
        } catch(e2){}
    }
    return console;
};
if ( window.djConfig != null && window.djConfig.isDebug )
{
    var ch = String.fromCharCode(0x00a0);
    jetspeed.debugindentch = ch;
    jetspeed.debugindentH = ch + ch;
    jetspeed.debugindentT = ch + ch + ch;
    jetspeed.debugindent = ch + ch + ch + ch;
    jetspeed.debugindent2 = jetspeed.debugindent + jetspeed.debugindent;
    jetspeed.debugindent3 = jetspeed.debugindent + jetspeed.debugindent + jetspeed.debugindent;
}
