<@html>
<@head>
<title>Chronos</title>
</@head>
<@body>
<p>Today is ${now?datetime}.</p>
<h3>Jobs</h3>
<dl>
<#list jobGroups as group>
<dt>Group: ${group.name}</dt>
<dd>
<ul>
<#list group.jobs as job>
<li>${job.name} (${job.class})</li>
</#list>
</ul>
</dd>
</#list>
</dl>
<h3>Triggers</h3>
<dl>
<#list triggerGroups as group>
<dt>Group: ${group.name}</dt>
<dd>
<ul>
<#list group.triggers as trigger>
<li>${trigger.name} (${trigger.class})<#if trigger.description??> : ${trigger.description}</#if></li>
</#list>
</ul>
</dd>
</#list>
</dl>
</@body>
</@html>
