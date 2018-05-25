<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ include file="header.jsp" %>

<div id="bodyColumn">

	<h4><spring:message code="repository.title"/></h4>
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
    
    <table id="repositories"> 
        <thead>
            <tr>
                <th><spring:message code="table.header.name"/></th>
                <th><spring:message code="table.header.publicationuri"/></th>
                <th><spring:message code="table.header.serveraddress"/></th>
                <th><spring:message code="table.header.version"/></th>
                <th><spring:message code="table.header.numberofpackages"/></th>
                <th><spring:message code="table.header.published"/></th>
                <th><spring:message code="table.header.actions"/></th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${repositories}" var="repository" >
                <tr id="tr${repository.id}">
                    <td><a href="<c:url value='/manager/repositories' />/${repository.name}">${repository.name}</a></td>
                    <td><a href="${repository.publicationUri}">${repository.publicationUri}</a></td>
                    <td>${repository.serverAddress}</td>
                    <td>${repository.version}</td>
                    <td><a href="<c:url value='/manager/repositories' />/${repository.id}/packages">${repository.packages.size()}</a></td>
                    <td>
                        <c:choose>
                           <c:when test="${repository.isPublished()}">
                              <i id="published-icon-${repository.id}" class="glyphicon glyphicon-check"></i>
                           </c:when>
                           <c:otherwise>
                                <i id="published-icon-${repository.id}" class="glyphicon glyphicon-unchecked"></i>
                           </c:otherwise>
                       </c:choose>
                    </td>
                    <td>
                    	<a data-placement="bottom" data-toggle="tooltip" data-original-title="<spring:message code='table.actions.feed'/>" class="btn btn-info" href="<c:url value='/manager/repositories' />/${repository.id}/feed">
                            <span class="glyphicon glyphicon-calendar"></span>
                        </a> 
                        <a data-placement="bottom" data-toggle="tooltip" data-original-title="<spring:message code='table.actions.edit'/>" class="btn btn-info" href="<c:url value='/manager/repositories' />/${repository.id}/edit">
                            <span class="glyphicon glyphicon-edit"></span>
                        </a> 
                        <button id="publish-${repository.id}" data-placement="bottom" data-toggle="tooltip" data-original-title="<spring:message code='table.actions.publish'/>" type="button" class="btn btn-success" onclick="publishRepository(${repository.id})">
                            <span class="glyphicon glyphicon-globe"></span>
                        </button> 
                    
	                    <button <c:if test="${!repository.isPublished()}">style="display: none"</c:if> id="unpublish-${repository.id}" data-placement="bottom" data-toggle="tooltip" data-original-title="<spring:message code='table.actions.unpublish'/>" type="button" class="btn btn-warning" onclick="unpublishRepository(${repository.id})" >
	                        <span class="glyphicon glyphicon-off"></span>
                        </button> 
	                    
	                    <button data-placement="bottom" data-toggle="tooltip" data-original-title="<spring:message code='table.actions.delete'/>" class="btn btn-danger" type="button" id="del${repository.id}" onclick="deleteRepository(${repository.id})" >
	                        <span class="glyphicon glyphicon-remove"></span>
                        </button>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
        <c:if test="${role > 2}">
		      <tfoot>
		          <tr>
		              <td colspan="7"><a class="btn btn-success" href="<c:url value='/manager/repositories/create' />">+ <spring:message code="menu.general.create"/></a></td>
		          </tr>
		      </tfoot>
		    </c:if>
    </table>
</div>
<script type="text/javascript">

    var $dialog = $('<div></div>');
    $dialog.html('<spring:message code="repository.delete.warning"/>');
    
    /* $.ajax({
        type: "GET",
        dataType: 'json',
        url: "<c:url value="/manager/repositories/list" />",
        success: function(data)
        {
            var repositores = "";
          
            for(var i = 0, l = data.length; i < l; i++)
            {
                repositories += '<tr id="tr' + data[i].id + '"><td><a href="<c:url value="/manager/repository" />/' + data[i].id + '">' + data[i].name + '</a></td><td>' + data[i].publicationUri + '</td><td>' + data[i].serverAddress + '</td><td>' + data[i].version + '</td><td>' + data[i].packages.length + '</td><td><a id="del' + data[i].id + '" href="#"><spring:message code="table.actions.delete"/></a></td></tr>';
            }
            $(document).ready(function(){
                $("#repositories").append(repositories);       
            });
        }
    }); */

    function deleteRepository(id)
    {
        var postUrl = "<c:url value="/manager/repositories" />" + "/" + String(id) + "/delete";
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
    
    function publishRepository(id)
    {
        var postUrl = "<c:url value="/manager/repositories" />" + "/" + String(id) + "/publish";
        $.ajax({
                type: "POST",
                dataType: 'json',
                url: postUrl,
                success: function(data)
                {
                    if(data.success != null)
                    {
                        alert(data.success);
                        $("#unpublish-"+id).show();
                        if($("#published-icon-"+id).hasClass("glyphicon-unchecked"))
                    	{
                        	$("#published-icon-"+id).removeClass("glyphicon-unchecked");
                        	$("#published-icon-"+id).addClass("glyphicon-check");
                    	}
                    }
                    else
                    {
                        alert(data.error);
                    }
               }
        });	    
    }
    
    function unpublishRepository(id)
    {
        var postUrl = "<c:url value="/manager/repositories" />" + "/" + String(id) + "/unpublish";
        $.ajax({
                type: "POST",
                dataType: 'json',
                url: postUrl,
                success: function(data)
                {
                    if(data.success != null)
                    {
                        alert(data.success);
                        $("#unpublish-"+id).hide();
                        if($("#published-icon-"+id).hasClass("glyphicon-check"))
                        {
                           	$("#published-icon-"+id).removeClass("glyphicon-check");
                           	$("#published-icon-"+id).addClass("glyphicon-unchecked");
                        }
                    }
                    else
                    {
                        alert(data.error);
                    }
               }
        });	    
    }

</script>
<%@ include file="footer.jsp" %>
