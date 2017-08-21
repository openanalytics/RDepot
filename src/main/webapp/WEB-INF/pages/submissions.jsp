<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ include file="header.jsp" %>
<%@ taglib prefix="f" uri="functions.tld"%>

<div id="bodyColumn">
	<h4><spring:message code="submission.title"/></h4>

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
    
    <table id="submissions"> 
        <tr>
            <th><spring:message code="table.header.date"/></th>
            <th><spring:message code="table.header.package"/></th>
            <th><spring:message code="table.header.repository"/></th>
            <th><spring:message code="table.header.submitter"/></th>
            <th><spring:message code="table.header.changes"/></th>
            <th><spring:message code="table.header.accepted"/></th>
            <th><spring:message code="table.header.actions"/></th>
        </tr>
        <c:forEach items="${submissions}" var="submission" >
            <tr id="tr${submission.id}">
                <c:forEach items="${submission.getSubmissionEvents()}" var="event" >
                    <c:if test="${event.getEvent().getValue().equals('create')}">
                        <td>${event.getDate()}</td>
                    </c:if>
                </c:forEach>
                <td>${submission.getPackage().name} ${submission.getPackage().version}</td>
                <td>${submission.getPackage().repository.name}</td>
                <td>${submission.user.name}</td>
                <td>${submission.changes}</td>
                <td><input id="act${submission.id}" type="checkbox" <c:if test="${not f:isAuthorizedToAccept(submission, user)}">disabled="disabled"</c:if><c:if test='${submission.accepted}'>checked="checked" disabled="disabled"</c:if> onchange="acceptSubmission(${submission.id})" ></td>
                <td><input id="del${submission.id}" type="button" class="btn btn-danger" <c:if test='${submission.accepted or not f:isAuthorizedToCancel(submission, user)}'>disabled="disabled"</c:if> onclick="cancelSubmission(${submission.id})" value="<spring:message code='table.actions.cancel'/>"></td>
            </tr>
        </c:forEach>
        <tr>
            <td colspan="7"><a class="btn btn-success" href="<c:url value='/manager' />">+ <spring:message code="menu.general.create"/></a></td>
        </tr>
    </table>
		
</div>
<script type="text/javascript">

    var $dialog = $('<div></div>');
    $dialog.html('<spring:message code="submission.cancel.warning"/>');

    $(document).ready(function(){
        $dialog.dialog({ autoOpen: false, title: "<spring:message code="submission.cancel.title"/>", dialogClass: "alert", modal: true });
    });
    
    function cancelSubmission(id)
    {
        var postUrl = "<c:url value="/manager/submissions" />" + "/" + String(id) + "/cancel";
	    $dialog.dialog( "option", "buttons", { "<spring:message code="dialog.no"/>": function() { $(this).dialog("close"); }, "<spring:message code="dialog.yes"/>": function() { 
	        $(this).dialog("close");	    
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
	       }});
	    $dialog.dialog("open");
    }
    
    function acceptSubmission(id)
    {
        if($("#act" + String(id)).is(":checked") && !$("#act" + String(id)).is(":disabled"))
        {
            var postUrl = "<c:url value="/manager/submissions" />" + "/" + String(id) + "/accept";  
	        $.ajax({
                type: "PUT",
                dataType: 'json',
                url: postUrl,
                success: function(data)
                {
                    if(data.success != null)
                    {
                        alert(data.success);
                        $("#act"+String(id)).attr("disabled", true);
                        $("#act"+String(id)).attr("checked", true);
                        $("#del"+String(id)).attr("disabled", true);
                    }
                    else
                    {
                        alert(data.error);
                    }
                }
            });
        }
    }

</script>
<%@ include file="footer.jsp" %>
