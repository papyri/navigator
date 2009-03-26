dojo.provide("jetspeed.widget.PortalAccordionContainer");
dojo.provide("jetspeed.widget.PortalMenuOptionLink");
dojo.require("jetspeed.desktop.core");
dojo.require("dojo.widget.*");
dojo.require("dojo.widget.AccordionContainer");
jetspeed.widget.PortalAccordionContainer=function(){
this.widgetType="PortalAccordionContainer";
this.isContainer=true;
this.templateString="<div dojoAttachPoint=\"containerNode\" class=\"toolgroup\"></div>";
dojo.widget.HtmlWidget.call(this);
};
dojo.inherits(jetspeed.widget.PortalAccordionContainer,dojo.widget.HtmlWidget);
dojo.lang.extend(jetspeed.widget.PortalAccordionContainer,{postMixInProperties:function(_1,_2,_3){
this.templateCssPath=new dojo.uri.Uri(jetspeed.prefs.getLayoutRootUrl()+"/css/PortalAccordionContainer.css");
jetspeed.widget.PortalAccordionContainer.superclass.postMixInProperties.call(this,_1,_2,_3);
},createAndAddPane:function(_4,_5){
if(!_5){
_5={};
}
if(_4){
_5.label=_4.getText();
if(_4.getHidden()){
_5.open=false;
}else{
_5.open=true;
}
_5.labelNodeClass="label";
_5.containerNodeClass="FolderList";
_5.templatePath=new dojo.uri.Uri(jetspeed.url.basePortalDesktopUrl()+"/javascript/jetspeed/widget/TitlePane.html");
_5.allowCollapse=false;
}
var _6=dojo.widget.createWidget("AccordionPane",_5);
this.addChild(_6);
return _6;
},addLinksToPane:function(_7,_8){
if(!_8||!_7){
return;
}
var _9;
for(var i=0;i<_8.length;i++){
_9=dojo.widget.createWidget("jetspeed:PortalMenuOptionLink",{menuOption:_8[i]});
_7.addChild(_9);
}
},createJetspeedMenu:function(_b){
if(!_b){
return;
}
if(this.children&&this.children.length>0){
for(var i=(this.children.length-1);i>=0;i--){
this.children[i].destroy();
}
}
var _d=_b.getOptions();
var _e=[],_f=null,_10=null,_11=0;
while(_e!=null){
_10=null;
if(_11<_d.length){
_10=_d[_11];
_11++;
}
if(_10==null||_10.isSeparator()){
if(_e!=null&&_e.length>0){
var _12=this.createAndAddPane(_f);
this.addLinksToPane(_12,_e);
}
_f=null;
_e=null;
if(_10!=null){
_f=_10;
_e=[];
}
}else{
if(_10.isLeaf()&&_10.getUrl()){
_e.push(_10);
}
}
}
},selectChild:function(_13){
}});
jetspeed.widget.PortalMenuOptionLink=function(){
dojo.widget.HtmlWidget.call(this);
this.widgetType="PortalMenuOptionLink";
this.templateString="<div dojoAttachPoint=\"containerNode\"><a href=\"\" dojoAttachPoint=\"menuOptionLinkNode\" dojoAttachEvent=\"onClick\" class=\"Link\"></a></div>";
};
dojo.inherits(jetspeed.widget.PortalMenuOptionLink,dojo.widget.HtmlWidget);
dojo.lang.extend(jetspeed.widget.PortalMenuOptionLink,{fillInTemplate:function(){
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

