dojo.provide("jetspeed.widget.EditorTable");
dojo.require("dojo.widget.SortableTable");
jetspeed.widget.EditorTable=function(){
dojo.widget.SortableTable.call(this);
this.widgetType="EditorTable";
this.headerSortUpClass="selectedUp";
this.headerSortDownClass="selectedDown";
};
dojo.inherits(jetspeed.widget.EditorTable,dojo.widget.SortableTable);
dojo.lang.extend(jetspeed.widget.EditorTable,{saveWarningDialogWidgetId:null,updateEditor:function(_1){
},saveEntrySubmit:function(_2,_3){
},clearAndDisableEditor:function(){
},getNewEntryPrototype:function(){
return {};
},render:function(_4){
_4=true;
jetspeed.widget.EditorTable.superclass.render.call(this,_4);
},buildRendering:function(_5,_6){
jetspeed.widget.EditorTable.superclass.buildRendering.call(this,_5,_6);
if(_5.templateCssPath){
dojo.html.insertCssFile(dojo.uri.dojoUri(_5.templateCssPath),document,true);
}
},hasRowChanged:function(_7){
if(!_7){
return false;
}
var _8=_7.Id;
var _9=this.getData(this.js_masterdata,_8);
var _a=(_7.__isNew||_7.__isModified)?true:false;
if(!_a){
for(var _b in _9){
if(_7[_b]!=_9[_b]){
_a=true;
break;
}
}
}
return _a;
},entryIsNew:function(_c){
if(!_c){
return false;
}
return _c.__isNew;
},setModified:function(_d){
if(!_d){
return;
}
_d.__isModified=true;
},getSelectedRow:function(){
if(this.selected&&this.selected.length==1){
var _e=this.selected[0].Id;
var _f=this.getData(this.data,_e);
return _f;
}
return null;
},getData:function(_10,_11){
if(!_10){
return null;
}
for(var i=0;i<_10.length;i++){
if(_10[i].Id==_11){
return _10[i];
}
}
return null;
},getDataIndex:function(_13,_14){
if(!_13){
return -1;
}
for(var i=0;i<_13.length;i++){
if(_13[i].Id==_14){
return i;
}
}
return -1;
},processTableRowEvent:function(e){
var row=dojo.html.getParentByType(e.target,"tr");
var _18=this.getObjectFromRow(row);
this.updateEditor(_18);
},checkForChanges:function(){
var _19=this.getSelectedRow();
if(!_19){
return false;
}
var _1a=this.hasRowChanged(_19);
return (!_1a?false:(_19.__isNew?"new":"modified"));
},updateClonedData:function(_1b,_1c){
if(!_1b||!_1c){
return;
}
for(var _1d in _1b){
_1c[_1d]=_1b[_1d];
}
},printSelection:function(){
if(this.selected&&this.selected.length==1){
dojo.debug(this.widgetId+" selection: "+jsDebugShallow(this.selected[0]));
}else{
dojo.debug(this.widgetId+" selection: null");
}
},newEntry:function(){
if(this.saveWarningDialogWidgetId){
if(this.checkForChanges()){
dojo.widget.byId(this.saveWarningDialogWidgetId).show();
return;
}
}
var _1e=dojo.lang.shallowCopy(this.getNewEntryPrototype());
var tId=1;
for(var i=0;i<this.js_masterdata.length;i++){
if(this.js_masterdata[i].Id>=tId){
tId=this.js_masterdata[i].Id+1;
}
}
_1e.Id=tId;
this.js_masterdata.push(dojo.lang.shallowCopy(_1e));
_1e.__isNew=true;
this.data.push(_1e);
this.selected=[dojo.lang.shallowCopy(_1e)];
this.render(true);
this.showSelections();
this.updateEditor(_1e);
},deleteEntry:function(){
var _21=this.getSelectedRow();
if(!_21){
return;
}
var tId=_21.Id;
if(!_21.__isNew){
this.saveEntrySubmit(_21,true);
}
var _23=this.getDataIndex(this.js_masterdata,tId);
if(_23!=-1){
this.js_masterdata.splice(_23,1);
}
_23=this.getDataIndex(this.data,tId);
if(_23!=-1){
this.data.splice(_23,1);
}
this.selected=[];
this.render(true);
this.showSelections();
this.clearAndDisableEditor();
},saveEntry:function(){
var _24=this.getSelectedRow();
if(!_24){
dojo.raise("saveEntry can't find selectedRowData");
return;
}
var _25=this.getData(this.js_masterdata,_24.Id);
if(!_25){
dojo.raise("saveEntry can't find masterdata");
return;
}
this.saveEntrySubmit(_24);
delete _24.__isNew;
delete _24.__isModified;
this.updateClonedData(_24,_25);
this.updateClonedData(_24,this.selected[0]);
this.updateEditor(_24);
},revertEntry:function(){
var _26=this.getSelectedRow();
if(!_26){
return;
}
if(_26.__isNew){
deleteEntry();
}else{
delete _26.__isModified;
var _27=this.getData(this.js_masterdata,_26.Id);
if(!_27){
return;
}
this.updateClonedData(_27,_26);
this.updateClonedData(_27,this.selected[0]);
this.render(true);
this.showSelections();
this.updateEditor(_27);
}
},okToChangeSelectionOrExit:function(_28){
if(this.checkForChanges()){
if(this.saveWarningDialogWidgetId){
dojo.widget.byId(this.saveWarningDialogWidgetId).show();
return false;
}else{
this.saveEntry();
}
}
if(_28!=null){
_28.proceed();
}
return true;
},dojoDebugTableData:function(){
dojo.debug(debugTableData());
},debugTableData:function(){
var _29=this;
buff=_29.widgetId+" data:"+"\r\n";
for(var _2a=0;_2a<_29.js_masterdata.length;_2a++){
buff+="["+_2a+"]"+"\r\n";
var _2b=new Object();
for(var _2c in _29.js_masterdata[_2a]){
buff+="   "+_2c+"="+_29.js_masterdata[_2a][_2c];
if(_2c=="__isModified"||_2c=="__isNew"){
buff+="\r\n";
}else{
var _2d=null;
if(_29.data.length<=_2a){
buff+=" <out-of-bounds>"+"\r\n";
}else{
_2d=_29.data[_2a][_2c];
buff+=" ("+(_2d==null?"null":_2d)+")"+"\r\n";
}
}
}
}
return buff;
}});

