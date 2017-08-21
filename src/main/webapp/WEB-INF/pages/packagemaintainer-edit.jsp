<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ include file="header.jsp" %>

<div id="bodyColumn">
 	<center>
    <h2><spring:message code="packagemaintainer.edit"/></h2>
    <c:if test="${not empty error}">
	    <div class="errorblock">
	
		    <spring:message code="packagemaintainer.error"/>
		    <br />
		    <spring:message code="general.info"/>
		    ${error}
		
	    </div>
    </c:if>
    <c:url value="/manager/packages/maintainers/${packagemaintainer.id}/edit" var="editPackageMaintainer" />
	<form:form method="POST" commandName="packagemaintainer" modelAttribute="packagemaintainer" action="${editPackageMaintainer}" >
	<form:hidden path="user" value="${packagemaintainer.user.id}" />
      <table>
        <tr>
          <td><spring:message code="form.label.user"/></td>
          <td>
            <input class="form-control" value="${packagemaintainer.user.name}" size="40" disabled >
            <form:errors path="user" cssStyle="color: red;" />
          </td>
        </tr>
        <tr>
          <td><spring:message code="form.label.package"/></td>
          <td>
            <form:input path="package" id="package" name="package" class="form-control" size="40" list="packages" required="true" />
            <form:errors path="package" cssStyle="color: red;" />
          </td>
        </tr>
        <tr>
          <td><spring:message code="form.label.repository"/></td>
          <td>
            <form:select class="form-control" path="repository" items="${repositories}" itemValue="name" itemLabel="name" />
            <form:errors path="repository" cssStyle="color: red;" />
          </td>
        </tr>
        <tr>
          <td colspan="2" style="text-align: center;">
            <input class="btn btn-info" type="submit" value="<spring:message code="form.button.update"/>"><br/>				
          </td>
        </tr>
      </table>
    </form:form>
    </center>
    <datalist id="packages">
    </datalist>
</div>
<script type="text/javascript">
    
    var json = ${packages};
    
    $("#repository").change(function(){
    
        setSuggestions();
    
    });
    
     $(document).ready(function(){
     
        setSuggestions();
     
     });
     
     function setSuggestions()
     {
        $("#packages").html("");
        $.each(json[$("#repository").val()], function(index, pkg) 
        {  
            $("#packages").append($('<option>', { value: pkg, text: pkg }));
        });
     }
    
</script>
<%@ include file="footer.jsp" %>
