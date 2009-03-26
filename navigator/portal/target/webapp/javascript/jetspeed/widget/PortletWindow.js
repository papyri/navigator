dojo.provide("jetspeed.widget.PortletWindow");
dojo.require("jetspeed.desktop.core");
jetspeed.widget.PortletWindow=function(){
this.windowInitialized=false;
this.actionButtons={};
this.actionMenuWidget=null;
this.tooltips=[];
this._onLoadStack=[];
this._onUnloadStack=[];
this._callOnUnload=false;
};
dojo.extend(jetspeed.widget.PortletWindow,{title:"",nextIndex:1,resizable:true,moveable:true,moveAllowTilingChg:true,posStatic:false,heightToFit:false,decName:null,decConfig:null,titlebarEnabled:true,resizebarEnabled:true,editPageEnabled:false,iframeCoverContainerClass:"portletWindowIFrameClient",colWidth_pbE:0,portlet:null,altInitParams:null,inContentChgd:false,exclPContent:false,minimizeTempRestore:null,executeScripts:false,scriptSeparation:false,adjustPaths:false,parseContent:true,childWidgets:null,dbProfile:(djConfig.isDebug&&jetspeed.debug.profile),dbOn:djConfig.isDebug,dbMenuDims:"Dump Dimensions",altInitParamsDef:function(_1,_2){
if(!_1){
_1={getProperty:function(_3){
if(!_3){
return null;
}
return this.altInitParams[_3];
},retrieveContent:function(_4,_5){
var _6=this.altInitParams[jetspeed.id.PP_CONTENT_RETRIEVER];
if(_6){
_6.getContent(_5,_4,this,jetspeed.debugPortletDumpRawContent);
}else{
jetspeed.url.retrieveContent(_5,_4,this,jetspeed.debugPortletDumpRawContent);
}
}};
}
if(!_2){
_2={};
}
if(_2.altInitParams){
_1.altInitParams=_2.altInitParams;
}else{
_1.altInitParams=_2;
}
return _1;
},build:function(_7,_8){
var _9=jetspeed;
var _a=_9.id;
var _b=_9.prefs;
var _c=_9.page;
var _d=_9.css;
var _e=_9.ui;
var _f=document;
var _10=_9.docBody;
var _11=dojo;
var _12=_9.widget.PortletWindow.prototype.nextIndex;
this.windowIndex=_12;
var ie6=_9.UAie6;
this.ie6=ie6;
var _14=false;
if(_7){
if(_7.portlet){
this.portlet=_7.portlet;
}
if(_7.altInitParams){
this.altInitParams=_7.altInitParams;
}
if(_7.printMode){
_14=true;
}
}
var _15=this.portlet;
var iP=(_15?_15.getProperties():(this.altInitParams?this.altInitParams:{}));
var _17=iP[_a.PP_WIDGET_ID];
if(!_17){
if(_15){
_11.raise("PortletWindow is null for portlet: "+_15.entityId);
}else{
_17=_a.PW_ID_PREFIX+_12;
}
}
this.widgetId=_17;
_9.widget.PortletWindow.prototype.nextIndex++;
var _18=iP[_a.PP_WINDOW_DECORATION];
this.decName=_18;
var wDC=_9.loadPortletDecorationStyles(_18,_b);
if(wDC==null){
_11.raise("No portlet decoration is available: "+this.widgetId);
}
this.decConfig=wDC;
var _1a=wDC.dNodeClass;
var _1b=wDC.cNodeClass;
var _1c=_f.createElement("div");
_1c.id=_17;
_1c.className=_1a;
_1c.style.display="none";
var _1d=_f.createElement("div");
_1d.className=_1b;
var _1e=null,_1f=null,_20=null,_21=null;
if(!_14){
_1e=_f.createElement("div");
_1e.className="portletWindowTitleBar";
_20=_f.createElement("img");
_20.className="portletWindowTitleBarIcon";
var _22=_f.createElement("div");
_22.className="portletWindowTitleText";
_1e.appendChild(_20);
_1e.appendChild(_22);
_1f=_f.createElement("div");
_1f.className="portletWindowResizebar";
this.tbNode=_1e;
_21=_d.cssBase.concat();
this.tbNodeCss=_21;
this.tbIconNode=_20;
this.tbTextNode=_22;
this.rbNode=_1f;
this.rbNodeCss=_d.cssBase.concat();
}
if(_1e!=null){
_1c.appendChild(_1e);
}
_1c.appendChild(_1d);
if(_1f!=null){
_1c.appendChild(_1f);
}
this.domNode=_1c;
var _23=_d.cssPosition.concat();
if(_c.maximizedOnInit!=null){
_23[_d.cssNoSelNm]=" visibility: ";
_23[_d.cssNoSel]="hidden";
_23[_d.cssNoSelEnd]=";";
}
this.dNodeCss=_23;
this.containerNode=_1d;
var _24=_d.cssOverflow.concat();
this.cNodeCss=_24;
this.setPortletTitle(iP[_a.PP_WINDOW_TITLE]);
var _25=iP[_a.PP_WINDOW_POSITION_STATIC];
this.posStatic=this.preMaxPosStatic=_25;
var _26=iP[_a.PP_WINDOW_HEIGHT_TO_FIT];
this.heightToFit=this.preMaxHeightToFit=_26;
var _27=null,_28=null,_29=null,_2a=null;
if(_15){
var _2b=_15.getInitialWinDims();
_27=_2b.width;
_28=_2b.height;
_29=_2b.left;
_2a=_2b.top;
}else{
_27=iP[_a.PP_WIDTH];
_28=iP[_a.PP_HEIGHT];
_29=iP[_a.PP_LEFT];
_2a=iP[_a.PP_TOP];
}
var _2c={};
var _2d={w:null};
if(_27!=null&&_27>0){
_2c.w=_27=Math.floor(_27);
}else{
_2c.w=_27=_b.windowWidth;
}
if(_28!=null&&_28>0){
_2c.h=_2d.h=_28=Math.floor(_28);
}else{
_2c.h=_2d.h=_28=_b.windowHeight;
}
if(_29!=null&&_29>=0){
_2c.l=Math.floor(_29);
}else{
if(!_25){
_2c.l=(((_12-2)*30)+200);
}
}
if(_2a!=null&&_2a>=0){
_2c.t=Math.floor(_2a);
}else{
if(!_25){
_2c.t=(((_12-2)*30)+170);
}
}
this.dimsUntiled=_2c;
this.dimsTiled=_2d;
this.exclPContent=iP[_a.PP_EXCLUDE_PCONTENT];
_c.putPWin(this);
_10.appendChild(_1c);
if(_20){
var _2e=null;
if(wDC.windowIconEnabled&&wDC.windowIconPath!=null){
var wI=iP[_a.PP_WINDOW_ICON];
if(!wI){
wI="document.gif";
}
_2e=new _11.uri.Uri(_9.url.basePortalDesktopUrl()+wDC.windowIconPath+"/"+wI);
_2e=_2e.toString();
if(_2e.length==0){
_2e=null;
}
this.iconSrc=_2e;
}
if(_2e){
_20.src=_2e;
}else{
_11.dom.removeNode(_20);
this.tbIconNode=_20=null;
}
}
if(_1e){
if(_9.UAmoz||_9.UAsaf){
if(_9.UAmoz){
_21[_d.cssNoSelNm]=" -moz-user-select: ";
}else{
_21[_d.cssNoSelNm]=" -khtml-user-select: ";
}
_21[_d.cssNoSel]="none";
_21[_d.cssNoSelEnd]=";";
}else{
if(_9.UAie){
_1e.unselectable="on";
}
}
this._setupTitlebar(wDC,null,_15,_10,_f,_9,_a,_b,_e,_c,_11);
}
var _30=this.resizable;
var _31=null;
if(_30&&_1f){
var _32=_17+"_resize";
var _31=_9.widget.CreatePortletWindowResizeHandler(this,_9);
this.resizeHandle=_31;
if(_31){
_1f.appendChild(_31.domNode);
}
}else{
this.resizable=false;
}
_10.removeChild(_1c);
if(!wDC.windowTitlebar||!wDC.windowResizebar){
var _33=_9.css.cssDis;
if(!wDC.windowTitlebar){
this.titlebarEnabled=false;
if(this.tbNodeCss){
this.tbNodeCss[_33]="none";
}
}
if(!wDC.windowResizebar){
this.resizebarEnabled=false;
if(this.rbNodeCss){
this.rbNodeCss[_33]="none";
}
}
}
var _34=false;
var _35=_8.childNodes;
if(_25&&_35){
var _36=iP[_a.PP_ROW];
if(_36!=null){
var _37=new Number(_36);
if(_37>=0){
var _38=_35.length-1;
if(_38>=_37){
var _39=_35[_37];
if(_39){
_8.insertBefore(_1c,_39);
_34=true;
}
}
}
}
}
if(!_34){
_8.appendChild(_1c);
}
if(!wDC.layout){
var _3a="display: block; visibility: hidden; width: "+_27+"px"+((_28!=null&&_28>0)?("; height: "+_28+"px"):"");
_1c.style.cssText=_3a;
this._createLayoutInfo(wDC,false,_1c,_1d,_1e,_1f,_11,_9,_e);
}
if(_1e){
this.drag=new _11.dnd.Moveable(this,{handle:_1e});
this._setTitleBarDragging(true,_d);
}
if(ie6&&_25){
_2d.w=Math.max(0,_8.offsetWidth-this.colWidth_pbE);
}
this._setAsTopZIndex(_c,_d,_23,_25);
this._alterCss(true,true);
if(!_25){
this._addUntiledEvents();
}
if(ie6){
this.bgIframe=new _9.widget.BackgroundIframe(_1c,null,_11);
}
this.windowInitialized=true;
if(_9.debug.createWindow){
_11.debug("createdWindow ["+(_15?_15.entityId:_17)+(_15?(" / "+_17):"")+"]"+" width="+_1c.style.width+" height="+_1c.style.height+" left="+_1c.style.left+" top="+_1c.style.top);
}
this.windowState=_a.ACT_RESTORE;
var iWS=null;
if(_15){
iWS=_15.getCurrentActionState();
}else{
iWS=iP[_a.PP_WINDOW_STATE];
}
if(iWS==_a.ACT_MINIMIZE){
this.minimizeOnNextRender=true;
}
if(_9.widget.pwGhost==null&&_c!=null){
var _3c=_f.createElement("div");
_3c.id="pwGhost";
_3c.className=_1a;
_3c.style.position="static";
_3c.style.width="";
_3c.style.left="auto";
_3c.style.top="auto";
_9.widget.pwGhost=_3c;
}
if(ie6&&_9.widget.ie6ZappedContentHelper==null){
var _3d=_f.createElement("span");
_3d.id="ie6ZappedContentHelper";
_9.widget.ie6ZappedContentHelper=_3d;
}
},_buildActionStructures:function(wDC,_3f,_40,_41,_42,_43,_44){
var _45=new Array();
var aNm,_47,_48=false;
var _49=new Array();
var _4a=new Object();
var _4b=wDC.windowActionButtonOrder;
var _4c=wDC.windowActionMenuOrder;
var _4d=new Object();
var _4e=wDC.windowActionNoImage;
var _4f=wDC.windowActionButtonMax;
_4f=(_4f==null?-1:_4f);
if(_4c){
for(var aI=0;aI<_4c.length;aI++){
aNm=_4c[aI];
if(aNm){
_4d[aNm]=true;
}
}
}
if(_4b!=null){
for(var aI=(_4b.length-1);aI>=0;aI--){
aNm=_4b[aI];
_47=false;
if(_3f){
_47=true;
}else{
if(aNm==_42.ACT_MINIMIZE||aNm==_42.ACT_MAXIMIZE||aNm==_42.ACT_RESTORE||aNm==_42.ACT_MENU||_43.windowActionDesktop[aNm]!=null){
_47=true;
}
}
if(_47&&_4e&&_4e[aNm]){
if(!_4d[aNm]){
_49.push(aNm);
}
_47=false;
}
if(_47){
_45.push(aNm);
_4a[aNm]=true;
}
}
if(!_4a[_42.ACT_MENU]){
_48=true;
}
var _51=_45.length;
if(_4f!=-1&&_51>_4f){
var _52=0;
var _53=_51-_4f;
for(var j=0;j<2&&_52<_53;j++){
for(var i=(_45.length-1);i>=0&&_52<_53;i--){
aNm=_45[i];
if(aNm==null||aNm==_42.ACT_MENU){
continue;
}
if(j==0){
var _56=new RegExp("\b"+aNm+"\b");
if(_56.test(_43.windowActionNotPortlet)||aNm==_42.ACT_VIEW){
continue;
}
}
_49.push(aNm);
_45[i]=null;
delete _4a[aNm];
_52++;
}
}
}
}
var _57=new Array();
var _58=new Object();
var _59=_42.ACT_CHANGEPORTLETTHEME;
var _5a=_43.portletDecorationsAllowed;
if(_43.pageEditorLabels&&_5a&&_5a.length>1){
aNm=_59;
var _5b=_43.pageEditorLabels[aNm];
if(_5b){
_57.push(aNm);
_58[aNm];
this.actionLabels[aNm]=_5b;
}
}
for(var i=0;i<_49.length;i++){
aNm=_49[i];
if(aNm!=null&&!_58[aNm]&&!_4a[aNm]){
_57.push(aNm);
_58[aNm]=true;
}
}
if(_4c){
for(var aI=0;aI<_4c.length;aI++){
aNm=_4c[aI];
if(aNm!=null&&!_58[aNm]&&!_4a[aNm]&&(_3f||_43.windowActionDesktop[aNm])){
_57.push(aNm);
_58[aNm]=true;
}
}
}
if(this.dbOn){
_57.push({aNm:this.dbMenuDims,dev:true});
}
var _5c=null;
if(_57.length>0){
var _5d={};
var aNm,_5e,_5f,_60,_61,_62;
var _63=wDC.name+"_menu"+(!_3f?"Np":"");
var _64=_63;
_5c=_44.widget.createWidget("PopupMenu2",{id:_64,contextMenuForWindow:false},null);
_5c.onItemClick=function(mi){
var _aN=mi.jsActNm;
var _67=this.pWin;
if(!mi.jsActDev){
_67.actionProcess(_aN);
}else{
_67.actionProcessDev(_aN);
}
};
for(var i=0;i<_57.length;i++){
aNm=_57[i];
_61=null;
_62=false;
if(!aNm.dev){
_5e=this.actionLabels[aNm];
if(aNm==_59){
_61=_63+"_sub_"+aNm;
_60=_44.widget.createWidget("PopupMenu2",{id:_61,contextMenuForWindow:false},null);
_60.onItemClick=function(mi){
var _69=mi.jsPDecNm;
var _6a=_5c.pWin;
_6a.changeDecorator(_69);
};
for(var j=0;j<_5a.length;j++){
var _6b=_5a[j];
var _6c=_44.widget.createWidget("MenuItem2",{caption:_6b,jsPDecNm:_6b});
_60.addChild(_6c);
}
_40.appendChild(_60.domNode);
_41.ui.addPopupMenuWidget(_60);
}
}else{
_62=true;
_5e=aNm=aNm.aNm;
}
_5f=_44.widget.createWidget("MenuItem2",{caption:_5e,submenuId:_61,jsActNm:aNm,jsActDev:_62});
_5d[aNm]=_5f;
_5c.addChild(_5f);
}
_5c.menuItemsByName=_5d;
_40.appendChild(_5c.domNode);
_41.ui.addPopupMenuWidget(_5c);
}
wDC.windowActionMenuHasNoImg=_48;
if(_3f){
wDC.windowActionButtonNames=_45;
wDC.windowActionMenuNames=_57;
wDC.windowActionMenuWidget=_5c;
}else{
wDC.windowActionButtonNamesNp=_45;
wDC.windowActionMenuNamesNp=_57;
wDC.windowActionMenuWidgetNp=_5c;
}
return _45;
},_setupTitlebar:function(wDC,_6e,_6f,_70,doc,_72,_73,_74,_75,_76,_77){
var _78=_77.event;
var aNm;
var _7a=_76.tooltipMgr;
var _7b=this.tbNode;
var _7c=(_6e&&wDC);
if(_6e){
if(this.actionMenuWidget&&_6e.windowActionMenuHasNoImg){
_75.evtDisconnect("after",_7b,"oncontextmenu",this,"actionMenuOpen",_78);
}
_76.tooltipMgr.removeNodes(this.tooltips);
this.tooltips=ttps=[];
var _7d=this.actionButtons;
if(_7d){
var _7e=(_6e&&_6e.windowActionButtonTooltip);
for(aNm in _7d){
var _7f=_7d[aNm];
if(_7f){
_75.evtDisconnect("after",_7f,"onclick",this,"actionBtnClick",_78);
if(!_7e){
_75.evtDisconnect("after",_7f,"onmousedown",_72,"_stopEvent",_78);
}
if(_7c){
_77.dom.removeNode(_7f);
}
}
}
this.actionButtons=_7d={};
}
}
if(wDC){
if(wDC.windowActionButtonTooltip){
if(this.actionLabels[_73.ACT_DESKTOP_MOVE_TILED]!=null&&this.actionLabels[_73.ACT_DESKTOP_MOVE_UNTILED]!=null){
this.tooltips.push(_7a.addNode(_7b,null,true,1200,this,"getTitleBarTooltip",_72,_75,_78));
}
}
var _80=(_6f)?wDC.windowActionButtonNames:wDC.windowActionButtonNamesNp;
if(_80==null){
_80=this._buildActionStructures(wDC,_6f,_70,_72,_73,_74,_77);
}
for(var i=0;i<_80.length;i++){
aNm=_80[i];
if(aNm!=null){
if(!_6f||(aNm==_73.ACT_RESTORE||aNm==_73.ACT_MENU||_6f.getAction(aNm)!=null||_74.windowActionDesktop[aNm]!=null)){
this._createActionButtonNode(aNm,doc,_70,_7a,wDC,_72,_74,_75,_77,_78);
}
}
}
this.actionMenuWidget=(_6f)?wDC.windowActionMenuWidget:wDC.windowActionMenuWidgetNp;
if(this.actionMenuWidget&&wDC.windowActionMenuHasNoImg){
_75.evtConnect("after",_7b,"oncontextmenu",this,"actionMenuOpen",_78);
}
if(this.ie6&&!wDC._ie6used){
wDC._ie6used=true;
this.actionBtnSyncDefer(false,_72,_77);
}else{
this.actionBtnSync(_72,_73);
}
if(wDC.windowDisableResize){
this.resizable=false;
}
if(wDC.windowDisableMove){
this.moveable=false;
}
}
},_createActionButtonNode:function(aNm,doc,_84,_85,wDC,_87,_88,_89,_8a,_8b){
if(aNm!=null){
var _8c=doc.createElement("div");
_8c.className="portletWindowActionButton";
_8c.style.backgroundImage="url("+_88.getPortletDecorationBaseUrl(this.decName)+"/images/desktop/"+aNm+".gif)";
_8c.actionName=aNm;
this.actionButtons[aNm]=_8c;
this.tbNode.appendChild(_8c);
_89.evtConnect("after",_8c,"onclick",this,"actionBtnClick",_8b);
if(wDC.windowActionButtonTooltip){
var _8d=this.actionLabels[aNm];
this.tooltips.push(_85.addNode(_8c,_8d,true,null,null,null,_87,_89,_8b));
}else{
_89.evtConnect("after",_8c,"onmousedown",_87,"_stopEvent",_8b);
}
}
},getTitleBarTooltip:function(){
if(!this.getLayoutActionsEnabled()){
return null;
}
if(this.posStatic){
return this.actionLabels[jetspeed.id.ACT_DESKTOP_MOVE_TILED];
}else{
return this.actionLabels[jetspeed.id.ACT_DESKTOP_MOVE_UNTILED];
}
},_createLayoutInfo:function(_8e,_8f,_90,_91,_92,_93,_94,_95,_96){
var _97=_94.gcs(_90);
var _98=_94.gcs(_91);
var _99=_96.getLayoutExtents(_90,_97,_94,_95);
var _9a=_96.getLayoutExtents(_91,_98,_94,_95);
var _9b={dNode:_99,cNode:_9a};
var _9c=Math.max(0,_9a.mE.t);
var _9d=Math.max(0,_9a.mE.h-_9a.mE.t);
var _9e=0;
var _9f=0;
var _a0=null;
if(_92){
var _a1=_94.gcs(_92);
_a0=_96.getLayoutExtents(_92,_a1,_94,_95);
if(!_8e.dragCursor){
var _a2=_a1.cursor;
if(_a2==null||_a2.length==0){
_a2="move";
}
_8e.dragCursor=_a2;
}
_a0.mBh=_94.getMarginBox(_92,_a1,_95).h;
var _a3=Math.max(0,_a0.mE.h-_a0.mE.t);
_9e=(_a0.mBh-_a3)+Math.max(0,(_a3-_9c));
_9b.tbNode=_a0;
}
var _a4=null;
if(_93){
var _a5=_94.gcs(_93);
_a4=_96.getLayoutExtents(_93,_a5,_94,_95);
_a4.mBh=_94.getMarginBox(_93,_a5,_95).h;
var _a6=Math.max(0,_a4.mE.t);
_9f=(_a4.mBh-_a6)+Math.max(0,(_a6-_9d));
_9b.rbNode=_a4;
}
_9b.cNode_mBh_LessBars=_9e+_9f;
if(!_8f){
_8e.layout=_9b;
}else{
_8e.layoutIFrame=_9b;
}
},actionBtnClick:function(evt){
if(evt==null||evt.target==null){
return;
}
this.actionProcess(evt.target.actionName,evt);
},actionMenuOpen:function(evt){
var _a9=jetspeed;
var _aa=_a9.id;
var _ab=this.actionMenuWidget;
if(!_ab){
return;
}
if(_ab.isShowingNow){
_ab.close();
}
var _ac=null;
var _ad=null;
if(this.portlet){
_ac=this.portlet.getCurrentActionState();
_ad=this.portlet.getCurrentActionMode();
}
var _ae=_ab.menuItemsByName;
for(var aNm in _ae){
var _b0=_ae[aNm];
var _b1=(this._isActionEnabled(aNm,_ac,_ad,_a9,_aa))?"":"none";
_b0.domNode.style.display=_b1;
}
_ab.pWin=this;
_ab.onOpen(evt);
},actionProcessDev:function(aNm,evt){
if(aNm==this.dbMenuDims&&jetspeed.debugPWinPos){
jetspeed.debugPWinPos(this);
}
},actionProcess:function(aNm,evt){
var _b6=jetspeed;
var _b7=_b6.id;
if(aNm==null){
return;
}
if(_b6.prefs.windowActionDesktop[aNm]!=null){
if(aNm==_b7.ACT_DESKTOP_TILE){
this.makeTiled();
}else{
if(aNm==_b7.ACT_DESKTOP_UNTILE){
this.makeUntiled();
}else{
if(aNm==_b7.ACT_DESKTOP_HEIGHT_EXPAND){
this.makeHeightToFit(false);
}else{
if(aNm==_b7.ACT_DESKTOP_HEIGHT_NORMAL){
this.makeHeightVariable(false,false);
}
}
}
}
}else{
if(aNm==_b7.ACT_MENU){
this.actionMenuOpen(evt);
}else{
if(aNm==_b7.ACT_MINIMIZE){
if(this.portlet&&this.windowState==_b7.ACT_MAXIMIZE){
this.needsRenderOnRestore=true;
}
this.minimizeWindow();
if(this.portlet){
_b6.changeActionForPortlet(this.portlet.getId(),_b7.ACT_MINIMIZE,null);
}
if(!this.portlet){
this.actionBtnSyncDefer(false,_b6,dojo);
}
}else{
if(aNm==_b7.ACT_RESTORE){
var _b8=false;
if(this.portlet){
if(this.windowState==_b7.ACT_MAXIMIZE||this.needsRenderOnRestore){
if(this.needsRenderOnRestore){
_b8=true;
this.restoreOnNextRender=true;
this.needsRenderOnRestore=false;
}
this.portlet.renderAction(aNm);
}else{
_b6.changeActionForPortlet(this.portlet.getId(),_b7.ACT_RESTORE,null);
}
}
if(!_b8){
this.restoreWindow();
}
if(!this.portlet){
this.actionBtnSyncDefer(false,_b6,dojo);
}
}else{
if(aNm==_b7.ACT_MAXIMIZE){
this.maximizeWindow();
if(this.portlet){
this.portlet.renderAction(aNm);
}else{
this.actionBtnSync(_b6,_b7);
}
}else{
if(aNm==_b7.ACT_REMOVEPORTLET){
if(this.portlet){
var _b9=dojo.widget.byId(_b7.PG_ED_WID);
if(_b9!=null){
_b9.deletePortlet(this.portlet.entityId,this.title);
}
}
}else{
if(this.portlet){
this.portlet.renderAction(aNm);
}
}
}
}
}
}
}
},_isActionEnabled:function(aNm,_bb,_bc,_bd,_be){
var _bd=jetspeed;
var _be=_bd.id;
var _bf=false;
var _c0=this.windowState;
if(aNm==_be.ACT_MENU){
if(!this._actionMenuIsEmpty(_bd,_be)){
_bf=true;
}
}else{
if(_bd.prefs.windowActionDesktop[aNm]!=null){
if(this.getLayoutActionsEnabled()){
var _c1=(this.ie6&&_c0==_be.ACT_MINIMIZE);
if(aNm==_be.ACT_DESKTOP_HEIGHT_EXPAND){
if(!this.heightToFit&&!_c1){
_bf=true;
}
}else{
if(aNm==_be.ACT_DESKTOP_HEIGHT_NORMAL){
if(this.heightToFit&&!_c1){
_bf=true;
}
}else{
if(aNm==_be.ACT_DESKTOP_TILE&&_bd.prefs.windowTiling){
if(!this.posStatic){
_bf=true;
}
}else{
if(aNm==_be.ACT_DESKTOP_UNTILE){
if(this.posStatic){
_bf=true;
}
}
}
}
}
}
}else{
if(aNm==_be.ACT_CHANGEPORTLETTHEME){
if(this.cP_D&&this.editPageEnabled&&this.getLayoutActionsEnabled()){
_bf=true;
}
}else{
if(aNm==this.dbMenuDims){
_bf=true;
}else{
if(this.minimizeTempRestore!=null){
if(this.portlet){
var _c2=this.portlet.getAction(aNm);
if(_c2!=null){
if(_c2.id==_be.ACT_REMOVEPORTLET){
if(_bd.page.editMode&&this.getLayoutActionsEnabled()){
_bf=true;
}
}
}
}
}else{
if(this.portlet){
var _c2=this.portlet.getAction(aNm);
if(_c2!=null){
if(_c2.id==_be.ACT_REMOVEPORTLET){
if(_bd.page.editMode&&this.getLayoutActionsEnabled()){
_bf=true;
}
}else{
if(_c2.type==_be.PORTLET_ACTION_TYPE_MODE){
if(aNm!=_bc){
_bf=true;
}
}else{
if(aNm!=_bb){
_bf=true;
}
}
}
}
}else{
if(aNm==_be.ACT_MAXIMIZE){
if(aNm!=_c0&&this.minimizeTempRestore==null){
_bf=true;
}
}else{
if(aNm==_be.ACT_MINIMIZE){
if(aNm!=_c0){
_bf=true;
}
}else{
if(aNm==_be.ACT_RESTORE){
if(_c0==_be.ACT_MAXIMIZE||_c0==_be.ACT_MINIMIZE){
_bf=true;
}
}else{
if(aNm==this.dbMenuDims){
_bf=true;
}
}
}
}
}
}
}
}
}
}
return _bf;
},_actionMenuIsEmpty:function(_c3,_c4){
var _c5=true;
var _c6=this.actionMenuWidget;
if(_c6){
var _c7=null;
var _c8=null;
if(this.portlet){
_c7=this.portlet.getCurrentActionState();
_c8=this.portlet.getCurrentActionMode();
}
for(var aNm in _c6.menuItemsByName){
if(aNm!=_c4.ACT_MENU&&this._isActionEnabled(aNm,_c7,_c8,_c3,_c4)){
_c5=false;
break;
}
}
}
return _c5;
},actionBtnSyncDefer:function(_ca,_cb,_cc){
if(_ca&&_cb.UAie){
_ca=false;
}
if(_ca){
var _cd=_cc.gcs(this.domNode).opacity;
if(typeof _cd=="undefined"||_cd==null){
_ca=false;
}else{
_cd=Number(_cd);
this._savedOpacity=_cd;
var _ce=_cd-0.005;
_ce=((_ce<=0.1)?(_cd+0.005):_ce);
this.domNode.style.opacity=_ce;
_cc.lang.setTimeout(this,this._actionBtnSyncRepaint,20);
}
}
if(!_ca){
_cc.lang.setTimeout(this,this.actionBtnSync,10);
}
},_actionBtnSyncRepaint:function(_cf,_d0){
this.actionBtnSync(_cf,_d0);
if(this._savedOpacity!=null){
this.domNode.style.opacity=this._savedOpacity;
delete this._savedOpacity;
}
},actionBtnSync:function(_d1,_d2){
if(!_d1){
_d1=jetspeed;
_d2=_d1.id;
}
var _d3=null;
var _d4=null;
if(this.portlet){
_d3=this.portlet.getCurrentActionState();
_d4=this.portlet.getCurrentActionMode();
}
for(var aNm in this.actionButtons){
var _d6=this._isActionEnabled(aNm,_d3,_d4,_d1,_d2);
var _d7=this.actionButtons[aNm];
_d7.style.display=(_d6)?"block":"none";
}
},_postCreateMaximizeWindow:function(){
var _d8=jetspeed;
var _d9=_d8.id;
this.maximizeWindow();
if(this.portlet){
this.portlet.renderAction(_d9.ACT_MAXIMIZE);
}else{
this.actionBtnSync(_d8,_d9);
}
},minimizeWindowTemporarily:function(_da){
var _db=jetspeed;
var _dc=_db.id;
if(_da){
this.needsRenderOnRestore=true;
}
if(!this.minimizeTempRestore){
this.minimizeTempRestore=this.windowState;
if(this.windowState!=_dc.ACT_MINIMIZE){
this.minimizeWindow(false);
}
this.actionBtnSync(_db,_dc);
}
},restoreAllFromMinimizeWindowTemporarily:function(){
var _dd=jetspeed;
var _de=_dd.id;
var _df=_de.ACT_MINIMIZE,_e0=_de.ACT_MAXIMIZE;
var _e1;
var _e2=[];
var _e3=null;
var _e4=_dd.page.getPWins();
for(var i=0;i<_e4.length;i++){
_e1=_e4[i];
var _e6=_e1.minimizeTempRestore;
delete _e1.minimizeTempRestore;
if(_e6){
if(_e6==_e0){
_e3=_e1;
}
if(_e6==_df){
}else{
if(_e1.needsRenderOnRestore&&_e1.portlet){
deferRestoreWindow=true;
if(_e6!=_e0){
_e1.restoreOnNextRender=true;
}
delete _e1.needsRenderOnRestore;
_e1.portlet.renderAction(_e6);
}else{
_e1.restoreWindow();
if(!_e1.portlet){
_e1.actionBtnSyncDefer(false,_dd,dojo);
}
}
}
_e1.actionBtnSync(_dd,_de);
}
if(_e1.ie6&&_e1.posStatic){
var _e7=_e1.domNode.parentNode;
var _e8=false;
for(var j=0;j<_e2.length;j++){
if(_e2[j]==_e7){
_e8=true;
break;
}
}
if(!_e8){
_e2.push(_e7);
}
}
}
_dd.widget.showAllPortletWindows();
if(_e3!=null){
_e3.maximizeWindow();
}
if(_dd.UAie6){
if(_e2.length>0){
var _ea=new jetspeed.widget.IE6ZappedContentRestorer(_e2);
dojo.lang.setTimeout(_ea,_ea.showNext,20);
}
}
},minimizeWindow:function(_eb){
if(!this.tbNode){
return;
}
var _ec=jetspeed;
if(this.windowState==jetspeed.id.ACT_MAXIMIZE){
_ec.widget.showAllPortletWindows();
this.restoreWindow();
}else{
if(!_eb){
this._updtDimsObj(false,false);
}
}
var _ed=_ec.css.cssDis;
this.cNodeCss[_ed]="none";
if(this.rbNodeCss){
this.rbNodeCss[_ed]="none";
}
this.windowState=_ec.id.ACT_MINIMIZE;
if(this.ie6){
this.containerNode.style.display="none";
}
this._alterCss(true,true);
},maximizeWindow:function(){
var _ee=jetspeed;
var _ef=_ee.id;
var _f0=this.domNode;
var _f1=[this.widgetId];
_ee.widget.hideAllPortletWindows(_f1);
if(this.windowState==_ef.ACT_MINIMIZE){
this.restoreWindow();
}
var _f2=this.posStatic;
this.preMaxPosStatic=_f2;
this.preMaxHeightToFit=this.heightToFit;
var _f3=_f2;
this._updtDimsObj(false,_f3);
this._setTitleBarDragging(true,_ee.css,false);
this.posStatic=false;
this.heightToFit=false;
this._setMaximizeSize(true,true,_ee);
this._alterCss(true,true);
if(_f2){
jetspeedDesktop.appendChild(_f0);
}
window.scrollTo(0,0);
this.windowState=_ef.ACT_MAXIMIZE;
},_setMaximizeSize:function(_f4,_f5,_f6){
if(_f6==null){
_f6=jetspeed;
}
var _f7=0,_f8=0;
if(_f4){
var _f9=_f6.ui.scrollWidth;
if(_f9==null){
_f9=_f6.ui.getScrollbar(_f6);
}
_f8=_f7=((_f9+5)*-1);
}
var _fa=document.getElementById(_f6.id.DESKTOP);
var _fb=dojo;
var djH=_fb.html;
var _fd=djH.getAbsolutePosition(_fa,true).y;
var _fe=djH.getViewport();
var _ff=djH.getPadding(_f6.docBody);
var _100={w:(_fe.width+_f8)-_ff.width-2,h:(_fe.height+_f7)-_ff.height-_fd,l:1,t:_fd};
this.dimsUntiledTemp=_100;
if(!_f5){
this._alterCss(false,false,true);
}
if(_f4){
_fb.lang.setTimeout(this,this._setMaximizeSize,40,false,false,_f6);
}
return _100;
},restoreWindow:function(){
var _101=jetspeed;
var jsId=_101.id;
var _103=_101.css;
var _104=this.domNode;
var _105=false;
if(_104.style.position=="absolute"){
_105=true;
}
var _106=null;
var _107=false;
if(this.windowState==jsId.ACT_MAXIMIZE){
_101.widget.showAllPortletWindows();
this.posStatic=this.preMaxPosStatic;
this.heightToFit=this.preMaxHeightToFit;
this.dimsUntiledTemp=null;
_107=true;
}
var _108=_103.cssDis;
this.cNodeCss[_108]="block";
if(this.rbNodeCss&&this.resizebarEnabled){
this.rbNodeCss[_108]="block";
}
this.windowState=jsId.ACT_RESTORE;
this._setTitleBarDragging(true,_101.css);
var _109=null;
var ie6=this.ie6;
if(!ie6){
this._alterCss(true,true);
}else{
if(this.heightToFit){
_109=this.iNodeCss;
this.iNodeCss=null;
}
this._alterCss(true,true);
}
if(this.posStatic&&_105){
this._tileWindow(_101);
}
if(ie6){
this._updtDimsObj(false,false,true,false,true);
if(_107){
this._resetIE6TiledSize(false,true);
}
if(_109!=null){
this.iNodeCss=_109;
}
this._alterCss(false,false,true);
}
},_tileWindow:function(_10b){
if(!this.posStatic){
return;
}
var _10c=this.domNode;
var _10d=this.getDimsObj(this.posStatic);
var _10e=true;
if(_10d!=null){
var _10f=_10d.colInfo;
if(_10f!=null&&_10f.colI!=null){
var _110=_10b.page.columns[_10f.colI];
var _111=((_110!=null)?_110.domNode:null);
if(_111!=null){
var _112=null;
var _113=_111.childNodes.length;
if(_113==0){
_111.appendChild(_10c);
_10e=false;
}else{
var _114,_115,_116=0;
if(_10f.pSibId!=null||_10f.nSibId!=null){
_114=_111.firstChild;
do{
_115=_114.id;
if(_115==null){
continue;
}
if(_115==_10f.pSibId){
dojo.dom.insertAfter(_10c,_114);
_10e=false;
}else{
if(_115==_10f.nSibId){
dojo.dom.insertBefore(_10c,_114);
_10e=false;
}else{
if(_116==_10f.elmtI){
_112=_114;
}
}
}
_114=_114.nextSibling;
_116++;
}while(_10e&&_114!=null);
}
}
if(_10e){
if(_112!=null){
dojo.dom.insertBefore(_10c,_112);
}else{
dojo.dom.prependChild(_10c,_111);
}
_10e=false;
}
}
}
}
if(_10e){
var _117=_10b.page.getColumnDefault();
if(_117!=null){
dojo.dom.prependChild(_10c,_117.domNode);
}
}
},getDimsObj:function(_118,_119){
return (_118?((this.dimsTiledTemp!=null&&!_119)?this.dimsTiledTemp:this.dimsTiled):((this.dimsUntiledTemp!=null&&!_119)?this.dimsUntiledTemp:this.dimsUntiled));
},_updtDimsObj:function(_11a,_11b,_11c,_11d,_11e,_11f){
var _120=jetspeed;
var _121=dojo;
var _122=this.domNode;
var _123=this.posStatic;
var _124=this.getDimsObj(_123,_11f);
var _125=(!_11c&&!_123&&(!_11a||_124.l==null||_124.t==null));
var _126=(!_11d&&(!_11a||_125||_11e||_124.w==null||_124.h==null));
if(_126||_125){
var _127=this._getLayoutInfo().dNode;
if(_126){
var _128=_120.ui.getMarginBoxSize(_122,_127);
_124.w=_128.w;
_124.h=_128.h;
if(!_123){
_125=true;
}
}
if(_125){
var _129=_121.html.getAbsolutePosition(_122,true);
_124.l=_129.x-_127.mE.l-_127.pbE.l;
_124.t=_129.y-_127.mE.t-_127.pbE.t;
}
}
if(_123){
if(_11b||_11f&&_124.colInfo==null){
var _12a=0,_12b=_122.previousSibling,_12c=_122.nextSibling;
var _12d=(_12b!=null?_12b.id:null),_12e=(_12c!=null?_12c.id:null);
if(_12b!=null){
_12d=_12b.id;
}
while(_12b!=null){
_12a++;
_12b=_12b.previousSibling;
}
_124.colInfo={elmtI:_12a,pSibId:_12d,nSibId:_12e,colI:this.getPageColumnIndex()};
}
if(_11f){
this.dimsTiledTemp={w:_124.w,h:_124.h,colInfo:_124.colInfo};
_124=this.dimsTiledTemp;
}
}else{
if(_11f){
this.dimsUntiledTemp={w:_124.w,h:_124.h,l:_124.l,t:_124.t};
_124=this.dimsUntiledTemp;
}
}
return _124;
},getLayoutActionsEnabled:function(){
return (this.windowState!=jetspeed.id.ACT_MAXIMIZE&&(this.portlet==null||(!this.portlet.layoutActionsDisabled||(this.cL_NA_ED==true))));
},_setTitleBarDragging:function(_12f,_130,_131){
var _132=this.tbNode;
if(!_132){
return;
}
if(typeof _131=="undefined"){
_131=this.getLayoutActionsEnabled();
}
var _133=this.resizeHandle;
var _134=null;
var wDC=this.decConfig;
var _136=_131;
if(_136&&!this.resizebarEnabled){
_136=false;
}
if(_131&&!this.titlebarEnabled){
_131=false;
}
if(_131){
_134=wDC.dragCursor;
if(this.drag){
this.drag.enable();
}
}else{
_134="default";
if(this.drag){
this.drag.disable();
}
}
if(_136){
if(_133){
_133.domNode.style.display="";
}
}else{
if(_133){
_133.domNode.style.display="none";
}
}
this.tbNodeCss[_130.cssCur]=_134;
if(!_12f){
_132.style.cursor=_134;
}
},onMouseDown:function(evt){
this.bringToTop(evt,false,false,jetspeed);
},bringToTop:function(evt,_139,_13a,_13b){
if(!this.posStatic){
var _13c=_13b.page;
var _13d=_13b.css;
var _13e=this.dNodeCss;
var _13f=_13c.getPWinHighZIndex();
var zCur=_13e[_13d.cssZIndex];
if(_13f!=zCur){
var zTop=this._setAsTopZIndex(_13c,_13d,_13e,false);
if(this.windowInitialized){
this.domNode.style.zIndex=zTop;
if(!_13a&&this.portlet&&this.windowState!=jetspeed.id.ACT_MAXIMIZE){
this.portlet.submitWinState();
}
}
}
}else{
if(_139){
var zTop=this._setAsTopZIndex(_13c,_13d,_13e,true);
if(this.windowInitialized){
this.domNode.style.zIndex=zTop;
}
}
}
},_setAsTopZIndex:function(_142,_143,_144,_145){
var zTop=String(_142.getPWinTopZIndex(_145));
_144[_143.cssZIndex]=zTop;
return zTop;
},makeUntiled:function(){
var _147=jetspeed;
this._updtDimsObj(false,true);
this.posStatic=false;
this._updtDimsObj(true,false);
this._setAsTopZIndex(_147.page,_147.css,this.dNodeCss,false);
this._alterCss(true,true);
var _148=this.domNode.parentNode;
var _149=document.getElementById(jetspeed.id.DESKTOP);
_149.appendChild(this.domNode);
_147.page.columnEmptyCheck(_148);
if(this.windowState==_147.id.ACT_MINIMIZE){
this.minimizeWindow();
}
if(this.portlet){
this.portlet.submitWinState();
}
this._addUntiledEvents();
},makeTiled:function(){
this.posStatic=true;
var _14a=jetspeed;
this._setAsTopZIndex(_14a.page,_14a.css,this.dNodeCss,true);
this._alterCss(true,true);
this._tileWindow(_14a);
_14a.page.columnEmptyCheck(this.domNode.parentNode);
if(this.portlet){
this.portlet.submitWinState();
}
this._removeUntiledEvents();
},_addUntiledEvents:function(){
if(this._untiledEvts==null){
this._untiledEvts=[jetspeed.ui.evtConnect("after",this.domNode,"onmousedown",this,"onMouseDown")];
}
},_removeUntiledEvents:function(){
if(this._untiledEvts!=null){
jetspeed.ui.evtDisconnectWObjAry(this._untiledEvts);
delete this._untiledEvts;
}
},makeHeightToFit:function(_14b){
var _14c=dojo.html.getMarginBox(this.domNode);
this.heightToFit=true;
if(this.ie6){
var _14d=this.iNodeCss;
this.iNodeCss=null;
this._alterCss(false,true);
this._updtDimsObj(false,false,true,false,true);
this.iNodeCss=_14d;
}
this._alterCss(false,true);
if(!_14b&&this.portlet){
this.portlet.submitWinState();
}
},makeHeightVariable:function(_14e,_14f){
var _150=this.getDimsObj(this.posStatic);
var _151=this._getLayoutInfo().dNode;
var _152=jetspeed.ui.getMarginBoxSize(this.domNode,_151);
_150.w=_152.w;
_150.h=_152.h;
this.heightToFit=false;
this._alterCss(false,true);
if(!_14f&&this.iframesInfo){
dojo.lang.setTimeout(this,this._forceRefreshZIndex,70);
}
if(!_14e&&this.portlet){
this.portlet.submitWinState();
}
},editPageInitiate:function(cP_D,_154,_155,_156,_157){
this.editPageEnabled=true;
this.cP_D=cP_D;
this.cL_NA_ED=_154;
var wDC=this.decConfig;
if(!wDC.windowTitlebar||!wDC.windowResizebar){
var _159=_156.cssDis;
if(!wDC.windowTitlebar){
this.titlebarEnabled=true;
if(this.tbNodeCss){
this.tbNodeCss[_159]="block";
}
}
if(!wDC.windowResizebar){
this.resizebarEnabled=true;
if(this.rbNodeCss&&this.windowState!=_155.id.ACT_MINIMIZE){
this.rbNodeCss[_159]="block";
}
}
this._setTitleBarDragging(false,_156);
if(!_157){
this._alterCss(true,true);
}
}else{
this._setTitleBarDragging(false,_156);
}
},editPageTerminate:function(_15a,_15b){
this.editPageEnabled=false;
delete this.cP_D;
delete this.cL_NA_ED;
var wDC=this.decConfig;
if(!wDC.windowTitlebar||!wDC.windowResizebar){
var _15d=_15a.cssDis;
if(!wDC.windowTitlebar){
this.titlebarEnabled=false;
if(this.tbNodeCss){
this.tbNodeCss[_15d]="none";
}
}
if(!wDC.windowResizebar){
this.resizebarEnabled=false;
if(this.rbNodeCss){
this.rbNodeCss[_15d]="none";
}
}
this._setTitleBarDragging(false,_15a);
if(!_15b){
this._alterCss(true,true);
}
}else{
this._setTitleBarDragging(false,_15a);
}
},changeDecorator:function(_15e){
var _15f=jetspeed;
var _160=_15f.css;
var jsId=_15f.id;
var jsUI=_15f.ui;
var _163=_15f.prefs;
var _164=dojo;
var _165=this.decConfig;
if(_165&&_165.name==_15e){
return;
}
var wDC=_15f.loadPortletDecorationStyles(_15e,_163);
if(!wDC){
return;
}
var _167=this.portlet;
if(_167){
_167._submitAjaxApi("updatepage","&method=update-portlet-decorator&portlet-decorator="+_15e);
}
this.decConfig=wDC;
this.decName=wDC.name;
var _168=this.domNode;
var _169=this.containerNode;
var _16a=this.iframesInfo;
var _16b=(_16a&&_16a.layout);
var _16c=(!_16b?wDC.layout:wDC.layoutIFrame);
if(!_16c){
if(!_16b){
this._createLayoutInfo(wDC,false,_168,_169,this.tbNode,this.rbNode,_164,_15f,jsUI);
}else{
this._createLayoutInfo(wDC,true,_168,_169,this.tbNode,this.rbNode,_164,_15f,jsUI);
}
}
this._setupTitlebar(wDC,_165,this.portlet,_15f.docBody,document,_15f,_15f.id,_163,jsUI,_15f.page,_164);
_168.className=wDC.dNodeClass;
if(_16b){
_169.className=wDC.cNodeClass+" "+this.iframeCoverContainerClass;
}else{
_169.className=wDC.cNodeClass;
}
var _16d=_160.cssDis;
this.titlebarEnabled=true;
if(this.tbNodeCss){
this.tbNodeCss[_16d]="block";
}
this.resizebarEnabled=true;
if(this.rbNodeCss&&this.windowState!=jsId.ACT_MINIMIZE){
this.rbNodeCss[_16d]="block";
}
if(this.editPageEnabled){
this.editPageInitiate(this.cP_D,this.cL_NA_ED,_15f,_160,true);
}else{
this.editPageTerminate(_160,true);
}
this._setTitleBarDragging(true,_160);
this._alterCss(true,true);
},resizeTo:function(w,h,_170){
var _171=this.getDimsObj(this.posStatic);
_171.w=w;
_171.h=h;
this._alterCss(false,false,true);
if(!this.windowIsSizing){
var _172=this.resizeHandle;
if(_172!=null&&_172._isSizing){
jetspeed.ui.evtConnect("after",_172,"_endSizing",this,"endSizing");
this.windowIsSizing=true;
}
}
this.resizeNotifyChildWidgets();
},resizeNotifyChildWidgets:function(){
if(this.childWidgets){
var _173=this.childWidgets;
var _174=_173.length,_175;
for(var i=0;i<_174;i++){
try{
_175=_173[i];
if(_175){
_175.checkSize();
}
}
catch(e){
}
}
}
},_getLayoutInfo:function(){
var _177=this.iframesInfo;
return ((!(_177&&_177.layout))?this.decConfig.layout:this.decConfig.layoutIFrame);
},_getLayoutInfoMoveable:function(){
return this._getLayoutInfo().dNode;
},onBrowserWindowResize:function(){
var _178=jetspeed;
if(this.ie6){
this._resetIE6TiledSize(false);
}
if(this.windowState==_178.id.ACT_MAXIMIZE){
this._setMaximizeSize(true,false,_178);
}
},_resetIE6TiledSize:function(_179,_17a){
var _17b=this.posStatic;
if(_17b){
var _17c=this.domNode;
var _17d=this.getDimsObj(_17b);
_17d.w=Math.max(0,this.domNode.parentNode.offsetWidth-this.colWidth_pbE);
if(!_17a){
this._alterCss(_179,false,false,false,true);
}
}
},_alterCss:function(_17e,_17f,_180,_181,_182,_183){
var _184=jetspeed;
var _185=_184.css;
var _186=this.iframesInfo;
var _187=(_186&&_186.layout);
var _188=(!_187?this.decConfig.layout:this.decConfig.layoutIFrame);
var _189=this.dNodeCss,_18a=null,_18b=null,_18c=null,_18d=false,_18e=this.iNodeCss,_18f=null;
if(_18e&&_187){
_18f=_186.iframeCoverIE6Css;
}
var _190=this.posStatic;
var _191=(_190&&_18e==null);
var _192=this.heightToFit;
var _193=(_17e||_182||(_180&&!_191));
var _194=(_17f||_180);
var _195=(_17e||_181);
var _196=(_17f||(_180&&_187));
var _197=this.getDimsObj(_190);
if(_17e){
_189[_185.cssPos]=(_190?"relative":"absolute");
}
var _198=null,_199=null;
if(_17f){
if(_187){
var _19a=this.getIFramesAndObjects(false,true);
if(_19a&&_19a.iframes&&_19a.iframes.length==1&&_186.iframesSize&&_186.iframesSize.length==1){
var _19b=_186.iframesSize[0].h;
if(_19b!=null){
_198=_19a.iframes[0];
_199=(_192?_19b:(!_184.UAie?"100%":"99%"));
_183=false;
}
}
}
}
if(_196){
_18a=this.cNodeCss;
var _19c=_185.cssOx,_19d=_185.cssOy;
if(_192&&!_187){
_189[_19d]="hidden";
_18a[_19d]="visible";
}else{
_189[_19d]="hidden";
_18a[_19d]=(!_187?"auto":"hidden");
}
}
if(_195){
var lIdx=_185.cssL,_19f=_185.cssLU;
var tIdx=_185.cssT,_1a1=_185.cssTU;
if(_190){
_189[lIdx]="auto";
_189[_19f]="";
_189[tIdx]="auto";
_189[_1a1]="";
}else{
_189[lIdx]=_197.l;
_189[_19f]="px";
_189[tIdx]=_197.t;
_189[_1a1]="px";
}
}
if(_194){
_18a=this.cNodeCss;
var hIdx=_185.cssH,_1a3=_185.cssHU;
if(_192&&_18e==null){
_189[hIdx]="";
_189[_1a3]="";
_18a[hIdx]="";
_18a[_1a3]="";
}else{
var h=_197.h;
var _1a5=_184.css.cssDis;
var _1a6;
var _1a7;
if(_18a[_1a5]=="none"){
_1a6=_188.tbNode.mBh;
_1a7="";
_18a[_1a3]="";
}else{
_1a6=(h-_188.dNode.lessH);
_1a7=_1a6-_188.cNode.lessH-_188.cNode_mBh_LessBars;
_18a[_1a3]="px";
}
_189[hIdx]=_1a6;
_189[_1a3]="px";
_18a[hIdx]=_1a7;
if(_18e){
_18e[hIdx]=_1a6;
_18e[_1a3]="px";
_18d=true;
if(_18f){
_18f[hIdx]=_1a7;
_18f[_1a3]=_18a[_1a3];
}
}
}
}
if(_193){
var w=_197.w;
_18a=this.cNodeCss;
_18b=this.tbNodeCss;
_18c=this.rbNodeCss;
var wIdx=_185.cssW,_1aa=_185.cssWU;
if(_191&&(!this.ie6||!w)){
_189[wIdx]="";
_189[_1aa]="";
_18a[wIdx]="";
_18a[_1aa]="";
if(_18b){
_18b[wIdx]="";
_18b[_1aa]="";
}
if(_18c){
_18c[wIdx]="";
_18c[_1aa]="";
}
}else{
var _1ab=(w-_188.dNode.lessW);
_189[wIdx]=_1ab;
_189[_1aa]="px";
_18a[wIdx]=_1ab-_188.cNode.lessW;
_18a[_1aa]="px";
if(_18b){
_18b[wIdx]=_1ab-_188.tbNode.lessW;
_18b[_1aa]="px";
}
if(_18c){
_18c[wIdx]=_1ab-_188.rbNode.lessW;
_18c[_1aa]="px";
}
if(_18e){
_18e[wIdx]=_1ab;
_18e[_1aa]="px";
_18d=true;
if(_18f){
_18f[wIdx]=_18a[wIdx];
_18f[_1aa]=_18a[_1aa];
}
}
}
}
if(!_183){
this.domNode.style.cssText=_189.join("");
if(_18a){
this.containerNode.style.cssText=_18a.join("");
}
if(_18b){
this.tbNode.style.cssText=_18b.join("");
}
if(_18c){
this.rbNode.style.cssText=_18c.join("");
}
if(_18d){
this.bgIframe.iframe.style.cssText=_18e.join("");
if(_18f){
_186.iframeCover.style.cssText=_18f.join("");
}
}
}
if(_198&&_199){
this._deferSetIFrameH(_198,_199,false,50);
}
},_deferSetIFrameH:function(_1ac,_1ad,_1ae,_1af,_1b0){
if(!_1af){
_1af=100;
}
var pWin=this;
window.setTimeout(function(){
_1ac.height=_1ad;
if(_1ae){
if(_1b0==null){
_1b0=50;
}
if(_1b0==0){
pWin._forceRefreshZIndexAndForget();
}else{
dojo.lang.setTimeout(pWin,pWin._forceRefreshZIndexAndForget,_1b0);
}
}
},_1af);
},_getWindowMarginBox:function(_1b2,_1b3){
var _1b4=this.domNode;
if(_1b2==null){
_1b2=this._getLayoutInfo().dNode;
}
var _1b5=null;
if(_1b3.UAope){
_1b5=(this.posStatic?_1b3.page.layoutInfo.column:_1b3.page.layoutInfo.desktop);
}
return _1b3.ui.getMarginBox(_1b4,_1b2,_1b5,_1b3);
},_forceRefreshZIndex:function(){
var _1b6=jetspeed;
var zTop=this._setAsTopZIndex(_1b6.page,_1b6.css,this.dNodeCss,this.posStatic);
this.domNode.style.zIndex=zTop;
},_forceRefreshZIndexAndForget:function(){
var zTop=jetspeed.page.getPWinTopZIndex(this.posStatic);
this.domNode.style.zIndex=String(zTop);
},getIFramesAndObjects:function(_1b9,_1ba){
var _1bb=this.containerNode;
var _1bc={};
var _1bd=false;
if(!_1ba){
var _1be=_1bb.getElementsByTagName("object");
if(_1be&&_1be.length>0){
_1bc.objects=_1be;
_1bd=true;
}
}
var _1bf=_1bb.getElementsByTagName("iframe");
if(_1bf&&_1bf.length>0){
_1bc.iframes=_1bf;
if(!_1b9){
return _1bc;
}
_1bd=true;
var _1c0=[];
for(var i=0;i<_1bf.length;i++){
var ifrm=_1bf[i];
var w=new Number(String(ifrm.width));
w=(isNaN(w)?null:String(ifrm.width));
var h=new Number(String(ifrm.height));
h=(isNaN(h)?null:String(ifrm.height));
_1c0.push({w:w,h:h});
}
_1bc.iframesSize=_1c0;
}
if(!_1bd){
return null;
}
return _1bc;
},contentChanged:function(evt){
if(this.inContentChgd==false){
this.inContentChgd=true;
if(this.heightToFit){
this.makeHeightToFit(true);
}
this.inContentChgd=false;
}
},closeWindow:function(){
var _1c6=jetspeed;
var jsUI=_1c6.ui;
var _1c8=_1c6.page;
var _1c9=dojo;
var _1ca=_1c9.event;
var wDC=this.decConfig;
if(this.iframesInfo){
_1c8.unregPWinIFrameCover(this);
}
this._setupTitlebar(null,wDC,this.portlet,_1c6.docBody,document,_1c6,_1c6.id,_1c6.prefs,jsUI,_1c8,_1c9);
if(this.drag){
this.drag.destroy(_1c9,_1ca,_1c6,jsUI);
this.drag=null;
}
if(this.resizeHandle){
this.resizeHandle.destroy(_1ca,_1c6,jsUI);
this.resizeHandle=null;
}
this._destroyChildWidgets(_1c9);
this._removeUntiledEvents();
var _1cc=this.domNode;
if(_1cc&&_1cc.parentNode){
_1cc.parentNode.removeChild(_1cc);
}
this.domNode=null;
this.containerNode=null;
this.tbNode=null;
this.rbNode=null;
},_destroyChildWidgets:function(_1cd){
if(this.childWidgets){
var _1ce=this.childWidgets;
var _1cf=_1ce.length,_1d0,swT,swI;
_1cd.debug("PortletWindow ["+this.widgetId+"] destroy child widgets ("+_1cf+")");
for(var i=(_1cf-1);i>=0;i--){
try{
_1d0=_1ce[i];
if(_1d0){
swT=_1d0.widgetType;
swI=_1d0.widgetId;
_1d0.destroy();
_1cd.debug("destroyed child widget["+i+"]: "+swT+" "+swI);
}
_1ce[i]=null;
}
catch(e){
}
}
this.childWidgets=null;
}
},getPageColumnIndex:function(){
return jetspeed.page.getColIndexForNode(this.domNode);
},endSizing:function(e){
jetspeed.ui.evtDisconnect("after",this.resizeHandle,"_endSizing",this,"endSizing");
this.windowIsSizing=false;
if(this.portlet&&this.windowState!=jetspeed.id.ACT_MAXIMIZE){
this.portlet.submitWinState();
}
},endDragging:function(_1d5,_1d6,_1d7){
var _1d8=jetspeed;
var ie6=this.ie6;
if(_1d6){
this.posStatic=false;
}else{
if(_1d7){
this.posStatic=true;
}
}
var _1da=this.posStatic;
if(!_1da){
var _1db=this.getDimsObj(_1da);
if(_1d5&&_1d5.left!=null&&_1d5.top!=null){
_1db.l=_1d5.left;
_1db.t=_1d5.top;
if(!_1d6){
this._alterCss(false,false,false,true,false,true);
}
}
if(_1d6){
this._updtDimsObj(false,false,true);
this._alterCss(true,true,false,true);
this._addUntiledEvents();
}
}else{
if(_1d7){
this._setAsTopZIndex(_1d8.page,_1d8.css,this.dNodeCss,_1da);
this._updtDimsObj(false,false);
}
if(!ie6){
this._alterCss(true);
this.resizeNotifyChildWidgets();
}else{
this._resetIE6TiledSize(_1d7);
}
}
if(this.portlet&&this.windowState!=_1d8.id.ACT_MAXIMIZE){
this.portlet.submitWinState();
}
if(ie6){
dojo.lang.setTimeout(this,this._IEPostDrag,_1d8.widget.ie6PostDragAddDelay);
}
},getCurWinState:function(_1dc){
var _1dd=this.domNode;
var _1de=this.posStatic;
if(!_1dd){
return null;
}
var _1df=_1dd.style;
var _1e0={};
if(!_1de){
_1e0.zIndex=_1df.zIndex;
}
if(_1dc){
return _1e0;
}
var _1e1=this.getDimsObj(_1de);
_1e0.width=(_1e1.w?String(_1e1.w):"");
_1e0.height=(_1e1.h?String(_1e1.h):"");
_1e0[jetspeed.id.PP_WINDOW_POSITION_STATIC]=_1de;
_1e0[jetspeed.id.PP_WINDOW_HEIGHT_TO_FIT]=this.heightToFit;
if(!_1de){
_1e0.left=(_1e1.l!=null?String(_1e1.l):"");
_1e0.top=(_1e1.t!=null?String(_1e1.t):"");
}else{
var _1e2=jetspeed.page.getPortletCurColRow(_1dd);
if(_1e2!=null){
_1e0.column=_1e2.column;
_1e0.row=_1e2.row;
_1e0.layout=_1e2.layout;
}else{
throw new Error("Can't find row/col/layout for window: "+this.widgetId);
}
}
return _1e0;
},getCurWinStateForPersist:function(_1e3){
var _1e4=this.getCurWinState(_1e3);
this._mkNumProp(null,_1e4,"left");
this._mkNumProp(null,_1e4,"top");
this._mkNumProp(null,_1e4,"width");
this._mkNumProp(null,_1e4,"height");
return _1e4;
},_mkNumProp:function(_1e5,_1e6,_1e7){
var _1e8=(_1e6!=null&&_1e7!=null);
if(_1e5==null&&_1e8){
_1e5=_1e6[_1e7];
}
if(_1e5==null||_1e5.length==0){
_1e5=0;
}else{
var _1e9="";
for(var i=0;i<_1e5.length;i++){
var _1eb=_1e5.charAt(i);
if((_1eb>="0"&&_1eb<="9")||_1eb=="."){
_1e9+=_1eb.toString();
}
}
if(_1e9==null||_1e9.length==0){
_1e9="0";
}
if(_1e8){
_1e6[_1e7]=_1e9;
}
_1e5=new Number(_1e9);
}
return _1e5;
},setPortletContent:function(html,url){
var _1ee=jetspeed;
var _1ef=dojo;
var ie6=this.ie6;
var _1f1=null;
var _1f2=this.containerNode;
if(ie6){
_1f1=this.iNodeCss;
if(this.heightToFit){
this.iNodeCss=null;
this._alterCss(false,true);
}
}
var _1f3=html.toString();
if(!this.exclPContent){
_1f3="<div class=\"PContent\" >"+_1f3+"</div>";
}
var _1f4=this._splitAndFixPaths_scriptsonly(_1f3,url,_1ee);
var doc=_1f2.ownerDocument;
var _1f6=this.setContent(_1f4,doc,_1ef);
this.childWidgets=((_1f6&&_1f6.length>0)?_1f6:null);
var _1f7=false;
if(_1f4.scripts!=null&&_1f4.scripts.length!=null&&_1f4.scripts.length>0){
_1ee.page.win_onload=false;
this._executeScripts(_1f4.scripts,_1ef);
this.onLoad();
if(_1ee.page.win_onload&&(typeof setTimeout=="object")){
_1f7=true;
}
}
if(_1f7){
_1ef.lang.setTimeout(this,this._setPortletContentScriptsDone,20,_1ee,_1ef,_1f1);
}else{
this._setPortletContentScriptsDone(_1ee,_1ef,_1f1);
}
},_setPortletContentScriptsDone:function(_1f8,_1f9,_1fa,_1fb){
_1f8=(_1f8!=null?_1f8:jetspeed);
_1f9=(_1f9!=null?_1f9:dojo);
var _1fc=this.containerNode;
var doc=_1fc.ownerDocument;
var ie6=this.ie6;
if(this.portlet){
this.portlet.postParseAnnotateHtml(_1fc);
}
var _1ff=this.iframesInfo;
var _200=this.getIFramesAndObjects(true,false);
var _201=null,_202=false;
if(_200!=null){
if(_1ff==null){
this.iframesInfo=_1ff={layout:false};
var _203=doc.createElement("div");
var _204="portletWindowIFrameCover";
_203.className=_204;
_1fc.appendChild(_203);
if(_1f8.UAie){
_203.className=(_204+"IE")+" "+_204;
if(ie6){
_1ff.iframeCoverIE6Css=_1f8.css.cssWidthHeight.concat();
}
}
_1ff.iframeCover=_203;
_1f8.page.regPWinIFrameCover(this);
}
var _205=_1ff.iframesSize=_200.iframesSize;
var _206=_200.iframes;
var _207=_1ff.layout;
var _208=_1ff.layout=(_206&&_206.length==1&&_205[0].h!=null);
if(_207!=_208){
_202=true;
}
if(_208){
if(!this.heightToFit){
_201=_206[0];
}
var wDC=this.decConfig;
var _1fc=this.containerNode;
_1fc.firstChild.className="PContent portletIFramePContent";
_1fc.className=wDC.cNodeClass+" "+this.iframeCoverContainerClass;
if(!wDC.layoutIFrame){
this._createLayoutInfo(wDC,true,this.domNode,_1fc,this.tbNode,this.rbNode,_1f9,_1f8,_1f8.ui);
}
}
var _20a=null;
var _20b=_200.objects;
if(_20b){
var _20c=_1f8.page.swfInfo;
if(_20c){
for(var i=0;i<_20b.length;i++){
var _20e=_20b[i];
var _20f=_20e.id;
if(_20f){
var swfI=_20c[_20f];
if(swfI){
if(_20a==null){
_20a={};
}
_20a[_20f]=swfI;
}
}
}
}
}
if(_20a){
_1ff.swfInfo=_20a;
}else{
delete _1ff.swfInfo;
}
}else{
if(_1ff!=null){
if(_1ff.layout){
this.containerNode.className=this.decConfig.cNodeClass;
_202=true;
}
this.iframesInfo=null;
_1f8.page.unregPWinIFrameCover(this);
}
}
if(_202){
this._alterCss(false,false,true);
}
if(this.restoreOnNextRender){
this.restoreOnNextRender=false;
this.restoreWindow();
}
if(ie6){
this._updtDimsObj(false,false,true,false,true);
if(_1fa==null){
var _211=_1f8.css;
_1fa=_211.cssHeight.concat();
_1fa[_211.cssDis]="inline";
}
this.iNodeCss=_1fa;
this._alterCss(false,false,true);
}
if(this.minimizeOnNextRender){
this.minimizeOnNextRender=false;
this.minimizeWindow(true);
this.actionBtnSync(_1f8,_1f8.id);
this.needsRenderOnRestore=true;
}
if(_201){
this._deferSetIFrameH(_201,(!_1f8.UAie?"100%":"99%"),true);
}
},_setContentObjects:function(){
delete this._objectsInfo;
},setContent:function(data,doc,_214){
var _215=null;
var step=1;
try{
if(this._callOnUnload){
this.onUnload();
}
this._callOnUnload=true;
step=2;
this._setContent(data.xml,_214);
step=3;
if(this.parseContent){
var node=this.containerNode;
var _218=new _214.xml.Parse();
var frag=_218.parseElement(node,null,true);
_215=_214.widget.getParser().createSubComponents(frag,null);
}
}
catch(e){
dojo.hostenv.println("ERROR in PortletWindow ["+this.widgetId+"] setContent while "+(step==1?"running onUnload":(step==2?"setting innerHTML":"creating dojo widgets"))+" - "+jetspeed.formatError(e));
}
return _215;
},_setContent:function(cont,_21b){
this._destroyChildWidgets(_21b);
try{
var node=this.containerNode;
while(node.firstChild){
_21b.html.destroyNode(node.firstChild);
}
node.innerHTML=cont;
}
catch(e){
e.text="Couldn't load content:"+e.description;
this._handleDefaults(e,"onContentError");
}
},_splitAndFixPaths_scriptsonly:function(s,url,_21f){
var _220=true;
var _221,attr;
var _223=[];
var _224=/<script([^>]*)>([\s\S]*?)<\/script>/i;
var _225=/src=(['"]?)([^"']*)\1/i;
while(_221=_224.exec(s)){
if(_220&&_221[1]){
if(attr=_225.exec(_221[1])){
_223.push({path:attr[2]});
}
}
if(_221[2]){
var sc=_221[2];
if(!sc){
continue;
}
if(_220){
_223.push(sc);
}
}
s=s.substr(0,_221.index)+s.substr(_221.index+_221[0].length);
}
return {"xml":s,"styles":[],"titles":[],"requires":[],"scripts":_223,"url":url};
},onLoad:function(e){
this._runStack("_onLoadStack");
this.isLoaded=true;
},onUnload:function(e){
this._runStack("_onUnloadStack");
delete this.scriptScope;
},_runStack:function(_229){
var st=this[_229];
var err="";
var _22c=this.scriptScope||window;
for(var i=0;i<st.length;i++){
try{
st[i].call(_22c);
}
catch(e){
err+="\n"+st[i]+" failed: "+e.description;
}
}
this[_229]=[];
if(err.length){
var name=(_229=="_onLoadStack")?"addOnLoad":"addOnUnLoad";
this._handleDefaults(name+" failure\n "+err,"onExecError","debug");
}
},_executeScripts:function(_22f,_230){
var _231=jetspeed;
var _232=_230.hostenv;
var _233=_231.page;
var _234=document.getElementsByTagName("head")[0];
var tmp,uri,code="";
for(var i=0;i<_22f.length;i++){
if(!_22f[i].path){
tmp=this._fixScripts(_22f[i],true);
if(tmp){
code+=((code.length>0)?";":"")+tmp;
}
continue;
}
var uri=_22f[i].path;
var _239=null;
try{
_239=_232.getText(uri,null,false);
if(_239){
_239=this._fixScripts(_239,false);
code+=((code.length>0)?";":"")+_239;
}
}
catch(ex){
_230.debug("Error loading script for portlet ["+this.widgetId+"] url="+uri+" - "+_231.formatError(ex));
}
try{
if(_239&&!_231.containsElement("script","src",uri,_234)){
_231.addDummyScriptToHead(uri);
}
}
catch(ex){
_230.debug("Error added fake script element to head for portlet ["+this.widgetId+"] url="+uri+" - "+_231.formatError(ex));
}
}
try{
var djg=_230.global();
if(djg.execScript){
djg.execScript(code);
}else{
var djd=_230.doc();
var sc=djd.createElement("script");
sc.appendChild(djd.createTextNode(code));
(this.containerNode||this.domNode).appendChild(sc);
}
}
catch(e){
var _23d="Error running scripts for portlet ["+this.widgetId+"] - "+_231.formatError(e);
e.text=_23d;
_230.hostenv.println(_23d);
_230.hostenv.println(code);
}
},_fixScripts:function(_23e,_23f){
var _240=/\b([a-z_A-Z$]\w*)\s*\.\s*(addEventListener|attachEvent)\s*\(/;
var _241,_242,_243;
while(_241=_240.exec(_23e)){
_242=_241[1];
_243=_241[2];
_23e=_23e.substr(0,_241.index)+"jetspeed.postload_"+_243+"("+_242+","+_23e.substr(_241.index+_241[0].length);
}
var _244=/\b(document\s*.\s*write(ln)?)\s*\(/;
while(_241=_244.exec(_23e)){
_23e=_23e.substr(0,_241.index)+"jetspeed.postload_docwrite("+_23e.substr(_241.index+_241[0].length);
}
var _245=/(;\s|\s+)([a-z_A-Z$][\w.]*)\s*\.\s*(URL\s*|(location\s*(\.\s*href\s*){0,1}))=\s*(("[^"]*"|'[^']*'|[^;])[^;]*)/;
while(_241=_245.exec(_23e)){
var _246=_241[3];
_246=_246.replace(/^\s+|\s+$/g,"");
_23e=_23e.substr(0,_241.index)+_241[1]+"jetspeed.setdoclocation("+_241[2]+", \""+_246+"\", ("+_241[6]+"))"+_23e.substr(_241.index+_241[0].length);
}
if(_23f){
_23e=_23e.replace(/<!--|-->/g,"");
}
return _23e;
},_cacheSetting:function(_247,_248){
var _249=dojo.lang;
for(var x in this.bindArgs){
if(_249.isUndefined(_247[x])){
_247[x]=this.bindArgs[x];
}
}
if(_249.isUndefined(_247.useCache)){
_247.useCache=_248;
}
if(_249.isUndefined(_247.preventCache)){
_247.preventCache=!_248;
}
if(_249.isUndefined(_247.mimetype)){
_247.mimetype="text/html";
}
return _247;
},_handleDefaults:function(e,_24c,_24d){
var _24e=dojo;
if(!_24c){
_24c="onContentError";
}
if(_24e.lang.isString(e)){
e={text:e};
}
if(!e.text){
e.text=e.toString();
}
e.toString=function(){
return this.text;
};
if(typeof e.returnValue!="boolean"){
e.returnValue=true;
}
if(typeof e.preventDefault!="function"){
e.preventDefault=function(){
this.returnValue=false;
};
}
this[_24c](e);
if(e.returnValue){
switch(_24d){
case true:
case "alert":
alert(e.toString());
break;
case "debug":
_24e.debug(e.toString());
break;
default:
if(this._callOnUnload){
this.onUnload();
}
this._callOnUnload=false;
if(arguments.callee._loopStop){
_24e.debug(e.toString());
}else{
arguments.callee._loopStop=true;
this._setContent(e.toString(),_24e);
}
}
}
arguments.callee._loopStop=false;
},onExecError:function(e){
},onContentError:function(e){
},setPortletTitle:function(_251){
if(_251){
this.title=_251;
}else{
this.title="";
}
if(this.windowInitialized&&this.tbTextNode){
this.tbTextNode.innerHTML=this.title;
}
},getPortletTitle:function(){
return this.title;
},_IEPostDrag:function(){
if(!this.posStatic){
return;
}
var _252=this.domNode.parentNode;
dojo.dom.insertAtIndex(jetspeed.widget.ie6ZappedContentHelper,_252,0);
dojo.lang.setTimeout(this,this._IERemoveHelper,jetspeed.widget.ie6PostDragRmDelay);
},_IERemoveHelper:function(){
dojo.dom.removeNode(jetspeed.widget.ie6ZappedContentHelper);
}});
jetspeed.widget.showAllPortletWindows=function(){
var _253=jetspeed;
var _254=_253.css;
var _255=_254.cssDis,_256=_254.cssNoSelNm,_257=_254.cssNoSel,_258=_254.cssNoSelEnd;
var _259=_253.page.getPWins(false);
var _25a,_25b;
for(var i=0;i<_259.length;i++){
_25a=_259[i];
if(_25a){
_25b=_25a.dNodeCss;
_25b[_256]="";
_25b[_257]="";
_25b[_258]="";
_25b[_255]="block";
_25a.domNode.style.display="block";
_25a.domNode.style.visibility="visible";
}
}
};
jetspeed.widget.hideAllPortletWindows=function(_25d){
var _25e=jetspeed;
var _25f=_25e.css;
var _260=_25f.cssDis,_261=_25f.cssNoSelNm,_262=_25f.cssNoSel,_263=_25f.cssNoSelEnd;
var _264=_25e.page.getPWins(false);
var _265,_266,_267;
for(var i=0;i<_264.length;i++){
_266=_264[i];
_265=true;
if(_266&&_25d&&_25d.length>0){
for(var _269=0;_269<_25d.length;_269++){
if(_266.widgetId==_25d[_269]){
_265=false;
break;
}
}
}
if(_266){
_267=_266.dNodeCss;
_267[_261]="";
_267[_262]="";
_267[_263]="";
if(_265){
_267[_260]="none";
_266.domNode.style.display="none";
}else{
_267[_260]="block";
_266.domNode.style.display="block";
}
_266.domNode.style.visibility="visible";
}
}
};
jetspeed.widget.WinScroller=function(){
var _26a=this.jsObj;
this.UAmoz=_26a.UAmoz;
this.UAope=_26a.UAope;
};
dojo.extend(jetspeed.widget.WinScroller,{jsObj:jetspeed,djObj:dojo,typeNm:"WinScroller",V_AS_T:32,V_AS_V:16,autoScroll:function(e){
try{
var w=window;
var dy=0;
if(e.clientY<this.V_AS_T){
dy=-this.V_AS_V;
}else{
var _26e=null;
if(this.UAmoz){
_26e=w.innerHeight;
}else{
var doc=document,dd=doc.documentElement;
if(!this.UAope&&w.innerWidth){
_26e=w.innerHeight;
}else{
if(!this.UAope&&dd&&dd.clientWidth){
_26e=dd.clientHeight;
}else{
var b=jetspeed.docBody;
if(b.clientWidth){
_26e=b.clientHeight;
}
}
}
}
if(_26e!=null&&e.clientY>_26e-this.V_AS_T){
dy=this.V_AS_V;
}
}
w.scrollBy(0,dy);
}
catch(ex){
}
},_getErrMsg:function(ex,msg,_274,_275){
return ((_275!=null?(_275+"; "):"")+this.typeNm+" "+(_274==null?"<unknown>":_274.widgetId)+" "+msg+" ("+ex.toString()+")");
}});
jetspeed.widget.CreatePortletWindowResizeHandler=function(_276,_277){
var _278=new jetspeed.widget.PortletWindowResizeHandle(_276,_277);
var doc=document;
var _27a=doc.createElement("div");
_27a.className=_278.rhClass;
var _27b=doc.createElement("div");
_27a.appendChild(_27b);
_276.rbNode.appendChild(_27a);
_278.domNode=_27a;
_278.build();
return _278;
};
jetspeed.widget.PortletWindowResizeHandle=function(_27c,_27d){
this.pWin=_27c;
_27d.widget.WinScroller.call(this);
};
dojo.inherits(jetspeed.widget.PortletWindowResizeHandle,jetspeed.widget.WinScroller);
dojo.extend(jetspeed.widget.PortletWindowResizeHandle,{typeNm:"Resize",rhClass:"portletWindowResizeHandle",build:function(){
this.events=[jetspeed.ui.evtConnect("after",this.domNode,"onmousedown",this,"_beginSizing")];
},destroy:function(_27e,_27f,jsUI){
this._cleanUpLastEvt(_27e,_27f,jsUI);
jsUI.evtDisconnectWObjAry(this.events,_27e);
this.events=this.pWin=null;
},_cleanUpLastEvt:function(_281,_282,jsUI){
var _284=null;
try{
jsUI.evtDisconnectWObjAry(this.tempEvents,_281);
this.tempEvents=null;
}
catch(ex){
_284=this._getErrMsg(ex,"event clean-up error",this.pWin,_284);
}
try{
_282.page.displayAllPWinIFrameCovers(true);
}
catch(ex){
_284=this._getErrMsg(ex,"clean-up error",this.pWin,_284);
}
if(_284!=null){
dojo.raise(_284);
}
},_beginSizing:function(e){
if(this._isSizing){
return false;
}
var pWin=this.pWin;
var node=pWin.domNode;
if(!node){
return false;
}
this.targetDomNode=node;
var _288=jetspeed;
var jsUI=_288.ui;
var _28a=dojo;
var _28b=_28a.event;
var _28c=_288.docBody;
if(this.tempEvents!=null){
this._cleanUpLastEvt(_28b,_288,jsUI);
}
this._isSizing=true;
this.startPoint={x:e.pageX,y:e.pageY};
var mb=_28a.html.getMarginBox(node);
this.startSize={w:mb.width,h:mb.height};
var d=node.ownerDocument;
var _28f=[];
_28f.push(jsUI.evtConnect("after",_28c,"onmousemove",this,"_changeSizing",_28b,25));
_28f.push(jsUI.evtConnect("after",_28c,"onmouseup",this,"_endSizing",_28b));
_28f.push(jsUI.evtConnect("after",d,"ondragstart",_288,"_stopEvent",_28b));
_28f.push(jsUI.evtConnect("after",d,"onselectstart",_288,"_stopEvent",_28b));
_288.page.displayAllPWinIFrameCovers(false);
this.tempEvents=_28f;
try{
e.preventDefault();
}
catch(ex){
}
},_changeSizing:function(e){
var pWin=this.pWin;
if(pWin.heightToFit){
pWin.makeHeightVariable(true,true);
}
try{
if(!e.pageX||!e.pageY){
return;
}
}
catch(ex){
return;
}
this.autoScroll(e);
var dx=this.startPoint.x-e.pageX;
var dy=this.startPoint.y-e.pageY;
var newW=this.startSize.w-dx;
var newH=this.startSize.h-dy;
var _296=pWin.posStatic;
if(_296){
newW=this.startSize.w;
}
if(this.minSize){
var mb=dojo.html.getMarginBox(this.targetDomNode);
if(newW<this.minSize.w){
newW=mb.width;
}
if(newH<this.minSize.h){
newH=mb.height;
}
}
pWin.resizeTo(newW,newH);
try{
e.preventDefault();
}
catch(ex){
}
},_endSizing:function(e){
var _299=jetspeed;
var _29a=dojo;
this._cleanUpLastEvt(_29a.event,_299,_299.ui);
this.pWin.actionBtnSyncDefer(true,_299,_29a);
this._isSizing=false;
}});
jetspeed.widget.ie6PostDragAddDelay=60;
jetspeed.widget.ie6PostDragRmDelay=120;
jetspeed.widget.BackgroundIframe=function(node,_29c,_29d){
if(!_29c){
_29c=this.defaultStyleClass;
}
var html="<iframe src='' frameborder='0' scrolling='no' class='"+_29c+"'>";
this.iframe=_29d.doc().createElement(html);
this.iframe.tabIndex=-1;
node.appendChild(this.iframe);
};
dojo.lang.extend(jetspeed.widget.BackgroundIframe,{defaultStyleClass:"ie6BackgroundIFrame",iframe:null});
if(!dojo.dnd){
dojo.dnd={};
}
dojo.dnd.Mover=function(_29f,_2a0,_2a1,_2a2,_2a3,e,_2a5,_2a6,_2a7){
var jsUI=_2a7.ui;
var _2a9=_2a6.event;
_2a7.widget.WinScroller.call(this);
if(_2a7.widget._movingInProgress){
if(djConfig.isDebug){
_2a7.debugAlert("ERROR - Mover initiation before previous Mover was destroyed");
}
}
_2a7.widget._movingInProgress=true;
this.moveInitiated=false;
this.moveableObj=_2a3;
this.windowOrLayoutWidget=_29f;
this.node=_2a0;
this.dragLayoutColumn=_2a1;
this.cL_NA_ED=_2a2;
this.posStatic=_29f.posStatic;
this.notifyOnAbsolute=_2a5;
if(e.ctrlKey&&_29f.moveAllowTilingChg){
if(this.posStatic){
this.changeToUntiled=true;
}else{
if(_2a7.prefs.windowTiling){
this.changeToTiled=true;
this.changeToTiledStarted=false;
}
}
}
this.posRecord={};
this.disqualifiedColumnIndexes={};
if(_2a1!=null){
this.disqualifiedColumnIndexes=_2a1.col.getDescendantCols();
}
this.marginBox={l:e.pageX,t:e.pageY};
var doc=this.node.ownerDocument;
var _2ab=[];
var _2ac=jsUI.evtConnect("after",doc,"onmousemove",this,"onFirstMove",_2a9);
_2ab.push(jsUI.evtConnect("after",doc,"onmousemove",this,"onMouseMove",_2a9));
_2ab.push(jsUI.evtConnect("after",doc,"onmouseup",this,"mouseUpDestroy",_2a9));
_2ab.push(jsUI.evtConnect("after",doc,"ondragstart",_2a7,"_stopEvent",_2a9));
_2ab.push(jsUI.evtConnect("after",doc,"onselectstart",_2a7,"_stopEvent",_2a9));
if(_2a7.UAie6){
_2ab.push(jsUI.evtConnect("before",doc,"onmousedown",this,"mouseDownDestroy",_2a9));
_2ab.push(jsUI.evtConnect("before",_2a3.handle,"onmouseup",_2a3,"onMouseUp",_2a9));
}
_2a7.page.displayAllPWinIFrameCovers(false);
_2ab.push(_2ac);
this.events=_2ab;
this.pSLastColChgIdx=null;
this.pSLastColChgTime=null;
this.pSLastNaturalColChgYTest=null;
this.pSLastNaturalColChgHistory=null;
this.pSLastNaturalColChgChoiceMap=null;
this.isDebug=false;
if(_2a7.debug.dragWindow){
this.isDebug=true;
this.devKeepLastMsg=null;
this.devKeepLastCount=0;
this.devLastX=null;
this.devLastY=null;
this.devLastTime=null,this.devLastColI=null;
this.devChgTh=30;
this.devLrgTh=200;
this.devChgSubsqTh=10;
this.devTimeTh=6000;
this.devI=_2a7.debugindent;
this.devIH=_2a7.debugindentH;
this.devIT=_2a7.debugindentT;
this.devI3=_2a7.debugindent3;
this.devICH=_2a7.debugindentch;
}
};
dojo.inherits(dojo.dnd.Mover,jetspeed.widget.WinScroller);
dojo.extend(dojo.dnd.Mover,{typeNm:"Mover",pSColChgTimeTh:3000,onMouseMove:function(e){
var _2ae=this.jsObj;
var _2af=this.djObj;
var _2b0=this.UAmoz;
this.autoScroll(e);
var m=this.marginBox;
var _2b2=false;
var x=m.l+e.pageX;
var y=m.t+e.pageY;
var _2b5=this.isDebug;
var _2b6=false;
var _2b7=null,_2b8=null,_2b9,_2ba,_2bb,_2bc,_2bd;
if(_2b5){
_2b9=this.devI;
_2ba=this.devIH;
_2bb=this.devI3;
_2bc=this.devICH,_2bd=this.devIT;
_2b7=(new Date().getTime());
if(this.devLastX==null||this.devLastY==null){
this.devLastX=x;
this.devLastY=y;
}else{
var _2be=(Math.abs(x-this.devLastX)>this.devLrgTh)||(Math.abs(y-this.devLastY)>this.devLrgTh);
if(!_2be&&this.devLastTime!=null&&((this.devLastTime+this.devTimeTh)>_2b7)){
}else{
if(Math.abs(x-this.devLastX)>this.devChgTh){
this.devLastX=x;
_2b6=true;
}
if(Math.abs(y-this.devLastY)>this.devChgTh){
this.devLastY=y;
_2b6=true;
}
}
}
}
if(_2b0&&this.firstEvtAdjustXY!=null){
x=x+this.firstEvtAdjustXY.l;
y=y+this.firstEvtAdjustXY.t;
this.firstEvtAdjustXY=null;
_2b2=true;
}
_2ae.ui.setMarginBox(this.node,x,y,null,null,this.nodeLayoutInfo,_2ae,_2af);
var _2bf=this.posRecord;
_2bf.left=x;
_2bf.top=y;
var _2c0=false;
var _2c1=this.posStatic;
if(!_2c1){
if(!_2b2&&this.changeToTiled&&!this.changeToTiledStarted){
_2c0=true;
_2c1=true;
}
}
if(_2c1&&!_2b2){
var _2c2=this.columnInfoArray;
var _2c3=_2ae.page.columns;
var _2c4=this.heightHalf;
var _2c5=_2c3.length;
var _2c6=e.pageX;
var _2c7=y+_2c4;
var _2c8=this.pSLastColChgIdx;
var _2c9=this.pSLastNaturalColChgChoiceMap;
var _2ca=null,_2cb=[],_2cc=null;
var _2cd,_2ce,_2cf,_2d0,lowY,_2d2,_2d3,_2d4,_2d5;
for(var i=0;i<_2c5;i++){
_2cd=_2c2[i];
if(_2cd!=null){
if(_2c6>=_2cd.left&&_2c6<=_2cd.right){
if(_2c7>=(_2cd.top-30)||(_2c9!=null&&_2c9[i]!=null)){
_2ce=Math.min(Math.abs(_2c7-(_2cd.top)),Math.abs(e.pageY-(_2cd.top)));
_2cf=Math.min(Math.abs(_2c7-(_2cd.yhalf)),Math.abs(e.pageY-(_2cd.yhalf)));
_2d0=Math.min(Math.abs(_2c7-_2cd.bottom),Math.abs(e.pageY-_2cd.bottom));
lowY=Math.min(_2ce,_2cf);
lowY=Math.min(lowY,_2d0);
_2d3=null;
_2d5=_2ca;
while(_2d5!=null){
_2d4=_2cb[_2d5];
if(lowY<_2d4.lowY){
break;
}else{
_2d3=_2d4;
_2d5=_2d4.nextIndex;
}
}
_2cb.push({index:i,lowY:lowY,nextIndex:_2d5,lowYAlign:((!_2b5)?null:(lowY==_2ce?"^":(lowY==_2cf?"~":"_")))});
_2d2=(_2cb.length-1);
if(_2d3!=null){
_2d3.nextIndex=_2d2;
}else{
_2ca=_2d2;
}
if(i==_2c8){
_2cc=lowY;
}
}else{
if(_2b5){
if(_2b8==null){
_2b8=[];
}
var _2d7=(_2cd.top-30)-_2c7;
_2b8.push(_2af.string.padRight(String(i),2,_2bc)+" y! "+_2af.string.padRight(String(_2d7),4,_2bc));
}
}
}else{
if(_2b5&&_2c6>_2cd.width){
if(_2b8==null){
_2b8=[];
}
var _2d7=_2c6-_2cd.width;
_2b8.push(_2af.string.padRight(String(i),2,_2bc)+" x! "+_2af.string.padRight(String(_2d7),4,_2bc));
}
}
}
}
var _2d8=-1;
var _2d9=-1,_2da=-1;
var _2db=null,_2dc=null,_2dd=null,_2de=null,_2df=null;
if(_2ca!=null){
_2d4=_2cb[_2ca];
_2d8=_2d4.index;
_2db=_2d4.lowY;
if(_2d4.nextIndex!=null){
_2d4=_2cb[_2d4.nextIndex];
_2d9=_2d4.index;
_2dc=_2d4.lowY;
_2de=_2dc-_2db;
if(_2d4.nextIndex!=null){
_2d4=_2cb[_2d4.nextIndex];
_2da=_2d4.index;
_2dd=_2d4.lowY;
_2df=_2dd-_2db;
}
}
}
var _2e0=null;
var _2e1=(new Date().getTime());
var _2e2=this.pSLastNaturalColChgYTest;
if(_2cc==null||(_2e2!=null&&Math.abs(_2c7-_2e2)>=Math.max((_2c4-Math.floor(_2c4*0.3)),Math.min(_2c4,21)))){
if(_2d8>=0){
this.pSLastNaturalColChgYTest=_2c7;
this.pSLastNaturalColChgHistory=[_2d8];
_2c9={};
_2c9[_2d8]=true;
this.pSLastNaturalColChgChoiceMap=_2c9;
}
}else{
if(_2e2==null){
this.pSLastNaturalColChgYTest=_2c7;
_2d8=_2c8;
this.pSLastNaturalColChgHistory=[_2d8];
_2c9={};
_2c9[_2d8]=true;
this.pSLastNaturalColChgChoiceMap=_2c9;
}else{
var _2e3=null;
var _2e4=this.pSLastColChgTime+this.pSColChgTimeTh;
if(_2e4<_2e1){
var _2e5=this.pSLastNaturalColChgHistory;
var _2e6=(_2e5==null?0:_2e5.length);
var _2e7=null,_2e8;
_2d5=_2ca;
while(_2d5!=null){
_2d4=_2cb[_2d5];
colI=_2d4.index;
if(_2e6==0){
_2e3=colI;
break;
}else{
_2e8=false;
for(var i=(_2e6-1);i>=0;i--){
if(_2e5[i]==colI){
if(_2e7==null||_2e7>i){
_2e7=i;
_2e3=colI;
}
_2e8=true;
break;
}
}
if(!_2e8){
_2e3=colI;
break;
}
}
_2d5=_2d4.nextIndex;
}
if(_2e3!=null){
_2d8=_2e3;
_2c9[_2d8]=true;
if(_2e6==0||_2e5[(_2e6-1)]!=_2d8){
_2e5.push(_2d8);
}
}
}else{
_2d8=_2c8;
}
if(_2b5&&_2e3!=null){
_2af.hostenv.println(_2b9+"ColChg YTest="+_2e2+" LeastRecentColI="+_2e3+" History=["+(this.pSLastNaturalColChgHistory?this.pSLastNaturalColChgHistory.join(", "):"")+"] Map={"+_2ae.printobj(this.pSLastNaturalColChgChoiceMap)+"} expire="+(_2e1-_2e4)+"}");
}
}
}
if(_2b5&&_2e0!=null){
if(this.devKeepLastMsg!=null){
_2af.hostenv.println(this.devKeepLastMsg);
this.devKeepLastMsg=null;
this.devKeepLastCount=0;
}
_2af.hostenv.println(_2e0);
}
var col=(_2d8>=0?_2c3[_2d8]:null);
if(_2b5){
if(this.devLastColI!=_2d8){
_2b6=true;
}
this.devLastColI=_2d8;
}
var _2eb=_2ae.widget.pwGhost;
if(_2c0){
if(col!=null){
_2ae.ui.setMarginBox(_2eb,null,null,null,m.h,this.nodeLayoutInfo,_2ae,_2af);
_2eb.col=null;
this.changeToTiledStarted=true;
this.posStatic=true;
}
}
var _2ec=null,_2ed=false,_2ee=false;
if(_2eb.col!=col&&col!=null){
this.pSLastColChgTime=_2e1;
this.pSLastColChgIdx=_2d8;
var _2ef=_2eb.col;
if(_2ef!=null){
_2af.dom.removeNode(_2eb);
}
_2eb.col=col;
var _2f0=_2c2[_2d8];
var _2f1=_2f0.childCount+1;
_2f0.childCount=_2f1;
if(_2f1==1){
_2c3[_2d8].domNode.style.height="";
}
col.domNode.appendChild(_2eb);
_2ee=true;
var _2f2=(_2c8!=null?((_2c8!=_2d8)?_2c2[_2c8]:null):(_2ef!=null?_2c2[_2ef.getPageColumnIndex()]:null));
if(_2f2!=null){
var _2f3=_2f2.childCount-1;
if(_2f3<0){
_2f3=0;
}
_2f2.childCount=_2f3;
if(_2f3==0){
_2c3[_2f2.pageColIndex].domNode.style.height="1px";
}
}
}
var _2f4=null,_2f5=null;
if(col!=null){
_2f4=_2ae.ui.getPWinAndColChildren(col.domNode,_2eb,true,false,true,false);
_2f5=_2f4.matchingNodes;
}
if(_2f5!=null&&_2f5.length>1){
var _2f6=_2f4.matchNodeIndexInMatchingNodes;
var _2f7=-1;
var _2f8=-1;
if(_2f6>0){
var _2f7=_2af.html.getAbsolutePosition(_2f5[_2f6-1],true).y;
if((y-25)<=_2f7){
_2af.dom.removeNode(_2eb);
_2ec=_2f5[_2f6-1];
_2af.dom.insertBefore(_2eb,_2ec,true);
}
}
if(_2f6!=(_2f5.length-1)){
var _2f8=_2af.html.getAbsolutePosition(_2f5[_2f6+1],true).y;
if((y+10)>=_2f8){
if(_2f6+2<_2f5.length){
_2ec=_2f5[_2f6+2];
_2af.dom.insertBefore(_2eb,_2ec,true);
}else{
col.domNode.appendChild(_2eb);
_2ed=true;
}
}
}
}
if(_2b6){
var _2f9="";
if(_2ec!=null||_2ed||_2ee){
_2f9="put=";
if(_2ec!=null){
_2f9+="before("+_2ec.id+")";
}else{
if(_2ed){
_2f9+="end";
}else{
if(_2ee){
_2f9+="end-default";
}
}
}
}
_2af.hostenv.println(_2b9+"col="+_2d8+_2ba+_2f9+_2ba+"x="+x+_2ba+"y="+y+_2ba+"ePGx="+e.pageX+_2ba+"ePGy="+e.pageY+_2ba+"yTest="+_2c7);
var _2fa="",colI,_2cd;
_2d5=_2ca;
while(_2d5!=null){
_2d4=_2cb[_2d5];
colI=_2d4.index;
_2cd=_2c2[_2d4.index];
_2fa+=(_2fa.length>0?_2bd:"")+colI+_2d4.lowYAlign+(colI<10?_2bc:"")+" -> "+_2af.string.padRight(String(_2d4.lowY),4,_2bc);
_2d5=_2d4.nextIndex;
}
_2af.hostenv.println(_2bb+_2fa);
if(_2b8!=null){
var _2fb="";
for(i=0;i<_2b8.length;i++){
_2fb+=(i>0?_2bd:"")+_2b8[i];
}
_2af.hostenv.println(_2bb+_2fb);
}
this.devLastTime=_2b7;
this.devChgTh=this.devChgSubsqTh;
}
}
},onFirstMove:function(){
var _2fc=this.jsObj;
var jsUI=_2fc.ui;
var _2fe=this.djObj;
var _2ff=this.windowOrLayoutWidget;
var node=this.node;
var _301=_2ff._getLayoutInfoMoveable();
this.nodeLayoutInfo=_301;
var mP=_2ff._getWindowMarginBox(_301,_2fc);
this.staticWidth=null;
var _303=_2fc.widget.pwGhost;
var _304=this.UAmoz;
var _305=this.changeToUntiled;
var _306=this.changeToTiled;
var m=null;
if(this.posStatic){
if(!_305){
var _308=_2ff.getPageColumnIndex();
var _309=(_308>=0?_2fc.page.columns[_308]:null);
_303.col=_309;
this.pSLastColChgTime=new Date().getTime();
this.pSLastColChgIdx=_308;
}
m={w:mP.w,h:mP.h};
var _30a=node.parentNode;
var _30b=document.getElementById(_2fc.id.DESKTOP);
var _30c=node.style;
this.staticWidth=_30c.width;
var _30d=_2fe.html.getAbsolutePosition(node,true);
var _30e=_301.mE;
m.l=_30d.left-_30e.l;
m.t=_30d.top-_30e.t;
if(_304){
if(!_305){
jsUI.setMarginBox(_303,null,null,null,mP.h,_301,_2fc,_2fe);
}
this.firstEvtAdjustXY={l:m.l,t:m.t};
}
_30c.position="absolute";
if(!_305){
_30c.zIndex=_2fc.page.getPWinHighZIndex()+1;
}else{
_30c.zIndex=(_2ff._setAsTopZIndex(_2fc.page,_2fc.css,_2ff.dNodeCss,false));
}
if(!_305){
_30a.insertBefore(_303,node);
if(!_304){
jsUI.setMarginBox(_303,null,null,null,mP.h,_301,_2fc,_2fe);
}
_30b.appendChild(node);
var _30f=jsUI.getPWinAndColChildren(_30a,_303,true,false,true);
this.prevColumnNode=_30a;
this.prevIndexInCol=_30f.matchNodeIndexInMatchingNodes;
}else{
_2ff._updtDimsObj(false,true);
_30b.appendChild(node);
}
}else{
m=mP;
}
this.moveInitiated=true;
m.l-=this.marginBox.l;
m.t-=this.marginBox.t;
this.marginBox=m;
jsUI.evtDisconnectWObj(this.events.pop(),_2fe.event);
var _310=this.disqualifiedColumnIndexes;
var _311=(this.isDebug||_2fc.debug.dragWindowStart),_312;
if(_311){
_312=_2fc.debugindentT;
var _313=_2fc.debugindentH;
var _314="";
if(_310!=null){
_314=_313+"dqCols=["+_2fc.objectKeys(_310).join(", ")+"]";
}
var _315=_2ff.title;
if(_315==null){
_315=node.id;
}
_2fe.hostenv.println("DRAG \""+_315+"\""+_313+((this.posStatic&&!_305)?("col="+(_303.col?_303.col.getPageColumnIndex():"null")+_313):"")+"m.l = "+m.l+_313+"m.t = "+m.t+_314);
}
if(this.posStatic||_306){
this.heightHalf=mP.h/2;
var _316=this.dragLayoutColumn||{};
var _317=jsUI.updateChildColInfo(node,_310,_316.maxdepth,this.cL_NA_ED,(_311?1:null),_312);
if(_311){
_2fe.hostenv.println(_312+"--------------------");
}
this.columnInfoArray=_317;
}
if(this.posStatic){
jsUI.setMarginBox(node,m.l,m.t,mP.w,null,_301,_2fc,_2fe);
if(this.notifyOnAbsolute){
_2ff.dragChangeToAbsolute(this,node,this.marginBox,_2fe,_2fc);
}
if(_305){
this.posStatic=false;
}
}
},mouseDownDestroy:function(e){
var _319=this.jsObj;
_319.stopEvent(e);
this.mouseUpDestroy();
},mouseUpDestroy:function(){
var _31a=this.djObj;
var _31b=this.jsObj;
this.destroy(_31a,_31a.event,_31b,_31b.ui);
},destroy:function(_31c,_31d,_31e,jsUI){
var _320=this.windowOrLayoutWidget;
var node=this.node;
var _322=null;
if(this.moveInitiated&&_320&&node){
this.moveInitiated=false;
try{
if(this.posStatic){
var _323=_31e.widget.pwGhost;
var _324=node.style;
if(_323&&_323.col){
_320.column=0;
_31c.dom.insertBefore(node,_323,true);
}else{
if(this.prevColumnNode!=null&&this.prevIndexInCol!=null){
_31c.dom.insertAtIndex(node,this.prevColumnNode,this.prevIndexInCol);
}else{
var _325=_31e.page.getColumnDefault();
if(_325!=null){
_31c.dom.prependChild(node,_325.domNode);
}
}
}
if(_323){
_31c.dom.removeNode(_323);
}
}
_320.endDragging(this.posRecord,this.changeToUntiled,this.changeToTiled);
}
catch(ex){
_322=this._getErrMsg(ex,"destroy reset-window error",_320,_322);
}
}
try{
jsUI.evtDisconnectWObjAry(this.events,_31d);
if(this.moveableObj!=null){
this.moveableObj.mover=null;
}
this.events=this.node=this.windowOrLayoutWidget=this.moveableObj=this.prevColumnNode=this.prevIndexInCol=null;
}
catch(ex){
_322=this._getErrMsg(ex,"destroy event clean-up error",_320,_322);
if(this.moveableObj!=null){
this.moveableObj.mover=null;
}
}
try{
_31e.page.displayAllPWinIFrameCovers(true);
}
catch(ex){
_322=this._getErrMsg(ex,"destroy clean-up error",_320,_322);
}
_31e.widget._movingInProgress=false;
if(_322!=null){
_31c.raise(_322);
}
}});
dojo.dnd.Moveable=function(_326,opt){
var _328=jetspeed;
var jsUI=_328.ui;
var _32a=dojo;
var _32b=_32a.event;
this.windowOrLayoutWidget=_326;
this.handle=opt.handle;
var _32c=[];
_32c.push(jsUI.evtConnect("after",this.handle,"onmousedown",this,"onMouseDown",_32b));
_32c.push(jsUI.evtConnect("after",this.handle,"ondragstart",_328,"_stopEvent",_32b));
_32c.push(jsUI.evtConnect("after",this.handle,"onselectstart",_328,"_stopEvent",_32b));
this.events=_32c;
};
dojo.extend(dojo.dnd.Moveable,{minMove:5,enabled:true,mover:null,onMouseDown:function(e){
if(e&&e.button==2){
return;
}
var _32e=dojo;
var _32f=_32e.event;
var _330=jetspeed;
var jsUI=jetspeed.ui;
if(this.mover!=null||this.tempEvents!=null){
this._cleanUpLastEvt(_32e,_32f,_330,jsUI);
_330.stopEvent(e);
}else{
if(this.enabled){
if(this.tempEvents!=null){
if(djConfig.isDebug){
_330.debugAlert("ERROR: Moveable onmousedown tempEvent already defined");
}
}else{
var _332=[];
var doc=this.handle.ownerDocument;
_332.push(jsUI.evtConnect("after",doc,"onmousemove",this,"onMouseMove",_32f));
this.tempEvents=_332;
}
if(!this.windowOrLayoutWidget.posStatic){
this.windowOrLayoutWidget.bringToTop(e,false,true,_330);
}
this._lastX=e.pageX;
this._lastY=e.pageY;
this._mDownEvt=e;
}
}
_330.stopEvent(e);
},onMouseMove:function(e,_335){
var _336=jetspeed;
var _337=dojo;
var _338=_337.event;
if(_335||Math.abs(e.pageX-this._lastX)>this.minMove||Math.abs(e.pageY-this._lastY)>this.minMove){
this._cleanUpLastEvt(_337,_338,_336,_336.ui);
var _339=this.windowOrLayoutWidget;
this.beforeDragColRowInfo=null;
if(!_339.isLayoutPane){
var _33a=_339.domNode;
if(_33a!=null){
this.node=_33a;
this.mover=new _337.dnd.Mover(_339,_33a,null,_339.cL_NA_ED,this,e,false,_337,_336);
}
}else{
_339.startDragging(e,this,_337,_336);
}
}
_336.stopEvent(e);
},onMouseUp:function(e,_33c){
var _33d=dojo;
var _33e=jetspeed;
this._cleanUpLastEvt(_33d,_33d.event,_33e,_33e.ui,_33c);
},_cleanUpLastEvt:function(_33f,_340,_341,jsUI,_343){
if(this._mDownEvt!=null){
_341.stopEvent(this._mDownEvt,_343);
this._mDownEvt=null;
}
if(this.mover!=null){
this.mover.destroy(_33f,_340,_341,jsUI);
this.mover=null;
}
jsUI.evtDisconnectWObjAry(this.tempEvents,_340);
this.tempEvents=null;
},destroy:function(_344,_345,_346,jsUI){
this._cleanUpLastEvt(_344,_345,_346,jsUI);
jsUI.evtDisconnectWObjAry(this.events,_345);
this.events=this.node=this.handle=this.windowOrLayoutWidget=this.beforeDragColRowInfo=null;
},enable:function(){
this.enabled=true;
},disable:function(){
this.enabled=false;
}});
dojo.getMarginBox=function(node,_349,_34a){
var s=_349||dojo.gcs(node),me=dojo._getMarginExtents(node,s,_34a);
var l=node.offsetLeft-me.l,t=node.offsetTop-me.t;
if(_34a.UAmoz){
var sl=parseFloat(s.left),st=parseFloat(s.top);
if(!isNaN(sl)&&!isNaN(st)){
l=sl,t=st;
}else{
var p=node.parentNode;
if(p){
var pcs=dojo.gcs(p);
if(pcs.overflow!="visible"){
var be=dojo._getBorderExtents(p,pcs);
l+=be.l,t+=be.t;
}
}
}
}else{
if(_34a.UAope){
var p=node.parentNode;
if(p){
var be=dojo._getBorderExtents(p);
l-=be.l,t-=be.t;
}
}
}
return {l:l,t:t,w:node.offsetWidth+me.w,h:node.offsetHeight+me.h};
};
dojo.getContentBox=function(node,_355,_356){
var s=_355||dojo.gcs(node),pe=dojo._getPadExtents(node,s),be=dojo._getBorderExtents(node,s),w=node.clientWidth,h;
if(!w){
w=node.offsetWidth,h=node.offsetHeight;
}else{
h=node.clientHeight,be.w=be.h=0;
}
if(_356.UAope){
pe.l+=be.l;
pe.t+=be.t;
}
return {l:pe.l,t:pe.t,w:w-pe.w-be.w,h:h-pe.h-be.h};
};
dojo.setMarginBox=function(node,_35d,_35e,_35f,_360,_361,_362){
var s=_361||dojo.gcs(node);
var bb=dojo._usesBorderBox(node),pb=bb?{l:0,t:0,w:0,h:0}:dojo._getPadBorderExtents(node,s),mb=dojo._getMarginExtents(node,s,_362);
if(_35f!=null&&_35f>=0){
_35f=Math.max(_35f-pb.w-mb.w,0);
}
if(_360!=null&&_360>=0){
_360=Math.max(_360-pb.h-mb.h,0);
}
dojo._setBox(node,_35d,_35e,_35f,_360);
};
dojo._setBox=function(node,l,t,w,h,u){
u=u||"px";
with(node.style){
if(l!=null&&!isNaN(l)){
left=l+u;
}
if(t!=null&&!isNaN(t)){
top=t+u;
}
if(w!=null&&w>=0){
width=w+u;
}
if(h!=null&&h>=0){
height=h+u;
}
}
};
dojo._usesBorderBox=function(node){
var n=node.tagName;
return false;
};
dojo._getPadExtents=function(n,_370){
var s=_370||dojo.gcs(n),px=dojo._toPixelValue,l=px(n,s.paddingLeft),t=px(n,s.paddingTop);
return {l:l,t:t,w:l+px(n,s.paddingRight),h:t+px(n,s.paddingBottom)};
};
dojo._getPadBorderExtents=function(n,_376){
var s=_376||dojo.gcs(n),p=dojo._getPadExtents(n,s),b=dojo._getBorderExtents(n,s);
return {l:p.l+b.l,t:p.t+b.t,w:p.w+b.w,h:p.h+b.h};
};
dojo._getMarginExtents=function(n,_37b,_37c){
var s=_37b||dojo.gcs(n),px=dojo._toPixelValue,l=px(n,s.marginLeft),t=px(n,s.marginTop),r=px(n,s.marginRight),b=px(n,s.marginBottom);
if(_37c.UAsaf&&(s.position!="absolute")){
r=l;
}
return {l:l,t:t,w:l+r,h:t+b};
};
dojo._getBorderExtents=function(n,_384){
var ne="none",px=dojo._toPixelValue,s=_384||dojo.gcs(n),bl=(s.borderLeftStyle!=ne?px(n,s.borderLeftWidth):0),bt=(s.borderTopStyle!=ne?px(n,s.borderTopWidth):0);
return {l:bl,t:bt,w:bl+(s.borderRightStyle!=ne?px(n,s.borderRightWidth):0),h:bt+(s.borderBottomStyle!=ne?px(n,s.borderBottomWidth):0)};
};
if(!jetspeed.UAie){
var dv=document.defaultView;
dojo.getComputedStyle=((jetspeed.UAsaf)?function(node){
var s=dv.getComputedStyle(node,null);
if(!s&&node.style){
node.style.display="";
s=dv.getComputedStyle(node,null);
}
return s||{};
}:function(node){
return dv.getComputedStyle(node,null);
});
dojo._toPixelValue=function(_38d,_38e){
return (parseFloat(_38e)||0);
};
}else{
dojo.getComputedStyle=function(node){
return node.currentStyle;
};
dojo._toPixelValue=function(_390,_391){
if(!_391){
return 0;
}
if(_391.slice&&(_391.slice(-2)=="px")){
return parseFloat(_391);
}
with(_390){
var _392=style.left;
var _393=runtimeStyle.left;
runtimeStyle.left=currentStyle.left;
try{
style.left=_391;
_391=style.pixelLeft;
}
catch(e){
_391=0;
}
style.left=_392;
runtimeStyle.left=_393;
}
return _391;
};
}
dojo.gcs=dojo.getComputedStyle;

