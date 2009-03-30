<%--
Copyright 2004 The Apache Software Foundation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>
<html>
<head>
<style type="text/css">
UL {
list-style-type: circle;
}
.pn-portal-title {
color:#1f4e66;
font-size:36px;
font-family:"Times New Roman", Times, Serif;
font-weight:normal;
letter-spacing:2px;
width:100%;
border-bottom-width:medium;
border-bottom-style:solid;
border-bottom-color:#900;
background-color:#f6f5e0;
}
A {
font-weight:bold;
color:#1f4e66;
}
A:hover{
color:#900;
}
</style>
</head>
<body>
<h1 class="pn-portal-title">papyri<span style="color: #990000;font-weight:bold">.</span>info</h1>
<div>
<ul>
<li><a href="portal/default-page.psml">Search for Metadata, Images and Duke Databank Transcriptions</a></li>
<li><a href="portal/apisfull.psml?controlName=toronto.apis.17">See an example display for toronto.apis.17</a></li>
<li><a href="portal/apisfull.psml?controlName=hermitage.apis.21">See an example display for hermitage.apis.21</a></li>
<li><a href="portal/apisfull.psml?controlName=columbia.apis.p260">See an example display for columbia.apis.p260</a></li>
<li><a href="portal/apisfull.psml?controlName=columbia.apis.1377">See an example display for columbia.apis.1377</a></li>
<li><a href="portal/apisfull.psml?controlName=columbia.apis.p179">See an example display for columbia.apis.p179</a></li>
</ul>
  </div>   
<!--jsp:forward page="portal/"/-->
</body>
</html>
