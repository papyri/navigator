
dojo.provide("dojo.widget.AccordionContainer");
dojo.require("dojo.widget.*");
dojo.require("dojo.html.*");
dojo.require("dojo.lfx.html");
dojo.require("dojo.html.selection");
dojo.require("dojo.widget.html.layout");
dojo.require("dojo.widget.PageContainer");
dojo.widget.defineWidget(
"dojo.widget.AccordionContainer",
dojo.widget.HtmlWidget,
{isContainer: true,
labelNodeClass: "label",
containerNodeClass: "accBody",
duration: 250,
fillInTemplate: function(){with(this.domNode.style){if(position!="absolute"){position="relative";}
overflow="hidden";}},
addChild: function( widget){var child = this._addChild(widget);
this._setSizes();
return child;},
_addChild: function( widget){if(widget.open){dojo.deprecated("open parameter deprecated, use 'selected=true' instead will be removed in ", "0.5");
dojo.debug(widget.widgetId + ": open == " + widget.open);
widget.selected=true;}
if (widget.widgetType != "AccordionPane") {var wrapper=dojo.widget.createWidget("AccordionPane",{label: widget.label, selected: widget.selected, labelNodeClass: this.labelNodeClass, containerNodeClass: this.containerNodeClass, allowCollapse: this.allowCollapse });
wrapper.addChild(widget);
this.addWidgetAsDirectChild(wrapper);
this.registerChild(wrapper, this.children.length);
return wrapper;} else {dojo.html.addClass(widget.containerNode, this.containerNodeClass);
dojo.html.addClass(widget.labelNode, this.labelNodeClass);
this.addWidgetAsDirectChild(widget);
this.registerChild(widget, this.children.length);
return widget;}},
postCreate: function() {var tmpChildren = this.children;
this.children=[];
dojo.html.removeChildren(this.domNode);
dojo.lang.forEach(tmpChildren, dojo.lang.hitch(this,"_addChild"));
this._setSizes();},
removeChild: function( widget) {dojo.widget.AccordionContainer.superclass.removeChild.call(this, widget);
this._setSizes();},
onResized: function(){this._setSizes();},
_setSizes: function() {var totalCollapsedHeight = 0;
var openIdx = 0;
dojo.lang.forEach(this.children, function(child, idx){totalCollapsedHeight += child.getLabelHeight();
if(child.selected){ openIdx=idx; }});
var mySize=dojo.html.getContentBox(this.domNode);
var y = 0;
dojo.lang.forEach(this.children, function(child, idx){var childCollapsedHeight = child.getLabelHeight();
child.resizeTo(mySize.width, mySize.height-totalCollapsedHeight+childCollapsedHeight);
child.domNode.style.zIndex=idx+1;
child.domNode.style.position="absolute";
child.domNode.style.top = y+"px";
y += (idx==openIdx) ? dojo.html.getBorderBox(child.domNode).height : childCollapsedHeight;});},
selectChild: function( page){dojo.lang.forEach(this.children, function(child){child.setSelected(child==page);});
var y = 0;
var anims = [];
dojo.lang.forEach(this.children, function(child, idx){if(child.domNode.style.top != (y+"px")){anims.push(dojo.lfx.html.slideTo(child.domNode, {top: y, left: 0}, this.duration));}
y += child.selected ? dojo.html.getBorderBox(child.domNode).height : child.getLabelHeight();}, this);
dojo.lfx.combine(anims).play();}}
);
dojo.widget.defineWidget(
"dojo.widget.AccordionPane",
dojo.widget.HtmlWidget,
{label: "",
"class": "dojoAccordionPane",
labelNodeClass: "label",
containerNodeClass: "accBody",
selected: false,
templatePath: dojo.uri.dojoUri("src/widget/templates/AccordionPane.html"),
templateCssPath: dojo.uri.dojoUri("src/widget/templates/AccordionPane.css"),
isContainer: true,
fillInTemplate: function() {dojo.html.addClass(this.domNode, this["class"]);
dojo.widget.AccordionPane.superclass.fillInTemplate.call(this);
dojo.html.disableSelection(this.labelNode);
this.setSelected(this.selected);},
setLabel: function( label) {this.labelNode.innerHTML=label;},
resizeTo: function(width, height){dojo.html.setMarginBox(this.domNode, {width: width, height: height});
var children = [
{domNode: this.labelNode, layoutAlign: "top"},
{domNode: this.containerNode, layoutAlign: "client"}
];
dojo.widget.html.layout(this.domNode, children);
var childSize = dojo.html.getContentBox(this.containerNode);
this.children[0].resizeTo(childSize.width, childSize.height);},
getLabelHeight: function() {return dojo.html.getMarginBox(this.labelNode).height;},
onLabelClick: function() {this.parent.selectChild(this);},
setSelected: function( isSelected){this.selected=isSelected;
(isSelected ? dojo.html.addClass : dojo.html.removeClass)(this.domNode, this["class"]+"-selected");
var child = this.children[0];
if(child){if(isSelected){if(!child.isShowing()){child.show();}else{child.onShow();}}else{child.onHide();}}}});
dojo.lang.extend(dojo.widget.Widget, {open: false});
