<!-- TITLE: Launch the TEC Browser!!  -->
<!-- HDLMTS: <SCRIPT LANGUAGE="Javascript">  var javawsInstalled = 0;  isIE = "false";  if (navigator.mimeTypes && navigator.mimeTypes.length) {     x = navigator.mimeTypes['application/x-java-jnlp-file'];     if (x) javawsInstalled = 1  }   else {     isIE = "true";  }  function insertLink(url, name) {    if (javawsInstalled) {       document.write("<a href=" + url + ">"  + name + "</a>");    } else {      document.write("Need to install Java Web Start");    }  }</SCRIPT><SCRIPT LANGUAGE="VBScript">  on error resume next  If isIE = "true" Then     If Not(IsObject(CreateObject("JavaWebStart.IsInstalled"))) Then        javawsInstalled = 0     Else        javawsInstalled = 1     End If  End If</SCRIPT> -->
<!-- Document created by ../UPDATEwww1.12/html2bdy.pl v1.1  from /usr/contrib/www/tec/update/new/tecconmainpage.bdy -->
<body BGCOLOR="#FEFFFF" >

<br><br><br>
<h3>Launching the TEC tool from your web browser</h3>
<table>
<tr>
<td>
<img SRC="stop.gif" ALT="STOP" 
WIDTH="60" HEIGHT="62">
</td>
<td>
<font size="+1">
The TEC corpus browser uses Java<sup><font SIZE="-2">TM</font></sup>
Web Start technology.  If you have not already downloaded
Java<sup><font SIZE="-2">TM</font></sup> Web Start technology, you
will need to <b><a
HREF="http://java.sun.com/cgi-bin/javawebstart-platform.sh?">do so
now</a></b>, before starting the TEC tool.
</font>
</td>
<table>

<br>
<br>
<p>
If you already have Java Web Start installed, launch the tool by
clicking on the icon below:

<center>
<a href="tec.jnlp"><img border=0 src="teclick.png"></a>
</center>

<br>
<P> 
If you need instruction on how to use the concordancing tool, 
 <a target=freq href="../Doc/userguide.html">have a look at the TEC User Guide.</a>


</body>
