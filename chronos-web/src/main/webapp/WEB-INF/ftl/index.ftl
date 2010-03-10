<@html>
<@head>
<title>Chronos</title>
</@head>
<@body>
<p>Today is ${now?datetime}.</p>
<dl>
<#list groups as group>
<dt>${group.name}</dt>
<dd>
<ul>
<#list group.jobs as job>
<li>${job.name} : ${job.class}</li>
</#list>
</ul>
</dd>
</#list>
</dl>
</@body>
</@html>
