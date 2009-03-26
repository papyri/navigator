dojo.provide("jetspeed.desktop.debug");
dojo.require("jetspeed.debug");
dojo.require("dojo.profile");
if(!window.jetspeed){
jetspeed={};
}
if(!jetspeed.om){
jetspeed.om={};
}
jetspeed.debug={pageLoad:false,retrievePsml:false,setPortletContent:false,doRenderDoAction:false,postParseAnnotateHtml:false,postParseAnnotateHtmlDisableAnchors:false,confirmOnSubmit:false,createWindow:false,initWinState:false,submitWinState:false,ajaxPageNav:false,dragWindow:false,dragWindowStart:false,profile:false,windowDecorationRandom:false,debugInPortletWindow:true,debugContainerId:(djConfig.debugContainerId?djConfig.debugContainerId:dojo.hostenv.defaultDebugContainerId)};
jetspeed.debugAlert=function(_1){
if(_1){
alert(_1);
}
};
jetspeed.debugWindowLoad=function(){
var _2=jetspeed;
var _3=_2.id;
var _4=dojo;
if(djConfig.isDebug&&_2.debug.debugInPortletWindow&&_4.byId(_2.debug.debugContainerId)==null){
var _5=_2.debugWindowReadCookie(true);
var wP={};
var _7=_3.PW_ID_PREFIX+_3.DEBUG_WINDOW_TAG;
wP[_3.PP_WINDOW_POSITION_STATIC]=false;
wP[_3.PP_WINDOW_HEIGHT_TO_FIT]=false;
wP[_3.PP_WINDOW_DECORATION]=_2.prefs.windowDecoration;
wP[_3.PP_WINDOW_TITLE]="Dojo Debug";
wP[_3.PP_WINDOW_ICON]="text-x-script.png";
wP[_3.PP_WIDGET_ID]=_7;
wP[_3.PP_WIDTH]=_5.width;
wP[_3.PP_HEIGHT]=_5.height;
wP[_3.PP_LEFT]=_5.left;
wP[_3.PP_TOP]=_5.top;
wP[_3.PP_EXCLUDE_PCONTENT]=false;
wP[_3.PP_CONTENT_RETRIEVER]=new _2.om.DojoDebugContentRetriever();
wP[_3.PP_WINDOW_STATE]=_5.windowState;
if(_5.windowState==_3.ACT_MAXIMIZE){
_2.page.maximizedOnInit=_7;
}
var _8=_2.widget.PortletWindow.prototype.altInitParamsDef(null,wP);
_2.ui.createPortletWindow(_8,null,_2);
_8.retrieveContent(null,null);
var _9=_2.page.getPWin(_7);
_9.dbContentAdded=function(_a){
this.contentChanged(_a);
var _b=document.getElementById("_dbclrspan");
if(_b){
_b.style.visibility="visible";
}
};
_4.event.connect("after",_4.hostenv,"println",_9,"dbContentAdded");
_4.event.connect(_9,"actionBtnSync",_2,"debugWindowSave");
_4.event.connect(_9,"endSizing",_2,"debugWindowSave");
_4.event.connect(_9,"endDragging",_2,"debugWindowSave");
}
};
jetspeed.debugWindowReadCookie=function(_c){
var _d={};
if(_c){
_d={width:"400",height:"400",left:"320",top:"0",windowState:jetspeed.id.ACT_MINIMIZE};
}
var _e=dojo.io.cookie.getCookie(jetspeed.id.DEBUG_WINDOW_TAG);
if(_e!=null&&_e.length>0){
var _f=_e.split("|");
if(_f&&_f.length>=4){
_d.width=_f[0];
_d.height=_f[1];
_d.top=_f[2];
_d.left=_f[3];
if(_f.length>4&&_f[4]!=null&&_f[4].length>0){
_d.windowState=_f[4];
}
}
}
return _d;
};
jetspeed.debugWindowRestore=function(){
var _10=jetspeed.debugWindow();
if(!_10){
return;
}
_10.restoreWindow();
};
jetspeed.debugWindow=function(){
var _11=jetspeed.id.PW_ID_PREFIX+jetspeed.id.DEBUG_WINDOW_TAG;
return jetspeed.page.getPWin(_11);
};
jetspeed.debugWindowId=function(){
return jetspeed.id.PW_ID_PREFIX+jetspeed.id.DEBUG_WINDOW_TAG;
};
jetspeed.debugWindowSave=function(){
var _12=jetspeed.debugWindow();
if(!_12){
return null;
}
if(!_12.posStatic){
var _13=_12.getCurWinStateForPersist(false);
var _14=_13.width,_15=_13.height,_16=_13.top,_17=_13.left;
var _18=_12.windowState;
if(!_18){
_18=jetspeed.id.ACT_RESTORE;
}
var _19=_14+"|"+_15+"|"+_16+"|"+_17+"|"+_18;
dojo.io.cookie.setCookie(jetspeed.id.DEBUG_WINDOW_TAG,_19,30,"/");
var _1a=dojo.io.cookie.getCookie(jetspeed.id.DEBUG_WINDOW_TAG);
}
};
jetspeed.debugDumpForm=function(_1b){
if(!_1b){
return null;
}
var _1c=_1b.toString();
if(_1b.name){
_1c+=" name="+_1b.name;
}
if(_1b.id){
_1c+=" id="+_1b.id;
}
var _1d=dojo.io.encodeForm(_1b);
_1c+=" data="+_1d;
return _1c;
};
jetspeed.om.DojoDebugContentRetriever=function(){
this.initialized=false;
};
jetspeed.om.DojoDebugContentRetriever.prototype={getContent:function(_1e,_1f,_20,_21){
if(!_1e){
_1e={};
}
if(!this.initialized){
var _22=jetspeed;
var _23="";
var _24=_22.debug.debugContainerId;
var _25=_22.debugWindow();
if(_22.altDebugWindowContent){
_23=_22.altDebugWindowContent();
}else{
_23+="<div id=\""+_24+"\"></div>";
}
if(_1f){
_1f.notifySuccess(_23,_1e.url,_20);
}else{
if(_25){
_25.setPortletContent(_23,_1e.url);
}
}
this.initialized=true;
if(_25){
var _26="javascript: void(jetspeed.debugWindowClear())";
var _27="";
for(var i=0;i<20;i++){
_27+="&nbsp;";
}
var _29=_25.title+_27+"<a href=\""+_26+"\"><span id=\"_dbclrspan\" style=\"visibility: hidden; font-size: xx-small; font-weight: normal; color: blue\">Clear</span></a>";
_25.tbTextNode.innerHTML=_29;
}
}
}};
jetspeed.debugWindowClear=function(){
var _2a=jetspeed;
var _2b=_2a.debug.debugContainerId;
var _2c=_2a.debugWindow();
document.getElementById(_2b).innerHTML="";
if(_2c&&_2c.drag){
_2c.drag.onMouseUp(null,true);
}
var clr=document.getElementById("_dbclrspan");
if(clr){
clr.style.visibility="hidden";
}
};
jetspeed.debugDumpColWidths=function(){
for(var i=0;i<jetspeed.page.columns.length;i++){
var _2f=jetspeed.page.columns[i];
dojo.debug("jetspeed.page.columns["+i+"] outer-width: "+dojo.html.getMarginBox(_2f.domNode).width);
}
};
jetspeed.debugDumpWindowsPerCol=function(){
for(var i=0;i<jetspeed.page.columns.length;i++){
var _31=jetspeed.page.columns[i];
var _32=jetspeed.ui.getPWinAndColChildren(_31.domNode,null);
var _33=jetspeed.ui.getPWinsFromNodes(_32.matchingNodes);
var _34={dumpMsg:""};
if(_33!=null){
dojo.lang.forEach(_33,function(_35){
_34.dumpMsg=_34.dumpMsg+(_34.dumpMsg.length>0?", ":"")+_35.portlet.entityId;
});
}
_34.dumpMsg="column "+i+": "+_34.dumpMsg;
dojo.debug(_34.dumpMsg);
}
};
jetspeed.debugDumpWindows=function(){
var _36=jetspeed.page.getPWins();
var _37="";
for(var i=0;i<_36.length;i++){
if(i>0){
_37+=", ";
}
_37+=_36[i].widgetId;
}
dojo.debug("PortletWindows: "+_37);
};
jetspeed.debugLayoutInfo=function(){
var _39=jetspeed.page;
var _3a="";
var i=0;
for(var _3c in _39.layouts){
if(i>0){
_3a+="\r\n";
}
_3a+="layout["+_3c+"]: "+jetspeed.printobj(_39.layouts[_3c],true,true,true);
i++;
}
return _3a;
};
jetspeed.debugColumns=function(_3d,_3e){
var _3f=jetspeed;
var _40=_3f.page;
var _41=(!_3e);
var _42=_40.columns,col;
if(!_42){
return null;
}
var _44=dojo.byId(_3f.id.COLUMNS);
var _45="";
var _46=!_3d;
return _3f._debugColumnTree(_41,_44,_45,"\r\n",_3f.debugindentT,_46,_3f,_40);
};
jetspeed._debugColumnTree=function(_47,_48,_49,_4a,_4b,_4c,_4d,_4e){
var _4f=_4d.ui.getPWinAndColChildren(_48,null,false,true,true,_4c);
var _50=_4f.matchingNodes;
if(!_50||_50.length==0){
return _49;
}
var _51,col,_53,_54,_55=(_4a+_4b);
for(var i=0;i<_50.length;i++){
_51=_50[i];
col=_4e.getColFromColNode(_51);
_53=null;
if(!col){
_53=_4e.getPWinFromNode(_51);
}
_49+=_4a;
if(col){
_49+=_4d.debugColumn(col,_47);
_49=_4d._debugColumnTree(_47,_51,_49,_55,_4b,_4c,_4d,_4e);
}else{
if(_53){
_54=_53.title;
_49+=_53.widgetId+((_54&&_54.length>0)?(" - "+_54):"");
}else{
_49+=_4d.debugNode(_51);
}
}
}
return _49;
};
jetspeed.debugColumn=function(col,_58){
if(!col){
return null;
}
var _59=col.domNode;
var out="column["+dojo.string.padLeft(String(col.pageColumnIndex),2," ")+"]";
out+=" layoutHeader="+(col.layoutHeader?"T":"F")+" id="+(_59!=null?_59.id:"null")+" layoutCol="+col.layoutColumnIndex+" layoutId="+col.layoutId+" size="+col.size+(col.layoutDepth!=null?(" depth="+col.layoutDepth):"")+(col.layoutMaxChildDepth>0?(" childDepth="+col.layoutMaxChildDepth):"")+(col.layoutActionsDisabled?" noLayout=true":"");
if(_59!=null&&!_58){
var _5b=dojo.html.getAbsolutePosition(_59,true);
var _5c=dojo.html.getMarginBox(_59);
out+=" dims={"+"l="+(_5b.x)+" t="+(_5b.y)+" r="+(_5b.x+_5c.width)+" b="+(_5b.y+_5c.height)+" wOff="+_59.offsetWidth+" hOff="+_59.offsetHeight+" wCl="+_59.clientWidth+" hCl="+_59.clientHeight+"}";
}
return out;
};
jetspeed.debugSavedWinState=function(){
return jetspeed.debugWinStateAll(true);
};
jetspeed.debugWinState=function(){
return jetspeed.debugWinStateAll(false);
};
jetspeed.debugPortletActions=function(){
var _5d=jetspeed.page.getPortletArray();
var _5e="";
for(var i=0;i<_5d.length;i++){
var _60=_5d[i];
if(i>0){
_5e+="\r\n";
}
_5e+="portlet ["+_60.name+"] actions: {";
for(var _61 in _60.actions){
_5e+=_61+"={"+jetspeed.printobj(_60.actions[_61],true)+"} ";
}
_5e+="}";
}
return _5e;
};
jetspeed.debugWinStateAll=function(_62){
var _63=jetspeed.page.getPortletArray();
var _64="";
for(var i=0;i<_63.length;i++){
var _66=_63[i];
if(i>0){
_64+="\r\n";
}
var _67=null;
try{
if(_62){
_67=_66.getSavedWinState();
}else{
_67=_66.getCurWinState();
}
}
catch(e){
}
_64+="["+_66.name+"] "+((_67==null)?"null":jetspeed.printobj(_67,true));
}
return _64;
};
jetspeed.debugPWinPos=function(_68){
var _69=jetspeed;
var _6a=dojo;
var _6b=_69.UAie;
var djH=_6a.html;
var _6d=_68.domNode;
var _6e=_68.containerNode;
var _6f=_68.tbNode;
var _70=_68.rbNode;
var _71=djH.getAbsolutePosition(_6d,true);
var _72=djH.getAbsolutePosition(_6e,true);
var _73=djH.getAbsolutePosition(_6f,true);
var _74=djH.getAbsolutePosition(_70,true);
var _75=_6a.gcs(_6d),_76=_6a.gcs(_6e),_77=_6a.gcs(_6f),_78=_6a.gcs(_70);
var _79=null;
if(_69.UAie6){
_79=djH.getAbsolutePosition(_68.bgIframe.iframe,true);
}
var _7a=null;
var _7b=null;
var _7c=null;
if(_68.iframesInfo!=null&&_68.iframesInfo.iframeCover!=null){
_7a=_68.iframesInfo.iframeCover;
_7b=djH.getAbsolutePosition(_7a,true);
_7c=_6a.gcs(_7a);
}
var _7d=_68._getLayoutInfo();
var ind=_69.debugindent;
var _7f=_69.debugindentH;
_6a.hostenv.println("wnd-dims ["+_68.widgetId+"  "+_68.title+"]"+"  z="+_6d.style.zIndex+" hfit="+_68.heightToFit);
_6a.hostenv.println(ind+"d.abs {x="+_71.x+" y="+_71.y+"}"+(_6b?("  hasLayout="+_6d.currentStyle.hasLayout):""));
_6a.hostenv.println(ind+"c.abs {x="+_72.x+" y="+_72.y+"}"+(_6b?("  hasLayout="+_6e.currentStyle.hasLayout):""));
_6a.hostenv.println(ind+"t.abs {x="+_73.x+" y="+_73.y+"}"+(_6b?("  hasLayout="+_6f.currentStyle.hasLayout):""));
_6a.hostenv.println(ind+"r.abs {x="+_74.x+" y="+_74.y+"}"+(_6b?("  hasLayout="+_70.currentStyle.hasLayout):""));
if(_79!=null){
_6a.hostenv.println(ind+"ibg.abs {x="+_79.x+" y="+_79.y+"}"+_7f+" z="+_68.bgIframe.iframe.currentStyle.zIndex+(_6b?(" hasLayout="+_68.bgIframe.iframe.currentStyle.hasLayout):""));
}
if(_7b!=null){
_6a.hostenv.println(ind+"icv.abs {x="+_7b.x+" y="+_7b.y+"}"+_7f+" z="+_7c.zIndex+(_6b?(" hasLayout="+_7a.currentStyle.hasLayout):""));
}
_6a.hostenv.println(ind+"d.mb "+_69.debugDims(_6a.getMarginBox(_6d,_75,_69))+_7f+" d.offset {w="+_6d.offsetWidth+" h="+_6d.offsetHeight+"}");
_6a.hostenv.println(ind+"d.cb "+_69.debugDims(_6a.getContentBox(_6d,_75,_69))+_7f+" d.client {w="+_6d.clientWidth+" h="+_6d.clientHeight+"}");
_6a.hostenv.println(ind+"d.style {"+_69._debugPWinStyle(_6d,_75,"width",true)+_69._debugPWinStyle(_6d,_75,"height")+_7f+_69._debugPWinStyle(_6d,_75,"left")+_69._debugPWinStyle(_6d,_75,"top")+_7f+" pos="+_75.position.substring(0,1)+" ofx="+_75.overflowX.substring(0,1)+" ofy="+_75.overflowY.substring(0,1)+"}");
_6a.hostenv.println(ind+"c.mb "+_69.debugDims(_6a.getMarginBox(_6e,_76,_69))+_7f+" c.offset {w="+_6e.offsetWidth+" h="+_6e.offsetHeight+"}");
_6a.hostenv.println(ind+"c.cb "+_69.debugDims(_6a.getContentBox(_6e,_76,_69))+_7f+" c.client {w="+_6e.clientWidth+" h="+_6e.clientHeight+"}");
_6a.hostenv.println(ind+"c.style {"+_69._debugPWinStyle(_6e,_76,"width",true)+_69._debugPWinStyle(_6e,_76,"height")+_7f+_69._debugPWinStyle(_6e,_76,"left")+_69._debugPWinStyle(_6e,_76,"top")+_7f+" ofx="+_76.overflowX.substring(0,1)+" ofy="+_76.overflowY.substring(0,1)+" d="+_76.display.substring(0,1)+"}");
_6a.hostenv.println(ind+"t.mb "+_69.debugDims(_6a.getMarginBox(_6f,_77,_69))+_7f+" t.offset {w="+_6f.offsetWidth+" h="+_6f.offsetHeight+"}");
_6a.hostenv.println(ind+"t.cb "+_69.debugDims(_6a.getContentBox(_6f,_77,_69))+_7f+" t.client {w="+_6f.clientWidth+" h="+_6f.clientHeight+"}");
_6a.hostenv.println(ind+"t.style {"+_69._debugPWinStyle(_6f,_77,"width",true)+_69._debugPWinStyle(_6f,_77,"height")+_7f+_69._debugPWinStyle(_6f,_77,"left")+_69._debugPWinStyle(_6f,_77,"top")+"}");
_6a.hostenv.println(ind+"r.mb "+_69.debugDims(_6a.getMarginBox(_70,_78,_69))+_7f+" r.offset {w="+_70.offsetWidth+" h="+_70.offsetHeight+"}");
_6a.hostenv.println(ind+"r.cb "+_69.debugDims(_6a.getContentBox(_70,_78,_69))+_7f+" r.client {w="+_70.clientWidth+" h="+_70.clientHeight+"}");
_6a.hostenv.println(ind+"r.style {"+_69._debugPWinStyle(_70,_78,"width",true)+_69._debugPWinStyle(_70,_78,"height")+_7f+_69._debugPWinStyle(_70,_78,"left")+_69._debugPWinStyle(_70,_78,"top")+"}");
if(_79!=null){
var _80=_68.bgIframe.iframe;
var _81=_6a.gcs(_80);
_6a.hostenv.println(ind+"ibg.mb "+_69.debugDims(_6a.getMarginBox(_80,_81,_69)));
_6a.hostenv.println(ind+"ibg.cb "+_69.debugDims(_6a.getContentBox(_80,_81,_69)));
_6a.hostenv.println(ind+"ibg.style {"+_69._debugPWinStyle(_80,_81,"width",true)+_69._debugPWinStyle(_80,_81,"height")+_7f+_69._debugPWinStyle(_80,_81,"left")+_69._debugPWinStyle(_80,_81,"top")+_7f+" pos="+_81.position.substring(0,1)+" ofx="+_81.overflowX.substring(0,1)+" ofy="+_81.overflowY.substring(0,1)+" d="+_81.display.substring(0,1)+"}");
}
if(_7a){
_6a.hostenv.println(ind+"icv.mb "+_69.debugDims(_6a.getMarginBox(_7a,_7c,_69)));
_6a.hostenv.println(ind+"icv.cb "+_69.debugDims(_6a.getContentBox(_7a,_7c,_69)));
_6a.hostenv.println(ind+"icv.style {"+_69._debugPWinStyle(_7a,_7c,"width",true)+_69._debugPWinStyle(_7a,_7c,"height")+_7f+_69._debugPWinStyle(_7a,_7c,"left")+_69._debugPWinStyle(_7a,_7c,"top")+_7f+" pos="+_7c.position.substring(0,1)+" ofx="+_7c.overflowX.substring(0,1)+" ofy="+_7c.overflowY.substring(0,1)+" d="+_7c.display.substring(0,1)+"}");
}
var leN=_7d.dNode;
_6a.hostenv.println(ind+"dLE {"+"-w="+leN.lessW+" -h="+leN.lessH+" mw="+leN.mE.w+" mh="+leN.mE.h+" bw="+leN.bE.w+" bh="+leN.bE.h+" pw="+leN.pE.w+" ph="+leN.pE.h+"}");
leN=_7d.cNode;
_6a.hostenv.println(ind+"cLE {"+"-w="+leN.lessW+" -h="+leN.lessH+" mw="+leN.mE.w+" mh="+leN.mE.h+" bw="+leN.bE.w+" bh="+leN.bE.h+" pw="+leN.pE.w+" ph="+leN.pE.h+"}");
leN=_7d.tbNode;
_6a.hostenv.println(ind+"tLE {"+"-w="+leN.lessW+" -h="+leN.lessH+" mw="+leN.mE.w+" mh="+leN.mE.h+" bw="+leN.bE.w+" bh="+leN.bE.h+" pw="+leN.pE.w+" ph="+leN.pE.h+"}");
leN=_7d.rbNode;
_6a.hostenv.println(ind+"rLE {"+"-w="+leN.lessW+" -h="+leN.lessH+" mw="+leN.mE.w+" mh="+leN.mE.h+" bw="+leN.bE.w+" bh="+leN.bE.h+" pw="+leN.pE.w+" ph="+leN.pE.h+"}");
_6a.hostenv.println(ind+"cNode_mBh_LessBars="+_7d.cNode_mBh_LessBars);
_6a.hostenv.println(ind+"dimsTiled "+_69.debugDims(_68.dimsTiled));
_6a.hostenv.println(ind+"dimsUntiled "+_69.debugDims(_68.dimsUntiled));
if(_68.dimsTiledTemp!=null){
_6a.hostenv.println(ind+"dimsTiledTemp "+_69.debugDims(_68.dimsTiledTemp));
}
if(_68.dimsUntiledTemp!=null){
_6a.hostenv.println(ind+"dimsUntiledTemp="+_69.debugDims(_68.dimsUntiledTemp));
}
_6a.hostenv.println(ind+"--------------------");
},jetspeed.debugDims=function(box,_84){
return ("{w="+(box.w==undefined?(box.width==undefined?"null":box.width):box.w)+" h="+(box.h==undefined?(box.height==undefined?"null":box.height):box.h)+(box.l!=undefined?(" l="+box.l):(box.left==undefined?"":(" l="+box.left)))+(box.t!=undefined?(" t="+box.t):(box.top==undefined?"":(" t="+box.top)))+(box.right!=undefined?(" r="+box.right):"")+(box.bottom!=undefined?(" b="+box.bottom):"")+(!_84?"}":""));
};
jetspeed._debugPWinStyle=function(_85,_86,_87,_88){
var _89=_85.style[_87];
var _8a=_86[_87];
if(_89=="auto"){
_89="a";
}
if(_8a=="auto"){
_8a="a";
}
var _8b=null;
if(_89==_8a){
_8b=("\""+_8a+"\"");
}else{
_8b=("\""+_89+"\"/"+_8a);
}
return ((_88?"":" ")+_87.substring(0,1)+"="+_8b);
};
if(jetspeed.debug.profile){
dojo.profile.clearItem=function(_8c){
return (this._profiles[_8c]={iters:0,total:0});
};
dojo.profile.debugItem=function(_8d,_8e){
var _8f=this._profiles[_8d];
if(_8f==null){
return null;
}
if(_8f.iters==0){
return [_8d," not profiled."].join("");
}
var _90=[_8d," took ",_8f.total," msec for ",_8f.iters," iteration"];
if(_8f.iters>1){
_90.push("s (",(Math.round(_8f.total/_8f.iters*100)/100)," msec each)");
}
dojo.debug(_90.join(""));
if(_8e){
this.clearItem(_8d);
}
};
dojo.profile.debugAllItems=function(_91){
for(var x=0;x<this._pns.length;x++){
this.debugItem(this._pns[x],_91);
}
};
}
window.getPWin=function(_93){
return jetspeed.page.getPWin(_93);
};

