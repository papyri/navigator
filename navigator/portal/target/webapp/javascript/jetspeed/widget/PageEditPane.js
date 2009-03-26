dojo.provide("jetspeed.widget.PageEditPane");
dojo.require("dojo.widget.*");
dojo.require("dojo.io.*");
dojo.require("dojo.event.*");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.widget.Dialog");
dojo.require("dojo.widget.Select");
dojo.require("dojo.widget.Button");
dojo.require("dojo.html.common");
dojo.require("dojo.html.display");
jetspeed.widget.PageEditPane=function(){
};
dojo.widget.defineWidget("jetspeed.widget.PageEditPane",dojo.widget.HtmlWidget,{pageEditContainer:null,pageEditLDContainer:null,pageEditPDContainer:null,deletePageDialog:null,deletePageDialogBg:null,deletePageDialogFg:null,createPageDialog:null,createPageDialogBg:null,createPageDialogFg:null,layoutDecoratorSelect:null,portletDecoratorSelect:null,isContainer:true,widgetsInTemplate:true,layoutDecoratorDefinitions:null,portletDecoratorDefinitions:null,postMixInProperties:function(_1,_2,_3){
jetspeed.widget.PageEditPane.superclass.postMixInProperties.apply(this,arguments);
this.templateCssPath=new dojo.uri.Uri(jetspeed.url.basePortalDesktopUrl()+"/javascript/jetspeed/widget/PageEditPane.css");
this.templatePath=new dojo.uri.Uri(jetspeed.url.basePortalDesktopUrl()+"/javascript/jetspeed/widget/PageEditPane.html");
},fillInTemplate:function(_4,_5){
var _6=this;
this.deletePageDialog=dojo.widget.createWidget("dialog",{widgetsInTemplate:true,deletePageConfirmed:function(){
this.hide();
_6.deletePageConfirmed();
}},this.deletePageDialog);
this.deletePageDialog.setCloseControl(this.deletePageDialog.deletePageCancel.domNode);
var _7={};
_7.widgetsInTemplate=true;
_7.createPageConfirmed=function(){
var _8=this.createPageNameTextbox.textbox.value;
var _9=this.createPageTitleTextbox.textbox.value;
var _a=this.createPageShortTitleTextbox.textbox.value;
this.hide();
_6.createPageConfirmed(_8,_9,_a);
};
this.createPageDialog=dojo.widget.createWidget("dialog",_7,this.createPageDialog);
this.createPageDialog.setCloseControl(this.createPageDialog.createPageCancel.domNode);
jetspeed.widget.PageEditPane.superclass.fillInTemplate.call(this);
},destroy:function(){
if(this.deletePageDialog!=null){
this.deletePageDialog.destroy();
}
if(this.createPageDialog!=null){
this.createPageDialog.destroy();
}
jetspeed.widget.PageEditPane.superclass.destroy.apply(this,arguments);
},postCreate:function(_b,_c,_d){
var _e=jetspeed;
var _f=dojo.html;
_e.widget.PageEditPane.superclass.postCreate.apply(this,arguments);
var _10=_e.widget.PageEditor.prototype;
if(this.pageEditContainer!=null){
_f.addClass(this.pageEditContainer,_10.styleBaseAdd);
}
if(this.pageEditLDContainer!=null){
_f.addClass(this.pageEditLDContainer,_10.styleDetailAdd);
}
if(this.pageEditPDContainer!=null){
_f.addClass(this.pageEditPDContainer,_10.styleDetailAdd);
}
if(this.layoutDecoratorSelect!=null){
if(!_10.checkPerm(_10.PM_PG_L_D,_e)){
if(this.pageEditLDContainer){
this.pageEditLDContainer.style.display="none";
}else{
this.layoutDecoratorSelect.domNode.style.display="none";
}
}else{
var _11=_e.page.layoutDecorator;
var _12=[];
if(this.layoutDecoratorDefinitions){
for(var i=0;i<this.layoutDecoratorDefinitions.length;i++){
var _14=this.layoutDecoratorDefinitions[i];
if(_14&&_14.length==2){
_12.push([_14[0],_14[1]]);
if(_11==_14[1]){
this.layoutDecoratorSelect.setAllValues(_14[0],_14[1]);
}
}
}
}
this.layoutDecoratorSelect.dataProvider.setData(_12);
}
}
if(this.portletDecoratorSelect!=null){
if(!_10.checkPerm(_10.PM_PG_P_D,_e)){
if(this.pageEditPDContainer){
this.pageEditPDContainer.style.display="none";
}else{
this.portletDecoratorSelect.domNode.style.display="none";
}
}else{
var _15=_e.page.portletDecorator;
var _16=[];
if(this.portletDecoratorDefinitions){
for(var i=0;i<this.portletDecoratorDefinitions.length;i++){
var _17=this.portletDecoratorDefinitions[i];
if(_17&&_17.length==2){
_16.push([_17[0],_17[1]]);
if(_15==_17[1]){
this.portletDecoratorSelect.setAllValues(_17[0],_17[1]);
}
}
}
}
this.portletDecoratorSelect.dataProvider.setData(_16);
}
}
var _18=_10.checkPerm(_10.PM_PG_AD,_e);
if(!_18){
this.createPageButton.domNode.style.display="none";
}
},deletePage:function(){
this.pageEditorWidget._openDialog(this.deletePageDialog);
},deletePageConfirmed:function(){
var _19=new jetspeed.widget.RemovePageContentManager(this.pageEditorWidget);
_19.getContent();
},createPage:function(){
this.pageEditorWidget._openDialog(this.createPageDialog);
},createPageConfirmed:function(_1a,_1b,_1c){
if(_1a!=null&&_1a.length>0){
var _1d=jetspeed.page.getPageDirectory(true)+_1a;
var _1e=jetspeed.page.getPageDirectory()+_1a;
var _1f=new jetspeed.widget.AddPageContentManager(_1d,_1e,_1a,null,_1b,_1c,this.pageEditorWidget);
_1f.getContent();
}
},changeLayoutDecorator:function(){
var _20=new jetspeed.widget.UpdatePageInfoContentManager(this.layoutDecoratorSelect.getValue(),null,this.pageEditorWidget);
_20.getContent();
},changePortletDecorator:function(){
var _21=new jetspeed.widget.UpdatePageInfoContentManager(null,this.portletDecoratorSelect.getValue(),this.pageEditorWidget);
_21.getContent();
},editModeRedisplay:function(){
this.show();
},onBrowserWindowResize:function(){
var _22=this.deletePageDialog;
var _23=this.createPageDialog;
if(_22&&_22.isShowing()){
_22.domNode.style.display="none";
_22.domNode.style.display="block";
}
if(_23&&_23.isShowing()){
_23.domNode.style.display="none";
_23.domNode.style.display="block";
}
}});

