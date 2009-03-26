
dojo.provide("dojo.widget.TreeDocIconExtension");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.widget.TreeExtension");
dojo.widget.defineWidget(
"dojo.widget.TreeDocIconExtension",
[dojo.widget.TreeExtension],
{templateCssPath: dojo.uri.dojoUri("src/widget/templates/TreeDocIcon.css"),
listenTreeEvents: ["afterChangeTree","afterSetFolder","afterUnsetFolder"],
listenNodeFilter: function(elem) { return elem instanceof dojo.widget.Widget },
getnodeDocType: function(node) {var nodeDocType = node.getnodeDocType();
if (!nodeDocType) {nodeDocType = node.isFolder ? "Folder" : "Document";}
return nodeDocType;},
setnodeDocTypeClass: function(node) {var reg = new RegExp("(^|\\s)"+node.tree.classPrefix+"Icon\\w+",'g');
var clazz = dojo.html.getClass(node.iconNode).replace(reg,'') + ' ' + node.tree.classPrefix+'Icon'+this.getnodeDocType(node);
dojo.html.setClass(node.iconNode, clazz);},
onAfterSetFolder: function(message) {if (message.source.iconNode) {this.setnodeDocTypeClass(message.source);}},
onAfterUnsetFolder: function(message) {this.setnodeDocTypeClass(message.source);},
listenNode: function(node) {node.contentIconNode = document.createElement("div");
var clazz = node.tree.classPrefix+"IconContent";
if (dojo.render.html.ie) {clazz = clazz+' '+ node.tree.classPrefix+"IEIconContent";}
dojo.html.setClass(node.contentIconNode, clazz);
node.contentNode.parentNode.replaceChild(node.contentIconNode, node.expandNode);
node.iconNode = document.createElement("div");
dojo.html.setClass(node.iconNode, node.tree.classPrefix+"Icon"+' '+node.tree.classPrefix+'Icon'+this.getnodeDocType(node));
node.contentIconNode.appendChild(node.expandNode);
node.contentIconNode.appendChild(node.iconNode);
dojo.dom.removeNode(node.contentNode);
node.contentIconNode.appendChild(node.contentNode);},
onAfterChangeTree: function(message) {var _this = this;
if (!message.oldTree || !this.listenedTrees[message.oldTree.widgetId]) {this.processDescendants(message.node,
this.listenNodeFilter,
this.listenNode
);}}});
