var firstByteMark = [ 0x00,0x00,0xC0,0xE0,0xF0,0xF8,0xFC ];
var byteMask = 0xBF;
var byteMark = 0x80;
function clearForm(form){
    for(var i=0;i<form.elements.length;i++){
        if(form.elements[i].checked) form.elements[i].checked = false;
        else if(form.elements[i].type == 'text') form.elements[i].value = "";
    }
}

function help(id){
var beta = (id == 'help-beta');
var word = (id == 'help-word');
var phrase = (id == 'help-phrase');
var lemma = (id == 'help-lemma');
document.getElementById('help-beta').style.visibility = (beta)?'visible':'hidden';
document.getElementById('help-beta').style.visibility = (beta)?'visible':'hidden';
document.getElementById('help-word').style.visibility = (word)?'visible':'hidden';
document.getElementById('help-lemma').style.visibility = (lemma)?'visible':'hidden';
document.getElementById('help-phrase').style.visibility = (phrase)?'visible':'hidden';
}

function UTF16toUTF8Bytes(u16){
    var bytes = new Array();
    if (u16 < 128){
        bytes.length = 1;
    } else if (u16 < 2048){
        bytes.length = 2;
    } else { // presuming max js charCode of 65535
        bytes.length = 3;
    }
    switch (bytes.length){
        case 3:
            bytes[2] = ((u16 | byteMark) & byteMask);
            u16 >>= 6;
        case 2:
            bytes[1] = ((u16 | byteMark) & byteMask);
            u16 >>= 6;
        case 1:
            bytes[0] = (u16 | firstByteMark[bytes.length]);
    }
    return bytes;
}
function encode(input){
    var output = new Array();
    var inputArray = input.split(/\s+/);
    for(var i=0;i<inputArray.length;i++){
        var term = '';
        for(var j=0;j<inputArray[i].length;j++){
            var u16 = inputArray[i].charCodeAt(j);
            if (u16 < 128){
                term += inputArray[i].charAt(j);
                continue;
            }
            var utf8bytes = UTF16toUTF8Bytes(u16);
            for(var k=0;k<utf8bytes.length;k++){
                if(utf8bytes[k] < 16){
                    term += "%0";
                } else {
                   term += "%";
                }
                term += utf8bytes[k].toString(16);
            }
        }
        output[i] = term;
    }
    return output;
}
function setQueries(){
    var debug = document.getElementById('debug') && document.getElementById('debug').checked;
    var form = document.getElementById("query-form");
    var queries = new Array();
    var rels = new Array();
    var slops = new Array(); 
    var termctr = 0;
    var caps = form.caps.checked;
    caps |= (form.caps.type == 'hidden' && form.caps.value=="on");
    var marks = form.marks.checked;
    marks |= (form.marks.type == 'hidden' && form.marks.value=="on");
    var lemmas = form.lemmas.checked;
    lemmas |= (form.lemmas.type == 'hidden' && form.lemmas.value=="on");
    var beta = (document.getElementById('betaYes') && document.getElementById('betaYes').checked);
    beta |= (form.beta.type == 'hidden' && form.beta.value=="on");
    var tmod = 'IA';
    
    if(caps){
      if(marks){
        tmod = -1;
      }
      else{
        tmod = 'IM';
      }
    }
    else if(marks) tmod = 'IC';
    if(lemmas) tmod = -1;
    for(var i=0;i<form.elements.length;i++){
        if(form.elements[i].name != "queryterm") continue;
        else termctr++;

        if(form.elements[i].value == '') continue;

        var input = form.elements[i].value;
        var out = input.split(/\s+/); // encode(input);
        var query = (lemmas)?"lemma(":"sub(";
        query += (beta)?("beta(\"" + input + "\")"):("\""+input+"\"");
        if(tmod != -1) query += (',' + tmod);
        query += ")";
        query = query.replace(/[#]/g,'^');
        if(debug) window.alert(query);
        
        queries[queries.length] = query;
        
        if (termctr > 1){
            var bID = "boolean-" + (termctr - 1);
            rels[rels.length] = document.getElementById(bID).value;
            var sID = "slop-" + (termctr - 1);
            var slop = (document.getElementById(sID))?parseInt(document.getElementById(sID).value):10;
            if(isNaN(slop)) slop = 10;
            slops[slops.length] = slop;
        }
    }
    if(queries.length == 0) return false;
    var query = queries[0];
    for(var i=0;i<rels.length;i++){
        if(rels[i] == 'NOT') query = 'notnear(' + query + ',' + queries[i+1] + ',' + slops[i] + ')';
        if(rels[i] == 'AND') query = 'near(' + query + ',' + queries[i+1] + ',' + slops[i] + ')';
        if(rels[i] == 'THEN') query = 'then(' + query + ',' + queries[i+1] + ',' + slops[i] + ')';
        if(rels[i] == 'OR') query = 'or(' + query + ',' + queries[i+1] + ')';
    }
    query = 'query('+query+')';
    document.getElementById('query-1').value = query;
    return true;}
function setDocQueries(){
    var debug = document.getElementById('debug') && document.getElementById('debug').checked;
    var form = document.getElementById("query-form");
    var queries = new Array();
    var rels = new Array();
    var slops = new Array(); 
    var termctr = 0;
    var tmod = 'IA';
    var caps = form.caps.checked;
    caps |= (form.caps.type == 'hidden' && form.caps.value=="on");
    var marks = form.marks.checked;
    marks |= (form.marks.type == 'hidden' && form.marks.value=="on");
    var lemmas = form.lemmas.checked;
    lemmas |= (form.lemmas.type == 'hidden' && form.lemmas.value=="on");
    var beta = (document.getElementById('betaYes') && document.getElementById('betaYes').checked);
    beta |= (form.beta.type == 'hidden' && form.beta.value=="on");
    if(caps){
      if(marks){
        tmod = -1;
      }
      else{
        tmod = 'IM';
      }
    }
    else if(marks) tmod = 'IC';
    if(lemmas) tmod = -1;
    
    for(var i=0;i<form.elements.length;i++){
        if(form.elements[i].name != "queryterm") continue;
        else termctr++;

        if(form.elements[i].value == '') continue;

        var input = form.elements[i].value;
        var out = input.split(/\s+/); // encode(input);
        var query = (lemmas)?"lemma(":"sub(";
        query += (beta)?("beta(\"" + input + "\")"):("\""+input+"\"");
        if(tmod != -1) query += (',' + tmod);
        query += ")";
        query = query.replace(/[#]/g,'^');
        if(debug) window.alert(query);
        
        queries[queries.length] = query;
        
        if (termctr > 1){
            var bID = "boolean-" + (termctr - 1);
            rels[rels.length] = document.getElementById(bID).value;
            var sID = "slop-" + (termctr - 1);
            var slop = (document.getElementById(sID))?parseInt(document.getElementById(sID).value):10;
            if(isNaN(slop)) slop = 10;
            slops[slops.length] = slop;
        }
    }
    var query = queries[0];
    for(var i=0;i<rels.length;i++){
        if(rels[i] == 'NOT') query = 'notnear(' + query + ',' + queries[i+1] + ',' + slops[i] + ')';
        if(rels[i] == 'AND') query = 'near(' + query + ',' + queries[i+1] + ',' + slops[i] + ')';
        if(rels[i] == 'THEN') query = 'then(' + query + ',' + queries[i+1] + ',' + slops[i] + ')';
        if(rels[i] == 'OR') query = 'or(' + query + ',' + queries[i+1] + ')';
    }
    var offset = document.getElementById('offset').value;
    query = 'docs(' + query + ',' + offset + ')';
    document.getElementById('query-1').value = query;
    return true;
}
function openWindow(pipeline)
    {    
      var vWinUsers = window.open(pipeline, 'PortletSelector', 'status=no,resizable=yes,width=500,height=600,scrollbars=yes');
      vWinUsers.opener = self;
      vWinUsers.focus();
    }
    function highlightBoxes(boxcolor) {
	    // ASSUMPTION: this isn't going to change much :) -- hence the hard-coded loop 
	    MINBOX = 1;
	    MAXBOX = 4;
	    for (MINBOX;MINBOX<MAXBOX;MINBOX++) {
		 var check="word"+MINBOX;
	    	document.getElementById(check).style.background = boxcolor;
	    }

	    linkBackground = boxcolor;
	   if (boxcolor == "white") 
		   linkBackground = "transparent";
	   document.getElementById('betaCodeLink').style.background = linkBackground; 
	    
    }
   function enableHelp(divName) {
	   document.getElementById(divName).style.display = 'block';
   }
   function disableHelp(divName) {
	   document.getElementById(divName).style.display = 'none';
   }

    