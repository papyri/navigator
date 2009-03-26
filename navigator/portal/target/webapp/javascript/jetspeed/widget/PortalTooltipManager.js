dojo.provide("jetspeed.widget.PortalTooltipManager");
dojo.require("dojo.widget.PopupContainer");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.uri.Uri");
dojo.require("dojo.widget.*");
dojo.require("dojo.event.*");
dojo.require("dojo.html.style");
dojo.require("dojo.html.util");
dojo.widget.defineWidget("jetspeed.widget.PortalTooltipManager",[dojo.widget.HtmlWidget,dojo.widget.PopupContainerBase],function(){
this.connections=[];
},{startNorm:1,startForce:2,startAbort:3,templateString:"<div dojoAttachPoint=\"containerNode\" style=\"display:none;position:absolute;\" class=\"portalTooltip\" ></div>",fillInTemplate:function(_1,_2){
var _3=this.getFragNodeRef(_2);
dojo.html.copyStyle(this.domNode,_3);
this.applyPopupBasicStyle();
},_setCurrent:function(_4){
var _5=this.startNorm;
var _6=this._curr;
if(_6!=null){
if(!(_6.connectNode.parentNode===_4.connectNode)){
_6.close();
}else{
if(_6._tracking){
if(!this.isShowingNow){
_5=this.startAbort;
}else{
_6._onUnHover();
_5=this.startForce;
}
}else{
_6.close();
}
}
}
if(_5!=this.startAbort){
this._curr=_4;
}
return _5;
},_isCurrent:function(_7,_8){
if(this._curr===_7){
if(_8){
this._curr=null;
}
return true;
}
return false;
},open:function(_9,x,y,_c,_d,_e,_f){
dojo.widget.PopupContainerBase.prototype.open.call(this,x,y,_c,_d,_e,_f);
},close:function(_10){
var _11=this._curr;
if(_11!=null){
_11.close(_10);
}
},_close:function(_12){
dojo.widget.PopupContainerBase.prototype.close.call(this,_12);
},addNode:function(_13,_14,_15,_16,_17,_18,_19,_1a,_1b){
var _1c=new _19.widget.PortalTooltipDisplay(_13,_14,_15,_16,_17,_18,this,_1a,_1b);
this.connections.push(_1c);
return _1c;
},removeNodes:function(_1d){
if(_1d==null||_1d.length==0){
return;
}
for(var i=0;i<_1d.length;i++){
_1d[i].destroy();
}
var _1f=[];
var _20=this.connections;
for(var i=0;i<_20.length;i++){
if(!_20[i].isDestroyed){
_1f.push(_20[i]);
}
}
this.connections=_1f;
},checkSize:function(){
},uninitialize:function(){
var _21=this.connections;
for(var i=0;i<_21.length;i++){
_21[i].destroy();
}
}});
jetspeed.widget.PortalTooltipDisplay=function(_23,_24,_25,_26,_27,_28,_29,_2a,_2b){
this.connectNode=_23;
this.caption=_24;
this.mouseDownStop=_25;
this.tooltipMgr=_29;
this.domNode=_29.domNode;
if(_26!=null){
this.showDelay=_26;
}
if(_28!=null&&_27!=null){
this.captionSelectFncObj=_27;
this.captionSelectFnc=_27[_28];
}
_2a.evtConnect("after",_23,"onmouseover",this,"_onMouseOver",_2b);
if(_25){
_2a.evtConnect("after",_23,"onmousedown",this,"_onMouseDown",_2b);
}
};
dojo.lang.extend(jetspeed.widget.PortalTooltipDisplay,{showDelay:750,hideDelay:100,captionSelectFnc:null,_onMouseOver:function(e){
var _2d=jetspeed;
if(_2d.widget._movingInProgress||_2d.ui.isWindowActionMenuOpen()){
return;
}
this._mouse={x:e.pageX,y:e.pageY};
this._abort=false;
var _2e=true;
if(this._tracking){
_2e=false;
}
this._tracking=true;
var _2f=this.tooltipMgr._setCurrent(this);
if(_2f!=this.tooltipMgr.startAbort){
if(_2e){
_2d.ui.evtConnect("after",document.documentElement,"onmousemove",this,"_onMouseMove");
}
this._onHover(e,_2f);
}else{
if(_2e){
this._tracking=false;
}
}
},_onMouseMove:function(e){
this._mouse={x:e.pageX,y:e.pageY};
if(dojo.html.overElement(this.connectNode,e)||dojo.html.overElement(this.domNode,e)){
this._onHover(e);
}else{
this._onUnHover(e);
}
},_onMouseDown:function(e){
this._abort=true;
jetspeed.stopEvent(e);
this._onUnHover(e);
if(this.tooltipMgr.isShowingNow){
this.close();
}
},_onHover:function(e,_33){
if(this._hover){
return;
}
this._hover=true;
if(this._hideTimer){
clearTimeout(this._hideTimer);
delete this._hideTimer;
}
if((!this.tooltipMgr.isShowingNow||_33==this.tooltipMgr.startForce)&&!this._showTimer){
this._showTimer=setTimeout(dojo.lang.hitch(this,"open"),this.showDelay);
}
},_onUnHover:function(e){
if(!this._hover&&!this._abort){
return;
}
this._hover=false;
if(this._showTimer){
clearTimeout(this._showTimer);
delete this._showTimer;
}
if(this.tooltipMgr.isShowingNow&&!this._hideTimer&&!this._abort){
this._hideTimer=setTimeout(dojo.lang.hitch(this,"close"),this.hideDelay);
}
if(!this.tooltipMgr.isShowingNow&&this._tracking){
jetspeed.ui.evtDisconnect("after",document.documentElement,"onmousemove",this,"_onMouseMove");
this._tracking=false;
}
},open:function(){
var _35=jetspeed;
if(this.tooltipMgr.isShowingNow||this._abort){
return;
}
if(_35.widget._movingInProgress||_35.ui.isWindowActionMenuOpen()){
this.close();
return;
}
var _36=this.caption;
if(this.captionSelectFnc!=null){
var _37=this.captionSelectFnc.call(this.captionSelectFncObj);
if(_37){
_36=_37;
}
}
if(!_36){
this.close();
}else{
this.domNode.innerHTML=_36;
this.tooltipMgr.open(this,this._mouse.x,this._mouse.y,null,[this._mouse.x,this._mouse.y],"TL,TR,BL,BR",[10,15]);
}
},close:function(_38){
this.tooltipMgr._isCurrent(this,true);
if(this._showTimer){
clearTimeout(this._showTimer);
delete this._showTimer;
}
if(this._hideTimer){
clearTimeout(this._hideTimer);
delete this._hideTimer;
}
if(this._tracking){
jetspeed.ui.evtDisconnect("after",document.documentElement,"onmousemove",this,"_onMouseMove");
this._tracking=false;
}
this.tooltipMgr._close(_38);
this._hover=false;
},_position:function(){
this.tooltipMgr.move.call(this.tooltipMgr,this._mouse.x,this._mouse.y,[10,15],"TL,TR,BL,BR");
},destroy:function(){
if(this.isDestroyed){
return;
}
this.close();
var _39=dojo.event;
var _3a=jetspeed.ui;
var _3b=this.connectNode;
_3a.evtDisconnect("after",_3b,"onmouseover",this,"_onMouseOver",_39);
if(this.mouseDownStop){
_3a.evtDisconnect("after",_3b,"onmousedown",this,"_onMouseDown",_39);
}
this.isDestroyed=true;
}});

