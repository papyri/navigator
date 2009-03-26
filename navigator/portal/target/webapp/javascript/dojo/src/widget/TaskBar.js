
dojo.provide("dojo.widget.TaskBar");
dojo.require("dojo.widget.*");
dojo.require("dojo.widget.FloatingPane");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.event.*");
dojo.require("dojo.html.selection");
dojo.widget.defineWidget(
"dojo.widget.TaskBarItem",
dojo.widget.HtmlWidget,
{iconSrc: '',
caption: 'Untitled',
templatePath: dojo.uri.dojoUri("src/widget/templates/TaskBarItemTemplate.html"),
templateCssPath: dojo.uri.dojoUri("src/widget/templates/TaskBar.css"),
fillInTemplate: function() {if (this.iconSrc) {var img = document.createElement("img");
img.src = this.iconSrc;
this.domNode.appendChild(img);}
this.domNode.appendChild(document.createTextNode(this.caption));
dojo.html.disableSelection(this.domNode);},
postCreate: function() {this.window=dojo.widget.getWidgetById(this.windowId);
this.window.explodeSrc = this.domNode;
dojo.event.connect(this.window, "destroy", this, "destroy")},
onClick: function() {this.window.toggleDisplay();}});
dojo.widget.defineWidget(
"dojo.widget.TaskBar",
dojo.widget.FloatingPane,
function(){this._addChildStack = [];},
{resizable: false,
titleBarDisplay: false,
addChild: function( child) {if(!this.containerNode){this._addChildStack.push(child);}else if(this._addChildStack.length > 0){var oarr = this._addChildStack;
this._addChildStack = [];
dojo.lang.forEach(oarr, this.addChild, this);}
var tbi = dojo.widget.createWidget("TaskBarItem",
{	windowId: child.widgetId,
caption: child.title,
iconSrc: child.iconSrc});
dojo.widget.TaskBar.superclass.addChild.call(this,tbi);}});
