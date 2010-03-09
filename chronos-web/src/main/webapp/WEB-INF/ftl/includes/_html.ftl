<#--
This FTL file defines macros to replace commonly used HTML tags.
-->

<#--
Replacement for the HTML "html" tag.
-->
<#macro html>
<#compress>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
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
Write the HTML title tag. Use the global variable "pageTitle" if available, otherwise default to "ESE".
-->
<#macro pageTitle><title>${.globals.pageTitle!"ESE"}</title></#macro>
