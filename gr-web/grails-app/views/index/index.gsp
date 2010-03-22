<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
</head>
<body>
<p>Got scheduler <%=schedulerUniqueId %></p>
<p>Got <%=jobGroupNames.size() %> job group names</p>
<g:each in="${jobGroupNames}" var="jobGroupName">
<h3>${jobGroupName}</h3>
</g:each>
</body>
</html>
