<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ include file="header.jsp" %>

<div id="bodyColumn">
 	<center>
    <h2><spring:message code="repository.create"/></h2>
    <c:if test="${not empty error}">
	    <div class="errorblock">
	
		    <spring:message code="repository.error"/>
		    <br />
		    <spring:message code="general.info"/>
		    ${error}
		
	    </div>
    </c:if>
    <c:url value="/manager/repositories/create" var="createRepository" />
	<form:form method="POST" commandName="repository" modelAttribute="repository" action="${createRepository}" >
      <table>
        <tr>
          <td><spring:message code="form.label.name"/></td>
          <td>
            <form:input path="name" class="form-control" name="name" required="true" size="40" />
            <form:errors path="name" cssStyle="color: red;" />
          </td>
        </tr>
        <tr>
          <td><spring:message code="form.label.publicationuri"/></td>
          <td>
            <form:input path="publicationUri" class="form-control" name="publicationuri" size="40" required="true" />
            <form:errors path="publicationUri" cssStyle="color: red;" />
          </td>
        </tr>
        <tr>
          <td><spring:message code="form.label.serveraddress"/></td>
          <td>
            <form:input path="serverAddress" class="form-control" required="true" name="serveraddress" size="40" />
            <form:errors path="serverAddress" cssStyle="color: red;" />
          </td>
        </tr>
        <tr>
          <td colspan="2" style="text-align: center;">
            <input class="btn btn-info" type="submit" value="<spring:message code="form.button.create"/>"><br/>				
          </td>
        </tr>
      </table>
    </form:form>
    </center>
</div>
<%@ include file="footer.jsp" %>
