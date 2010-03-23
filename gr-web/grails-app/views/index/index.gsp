<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
</head>
<body>
<p>Got scheduler <%=schedulerUniqueId %></p>
<h3>Jobs</h3>
<p>Got <%=jobGroupNames.size() %> job group names</p>
<dl>
<g:each in="${jobGroupNames}" var="jobGroupName">
<dt>${jobGroupName}</dt>
<dd>
<ul>
<g:each in="${jobGroups[jobGroupName]}" var="e">
<g:set var="job" value="${e.value}" />
<li>${job.name} (${job.className}) : ${job.triggerNames}</li>
</g:each>
</ul>
</dd>
</g:each>
</dl>
</body>
</html>
