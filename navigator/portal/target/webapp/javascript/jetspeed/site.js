if(window.dojo){
dojo.provide("jetspeed.site");
dojo.require("jetspeed.common");
}
if(!window.jetspeed){
jetspeed={};
}
if(!jetspeed.site){
jetspeed.site={};
}
jetspeed.site.getFolders=function(_1,_2){
var _3=new jetspeed.site.FoldersListContentListener(_2);
var _4="?action=getfolders&data="+_1;
var _5=jetspeed.url.basePortalUrl()+jetspeed.url.path.AJAX_API+_4;
var _6="text/xml";
var _7=new jetspeed.om.Id("getfolders",{});
jetspeed.url.retrieveContent({url:_5,mimetype:_6},_3,_7,jetspeed.debugContentDumpIds);
};
jetspeed.site.FoldersListContentListener=function(_8){
this.notifyFinished=_8;
};
dojo.lang.extend(jetspeed.site.FoldersListContentListener,{notifySuccess:function(_9,_a,_b){
var _c=this.parseFolders(_9);
var _d=this.parsePages(_9);
var _e=this.parseLinks(_9);
if(dojo.lang.isFunction(this.notifyFinished)){
this.notifyFinished(_b,_c,_d,_e);
}
},notifyFailure:function(_f,_10,_11,_12){
dojo.raise("FoldersListContentListener error ["+_12.toString()+"] url: "+_11+" type: "+_f+jetspeed.formatError(_10));
},parseFolders:function(_13){
var _14=[];
var _15=_13.getElementsByTagName("js");
if(!_15||_15.length>1){
dojo.raise("unexpected zero or multiple <js> elements in portlet selector xml");
}
var _16=_15[0].childNodes;
for(var i=0;i<_16.length;i++){
var _18=_16[i];
if(_18.nodeType!=dojo.dom.ELEMENT_NODE){
continue;
}
var _19=_18.nodeName;
if(_19=="folders"){
var _1a=_18;
var _1b=_1a.childNodes;
for(var pI=0;pI<_1b.length;pI++){
var _1d=_1b[pI];
if(_1d.nodeType!=dojo.dom.ELEMENT_NODE){
continue;
}
var _1e=_1d.nodeName;
if(_1e=="folder"){
var _1f=this.parsePortletElement(_1d);
_14.push(_1f);
}
}
}
}
return _14;
},parsePages:function(_20){
var _21=[];
var _22=_20.getElementsByTagName("js");
if(!_22||_22.length>1){
dojo.raise("unexpected zero or multiple <js> elements in portlet selector xml");
}
var _23=_22[0].childNodes;
for(var i=0;i<_23.length;i++){
var _25=_23[i];
if(_25.nodeType!=dojo.dom.ELEMENT_NODE){
continue;
}
var _26=_25.nodeName;
if(_26=="folders"){
var _27=_25;
var _28=_27.childNodes;
for(var pI=0;pI<_28.length;pI++){
var _2a=_28[pI];
if(_2a.nodeType!=dojo.dom.ELEMENT_NODE){
continue;
}
var _2b=_2a.nodeName;
if(_2b=="page"){
var _2c=this.parsePortletElement(_2a);
_21.push(_2c);
}
}
}
}
return _21;
},parseLinks:function(_2d){
var _2e=[];
var _2f=_2d.getElementsByTagName("js");
if(!_2f||_2f.length>1){
dojo.raise("unexpected zero or multiple <js> elements in portlet selector xml");
}
var _30=_2f[0].childNodes;
for(var i=0;i<_30.length;i++){
var _32=_30[i];
if(_32.nodeType!=dojo.dom.ELEMENT_NODE){
continue;
}
var _33=_32.nodeName;
if(_33=="folders"){
var _34=_32;
var _35=_34.childNodes;
for(var pI=0;pI<_35.length;pI++){
var _37=_35[pI];
if(_37.nodeType!=dojo.dom.ELEMENT_NODE){
continue;
}
var _38=_37.nodeName;
if(_38=="link"){
var _39=this.parsePortletElement(_37);
_2e.push(_39);
}
}
}
}
return _2e;
},parsePortletElement:function(_3a){
var _3b=_3a.getAttribute("name");
var _3c=_3a.getAttribute("path");
return new jetspeed.site.FolderDef(_3b,_3c);
}});
jetspeed.site.FolderDef=function(_3d,_3e){
this.folderName=_3d;
this.folderPath=_3e;
};
dojo.inherits(jetspeed.site.FolderDef,jetspeed.om.Id);
dojo.lang.extend(jetspeed.site.FolderDef,{folderName:null,folderPath:null,getName:function(){
return this.folderName;
},getPath:function(){
return this.folderPath;
}});

