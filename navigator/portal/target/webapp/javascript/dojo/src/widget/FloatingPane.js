
dojo.provide("dojo.widget.FloatingPane");
dojo.require("dojo.widget.*");
dojo.require("dojo.widget.Manager");
dojo.require("dojo.html.*");
dojo.require("dojo.html.layout");
dojo.require("dojo.html.iframe");
dojo.require("dojo.html.selection");
dojo.require("dojo.lfx.shadow");
dojo.require("dojo.widget.html.layout");
dojo.require("dojo.widget.ContentPane");
dojo.require("dojo.dnd.HtmlDragMove");
dojo.require("dojo.widget.Dialog");
dojo.require("dojo.widget.ResizeHandle");
dojo.declare(
"dojo.widget.FloatingPaneBase",
null,
{title: '',
iconSrc: '',
hasShadow: false,
constrainToContainer: false,
taskBarId: "",
resizable: true,
titleBarDisplay: true,
windowState: "normal",
displayCloseAction: false,
displayMinimizeAction: false,
displayMaximizeAction: false,
_max_taskBarConnectAttempts: 5,
_taskBarConnectAttempts: 0,
templatePath: dojo.uri.dojoUri("src/widget/templates/FloatingPane.html"),
templateCssPath: dojo.uri.dojoUri("src/widget/templates/FloatingPane.css"),
fillInFloatingPaneTemplate: function(args, frag){var source = this.getFragNodeRef(frag);
dojo.html.copyStyle(this.domNode, source);
dojo.body().appendChild(this.domNode);
if(!this.isShowing()){this.windowState="minimized";}
if(this.iconSrc==""){dojo.html.removeNode(this.titleBarIcon);}else{this.titleBarIcon.src = this.iconSrc.toString();}
if(this.titleBarDisplay){this.titleBar.style.display="";
dojo.html.disableSelection(this.titleBar);
this.titleBarIcon.style.display = (this.iconSrc=="" ? "none" : "");
this.minimizeAction.style.display = (this.displayMinimizeAction ? "" : "none");
this.maximizeAction.style.display=
(this.displayMaximizeAction && this.windowState!="maximized" ? "" : "none");
this.restoreAction.style.display=
(this.displayMaximizeAction && this.windowState=="maximized" ? "" : "none");
this.closeAction.style.display= (this.displayCloseAction ? "" : "none");
this.drag = new dojo.dnd.HtmlDragMoveSource(this.domNode);
if (this.constrainToContainer) {this.drag.constrainTo();}
this.drag.setDragHandle(this.titleBar);
var self = this;
dojo.event.topic.subscribe("dragMove",
function (info){if (info.source.domNode == self.domNode){dojo.event.topic.publish('floatingPaneMove', { source: self } );}}
);}
if(this.resizable){this.resizeBar.style.display="";
this.resizeHandle = dojo.widget.createWidget("ResizeHandle", {targetElmId: this.widgetId, id:this.widgetId+"_resize"});
this.resizeBar.appendChild(this.resizeHandle.domNode);}
if(this.hasShadow){this.shadow=new dojo.lfx.shadow(this.domNode);}
this.bgIframe = new dojo.html.BackgroundIframe(this.domNode);
if( this.taskBarId ){this._taskBarSetup();}
dojo.body().removeChild(this.domNode);},
postCreate: function(){if (dojo.hostenv.post_load_) {this._setInitialWindowState();} else {dojo.addOnLoad(this, "_setInitialWindowState");}},
maximizeWindow: function( evt) {var mb = dojo.html.getMarginBox(this.domNode);
this.previous={width: mb.width || this.width,
height: mb.height || this.height,
left: this.domNode.style.left,
top: this.domNode.style.top,
bottom: this.domNode.style.bottom,
right: this.domNode.style.right};
if(this.domNode.parentNode.style.overflow.toLowerCase() != 'hidden'){this.parentPrevious={overflow: this.domNode.parentNode.style.overflow};
dojo.debug(this.domNode.parentNode.style.overflow);
this.domNode.parentNode.style.overflow = 'hidden';}
this.domNode.style.left =
dojo.html.getPixelValue(this.domNode.parentNode, "padding-left", true) + "px";
this.domNode.style.top =
dojo.html.getPixelValue(this.domNode.parentNode, "padding-top", true) + "px";
if ((this.domNode.parentNode.nodeName.toLowerCase() == 'body')) {var viewport = dojo.html.getViewport();
var padding = dojo.html.getPadding(dojo.body());
this.resizeTo(viewport.width-padding.width, viewport.height-padding.height);} else {var content = dojo.html.getContentBox(this.domNode.parentNode);
this.resizeTo(content.width, content.height);}
this.maximizeAction.style.display="none";
this.restoreAction.style.display="";
if(this.resizeHandle){this.resizeHandle.domNode.style.display="none";}
this.drag.setDragHandle(null);
this.windowState="maximized";},
minimizeWindow: function( evt) {this.hide();
for(var attr in this.parentPrevious){this.domNode.parentNode.style[attr] = this.parentPrevious[attr];}
this.lastWindowState = this.windowState;
this.windowState = "minimized";},
restoreWindow: function( evt) {if (this.windowState=="minimized") {this.show();
if(this.lastWindowState == "maximized"){this.domNode.parentNode.style.overflow = 'hidden';
this.windowState="maximized";}else{this.windowState="normal";}} else if (this.windowState=="maximized"){for(var attr in this.previous){this.domNode.style[attr] = this.previous[attr];}
for(var attr in this.parentPrevious){this.domNode.parentNode.style[attr] = this.parentPrevious[attr];}
this.resizeTo(this.previous.width, this.previous.height);
this.previous=null;
this.parentPrevious=null;
this.restoreAction.style.display="none";
this.maximizeAction.style.display=this.displayMaximizeAction ? "" : "none";
if(this.resizeHandle){this.resizeHandle.domNode.style.display="";}
this.drag.setDragHandle(this.titleBar);
this.windowState="normal";} else {}},
toggleDisplay: function(){if(this.windowState=="minimized"){this.restoreWindow();}else{this.minimizeWindow();}},
closeWindow: function( evt) {dojo.html.removeNode(this.domNode);
this.destroy();},
onMouseDown: function( evt) {this.bringToTop();},
bringToTop: function() {var floatingPanes= dojo.widget.manager.getWidgetsByType(this.widgetType);
var windows = [];
for (var x=0; x<floatingPanes.length; x++) {if (this.widgetId != floatingPanes[x].widgetId) {windows.push(floatingPanes[x]);}}
windows.sort(function(a,b) {return a.domNode.style.zIndex - b.domNode.style.zIndex;});
windows.push(this);
var floatingPaneStartingZ = 100;
for (x=0; x<windows.length;x++) {windows[x].domNode.style.zIndex = floatingPaneStartingZ + x*2;}},
_setInitialWindowState: function() {if(this.isShowing()){this.width=-1;
var mb = dojo.html.getMarginBox(this.domNode);
this.resizeTo(mb.width, mb.height);}
if (this.windowState == "maximized") {this.maximizeWindow();
this.show();
return;}
if (this.windowState=="normal") {this.show();
return;}
if (this.windowState=="minimized") {this.hide();
return;}
this.windowState="minimized";},
_taskBarSetup: function() {var taskbar = dojo.widget.getWidgetById(this.taskBarId);
if (!taskbar){if (this._taskBarConnectAttempts <  this._max_taskBarConnectAttempts) {dojo.lang.setTimeout(this, this._taskBarSetup, 50);
this._taskBarConnectAttempts++;} else {dojo.debug("Unable to connect to the taskBar");}
return;}
taskbar.addChild(this);},
showFloatingPane: function(){this.bringToTop();},
onFloatingPaneShow: function(){var mb = dojo.html.getMarginBox(this.domNode);
this.resizeTo(mb.width, mb.height);},
resizeTo: function( width,  height){dojo.html.setMarginBox(this.domNode, { width: width, height: height });
dojo.widget.html.layout(this.domNode,
[
{domNode: this.titleBar, layoutAlign: "top"},
{domNode: this.resizeBar, layoutAlign: "bottom"},
{domNode: this.containerNode, layoutAlign: "client"}
] );
dojo.widget.html.layout(this.containerNode, this.children, "top-bottom");
this.bgIframe.onResized();
if(this.shadow){ this.shadow.size(width, height); }
this.onResized();},
checkSize: function() {},
destroyFloatingPane: function() {if(this.resizeHandle){this.resizeHandle.destroy();
this.resizeHandle = null;}}}
);
dojo.widget.defineWidget(
"dojo.widget.FloatingPane",
[dojo.widget.ContentPane, dojo.widget.FloatingPaneBase],
{fillInTemplate: function(args, frag){this.fillInFloatingPaneTemplate(args, frag);
dojo.widget.FloatingPane.superclass.fillInTemplate.call(this, args, frag);},
postCreate: function(){dojo.widget.FloatingPaneBase.prototype.postCreate.apply(this, arguments);
dojo.widget.FloatingPane.superclass.postCreate.apply(this, arguments);},
show: function(){dojo.widget.FloatingPane.superclass.show.apply(this, arguments);
this.showFloatingPane();},
onShow: function(){dojo.widget.FloatingPane.superclass.onShow.call(this);
this.onFloatingPaneShow();},
destroy: function(){this.destroyFloatingPane();
dojo.widget.FloatingPane.superclass.destroy.apply(this, arguments);}});
dojo.widget.defineWidget(
"dojo.widget.ModalFloatingPane",
[dojo.widget.FloatingPane, dojo.widget.ModalDialogBase],
{windowState: "minimized",
displayCloseAction: true,
postCreate: function(){dojo.widget.ModalDialogBase.prototype.postCreate.call(this);
dojo.widget.ModalFloatingPane.superclass.postCreate.call(this);},
show: function(){this.showModalDialog();
dojo.widget.ModalFloatingPane.superclass.show.apply(this, arguments);
this.bg.style.zIndex = this.domNode.style.zIndex-1;},
hide: function(){this.hideModalDialog();
dojo.widget.ModalFloatingPane.superclass.hide.apply(this, arguments);},
closeWindow: function(){this.hide();
dojo.widget.ModalFloatingPane.superclass.closeWindow.apply(this, arguments);}}
);
