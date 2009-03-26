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
 */

dojo.provide("jetspeed.widget.SiteManagerTreeRPCController");

dojo.require("dojo.widget.TreeRPCController");

jetspeed.widget.SiteManagerTreeRPCController = function()
{    
    this.widgetType = "SiteManagerTreeRPCController";
    dojo.widget.TreeRPCController.call(this);
};

dojo.inherits( jetspeed.widget.SiteManagerTreeRPCController, dojo.widget.TreeRPCController );

dojo.lang.extend( jetspeed.widget.SiteManagerTreeRPCController,
{
    doRemoveNode: function(node, callObj, callFunc)
    {
        var args = [ node, callObj, callFunc ];
        dojo.widget.TreeLoadingController.prototype.doRemoveNode.apply(this, args);
    },
    doCreateChild: function(parent, index, output, callObj, callFunc)
    {
        if ( output == null ) output = {};
        var args = [parent, index, output, callObj, callFunc];
		dojo.widget.TreeLoadingController.prototype.doCreateChild.apply(this, args);
    }
});
