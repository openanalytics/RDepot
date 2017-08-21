<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ include file="header.jsp" %>

<div id="bodyColumn">

	<h4><spring:message code="user.title"/></h4>
	<c:if test="${not empty success}">
		<div class="successblock">
		
			<spring:message code="general.success"/>
			<br />
			<spring:message code="general.info"/>
			${success}
			
		</div>
	</c:if>
	<c:if test="${not empty warning}">
		<div class="warningblock">
		
			<spring:message code="general.warning"/>
			<br />
			<spring:message code="general.info"/>
			${warning}
			
		</div>
	</c:if>
	<c:if test="${not empty error}">
		<div class="errorblock">
		
			<spring:message code="general.error"/>
			<br />
			<spring:message code="general.info"/>
			${error}
			
		</div>
	</c:if>
    
    <table id="users"> 
        <tr>
            <th><spring:message code="table.header.name"/></th>
            <th><spring:message code="table.header.email"/></th>
            <th><spring:message code="table.header.username"/></th>
            <th><spring:message code="table.header.role"/></th>
            <th><spring:message code="table.header.active"/></th>
            <th><spring:message code="table.header.actions"/></th>
        </tr>
        <c:forEach items="${users}" var="user" >
            <tr id="tr${user.id}">
                <td><a href="<c:url value='/manager/users' />/${user.login}">${user.name}</a></td>
                <td>${user.email}</td>
                <td>${user.login}</td>
                <td>${user.role.description}</td>
                <td><input id="act${user.id}" type="checkbox" onchange="changeActive(${user.id})" <c:if test='${user.active}'>checked="checked"</c:if> ></td>
                <td><a href="<c:url value='/manager/users' />/${user.id}/edit"><img data-placement="bottom" data-toggle="tooltip" data-original-title="<spring:message code='table.actions.edit'/>" src="${staticUrl}/img/edit.png"></a> <a onclick="deleteUser(${user.id})" href="#"><img data-placement="bottom" data-toggle="tooltip" data-original-title="<spring:message code='table.actions.delete'/>" src="${staticUrl}/img/delete.png"></a></td>
            </tr>
        </c:forEach>
    </table>
</div>
<script type="text/javascript">

    var $dialog = $('<div></div>');
    $dialog.html('<spring:message code="user.delete.warning"/>');

    /* $.ajax({
        type: "GET",
        dataType: 'json',
        url: "<c:url value="/manager/users/list" />",
        success: function(data)
        {
          var users = "";
          for(var i = 0, l = data.length; i < l; i++)
          {
              var check = "";
              if(data[i].active)
              {
                check = "checked";
              }
              users += '<tr id="tr' + data[i].id + '"><td><a href="<c:url value="/manager/user" />/' + data[i].login + '">' + data[i].name + '</a></td><td>' + data[i].email + '</td><td>' + data[i].login + '</td><td>' + data[i].role.description + '</td><td><input id="act' + data[i].id + '" type="checkbox" checked="' + check + '" ></td><td><a id="del' + data[i].id + '" href="#"><spring:message code="table.actions.delete"/></a></td></tr>';
          }
          $(document).ready(function(){
            $("#users").append(users);
          });
        }
    }); */

    function deleteUser(id)
    {
        var postUrl = "<c:url value="/manager/users" />" + "/" + String(id) + "/delete";
	        $.ajax({
                type: "DELETE",
                dataType: 'json',
                url: postUrl,
                success: function(data)
                {
                    if(data.success != null)
                    {
                        alert(data.success);
                        $("#tr"+String(id)).remove();
                    }
                    else
                    {
                        alert(data.error);
                    }
                }});	    
    }
    
    function changeActive(id)
    {
        var action = "";
        var checked = false;
        if($("#act" + String(id)).is(":checked"))
        {
            action = "activate"
            checked = true;
        }
        else
        {
            action = "deactivate"
            checked = false;
        }
        var postUrl = "<c:url value="/manager/users" />" + "/" + String(id) + "/" + action;  
        $.ajax({
            type: "PUT",
            dataType: 'json',
            url: postUrl,
            success: function(data)
            {
                if(data.success != null)
                {
                    alert(data.success);
                    /*$("#act"+String(id)).attr("checked", checked);*/
                }
                else
                {
                    alert(data.error);
                    $("#act"+String(id)).attr("checked", !checked);
                }
            }
        });
    }

</script>
<%@ include file="footer.jsp" %>
