dojo.provide("jetspeed.widget.PortalBreadcrumbContainer");
dojo.provide("jetspeed.widget.PortalBreadcrumbLink");
dojo.provide("jetspeed.widget.PortalBreadcrumbLinkSeparator");
dojo.require("jetspeed.desktop.core");
dojo.require("dojo.widget.*");
dojo.require("dojo.widget.TabContainer");
jetspeed.widget.PortalBreadcrumbContainer=function(){
this.widgetType="PortalBreadcrumbContainer";
this.isContainer=true;
dojo.widget.HtmlWidget.call(this);
};
dojo.inherits(jetspeed.widget.PortalBreadcrumbContainer,dojo.widget.HtmlWidget);
dojo.lang.extend(jetspeed.widget.PortalBreadcrumbContainer,{postMixInProperties:function(_1,_2,_3){
this.templateCssPath=new dojo.uri.Uri(jetspeed.prefs.getLayoutRootUrl()+"/css/PortalBreadcrumbContainer.css");
this.templatePath=new dojo.uri.dojoUri(jetspeed.prefs.getLayoutRootUrl()+"/templates/PortalBreadcrumbContainer.html");
jetspeed.widget.PortalBreadcrumbContainer.superclass.postMixInProperties.call(this,_1,_2,_3);
},fillInTemplate:function(_4,_5){
var _6=this.getFragNodeRef(_5);
dojo.html.copyStyle(this.domNode,_6);
jetspeed.widget.PortalBreadcrumbContainer.superclass.fillInTemplate.apply(this,arguments);
},createJetspeedMenu:function(_7){
if(!_7){
return;
}
if(this.containerNode.childNodes&&this.containerNode.childNodes.length>0){
for(var i=(this.containerNode.childNodes.length-1);i>=0;i--){
dojo.dom.removeNode(this.containerNode.childNodes[i]);
}
}
var _9=_7.getOptions();
var _a=[],_b=null;
for(var i=0;i<_9.length;i++){
_b=_9[i];
if(_b!=null&&!_b.isSeparator()){
_a.push(_b);
}
}
if(_a!=null&&_a.length>0){
var _c,_d;
var _e=_a.length;
for(var i=0;i<_e;i++){
if(i>0){
_d=dojo.widget.createWidget("jetspeed:PortalBreadcrumbLinkSeparator");
this.containerNode.appendChild(_d.domNode);
}
if(i==(_e-1)){
var _f=document.createElement("span");
_f.appendChild(document.createTextNode(_a[i].getShortTitle()));
this.containerNode.appendChild(_f);
}else{
_c=dojo.widget.createWidget("jetspeed:PortalBreadcrumbLink",{menuOption:_a[i]});
this.containerNode.appendChild(_c.domNode);
}
}
}
}});
jetspeed.widget.PortalBreadcrumbLink=function(){
dojo.widget.HtmlWidget.call(this);
this.widgetType="PortalBreadcrumbLink";
this.templateString="<span dojoAttachPoint=\"containerNode\"><a href=\"\" dojoAttachPoint=\"menuOptionLinkNode\" dojoAttachEvent=\"onClick\" class=\"Link\"></a></span>";
};
dojo.inherits(jetspeed.widget.PortalBreadcrumbLink,dojo.widget.HtmlWidget);
dojo.lang.extend(jetspeed.widget.PortalBreadcrumbLink,{fillInTemplate:function(){
if(this.menuOption.type=="page"){
this.menuOptionLinkNode.className="LinkPage";
}else{
if(this.menuOption.type=="folder"){
this.menuOptionLinkNode.className="LinkFolder";
}
}
if(this.iconSrc){
var img=document.createElement("img");
img.src=this.iconSrc;
this.menuOptionLinkNode.appendChild(img);
}
this.menuOptionLinkNode.href=this.menuOption.navigateUrl();
this.menuOptionLinkNode.appendChild(document.createTextNode(this.menuOption.getShortTitle()));
dojo.html.disableSelection(this.domNode);
},onClick:function(evt){
this.menuOption.navigateTo();
dojo.event.browser.stopEvent(evt);
}});
jetspeed.widget.PortalBreadcrumbLinkSeparator=function(){
dojo.widget.HtmlWidget.call(this);
this.widgetType="PortalBreadcrumbLinkSeparator";
this.templatePath=new dojo.uri.dojoUri(jetspeed.prefs.getLayoutRootUrl()+"/templates/PortalBreadcrumbLinkSeparator.html");
};
dojo.inherits(jetspeed.widget.PortalBreadcrumbLinkSeparator,dojo.widget.HtmlWidget);

