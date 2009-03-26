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

// jetspeed javascript to help support portlet-selector in both /portal and /desktop

if ( window.dojo )
{
    dojo.provide( "jetspeed.selector" );
    dojo.require( "jetspeed.common" );
}

// ... jetspeed base objects
if ( ! window.jetspeed )
    jetspeed = {};
if ( ! jetspeed.selector )
    jetspeed.selector = {} ;

// ... jetspeed.selector.PortletDef
jetspeed.selector.PortletDef = function( /* String */ portletName, /* String */ portletDisplayName, /* String */ portletDescription, /* String */ portletImage,portletCount)
{
    this.portletName = portletName;
    this.portletDisplayName = portletDisplayName;
    this.portletDescription = portletDescription;
    this.image = portletImage;
	this.count = portletCount;
};
jetspeed.selector.PortletDef.prototype =
{
    portletName: null,
    portletDisplayName: null,
    portletDescription: null,
    portletImage: null,
	portletCount: null,
    getId: function()
    {
        return this.portletName;
    },
    getPortletName: function()
    {
        return this.portletName;
    },
    getPortletDisplayName: function()
    {
        return this.portletDisplayName;
    },
	getPortletCount: function()
    {
        return this.portletCount;
    },
    getPortletDescription: function()
    {
        return this.portletDescription;
    }
};

jetspeed.selector.addNewPortletDefinition = function( /* jetspeed.selector.PortletDef */ portletDef, /* String */ psmlUrl, /* String */ layoutId )
{
    var contentListener = new jetspeed.selector.PortletAddAjaxApiCallbackCL( portletDef );
    var queryString = "?action=add&id=" + escape( portletDef.getPortletName() );
    if ( layoutId != null && layoutId.length > 0 )
    {
        queryString += "&layoutid=" + escape( layoutId );
    }
    var addPortletUrl = psmlUrl + queryString;   //  psmlUrl example: http://localhost:8080/jetspeed/ajaxapi/google-maps.psml
    var mimetype = "text/xml";
    var ajaxApiContext = new jetspeed.om.Id( "addportlet", { } );
    jetspeed.url.retrieveContent( { url: addPortletUrl, mimetype: mimetype }, contentListener, ajaxApiContext, jetspeed.debugContentDumpIds );
};

// ... jetspeed.selector.PortletAddAjaxApiCallbackCL
jetspeed.selector.PortletAddAjaxApiCallbackCL = function( /* jetspeed.selector.PortletDef */ portletDef )
{
    this.portletDef = portletDef;
};
jetspeed.selector.PortletAddAjaxApiCallbackCL.prototype =
{
    notifySuccess: function( /* XMLDocument */ data, /* String */ requestUrl, domainModelObject )
    {
        var jsObj = jetspeed;
        var successIndicator = jsObj.url.checkAjaxApiResponse( requestUrl, data, [ "refresh" ], true, "add-portlet" );
        if ( successIndicator == "refresh" && jsObj.page != null )
        {
            var navUrl = jsObj.page.getPageUrl();
            if ( navUrl != null )
            {
                if ( ! jsObj.prefs.ajaxPageNavigation )
                {
                    jsObj.pageNavigate( navUrl, null, true );
                }
                else
                {
                    jsObj.updatePage( navUrl, false, true );
                }
            }
        }
    },
    notifyFailure: function( /* String */ type, /* Object */ error, /* String */ requestUrl, domainModelObject )
    {
        dojo.raise( "PortletAddAjaxApiCallbackCL error [" + domainModelObject.toString() + "] url: " + requestUrl + " type: " + type + jetspeed.formatError( error ) );
    }
};
