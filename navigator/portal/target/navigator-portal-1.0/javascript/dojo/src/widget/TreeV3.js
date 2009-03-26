
dojo.provide("dojo.widget.TreeV3");
dojo.require("dojo.widget.TreeWithNode");
dojo.require("dojo.widget.*");
dojo.require("dojo.event.*");
dojo.require("dojo.io.*");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.widget.TreeNodeV3");
dojo.widget.defineWidget(
"dojo.widget.TreeV3",
[dojo.widget.HtmlWidget, dojo.widget.TreeWithNode],
function() {this.eventNames = {};
this.DndAcceptTypes = [];
this.actionsDisabled = [];
this.listeners = [];
this.tree = this;},
{DndMode: "",
defaultChildWidget: null,
defaultChildTitle: "New Node",
eagerWidgetInstantiation: false,
eventNamesDefault: {afterTreeCreate: "afterTreeCreate",
beforeTreeDestroy: "beforeTreeDestroy",
beforeNodeDestroy: "beforeNodeDestroy",
afterChangeTree: "afterChangeTree",
afterSetFolder: "afterSetFolder",
afterUnsetFolder: "afterUnsetFolder",
beforeMoveFrom: "beforeMoveFrom",
beforeMoveTo: "beforeMoveTo",
afterMoveFrom: "afterMoveFrom",
afterMoveTo: "afterMoveTo",
afterAddChild: "afterAddChild",
afterDetach: "afterDetach",
afterExpand: "afterExpand",
beforeExpand: "beforeExpand",
afterSetTitle: "afterSetTitle",
afterCollapse: "afterCollapse",
beforeCollapse: "beforeCollapse"},
classPrefix: "Tree",
style: "",
allowAddChildToLeaf: true,
unsetFolderOnEmpty: true,
DndModes: {BETWEEN: 1,
ONTO: 2},
DndAcceptTypes: "",
templateCssPath: dojo.uri.dojoUri("src/widget/templates/TreeV3.css"),
templateString: '<div style="${this.style}">\n</div>',
isExpanded: true,
isTree: true,
createNode: function(data) {data.tree = this.widgetId;
if (data.widgetName) {return dojo.widget.createWidget(data.widgetName, data);} else if (this.defaultChildWidget.prototype.createSimple) {return this.defaultChildWidget.prototype.createSimple(data);} else {var ns = this.defaultChildWidget.prototype.ns;
var wt = this.defaultChildWidget.prototype.widgetType;
return dojo.widget.createWidget(ns + ":" + wt, data);}},
makeNodeTemplate: function() {var domNode = document.createElement("div");
dojo.html.setClass(domNode, this.classPrefix+"Node "+this.classPrefix+"ExpandLeaf "+this.classPrefix+"ChildrenNo");
this.nodeTemplate = domNode;
var expandNode = document.createElement("div");
var clazz = this.classPrefix+"Expand";
if (dojo.render.html.ie) {clazz = clazz + ' ' + this.classPrefix+"IEExpand";}
dojo.html.setClass(expandNode, clazz);
this.expandNodeTemplate = expandNode;
var labelNode = document.createElement("span");
dojo.html.setClass(labelNode, this.classPrefix+"Label");
this.labelNodeTemplate = labelNode;
var contentNode = document.createElement("div");
var clazz = this.classPrefix+"Content";
if (dojo.render.html.ie && !dojo.render.html.ie70) {clazz = clazz + ' ' + this.classPrefix+"IEContent";}
dojo.html.setClass(contentNode, clazz);
this.contentNodeTemplate = contentNode;
domNode.appendChild(expandNode);
domNode.appendChild(contentNode);
contentNode.appendChild(labelNode);},
makeContainerNodeTemplate: function() {var div = document.createElement('div');
div.style.display = 'none';
dojo.html.setClass(div, this.classPrefix+"Container");
this.containerNodeTemplate = div;},
actions: {ADDCHILD: "ADDCHILD"},
getInfo: function() {var info = {widgetId: this.widgetId,
objectId: this.objectId}
return info;},
adjustEventNames: function() {for(var name in this.eventNamesDefault) {if (dojo.lang.isUndefined(this.eventNames[name])) {this.eventNames[name] = this.widgetId+"/"+this.eventNamesDefault[name];}}},
adjustDndMode: function() {var _this = this;
var DndMode = 0;
dojo.lang.forEach(this.DndMode.split(';'),
function(elem) {var mode = _this.DndModes[dojo.string.trim(elem).toUpperCase()];
if (mode) DndMode = DndMode | mode;}
);
this.DndMode = DndMode;},
destroy: function() {dojo.event.topic.publish(this.tree.eventNames.beforeTreeDestroy, { source: this } );
return dojo.widget.HtmlWidget.prototype.destroy.apply(this, arguments);},
initialize: function(args){this.domNode.widgetId = this.widgetId;
for(var i=0; i<this.actionsDisabled.length;i++) {this.actionsDisabled[i] = this.actionsDisabled[i].toUpperCase();}
if (!args.defaultChildWidget) {this.defaultChildWidget = dojo.widget.TreeNodeV3;} else {this.defaultChildWidget = dojo.lang.getObjPathValue(args.defaultChildWidget);}
this.adjustEventNames();
this.adjustDndMode();
this.makeNodeTemplate();
this.makeContainerNodeTemplate();
this.containerNode = this.domNode;
dojo.html.setClass(this.domNode, this.classPrefix+"Container");
var _this = this;
dojo.lang.forEach(this.listeners,
function(elem) {var t = dojo.lang.isString(elem) ? dojo.widget.byId(elem) : elem;
t.listenTree(_this)}
);},
postCreate: function() {dojo.event.topic.publish(this.eventNames.afterTreeCreate, { source: this } );},
move: function(child, newParent, index) {if (!child.parent) {dojo.raise(this.widgetType+": child can be moved only while it's attached");}
var oldParent = child.parent;
var oldTree = child.tree;
var oldIndex = child.getParentIndex();
var newTree = newParent.tree;
var newParent = newParent;
var newIndex = index;
var message = {oldParent: oldParent, oldTree: oldTree, oldIndex: oldIndex,
newParent: newParent, newTree: newTree, newIndex: newIndex,
child: child};
dojo.event.topic.publish(oldTree.eventNames.beforeMoveFrom, message);
dojo.event.topic.publish(newTree.eventNames.beforeMoveTo, message);
this.doMove.apply(this, arguments);
dojo.event.topic.publish(oldTree.eventNames.afterMoveFrom, message);
dojo.event.topic.publish(newTree.eventNames.afterMoveTo, message);},
doMove: function(child, newParent, index) {child.doDetach();
newParent.doAddChild(child, index);},
toString: function() {return "["+this.widgetType+" ID:"+this.widgetId	+"]"}});
