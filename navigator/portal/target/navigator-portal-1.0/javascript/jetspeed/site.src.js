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

// jetspeed javascript to help support site editor in both /portal and /desktop

if ( window.dojo )
{
    dojo.provide( "jetspeed.site" );
    dojo.require( "jetspeed.common" );
}

// ... jetspeed base objects
if ( ! window.jetspeed )
    jetspeed = {};
if ( ! jetspeed.site )
    jetspeed.site = {} ;


jetspeed.site.getFolders = function(data, handler)
{
    var contentListener = new jetspeed.site.FoldersListContentListener(handler);
    var queryString = "?action=getfolders&data=" + data;
    var getPortletsUrl = jetspeed.url.basePortalUrl() + jetspeed.url.path.AJAX_API + queryString ;
    var mimetype = "text/xml";
    var ajaxApiContext = new jetspeed.om.Id( "getfolders", { } );
    jetspeed.url.retrieveContent( { url: getPortletsUrl, mimetype: mimetype }, contentListener, ajaxApiContext, jetspeed.debugContentDumpIds );
};


// ... jetspeed.site.FoldersListContentListener

jetspeed.site.FoldersListContentListener = function(finishedFunction)
{
    this.notifyFinished = finishedFunction;
};

dojo.lang.extend( jetspeed.site.FoldersListContentListener,
{
    notifySuccess: function( /* XMLDocument */ data, /* String */ requestUrl, domainModelObject )
    {
        var folderlist = this.parseFolders( data );
        var pagesList = this.parsePages( data );
        var linksList = this.parseLinks( data );
        if ( dojo.lang.isFunction( this.notifyFinished ) )
        {
            this.notifyFinished( domainModelObject, folderlist,pagesList,linksList);
        }
    },

    notifyFailure: function( /* String */ type, /* String */ error, /* String */ requestUrl, domainModelObject )
    {
        dojo.raise( "FoldersListContentListener error [" + domainModelObject.toString() + "] url: " + requestUrl + " type: " + type + jetspeed.formatError( error ) );
    },    

    parseFolders: function( /* XMLNode */ node )
    {
        var folderlist = [];
        var jsElements = node.getElementsByTagName( "js" );
        if ( ! jsElements || jsElements.length > 1 )
            dojo.raise( "unexpected zero or multiple <js> elements in portlet selector xml" );

        var children = jsElements[0].childNodes;
        
        for ( var i = 0 ; i < children.length ; i++ )
        {
            var child = children[i];
            if ( child.nodeType != dojo.dom.ELEMENT_NODE )
                continue;
            var childLName = child.nodeName;
            if ( childLName == "folders" )
            {
                var portletsNode = child ;
                var portletChildren = portletsNode.childNodes ;
                for ( var pI = 0 ; pI < portletChildren.length ; pI++ )
                {
                    var pChild = portletChildren[pI];
                    if ( pChild.nodeType != dojo.dom.ELEMENT_NODE )
                        continue;

                    var pChildLName = pChild.nodeName;
                    if (pChildLName == "folder")
                    {
                        var folderdef = this.parsePortletElement( pChild );
                        folderlist.push( folderdef ) ;
                    }                   
                }
            }
        }
        return folderlist ;
    },

    parsePages: function( /* XMLNode */ node )
    {
        var pageslist = [];
        var jsElements = node.getElementsByTagName( "js" );
        if ( ! jsElements || jsElements.length > 1 )
            dojo.raise( "unexpected zero or multiple <js> elements in portlet selector xml" );

        var children = jsElements[0].childNodes;
        
        for ( var i = 0 ; i < children.length ; i++ )
        {
            var child = children[i];
            if ( child.nodeType != dojo.dom.ELEMENT_NODE )
                continue;

            var childLName = child.nodeName;
            if ( childLName == "folders" )
            {
                var portletsNode = child ;
                var portletChildren = portletsNode.childNodes ;
                for ( var pI = 0 ; pI < portletChildren.length ; pI++ )
                {
                    var pChild = portletChildren[pI];
                    if ( pChild.nodeType != dojo.dom.ELEMENT_NODE )
                        continue;

                    var pChildLName = pChild.nodeName;
                    if (pChildLName == "page")
                    {
                        var folderdef = this.parsePortletElement( pChild );
                        pageslist.push( folderdef ) ;
                    }
                    
                }
            }
        }
        return pageslist ;
    },

    parseLinks: function( /* XMLNode */ node )
    {
        var linkslist = [];
        var jsElements = node.getElementsByTagName( "js" );
        if ( ! jsElements || jsElements.length > 1 )
            dojo.raise( "unexpected zero or multiple <js> elements in portlet selector xml" );

        var children = jsElements[0].childNodes;
        
        for ( var i = 0 ; i < children.length ; i++ )
        {
            var child = children[i];
            if ( child.nodeType != dojo.dom.ELEMENT_NODE )
                continue;

            var childLName = child.nodeName;
            if ( childLName == "folders" )
            {
                var portletsNode = child ;
                var portletChildren = portletsNode.childNodes ;
                for ( var pI = 0 ; pI < portletChildren.length ; pI++ )
                {
                    var pChild = portletChildren[pI];
                    if ( pChild.nodeType != dojo.dom.ELEMENT_NODE )
                        continue;

                    var pChildLName = pChild.nodeName;
                    if (pChildLName == "link")
                    {
                        var folderdef = this.parsePortletElement( pChild );
                        linkslist.push( folderdef ) ;
                    }
                    
                }
            }
        }
        return linkslist ;
    },

    parsePortletElement: function( /* XMLNode */ node )
    {
        var folderName = node.getAttribute( "name" );
        var folderPath = node.getAttribute( "path" );
        return new jetspeed.site.FolderDef( folderName, folderPath) ;
    }
});


// ... jetspeed.site.FolderDef

jetspeed.site.FolderDef = function( /* String */ folderName, /* String */ folderPath)
{
    this.folderName = folderName;
    this.folderPath = folderPath;
};

dojo.inherits( jetspeed.site.FolderDef, jetspeed.om.Id);

dojo.lang.extend( jetspeed.site.FolderDef,
{
    folderName: null,
    folderPath: null,
    getName: function()  // jetspeed.om.Id protocol
    {
        return this.folderName;
    },
    getPath: function()
    {
        return this.folderPath;
    }
});
