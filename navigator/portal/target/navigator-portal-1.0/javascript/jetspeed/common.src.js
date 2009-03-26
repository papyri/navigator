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

// jetspeed javascript to help support portlets in both /portal and /desktop

if ( window.dojo )
{
    dojo.provide( "jetspeed.common" );
    dojo.require( "dojo.io.*" );
    dojo.require( "dojo.uri.Uri" );
}


// jetspeed base objects

if ( ! window.jetspeed )
    jetspeed = {};
if ( ! jetspeed.url )
    jetspeed.url = {};
if ( ! jetspeed.om )
    jetspeed.om = {};
if ( ! jetspeed.widget )
    jetspeed.widget = {};

// jetspeed version

jetspeed.version = 
{
    major: 2, minor: 1, patch: 0, flag: "dev",
    revision: "",
    toString: function() 
    {
        with (jetspeed.version) 
        {
            return major + "." + minor + "." + patch + flag + " (" + revision + ")";
        }
    }
};

jetspeed.initcommon = function()
{
    var jsObj = jetspeed;
    if ( ! window.dojo )
    {
        var jsObj = jetspeed;
        jsObj.no_dojo_load_notifying = false;
        jsObj.no_dojo_post_load = false;
        jsObj.pageLoadedListeners = [];
    
        window.onload = function()
        {
            if ( ! window.dojo )
            {
                var _jsObj = jetspeed;
                _jsObj.no_dojo_load_notifying = true;
                _jsObj.no_dojo_post_load = true;
                var pll = _jsObj.pageLoadedListeners;
    	        for( var x=0; x < pll.length; x++ )
                {
    		        pll[x]();
    	        }
                _jsObj.pageLoadedListeners = [];
            }
        };
    }
    else
    {
        var djRH = dojo.render.html;
        if ( djRH.ie )
        {
            jsObj.UAie = true;
            if ( djRH.ie60 || djRH.ie50 || djRH.ie55 )
                jsObj.UAie6 = true;
    
            jsObj.stopEvent = function(/*Event*/evt, suppressErrors)
            {   // do no use in event connect
                try
                {
                    evt = evt || window.event;
                    if ( evt )
                    {
                        evt.cancelBubble = true;
                        evt.returnValue = false;
                    }
                }
                catch(ex)
                {
                    if ( ! suppressErrors && djConfig.isDebug )
                        dojo.debug( "stopEvent (" + ( typeof evt ) + ") failure: " + jetspeed.formatError( ex ) );
                }
    	    };
    	    jsObj._stopEvent = function(/*Event*/evt)
            {   // use in event connect
                jetspeed.stopEvent( evt );
        	};
        }
        else
        {
            if ( djRH.mozilla )
                jsObj.UAmoz = true;
            else if ( djRH.safari )
                jsObj.UAsaf = true ;
            else if ( djRH.opera )
                jsObj.UAope = true ;
    
            jsObj.stopEvent = function(/*Event*/evt)
            {   // do no use in event connect
                evt.preventDefault();
                evt.stopPropagation();
        	};
            jsObj._stopEvent = function(/*Event*/evt)
            {   // use in event connect
                jetspeed.stopEvent( evt );
        	};
        }
    }
}




// Call styles:
//	jetspeed.addOnLoad( functionPointer )
//	jetspeed.addOnLoad( object, "functionName" )
jetspeed.addOnLoad = function( obj, fcnName )
{
    if ( window.dojo )
    {
        if ( arguments.length == 1 )
            dojo.addOnLoad( obj );
        else
            dojo.addOnLoad( obj, fcnName );
    }
    else
    {
	    if ( arguments.length == 1 )
        {
		    jetspeed.pageLoadedListeners.push(obj);
	    }
        else if( arguments.length > 1 )
        {
		    jetspeed.pageLoadedListeners.push( function()
            {
			    obj[fcnName]();
		    } );
	    }
        if ( jetspeed.no_dojo_post_load && ! jetspeed.no_dojo_load_notifying )
        {
		    jetspeed.callPageLoaded();
	    }
    }
};

jetspeed.callPageLoaded = function()
{
	if( typeof setTimeout == "object" )  // IE
    {
		setTimeout( "jetspeed.pageLoaded();", 0 );
	}
    else
    {
		jetspeed.pageLoaded();
	}
};

jetspeed.getBody = function()
{
    var jsObj = jetspeed;
    if ( jsObj.docBody == null )
        jsObj.docBody = document.body || document.getElementsByTagName( "body" )[0];
    return jsObj.docBody;
};

jetspeed.formatError = function( ex )
{
    if ( ex == null ) return "";
    var msg = " error:";
    if ( ex.message != null )
        msg += " " + ex.message;
    var lineNo = ex.number||ex.lineNumber||ex.lineNo;
    if ( lineNo == null || lineNo == "0" || lineNo.length == 0 )
        lineNo = null;
    var fileNm = ex.fileName;
    if ( fileNm != null )
    {
        var lastDirSep = fileNm.lastIndexOf( "/" );
        if ( lastDirSep != -1 && lastDirSep < (fileNm.length -1) )
            fileNm = fileNm.substring( lastDirSep + 1 );
    }
    if ( fileNm == null || fileNm.length == 0 )
        fileNm = null;
    var errType = ex.type;
    if ( errType == null || errType.length == 0 || errType == "unknown" )
        errType = null;

    if ( lineNo != null || fileNm != null || errType != null )
    {
        msg += " (" + ( fileNm != null ? ( " " + fileNm ) : "" );
        msg += ( lineNo != null ? (" line " + lineNo) : "" );
        msg += ( errType != null ? (" type " + errType) : "" );
        msg += " )";
    }
    return msg;
};


// jetspeed.url

jetspeed.url.LOADING_INDICATOR_ID = "js-showloading";
jetspeed.url.LOADING_INDICATOR_IMG_ID = "js-showloading-img";
jetspeed.url.path =
{
    SERVER: null,     //   http://localhost:8080
    JETSPEED: null,   //   /jetspeed
    AJAX_API: null,   //   /jetspeed/ajaxapi
    DESKTOP: null,    //   /jetspeed/desktop
    PORTAL: null,     //   /jetspeed/portal
    PORTLET: null,    //   /jetspeed/portlet
    ACTION: null,     //   /jetspeed/action
    RENDER: null,     //   /jetspeed/render
    initialized: false
};

jetspeed.url.pathInitialize = function( force )
{
    var jsU = jetspeed.url;
    var jsUP = jsU.path;
    if ( ! force && jsUP.initialized ) return;
    var baseTags = document.getElementsByTagName( "base" );

    var baseTagHref = null;
    if ( baseTags && baseTags.length == 1 )
        baseTagHref = baseTags[0].href;
    else
        baseTagHref = window.location.href;

    var baseTag = jsU.parse( baseTagHref );

    var basePath = baseTag.path;
    
    var serverUri = "";
    if ( baseTag.scheme != null) { serverUri += baseTag.scheme + ":"; }
    if ( baseTag.authority != null) { serverUri += "//" + baseTag.authority; }

    var jetspeedPath = null;
    if ( djConfig.jetspeed.rootContext )
    {
      jetspeedPath = "";
    }
    else
    {
      var sepPos = -1;
      for( var startPos =1 ; sepPos <= startPos ; startPos++ )
      {
        sepPos = basePath.indexOf( "/", startPos );
        if ( sepPos == -1 )
            break;
      }

      if ( sepPos == -1 )
        jetspeedPath = basePath;
      else
        jetspeedPath = basePath.substring( 0, sepPos );
    } 
    
    //dojo.debug( "pathInitialize  new-JETSPEED=" + jetspeedPath + " orig-JETSPEED=" + jsUP.JETSPEED + " new-SERVER=" + serverUri + " orig-SERVER=" + document.location.protocol + "//" + document.location.host );
    
    jsUP.JETSPEED = jetspeedPath;
    jsUP.SERVER = serverUri;
    jsUP.AJAX_API = jsUP.JETSPEED + "/ajaxapi";
    jsUP.DESKTOP = jsUP.JETSPEED + "/desktop";
    jsUP.PORTAL = jsUP.JETSPEED + "/portal";
    jsUP.PORTLET = jsUP.JETSPEED + "/portlet";
    jsUP.ACTION = jsUP.JETSPEED + "/action";
    jsUP.RENDER = jsUP.JETSPEED + "/render";

    jsUP.initialized = true;
};
jetspeed.url.parse = function( url )
{   // taken from dojo.uri.Uri
    if ( url == null )
        return null;
    if ( window.dojo && window.dojo.uri )
        return new dojo.uri.Uri( url );
    return new jetspeed.url.JSUri( url );
};
jetspeed.url.JSUri = function( url )
{
    if ( url != null )
    {
        if ( ! url.path )
        {
            var regexp = "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$";
            var r = url.toString().match( new RegExp( regexp ) );
            var parsedUrl = {};
            this.scheme = r[2] || (r[1] ? "" : null);
            this.authority = r[4] || (r[3] ? "" : null);
            this.path = r[5]; // can never be undefined
            this.query = r[7] || (r[6] ? "" : null);
            this.fragment  = r[9] || (r[8] ? "" : null);
        }
        else
        {
            this.scheme = url.scheme;
            this.authority = url.authority;
            this.path = url.path;
            this.query= url.query;
            this.fragment = url.fragment;
        }
    }
};
jetspeed.url.JSUri.prototype =
{
    scheme: null,
    authority: null,
    path: null,
    query: null,
    fragment: null,
    toString: function()
    {
        var uri = "";
        uri += ( this.scheme != null && this.scheme.length > 0 ) ? ( this.scheme + "://" ) : "";
        uri += ( this.authority != null && this.authority.length > 0 ) ? this.authority : "";
        uri += ( this.path != null && this.path.length > 0 ) ? this.path : "";
        uri += ( this.query != null && this.query.length > 0 ) ? ( "?" + this.query ) : "";
        uri += ( this.fragment != null && this.fragment > 0 ) ? ( "#" + this.fragment ) : "";
        return uri;
    }
};
jetspeed.url.scheme =
{   // used to make jetspeed.url.urlStartsWithHttp cleaner
    HTTP_PREFIX: "http://",
    HTTP_PREFIX_LEN: "http://".length,
    HTTPS_PREFIX: "https://",
    HTTPS_PREFIX_LEN: "https://".length
};
jetspeed.url.isPortal = function()
{
    if ( window.djConfig && window.djConfig.jetspeed )
    {
        var servletPath = window.djConfig.jetspeed.servletPath;
        if ( servletPath != null && servletPath.toLowerCase().indexOf( "/desktop" ) == 0 )
            return false;
    }
    return true;
};
jetspeed.url.isDesktop = function()
{
    return ! jetspeed.url.isPortal();
};
jetspeed.url.servletPath = function()
{
    if ( jetspeed.url.isPortal() )
        return "/portal";
    else
        return "/desktop";
};
jetspeed.url.basePortalUrl = function()
{
    if ( ! jetspeed.url.path.initialized )
        jetspeed.url.pathInitialize();
    return jetspeed.url.path.SERVER;    // return document.location.protocol + "//" + document.location.host ;
};
jetspeed.url.basePortalDesktopUrl = function()
{
    if ( ! jetspeed.url.path.initialized )
        jetspeed.url.pathInitialize();
    return jetspeed.url.basePortalUrl() + jetspeed.url.path.JETSPEED ;
};
jetspeed.url.addPath = function( url, path )
{
    if ( path == null || path.length == 0 )
        return url;
    var modUri = new jetspeed.url.JSUri( url );
    var origPath = modUri.path;
    if ( origPath != null && origPath.length > 0 )
    {
        if ( modUri.path.charCodeAt( origPath.length -1 ) == 47 )
        {
            if ( path.charCodeAt( 0 ) == 47 )
            {
                if ( path.length > 1 )
                    modUri.path += path.substring( 1 );
            }
            else
            {
                modUri.path += path;
            }
        }
        else
        {
            if ( path.charCodeAt( 0 ) == 47 )
            {
                modUri.path += path;
            }
            else
            {
                if ( path.length > 1 )
                    modUri.path += "/" + path;
            }
        }
    }
    var urlObj = jetspeed.url.parse( modUri );
    return urlObj.toString();
};
jetspeed.url.urlStartsWithHttp = function( url )
{
    if ( url )
    {
        var len = url.length;
        var hSLen = jetspeed.url.scheme.HTTPS_PREFIX_LEN;
        if ( len > hSLen )  // has to be at least longer than as https://
        {
            var hLen = jetspeed.url.scheme.HTTP_PREFIX_LEN;
            if ( url.substring( 0, hLen ) == jetspeed.url.scheme.HTTP_PREFIX )
                return true;
            if ( url.substring( 0, hSLen ) == jetspeed.url.scheme.HTTPS_PREFIX )
                return true;
        }
    }
    return false;
};
jetspeed.url.addQueryParameter = function( urlObj, paramname, paramvalue, removeExisting )
{
    if ( urlObj == null )
        return urlObj;
    if ( ! urlObj.path )
        urlObj = jetspeed.url.parse( urlObj );
    if ( urlObj == null )
        return null;
    if ( paramname == null )
        return urlObj;
    urlObj.jsQParamN = null;
    if ( removeExisting )
        urlObj = jetspeed.url.removeQueryParameter( urlObj, paramname, false );
    
    var urlQuery = urlObj.query;
    if ( urlQuery == null )
        urlQuery = "";
    var urlQueryLen = urlQuery.length;
    if ( urlQueryLen > 0 )
        urlQuery += "&";
    urlQuery += paramname + "=" + ( paramvalue != null ? paramvalue : "" );
    urlObj.query = urlQuery;
    var modUri = new jetspeed.url.JSUri( urlObj );        
    urlObj = jetspeed.url.parse( modUri );
    return urlObj;
};
jetspeed.url.removeAllQueryParameters = function( urlObj )
{
    return jetspeed.url.removeQueryParameter( urlObj, null, true );
};
jetspeed.url.removeQueryParameter = function( urlObj, paramname, removeAllParameters )
{
    if ( urlObj == null )
        return urlObj;
    if ( ! urlObj.path )
        urlObj = jetspeed.url.parse( urlObj );
    if ( urlObj == null )
        return null;
    urlObj.jsQParamN = null;
    var urlQuery = urlObj.query;
    var urlQueryLen = ( ( urlQuery != null ) ? urlQuery.length : 0 );
    if ( urlQueryLen > 0 )
    {
        if ( removeAllParameters )
            urlQuery = null;
        else if ( paramname == null )
            return urlObj;
        else
        {
            var matchParam = paramname;
            var matchPos = urlQuery.indexOf( matchParam );
            if ( matchPos == 0 )
                urlQuery = jetspeed.url._removeQP( urlQuery, urlQueryLen, matchParam, matchPos );
            
            matchParam = "&" + paramname;
            while ( true )
            {
                urlQueryLen = ( ( urlQuery != null ) ? urlQuery.length : 0 );
                matchPos = urlQuery.indexOf( matchParam, 0 );
                if ( matchPos == -1 )
                    break;
                var modUrlQuery = jetspeed.url._removeQP( urlQuery, urlQueryLen, matchParam, matchPos );
                if ( modUrlQuery == urlQuery )
                    break;
                urlQuery = modUrlQuery;
            }
            if ( urlQuery.length > 0 )
            {
                if ( urlQuery.charCodeAt( 0 ) == 38 ) // "&"
                    urlQuery = ( ( urlQuery.length > 1 ) ? urlQuery.substring( 1 ) : "" );
                if ( urlQuery.length > 0 && urlQuery.charCodeAt( 0 ) == 63 ) // "?"
                    urlQuery = ( ( urlQuery.length > 1 ) ? urlQuery.substring( 1 ) : "" );
            }
        }
        urlObj.query = urlQuery;
        var modUri = new jetspeed.url.JSUri( urlObj );        
        urlObj = jetspeed.url.parse( modUri );
    }
    return urlObj;
};

jetspeed.url._removeQP = function( urlQuery, urlQueryLen, matchParam, matchPos )
{
    if ( matchPos == -1 ) return urlQuery;
    if ( urlQueryLen > ( matchPos + matchParam.length ) )
    {
        var nextCh = urlQuery.charCodeAt( matchPos + matchParam.length );
        if ( nextCh == 61 )  // "="
        {
            var ampPos = urlQuery.indexOf( "&", matchPos + matchParam.length + 1 );
            if ( ampPos != -1 )
            {
                if ( matchPos > 0 )
                    urlQuery = urlQuery.substring( 0, matchPos ) + urlQuery.substring( ampPos );
                else
                    urlQuery = ( ( ampPos < (urlQueryLen -1) ) ? urlQuery.substring( ampPos ) : "" );
            }
            else
            {
                if ( matchPos > 0 )
                    urlQuery = urlQuery.substring( 0, matchPos )
                else
                    urlQuery = "";
            }
        }
        else if ( nextCh == 38 ) // "&"
        {
            if ( matchPos > 0 )
                urlQuery = urlQuery.substring( 0, matchPos ) + urlQuery.substring( matchPos + matchParam.length );
            else
                urlQuery = urlQuery.substring( matchPos + matchParam.length );
        }
    }
    else if ( urlQueryLen == ( matchPos + matchParam.length ) )
    {
        urlQuery = "";
    }
    return urlQuery;
};

jetspeed.url.getQueryParameter = function( urlObj, paramname )
{
    if ( urlObj == null )
        return null;
    if ( ! urlObj.authority || ! urlObj.scheme )
        urlObj = jetspeed.url.parse( urlObj );
    if ( urlObj == null )
        return null;
    if ( urlObj.jsQParamN == null && urlObj.query )
    {
        var vAry=new Array() ;
        var nAry = urlObj.query.split( "&" );
        for ( var i=0; i < nAry.length; i++ )
        {
            if ( nAry[i] == null )
                nAry[i]="";
            var sepP = nAry[i].indexOf( "=" );
            if ( sepP > 0 && sepP < (nAry[i].length -1) )
            {
                vAry[i] = unescape( nAry[i].substring( sepP + 1 ) );
                nAry[i] = unescape( nAry[i].substring( 0, sepP ) );
            }
            else
            {
                vAry[i] = "";
            }
        }
        urlObj.jsQParamN = nAry;
        urlObj.jsQParamV = vAry;
    }
    if ( urlObj.jsQParamN != null )
    {
        for ( var i=0; i < urlObj.jsQParamN.length; i++ )
        {
            if ( urlObj.jsQParamN[i] == paramname )
            {
                return urlObj.jsQParamV[i];
            }
        }
    }
    return null;
};


// jetspeed.om.Id

jetspeed.om.Id = function( /* ... */ )  // intended as a simple, general object with an id and a getId() function
{
    var idBuff = "";
    for ( var i = 0; i < arguments.length; i++ )
    {
        if( dojo.lang.isString( arguments[i] ) )
        {
            if ( idBuff.length > 0 )
                idBuff += "-";
            idBuff += arguments[i];
        }
        else if ( dojo.lang.isObject( arguments[i] ) )
        {
            for ( var slotKey in arguments[i] )
            {
                this[ slotKey ] = arguments[i][slotKey];
            }
        }
    }
    this.id = idBuff;
};
jetspeed.om.Id.prototype =
{
    getId: function()
    {
        return this.id;
    }
};

if ( window.dojo )
{
    jetspeed.url.BindArgs = function( bindArgs )
    {
        dojo.lang.mixin( this, bindArgs );
    
        if ( ! this.mimetype )
            this.mimetype = "text/html";

        if ( ! this.encoding )
            this.encoding = "utf-8";
    };
    
    dojo.lang.extend( jetspeed.url.BindArgs,
    {
        createIORequest: function()
        {
            var ioReq = new dojo.io.Request( this.url, this.mimetype );
            ioReq.fromKwArgs( this );  // doing this cause dojo.io.Request tests arg0 for ctor == Object; we want out own obj here
            return ioReq;
        },
    
        load: function( type, data, http )
        {
            //dojo.debug( "loaded content for url: " + this.url );
            //dojo.debug( "r e t r i e v e C o n t e n t . l o a d" ) ;
            //dojo.debug( "  type:" );
            //dojo.debugShallow( type ) ;
            //dojo.debug( "  http:" );
            //dojo.debugShallow( http ) ;
            try
            {
                var dmId = null;
                if ( this.debugContentDumpIds )
                {
                    dmId = ( ( this.domainModelObject && dojo.lang.isFunction( this.domainModelObject.getId ) ) ? this.domainModelObject.getId() : ( ( this.domainModelObject && this.domainModelObject.id ) ? String( this.domainModelObject.id ) : "" ) );
                    var outputResponse = false;
                    for ( var debugContentIndex = 0 ; debugContentIndex < this.debugContentDumpIds.length; debugContentIndex++ )
                    {
                        if ( dmId.match( new RegExp( this.debugContentDumpIds[ debugContentIndex ] ) ) )
                        {
                            outputResponse = true;
                            break;
                        }
                    }
                    if ( outputResponse )
                    {
                        if ( dojo.lang.isString( data ) )
                            dojo.debug( "retrieveContent [" + ( dmId ? dmId : this.url ) + "] content: " + data );
                        else
                        {
                            var textContent = dojo.dom.innerXML( data );
                            if ( ! textContent )
                                textContent = ( data != null ? "!= null (IE no XMLSerializer)" : "null" );
                            dojo.debug( "retrieveContent [" + ( dmId ? dmId : this.url ) + "] xml-content: " + textContent );
                        }
                    }
                }
                if ( this.contentListener && dojo.lang.isFunction( this.contentListener.notifySuccess ) )
                {
                    this.contentListener.notifySuccess( data, this.url, this.domainModelObject, http ) ;
                }
                else
                {
                    dmId = ( ( this.domainModelObject && dojo.lang.isFunction( this.domainModelObject.getId ) ) ? this.domainModelObject.getId() : "" );
                    dojo.debug( "retrieveContent [" + ( dmId ? dmId : this.url ) + "] no valid contentListener" );
                }
                if ( this.hideLoadingIndicator )
                    jetspeed.url.loadingIndicatorHide();
            }
            catch(e)
            {
                if ( this.hideLoadingIndicator )
                    jetspeed.url.loadingIndicatorHide();
                dojo.raise( "dojo.io.bind " + jetspeed.formatError( e ) );
            }
        },

        error: function( type, error )
        {
            //dojo.debug( "r e t r i e v e C o n t e n t . e r r o r" ) ;
            //dojo.debug( "  type:" );
            //dojo.debugShallow( type ) ;
            //dojo.debug( "  error:" );
            //dojo.debugShallow( error ) ;
            try
            {
                if ( this.contentListener && dojo.lang.isFunction( this.contentListener.notifyFailure ) )
                {
                    this.contentListener.notifyFailure( type, error, this.url, this.domainModelObject );
                }
                if ( this.hideLoadingIndicator )
                    jetspeed.url.loadingIndicatorHide();
            }
            catch(e)
            {
                if ( this.hideLoadingIndicator )
                    jetspeed.url.loadingIndicatorHide();
                throw e;
            }
        }
    });
    
    jetspeed.url.retrieveContent = function( bindArgs, contentListener, domainModelObject, debugContentDumpIds )
    {
        if ( ! bindArgs ) bindArgs = {};
        bindArgs.contentListener = contentListener ;
        bindArgs.domainModelObject = domainModelObject ;
        bindArgs.debugContentDumpIds = debugContentDumpIds ;
        
        var jetspeedBindArgs = new jetspeed.url.BindArgs( bindArgs );

        if ( bindArgs.showLoadingIndicator || ( contentListener && ! contentListener.suppressLoadingIndicator && bindArgs.showLoadingIndicator != false ) )
        {
            if ( jetspeed.url.loadingIndicatorShow() )
                jetspeedBindArgs.hideLoadingIndicator = true ;
        }
        dojo.io.bind( jetspeedBindArgs.createIORequest() ) ;
    };
    
    jetspeed.url.checkAjaxApiResponse = function( requestUrl, data, otherSuccessValues, reportError, apiRequestDescription, dumpOutput )
    {
        var success = false;
        var statusElmt = data.getElementsByTagName( "status" );
        if ( statusElmt != null )
        {
            var successVal = statusElmt[0].firstChild.nodeValue;
            if ( successVal == "success" )
            {
                success = successVal;
            }
            else if ( otherSuccessValues && otherSuccessValues.length > 0 )
            {
                for ( var i = 0 ; i < otherSuccessValues.length ; i++ )
                {
                    if ( successVal == otherSuccessValues[i] )
                    {
                        success = successVal;
                        break;
                    }
                }
            }
        }
        if ( ( ! success && reportError ) || dumpOutput )
        {
            var textContent = dojo.dom.innerXML( data );
            if ( ! textContent )
                textContent = ( data != null ? "!= null (IE no XMLSerializer)" : "null" );
            if ( apiRequestDescription == null )
                apiRequestDescription = "ajax-api";
            if ( success )
                dojo.debug( apiRequestDescription + " success  url=" + requestUrl + "  xml-content=" + textContent );
            else
                dojo.raise( apiRequestDescription + " failure  url=" + requestUrl + "  xml-content=" + textContent );
        }
        return success;
    };
    
    jetspeed.url._loadingImgUpdate = function( useStepImgs, resetNextStep, stepPreloadOnly, doc, jsPrefs, jsUrl )
    {
        var loadingProps = jsPrefs.loadingImgProps;
        if ( loadingProps )
        {
            var loading = doc.getElementById( jsUrl.LOADING_INDICATOR_ID );
            if ( loading == null || ! loading.style || loading.style.display == "none" )
                return;
            var imgAnimated = loadingProps.imganimated;
            var loadingImgElmt = doc.getElementById( jsUrl.LOADING_INDICATOR_IMG_ID );
            if ( imgAnimated && loadingImgElmt )
            {
                var imgBaseUrl = loadingProps._imgBaseUrl;
                if ( imgBaseUrl == null )
                {
                    var imgDir = loadingProps.imgdir;
                    if ( imgDir == null || imgDir.length == 0 )
                        imgBaseUrl = false;
                    else
                        imgBaseUrl = jsPrefs.getLayoutRootUrl() + imgDir;
                    loadingProps._imgBaseUrl = imgBaseUrl;
                }
                if ( imgBaseUrl )
                {
                    var srcSet = false;
                    if ( ( useStepImgs || stepPreloadOnly ) && ! loadingProps._stepDisabled )
                    {
                        var stepPrefix = loadingProps.imgstepprefix;
                        var stepExtn = loadingProps.imgstepextension;
                        var steps = loadingProps.imgsteps;
                        if ( stepPrefix && stepExtn && steps )
                        {
                            var nextStep = loadingProps._stepNext;
                            if ( resetNextStep || nextStep == null || nextStep >= steps.length )
                                nextStep = 0;
                            var imgStepBaseUrl = imgBaseUrl + "/" + stepPrefix;
                            if ( ! stepPreloadOnly )
                            {
                                loadingImgElmt.src = imgStepBaseUrl + steps[nextStep] + stepExtn;
                                srcSet = true;
                                loadingProps._stepNext = nextStep + 1;
                            }
                            else
                            {
                                var preloadImg, limit = Math.ceil( steps.length / 1.8 );
                                for ( var i = 0 ; i <= limit ; i++ )
                                {
                                    preloadImg = new Image();
                                    preloadImg.src = imgStepBaseUrl + steps[i] + stepExtn;
                                }
                            }
                        }
                        else
                        {
                            loadingProps._stepDisabled = true;
                        }
                    }
                    if ( ! srcSet && ! stepPreloadOnly )
                    {
                        loadingImgElmt.src = imgBaseUrl + "/" + imgAnimated;
                    }
                }
            }
        }
    };

    jetspeed.url.loadingIndicatorStep = function( jsObj )
    {
        var jsUrl = jsObj.url;
        jsUrl._loadingImgUpdate( true, false, false, document, jsObj.prefs, jsUrl );
    };

    jetspeed.url.loadingIndicatorStepPreload = function()
    {
        var jsObj = jetspeed;
        var jsUrl = jsObj.url;
        jsUrl._loadingImgUpdate( true, false, true, document, jsObj.prefs, jsUrl );
    };

    jetspeed.url.loadingIndicatorShow = function( actionName, useStepImgs )
    {
        var jsObj = jetspeed;
        var jsPrefs = jsObj.prefs;
        var jsUrl = jsObj.url;
        var doc = document;
        if ( typeof actionName == "undefined" )
            actionName = "loadpage";
        var loading = doc.getElementById( jsUrl.LOADING_INDICATOR_ID );
        if ( loading != null && loading.style )
        {
            var actionlabel = null;
            if ( jsPrefs != null && jsPrefs.desktopActionLabels != null )
                actionlabel = jsPrefs.desktopActionLabels[ actionName ];

            if ( actionlabel != null && actionlabel.length > 0 && loading.style[ "display" ] == "none" )
            {
                jsUrl._loadingImgUpdate( useStepImgs, true, false, doc, jsPrefs, jsUrl );

                loading.style[ "display" ] = "";

                if ( actionName != null )
                {
                    if ( actionlabel != null && actionlabel.length > 0 )
                    {
                        var loadingContent = doc.getElementById( jsUrl.LOADING_INDICATOR_ID + "-content" );
                        if ( loadingContent != null )
                        {
                            loadingContent.innerHTML = actionlabel;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    };
    jetspeed.url.loadingIndicatorHide = function()
    {
        var loading = document.getElementById( jetspeed.url.LOADING_INDICATOR_ID );
        if ( loading != null && loading.style )
            loading.style[ "display" ] = "none";
    };
}

jetspeed.widget.openDialog = function( dialogWidget )
{
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
};

jetspeed.initcommon();
