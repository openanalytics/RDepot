<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ include file="header.jsp" %>

<div id="bodyColumn">
	<h4><spring:message code="package.title"/></h4>

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
    
    <table id="packages"> 
        <tr>
            <th><spring:message code="table.header.name"/></th>
            <th><spring:message code="table.header.version"/></th>
            <th><spring:message code="table.header.description"/></th>
            <th><spring:message code="table.header.maintainer"/></th>
            <th><spring:message code="table.header.repository"/></th>
            <th><spring:message code="table.header.active"/></th>
            <th><spring:message code="table.header.actions"/></th>
        </tr>
        <c:forEach items="${packages}" var="packageBag" >
            <tr id="tr${packageBag.repository.id}${packageBag.name}${packageBag.id}">
                <td><a href="<c:url value='/manager/repositories' />/${packageBag.repository.name}/packages/${packageBag.name}/${packageBag.version}">${packageBag.name}</a></td>
                <td>${packageBag.version}</td>
                <td>${packageBag.description}</td>
                <td>${packageBag.user.name}</td>
                <td><a href="<c:url value='/manager/repositories' />/${packageBag.repository.name}">${packageBag.repository.name}</a></td>
                <td><input id="act${packageBag.id}" type="checkbox" onchange="changeActive(${packageBag.id})" <c:choose><c:when test='${packageBag.active}'>checked="checked"</c:when><c:otherwise></c:otherwise></c:choose> ></td>
                <td>
                  <a data-placement="bottom" data-toggle="tooltip" data-original-title="<spring:message code='table.actions.feed'/>" class="btn btn-lg" href="<c:url value='/manager/packages' />/${packageBag.id}/feed">
                    <span class="glyphicon glyphicon-calendar"></span>
                  </a> 
                  <!--<a href="<c:url value='/manager/packages' />/${packageBag.id}/edit"><img data-placement="bottom" data-toggle="tooltip" data-original-title="<spring:message code='table.actions.edit'/>" src="${staticUrl}/img/edit.png"></a>-->
                  <a id="del${packageBag.id}" onclick="deletePackage(${packageBag.id}, '${packageBag.repository.id}${packageBag.name}')" href="#"><img data-placement="bottom" data-toggle="tooltip" data-original-title="<spring:message code='table.actions.delete'/>" src="${staticUrl}/img/delete.png"></a></td>
            </tr>
        </c:forEach>
    </table>
		
</div>
<script type="text/javascript">

    function deletePackage(id, tr)
    {
        var postUrl = "<c:url value="/manager/packages" />" + "/" + String(id) + "/delete";
	        $.ajax({
                type: "DELETE",
                dataType: 'json',
                url: postUrl,
                success: function(data)
                {
                    if(data.success != null)
                    {
                        alert(data.success);
                        $("#tr"+tr+String(id)).remove();
                    }
                    else
                    {
                        alert(data.error);
                    }
                }
	        });	    
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
        var postUrl = "<c:url value="/manager/packages" />" + "/" + String(id) + "/" + action;  
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

    /* function toggleHideTable(id)
    {
	    $('#'+id).toggle();
    }

    function toggleHidePackage(id)
    {
	    $('.package'+id).toggle();
	    $('#packageHeader'+id).toggle();	
    } */

</script>
<%@ include file="footer.jsp" %>
