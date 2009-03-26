dojo.provide("jetspeed.widget.LayoutEditPane");
dojo.provide("jetspeed.widget.LayoutEditPaneMoveHandle");
dojo.require("dojo.widget.*");
dojo.require("dojo.io.*");
dojo.require("dojo.event.*");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.widget.Dialog");
dojo.require("dojo.widget.Select");
dojo.require("dojo.widget.Button");
dojo.require("dojo.html.common");
dojo.require("dojo.html.display");
jetspeed.widget.LayoutEditPane=function(){
};
dojo.widget.defineWidget("jetspeed.widget.LayoutEditPane",dojo.widget.HtmlWidget,{layoutId:null,layoutDefinitions:null,layoutColumn:null,layoutInfo:null,parentLayoutInfo:null,pageEditContainer:null,pageEditLNContainer:null,layoutNameSelect:null,buttonGroupRight:null,deleteLayoutButton:null,editMoveModeButton:null,editMoveModeExitButton:null,layoutMoveContainer:null,isContainer:true,widgetsInTemplate:true,isLayoutPane:true,depth:null,drag:null,posStatic:true,moveModeLayoutRelative:"movemode_layout",moveModes:["movemode_layout","movemode_portlet"],postMixInProperties:function(_1,_2,_3){
jetspeed.widget.LayoutEditPane.superclass.postMixInProperties.apply(this,arguments);
this.templateCssPath=new dojo.uri.Uri(jetspeed.url.basePortalDesktopUrl()+"/javascript/jetspeed/widget/LayoutEditPane.css");
this.templatePath=new dojo.uri.Uri(jetspeed.url.basePortalDesktopUrl()+"/javascript/jetspeed/widget/LayoutEditPane.html");
},fillInTemplate:function(_4,_5){
jetspeed.widget.LayoutEditPane.superclass.fillInTemplate.call(this);
},getCurrentLayout:function(){
var _6=null;
if(this.layoutId!=null){
_6=jetspeed.page.layouts[this.layoutId];
}
return _6;
},destroy:function(){
var _7=jetspeed;
if(this.layoutColumn){
_7.ui.evtDisconnect("after",this.layoutColumn,"layoutDepthChanged",this,"syncLayoutDepth");
}
_7.widget.LayoutEditPane.superclass.destroy.call(this);
},postCreate:function(_8,_9,_a){
var _b=jetspeed;
var _c=dojo;
var _d=_c.html;
var _e=_b.widget.PageEditor.prototype;
if(this.pageEditContainer!=null){
_d.addClass(this.pageEditContainer,_e.styleBaseAdd);
}
if(this.pageEditLNContainer!=null){
_d.addClass(this.pageEditLNContainer,_e.styleDetailAdd);
}
if(this.layoutNameSelect!=null){
var _f=this.getCurrentLayout();
var _10=null;
if(_f!=null){
_10=_f.name;
}
var _11=[];
if(this.layoutDefinitions){
for(var i=0;i<this.layoutDefinitions.length;i++){
var _13=this.layoutDefinitions[i];
if(_13&&_13.length==2){
_11.push([_13[0],_13[1]]);
if(_10==_13[1]){
this.layoutNameSelect.setAllValues(_13[0],_13[1]);
}
}
}
}
this.layoutNameSelect.dataProvider.setData(_11);
}
var _f=this.getCurrentLayout();
var _14=(_f==null||_f.layoutActionsDisabled);
var _15=false;
if(_14){
_15=_e.canL_NA_ED(_b,_e);
if(_15){
_14=false;
}else{
this["_n"+"a"]=true;
}
}
this.syncButtons(this.startInEditModeMove);
delete this.startInEditModeMove;
this.layoutMoveContainer=_c.widget.createWidget("jetspeed:LayoutEditPaneMoveHandle",{layoutImagesRoot:this.layoutImagesRoot});
this.addChild(this.layoutMoveContainer);
this.domNode.appendChild(this.layoutMoveContainer.domNode);
var _16=(!_14&&_e.checkPerm(_e.PM_P_AD,_b,_e));
if(!_16&&this.addPortletButton){
this.addPortletButton.domNode.style.display="none";
}
var _17=_e.checkPerm(_e.PM_L_CS,_b,_e);
if(_17&&_14){
_17=_e.checkPerm(_e.PM_L_NA_CS,_b,_e);
}
if(!_17&&this.columnSizeButton){
this.columnSizeButton.domNode.style.display="none";
}
this.syncLayoutDepth(_e,_b);
if(this.layoutColumn){
_b.ui.evtConnect("after",this.layoutColumn,"layoutDepthChanged",this,"syncLayoutDepth",_c.event);
}
},changeLayout:function(){
var _18=new jetspeed.widget.UpdateFragmentContentManager(this.layoutId,this.layoutNameSelect.getValue(),null,this.pageEditorWidget);
_18.getContent();
},openColumnSizeEditor:function(){
this.pageEditorWidget.openColumnSizesEditor(this.layoutId);
},addPortlet:function(){
var _19=jetspeed;
var _1a=_19.page.getPagePathAndQuery();
_1a=_19.url.addQueryParameter(_1a,_19.id.PG_ED_PARAM,"true",true);
_19.page.addPortletInitiate(this.layoutId,_1a.toString());
},addLayout:function(){
var _1b=this.getCurrentLayout();
if(_1b!=null){
var lns=this.layoutNameSelect;
var _1d=null;
if(lns){
_1d=lns.getValue();
}
if(_1d==null){
_1d=_1b.name;
}
var _1e=new jetspeed.widget.AddLayoutContentManager(this.layoutId,_1d,this.pageEditorWidget);
_1e.getContent();
}else{
alert("Cannot add layout (error: null parent layout).");
}
},deleteLayout:function(){
this.pageEditorWidget.deleteLayout(this.layoutId);
},editMoveModeExit:function(){
this.pageEditorWidget.editMoveModeExit();
if(this.editMoveModeButton!=null){
this.editMoveModeButton.domNode.style.display="block";
}
if(this.editMoveModeExitButton!=null){
this.editMoveModeExitButton.domNode.style.display="none";
}
},editMoveModeStart:function(){
this.pageEditorWidget.editMoveModeStart();
if(this.editMoveModeButton!=null){
this.editMoveModeButton.domNode.style.display="none";
}
if(this.editMoveModeExitButton!=null){
this.editMoveModeExitButton.domNode.style.display="block";
}
},_enableMoveMode:function(){
var lmc=this.layoutMoveContainer;
if(!lmc){
return;
}
mmOk=(this.drag!=null);
if(mmOk){
mmOk=(!this._na);
if(!mmOk){
var _20=jetspeed;
var _21=_20.widget.PageEditor.prototype;
var _22=_21.checkPerm(_21.PM_L_NA_TLMV,_20,_21);
var _23=(this.layoutColumn?this.layoutColumn.domNode:null);
if(_22&&_23){
var _24=_20.page.getColFromColNode(_23.parentNode);
if(_24&&_24.layoutActionsDisabled==false){
mmOk=true;
}
}
}
}
lmc.domNode.style.display=(mmOk?"block":"none");
},_disableMoveMode:function(){
var lmc=this.layoutMoveContainer;
if(!lmc){
return;
}
lmc.domNode.style.display="none";
},initializeDrag:function(){
var _26=this.layoutColumn;
if(_26!=null&&_26.domNode!=null){
this.dragStartStaticWidth=_26.domNode.style.width;
this.drag=new dojo.dnd.Moveable(this,{handle:this.layoutMoveContainer.domNode});
}
},startDragging:function(e,_28,_29,_2a){
var _2b=this.layoutColumn;
if(_2b!=null){
var _2c=_2b.domNode;
if(_2c){
if(this.buttonGroupRight){
this.buttonGroupRight.style.display="none";
}
var _2d=true;
var _2e=_2a.widget.PageEditor.prototype;
var _2f=_2e.canL_NA_ED(_2a,_2e);
var _30=_2e.getLDepthPerm(_2a);
var _31=_2b.getLayoutMaxChildDepth();
var _32=_2b.getLayoutDepth();
var _33=_30;
if(_31>_32){
_33=Math.max((_30-(_31-_32)),_32);
}
_28.beforeDragColRowInfo=_2a.page.getPortletCurColRow(_2c);
_28.node=_2c;
var _34={col:_2b,maxdepth:_33};
_28.mover=new _29.dnd.Mover(this,_2c,_34,_2f,_28,e,_2d,_29,_2a);
}
}
},dragChangeToAbsolute:function(_35,_36,_37,_38,_39){
var _3a=_38.getMarginBox(_36,null,_39);
var _3b=400-_37.w;
if(_3b<0){
_37.l=_37.l+(_3b*-1);
_37.w=400;
_38.setMarginBox(_36,_37.l,null,_37.w,null,null,_39);
}
if(_39.UAie){
var _3c=this.pageEditorWidget.bgIframe.iframe;
this.domNode.appendChild(_3c);
_3c.style.display="block";
_38.setMarginBox(_3c,null,null,null,_37.h,null,_39);
}
},endDragging:function(_3d){
var _3e=jetspeed;
var _3f=dojo;
var _40=this.layoutColumn;
if(this.drag==null||_40==null||_40.domNode==null){
return;
}
var _41=_40.domNode;
_41.style.position="static";
_41.style.width=this.dragStartStaticWidth;
_41.style.left="auto";
_41.style.top="auto";
if(this.buttonGroupRight){
this.buttonGroupRight.style.display="block";
}
if(_3e.UAie){
this.pageEditorWidget.bgIframe.iframe.style.display="none";
if(_3e.UAie6){
_3e.page.onBrowserWindowResize();
}
}
var _42=this.drag.beforeDragColRowInfo;
var _43=_3e.page.getPortletCurColRow(_41);
if(_42!=null&&_43!=null){
var ind=_3e.debugindent;
if(_43!=null&&(_43.row!=_42.row||_43.column!=_42.column||_43.layout!=_42.layout)){
var _45=new _3e.widget.MoveLayoutContentManager(this.layoutId,_43.layout,_43.column,_43.row,this.pageEditorWidget);
_45.getContent();
}
}
_3e.ui.updateChildColInfo();
},getLayoutColumn:function(){
return this.layoutColumn;
},getPageColumnIndex:function(){
if(this.layoutColumn){
var _46=jetspeed.page.getColWithNode(this.layoutColumn.domNode);
if(_46!=null){
return _46.getPageColumnIndex();
}
}
return null;
},_getLayoutInfoMoveable:function(){
return this.layoutInfo;
},_getWindowMarginBox:function(_47,_48){
if(this.layoutColumn){
var _49=this.parentLayoutInfo;
if(_48.UAope&&_49==null){
var _4a=_48.page.layoutInfo;
var _4b=_48.page.getColIndexForNode(this.layoutColumn.domNode);
if(_4b!=null){
var _4c=_48.page.columns[_4b];
if(_4c.layoutHeader){
_49=_4a.columnLayoutHeader;
}else{
_49=_4a.column;
}
}else{
_49=_4a.columns;
}
this.parentLayoutInfo=_49;
}
return _48.ui.getMarginBox(this.layoutColumn.domNode,_47,_49,_48);
}
return null;
},editModeRedisplay:function(_4d){
this.show();
this.syncButtons(_4d);
},syncButtons:function(_4e){
var _4f=this._na;
var mmB=this.editMoveModeButton;
var _51=this.editMoveModeExitButton;
var dLB=this.deleteLayoutButton;
this._delEnabled=false;
if(this.isRootLayout){
var _53="none",_54="none";
if(!_4f){
_53=_4e?"none":"block";
_54=_4e?"block":"none";
}
if(mmB){
mmB.domNode.style.display=_53;
}
if(_51){
_51.domNode.style.display=_54;
}
if(dLB){
dLB.domNode.style.display="none";
}
}else{
if(mmB){
mmB.domNode.style.display="none";
}
if(_51){
_51.domNode.style.display="none";
}
if(dLB){
if(_4f){
var _55=jetspeed;
var _56=null;
var _57=this.getLayoutColumn();
if(_57){
_56=_57.domNode;
}else{
if(this.isRootLayout){
_56=dojo.byId(_55.id.COLUMNS);
}
}
if(_56){
if(_55.page.columnsEmptyCheck(_56)){
_4f=false;
}
}
}
this._delEnabled=(!_4f);
dLB.domNode.style.display=(_4f?"none":"block");
}
}
},syncLayoutDepth:function(_58,_59){
if(!_59){
_59=jetspeed;
}
if(!_58){
_58=_59.widget.PageEditor.prototype;
}
var _5a=this._na;
var _5b=((!_5a||this._delEnabled)&&_58.checkPerm(_58.PM_L_N,_59,_58));
if(this.changeLayoutButton){
this.changeLayoutButton.domNode.style.display=(_5b?"block":"none");
}
if(this.layoutColumn){
this.depth=this.layoutColumn.getLayoutDepth();
}
var _5c=_58.getLDepthPerm(_59);
var _5d=(this.depth==null||this.depth>=_5c);
var _5e=(!_5d&&!_5a);
if(this.addLayoutButton){
this.addLayoutButton.domNode.style.display=(_5e?"block":"none");
}
if(this.layoutNameSelect){
if(!_5e&&!_5b){
this.layoutNameSelect.disable();
}else{
if(this.layoutNameSelect.disabled){
this.layoutNameSelect.enable();
}
}
}
},onBrowserWindowResize:function(){
}});
dojo.widget.defineWidget("jetspeed.widget.LayoutEditPaneMoveHandle",dojo.widget.HtmlWidget,{templateString:"<span class=\"layoutMoveContainer\"><img src=\"${this.layoutImagesRoot}layout_move.png\"></span>"});

