function clearForm(form,clearHidden){
  with (form){
    for (i=0;i<elements.length; i++){
      if(elements[i].type=='text')elements[i].value='';
      if(clearHidden && elements[i].type=='hidden')elements[i].value='';
      if(elements[i].options){
        for (j=0;j<elements[i].options.length;j++){
          elements[i].options[j].selected=false;  
        }
        elements[i].options[0].selected = true;
      }
      if(elements[i].checked)elements[i].checked=false;
    }
  }
}

function addFilter(filterId,form,field){
  var element = document.getElementById(filterId);
  if (element == null) return true;
  if (form[field] == null) return true;
  var selected = false;
  if (element && element.options){
    for(j=0;j<element.options.length;j++){
      if (element.options[j].selected && element.options[j].value.length > 0){
        window.alert("setting " + field + " to " + element.options[j].value);
        selected = true;
        form[field].value = element.options[j].value;
      }
    }
  }
  if (!selected){
    window.alert("No selection for " + field);
  }
  return true;
}

function doFilter(form){
  addFilter('provFilter',form,'provenance');
  addFilter('langFilter',form,'lang');
  addFilter('serFilter',form,'pubnum_series');
  return true;
}