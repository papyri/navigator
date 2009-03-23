if (!document.ELEMENT_NODE) {
  document.ELEMENT_NODE = 1;
  document.ATTRIBUTE_NODE = 2;
  document.TEXT_NODE = 3;
  document.CDATA_SECTION_NODE = 4;
  document.ENTITY_REFERENCE_NODE = 5;
  document.ENTITY_NODE = 6;
  document.PROCESSING_INSTRUCTION_NODE = 7;
  document.COMMENT_NODE = 8;
  document.DOCUMENT_NODE = 9;
  document.DOCUMENT_TYPE_NODE = 10;
  document.DOCUMENT_FRAGMENT_NODE = 11;
  document.NOTATION_NODE = 12;
}
document._importNode = function(node, allChildren) {
  var newNode = null;
  typeSwitch:
  switch (node.nodeType) {
    case document.ELEMENT_NODE:
      newNode = document.createElement(node.nodeName);
      /* does the node have any attributes to add? */
      if (node.attributes && node.attributes.length > 0){
        for (var i = 0; i < node.attributes.length;i++){
           newNode.setAttribute(node.attributes.item(i).nodeName, node.getAttribute(node.attributes.item(i).nodeName));
        }
      }
      if (allChildren && node.childNodes && node.childNodes.length > 0) {
        for (var j = 0; j < node.childNodes.length;j++){
          var cNode = document._importNode(node.childNodes.item(j), allChildren);
          if (window.ActiveXObject && (newNode.nodeName == 'STYLE' || newNode.nodeName == 'SCRIPT')){
              newNode.text = cNode.nodeValue;
          }
          else{
              newNode.appendChild(cNode);
          }
        }
      }
      if (newNode == null) alert('returning null for ELEMENT node');
      break typeSwitch;
    case document.TEXT_NODE:
      newNode = document.createTextNode(node.nodeValue);
      if (newNode == null) alert(('returning null for TEXT node\n' + node.nodeValue));
      break typeSwitch;
    case document.CDATA_SECTION_NODE:
      newNode = document.createTextNode(node.nodeValue);
      if (newNode == null) alert(('returning null for CDATA node\n' + node.nodeValue));
      break typeSwitch;
    case document.COMMENT_NODE:
      newNode = document.createTextNode(node.nodeValue);
      if (newNode == null) alert(('returning null for COMMENT node\n' + node.nodeValue));
      break typeSwitch;
  }
  return newNode;
};
// ^^ modified from http://www.alistapart.com/articles/crossbrowserscripting ^^
function processXML(url,target) {
  var obj;
  if (window.XMLHttpRequest) {
    obj = new XMLHttpRequest();
    obj.onreadystatechange = getNavigationCallBack(obj,target);
    obj.open("GET", url, true);
    obj.send(null);
  // IE/Windows ActiveX object
  } else if (window.ActiveXObject) {
    obj = new ActiveXObject("Microsoft.XMLHTTP");
    if (obj) {
      obj.onreadystatechange = getNavigationCallBack(obj,target);
      obj.open("GET", url, true);
      obj.send();
    }
  } else {
    alert("Your browser does not support AJAX");
  }
}
function processChange() {
    // 4 means the response has been returned and ready to be processed
    if (obj.readyState == 4) {
        // 200 means "OK"
        if (obj.status == 200) {
            // process whatever has been sent back here:
        // anything else means a problem
        } else {
            alert("There was a problem in the returned data:\n");
        }
    }
}

function getNavigationCallBack(xmlObj,target){
    return function(){
    if (xmlObj.readyState == 4) {
        if (xmlObj.status == 200) {
            oldSelected = target.options[target.selectedIndex];
            var option = document.createElement('OPTION');
            option.appendChild(document.createTextNode('Select'));
            option.setAttribute('value','');
            oldSelected.selected = false;
            options = [];
            for (i=0;i<target.options.length;i++){
                options[options.length] = target.options[i];
            }
            while (target.options.length > 0){
                target.remove(target.options.length - 1);
            }

            if (target.options.length == 0){
            target.appendChild(option);
            }
            else{
            target.insertBefore(option,target.options[0]);
            }
            target.selectedIndex = 0;
            
            var items = xmlObj.responseXML.getElementsByTagName('item');
            for(i=0;i<items.length;i++){
                option = document.createElement('option');
                textNode = items.item(i).childNodes[0];
                option.appendChild(document.createTextNode(textNode.nodeValue));
                option.setAttribute('value',textNode.nodeValue);
                target.appendChild(option);
            }
            if (items.length == 0){
                var msg = xmlObj.responseXML.getElementsByTagName('msg').item(0).childNodes[0].nodeValue;
                window.alert(msg);
            }
            // parse response, load new options
        } else {
            alert("There was a problem in the returned data:\nstatus code: " + xmlObj.status);
        }
    }
    };
} 

function getDocumentCallBack(xmlObj,target){
    return function(){
    if (xmlObj.readyState == 4) {
        if (xmlObj.status == 200) {
            var docElement = document.getElementById(target);
            nodes = [];
            for (i=0;i<docElement.childNodes.length;i++){
                nodes[nodes.length] = docElement.childNodes[i];
            }
            for (i=0;i<nodes.length;i++){
                docElement.removeChild(nodes[i]);
            }
            var tei2 = xmlObj.responseXML.documentElement;
            if (tei2 == null) alert('tei2 is null');
            for (i=0;i<tei2.childNodes.length;i++){
              var cNode = tei2.childNodes.item(i);
              var impNode = document._importNode(cNode,true);
              if (impNode == null){
                  alert(('imported doc node is null; cNode type was ' + cNode.nodeType + ' name was ' + cNode.nodeName));
              }
              else {
                  docElement.appendChild(impNode);
                  if (impNode.nodeType != document.ELEMENT_NODE && impNode.nodeType != document.TEXT_NODE) alert('unexpected node type: ' + impNode.nodeType);
              }
            }
            
        } else {
            alert("There was a problem in the returned data:\nstatus code: " + xmlObj.status);
        }
    }
    };
} 

function loadItems(mode) {
       var series = '';
       var volume = '';
       //var sI = document.getElementById('series').selectedIndex;
       var vI = document.getElementById('volume').selectedIndex;
       series = document.getElementById('series').value;
       if (vI != -1) volume = document.getElementById('volume').options.item(vI).value;
       var url  =  '/ddbdp-nav/items?mode=' + mode;
       var target;
       if (mode == 'volume'){
         url += '&series=' + series;
         target = document.getElementById('volume');
       }
       if (mode == 'document'){
         url += '&series=' + series + '&volume=' + volume;
         target = document.getElementById('document')
       }
       if (mode == 'series') target = document.getElementById('series');
       var items = processXML(url,target);
}

function resetItems(mode) {
            var target = document.getElementById(mode);
            oldSelected = target.options[target.selectedIndex];
            var option = document.createElement('OPTION');
            option.appendChild(document.createTextNode('Select'));
            option.setAttribute('value','');
            oldSelected.selected = false;
            options = [];
            for (i=0;i<target.options.length;i++){
                options[options.length] = target.options[i];
            }
            while (target.options.length > 0){
                target.remove(target.options.length - 1);
            }

            if (target.options.length == 0){
            target.appendChild(option);
            }
            else{
            target.insertBefore(option,target.options[0]);
            }
            target.selectedIndex = 0;
}

function loadDocument() {
       var series = '';
       var volume = '';
       var documentVal = '';
       //var sI = document.getElementById('series').selectedIndex;
       var vI = document.getElementById('volume').selectedIndex;
       var dI = document.getElementById('document').selectedIndex;
       series = document.getElementById('series').value;
       if (vI != -1) volume = document.getElementById('volume').options.item(vI).value;
       if (dI != -1) documentVal = document.getElementById('document').options.item(dI).value;
       if (documentVal == '') return;
       var url  =  '/ddbdp-nav/text?series=' + series;
       url += '&volume=' + volume;
       url += '&document=' + documentVal;

  var obj;
  if (window.XMLHttpRequest) {
    obj = new XMLHttpRequest();
    obj.onreadystatechange = getDocumentCallBack(obj,'docText');
    obj.open("GET", url, true);
    obj.send(null);
  // IE/Windows ActiveX object
  } else if (window.ActiveXObject) {
    obj = new ActiveXObject("Microsoft.XMLHTTP");
    if (obj) {
      obj.onreadystatechange = getDocumentCallBack(obj,'docText');
      obj.open("GET", url, true);
      obj.send();
    }
  } else {
    alert("Your browser does not support AJAX");
  }
}

var x="",$a=false,qa="",da=false,k="",M="",j="",r=-1,n=null,
A=-1,U=null,pa=5,w="",mb="div",Ya="span",HTML_FORM=null,textQuery=null,i=null,m=null,
Ta=null,Va=null,J=null,u=null,ua=false,Ca={},T=1,ia=1,ha=false,
aa=false,va=-1,Qa=(new Date).getTime(),XML_HHTP_FOUND=false,q=null,suggestURI=null,E=null,
K=null,Z=null,$=false,Da=false,v=60,Pa=null,na=null,D=0,I=0,La=null,X=null,
Y=null,ka=false,W=false,ja="",C=null,S=null,p=null,t=null,oa=null,
Ma=-1,Na=-1,G="left",V="right",R=0,ya=false;

    var mads = new Array();
      
  mads[0] = ["BGU", "BGU"];

  mads[1] = ["C.Epist.Lat.", "C.Epist.Lat."];

  mads[2] = ["CEL", "C.Epist.Lat."];

  mads[3] = ["C.Étiq.Mom.", "C.Étiq.Mom."];

  mads[4] = ["C.Illum.Pap.", "C.Illum.Pap."];

  mads[5] = ["C.Jud.Syr.Eg.", "C.Jud.Syr.Eg."];

  mads[6] = ["C.Pap.Gr.", "C.Pap.Gr."];

  mads[7] = ["CPGr", "C.Pap.Gr."];

  mads[8] = ["CP.Gr", "C.Pap.Gr."];

  mads[9] = ["Chrest.Mitt.", "Chrest.Mitt."];

  mads[10] = ["M.Chr.", "Chrest.Mitt."];

  mads[11] = ["Chrest.Wilck.", "Chrest.Wilck."];

  mads[12] = ["CPR", "CPR"];

  mads[13] = ["O.Amst.", "O.Amst."];

  mads[14] = ["O.Bodl.", ""];

  mads[15] = ["O.Ashm.Shelt.", "O.Ashm.Shelt."];

  mads[16] = ["O.Ashm.Shelton", "O.Ashm.Shelt."];

  mads[17] = ["O.Berl.", "O.Berl."];

  mads[18] = ["O.Bodl.", "O.Bodl."];

  mads[19] = ["O.Brux.", "O.Brux."];

  mads[20] = ["O.Bu Njem", "O.Bu Njem"];

  mads[21] = ["O.Buch.", "O.Buch."];

  mads[22] = ["O.Cair.", "O.Cair."];

  mads[23] = ["O.Cair. GPW", "O.Cair."];

  mads[24] = ["O.Camb.", "O.Camb."];

  mads[25] = ["O.Bodl.", "O.Camb."];

  mads[26] = ["O.Claud.", "O.Claud."];

  mads[27] = ["O.Deiss.", "O.Deiss."];

  mads[28] = ["O.Douch", "O.Douch"];

  mads[29] = ["O.Edfou", "O.Edfou"];

  mads[30] = ["O.Elkab", "O.Elkab"];

  mads[31] = ["O.Erem.", "O.Erem."];

  mads[32] = ["O.Fay.", "O.Fay."];

  mads[33] = ["O.Florida", "O.Florida"];

  mads[34] = ["O.Joach.", "O.Joach."];

  mads[35] = ["O.Joachim", "O.Joach."];

  mads[36] = ["O.dem.Joach.", "O.Joach."];

  mads[37] = ["O.Leid.", "O.Leid."];

  mads[38] = ["O.Lund.", "O.Lund."];

  mads[39] = ["O.Masada", "O.Masada"];

  mads[40] = ["O.Medin.Madi", "O.Medin.Madi"];

  mads[41] = ["P.Medin.Madi", "O.Medin.Madi"];

  mads[42] = ["O.Mich.", "O.Mich."];

  mads[43] = ["O.Minor", "O.Minor"];

  mads[44] = ["O.Bodl.", "O.Minor"];

  mads[45] = ["O.Narm.", "O.Narm."];

  mads[46] = ["O.Oasis", "O.Oasis"];

  mads[47] = ["O.Chams el-Din", "O.Oasis"];

  mads[48] = ["O.Ont.Mus.", "O.Ont.Mus."];

  mads[49] = ["O.ROM", "O.Ont.Mus."];

  mads[50] = ["O.Oslo", "O.Oslo"];

  mads[51] = ["O.Paris", "O.Paris"];

  mads[52] = ["O.Par.", "O.Paris"];

  mads[53] = ["O.Petr.", "O.Petr."];

  mads[54] = ["O.Bodl.", "O.Petr."];

  mads[55] = ["O.Sarga", "O.Sarga"];

  mads[56] = ["P.Sarga", "O.Sarga"];

  mads[57] = ["O.Stras.", "O.Stras."];

  mads[58] = ["O.Strasb.", "O.Stras."];

  mads[59] = ["O.Tebt.Pad.", "O.Tebt.Pad."];

  mads[60] = ["O.Tebt.", "O.Tebt."];

  mads[61] = ["O.Theb.", "O.Theb."];

  mads[62] = ["O.Vleem.", "O.Vleem."];

  mads[63] = ["O.WadiHamm.", "O.WadiHamm."];

  mads[64] = ["O.Waqfa", "O.Waqfa"];

  mads[65] = ["O.Wilb.", "O.Wilb."];

  mads[66] = ["O.Wilck.", "O.Wilck."];

  mads[67] = ["WO", "O.Wilck."];

  mads[68] = ["P.Aberd.", "P.Aberd."];

  mads[69] = ["P.Abinn.", "P.Abinn."];

  mads[70] = ["P.Achm.", "P.Achm."];

  mads[71] = ["P.Adl.", "P.Adl."];

  mads[72] = ["P.Adler", "P.Adl."];

  mads[73] = ["P.Alex.Giss.", "P.Alex.Giss."];

  mads[74] = ["P.Alex.", "P.Alex."];

  mads[75] = ["P.Amh.", "P.Amh."];

  mads[76] = ["P.Amst.", "P.Amst."];

  mads[77] = ["P.Anag.", "P.Anag."];

  mads[78] = ["P.Ant.", "P.Ant."];

  mads[79] = ["P.Ashm.", "P.Ashm."];

  mads[80] = ["P.Athen.", "P.Athen."];

  mads[81] = ["P.Athen.Xyla", "P.Athen.Xyla"];

  mads[82] = ["P.Sta.Xyla", "P.Athen.Xyla"];

  mads[83] = ["P.Aust.Herr.", "P.Aust.Herr."];

  mads[84] = ["P.Trophitis", "P.Aust.Herr."];

  mads[85] = ["P.Yadin", "P.Yadin"];

  mads[86] = ["P.Babatha", "P.Yadin"];

  mads[87] = ["P.Bacch.", "P.Bacch."];

  mads[88] = ["P.Bad.", "P.Bad."];

  mads[89] = ["VBP", "P.Bad."];

  mads[90] = ["P.Bal.", "P.Bal."];

  mads[91] = ["P.Bas.", "P.Bas."];

  mads[92] = ["P.Batav.", "P.Batav."];

  mads[93] = ["P.Berl.Bibl.", "P.Berl.Bibl."];

  mads[94] = ["P.Berl.Bork.", "P.Berl.Bork."];

  mads[95] = ["P.Berl.Brash.", "P.Berl.Brash."];

  mads[96] = ["P.Berl.Frisk", "P.Berl.Frisk"];

  mads[97] = ["P.Berl.Leihg.", "P.Berl.Leihg."];

  mads[98] = ["P.Berl.Möller", "P.Berl.Möller"];

  mads[99] = ["P.Berl.Sarisch.", "P.Berl.Sarisch."];

  mads[100] = ["P.Berl.Thun.", "P.Berl.Thun."];

  mads[101] = ["P.Berl.Zill.", "P.Berl.Zill."];

  mads[102] = ["P.Bon.", "P.Bon."];

  mads[103] = ["P.Bour.", "P.Bour."];

  mads[104] = ["P.Brem.", "P.Brem."];

  mads[105] = ["P.Brookl.", "P.Brookl."];

  mads[106] = ["P.Brook.", "P.Brookl."];

  mads[107] = ["P.Brux.", "P.Brux."];

  mads[108] = ["P.Bub.", "P.Bub."];

  mads[109] = ["P.Cair.Goodsp.", "P.Cair.Goodsp."];

  mads[110] = ["P.Cair. Goodspeed", "P.Cair.Goodsp."];

  mads[111] = ["P.Cair.Isid.", "P.Cair.Isid."];

  mads[112] = ["P.Cair.Masp.", "P.Cair.Masp."];

  mads[113] = ["P.Cair.Mich.", "P.Cair.Mich."];

  mads[114] = ["P.Cair.Preis.", "P.Cair.Preis."];

  mads[115] = ["P.Cair.Zen.", "P.Cair.Zen."];

  mads[116] = ["P.Charite", "P.Charite"];

  mads[117] = ["P.Col.", "P.Col."];

  mads[118] = ["P.Col.Teeter", "P.Col.Teeter"];

  mads[119] = ["P.Coll.Youtie", "P.Coll.Youtie"];

  mads[120] = ["P.Corn.", "P.Corn."];

  mads[121] = ["P.Customs", "P.Customs"];

  mads[122] = ["P.David", "P.David"];

  mads[123] = ["P.Diog.", "P.Diog."];

  mads[124] = ["P.Dion.", "P.Dion."];

  mads[125] = ["P.Dubl.", "P.Dubl."];

  mads[126] = ["P.Dub.", "P.Dubl."];

  mads[127] = ["P.Dura", "P.Dura"];

  mads[128] = ["P.Edfou", "P.Edfou"];

  mads[129] = ["P.Eleph.", "P.Eleph."];

  mads[130] = ["P.Enteux.", "P.Enteux."];

  mads[131] = ["P.Erasm.", "P.Erasm."];

  mads[132] = ["P.Erl.", "P.Erl."];

  mads[133] = ["P.Fam.Tebt.", "P.Fam.Tebt."];

  mads[134] = ["P.Fay.", "P.Fay."];

  mads[135] = ["P.Flor.", "P.Flor."];

  mads[136] = ["P.Fouad", "P.Fouad"];

  mads[137] = ["P.Frankf.", "P.Frankf."];

  mads[138] = ["P.Freer", "P.Freer"];

  mads[139] = ["P.Freib.", "P.Freib."];

  mads[140] = ["P.FuadUniv.", "P.FuadUniv."];

  mads[141] = ["P.Fuad I Univ.", "P.FuadUniv."];

  mads[142] = ["P.Gen.", "P.Gen."];

  mads[143] = ["P.Genova", "P.Genova"];

  mads[144] = ["PUG", "P.Genova"];

  mads[145] = ["P.Giss.", "P.Giss."];

  mads[146] = ["P.Giss.Univ.", "P.Giss.Univ."];

  mads[147] = ["P.Giss.Bibl.", "P.Giss.Univ."];

  mads[148] = ["P.Got.", "P.Got."];

  mads[149] = ["P.Grad.", "P.Grad."];

  mads[150] = ["P.Graux", "P.Graux"];

  mads[151] = ["P.Grenf.", "P.Grenf."];

  mads[152] = ["P.Gron.", "P.Gron."];

  mads[153] = ["P.Gur.", "P.Gur."];

  mads[154] = ["P.Gurob.", "P.Gur."];

  mads[155] = ["P.Hal.", "P.Hal."];

  mads[156] = ["P.Hamb.", "P.Hamb."];

  mads[157] = ["P.Harr.", "P.Harr."];

  mads[158] = ["P.Haun.", "P.Haun."];

  mads[159] = ["P.Heid.", "P.Heid."];

  mads[160] = ["P.Hels.", "P.Hels."];

  mads[161] = ["P.Herm.Landl.", "P.Herm.Landl."];

  mads[162] = ["P.Landl.", "P.Herm.Landl."];

  mads[163] = ["P.Herm.", "P.Herm."];

  mads[164] = ["P.Hib.", "P.Hib."];

  mads[165] = ["P.Hombert", "P.Hombert"];

  mads[166] = ["P.Iand.inv. 653", "P.Iand.inv. 653"];

  mads[167] = ["P.Iand.", "P.Iand."];

  mads[168] = ["P.IFAO", "P.IFAO"];

  mads[169] = ["P.Ital.", "P.Ital."];

  mads[170] = ["P.Jena", "P.Jena"];

  mads[171] = ["P.Kar.Goodsp.", "P.Kar.Goodsp."];

  mads[172] = ["P.Kell.", "P.Kell."];

  mads[173] = ["P.Kellis", "P.Kell."];

  mads[174] = ["P.Köln", "P.Köln"];

  mads[175] = ["P.Kroll", "P.Kroll"];

  mads[176] = ["P.Kroll.", "P.Kroll"];

  mads[177] = ["P.Kron.", "P.Kron."];

  mads[178] = ["P.Laur.", "P.Laur."];

  mads[179] = ["P.LeedsMus.", "P.LeedsMus."];

  mads[180] = ["P.Leeds Museum", "P.LeedsMus."];

  mads[181] = ["P.Leid.Inst.", "P.Leid.Inst."];

  mads[182] = ["P.Leipz.", "P.Leipz."];

  mads[183] = ["P.Leit.", "P.Leit."];

  mads[184] = ["P.Lille", "P.Lille"];

  mads[185] = ["P.Lips.", "P.Lips."];

  mads[186] = ["P.Lond.", "P.Lond."];

  mads[187] = ["P.Lund", "P.Lund"];

  mads[188] = ["P.Marm.", "P.Marm."];

  mads[189] = ["P.Masada", "P.Masada"];

  mads[190] = ["P.Matr.", "P.Matr."];

  mads[191] = ["P.Mert.", "P.Mert."];

  mads[192] = ["P.Merton", "P.Mert."];

  mads[193] = ["P.Meyer", "P.Meyer"];

  mads[194] = ["P.Mich.Aphrod.", "P.Mich.Aphrod."];

  mads[195] = ["P.Mich.Mchl.", "P.Mich.Mchl."];

  mads[196] = ["P.Mich.", "P.Mich."];

  mads[197] = ["P.Mich.Zen.", "P.Mich."];

  mads[198] = ["P.Michael.", "P.Michael."];

  mads[199] = ["P.Mil.Congr.XIV", "P.Mil.Congr.XIV"];

  mads[200] = ["P.Mil.Congr.XIX", "P.Mil.Congr.XIX"];

  mads[201] = ["P.Mil.Congr.XVII", "P.Mil.Congr.XVII"];

  mads[202] = ["P.Mil.Congr.XVIII", "P.Mil.Congr.XVIII"];

  mads[203] = ["P.Mil.", "P.Mil."];

  mads[204] = ["P.Med.", "P.Mil."];

  mads[205] = ["P.Mil.Vogl.", "P.Mil.Vogl."];

  mads[206] = ["P.Mil.R.Univ.", "P.Mil.Vogl."];

  mads[207] = ["PRIMI", "P.Mil.Vogl."];

  mads[208] = ["P.R.U.M.", "P.Mil.Vogl."];

  mads[209] = ["P.Münch.", "P.Münch."];

  mads[210] = ["P.Monac.", "P.Münch."];

  mads[211] = ["P.Murabba'ât", "P.Murabba'ât"];

  mads[212] = ["P.Mur.", "P.Murabba'ât"];

  mads[213] = ["P.Nag Hamm.", "P.Nag Hamm."];

  mads[214] = ["P.Neph.", "P.Neph."];

  mads[215] = ["P.Nepheros", "P.Neph."];

  mads[216] = ["P.Ness.", "P.Ness."];

  mads[217] = ["P.Nessana", "P.Ness."];

  mads[218] = ["P.NYU", "P.NYU"];

  mads[219] = ["P.Oslo", "P.Oslo"];

  mads[220] = ["P.Oxf.", "P.Oxf."];

  mads[221] = ["P.Oxy.Descr.", "P.Oxy.Descr."];

  mads[222] = ["P.Oxy.Hels.", "P.Oxy.Hels."];

  mads[223] = ["P.Oxy.", "P.Oxy."];

  mads[224] = ["P.Panop.Beatty", "P.Panop.Beatty"];

  mads[225] = ["P.Panop.", "P.Panop."];

  mads[226] = ["P.Paris", "P.Paris"];

  mads[227] = ["P.Petaus", "P.Petaus"];

  mads[228] = ["P.Petr.² I", "P.Petr.² I"];

  mads[229] = ["P.Petr.2", "P.Petr.² I"];

  mads[230] = ["P.Petr(2)", "P.Petr.² I"];

  mads[231] = ["P.Petr.", "P.Petr."];

  mads[232] = ["P.Phil.", "P.Phil."];

  mads[233] = ["P.Prag.", "P.Prag."];

  mads[234] = ["P.Prag.Varcl", "P.Prag.Varcl"];

  mads[235] = ["P.Princ.Roll", "P.Princ.Roll"];

  mads[236] = ["P.Princ.", "P.Princ."];

  mads[237] = ["P.Quseir", "P.Quseir"];

  mads[238] = ["P.Rain.Cent.", "P.Rain.Cent."];

  mads[239] = ["P.Rainer Cent.", "P.Rain.Cent."];

  mads[240] = ["P.Rein.", "P.Rein."];

  mads[241] = ["P.Rev.", "P.Rev."];

  mads[242] = ["P.Ross.Georg.", "P.Ross.Georg."];

  mads[243] = ["P.Ryl.", "P.Ryl."];

  mads[244] = ["P.Sakaon", "P.Sakaon"];

  mads[245] = ["P.Sarap.", "P.Sarap."];

  mads[246] = ["P.Sel.Warga", "P.Sel.Warga"];

  mads[247] = ["P.Select.", "P.Select."];

  mads[248] = ["P.Sorb.", "P.Sorb."];

  mads[249] = ["P.Soter.", "P.Soter."];

  mads[250] = ["P.Soterichos", "P.Soter."];

  mads[251] = ["P.Stras.", "P.Stras."];

  mads[252] = ["P.Strasb.", "P.Stras."];

  mads[253] = ["P.Tebt.", "P.Tebt."];

  mads[254] = ["P.Tebt.Tait", "P.Tebt.Tait"];

  mads[255] = ["P.Tebt.Wall", "P.Tebt.Wall"];

  mads[256] = ["P.Theon.", "P.Theon."];

  mads[257] = ["P.Thmouis", "P.Thmouis"];

  mads[258] = ["P.Tor.Amen.", "P.Tor.Amen."];

  mads[259] = ["P.Tor.Amenothes", "P.Tor.Amen."];

  mads[260] = ["P.Tor.Choach.", "P.Tor.Choach."];

  mads[261] = ["P.Tor.", "P.Tor."];

  mads[262] = ["P.Turner", "P.Turner"];

  mads[263] = ["P.Ups.Frid", "P.Ups.Frid"];

  mads[264] = ["P.Vars.", "P.Vars."];

  mads[265] = ["P.Vat.Aphrod.", "P.Vat.Aphrod."];

  mads[266] = ["P.Vatic.Aphrod.", "P.Vat.Aphrod."];

  mads[267] = ["P.Vind.Bosw.", "P.Vind.Bosw."];

  mads[268] = ["P.Vindob.Bosw.", "P.Vind.Bosw."];

  mads[269] = ["P.Vind.Pher.", "P.Vind.Pher."];

  mads[270] = ["P.Vindob.Pher.", "P.Vind.Pher."];

  mads[271] = ["P.Vind.Sal.", "P.Vind.Sal."];

  mads[272] = ["P.Vindob.Sal.", "P.Vind.Sal."];

  mads[273] = ["P.Vind.Sijp.", "P.Vind.Sijp."];

  mads[274] = ["P.Vindob.Sijp.", "P.Vind.Sijp."];

  mads[275] = ["P.Vind.Tand.", "P.Vind.Tand."];

  mads[276] = ["P.Vindob.Tand.", "P.Vind.Tand."];

  mads[277] = ["P.Vind.Worp", "P.Vind.Worp"];

  mads[278] = ["P.Vindob.Worp.", "P.Vind.Worp"];

  mads[279] = ["P.Warr.", "P.Warr."];

  mads[280] = ["P.Wash.Univ.", "P.Wash.Univ."];

  mads[281] = ["P.Wisc.", "P.Wisc."];

  mads[282] = ["P.Würzb.", "P.Würzb."];

  mads[283] = ["P.Yale", "P.Yale"];

  mads[284] = ["P.Zen.Pestm.", "P.Zen.Pestm."];

  mads[285] = ["Pap.Agon.", "Pap.Agon."];

  mads[286] = ["Pap.Biling.", "Pap.Biling."];

  mads[287] = ["Pap.Choix", "Pap.Choix"];

  mads[288] = ["PSI Congr.XI", "PSI Congr.XI"];

  mads[289] = ["PSI Congr.XVII", "PSI Congr.XVII"];

  mads[290] = ["PSI Congr.XX", "PSI Congr.XX"];

  mads[291] = ["PSI Congr.XXI", "PSI Congr.XXI"];

  mads[292] = ["PSI Corr.", "PSI Corr."];

  mads[293] = ["PSI", "PSI"];

  mads[294] = ["SB", "SB"];

  mads[295] = ["Stud.Pal.", "Stud.Pal."];

  mads[296] = ["SPP", "Stud.Pal."];

  mads[297] = ["T.Alb.", "T.Alb."];

  mads[298] = ["T.Mom.Louvre", "T.Mom.Louvre"];

  mads[299] = ["T.Varie", "T.Varie"];

  mads[300] = ["T.Vindol.", "T.Vindol."];

  mads[301] = ["UPZ", "UPZ"];

  mads[302] = ["P.Bodl.", "P.Bodl."];

  mads[303] = ["P.Hever", "P.Hever"];

  mads[304] = ["P.Naqlun", "P.Naqlun"];

  mads[305] = ["P.Pommersf.", "P.Pommersf."];

  mads[306] = ["O.Berenike", "O.Berenike"];

  mads[307] = ["O.Ber.", "O.Berenike"];

  mads[308] = ["P.Ammon", "P.Ammon"];

  mads[309] = ["P.Benaki", "P.Benaki"];

  mads[310] = ["P.Ben. Mus.", "P.Benaki"];

  mads[311] = ["P.Berl.Salmen.", "P.Berl.Salmen."];

  mads[312] = ["P.Berl.Salm.", "P.Berl.Salmen."];

  mads[313] = ["P.Bingen", "P.Bingen"];

  mads[314] = ["P.Harrauer", "P.Harrauer"];

  mads[315] = ["P.Petra", "P.Petra"];

  mads[316] = ["P.Polit.Iud.", "P.Polit.Iud."];

  mads[317] = ["P.Polit.Jud.", "P.Polit.Iud."];

  mads[318] = ["P.Thomas", "P.Thomas"];

  mads[319] = ["P.Eirene", "P.Eirene"];

  mads[320] = ["P.Vindob.Eirene", "P.Eirene"];

  mads[321] = ["P.Chic.Haw.", "P.Chic.Haw."];

  mads[322] = ["P.dem.Chic.Haw.", "P.Chic.Haw."];

  mads[323] = ["P.Dion.Herm.", "P.Dion.Herm."];

  mads[324] = ["P.Eleph.Wagner", "P.Eleph.Wagner"];

  mads[325] = ["O.Eleph.Wagner", "P.Eleph.Wagner"];

  mads[326] = ["P.Eleph.DAIK", "P.Eleph.Wagner"];

  mads[327] = ["O.Eleph.DAIK", "P.Eleph.Wagner"];

  mads[328] = ["P.Louvre", "P.Louvre"];

  mads[329] = ["P.Mon.Apollo", "P.Mon.Apollo"];

  mads[330] = ["P.PalauRib", "P.PalauRib"];

  mads[331] = ["P.Palau Rib", "P.PalauRib"];

  mads[332] = ["P.Gen.2", "P.Gen.2"];

  mads[333] = ["P.Diosk.", "P.Diosk."];

  mads[334] = ["P.Phrur.Diosk.", "P.Diosk."];

  mads[335] = ["P.Jud.Des.Misc.", "P.Jud.Des.Misc."];

  mads[336] = ["P.Erl.Diosp.", "P.Erl.Diosp."];

  mads[337] = ["P.Dryton", "P.Dryton"];

  mads[338] = ["O.Kellis", "O.Kellis"];

  mads[339] = ["O.Kell.", "O.Kellis"];

  mads[340] = ["P.Giss.Apoll.", "P.Giss.Apoll."];

  mads[341] = ["O.BawitIFAO", "O.BawitIFAO"];

  mads[342] = ["O.Bawit IFAO", "O.BawitIFAO"];

  mads[343] = ["P.Euphrates", "P.Euphrates"];

  mads[344] = ["P.Euphr.", "P.Euphrates"];

  mads[345] = ["P.Congr.XV", "P.Congr.XV"];

  mads[346] = ["P.XV.Congr.", "P.Congr.XV"];

  mads[347] = ["P.Paramone", "P.Paramone"];

  mads[348] = ["PSI Com.", "PSI Com."];

  mads[349] = ["P.Horak", "P.Horak"];

  mads[350] = ["Ch.L.A.", "Ch.L.A."];

  mads[351] = ["ChLA", "Ch.L.A."];

  mads[352] = ["Rom.Mil.Rec.", "Rom.Mil.Rec."];

  mads[353] = ["P.Zauzich", "P.Zauzich"];

  mads[354] = ["O.Heid.", "O.Heid."];

  mads[355] = ["Actenstücke", "Actenstücke"];

  mads[356] = ["BKT", "BKT"];

  mads[357] = ["BKU", "BKU"];

  mads[358] = ["C.Ord.Ptol.", "C.Ord.Ptol."];

  mads[359] = ["C.Pap.Lat.", "C.Pap.Lat."];

  mads[360] = ["CPL", "C.Pap.Lat."];

  mads[361] = ["C.Pap.Jud.", "C.Pap.Jud."];

  mads[362] = ["CPJ", "C.Pap.Jud."];

  mads[363] = ["C.Ptol.Sklav.", "C.Ptol.Sklav."];

  mads[364] = ["C.Zen.Palestine", "C.Zen.Palestine"];

  mads[365] = ["Chapa, Letters of Condolence", "Chapa, Letters of Condolence"];

  mads[366] = ["O.Ain Labakha", "O.Ain Labakha"];

  mads[367] = ["O.Bahria", "O.Bahria"];

  mads[368] = ["O.Bahria Div.", "O.Bahria Div."];

  mads[369] = ["O.Crum", "O.Crum"];

  mads[370] = ["O.Dor.", "O.Dor."];

  mads[371] = ["O.Krok.", "O.Krok."];

  mads[372] = ["O.Nancy", "O.Nancy"];

  mads[373] = ["O.Sarm.", "O.Sarm."];

  mads[374] = ["O.Skeat.", "O.Skeat."];

  mads[375] = ["O.Wångstedt", "O.Wångstedt"];

  mads[376] = ["P.Beatty", "P.Beatty"];

  mads[377] = ["P.Äg.Handschrift.", "P.Äg.Handschrift."];

  mads[378] = ["P.Brookl.Dem.", "P.Brookl.Dem."];

  mads[379] = ["P.Cair.Cat.", "P.Cair.Cat."];

  mads[380] = ["P.Cair. Cat.", "P.Cair.Cat."];

  mads[381] = ["P.Count.", "P.Count."];

  mads[382] = ["P.Gron.Amst.", "P.Gron.Amst."];

  mads[383] = ["P.Hawara", "P.Hawara"];

  mads[384] = ["P.Hawara dem.", "P.Hawara dem."];

  mads[385] = ["P.Haw.dem.", "P.Hawara dem."];

  mads[386] = ["P.HermitageCopt.", "P.HermitageCopt."];

  mads[387] = ["P.KölnÄgypt", "P.KölnÄgypt"];

  mads[388] = ["P.KölnÄg.", "P.KölnÄgypt"];

  mads[389] = ["P.KölnLüdecckens", "P.KölnLüdecckens"];

  mads[390] = ["P.Leid.", "P.Leid."];

  mads[391] = ["P.Lesestücke", "P.Lesestücke"];

  mads[392] = ["P.Lond.Copt. London", "P.Lond.Copt. London"];

  mads[393] = ["P.Lond.Copt.", "P.Lond.Copt. London"];

  mads[394] = ["P.Lond.Wasser.", "P.Lond.Wasser."];

  mads[395] = ["P.Magdola", "P.Magdola"];

  mads[396] = ["P.Oxy.Census", "P.Oxy.Census"];

  mads[397] = ["P.Recueil", "P.Recueil"];

  mads[398] = ["P.Zaki Aly", "P.Zaki Aly"];

  mads[399] = ["Sel.Pap.", "Sel.Pap."];

  mads[400] = ["Suppl.Mag.", "Suppl.Mag."];

  mads[401] = ["Witkowski, Epistulae privatae", "Witkowski, Epistulae privatae"];

  mads[402] = ["Tibiletti, Lettere privatae", "Tibiletti, Lettere privatae"];

  mads[403] = ["C.Pap.Hengstl", "C.Pap.Hengstl"];

  mads[404] = ["Doc.Eser.Rom.", "Doc.Eser.Rom."];

  mads[405] = ["Feste", "Feste"];

  mads[406] = ["Jur.Pap.", "Jur.Pap."];

  mads[407] = ["O.Ashm.Copt.", "O.Ashm.Copt."];

  mads[408] = ["O.CrumST", "O.CrumST"];

  mads[409] = ["O.CrumVC", "O.CrumVC"];

  mads[410] = ["O.Leid.Dem.", "O.Leid.Dem."];

  mads[411] = ["O.Louvre", "O.Louvre"];

  mads[412] = ["O.Bawit", "O.Bawit"];

  mads[413] = ["O.Brit.Mus.Copt.", "O.Brit.Mus.Copt."];

  mads[414] = ["P.Brux.Dem.", "P.Brux.Dem."];

  mads[415] = ["O.Deir el-Bahari", "O.Deir el-Bahari"];

  mads[416] = ["O.Hor", "O.Hor"];

  mads[417] = ["O.Magnien", "O.Magnien"];

  mads[418] = ["O.Medin. HabuCopt.", "O.Medin. HabuCopt."];

  mads[419] = ["O.Medin. HabuDem.", "O.Medin. HabuDem."];

  mads[420] = ["O.Mattha", "O.Mattha"];

  mads[421] = ["O.Mich.Copt.", "O.Mich.Copt."];

  mads[422] = ["O.Mich.Copt.Etmoulon", "O.Mich.Copt.Etmoulon"];

  mads[423] = ["O.Narm.Dem.", "O.Narm.Dem."];

  mads[424] = ["O.Métrologie", "O.Métrologie"];

  mads[425] = ["O.Mon.Phoib.", "O.Mon.Phoib."];

  mads[426] = ["P.Edmondstone", "P.Edmondstone"];

  mads[427] = ["O.Muzawwaqa", "O.Muzawwaqa"];

  mads[428] = ["O.Tempeleide", "O.Tempeleide"];

  mads[429] = ["O.Vind.Copt.", "O.Vind.Copt."];

  mads[430] = ["O.Zürich", "O.Zürich"];

  mads[431] = ["P.Amh.Eg.", "P.Amh.Eg."];

  mads[432] = ["P.Assoc.", "P.Assoc."];

  mads[433] = ["P.Auswahl", "P.Auswahl"];

  mads[434] = ["P.Berl.Dem.", "P.Berl.Dem."];

  mads[435] = ["P.Berl.Schmidt", "P.Berl.Schmidt"];

  mads[436] = ["P.Berl.Spieg.", "P.Berl.Spieg."];

  mads[437] = ["P.Bodm.", "P.Bodm."];

  mads[438] = ["P.Brit.Mus.", "P.Brit.Mus."];

  mads[439] = ["P.Brit.Mus.Reich.", "P.Brit.Mus.Reich."];

  mads[440] = ["P.Bürgsch.", "P.Bürgsch."];

  mads[441] = ["P.CLT", "P.CLT"];

  mads[442] = ["P.Carlsb.", "P.Carlsb."];

  mads[443] = ["P.CattleDocs.", "P.CattleDocs."];

  mads[444] = ["P.Chept.", "P.Chept."];

  mads[445] = ["P.Chic.", "P.Chic."];

  mads[446] = ["P.Choach.Survey", "P.Choach.Survey"];

  mads[447] = ["P.Chrest.Nouvelle", "P.Chrest.Nouvelle"];

  mads[448] = ["P.Chrest.Revillout", "P.Chrest.Revillout"];

  mads[449] = ["P.Chronik", "P.Chronik"];

  mads[450] = ["P.Corpus Revillout", "P.Corpus Revillout"];

  mads[451] = ["P.Demotica", "P.Demotica"];

  mads[452] = ["P.Edg.", "P.Edg."];

  mads[453] = ["P.Egerton", "P.Egerton"];

  mads[454] = ["P.Egger", "P.Egger"];

  mads[455] = ["P.Ehevertr.", "P.Ehevertr."];

  mads[456] = ["P.Eleph.Dem.", "P.Eleph.Dem."];

  mads[457] = ["P.Enteux.", "P.Enteux."];

  mads[458] = ["P.Erbstreit", "P.Erbstreit"];

  mads[459] = ["P.Fam.Theb.", "P.Fam.Theb."];

  mads[460] = ["P.Fay.Copt.", "P.Fay.Copt."];

  mads[461] = ["P.Gebelen", "P.Gebelen"];

  mads[462] = ["P.Giss.Lit.", "P.Giss.Lit."];

  mads[463] = ["P.Hausw.", "P.Hausw."];

  mads[464] = ["P.Hercul.", "P.Hercul."];

  mads[465] = ["P.Hermias", "P.Hermias"];

  mads[466] = ["P.Hermitage Copt.", "P.Hermitage Copt."];

  mads[467] = ["P.Holm.", "P.Holm."];

  mads[468] = ["P.Hou", "P.Hou"];

  mads[469] = ["P.KRU", "P.KRU"];

  mads[470] = ["P.LandLeases", "P.LandLeases"];

  mads[471] = ["P.Land Leases", "P.LandLeases"];

  mads[472] = ["P.Leid.Dem.", "P.Leid.Dem."];

  mads[473] = ["P.Libbey", "P.Libbey"];

  mads[474] = ["P.LilleDem.", "P.LilleDem."];

  mads[475] = ["P.Lille Dem.", "P.LilleDem."];

  mads[476] = ["P.Loeb", "P.Loeb"];

  mads[477] = ["P.Lond.Lit.", "P.Lond.Lit."];

  mads[478] = ["P.Lonsdorfer", "P.Lonsdorfer"];

  mads[479] = ["P.Lugd.Bat.", "P.Lugd.Bat."];

  mads[480] = ["P.L.Bat.", "P.Lugd.Bat."];

  mads[481] = ["P.Mallawi", "P.Mallawi"];

  mads[482] = ["P.Marini", "P.Marini"];

  mads[483] = ["P.Meerman.", "P.Meerman."];

  mads[484] = ["P.Mich. Copt.", "P.Mich. Copt."];

  mads[485] = ["P.Mich. Nims", "P.Mich. Nims"];

  mads[486] = ["P.Mon.Epiph.", "P.Mon.Epiph."];

  mads[487] = ["P.MorganLib.", "P.MorganLib."];

  mads[488] = ["P.MoscowCopt.", "P.MoscowCopt."];

  mads[489] = ["P.Oxy.Astr.", "P.Oxy.Astr."];

  mads[490] = ["P.Petersb.", "P.Petersb."];

  mads[491] = ["P.Pher.", "P.Pher."];

  mads[492] = ["P.PisaLit.", "P.PisaLit."];

  mads[493] = ["P.Pisa Lit.", "P.PisaLit."];

  mads[494] = ["P.Pisentius", "P.Pisentius"];

  mads[495] = ["P.Prag.Satzung", "P.Prag.Satzung"];

  mads[496] = ["P.QasrIbrim", "P.QasrIbrim"];

  mads[497] = ["P.Qasr Ibrim", "P.QasrIbrim"];

  mads[498] = ["P.QuelquesTextes", "P.QuelquesTextes"];

  mads[499] = ["P.Quelques Textes", "P.QuelquesTextes"];

  mads[500] = ["P.Rain.Unterricht", "P.Rain.Unterricht"];

  mads[501] = ["P.Rain.UnterrichtKopt.", "P.Rain.UnterrichtKopt."];

  mads[502] = ["P.Rain.Unterricht Kopt.", "P.Rain.UnterrichtKopt."];

  mads[503] = ["P.Revillout Copt.", "P.Revillout Copt."];

  mads[504] = ["P.Ryl.Copt.", "P.Ryl.Copt."];

  mads[505] = ["P.Ryl.Dem.", "P.Ryl.Dem."];

  mads[506] = ["P.Schenkung.", "P.Schenkung."];

  mads[507] = ["P.Schreibertrad.", "P.Schreibertrad."];

  mads[508] = ["P.Schub.", "P.Schub."];

  mads[509] = ["P.Schutzbriefe", "P.Schutzbriefe"];

  mads[510] = ["P.Siegesfeier", "P.Siegesfeier"];

  mads[511] = ["P.Siut", "P.Siut"];

  mads[512] = ["P.SlaveryDem.", "P.SlaveryDem."];

  mads[513] = ["P.Slavery.Dem.", "P.SlaveryDem."];

  mads[514] = ["P.Stras.Dem.", "P.Stras.Dem."];

  mads[515] = ["P.Strasb.Dem.", "P.Stras.Dem."];

  mads[516] = ["P.TestiBotti", "P.TestiBotti"];

  mads[517] = ["P.Testi Botti", "P.TestiBotti"];

  mads[518] = ["P.Thead.", "P.Thead."];

  mads[519] = ["P.Tor.Botti", "P.Tor.Botti"];

  mads[520] = ["P.Tsenhor", "P.Tsenhor"];

  mads[521] = ["P.Ups.8", "P.Ups.8"];

  mads[522] = ["P.Ups.", "P.Ups.8"];

  mads[523] = ["P.Verpfründung.", "P.Verpfründung."];

  mads[524] = ["P.YaleCopt.", "P.YaleCopt."];

  mads[525] = ["P.Yale Copt.", "P.YaleCopt."];

  mads[526] = ["P.Zen.Dem.", "P.Zen.Dem."];

  mads[527] = ["PSI Il.", "PSI Il."];

  mads[528] = ["PSI.Il.", "PSI Il."];

  mads[529] = ["PSI Od.", "PSI Od."];

  mads[530] = ["PSI.Od.", "PSI Od."];

  mads[531] = ["SB Kopt.", "SB Kopt."];

  mads[532] = ["T.Dacia", "T.Dacia"];

  mads[533] = ["T.Jucundus", "T.Jucundus"];

  mads[534] = ["T.Pizzaras", "T.Pizzaras"];

  mads[535] = ["T.Sulpicii", "T.Sulpicii"];

  mads[536] = ["T.Varie", "T.Varie"];

  mads[537] = ["T.Vindon.", "T.Vindon."];

InstallAC=function(a,b,c,e,f,h,g,l){
    HTML_FORM=a;textQuery=b;Ta=c;
    if(!e)e="search";
    Pa=e;

    var o="zh|zh-CN|zh-TW|ja|ko|",y="iw|ar|fa|ur|";
    if(!f||f.length<1)f="en";
    u=Ea(f);
    if(y.indexOf(u+"|")!=-1){
        G="right";V="left"
    }
    if(u=="zh-TW"||u=="zh-CN"||u=="ja"){
        ya=true
    }
    if(o.indexOf(u+"|")==-1){
        J=false;
        aa=true;
        ha=false;
        $=false
    }else{
        J=false;
        aa=true;
        if(u.indexOf("zh")==0)ha=false;
        $=true
    }
    if(!h)h=false; // wtf?
    na=h;
    if(!g)g="query";
    x=g;Va=l;db()
};

function Fa(){
    ua=true;
    textQuery.blur();
    setTimeout("sfi();",10)
}

function nb(){
    if(document.createEventObject){
        var a=document.createEventObject();
        a.ctrlKey=true;
        a.keyCode=70;
        document.fireEvent("onkeydown",a)
    }
}

function jb(a){
    if(!a&&window.event)a=window.event;
    if(a)va=a.keyCode;
    if(a&&a.keyCode==8){
        if(J&&textQuery.createTextRange&&a.srcElement==textQuery&&O(textQuery)==0&&P(textQuery)==0){
            Wa(textQuery);
            a.cancelBubble=true;
            a.returnValue=false;
            return false
        }
    }
}

function captureEnter(a){
    window.alert('captureEnter');
    if(!a&&window.event)a=window.event;
    if (!a) return true;
    if(a)va=a.keyCode;
    switch(a.keyCode){
    case 3:
    case 8:
    case 10:
    case 12:
    case 17:
        gb.apply(this);
        a.cancelBubble=true;
        a.returnValue=false;
        return false;
    default:
         break;
    }
}

function lb(){
    if(x=="url"){
        Ra()
    }
    ca()
}

function ca(){
    if(i && i.style){
        i.style.left=Ia(textQuery,"offsetLeft")+"px";
        i.style.top=Ia(textQuery,"offsetTop")+textQuery.offsetHeight-1+"px";
        i.style.width=Ha()+"px";
        if(m){
            m.style.left=i.style.left;
            m.style.top=i.style.top;
            m.style.width=i.style.width;
            m.style.height=i.style.height;
        }
    }
}

function Ha(){
    if(navigator&&navigator.userAgent.toLowerCase().indexOf("msie")==-1){
        return textQuery.offsetWidth-T*2
    }else{
        return textQuery.offsetWidth
    }
}

function db(){
    if(Ja()){
        XML_HHTP_FOUND=true
    }else{
        XML_HHTP_FOUND=false
    }
    if($a)E="complete";
    else E="/ddbdp-nav/complete/"+Pa;
    suggestURI=E+"?hl="+u+"&client=suggest";
    if(!XML_HHTP_FOUND){
        ta("qu","",0,E,null,null)
    }
    HTML_FORM.onsubmit=new Function("la();document.getElementById('series').value = textQuery.value;return false;");
    textQuery.autocomplete="off";
    textQuery.onblur=fb;
    textQuery.onfocus=kb;
    //alert(textQuery.innerHTML);
    textQuery.onchange=new Function("document.getElementById('series').value = textQuery.value;");
    if(textQuery.createTextRange){
        textQuery.onkeydown=new Function("return okdh(event); ");
        textQuery.onkeyup=new Function("return okuh(event); ")
    }else{
        textQuery.onkeypress=okdh;
        textQuery.onkeyup=okuh;
    }
    k=textQuery.value;
    qa=k;
    i=document.createElement("DIV");
    i.id="completeDiv";
    T=1;
    ia=1;
    i.style.borderRight="black "+T+"px solid";
    i.style.borderLeft="black "+T+"px solid";
    i.style.borderTop="black "+ia+"px solid";
    i.style.borderBottom="black "+ia+"px solid";
    i.style.zIndex="2";
    i.style.paddingRight="0";
    i.style.paddingLeft="0";
    i.style.paddingTop="0";
    i.style.paddingBottom="0";
    i.style.visibility="hidden";
    i.style.position="absolute";
    i.style.backgroundColor="white";
    m=document.createElement("IFRAME");
    m.id="completeIFrame";
    m.style.zIndex="1";
    m.style.position="absolute";
    if(window.opera&&(!window.opera.version||window.opera.version()<="8.54")) m.style.display="none";
    else m.style.display="block";
    m.style.visibility="hidden";
    m.style.borderRightWidth="0";
    m.style.borderLeftWidth="0";
    m.style.borderTopWidth="0";
    m.style.borderBottomWidth="0";
    ca();
    document.body.appendChild(i);
    document.body.appendChild(m);
    Aa("",[],[]);
    cb(i);
    var a=document.createElement("DIV");
    a.style.visibility="hidden";
    a.style.position="absolute";
    a.style.left="0";
    a.style.top="-10000";
    a.style.width="0";
    a.style.height="0";
    var b=document.createElement("IFRAME");
    b.completeDiv=i;
    b.name="completionFrame";
    b.id="completionFrame";
    if(!XML_HHTP_FOUND){
        b.src=suggestURI;
    }
    if(textQuery.createTextRange){
        b.onkeydown = captureEnter;
    }else{
        b.onkeypress=captureEnter;
    }
    a.appendChild(b);
    document.body.appendChild(a);
    if(frames&&frames["completionFrame"]&&frames["completionFrame"].frameElement)K=frames["completionFrame"].frameElement;
    else K=document.getElementById("completionFrame");
    if(x=="url"){
        Ra();
        ca();
    }
    window.onresize=lb;
    document.onkeydown=jb;
    nb();
    if($){
        setTimeout("idkc()",10);
        if(textQuery.attachEvent){
            textQuery.attachEvent("onpropertychange",ab);
        }
    }
    p=document.createElement("INPUT");
    p.type="hidden";
    p.name="aq";
    p.value=null;
    p.disabled=true;
    HTML_FORM.appendChild(p);
    t=document.createElement("INPUT");
    t.type="hidden";
    t.name="oq";
    t.value=null;
    t.disabled=true;
    HTML_FORM.appendChild(t);
}

function kb(a){
    W=true;
}

function fb(a){
    W=false;
    if(!a&&window.event)a=window.event;
    if(!ua){
        hideComplete();
        if(va==9){
            pb();
            va=-1;
        }
    }
    ua=false
}

function ga(a){
    if(a==38||a==63232){
        return true
    }
    return false;
}

function fa(a){
    if(a==40||a==63233){
        return true
    }
    return false
}
okdh=function(a){
    if(!(ga(a.keyCode)||fa(a.keyCode))){
        return true;
    }
    R++;
    if(R%3==1)za(a);
    return false
    };
okuh=function(a){
    if(!(ya&&(ga(a.keyCode)||fa(a.keyCode)))&&R==0){
        za(a);
    }
    R=0;
    return false;
    };
function za(a){
    if(!ka){
        ka=true
    }
    j=a.keyCode;
    Z=textQuery.value;
    Oa();
}

function pb(){
    Ta.focus()
}
sfi=function(){
    textQuery.focus();
    };
function rb(a){
    for(var b=0,c="",e="\n\r";b<a.length;b++)
    if(e.indexOf(a.charAt(b))==-1)c+=a.charAt(b);
    else c+=" ";
    return c;
}
function Ga(a,b){
    var c=a.getElementsByTagName(Ya);
    if(c){
        for(var e=0;e<c.length;++e){
            if(c[e].className==b){
                var f=c[e].innerHTML;
                if(f=="&nbsp;")return"";
                else{
                    var h=rb(f);
                    return h;
                }
            }
        }
    }
    else{
        return"";
    }
}

function L(a){
    if(!a)return null;
    return Ga(a,"cAutoComplete");
}

function ma(a){
    if(!a)return null;
    return Ga(a,"dAutoComplete")
}

function hideComplete(){
    document.getElementById("completeDiv").style.visibility="hidden";
    document.getElementById("completeIFrame").style.visibility="hidden";
}

function showComplete(){
    document.getElementById("completeDiv").style.visibility="visible";
    document.getElementById("completeIFrame").style.visibility="visible";
    ca();
}

function Aa(a,b,c){
    Ca[a]=new Array(b,c);
}

Suggest_apply=function(a,b,c,e){
    if(c.length==0||c[0]<2)return;
    var f=[],h=[],g=c[0],l=Math.floor((c.length-1)/g);
    for(var o=0;o<l;o++){
        f.push(c[o*g+1]);
        h.push(c[o*g+2])
    }
    var y=e?e:[];
    sendRPCDone(a,b,f,h,y)
};
sendRPCDone=function(elementNode,query,resultArray,labelArray,f){
    if(D>0)D--;
    var h=(new Date).getTime();
    if(!elementNode)elementNode=K;
    Aa(query,resultArray,labelArray);
    if(query==k){
        if(C){
            clearTimeout(C);
            C=null;
        }
        ja=query;
    }
    var g=elementNode.completeDiv;
    g.completeStrings=resultArray;
    g.displayStrings=labelArray;
    g.prefixStrings=f;
    Za(g,g.completeStrings,g.displayStrings);
    showSuggestions(g,L);
    if(pa>0){
        g.height=16*pa+4;
        m.height=g.height-4;
    }else{
        hideComplete();
    }
};
sendCached=function(elementNode,query,f){
    if(D>0)D--;
    var h=(new Date).getTime();
    if(!elementNode)elementNode=K;
    resultArray = new Array();
    labelArray = new Array();
    var qprefix = query.toLowerCase();
    var getSuggest = (qprefix.length > 2 || (qprefix != 'p.' && qprefix != 't.' ));
    if (getSuggest){
	    for(var i=0;i<window.mads.length;i++){
	        var mad = window.mads[i][0].toLowerCase();
	        var match = false;
	        match = (qprefix != 'p' && qprefix != 't' && mad.indexOf(qprefix) == 0);
	        if (!match) match = (mad.indexOf('p.' + qprefix) == 0 || mad.indexOf('t.' + qprefix)== 0);
	        if (match){
	            resultArray[resultArray.length] = window.mads[i][0];
	            labelArray[labelArray.length] = "";
	        }
	    }
    }
    else return;
    Aa(query,resultArray,labelArray);
    if(query==k){
        if(C){
            clearTimeout(C);
            C=null;
        }
        ja=query;
    }
    var g=elementNode.completeDiv;
    
    g.completeStrings=resultArray;
    g.displayStrings=labelArray;
    g.prefixStrings=f;
    Za(g,g.completeStrings,g.displayStrings);
    showSuggestions(g,L);
    if(pa>0){
        g.height=16*pa+4;
        m.height=g.height-4;
    }else{
        hideComplete();
    }
};
hcd=function(){
    hideComplete();
    C=null;
};
function Oa(){ // character handler called on key down and up
    if(j==40||j==38)Fa();
    var a=P(textQuery),b=O(textQuery),c=textQuery.value;
    if(J&&j!=0){
        if(a>0&&b!=-1)c=c.substring(0,b);
        if(j==13||j==3){
            var e=textQuery;
            if(e.createTextRange){
                var f=e.createTextRange();
                f.moveStart("character",e.value.length);
                f.select();
            }else if(e.setSelectionRange){
                e.setSelectionRange(e.value.length,e.value.length);
            }
        }else{
            if(textQuery.value!=c)N(c);
        }
    }
    if(j!=9&&j!=13&&!(j>=16&&j<=20)&&j!=27&&!(j>=33&&j<=38)&&j!=40&&j!=44&&!(j>=112&&j<=123)){
        k=c;
        if(j!=39)oa=c;
    }
    if(Xa(j)&&j!=0&&ja==k){
        showSuggestions(i,L);
    }
    if(j==13){
        var d = document.getElementById('series');
        if (d.value.search(/\w/) != -1) d.onchange();
    }
    if(ja!=k&&!C)C=setTimeout("hcd()",500);
}
function la(){
    return eb(x);
}
function eb(a){
    da=true;
    if(!XML_HHTP_FOUND){
        ta("qu","",0,E,null,null);
    }
    hideComplete();
    if(a=="url"){
        var b="";
        if(r!=-1&&n)b=L(n);
        if(b=="")b=textQuery.value;
        if(w=="")document.title=b;
        else document.title=w;
        var c="window.frames['"+Va+"'].location = \""+b+'";';
        setTimeout(c,10);
        return false
    }else if(a=="query"){
        p.disabled=(t.disabled=true);
        if(oa!=textQuery.value){
            p.value="t";
            p.disabled=false;
            t.value=oa;
            t.disabled=false;
        }else if(S){
            p.value=S;
            p.disabled=false
        }else if(I>=3||D>=10){
            p.value="o";
            p.disabled=false;
        }
        S=null;
        return true;
    }
}
newwin=function(){
    window.open(textQuery.value);
    hideComplete();
    return false;
};
idkc=function(a){
    if($){
        if(W){
            Ua();
        }
        var b=textQuery.value;
        if(b!=Z){
            j=0;
            Oa();
        }
        Z=b;
        setTimeout("idkc()",10);
    }
};
function Ea(a){
    if(encodeURIComponent)return encodeURIComponent(a);
    if(escape)return escape(a);
}
function bb(a){
    var b=100;
    for(var c=1;c<=(a-2)/2;c++){
        b=b*2;
    }
    b=b+50;
    return b;
}
idfn=function(){
    if(I>=3)return false;
    if(qa!=k){
        if(!da){
            var a=Ea(k),b=Ca[k];
            if(b){
                Qa=-1;
                sendCached(K,k,K.completeDiv.prefixStrings);
                //sendRPCDone(K,k,b[0],b[1],K.completeDiv.prefixStrings);
            }
            else{
                D++;
                Qa=(new Date).getTime();
                sendCached(frameElement,a,new Array());
                if(XML_HHTP_FOUND && false){
                    ob(a);
                }else{
                    if(false){
                    ta("qu",a,null,E,null,null);
                    frames["completionFrame"].document.location.reload(true);
                    }
                }
            }
            textQuery.focus();
        }
        da=false;
    }
    qa=k;setTimeout("idfn()",bb(D));
    return true;
};
setTimeout("idfn()",10);
var gb=function(){
    textQuery.blur();
    N(L(this));
    w=ma(this);
    da=true;
    if(la()){
        var series = document.getElementById('series');
        series.value = document.getElementById('q').value;
        loadItems('volume');
        resetItems('document');
        //NOSUBMIT
        //HTML_FORM.submit();
    }
};

var hb=function(){
    if(window.event){
        var a=window.event.x,b=window.event.y;
        if(a==Ma&&b==Na){
            return;
        }
        Ma=a;
        Na=b;
    }
    if(n)s(n,"aAutoComplete");
    s(this,"bAutoComplete");
    n=this;
    for(var c=0;c<A;c++){
        if(U[c]==n){r=c;break}
    }
};

var ib=function(){
    s(this,"aAutoComplete");
};
function sa(a){
    S="t";
    k=M;
    N(M);
    w=M;
    if(!U||A<=0)return;
    showComplete();
    if(n)s(n,"aAutoComplete");
    if(a==A||a==-1){
        r=-1;
        textQuery.focus();
        return;
    }else if(a>A){
        a=0;
    }else if(a<-1){
        a=A-1;
    }
    r=a;
    n=U.item(a);
    s(n,"bAutoComplete");
    k=M;
    w=ma(n);
    N(L(n));
}
function Xa(a){
    if(fa(a)){
        sa(r+1);
        return false;
    }else if(ga(a)){
        sa(r-1);
        return false;
    }else if(a==13||a==3){
        return false;
    }
    return true;
}
function showSuggestions(a,b){
    var c=textQuery,e=false;
    r=-1;
    var f=a.getElementsByTagName(mb),h=f.length;
    A=h;
    U=f;
    pa=h;
    M=k;
    if(k==""||h==0){
        hideComplete();
    }else{
        showComplete();
    }
    var g="";
    if(k.length>0){
        var l,o;
        iterateH:
        for(var l=0;l<h;l++){
            iteratePStrings:
            for(o=0;o<a.prefixStrings.length;o++){
                var y=a.prefixStrings[o]+k;
                if(ha||!aa&&b(f.item(l)).toUpperCase().indexOf(y.toUpperCase())==0||aa&&l==0&&b(f.item(l)).toUpperCase()==y.toUpperCase()){
                    g=a.prefixStrings[o];
                    e=true;
                    break iterateH;
                }
            }
        }
    }
    if(e)r=l;
    for(var l=0;l<h;l++)s(f.item(l),"aAutoComplete");
    if(e){
        n=f.item(r);
        w=ma(n);
    }else{
        w=k;
        r=-1;
        n=null;
    }
    var wa=false;
    switch(j){
        case 8:case 33:case 34:case 35:case 35:case 36:case 37:case 39:case 45:case 46:
            wa=true;
            break;
        default:break;
    }
    if(!wa&&n){
        var ea=k;s(n,"bAutoComplete");
        var Q;
        if(e)Q=b(n).substr(a.prefixStrings[o].length);
        else Q=ea;
        if(Q!=c.value){
            if(c.value!=k)return;
            if(J){
                if(c.createTextRange||c.setSelectionRange)N(Q);
                if(c.createTextRange){
                    var xa=c.createTextRange();
                    xa.moveStart("character",ea.length);
                    xa.select();
                }else if(c.setSelectionRange){
                    c.setSelectionRange(ea.length,c.value.length);
                }
            }
        }
    }else{
        r=-1;
        w=k;
    }
}
    
function Ia(a,b){
    var c=0;
    while(a){
        c+=a[b];
        a=a.offsetParent;
    }
    return c;
}

function ta(a,b,c,e,f,h){
    var g=a+"="+b+(c?"; expires="+c.toGMTString():"")+(e?"; path="+e:"")+(f?"; domain="+f:"")+(h?"; secure":"");
    document.cookie=g;
}
function Ra(){
    var a=document.body.scrollWidth-220;
    a=0.73*a;
    textQuery.size=Math.floor(a/6.18);
}
function P(a){
    var b=-1;
    if(a.createTextRange){
        var c=document.selection.createRange().duplicate();
        b=c.text.length;
    }else if(a.setSelectionRange){
        b=a.selectionEnd-a.selectionStart;
    }
    return b;
}
function O(a){
    var b=0;
    if(a.createTextRange){
        var c=document.selection.createRange().duplicate();
        c.moveEnd("textedit",1);
        b=a.value.length-c.text.length;
    }else if(a.setSelectionRange){
        b=a.selectionStart;
    }else{
        b=-1;
    }
    return b;
}
function Wa(a){
    if(a.createTextRange){
        var b=a.createTextRange();
        b.moveStart("character",a.value.length);
        b.select();
    }else if(a.setSelectionRange){
        a.setSelectionRange(a.value.length,a.value.length);
    }
}
function s(a,b){
    Ba();
    a.className=b;
    if(Da){
        return;
    }
    switch(b.charAt(0)){
        case "m":
            a.style.fontSize="13px";
            a.style.fontFamily="arial,sans-serif";
            a.style.wordWrap="break-word";
            break;
        case "l":
            a.style.display="block";
            a.style.paddingLeft="3";
            a.style.paddingRight="3";
            a.style.height="16px";
            a.style.overflow="hidden";
            break;
        case "a":
            a.style.backgroundColor="white";
            a.style.color="black";
            if(a.displaySpan){
                a.displaySpan.style.color="green";
            }
            break;
        case "b":
            a.style.backgroundColor="#3366cc";
            a.style.color="white";
            if(a.displaySpan){
                a.displaySpan.style.color="white";
            }
            break;
        case "c":
            a.style.width=v+"%";
            a.style.cssFloat=G;
            a.style.whiteSpace="nowrap";
            a.style.overflow="hidden";
            a.style.textOverflow="ellipsis";
            break;
        case "d":
            a.style.cssFloat=V;
            a.style.width=100-v+"%";
            if(x=="query"){
                a.style.fontSize="10px";
                a.style.textAlign=V;
                a.style.color="green";
                a.style.paddingTop="3px";
            }else{
                a.style.color="#696969";
            }
            break;
    }
}

function Ba(){
    v=65;
    if(x=="query"){
        var a=110,b=Ha(),c=(b-a)/b*100;
        v=c;
    }else{
        v=65;
    }
    if(na){v=99.99}
}
function cb(a){
    Ba();
    var b="font-size: 13px; font-family: arial,sans-serif; word-wrap:break-word;",
    c="display: block; padding-left: 3; padding-right: 3; height: 16px; overflow: hidden;",
    e="background-color: white;",f="background-color: #3366cc; color: white ! important;",
    h="display: block; margin-"+G+": 0%; width: "+v+"%; float: "+G+";",g="display: block; margin-"+G+": "+v+"%;";
    if(x=="query"){
        g+="font-size: 10px; text-align: "+V+"; color: green; padding-top: 3px;";
    }else{
        g+="color: #696969;"
    }
    z(".mAutoComplete",b);
    z(".lAutoComplete",c);
    z(".aAutoComplete *",e);
    z(".bAutoComplete *",f);
    z(".cAutoComplete",h);
    z(".dAutoComplete",g);
    s(a,"mAutoComplete");
}
function Za(a,b,c){
    while(a.childNodes.length>0)a.removeChild(a.childNodes[0]);
    for(var e=0;e<b.length;++e){
        var f=document.createElement("DIV");
        s(f,"aAutoComplete");
        f.onmousedown=gb;
        f.onmousemove=hb;
        f.onmouseout=ib;
        f.onkeydown=captureEnter;
        var h=document.createElement("SPAN");
        s(h,"lAutoComplete");
        if(u.substring(0,2)=="zh")h.style.height=textQuery.offsetHeight-4;
        else h.style.height=textQuery.offsetHeight-6;
        var g=document.createElement("SPAN");
        g.innerHTML=b[e];
        var l=document.createElement("SPAN");
        s(l,"dAutoComplete");
        s(g,"cAutoComplete");
        f.displaySpan=l;
        if(!na)l.innerHTML=c[e];
        h.appendChild(g);
        h.appendChild(l);
        f.appendChild(h);
        a.appendChild(f);
    }
}
function z(a,b){
    if(Da){
        var c=document.styleSheets[0];
        if(c.addRule){
            c.addRule(a,b);
        }else if(c.insertRule){
            c.insertRule(a+" { "+b+" }",c.cssRules.length);
        }
    }
}
function Ja(){
    var a=null;
    try{
        a=new ActiveXObject("Msxml2.XMLHTTP");
    }catch(b){
        try{
            a=new ActiveXObject("Microsoft.XMLHTTP");
        }catch(c){
            a=null;
        }
    }
    if(!a&&typeof XMLHttpRequest!="undefined"){
        a=new XMLHttpRequest;
    }
    return a;
}
function ob(a){
    if(q&&q.readyState!=0&&q.readyState!=4){
        q.abort();
    }
    q=Ja();
    if(q){
        var path = suggestURI+"&js=true&qu="+a;
        //alert(path);
        q.open("GET",suggestURI+"&js=true&qu="+a,true);
        q.onreadystatechange=function(){
            if(q.readyState==4&&q.responseText){
                switch(q.status){
                    case 403:
                        I=1000;
                        break;
                    case 302:case 500:case 502:case 503:
                        I++;
                        break;
                    case 200:
                        var b=q.responseText;
                        if(b.charAt(0)!="<"&&(b.indexOf("sendRPCDone")!=-1||b.indexOf("Suggest_apply")!=-1))eval(b);
                        else D--;
                    default:
                        I=0;
                        break;
                }
            }
        };
        q.send(null);
    }
}
function N(a){
    textQuery.value=a;
    Z=a;
}
function ab(a){
    if(!a&&window.event)a=window.event;
    if(!ka&&W&&a.propertyName=="value"){
        if(sb()){
            Ua();
            setTimeout("ba("+X+", "+Y+");",30);
        }
    }
}

function sb(){
    var a=textQuery.value,b=O(textQuery),c=P(textQuery);
    return b==X&&c==Y&&a==La;
}
function Ua(){
    La=textQuery.value;
    X=O(textQuery);
    Y=P(textQuery);
}
ba=function(a,b){
    if(a==X&&b==Y){
        qb();
    }
};
function qb(){Fa();sa(r+1)};
