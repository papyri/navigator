var x="",$a=false,qa="",da=false,k="",M="",j="",r=-1,n=null,
A=-1,U=null,pa=5,w="",mb="div",Ya="span",HTML_FORM=null,d=null,i=null,m=null,
Ta=null,Va=null,J=null,u=null,ua=false,Ca={},T=1,ia=1,ha=false,
aa=false,va=-1,Qa=(new Date).getTime(),XML_HHTP_FOUND=false,q=null,suggestURI=null,E=null,
K=null,Z=null,$=false,Da=false,v=60,Pa=null,na=null,D=0,I=0,La=null,X=null,
Y=null,ka=false,W=false,ja="",C=null,S=null,p=null,t=null,oa=null,
Ma=-1,Na=-1,G="left",V="right",R=0,ya=false;

   var mads = [
      
  ["BGU", "BGU"],

  ["C.Epist.Lat.", "C.Epist.Lat."],

  ["CEL", "C.Epist.Lat."],

  ["C.Étiq.Mom.", "C.Étiq.Mom."],

  ["C.Illum.Pap.", "C.Illum.Pap."],

  ["C.Jud.Syr.Eg.", "C.Jud.Syr.Eg."],

  ["C.Pap.Gr.", "C.Pap.Gr."],

  ["CPGr", "C.Pap.Gr."],

  ["CP.Gr", "C.Pap.Gr."],

  ["Chrest.Mitt.", "Chrest.Mitt."],

  ["M.Chr.", "Chrest.Mitt."],

  ["Chrest.Wilck.", "Chrest.Wilck."],

  ["CPR", "CPR"],

  ["O.Amst.", "O.Amst."],

  ["O.Bodl.", ""],

  ["O.Ashm.Shelt.", "O.Ashm.Shelt."],

  ["O.Ashm.Shelton", "O.Ashm.Shelt."],

  ["O.Berl.", "O.Berl."],

  ["O.Bodl.", "O.Bodl."],

  ["O.Brux.", "O.Brux."],

  ["O.Bu Njem", "O.Bu Njem"],

  ["O.Buch.", "O.Buch."],

  ["O.Cair.", "O.Cair."],

  ["O.Cair. GPW", "O.Cair."],

  ["O.Camb.", "O.Camb."],

  ["O.Bodl.", "O.Camb."],

  ["O.Claud.", "O.Claud."],

  ["O.Deiss.", "O.Deiss."],

  ["O.Douch", "O.Douch"],

  ["O.Edfou", "O.Edfou"],

  ["O.Elkab", "O.Elkab"],

  ["O.Erem.", "O.Erem."],

  ["O.Fay.", "O.Fay."],

  ["O.Florida", "O.Florida"],

  ["O.Joach.", "O.Joach."],

  ["O.Joachim", "O.Joach."],

  ["O.dem.Joach.", "O.Joach."],

  ["O.Leid.", "O.Leid."],

  ["O.Lund.", "O.Lund."],

  ["O.Masada", "O.Masada"],

  ["O.Medin.Madi", "O.Medin.Madi"],

  ["P.Medin.Madi", "O.Medin.Madi"],

  ["O.Mich.", "O.Mich."],

  ["O.Minor", "O.Minor"],

  ["O.Bodl.", "O.Minor"],

  ["O.Narm.", "O.Narm."],

  ["O.Oasis", "O.Oasis"],

  ["O.Chams el-Din", "O.Oasis"],

  ["O.Ont.Mus.", "O.Ont.Mus."],

  ["O.ROM", "O.Ont.Mus."],

  ["O.Oslo", "O.Oslo"],

  ["O.Paris", "O.Paris"],

  ["O.Par.", "O.Paris"],

  ["O.Petr.", "O.Petr."],

  ["O.Bodl.", "O.Petr."],

  ["O.Sarga", "O.Sarga"],

  ["P.Sarga", "O.Sarga"],

  ["O.Stras.", "O.Stras."],

  ["O.Strasb.", "O.Stras."],

  ["O.Tebt.Pad.", "O.Tebt.Pad."],

  ["O.Tebt.", "O.Tebt."],

  ["O.Theb.", "O.Theb."],

  ["O.Vleem.", "O.Vleem."],

  ["O.WadiHamm.", "O.WadiHamm."],

  ["O.Waqfa", "O.Waqfa"],

  ["O.Wilb.", "O.Wilb."],

  ["O.Wilck.", "O.Wilck."],

  ["WO", "O.Wilck."],

  ["P.Aberd.", "P.Aberd."],

  ["P.Abinn.", "P.Abinn."],

  ["P.Achm.", "P.Achm."],

  ["P.Adl.", "P.Adl."],

  ["P.Adler", "P.Adl."],

  ["P.Alex.Giss.", "P.Alex.Giss."],

  ["P.Alex.", "P.Alex."],

  ["P.Amh.", "P.Amh."],

  ["P.Amst.", "P.Amst."],

  ["P.Anag.", "P.Anag."],

  ["P.Ant.", "P.Ant."],

  ["P.Ashm.", "P.Ashm."],

  ["P.Athen.", "P.Athen."],

  ["P.Athen.Xyla", "P.Athen.Xyla"],

  ["P.Sta.Xyla", "P.Athen.Xyla"],

  ["P.Aust.Herr.", "P.Aust.Herr."],

  ["P.Trophitis", "P.Aust.Herr."],

  ["P.Yadin", "P.Yadin"],

  ["P.Babatha", "P.Yadin"],

  ["P.Bacch.", "P.Bacch."],

  ["P.Bad.", "P.Bad."],

  ["VBP", "P.Bad."],

  ["P.Bal.", "P.Bal."],

  ["P.Bas.", "P.Bas."],

  ["P.Batav.", "P.Batav."],

  ["P.Berl.Bibl.", "P.Berl.Bibl."],

  ["P.Berl.Bork.", "P.Berl.Bork."],

  ["P.Berl.Brash.", "P.Berl.Brash."],

  ["P.Berl.Frisk", "P.Berl.Frisk"],

  ["P.Berl.Leihg.", "P.Berl.Leihg."],

  ["P.Berl.Möller", "P.Berl.Möller"],

  ["P.Berl.Sarisch.", "P.Berl.Sarisch."],

  ["P.Berl.Thun.", "P.Berl.Thun."],

  ["P.Berl.Zill.", "P.Berl.Zill."],

  ["P.Bon.", "P.Bon."],

  ["P.Bour.", "P.Bour."],

  ["P.Brem.", "P.Brem."],

  ["P.Brookl.", "P.Brookl."],

  ["P.Brook.", "P.Brookl."],

  ["P.Brux.", "P.Brux."],

  ["P.Bub.", "P.Bub."],

  ["P.Cair.Goodsp.", "P.Cair.Goodsp."],

  ["P.Cair. Goodspeed", "P.Cair.Goodsp."],

  ["P.Cair.Isid.", "P.Cair.Isid."],

  ["P.Cair.Masp.", "P.Cair.Masp."],

  ["P.Cair.Mich.", "P.Cair.Mich."],

  ["P.Cair.Preis.", "P.Cair.Preis."],

  ["P.Cair.Zen.", "P.Cair.Zen."],

  ["P.Charite", "P.Charite"],

  ["P.Col.", "P.Col."],

  ["P.Col.Teeter", "P.Col.Teeter"],

  ["P.Coll.Youtie", "P.Coll.Youtie"],

  ["P.Corn.", "P.Corn."],

  ["P.Customs", "P.Customs"],

  ["P.David", "P.David"],

  ["P.Diog.", "P.Diog."],

  ["P.Dion.", "P.Dion."],

  ["P.Dubl.", "P.Dubl."],

  ["P.Dub.", "P.Dubl."],

  ["P.Dura", "P.Dura"],

  ["P.Edfou", "P.Edfou"],

  ["P.Eleph.", "P.Eleph."],

  ["P.Enteux.", "P.Enteux."],

  ["P.Erasm.", "P.Erasm."],

  ["P.Erl.", "P.Erl."],

  ["P.Fam.Tebt.", "P.Fam.Tebt."],

  ["P.Fay.", "P.Fay."],

  ["P.Flor.", "P.Flor."],

  ["P.Fouad", "P.Fouad"],

  ["P.Frankf.", "P.Frankf."],

  ["P.Freer", "P.Freer"],

  ["P.Freib.", "P.Freib."],

  ["P.FuadUniv.", "P.FuadUniv."],

  ["P.Fuad I Univ.", "P.FuadUniv."],

  ["P.Gen.", "P.Gen."],

  ["P.Genova", "P.Genova"],

  ["PUG", "P.Genova"],

  ["P.Giss.", "P.Giss."],

  ["P.Giss.Univ.", "P.Giss.Univ."],

  ["P.Giss.Bibl.", "P.Giss.Univ."],

  ["P.Got.", "P.Got."],

  ["P.Grad.", "P.Grad."],

  ["P.Graux", "P.Graux"],

  ["P.Grenf.", "P.Grenf."],

  ["P.Gron.", "P.Gron."],

  ["P.Gur.", "P.Gur."],

  ["P.Gurob.", "P.Gur."],

  ["P.Hal.", "P.Hal."],

  ["P.Hamb.", "P.Hamb."],

  ["P.Harr.", "P.Harr."],

  ["P.Haun.", "P.Haun."],

  ["P.Heid.", "P.Heid."],

  ["P.Hels.", "P.Hels."],

  ["P.Herm.Landl.", "P.Herm.Landl."],

  ["P.Landl.", "P.Herm.Landl."],

  ["P.Herm.", "P.Herm."],

  ["P.Hib.", "P.Hib."],

  ["P.Hombert", "P.Hombert"],

  ["P.Iand.inv. 653", "P.Iand.inv. 653"],

  ["P.Iand.", "P.Iand."],

  ["P.IFAO", "P.IFAO"],

  ["P.Ital.", "P.Ital."],

  ["P.Jena", "P.Jena"],

  ["P.Kar.Goodsp.", "P.Kar.Goodsp."],

  ["P.Kell.", "P.Kell."],

  ["P.Kellis", "P.Kell."],

  ["P.Köln", "P.Köln"],

  ["P.Kroll", "P.Kroll"],

  ["P.Kroll.", "P.Kroll"],

  ["P.Kron.", "P.Kron."],

  ["P.Laur.", "P.Laur."],

  ["P.LeedsMus.", "P.LeedsMus."],

  ["P.Leeds Museum", "P.LeedsMus."],

  ["P.Leid.Inst.", "P.Leid.Inst."],

  ["P.Leipz.", "P.Leipz."],

  ["P.Leit.", "P.Leit."],

  ["P.Lille", "P.Lille"],

  ["P.Lips.", "P.Lips."],

  ["P.Lond.", "P.Lond."],

  ["P.Lund", "P.Lund"],

  ["P.Marm.", "P.Marm."],

  ["P.Masada", "P.Masada"],

  ["P.Matr.", "P.Matr."],

  ["P.Mert.", "P.Mert."],

  ["P.Merton", "P.Mert."],

  ["P.Meyer", "P.Meyer"],

  ["P.Mich.Aphrod.", "P.Mich.Aphrod."],

  ["P.Mich.Mchl.", "P.Mich.Mchl."],

  ["P.Mich.", "P.Mich."],

  ["P.Mich.Zen.", "P.Mich."],

  ["P.Michael.", "P.Michael."],

  ["P.Mil.Congr.XIV", "P.Mil.Congr.XIV"],

  ["P.Mil.Congr.XIX", "P.Mil.Congr.XIX"],

  ["P.Mil.Congr.XVII", "P.Mil.Congr.XVII"],

  ["P.Mil.Congr.XVIII", "P.Mil.Congr.XVIII"],

  ["P.Mil.", "P.Mil."],

  ["P.Med.", "P.Mil."],

  ["P.Mil.Vogl.", "P.Mil.Vogl."],

  ["P.Mil.R.Univ.", "P.Mil.Vogl."],

  ["PRIMI", "P.Mil.Vogl."],

  ["P.R.U.M.", "P.Mil.Vogl."],

  ["P.Münch.", "P.Münch."],

  ["P.Monac.", "P.Münch."],

  ["P.Murabba'ât", "P.Murabba'ât"],

  ["P.Mur.", "P.Murabba'ât"],

  ["P.Nag Hamm.", "P.Nag Hamm."],

  ["P.Neph.", "P.Neph."],

  ["P.Nepheros", "P.Neph."],

  ["P.Ness.", "P.Ness."],

  ["P.Nessana", "P.Ness."],

  ["P.NYU", "P.NYU"],

  ["P.Oslo", "P.Oslo"],

  ["P.Oxf.", "P.Oxf."],

  ["P.Oxy.Descr.", "P.Oxy.Descr."],

  ["P.Oxy.Hels.", "P.Oxy.Hels."],

  ["P.Oxy.", "P.Oxy."],

  ["P.Panop.Beatty", "P.Panop.Beatty"],

  ["P.Panop.", "P.Panop."],

  ["P.Paris", "P.Paris"],

  ["P.Petaus", "P.Petaus"],

  ["P.Petr.² I", "P.Petr.² I"],

  ["P.Petr.2", "P.Petr.² I"],

  ["P.Petr(2)", "P.Petr.² I"],

  ["P.Petr.", "P.Petr."],

  ["P.Phil.", "P.Phil."],

  ["P.Prag.", "P.Prag."],

  ["P.Prag.Varcl", "P.Prag.Varcl"],

  ["P.Princ.Roll", "P.Princ.Roll"],

  ["P.Princ.", "P.Princ."],

  ["P.Quseir", "P.Quseir"],

  ["P.Rain.Cent.", "P.Rain.Cent."],

  ["P.Rainer Cent.", "P.Rain.Cent."],

  ["P.Rein.", "P.Rein."],

  ["P.Rev.", "P.Rev."],

  ["P.Ross.Georg.", "P.Ross.Georg."],

  ["P.Ryl.", "P.Ryl."],

  ["P.Sakaon", "P.Sakaon"],

  ["P.Sarap.", "P.Sarap."],

  ["P.Sel.Warga", "P.Sel.Warga"],

  ["P.Select.", "P.Select."],

  ["P.Sorb.", "P.Sorb."],

  ["P.Soter.", "P.Soter."],

  ["P.Soterichos", "P.Soter."],

  ["P.Stras.", "P.Stras."],

  ["P.Strasb.", "P.Stras."],

  ["P.Tebt.", "P.Tebt."],

  ["P.Tebt.Tait", "P.Tebt.Tait"],

  ["P.Tebt.Wall", "P.Tebt.Wall"],

  ["P.Theon.", "P.Theon."],

  ["P.Thmouis", "P.Thmouis"],

  ["P.Tor.Amen.", "P.Tor.Amen."],

  ["P.Tor.Amenothes", "P.Tor.Amen."],

  ["P.Tor.Choach.", "P.Tor.Choach."],

  ["P.Tor.", "P.Tor."],

  ["P.Turner", "P.Turner"],

  ["P.Ups.Frid", "P.Ups.Frid"],

  ["P.Vars.", "P.Vars."],

  ["P.Vat.Aphrod.", "P.Vat.Aphrod."],

  ["P.Vatic.Aphrod.", "P.Vat.Aphrod."],

  ["P.Vind.Bosw.", "P.Vind.Bosw."],

  ["P.Vindob.Bosw.", "P.Vind.Bosw."],

  ["P.Vind.Pher.", "P.Vind.Pher."],

  ["P.Vindob.Pher.", "P.Vind.Pher."],

  ["P.Vind.Sal.", "P.Vind.Sal."],

  ["P.Vindob.Sal.", "P.Vind.Sal."],

  ["P.Vind.Sijp.", "P.Vind.Sijp."],

  ["P.Vindob.Sijp.", "P.Vind.Sijp."],

  ["P.Vind.Tand.", "P.Vind.Tand."],

  ["P.Vindob.Tand.", "P.Vind.Tand."],

  ["P.Vind.Worp", "P.Vind.Worp"],

  ["P.Vindob.Worp.", "P.Vind.Worp"],

  ["P.Warr.", "P.Warr."],

  ["P.Wash.Univ.", "P.Wash.Univ."],

  ["P.Wisc.", "P.Wisc."],

  ["P.Würzb.", "P.Würzb."],

  ["P.Yale", "P.Yale"],

  ["P.Zen.Pestm.", "P.Zen.Pestm."],

  ["Pap.Agon.", "Pap.Agon."],

  ["Pap.Biling.", "Pap.Biling."],

  ["Pap.Choix", "Pap.Choix"],

  ["PSI Congr.XI", "PSI Congr.XI"],

  ["PSI Congr.XVII", "PSI Congr.XVII"],

  ["PSI Congr.XX", "PSI Congr.XX"],

  ["PSI Congr.XXI", "PSI Congr.XXI"],

  ["PSI Corr.", "PSI Corr."],

  ["PSI", "PSI"],

  ["SB", "SB"],

  ["Stud.Pal.", "Stud.Pal."],

  ["SPP", "Stud.Pal."],

  ["T.Alb.", "T.Alb."],

  ["T.Mom.Louvre", "T.Mom.Louvre"],

  ["T.Varie", "T.Varie"],

  ["T.Vindol.", "T.Vindol."],

  ["UPZ", "UPZ"],

  ["P.Bodl.", "P.Bodl."],

  ["P.Hever", "P.Hever"],

  ["P.Naqlun", "P.Naqlun"],

  ["P.Pommersf.", "P.Pommersf."],

  ["O.Berenike", "O.Berenike"],

  ["O.Ber.", "O.Berenike"],

  ["P.Ammon", "P.Ammon"],

  ["P.Benaki", "P.Benaki"],

  ["P.Ben. Mus.", "P.Benaki"],

  ["P.Berl.Salmen.", "P.Berl.Salmen."],

  ["P.Berl.Salm.", "P.Berl.Salmen."],

  ["P.Bingen", "P.Bingen"],

  ["P.Harrauer", "P.Harrauer"],

  ["P.Petra", "P.Petra"],

  ["P.Polit.Iud.", "P.Polit.Iud."],

  ["P.Polit.Jud.", "P.Polit.Iud."],

  ["P.Thomas", "P.Thomas"],

  ["P.Eirene", "P.Eirene"],

  ["P.Vindob.Eirene", "P.Eirene"],

  ["P.Chic.Haw.", "P.Chic.Haw."],

  ["P.dem.Chic.Haw.", "P.Chic.Haw."],

  ["P.Dion.Herm.", "P.Dion.Herm."],

  ["P.Eleph.Wagner", "P.Eleph.Wagner"],

  ["O.Eleph.Wagner", "P.Eleph.Wagner"],

  ["P.Eleph.DAIK", "P.Eleph.Wagner"],

  ["O.Eleph.DAIK", "P.Eleph.Wagner"],

  ["P.Louvre", "P.Louvre"],

  ["P.Mon.Apollo", "P.Mon.Apollo"],

  ["P.PalauRib", "P.PalauRib"],

  ["P.Palau Rib", "P.PalauRib"],

  ["P.Gen.2", "P.Gen.2"],

  ["P.Diosk.", "P.Diosk."],

  ["P.Phrur.Diosk.", "P.Diosk."],

  ["P.Jud.Des.Misc.", "P.Jud.Des.Misc."],

  ["P.Erl.Diosp.", "P.Erl.Diosp."],

  ["P.Dryton", "P.Dryton"],

  ["O.Kellis", "O.Kellis"],

  ["O.Kell.", "O.Kellis"],

  ["P.Giss.Apoll.", "P.Giss.Apoll."],

  ["O.BawitIFAO", "O.BawitIFAO"],

  ["O.Bawit IFAO", "O.BawitIFAO"],

  ["P.Euphrates", "P.Euphrates"],

  ["P.Euphr.", "P.Euphrates"],

  ["P.Congr.XV", "P.Congr.XV"],

  ["P.XV.Congr.", "P.Congr.XV"],

  ["P.Paramone", "P.Paramone"],

  ["PSI Com.", "PSI Com."],

  ["P.Horak", "P.Horak"],

  ["Ch.L.A.", "Ch.L.A."],

  ["ChLA", "Ch.L.A."],

  ["Actenstücke", "Actenstücke"],

  ["BKT", "BKT"],

  ["BKU", "BKU"],

  ["C.Ord.Ptol.", "C.Ord.Ptol."],

  ["C.Pap.Lat.", "C.Pap.Lat."],

  ["CPL", "C.Pap.Lat."],

  ["C.Pap.Jud.", "C.Pap.Jud."],

  ["CPJ", "C.Pap.Jud."],

  ["C.Ptol.Sklav.", "C.Ptol.Sklav."],

  ["C.Zen.Palestine", "C.Zen.Palestine"],

  ["Chapa, Letters of Condolence", "Chapa, Letters of Condolence"],

  ["O.Ain Labakha", "O.Ain Labakha"],

  ["O.Bahria", "O.Bahria"],

  ["O.Bahria Div.", "O.Bahria Div."],

  ["O.Crum", "O.Crum"],

  ["O.Dor.", "O.Dor."],

  ["O.Krok.", "O.Krok."],

  ["O.Nancy", "O.Nancy"],

  ["O.Sarm.", "O.Sarm."],

  ["O.Skeat.", "O.Skeat."],

  ["O.Wångstedt", "O.Wångstedt"],

  ["P.Beatty", "P.Beatty"],

  ["P.Äg.Handschrift.", "P.Äg.Handschrift."],

  ["P.Brookl.Dem.", "P.Brookl.Dem."],

  ["P.Cair.Cat.", "P.Cair.Cat."],

  ["P.Cair. Cat.", "P.Cair.Cat."],

  ["P.Count.", "P.Count."],

  ["P.Gron.Amst.", "P.Gron.Amst."],

  ["P.Hawara", "P.Hawara"],

  ["P.Hawara dem.", "P.Hawara dem."],

  ["P.Haw.dem.", "P.Hawara dem."],

  ["P.HermitageCopt.", "P.HermitageCopt."],

  ["P.KölnÄgypt", "P.KölnÄgypt"],

  ["P.KölnÄg.", "P.KölnÄgypt"],

  ["P.KölnLüdecckens", "P.KölnLüdecckens"],

  ["P.Leid.", "P.Leid."],

  ["P.Lesestücke", "P.Lesestücke"],

  ["P.Lond.Copt. London", "P.Lond.Copt. London"],

  ["P.Lond.Copt.", "P.Lond.Copt. London"],

  ["P.Lond.Wasser.", "P.Lond.Wasser."],

  ["P.Magdola", "P.Magdola"],

  ["P.Oxy.Census", "P.Oxy.Census"],

  ["P.Recueil", "P.Recueil"],

  ["P.Zaki Aly", "P.Zaki Aly"],

  ["Rom.Mil.Rec.", "Rom.Mil.Rec."],

  ["Sel.Pap.", "Sel.Pap."],

  ["Suppl.Mag.", "Suppl.Mag."],

  ["Witkowski, Epistulae privatae", "Witkowski, Epistulae privatae"],

  ["Tibiletti, Lettere privatae", "Tibiletti, Lettere privatae"],

  ["C.Pap.Hengstl", "C.Pap.Hengstl"],

  ["Doc.Eser.Rom.", "Doc.Eser.Rom."],

  ["Feste", "Feste"],

  ["Jur.Pap.", "Jur.Pap."],

  ["O.Ashm.Copt.", "O.Ashm.Copt."],

  ["O.CrumST", "O.CrumST"],

  ["O.CrumVC", "O.CrumVC"],

  ["O.Leid.Dem.", "O.Leid.Dem."],

  ["O.Louvre", "O.Louvre"],

  ["O.Bawit", "O.Bawit"],

  ["O.Brit.Mus.Copt.", "O.Brit.Mus.Copt."],

  ["P.Brux.Dem.", "P.Brux.Dem."],

  ["O.Deir el-Bahari", "O.Deir el-Bahari"],

  ["O.Hor", "O.Hor"],

  ["O.Magnien", "O.Magnien"],

  ["O.Medin. HabuCopt.", "O.Medin. HabuCopt."],

  ["O.Medin. HabuDem.", "O.Medin. HabuDem."],

  ["O.Mattha", "O.Mattha"],

  ["O.Mich.Copt.", "O.Mich.Copt."],

  ["O.Mich.Copt.Etmoulon", "O.Mich.Copt.Etmoulon"],

  ["O.Narm.Dem.", "O.Narm.Dem."],

  ["O.Métrologie", "O.Métrologie"],

  ["O.Mon.Phoib.", "O.Mon.Phoib."],

  ["P.Edmondstone", "P.Edmondstone"],

  ["O.Muzawwaqa", "O.Muzawwaqa"],

  ["O.Tempeleide", "O.Tempeleide"],

  ["O.Vind.Copt.", "O.Vind.Copt."],

  ["O.Zürich", "O.Zürich"],

  ["P.Amh.Eg.", "P.Amh.Eg."],

  ["P.Assoc.", "P.Assoc."],

  ["P.Auswahl", "P.Auswahl"],

  ["P.Berl.Dem.", "P.Berl.Dem."],

  ["P.Berl.Schmidt", "P.Berl.Schmidt"],

  ["P.Berl.Spieg.", "P.Berl.Spieg."],

  ["P.Bodm.", "P.Bodm."],

  ["P.Brit.Mus.", "P.Brit.Mus."],

  ["P.Brit.Mus.Reich.", "P.Brit.Mus.Reich."],

  ["P.Bürgsch.", "P.Bürgsch."],

  ["P.CLT", "P.CLT"],

  ["P.Carlsb.", "P.Carlsb."],

  ["P.CattleDocs.", "P.CattleDocs."],

  ["P.Chept.", "P.Chept."],

  ["P.Chic.", "P.Chic."],

  ["P.Choach.Survey", "P.Choach.Survey"],

  ["P.Chrest.Nouvelle", "P.Chrest.Nouvelle"],

  ["P.Chrest.Revillout", "P.Chrest.Revillout"],

  ["P.Chronik", "P.Chronik"],

  ["P.Corpus Revillout", "P.Corpus Revillout"],

  ["P.Demotica", "P.Demotica"],

  ["P.Edg.", "P.Edg."],

  ["P.Egerton", "P.Egerton"],

  ["P.Egger", "P.Egger"],

  ["P.Ehevertr.", "P.Ehevertr."],

  ["P.Eleph.Dem.", "P.Eleph.Dem."],

  ["P.Enteux.", "P.Enteux."],

  ["P.Erbstreit", "P.Erbstreit"],

  ["P.Fam.Theb.", "P.Fam.Theb."],

  ["P.Fay.Copt.", "P.Fay.Copt."],

  ["P.Gebelen", "P.Gebelen"],

  ["P.Giss.Lit.", "P.Giss.Lit."],

  ["P.Hausw.", "P.Hausw."],

  ["P.Hercul.", "P.Hercul."],

  ["P.Hermias", "P.Hermias"],

  ["P.Hermitage Copt.", "P.Hermitage Copt."],

  ["P.Holm.", "P.Holm."],

  ["P.Hou", "P.Hou"],

  ["P.KRU", "P.KRU"],

  ["P.LandLeases", "P.LandLeases"],

  ["P.Land Leases", "P.LandLeases"],

  ["P.Leid.Dem.", "P.Leid.Dem."],

  ["P.Libbey", "P.Libbey"],

  ["P.LilleDem.", "P.LilleDem."],

  ["P.Lille Dem.", "P.LilleDem."],

  ["P.Loeb", "P.Loeb"],

  ["P.Lond.Lit.", "P.Lond.Lit."],

  ["P.Lonsdorfer", "P.Lonsdorfer"],

  ["P.Lugd.Bat.", "P.Lugd.Bat."],

  ["P.L.Bat.", "P.Lugd.Bat."],

  ["P.Mallawi", "P.Mallawi"],

  ["P.Marini", "P.Marini"],

  ["P.Meerman.", "P.Meerman."],

  ["P.Mich. Copt.", "P.Mich. Copt."],

  ["P.Mich. Nims", "P.Mich. Nims"],

  ["P.Mon.Epiph.", "P.Mon.Epiph."],

  ["P.MorganLib.", "P.MorganLib."],

  ["P.MoscowCopt.", "P.MoscowCopt."],

  ["P.Oxy.Astr.", "P.Oxy.Astr."],

  ["P.Petersb.", "P.Petersb."],

  ["P.Pher.", "P.Pher."],

  ["P.PisaLit.", "P.PisaLit."],

  ["P.Pisa Lit.", "P.PisaLit."],

  ["P.Pisentius", "P.Pisentius"],

  ["P.Prag.Satzung", "P.Prag.Satzung"],

  ["P.QasrIbrim", "P.QasrIbrim"],

  ["P.Qasr Ibrim", "P.QasrIbrim"],

  ["P.QuelquesTextes", "P.QuelquesTextes"],

  ["P.Quelques Textes", "P.QuelquesTextes"],

  ["P.Rain.Unterricht", "P.Rain.Unterricht"],

  ["P.Rain.UnterrichtKopt.", "P.Rain.UnterrichtKopt."],

  ["P.Rain.Unterricht Kopt.", "P.Rain.UnterrichtKopt."],

  ["P.Revillout Copt.", "P.Revillout Copt."],

  ["P.Ryl.Copt.", "P.Ryl.Copt."],

  ["P.Ryl.Dem.", "P.Ryl.Dem."],

  ["P.Schenkung.", "P.Schenkung."],

  ["P.Schreibertrad.", "P.Schreibertrad."],

  ["P.Schub.", "P.Schub."],

  ["P.Schutzbriefe", "P.Schutzbriefe"],

  ["P.Siegesfeier", "P.Siegesfeier"],

  ["P.Siut", "P.Siut"],

  ["P.SlaveryDem.", "P.SlaveryDem."],

  ["P.Slavery.Dem.", "P.SlaveryDem."],

  ["P.Stras.Dem.", "P.Stras.Dem."],

  ["P.Strasb.Dem.", "P.Stras.Dem."],

  ["P.TestiBotti", "P.TestiBotti"],

  ["P.Testi Botti", "P.TestiBotti"],

  ["P.Thead.", "P.Thead."],

  ["P.Tor.Botti", "P.Tor.Botti"],

  ["P.Tsenhor", "P.Tsenhor"],

  ["P.Ups.8", "P.Ups.8"],

  ["P.Ups.", "P.Ups.8"],

  ["P.Verpfründung.", "P.Verpfründung."],

  ["P.YaleCopt.", "P.YaleCopt."],

  ["P.Yale Copt.", "P.YaleCopt."],

  ["P.Zauzich", "P.Zauzich"],

  ["P.Zen.Dem.", "P.Zen.Dem."],

  ["PSI Il.", "PSI Il."],

  ["PSI.Il.", "PSI Il."],

  ["PSI Od.", "PSI Od."],

  ["PSI.Od.", "PSI Od."],

  ["SB Kopt.", "SB Kopt."],

  ["T.Dacia", "T.Dacia"],

  ["T.Jucundus", "T.Jucundus"],

  ["T.Pizzaras", "T.Pizzaras"],

  ["T.Sulpicii", "T.Sulpicii"],

  ["T.Varie", "T.Varie"],

  ["T.Vindon.", "T.Vindon."],

      ]
;

InstallAC=function(a,b,c,e,f,h,g,l){
    HTML_FORM=a;d=b;Ta=c;
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
    d.blur();
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
        if(J&&d.createTextRange&&a.srcElement==d&&O(d)==0&&P(d)==0){
            Wa(d);
            a.cancelBubble=true;
            a.returnValue=false;
            return false
        }
    }
}

function lb(){
    if(x=="url"){
        Ra()
    }
    ca()
}

function ca(){
    if(i){
        i.style.left=Ia(d,"offsetLeft")+"px";
        i.style.top=Ia(d,"offsetTop")+d.offsetHeight-1+"px";
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
        return d.offsetWidth-T*2
    }else{
        return d.offsetWidth
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
    HTML_FORM.onsubmit=la;
    d.autocomplete="off";
    d.onblur=fb;
    d.onfocus=kb;
    if(d.createTextRange){
        d.onkeydown=new Function("return okdh(event); ");
        d.onkeyup=new Function("return okuh(event); ")
    }else{
        d.onkeypress=okdh;
        d.onkeyup=okuh;
    }
    d.onsubmit=la;
    k=d.value;
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
        if(d.attachEvent){
            d.attachEvent("onpropertychange",ab);
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
        B();
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
    Z=d.value;
    Oa()
}

function pb(){
    Ta.focus()
}
sfi=function(){
    d.focus();
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

function B(){
    document.getElementById("completeDiv").style.visibility="hidden";
    document.getElementById("completeIFrame").style.visibility="hidden";
}

function Sa(){
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
    Ka(g,L);
    if(pa>0){
        g.height=16*pa+4;
        m.height=g.height-4;
    }else{
        B();
    }
};
sendCached=function(elementNode,query,f){
    if(D>0)D--;
    var h=(new Date).getTime();
    if(!elementNode)elementNode=K;
    resultArray = new Array();
    labelArray = new Array();
    var qprefix = query.toLowerCase();
    for(var i=0;i<mads.length;i++){
        if(mads[i][0].toLowerCase().indexOf(qprefix) == 0){
            resultArray[resultArray.length] = mads[i][0];
            labelArray[labelArray.length] = "";
        }
    }
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
    Ka(g,L);
    if(pa>0){
        g.height=16*pa+4;
        m.height=g.height-4;
    }else{
        B();
    }
};
hcd=function(){
    B();
    C=null;
};
function Oa(){
    if(j==40||j==38)Fa();
    var a=P(d),b=O(d),c=d.value;
    if(J&&j!=0){
        if(a>0&&b!=-1)c=c.substring(0,b);
        if(j==13||j==3){
            var e=d;
            if(e.createTextRange){
                var f=e.createTextRange();
                f.moveStart("character",e.value.length);
                f.select();
            }else if(e.setSelectionRange){
                e.setSelectionRange(e.value.length,e.value.length);
            }
        }else{
            if(d.value!=c)N(c);
        }
    }
    if(j!=9&&j!=13&&!(j>=16&&j<=20)&&j!=27&&!(j>=33&&j<=38)&&j!=40&&j!=44&&!(j>=112&&j<=123)){
        k=c;
        if(j!=39)oa=c;
    }
    if(Xa(j)&&j!=0&&ja==k)Ka(i,L);
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
    B();
    if(a=="url"){
        var b="";
        if(r!=-1&&n)b=L(n);
        if(b=="")b=d.value;
        if(w=="")document.title=b;
        else document.title=w;
        var c="window.frames['"+Va+"'].location = \""+b+'";';
        setTimeout(c,10);
        return false
    }else if(a=="query"){
        p.disabled=(t.disabled=true);
        if(oa!=d.value){
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
    window.open(d.value);
    B();
    return false;
};
idkc=function(a){
    if($){
        if(W){
            Ua();
        }
        var b=d.value;
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
            d.focus();
        }
        da=false;
    }
    qa=k;setTimeout("idfn()",bb(D));
    return true;
};
setTimeout("idfn()",10);
var gb=function(){
    d.blur();
    N(L(this));
    w=ma(this);
    da=true;
    if(la())HTML_FORM.submit();
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
},ib=function(){
    s(this,"aAutoComplete");
};
function sa(a){
    S="t";
    k=M;
    N(M);
    w=M;
    if(!U||A<=0)return;
    Sa();
    if(n)s(n,"aAutoComplete");
    if(a==A||a==-1){
        r=-1;
        d.focus();
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
function Ka(a,b){
    var c=d,e=false;
    r=-1;
    var f=a.getElementsByTagName(mb),h=f.length;
    A=h;
    U=f;
    pa=h;
    M=k;
    if(k==""||h==0){
        B();
    }else{
        Sa();
    }
    var g="";
    if(k.length>0){
        var l,o;
        for(var l=0;l<h;l++){
            for(o=0;o<a.prefixStrings.length;o++){
                var y=a.prefixStrings[o]+k;
                if(ha||!aa&&b(f.item(l)).toUpperCase().indexOf(y.toUpperCase())==0||aa&&l==0&&b(f.item(l)).toUpperCase()==y.toUpperCase()){
                    g=a.prefixStrings[o];
                    e=true;
                    break;
                }
            }
            if(e){
                break;
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
    d.size=Math.floor(a/6.18);
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
        var h=document.createElement("SPAN");
        s(h,"lAutoComplete");
        if(u.substring(0,2)=="zh")h.style.height=d.offsetHeight-4;
        else h.style.height=d.offsetHeight-6;
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
    d.value=a;
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
    var a=d.value,b=O(d),c=P(d);
    return b==X&&c==Y&&a==La;
}
function Ua(){
    La=d.value;
    X=O(d);
    Y=P(d);
}
ba=function(a,b){
    if(a==X&&b==Y){
        qb();
    }
};
function qb(){Fa();sa(r+1)};
