<@html>
<@head>
<title>Chronos</title>
<script type="text/javascript" src="js/jquery-1.4.2.min.js"></script>
<script type="text/javascript">
function jq(id) { 
   return '#' + id.replace(/(:|\.)/g,'\\$1');
 }

function hideJob(jobId) {
  console.log("hiding " + jobId);
  $(jq(jobId)).hide('slow');
}

$(document).ready(function(){
  console.log("ready");
  $(".group .job").each(function() {
    id = $(this).attr("id");
    console.log("id:" + id); 
    $(this).find(".edit").bind("click", {id: id}, function(e) {
      hideJob(e.data.id);
    });
  });
});
</script>
</@head>
<@body>
<p>Today is ${now?datetime}.</p>
<h3>Jobs</h3>
<dl>
<#list jobGroups as group>
<dt>Group: ${group.name}</dt>
<dd>
<ul class="group">
<#list group.jobs as job>
<li class="job" id="${job.name}">${job.name} (${job.class}) : ${job.triggerNames} <a class="edit" href="#">EDIT</a></li>
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
