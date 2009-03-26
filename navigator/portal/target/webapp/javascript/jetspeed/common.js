if(window.dojo){
dojo.provide("jetspeed.common");
dojo.require("dojo.io.*");
dojo.require("dojo.uri.Uri");
}
if(!window.jetspeed){
jetspeed={};
}
if(!jetspeed.url){
jetspeed.url={};
}
if(!jetspeed.om){
jetspeed.om={};
}
if(!jetspeed.widget){
jetspeed.widget={};
}
jetspeed.version={major:2,minor:1,patch:0,flag:"dev",revision:"",toString:function(){
with(jetspeed.version){
return major+"."+minor+"."+patch+flag+" ("+revision+")";
}
}};
jetspeed.initcommon=function(){
var _1=jetspeed;
if(!window.dojo){
var _1=jetspeed;
_1.no_dojo_load_notifying=false;
_1.no_dojo_post_load=false;
_1.pageLoadedListeners=[];
window.onload=function(){
if(!window.dojo){
var _2=jetspeed;
_2.no_dojo_load_notifying=true;
_2.no_dojo_post_load=true;
var _3=_2.pageLoadedListeners;
for(var x=0;x<_3.length;x++){
_3[x]();
}
_2.pageLoadedListeners=[];
}
};
}else{
var _5=dojo.render.html;
if(_5.ie){
_1.UAie=true;
if(_5.ie60||_5.ie50||_5.ie55){
_1.UAie6=true;
}
_1.stopEvent=function(_6,_7){
try{
_6=_6||window.event;
if(_6){
_6.cancelBubble=true;
_6.returnValue=false;
}
}
catch(ex){
if(!_7&&djConfig.isDebug){
dojo.debug("stopEvent ("+(typeof _6)+") failure: "+jetspeed.formatError(ex));
}
}
};
_1._stopEvent=function(_8){
jetspeed.stopEvent(_8);
};
}else{
if(_5.mozilla){
_1.UAmoz=true;
}else{
if(_5.safari){
_1.UAsaf=true;
}else{
if(_5.opera){
_1.UAope=true;
}
}
}
_1.stopEvent=function(_9){
_9.preventDefault();
_9.stopPropagation();
};
_1._stopEvent=function(_a){
jetspeed.stopEvent(_a);
};
}
}
};
jetspeed.addOnLoad=function(_b,_c){
if(window.dojo){
if(arguments.length==1){
dojo.addOnLoad(_b);
}else{
dojo.addOnLoad(_b,_c);
}
}else{
if(arguments.length==1){
jetspeed.pageLoadedListeners.push(_b);
}else{
if(arguments.length>1){
jetspeed.pageLoadedListeners.push(function(){
_b[_c]();
});
}
}
if(jetspeed.no_dojo_post_load&&!jetspeed.no_dojo_load_notifying){
jetspeed.callPageLoaded();
}
}
};
jetspeed.callPageLoaded=function(){
if(typeof setTimeout=="object"){
setTimeout("jetspeed.pageLoaded();",0);
}else{
jetspeed.pageLoaded();
}
};
jetspeed.getBody=function(){
var _d=jetspeed;
if(_d.docBody==null){
_d.docBody=document.body||document.getElementsByTagName("body")[0];
}
return _d.docBody;
};
jetspeed.formatError=function(ex){
if(ex==null){
return "";
}
var _f=" error:";
if(ex.message!=null){
_f+=" "+ex.message;
}
var _10=ex.number||ex.lineNumber||ex.lineNo;
if(_10==null||_10=="0"||_10.length==0){
_10=null;
}
var _11=ex.fileName;
if(_11!=null){
var _12=_11.lastIndexOf("/");
if(_12!=-1&&_12<(_11.length-1)){
_11=_11.substring(_12+1);
}
}
if(_11==null||_11.length==0){
_11=null;
}
var _13=ex.type;
if(_13==null||_13.length==0||_13=="unknown"){
_13=null;
}
if(_10!=null||_11!=null||_13!=null){
_f+=" ("+(_11!=null?(" "+_11):"");
_f+=(_10!=null?(" line "+_10):"");
_f+=(_13!=null?(" type "+_13):"");
_f+=" )";
}
return _f;
};
jetspeed.url.LOADING_INDICATOR_ID="js-showloading";
jetspeed.url.LOADING_INDICATOR_IMG_ID="js-showloading-img";
jetspeed.url.path={SERVER:null,JETSPEED:null,AJAX_API:null,DESKTOP:null,PORTAL:null,PORTLET:null,ACTION:null,RENDER:null,initialized:false};
jetspeed.url.pathInitialize=function(_14){
var jsU=jetspeed.url;
var _16=jsU.path;
if(!_14&&_16.initialized){
return;
}
var _17=document.getElementsByTagName("base");
var _18=null;
if(_17&&_17.length==1){
_18=_17[0].href;
}else{
_18=window.location.href;
}
var _19=jsU.parse(_18);
var _1a=_19.path;
var _1b="";
if(_19.scheme!=null){
_1b+=_19.scheme+":";
}
if(_19.authority!=null){
_1b+="//"+_19.authority;
}
var _1c=null;
if(djConfig.jetspeed.rootContext){
_1c="";
}else{
var _1d=-1;
for(var _1e=1;_1d<=_1e;_1e++){
_1d=_1a.indexOf("/",_1e);
if(_1d==-1){
break;
}
}
if(_1d==-1){
_1c=_1a;
}else{
_1c=_1a.substring(0,_1d);
}
}
_16.JETSPEED=_1c;
_16.SERVER=_1b;
_16.AJAX_API=_16.JETSPEED+"/ajaxapi";
_16.DESKTOP=_16.JETSPEED+"/desktop";
_16.PORTAL=_16.JETSPEED+"/portal";
_16.PORTLET=_16.JETSPEED+"/portlet";
_16.ACTION=_16.JETSPEED+"/action";
_16.RENDER=_16.JETSPEED+"/render";
_16.initialized=true;
};
jetspeed.url.parse=function(url){
if(url==null){
return null;
}
if(window.dojo&&window.dojo.uri){
return new dojo.uri.Uri(url);
}
return new jetspeed.url.JSUri(url);
};
jetspeed.url.JSUri=function(url){
if(url!=null){
if(!url.path){
var _21="^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$";
var r=url.toString().match(new RegExp(_21));
var _23={};
this.scheme=r[2]||(r[1]?"":null);
this.authority=r[4]||(r[3]?"":null);
this.path=r[5];
this.query=r[7]||(r[6]?"":null);
this.fragment=r[9]||(r[8]?"":null);
}else{
this.scheme=url.scheme;
this.authority=url.authority;
this.path=url.path;
this.query=url.query;
this.fragment=url.fragment;
}
}
};
jetspeed.url.JSUri.prototype={scheme:null,authority:null,path:null,query:null,fragment:null,toString:function(){
var uri="";
uri+=(this.scheme!=null&&this.scheme.length>0)?(this.scheme+"://"):"";
uri+=(this.authority!=null&&this.authority.length>0)?this.authority:"";
uri+=(this.path!=null&&this.path.length>0)?this.path:"";
uri+=(this.query!=null&&this.query.length>0)?("?"+this.query):"";
uri+=(this.fragment!=null&&this.fragment>0)?("#"+this.fragment):"";
return uri;
}};
jetspeed.url.scheme={HTTP_PREFIX:"http://",HTTP_PREFIX_LEN:"http://".length,HTTPS_PREFIX:"https://",HTTPS_PREFIX_LEN:"https://".length};
jetspeed.url.isPortal=function(){
if(window.djConfig&&window.djConfig.jetspeed){
var _25=window.djConfig.jetspeed.servletPath;
if(_25!=null&&_25.toLowerCase().indexOf("/desktop")==0){
return false;
}
}
return true;
};
jetspeed.url.isDesktop=function(){
return !jetspeed.url.isPortal();
};
jetspeed.url.servletPath=function(){
if(jetspeed.url.isPortal()){
return "/portal";
}else{
return "/desktop";
}
};
jetspeed.url.basePortalUrl=function(){
if(!jetspeed.url.path.initialized){
jetspeed.url.pathInitialize();
}
return jetspeed.url.path.SERVER;
};
jetspeed.url.basePortalDesktopUrl=function(){
if(!jetspeed.url.path.initialized){
jetspeed.url.pathInitialize();
}
return jetspeed.url.basePortalUrl()+jetspeed.url.path.JETSPEED;
};
jetspeed.url.addPath=function(url,_27){
if(_27==null||_27.length==0){
return url;
}
var _28=new jetspeed.url.JSUri(url);
var _29=_28.path;
if(_29!=null&&_29.length>0){
if(_28.path.charCodeAt(_29.length-1)==47){
if(_27.charCodeAt(0)==47){
if(_27.length>1){
_28.path+=_27.substring(1);
}
}else{
_28.path+=_27;
}
}else{
if(_27.charCodeAt(0)==47){
_28.path+=_27;
}else{
if(_27.length>1){
_28.path+="/"+_27;
}
}
}
}
var _2a=jetspeed.url.parse(_28);
return _2a.toString();
};
jetspeed.url.urlStartsWithHttp=function(url){
if(url){
var len=url.length;
var _2d=jetspeed.url.scheme.HTTPS_PREFIX_LEN;
if(len>_2d){
var _2e=jetspeed.url.scheme.HTTP_PREFIX_LEN;
if(url.substring(0,_2e)==jetspeed.url.scheme.HTTP_PREFIX){
return true;
}
if(url.substring(0,_2d)==jetspeed.url.scheme.HTTPS_PREFIX){
return true;
}
}
}
return false;
};
jetspeed.url.addQueryParameter=function(_2f,_30,_31,_32){
if(_2f==null){
return _2f;
}
if(!_2f.path){
_2f=jetspeed.url.parse(_2f);
}
if(_2f==null){
return null;
}
if(_30==null){
return _2f;
}
_2f.jsQParamN=null;
if(_32){
_2f=jetspeed.url.removeQueryParameter(_2f,_30,false);
}
var _33=_2f.query;
if(_33==null){
_33="";
}
var _34=_33.length;
if(_34>0){
_33+="&";
}
_33+=_30+"="+(_31!=null?_31:"");
_2f.query=_33;
var _35=new jetspeed.url.JSUri(_2f);
_2f=jetspeed.url.parse(_35);
return _2f;
};
jetspeed.url.removeAllQueryParameters=function(_36){
return jetspeed.url.removeQueryParameter(_36,null,true);
};
jetspeed.url.removeQueryParameter=function(_37,_38,_39){
if(_37==null){
return _37;
}
if(!_37.path){
_37=jetspeed.url.parse(_37);
}
if(_37==null){
return null;
}
_37.jsQParamN=null;
var _3a=_37.query;
var _3b=((_3a!=null)?_3a.length:0);
if(_3b>0){
if(_39){
_3a=null;
}else{
if(_38==null){
return _37;
}else{
var _3c=_38;
var _3d=_3a.indexOf(_3c);
if(_3d==0){
_3a=jetspeed.url._removeQP(_3a,_3b,_3c,_3d);
}
_3c="&"+_38;
while(true){
_3b=((_3a!=null)?_3a.length:0);
_3d=_3a.indexOf(_3c,0);
if(_3d==-1){
break;
}
var _3e=jetspeed.url._removeQP(_3a,_3b,_3c,_3d);
if(_3e==_3a){
break;
}
_3a=_3e;
}
if(_3a.length>0){
if(_3a.charCodeAt(0)==38){
_3a=((_3a.length>1)?_3a.substring(1):"");
}
if(_3a.length>0&&_3a.charCodeAt(0)==63){
_3a=((_3a.length>1)?_3a.substring(1):"");
}
}
}
}
_37.query=_3a;
var _3f=new jetspeed.url.JSUri(_37);
_37=jetspeed.url.parse(_3f);
}
return _37;
};
jetspeed.url._removeQP=function(_40,_41,_42,_43){
if(_43==-1){
return _40;
}
if(_41>(_43+_42.length)){
var _44=_40.charCodeAt(_43+_42.length);
if(_44==61){
var _45=_40.indexOf("&",_43+_42.length+1);
if(_45!=-1){
if(_43>0){
_40=_40.substring(0,_43)+_40.substring(_45);
}else{
_40=((_45<(_41-1))?_40.substring(_45):"");
}
}else{
if(_43>0){
_40=_40.substring(0,_43);
}else{
_40="";
}
}
}else{
if(_44==38){
if(_43>0){
_40=_40.substring(0,_43)+_40.substring(_43+_42.length);
}else{
_40=_40.substring(_43+_42.length);
}
}
}
}else{
if(_41==(_43+_42.length)){
_40="";
}
}
return _40;
};
jetspeed.url.getQueryParameter=function(_46,_47){
if(_46==null){
return null;
}
if(!_46.authority||!_46.scheme){
_46=jetspeed.url.parse(_46);
}
if(_46==null){
return null;
}
if(_46.jsQParamN==null&&_46.query){
var _48=new Array();
var _49=_46.query.split("&");
for(var i=0;i<_49.length;i++){
if(_49[i]==null){
_49[i]="";
}
var _4b=_49[i].indexOf("=");
if(_4b>0&&_4b<(_49[i].length-1)){
_48[i]=unescape(_49[i].substring(_4b+1));
_49[i]=unescape(_49[i].substring(0,_4b));
}else{
_48[i]="";
}
}
_46.jsQParamN=_49;
_46.jsQParamV=_48;
}
if(_46.jsQParamN!=null){
for(var i=0;i<_46.jsQParamN.length;i++){
if(_46.jsQParamN[i]==_47){
return _46.jsQParamV[i];
}
}
}
return null;
};
jetspeed.om.Id=function(){
var _4c="";
for(var i=0;i<arguments.length;i++){
if(dojo.lang.isString(arguments[i])){
if(_4c.length>0){
_4c+="-";
}
_4c+=arguments[i];
}else{
if(dojo.lang.isObject(arguments[i])){
for(var _4e in arguments[i]){
this[_4e]=arguments[i][_4e];
}
}
}
}
this.id=_4c;
};
jetspeed.om.Id.prototype={getId:function(){
return this.id;
}};
if(window.dojo){
jetspeed.url.BindArgs=function(_4f){
dojo.lang.mixin(this,_4f);
if(!this.mimetype){
this.mimetype="text/html";
}
if(!this.encoding){
this.encoding="utf-8";
}
};
dojo.lang.extend(jetspeed.url.BindArgs,{createIORequest:function(){
var _50=new dojo.io.Request(this.url,this.mimetype);
_50.fromKwArgs(this);
return _50;
},load:function(_51,_52,_53){
try{
var _54=null;
if(this.debugContentDumpIds){
_54=((this.domainModelObject&&dojo.lang.isFunction(this.domainModelObject.getId))?this.domainModelObject.getId():((this.domainModelObject&&this.domainModelObject.id)?String(this.domainModelObject.id):""));
var _55=false;
for(var _56=0;_56<this.debugContentDumpIds.length;_56++){
if(_54.match(new RegExp(this.debugContentDumpIds[_56]))){
_55=true;
break;
}
}
if(_55){
if(dojo.lang.isString(_52)){
dojo.debug("retrieveContent ["+(_54?_54:this.url)+"] content: "+_52);
}else{
var _57=dojo.dom.innerXML(_52);
if(!_57){
_57=(_52!=null?"!= null (IE no XMLSerializer)":"null");
}
dojo.debug("retrieveContent ["+(_54?_54:this.url)+"] xml-content: "+_57);
}
}
}
if(this.contentListener&&dojo.lang.isFunction(this.contentListener.notifySuccess)){
this.contentListener.notifySuccess(_52,this.url,this.domainModelObject,_53);
}else{
_54=((this.domainModelObject&&dojo.lang.isFunction(this.domainModelObject.getId))?this.domainModelObject.getId():"");
dojo.debug("retrieveContent ["+(_54?_54:this.url)+"] no valid contentListener");
}
if(this.hideLoadingIndicator){
jetspeed.url.loadingIndicatorHide();
}
}
catch(e){
if(this.hideLoadingIndicator){
jetspeed.url.loadingIndicatorHide();
}
dojo.raise("dojo.io.bind "+jetspeed.formatError(e));
}
},error:function(_58,_59){
try{
if(this.contentListener&&dojo.lang.isFunction(this.contentListener.notifyFailure)){
this.contentListener.notifyFailure(_58,_59,this.url,this.domainModelObject);
}
if(this.hideLoadingIndicator){
jetspeed.url.loadingIndicatorHide();
}
}
catch(e){
if(this.hideLoadingIndicator){
jetspeed.url.loadingIndicatorHide();
}
throw e;
}
}});
jetspeed.url.retrieveContent=function(_5a,_5b,_5c,_5d){
if(!_5a){
_5a={};
}
_5a.contentListener=_5b;
_5a.domainModelObject=_5c;
_5a.debugContentDumpIds=_5d;
var _5e=new jetspeed.url.BindArgs(_5a);
if(_5a.showLoadingIndicator||(_5b&&!_5b.suppressLoadingIndicator&&_5a.showLoadingIndicator!=false)){
if(jetspeed.url.loadingIndicatorShow()){
_5e.hideLoadingIndicator=true;
}
}
dojo.io.bind(_5e.createIORequest());
};
jetspeed.url.checkAjaxApiResponse=function(_5f,_60,_61,_62,_63,_64){
var _65=false;
var _66=_60.getElementsByTagName("status");
if(_66!=null){
var _67=_66[0].firstChild.nodeValue;
if(_67=="success"){
_65=_67;
}else{
if(_61&&_61.length>0){
for(var i=0;i<_61.length;i++){
if(_67==_61[i]){
_65=_67;
break;
}
}
}
}
}
if((!_65&&_62)||_64){
var _69=dojo.dom.innerXML(_60);
if(!_69){
_69=(_60!=null?"!= null (IE no XMLSerializer)":"null");
}
if(_63==null){
_63="ajax-api";
}
if(_65){
dojo.debug(_63+" success  url="+_5f+"  xml-content="+_69);
}else{
dojo.raise(_63+" failure  url="+_5f+"  xml-content="+_69);
}
}
return _65;
};
jetspeed.url._loadingImgUpdate=function(_6a,_6b,_6c,doc,_6e,_6f){
var _70=_6e.loadingImgProps;
if(_70){
var _71=doc.getElementById(_6f.LOADING_INDICATOR_ID);
if(_71==null||!_71.style||_71.style.display=="none"){
return;
}
var _72=_70.imganimated;
var _73=doc.getElementById(_6f.LOADING_INDICATOR_IMG_ID);
if(_72&&_73){
var _74=_70._imgBaseUrl;
if(_74==null){
var _75=_70.imgdir;
if(_75==null||_75.length==0){
_74=false;
}else{
_74=_6e.getLayoutRootUrl()+_75;
}
_70._imgBaseUrl=_74;
}
if(_74){
var _76=false;
if((_6a||_6c)&&!_70._stepDisabled){
var _77=_70.imgstepprefix;
var _78=_70.imgstepextension;
var _79=_70.imgsteps;
if(_77&&_78&&_79){
var _7a=_70._stepNext;
if(_6b||_7a==null||_7a>=_79.length){
_7a=0;
}
var _7b=_74+"/"+_77;
if(!_6c){
_73.src=_7b+_79[_7a]+_78;
_76=true;
_70._stepNext=_7a+1;
}else{
var _7c,_7d=Math.ceil(_79.length/1.8);
for(var i=0;i<=_7d;i++){
_7c=new Image();
_7c.src=_7b+_79[i]+_78;
}
}
}else{
_70._stepDisabled=true;
}
}
if(!_76&&!_6c){
_73.src=_74+"/"+_72;
}
}
}
}
};
jetspeed.url.loadingIndicatorStep=function(_7f){
var _80=_7f.url;
_80._loadingImgUpdate(true,false,false,document,_7f.prefs,_80);
};
jetspeed.url.loadingIndicatorStepPreload=function(){
var _81=jetspeed;
var _82=_81.url;
_82._loadingImgUpdate(true,false,true,document,_81.prefs,_82);
};
jetspeed.url.loadingIndicatorShow=function(_83,_84){
var _85=jetspeed;
var _86=_85.prefs;
var _87=_85.url;
var doc=document;
if(typeof _83=="undefined"){
_83="loadpage";
}
var _89=doc.getElementById(_87.LOADING_INDICATOR_ID);
if(_89!=null&&_89.style){
var _8a=null;
if(_86!=null&&_86.desktopActionLabels!=null){
_8a=_86.desktopActionLabels[_83];
}
if(_8a!=null&&_8a.length>0&&_89.style["display"]=="none"){
_87._loadingImgUpdate(_84,true,false,doc,_86,_87);
_89.style["display"]="";
if(_83!=null){
if(_8a!=null&&_8a.length>0){
var _8b=doc.getElementById(_87.LOADING_INDICATOR_ID+"-content");
if(_8b!=null){
_8b.innerHTML=_8a;
}
}
}
return true;
}
}
return false;
};
jetspeed.url.loadingIndicatorHide=function(){
var _8c=document.getElementById(jetspeed.url.LOADING_INDICATOR_ID);
if(_8c!=null&&_8c.style){
_8c.style["display"]="none";
}
};
}
jetspeed.widget.openDialog=function(_8d){
var _8e=jetspeed.UAmoz;
if(_8e){
_8d.domNode.style.position="fixed";
if(!_8d._fixedIPtBug){
var _8f=_8d;
_8f.placeModalDialog=function(){
var _90=dojo.html.getScroll().offset;
var _91=dojo.html.getViewport();
var mb;
if(_8f.isShowing()){
mb=dojo.html.getMarginBox(_8f.domNode);
}else{
dojo.html.setVisibility(_8f.domNode,false);
dojo.html.show(_8f.domNode);
mb=dojo.html.getMarginBox(_8f.domNode);
dojo.html.hide(_8f.domNode);
dojo.html.setVisibility(_8f.domNode,true);
}
var x=(_91.width-mb.width)/2;
var y=(_91.height-mb.height)/2;
with(_8f.domNode.style){
left=x+"px";
top=y+"px";
}
};
_8f._fixedIPtBug=true;
}
}
_8d.show();
};
jetspeed.initcommon();

