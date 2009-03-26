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

dojo.provide("jetspeed.widget.MultiRowHeaderTable");

dojo.require("dojo.widget.SortableTable");

jetspeed.widget.MultiRowHeaderTable = function()
{
    dojo.widget.SortableTable.call(this);
    this.widgetType = "MultiRowHeaderTable";
    this.headerSortUpClass="selectedUp";
	this.headerSortDownClass="selectedDown";
};

dojo.inherits( jetspeed.widget.MultiRowHeaderTable, dojo.widget.SortableTable);

dojo.lang.extend( jetspeed.widget.MultiRowHeaderTable, {

    buildRendering: function(args, frag)
    {
        jetspeed.widget.MultiRowHeaderTable.superclass.buildRendering.call( this, args, frag );
        if ( args.templateCssPath )
        {
            dojo.widget.fillFromTemplateCache(this, null, dojo.uri.dojoUri(args.templateCssPath), null, false);
        }
    },

    parseColumns:function(/* HTMLTableHeadElement */ node){
        //	summary
		//	parses the passed element to create column objects
		this.reset();
        var rows=node.getElementsByTagName("tr");
        if (rows && rows.length > 0){
            for(var rowI=0; rowI<rows.length; rowI++){
                var cells=rows[rowI].getElementsByTagName("td");
		        if (cells.length==0) cells=rows[rowI].getElementsByTagName("th");

                for(var i=0; i<cells.length; i++){
                    var isCol=true;
                    if(dojo.html.hasAttribute(cells[i], "colspan")){
                        colspan=dojo.html.getAttribute(cells[i],"colspan");
                        if(colspan > 1)
                            isCol=false;
                    }
                    if(isCol){
                        var o={
                            domNode:cells[i],
				            field:null,
				            format:null,
				            noSort:false,
				            sortType:"String",
				            dataType:String,
				            sortFunction:null,
				            label:null,
				            align:"left",
				            valign:"middle",
				            getField:function(){ return this.field||this.label; },
				            getType:function(){ return this.dataType; }
			            };
			            //	presentation attributes
			            if(dojo.html.hasAttribute(cells[i], "align")){
				            o.align=dojo.html.getAttribute(cells[i],"align");
			            }
			            if(dojo.html.hasAttribute(cells[i], "valign")){
				            o.valign=dojo.html.getAttribute(cells[i],"valign");
			            }

			            //	sorting features.
			            if(dojo.html.hasAttribute(cells[i], "nosort")){
				            o.noSort=dojo.html.getAttribute(cells[i],"nosort")=="true";
			            }
			            if(dojo.html.hasAttribute(cells[i], "sortusing")){
				            var trans=dojo.html.getAttribute(cells[i],"sortusing");
				            var f=this.getTypeFromString(trans);
				            if (f!=null && f!=window && typeof(f)=="function") 
					            o.sortFunction=f;
			            }

			            if(dojo.html.hasAttribute(cells[i], "field")){
				            o.field=dojo.html.getAttribute(cells[i],"field");
			            }
			            if(dojo.html.hasAttribute(cells[i], "format")){
				            o.format=dojo.html.getAttribute(cells[i],"format");
			            }
			            if(dojo.html.hasAttribute(cells[i], "dataType")){
				            var sortType=dojo.html.getAttribute(cells[i],"dataType");
				            if(sortType.toLowerCase()=="html"||sortType.toLowerCase()=="markup"){
					            o.sortType="__markup__";	//	always convert to "__markup__"
					            o.noSort=true;
				            }else{
					            var type=this.getTypeFromString(sortType);
					            if(type){
						            o.sortType=sortType;
						            o.dataType=type;
					            }
				            }
			            }
			            o.label=dojo.html.renderedTextContent(cells[i]);
			            this.columns.push(o);

                        cells[i].className=this.headerClass;

			            //	check to see if there's a default sort, and set the properties necessary
			            if(dojo.html.hasAttribute(cells[i], "sort")){
				            this.sortIndex=i;
				            var dir=dojo.html.getAttribute(cells[i], "sort");
				            if(!isNaN(parseInt(dir))){
					            dir=parseInt(dir);
					            this.sortDirection=(dir!=0)?1:0;
				            }else{
					            this.sortDirection=(dir.toLowerCase()=="desc")?1:0;
				            }
			            }

                        if(!o.noSort){
				            dojo.event.connect(cells[i], "onclick", this, "onHeaderClick");
			            }
			            if(this.sortIndex == i && rowI == 0){
				            if(this.sortDirection==0){
					            cells[i].className=this.headerSortDownClass;
				            }else{
					            cells[i].className=this.headerSortUpClass;
                            }
				        }
                    }
                }
            }
        }
    },

	onHeaderClick:function(/* DomEvent */ e){
		//	summary
		//	Main handler function for each header column click.
		var oldIndex=this.sortIndex;
		var oldDirection=this.sortDirection;
		var source=e.target;
		for(var i=0; i<this.columns.length; i++){
			if(this.columns[i].domNode==source){
				if(i!=oldIndex){
					//	new col.
					this.sortIndex=i;
					this.sortDirection=0;
					this.columns[i].domNode.className=this.headerSortDownClass
				}else{
					this.sortDirection=(oldDirection==0)?1:0;
					if(this.sortDirection==0){
						this.columns[i].domNode.className=this.headerSortDownClass;
					}else{
						this.columns[i].domNode.className=this.headerSortUpClass;
					}
				}
			}else{
				//	reset the header class.
				this.columns[i].domNode.className=this.headerClass;
			}
		}
		this.render();
	},

	postCreate:function(){ 
		// 	summary
		//	overridden from HtmlWidget, initializes and renders the widget.
		var thead=this.domNode.getElementsByTagName("thead")[0];
		if(this.headClass.length>0){
			thead.className=this.headClass;
		}

		//	disable selections
		dojo.html.disableSelection(this.domNode);

		//	parse the columns.
		this.parseColumns(thead);

		//	attach header handlers.
		

		//	parse the tbody element and re-render it.
		var tbody=this.domNode.getElementsByTagName("tbody")[0];
		if (this.tbodyClass.length>0) {
			tbody.className=this.tbodyClass;
		}

		this.parseDataFromTable(tbody);
		this.render(true);
	}

});
