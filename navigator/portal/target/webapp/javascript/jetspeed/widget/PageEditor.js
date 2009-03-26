dojo.provide("jetspeed.widget.PageEditor");
dojo.require("dojo.widget.*");
dojo.require("dojo.io.*");
dojo.require("dojo.event.*");
dojo.require("dojo.string.extras");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.widget.Dialog");
dojo.require("dojo.widget.Select");
dojo.require("dojo.widget.Button");
dojo.require("dojo.widget.Spinner");
dojo.require("dojo.html.common");
dojo.require("dojo.html.display");
dojo.require("jetspeed.widget.PageEditPane");
dojo.require("jetspeed.widget.LayoutEditPane");
jetspeed.widget.PageEditor=function(){
};
dojo.widget.defineWidget("jetspeed.widget.PageEditor",dojo.widget.HtmlWidget,{deletePortletDialog:null,deletePortletDialogBg:null,deletePortletDialogFg:null,deleteLayoutDialog:null,deleteLayoutDialogBg:null,deleteLayoutDialogFg:null,columnSizeDialog:null,columnSizeDialogBg:null,columnSizeDialogFg:null,detail:null,editorInitiatedFromDesktop:false,isContainer:true,widgetsInTemplate:true,loadTimeDistribute:jetspeed.UAie,dbOn:djConfig.isDebug,styleBase:"pageEditorPaneContainer",styleBaseAdd:(jetspeed.UAie?"pageEditorPaneContainerIE":"pageEditorPaneContainerNotIE"),styleDetail:"pageEditorDetailContainer",styleDetailAdd:(jetspeed.UAie?"pageEditorDetailContainerIE":"pageEditorDetailContainerNotIE"),postMixInProperties:function(_1,_2,_3){
var _4=jetspeed;
_4.widget.PageEditor.superclass.postMixInProperties.apply(this,arguments);
this.layoutImagesRoot=_4.prefs.getLayoutRootUrl()+"/images/desktop/";
this.labels=_4.prefs.pageEditorLabels;
this.dialogLabels=_4.prefs.pageEditorDialogLabels;
this.templateCssPath=new dojo.uri.Uri(_4.url.basePortalDesktopUrl()+"/javascript/jetspeed/widget/PageEditor.css");
this.templatePath=new dojo.uri.Uri(_4.url.basePortalDesktopUrl()+"/javascript/jetspeed/widget/PageEditor.html");
},fillInTemplate:function(_5,_6){
var _7=jetspeed;
var _8=dojo;
var _9=this;
this.deletePortletDialog=_8.widget.createWidget("dialog",{widgetsInTemplate:true,deletePortletConfirmed:function(){
this.hide();
_9.deletePortletConfirmed(this.portletEntityId);
}},this.deletePortletDialog);
this.deletePortletDialog.setCloseControl(this.deletePortletDialog.deletePortletCancel.domNode);
this.deleteLayoutDialog=_8.widget.createWidget("dialog",{widgetsInTemplate:true,deleteLayoutConfirmed:function(){
this.hide();
_9.deleteLayoutConfirmed(this.portletEntityId);
}},this.deleteLayoutDialog);
this.deleteLayoutDialog.setCloseControl(this.deleteLayoutDialog.deleteLayoutCancel.domNode);
var _a={};
_a.widgetsInTemplate=true;
_a.columnSizeConfirmed=function(){
var _b=0;
var _c=new Array();
for(var i=0;i<this.columnCount;i++){
var _e=this["spinner"+i];
var _f=new Number(_e.getValue());
_c.push(_f);
_b+=_f;
}
if(_b>100){
alert("Sum of column sizes cannot exceed 100.");
}else{
this.hide();
_9.columnSizeConfirmed(this.layoutId,_c);
}
};
this.columnSizeDialog=_8.widget.createWidget("dialog",_a,this.columnSizeDialog);
this.columnSizeDialog.setCloseControl(this.columnSizeDialog.columnSizeCancel.domNode);
_7.widget.PageEditor.superclass.fillInTemplate.call(this);
},postCreate:function(_10,_11,_12){
var _13=false;
var _14=null;
if(this.editModeMove||this.checkPerm(this.PM_MZ_P,jetspeed)){
_14={execEditMode:true};
}
this.editPageInitiate(_14);
},editPageInitiate:function(_15){
var _16=null;
if(this.editorInitiatedFromDesktop){
_16=new jetspeed.widget.EditPageGetThemesContentManager(this,false,false,true,true,true,_15);
}else{
_16=new jetspeed.widget.EditPageGetThemesContentManager(this,true,true,true,false,false,_15);
}
_16.getContent();
},editPageBuild:function(_17){
var _18=jetspeed;
var _19=_18.page;
var _1a=dojo;
this.pageEditorWidgets=new Array();
this.layoutEditPaneWidgets=new Array();
var _1b=_1a.widget.createWidget("jetspeed:PageEditPane",{layoutDecoratorDefinitions:_19.themeDefinitions.pageDecorations,portletDecoratorDefinitions:_19.themeDefinitions.portletDecorations,layoutImagesRoot:this.layoutImagesRoot,labels:this.labels,dialogLabels:this.dialogLabels});
_1b.pageEditorWidget=this;
var _1c=_1b.domNode.style;
_1c.display="none";
_1c.visibility="hidden";
_1a.dom.insertAfter(_1b.domNode,this.domNode);
this.pageEditorWidgets.push(_1b);
this.pageEditPaneWidget=_1b;
_18.url.loadingIndicatorStep(_18);
this._buildDepth=0;
this._buildRootPane(_17);
if(_17!=null&&_17.execEditMode){
_1a.lang.setTimeout(this,this.editMoveModeStart,100);
}
},_buildRootPane:function(_1d){
var _1e=jetspeed;
var _1f=_1e.page;
var _20=dojo;
var _21=(_1d!=null);
var _22=_20.widget.createWidget("jetspeed:LayoutEditPane",{widgetId:"layoutEdit_root",layoutId:_1f.rootFragmentId,isRootLayout:true,depth:0,layoutDefinitions:_1f.themeDefinitions.layouts,layoutImagesRoot:this.layoutImagesRoot,labels:this.labels,dialogLabels:this.dialogLabels,startInEditModeMove:_21});
_22.pageEditorWidget=this;
var _23=_22.domNode.style;
_23.display="none";
_23.visibility="hidden";
_20.dom.insertAfter(_22.domNode,this.pageEditPaneWidget.domNode);
this.pageEditorWidgets.push(_22);
this.layoutEditPaneWidgets.push(_22);
this._buildNextColI=0;
this._buildColLen=(_1e.prefs.windowTiling?_1f.columns.length:0);
if(!this.loadTimeDistribute){
_1e.url.loadingIndicatorStep(_1e);
this._buildNextPane();
}else{
_20.lang.setTimeout(this,this._buildNextPane,10);
_1e.url.loadingIndicatorStep(_1e);
}
},_buildNextPane:function(){
var _24=jetspeed;
var _25=_24.page;
var _26=dojo;
var i=this._buildNextColI;
var _28=this._buildColLen;
if(i<_28){
var col,_2a=null;
while(i<_28&&_2a==null){
col=_25.columns[i];
if(col.layoutHeader){
_2a=_26.widget.createWidget("jetspeed:LayoutEditPane",{widgetId:"layoutEdit_"+i,layoutColumn:col,layoutId:col.layoutId,depth:col.layoutDepth,layoutInfo:_25.layoutInfo.columnLayoutHeader,layoutDefinitions:_25.themeDefinitions.layouts,layoutImagesRoot:this.layoutImagesRoot,labels:this.labels,dialogLabels:this.dialogLabels});
_2a.pageEditorWidget=this;
var _2b=_2a.domNode.style;
_2b.display="none";
_2b.visibility="hidden";
if(col.domNode.firstChild!=null){
col.domNode.insertBefore(_2a.domNode,col.domNode.firstChild);
}else{
col.domNode.appendChild(_2a.domNode);
}
_2a.initializeDrag();
this.pageEditorWidgets.push(_2a);
this.layoutEditPaneWidgets.push(_2a);
}
i++;
}
}
if(i<_28){
this._buildNextColI=i;
if(!this.loadTimeDistribute){
_24.url.loadingIndicatorStep(_24);
this._buildNextPane();
}else{
_26.lang.setTimeout(this,this._buildNextPane,10);
_24.url.loadingIndicatorStep(_24);
}
}else{
_26.lang.setTimeout(this,this._buildFinished,10);
}
},_buildFinished:function(){
var _2c=jetspeed;
if(_2c.UAie){
this.bgIframe=new _2c.widget.BackgroundIframe(this.domNode,"ieLayoutBackgroundIFrame",dojo);
}
var _2d=this.pageEditorWidgets;
if(_2d!=null){
for(var i=0;i<_2d.length;i++){
var _2f=_2d[i].domNode.style;
_2f.display="block";
_2f.visibility="visible";
}
}
this.editPageSyncPortletActions(true,_2c);
_2c.url.loadingIndicatorHide();
if(_2c.UAie6){
_2c.page.displayAllPWins();
}
},editPageSyncPortletActions:function(_30,_31){
var _32=_31.page;
var _33=_31.css;
if(_30){
_31.ui.updateChildColInfo();
}
var _34=_32.getPortletArray();
if(_34!=null){
for(var i=0;i<_34.length;i++){
_34[i].syncActions();
}
}
var _36=_31.widget.PageEditor.prototype;
var _37=this.checkPerm(this.PM_P_D,_31,_36);
var _38=this.canL_NA_ED(_31,_36);
var _39=_32.portlet_windows;
for(var _3a in _39){
var _3b=_39[_3a];
if(!_3b){
continue;
}
if(_30){
_3b.editPageInitiate(_37,_38,_31,_33);
}else{
_3b.editPageTerminate(_31,_33);
}
}
},editPageHide:function(){
var _3c=this.pageEditorWidgets;
if(_3c!=null){
for(var i=0;i<_3c.length;i++){
_3c[i].hide();
}
}
this.hide();
this.editPageSyncPortletActions(false,jetspeed);
},editPageShow:function(){
var _3e=jetspeed;
var _3f=this.pageEditorWidgets;
var _40=this.editModeMove;
if(_3f!=null){
for(var i=0;i<_3f.length;i++){
_3f[i].editModeRedisplay(_40);
}
}
this.show();
this.editPageSyncPortletActions(true,_3e);
if(_40){
this.editMoveModeStart();
}
if(_3e.UAie6){
_3e.page.displayAllPWins();
}
},editPageDestroy:function(){
var _42=this.pageEditorWidgets;
if(_42!=null){
for(var i=0;i<_42.length;i++){
_42[i].destroy();
_42[i]=null;
}
}
this.pageEditorWidgets=null;
this.layoutEditPaneWidgets=null;
this.pageEditPaneWidget=null;
if(this.deletePortletDialog!=null){
this.deletePortletDialog.destroy();
}
if(this.deleteLayoutDialog!=null){
this.deleteLayoutDialog.destroy();
}
if(this.columnSizeDialog!=null){
this.columnSizeDialog.destroy();
}
this.destroy();
},deletePortlet:function(_44,_45){
this.deletePortletDialog.portletEntityId=_44;
this.deletePortletDialog.portletTitle=_45;
this.deletePortletTitle.innerHTML=_45;
this._openDialog(this.deletePortletDialog);
},deletePortletConfirmed:function(_46){
var _47=new jetspeed.widget.RemovePortletContentManager(_46,this);
_47.getContent();
},deleteLayout:function(_48){
this.deleteLayoutDialog.layoutId=_48;
this.deleteLayoutDialog.layoutTitle=_48;
this.deleteLayoutTitle.innerHTML=_48;
this._openDialog(this.deleteLayoutDialog);
},deleteLayoutConfirmed:function(){
var _49=new jetspeed.widget.RemoveLayoutContentManager(this.deleteLayoutDialog.layoutId,this);
_49.getContent();
},openColumnSizesEditor:function(_4a){
var _4b=null;
if(_4a!=null){
_4b=jetspeed.page.layouts[_4a];
}
if(_4b!=null&&_4b.columnSizes!=null&&_4b.columnSizes.length>0){
var _4c=5;
var _4d=0;
for(var i=0;i<_4c;i++){
var _4f=this.columnSizeDialog["spinner"+i];
var _50=this["spinner"+i+"Field"];
if(i<_4b.columnSizes.length){
_4f.setValue(_4b.columnSizes[i]);
_50.style.display="block";
_4f.show();
_4d++;
}else{
_50.style.display="none";
_4f.hide();
}
}
this.columnSizeDialog.layoutId=_4a;
this.columnSizeDialog.columnCount=_4d;
this._openDialog(this.columnSizeDialog);
}
},columnSizeConfirmed:function(_51,_52){
if(_51!=null&&_52!=null&&_52.length>0){
var _53=jetspeed.page.layouts[_51];
var _54=null;
if(_53!=null){
_54=_53.name;
}
if(_54!=null){
var _55="";
for(var i=0;i<_52.length;i++){
if(i>0){
_55+=",";
}
_55+=_52[i]+"%";
}
var _57=new jetspeed.widget.UpdateFragmentContentManager(_51,_54,_55,this);
_57.getContent();
}
}
},checkPerm:function(p,_59,_5a){
var _5b=_5a||_59.widget.PageEditor.prototype;
var _5c=_5b.perms;
if(_5c==null){
_5c=_5b.perms=_59.page._perms(_59.prefs,-1,String.fromCharCode);
}
if(_5c==null){
return false;
}
if(p){
return ((_5c[0]&p)>0);
}
return _5c;
},getLDepthPerm:function(_5d,_5e){
var _5f=this.checkPerm(null,_5d,_5e);
if(_5f&&_5f.length>=2){
return _5f[1];
}
return -1;
},canL_NA_ED:function(_60,_61){
var _62=_61||_60.widget.PageEditor.prototype;
var _63=_62.checkPerm(_62.PM_L_NA_ED,_60,_62);
if(!_63){
_63=_62.hasRPerm(_60,_62);
}
return _63;
},hasRPerm:function(_64,_65){
var _66=_65||_64.widget.PageEditor.prototype;
var _67=false;
if(typeof _65.permR!="undefined"){
_67=_65.permR;
}else{
var _68=this.checkPerm(null,_64,_65);
if(_68&&_68.length>=3&&_68[2]&&_68[2].length>0){
var u=_64.page._getU();
if(u&&u.r&&u.r[_68[2]]){
_67=true;
}
}
_65.permR=_67;
}
return _67;
},refreshPage:function(){
dojo.lang.setTimeout(this,this._doRefreshPage,10);
},_doRefreshPage:function(){
var _6a=jetspeed;
var _6b=_6a.page.getPageUrl();
if(!_6a.prefs.ajaxPageNavigation){
var _6c=0;
var wt=null;
if(this.editModeMove){
if(!this.checkPerm(this.PM_MZ_P,jetspeed)){
_6c|=this.PM_MZ_P;
}
var _6e=/\b([a-z_A-Z$]\w*)\b(?!-)/;
var _6f=_6a.page.getPWins();
var _70=0;
var _71=[];
for(var i=0;i<_6f.length;i++){
pWin=_6f[i];
if(pWin&&pWin.portlet){
var _73=pWin.getPortletTitle();
if(_73!=null&&_73.length>0){
var _74=pWin.portlet.entityId;
if(!_6e.test(_74)){
_74="\""+_74+"\"";
}
if(_70>0){
_71.push(",");
}
_71.push(_74+":\""+_73+"\"");
_70++;
if(_71.length>1024){
_71=null;
break;
}
}
}
}
if(_70>0&&_71!=null&&_71.length>0){
wt="";
var _75=_71.join("");
var _76=_75.length;
var _77,_78,_79;
for(var _7a=0;_7a<_76;_7a++){
_77=_75.charCodeAt(_7a);
_78=(_77).toString(16);
_79=_78.length;
if(_79<1||_79>2){
wt=null;
_71=null;
break;
}else{
if(_79==1){
_78+="0";
}
}
wt+=_78;
}
if(wt==null||wt.length==0){
wt=null;
}else{
_6c|=this.PM_MZ_P;
}
}
}
var _7b=_6b;
if(wt!=null||_6c>0){
var _7c=_6a.url;
_7b=_7c.parse(_6b.toString());
if(_6c>0){
_7b=_7c.addQueryParameter(_7b,_6a.id.PG_ED_STATE_PARAM,(_6c).toString(16),true);
}
if(wt!=null&&wt.length>0){
_7b=_7c.addQueryParameter(_7b,_6a.id.PG_ED_TITLES_PARAM,wt,true);
}
}
_6a.pageNavigate(_7b.toString(),null,true);
}else{
_6a.updatePage(_6b.toString(),false,true,{editModeMove:this.editModeMove});
}
},editMoveModeExit:function(_7d){
var _7e=jetspeed;
var _7f=_7e.UAie6;
if(_7f){
_7e.page.displayAllPWins(true);
}
_7e.widget.PortletWindow.prototype.restoreAllFromMinimizeWindowTemporarily();
var _80=this.layoutEditPaneWidgets;
if(_80!=null){
for(var i=0;i<_80.length;i++){
_80[i]._disableMoveMode();
}
}
if(!_7d){
delete this.editModeMove;
}
},editMoveModeStart:function(){
var _82=jetspeed;
var _83=false;
if(_82.UAie6){
_82.page.displayAllPWins(true);
}
var _84=[];
var _85=[];
if(this.dbOn){
var _86=_82.debugWindow();
if(_86&&(!_83||!_86.posStatic||_82.debug.dragWindow)){
_84.push(_86);
_85.push(_86.widgetId);
}
}
if(!_83){
var _87;
var _88=_82.page.getPWins();
for(var i=0;i<_88.length;i++){
_87=_88[i];
if(_87.posStatic){
_84.push(_87);
_85.push(_87.widgetId);
_87.minimizeWindowTemporarily();
}
}
}
_82.widget.hideAllPortletWindows(_85);
var _8a=this.layoutEditPaneWidgets;
if(_8a!=null){
for(var i=0;i<_8a.length;i++){
_8a[i]._enableMoveMode();
}
}
if(_82.UAie6){
setTimeout(function(){
_82.page.displayAllPWins(false,_84);
},20);
}
this.editModeMove=true;
},onBrowserWindowResize:function(){
var _8b=this.deletePortletDialog;
var _8c=this.deleteLayoutDialog;
var _8d=this.columnSizeDialog;
if(_8b&&_8b.isShowing()){
_8b.domNode.style.display="none";
_8b.domNode.style.display="block";
}
if(_8c&&_8c.isShowing()){
_8c.domNode.style.display="none";
_8c.domNode.style.display="block";
}
if(_8d&&_8d.isShowing()){
_8d.domNode.style.display="none";
_8d.domNode.style.display="block";
}
var _8e=this.pageEditorWidgets;
if(_8e!=null){
for(var i=0;i<_8e.length;i++){
_8e[i].onBrowserWindowResize();
}
}
},PM_PG_L_D:16,PM_L_N:32,PM_L_CS:64,PM_PG_AD:128,PM_P_AD:256,PM_PG_P_D:512,PM_P_D:1024,PM_MZ_P:2048,PM_L_NA_ED:4096,PM_L_NA_TLMV:8192,PM_L_NA_CS:16384,_openDialog:function(_90){
var _91=jetspeed.UAmoz;
if(_91){
_90.domNode.style.position="fixed";
if(!_90._fixedIPtBug){
var _92=_90;
_92.placeModalDialog=function(){
var _93=dojo.html.getScroll().offset;
var _94=dojo.html.getViewport();
var mb;
if(_92.isShowing()){
mb=dojo.html.getMarginBox(_92.domNode);
}else{
dojo.html.setVisibility(_92.domNode,false);
dojo.html.show(_92.domNode);
mb=dojo.html.getMarginBox(_92.domNode);
dojo.html.hide(_92.domNode);
dojo.html.setVisibility(_92.domNode,true);
}
var x=(_94.width-mb.width)/2;
var y=(_94.height-mb.height)/2;
with(_92.domNode.style){
left=x+"px";
top=y+"px";
}
};
_92._fixedIPtBug=true;
}
}
_90.show();
}});
jetspeed.widget.EditPageGetThemesContentManager=function(_98,_99,_9a,_9b,_9c,_9d,_9e){
this.pageEditorWidget=_98;
var _9f=new Array();
if(_99){
_9f.push(["pageDecorations"]);
}
if(_9a){
_9f.push(["portletDecorations"]);
}
if(_9b){
_9f.push(["layouts"]);
}
if(_9c){
_9f.push(["desktopPageDecorations","pageDecorations"]);
}
if(_9d){
_9f.push(["desktopPortletDecorations","portletDecorations"]);
}
this.getThemeTypes=_9f;
this.getThemeTypeNextIndex=0;
this.startInEditModeMove=_9e;
};
jetspeed.widget.EditPageGetThemesContentManager.prototype={getContent:function(){
if(this.getThemeTypes!=null&&this.getThemeTypes.length>this.getThemeTypeNextIndex){
var _a0="?action=getthemes&type="+this.getThemeTypes[this.getThemeTypeNextIndex][0]+"&format=json";
var _a1=jetspeed.url.basePortalUrl()+jetspeed.url.path.AJAX_API+_a0;
var _a2=new jetspeed.om.Id("getthemes",{});
var _a3={};
_a3.url=_a1;
_a3.mimetype="text/json";
jetspeed.url.retrieveContent(_a3,this,_a2,jetspeed.debugContentDumpIds);
}else{
this.pageEditorWidget.editPageBuild(this.startInEditModeMove);
}
},notifySuccess:function(_a4,_a5,_a6){
if(jetspeed.page.themeDefinitions==null){
jetspeed.page.themeDefinitions={};
}
var _a7=((this.getThemeTypes[this.getThemeTypeNextIndex].length>1)?this.getThemeTypes[this.getThemeTypeNextIndex][1]:this.getThemeTypes[this.getThemeTypeNextIndex][0]);
jetspeed.page.themeDefinitions[_a7]=_a4;
this.getThemeTypeNextIndex++;
this.getContent();
},notifyFailure:function(_a8,_a9,_aa,_ab){
dojo.raise("EditPageGetThemesContentManager notifyFailure url: "+_aa+" type: "+_a8+jetspeed.formatError(_a9));
}};
jetspeed.widget.RemovePageContentManager=function(_ac){
this.pageEditorWidget=_ac;
};
jetspeed.widget.RemovePageContentManager.prototype={getContent:function(){
var _ad="?action=updatepage&method=remove";
var _ae=jetspeed.url.basePortalUrl()+jetspeed.url.path.AJAX_API+jetspeed.page.getPath()+_ad;
var _af=new jetspeed.om.Id("updatepage-remove-page",{});
var _b0={};
_b0.url=_ae;
_b0.mimetype="text/xml";
jetspeed.url.retrieveContent(_b0,this,_af,jetspeed.debugContentDumpIds);
},notifySuccess:function(_b1,_b2,_b3){
if(jetspeed.url.checkAjaxApiResponse(_b2,_b1,null,true,"updatepage-remove-page")){
var _b4=jetspeed.page.makePageUrl("/");
_b4+="?"+jetspeed.id.PG_ED_PARAM+"=true";
window.location.href=_b4;
}
},notifyFailure:function(_b5,_b6,_b7,_b8){
dojo.raise("RemovePageContentManager notifyFailure url: "+_b7+" type: "+_b5+jetspeed.formatError(_b6));
}};
jetspeed.widget.IE6ZappedContentRestorer=function(_b9){
this.colNodes=_b9;
this.nextColNodeIndex=0;
};
jetspeed.widget.IE6ZappedContentRestorer.prototype={showNext:function(){
if(this.colNodes&&this.colNodes.length>this.nextColNodeIndex){
dojo.dom.insertAtIndex(jetspeed.widget.ie6ZappedContentHelper,this.colNodes[this.nextColNodeIndex],0);
dojo.lang.setTimeout(this,this.removeAndShowNext,20);
}
},removeAndShowNext:function(){
dojo.dom.removeNode(jetspeed.widget.ie6ZappedContentHelper);
this.nextColNodeIndex++;
if(this.colNodes&&this.colNodes.length>this.nextColNodeIndex){
dojo.lang.setTimeout(this,this.showNext,20);
}
}};
jetspeed.widget.AddPageContentManager=function(_ba,_bb,_bc,_bd,_be,_bf,_c0){
this.pageRealPath=_ba;
this.pagePath=_bb;
this.pageName=_bc;
if(_bd==null){
if(jetspeed.page.themeDefinitions!=null&&jetspeed.page.themeDefinitions.layouts!=null&&jetspeed.page.themeDefinitions.layouts.length>0&&jetspeed.page.themeDefinitions.layouts[0]!=null&&jetspeed.page.themeDefinitions.layouts[0].length==2){
_bd=jetspeed.page.themeDefinitions.layouts[0][1];
}
}
this.layoutName=_bd;
this.pageTitle=_be;
this.pageShortTitle=_bf;
this.pageEditorWidget=_c0;
};
jetspeed.widget.AddPageContentManager.prototype={getContent:function(){
if(this.pageRealPath!=null&&this.pageName!=null){
var _c1="?action=updatepage&method=add&path="+escape(this.pageRealPath)+"&name="+escape(this.pageName);
if(this.layoutName!=null){
_c1+="&defaultLayout="+escape(this.layoutName);
}
if(this.pageTitle!=null){
_c1+="&title="+escape(this.pageTitle);
}
if(this.pageShortTitle!=null){
_c1+="&short-title="+escape(this.pageShortTitle);
}
var _c2=jetspeed.url.basePortalUrl()+jetspeed.url.path.AJAX_API+_c1;
var _c3=new jetspeed.om.Id("updatepage-add-page",{});
var _c4={};
_c4.url=_c2;
_c4.mimetype="text/xml";
jetspeed.url.retrieveContent(_c4,this,_c3,jetspeed.debugContentDumpIds);
}
},notifySuccess:function(_c5,_c6,_c7){
if(jetspeed.url.checkAjaxApiResponse(_c6,_c5,null,true,"updatepage-add-page")){
var _c8=jetspeed.page.makePageUrl(this.pagePath);
if(!dojo.string.endsWith(_c8,".psml",true)){
_c8+=".psml";
}
_c8+="?"+jetspeed.id.PG_ED_PARAM+"=true";
window.location.href=_c8;
}
},notifyFailure:function(_c9,_ca,_cb,_cc){
dojo.raise("AddPageContentManager notifyFailure url: "+_cb+" type: "+_c9+jetspeed.formatError(_ca));
}};
jetspeed.widget.MoveLayoutContentManager=function(_cd,_ce,_cf,row,_d1){
this.layoutId=_cd;
this.moveToLayoutId=_ce;
this.column=_cf;
this.row=row;
this.pageEditorWidget=_d1;
};
jetspeed.widget.MoveLayoutContentManager.prototype={getContent:function(){
if(this.layoutId!=null&&this.moveToLayoutId!=null){
var _d2="?action=moveabs&id="+this.layoutId+"&layoutid="+this.moveToLayoutId;
if(this.column!=null){
_d2+="&col="+this.column;
}
if(this.row!=null){
_d2+="&row="+this.row;
}
var _d3=jetspeed.url.basePortalUrl()+jetspeed.url.path.AJAX_API+jetspeed.page.getPath()+_d2;
var _d4=new jetspeed.om.Id("moveabs-layout",this.layoutId);
var _d5={};
_d5.url=_d3;
_d5.mimetype="text/xml";
jetspeed.url.retrieveContent(_d5,this,_d4,jetspeed.debugContentDumpIds);
}
},notifySuccess:function(_d6,_d7,_d8){
if(jetspeed.url.checkAjaxApiResponse(_d7,_d6,null,true,"moveabs-layout")){
}
},notifyFailure:function(_d9,_da,_db,_dc){
dojo.raise("MoveLayoutContentManager notifyFailure url: "+_db+" type: "+_d9+jetspeed.formatError(_da));
}};
jetspeed.widget.UpdateFragmentContentManager=function(_dd,_de,_df,_e0){
this.layoutId=_dd;
this.layoutName=_de;
this.layoutSizes=_df;
this.pageEditorWidget=_e0;
};
jetspeed.widget.UpdateFragmentContentManager.prototype={getContent:function(){
if(this.layoutId!=null){
var _e1="?action=updatepage&method=update-fragment&id="+this.layoutId;
if(this.layoutName!=null){
_e1+="&layout="+escape(this.layoutName);
}
if(this.layoutSizes!=null){
_e1+="&sizes="+escape(this.layoutSizes);
}
var _e2=jetspeed.url.basePortalUrl()+jetspeed.url.path.AJAX_API+jetspeed.page.getPath()+_e1;
var _e3=new jetspeed.om.Id("updatepage-update-fragment",{});
var _e4={};
_e4.url=_e2;
_e4.mimetype="text/xml";
jetspeed.url.retrieveContent(_e4,this,_e3,jetspeed.debugContentDumpIds);
}
},notifySuccess:function(_e5,_e6,_e7){
if(jetspeed.url.checkAjaxApiResponse(_e6,_e5,null,true,"updatepage-update-fragment")){
this.pageEditorWidget.refreshPage();
}
},notifyFailure:function(_e8,_e9,_ea,_eb){
dojo.raise("UpdateFragmentContentManager notifyFailure url: "+_ea+" type: "+_e8+jetspeed.formatError(_e9));
}};
jetspeed.widget.UpdatePageInfoContentManager=function(_ec,_ed,_ee){
this.refreshPage=((_ee.editorInitiatedFromDesktop)?true:false);
this.layoutDecorator=_ec;
this.portletDecorator=_ed;
this.pageEditorWidget=_ee;
};
jetspeed.widget.UpdatePageInfoContentManager.prototype={getContent:function(){
var _ef="?action=updatepage&method=info";
if(this.layoutDecorator!=null){
_ef+="&layout-decorator="+escape(this.layoutDecorator);
}
if(this.portletDecorator!=null){
_ef+="&portlet-decorator="+escape(this.portletDecorator);
}
var _f0=jetspeed.url.basePortalUrl()+jetspeed.url.path.AJAX_API+jetspeed.page.getPath()+_ef;
var _f1=new jetspeed.om.Id("updatepage-info",{});
var _f2={};
_f2.url=_f0;
_f2.mimetype="text/xml";
jetspeed.url.retrieveContent(_f2,this,_f1,jetspeed.debugContentDumpIds);
},notifySuccess:function(_f3,_f4,_f5){
if(jetspeed.url.checkAjaxApiResponse(_f4,_f3,null,true,"updatepage-info")){
if(this.refreshPage){
this.pageEditorWidget.refreshPage();
}
}
},notifyFailure:function(_f6,_f7,_f8,_f9){
dojo.raise("UpdatePageInfoContentManager notifyFailure url: "+_f8+" type: "+_f6+jetspeed.formatError(_f7));
}};
jetspeed.widget.RemovePortletContentManager=function(_fa,_fb){
this.portletEntityId=_fa;
this.pageEditorWidget=_fb;
};
jetspeed.widget.RemovePortletContentManager.prototype={getContent:function(){
if(this.portletEntityId!=null){
var _fc="?action=remove&id="+this.portletEntityId;
var _fd=jetspeed.url.basePortalUrl()+jetspeed.url.path.AJAX_API+jetspeed.page.getPath()+_fc;
var _fe=new jetspeed.om.Id("removeportlet",{});
var _ff={};
_ff.url=_fd;
_ff.mimetype="text/xml";
jetspeed.url.retrieveContent(_ff,this,_fe,jetspeed.debugContentDumpIds);
}
},notifySuccess:function(data,_101,_102){
if(jetspeed.url.checkAjaxApiResponse(_101,data,null,true,"removeportlet")){
this.pageEditorWidget.refreshPage();
}
},notifyFailure:function(type,_104,_105,_106){
dojo.raise("RemovePortletContentManager notifyFailure url: "+_105+" type: "+type+jetspeed.formatError(_104));
}};
jetspeed.widget.RemoveLayoutContentManager=function(_107,_108){
this.layoutId=_107;
this.pageEditorWidget=_108;
};
jetspeed.widget.RemoveLayoutContentManager.prototype={getContent:function(){
if(this.layoutId!=null){
var _109="?action=updatepage&method=remove-fragment&id="+this.layoutId;
var _10a=jetspeed.url.basePortalUrl()+jetspeed.url.path.AJAX_API+jetspeed.page.getPath()+_109;
var _10b=new jetspeed.om.Id("removelayout",{});
var _10c={};
_10c.url=_10a;
_10c.mimetype="text/xml";
jetspeed.url.retrieveContent(_10c,this,_10b,jetspeed.debugContentDumpIds);
}
},notifySuccess:function(data,_10e,_10f){
if(jetspeed.url.checkAjaxApiResponse(_10e,data,null,true,"removeportlet")){
this.pageEditorWidget.refreshPage();
}
},notifyFailure:function(type,_111,_112,_113){
dojo.raise("RemoveLayoutContentManager notifyFailure url: "+_112+" type: "+type+jetspeed.formatError(_111));
}};
jetspeed.widget.AddLayoutContentManager=function(_114,_115,_116){
this.parentLayoutId=_114;
this.layoutName=_115;
this.pageEditorWidget=_116;
};
jetspeed.widget.AddLayoutContentManager.prototype={getContent:function(){
if(this.parentLayoutId!=null){
var _117="?action=updatepage&method=add-fragment&layoutid="+this.parentLayoutId+(this.layoutName!=null?("&layout="+this.layoutName):"");
var _118=jetspeed.url.basePortalUrl()+jetspeed.url.path.AJAX_API+jetspeed.page.getPath()+_117;
var _119=new jetspeed.om.Id("addlayout",{});
var _11a={};
_11a.url=_118;
_11a.mimetype="text/xml";
jetspeed.url.retrieveContent(_11a,this,_119,jetspeed.debugContentDumpIds);
}
},notifySuccess:function(data,_11c,_11d){
if(jetspeed.url.checkAjaxApiResponse(_11c,data,null,true,"addlayout")){
this.pageEditorWidget.refreshPage();
}
},notifyFailure:function(type,_11f,_120,_121){
dojo.raise("AddLayoutContentManager notifyFailure url: "+_120+" type: "+type+jetspeed.formatError(_11f));
}};

