
dojo.provide("dojo.collections.Store");
dojo.require("dojo.lang.common");
dojo.collections.Store = function(jsonArray){var data = [];
this.keyField = "Id";
this.get = function(){return data;};
this.getByKey = function(key){for(var i=0; i<data.length; i++){if(data[i].key==key){return data[i];}}
return null;};
this.getByIndex = function(idx){return data[idx];};
this.getData = function(){var arr = [];
for(var i=0; i<data.length; i++){arr.push(data[i].src);}
return arr;};
this.getDataByKey = function(key){for(var i=0; i<data.length; i++){if(data[i].key==key){return data[i].src;}}
return null;};
this.getDataByIndex = function(idx){return data[idx].src;};
this.update = function(obj, fieldPath, val){var parts=fieldPath.split("."), i=0, o=obj, field;
if(parts.length>1) {field = parts.pop();
do{if(parts[i].indexOf("()")>-1){var temp=parts[i++].split("()")[0];
if(!o[temp]){dojo.raise("dojo.collections.Store.getField(obj, '" + field + "'): '" + temp + "' is not a property of the passed object.");} else {o = o[temp]();}} else {o = o[parts[i++]];}} while (i<parts.length && o != null);} else {field = parts[0];}
obj[field] = val;
this.onUpdateField(obj, fieldPath, val);};
this.forEach = function(fn){if(Array.forEach){Array.forEach(data, fn, this);}else{for(var i=0; i<data.length; i++){fn.call(this, data[i]);}}};
this.forEachData = function(fn){if(Array.forEach){Array.forEach(this.getData(), fn, this);}else{var a=this.getData();
for(var i=0; i<a.length; i++){fn.call(this, a[i]);}}};
this.setData = function(arr){data = [];
for(var i=0; i<arr.length; i++){data.push({key:arr[i][this.keyField], 
src:arr[i]});}
this.onSetData();};
this.clearData = function(){data = [];
this.onClearData();};
this.addData = function(obj,key){var k = key || obj[this.keyField];
if(this.getByKey(k)){var o = this.getByKey(k);
o.src = obj;} else {var o={ key:k, src:obj };
data.push(o);}
this.onAddData(o);};
this.addDataRange = function(arr){var objects=[];
for(var i=0; i<arr.length; i++){var k = arr[i][this.keyField];
if(this.getByKey(k)){var o = this.getByKey(k);
o.src = obj;} else {var o = { key:k, src:arr[i] };
data.push(o);}
objects.push(o);}
this.onAddDataRange(objects);};
this.removeData = function(obj){var idx=-1;
var o=null;
for(var i=0; i<data.length; i++){if(data[i].src==obj){idx=i;
o=data[i];
break;}}
this.onRemoveData(o);
if(idx>-1){data.splice(idx,1);}};
this.removeDataByKey = function(key){this.removeData(this.getDataByKey(key));};
this.removeDataByIndex = function(idx){this.removeData(this.getDataByIndex(idx));};
if(jsonArray && jsonArray.length && jsonArray[0]){this.setData(jsonArray);}};
dojo.extend(dojo.collections.Store, {getField:function(obj, field){var parts=field.split("."), i=0, o=obj;
do{if(parts[i].indexOf("()")>-1){var temp=parts[i++].split("()")[0];
if(!o[temp]){dojo.raise("dojo.collections.Store.getField(obj, '" + field + "'): '" + temp + "' is not a property of the passed object.");} else {o = o[temp]();}} else {o = o[parts[i++]];}} while (i<parts.length && o != null);
if(i < parts.length){dojo.raise("dojo.collections.Store.getField(obj, '" + field + "'): '" + field + "' is not a property of the passed object.");}
return o;},
getFromHtml:function(meta, body, fnMod){var rows = body.rows;
var ctor=function(row){var obj = {};
for(var i=0; i<meta.length; i++){var o = obj;
var data = row.cells[i].innerHTML;
var p = meta[i].getField();
if(p.indexOf(".") > -1){p = p.split(".");
while(p.length>1){var pr = p.shift();
o[pr] = {};
o = o[pr];}
p = p[0];}
var type = meta[i].getType();
if(type == String){o[p] = data;} else {if(data){o[p] = new type(data);} else {o[p] = new type();}}}
return obj;};
var arr=[];
for(var i=0; i<rows.length; i++){var o = ctor(rows[i]);
if(fnMod){fnMod(o, rows[i]);}
arr.push(o);}
return arr;},
onSetData:function(){ },
onClearData:function(){ },
onAddData:function(obj){ },
onAddDataRange:function(arr){ },
onRemoveData:function(obj){ },
onUpdateField:function(obj, field, val){ }});
