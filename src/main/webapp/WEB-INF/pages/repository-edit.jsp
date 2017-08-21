<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ include file="header.jsp" %>

<div id="bodyColumn">
 	<center>
    <h2><spring:message code="repository.edit"/></h2>
    <c:if test="${not empty error}">
	    <div class="errorblock">
	
		    <spring:message code="repository.error"/>
		    <br />
		    <spring:message code="general.info"/>
		    ${error}
		
	    </div>
    </c:if>
    <c:url value="/manager/repositories" var="editRepository" />
	<form:form method="POST" commandName="repository" modelAttribute="repository" action="${editRepository}/${repository.id}/edit" >
	  <form:errors path="id" cssStyle="color: red;" />
	  <form:hidden path="version" value="${repository.version}" />
      <table>
        <tr>
          <td><spring:message code="form.label.name"/></td>
          <td>
            <form:input class="form-control" path="name" required="true" type="text" size="40" />
            <form:errors path="name" cssStyle="color: red;" />
          </td>
        </tr>
        <tr>
          <td><spring:message code="form.label.publicationuri"/></td>
          <td>
            <form:input class="form-control" path="publicationUri" required="true" size="40" />
            <form:errors path="publicationUri" cssStyle="color: red;" />
          </td>
        </tr>
        <tr>
          <td><spring:message code="form.label.serveraddress"/></td>
          <td>
            <form:input path="serverAddress" class="form-control" required="true" size="40" />
            <form:errors path="serverAddress" cssStyle="color: red;" />
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
</div>
<%@ include file="footer.jsp" %>
