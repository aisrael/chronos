<#--
This FTL file defines macros to replace commonly used HTML tags.
-->

<#--
Replacement for the HTML "html" tag.
-->
<#macro html>
<#compress>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<#nested>
</html>
</#compress>
</#macro>

<#--
Replacement for the HTML "head" tag.
-->
<#macro head>
<head>
<#nested>
</head>
</#macro>

<#--
Replacement for the HTML "body" tag.
-->
<#macro body>
<body>
<#nested>
</body>
</#macro>

<#--
Write the HTML title tag. Use the global variable "pageTitle" if available, otherwise default to "Chronos".
-->
<#macro pageTitle><title>${.globals.pageTitle!"Chronos"}</title></#macro>
