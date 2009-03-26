if(window.dojo){
dojo.provide("jetspeed.selector");
dojo.require("jetspeed.common");
}
if(!window.jetspeed){
jetspeed={};
}
if(!jetspeed.selector){
jetspeed.selector={};
}
jetspeed.selector.PortletDef=function(_1,_2,_3,_4,_5){
this.portletName=_1;
this.portletDisplayName=_2;
this.portletDescription=_3;
this.image=_4;
this.count=_5;
};
jetspeed.selector.PortletDef.prototype={portletName:null,portletDisplayName:null,portletDescription:null,portletImage:null,portletCount:null,getId:function(){
return this.portletName;
},getPortletName:function(){
return this.portletName;
},getPortletDisplayName:function(){
return this.portletDisplayName;
},getPortletCount:function(){
return this.portletCount;
},getPortletDescription:function(){
return this.portletDescription;
}};
jetspeed.selector.addNewPortletDefinition=function(_6,_7,_8){
var _9=new jetspeed.selector.PortletAddAjaxApiCallbackCL(_6);
var _a="?action=add&id="+escape(_6.getPortletName());
if(_8!=null&&_8.length>0){
_a+="&layoutid="+escape(_8);
}
var _b=_7+_a;
var _c="text/xml";
var _d=new jetspeed.om.Id("addportlet",{});
jetspeed.url.retrieveContent({url:_b,mimetype:_c},_9,_d,jetspeed.debugContentDumpIds);
};
jetspeed.selector.PortletAddAjaxApiCallbackCL=function(_e){
this.portletDef=_e;
};
jetspeed.selector.PortletAddAjaxApiCallbackCL.prototype={notifySuccess:function(_f,_10,_11){
var _12=jetspeed;
var _13=_12.url.checkAjaxApiResponse(_10,_f,["refresh"],true,"add-portlet");
if(_13=="refresh"&&_12.page!=null){
var _14=_12.page.getPageUrl();
if(_14!=null){
if(!_12.prefs.ajaxPageNavigation){
_12.pageNavigate(_14,null,true);
}else{
_12.updatePage(_14,false,true);
}
}
}
},notifyFailure:function(_15,_16,_17,_18){
dojo.raise("PortletAddAjaxApiCallbackCL error ["+_18.toString()+"] url: "+_17+" type: "+_15+jetspeed.formatError(_16));
}};

