<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ include file="header.jsp" %>

<div id="bodyColumn">
 	<center>
    <h2><spring:message code="repositorymaintainer.edit"/></h2>
    <c:if test="${not empty error}">
	    <div class="errorblock">
	
		    <spring:message code="repositorymaintainer.error"/>
		    <br />
		    <spring:message code="general.info"/>
		    ${error}
		
	    </div>
    </c:if>
    <c:url value="/manager/repositories/maintainers/${repositorymaintainer.id}/edit" var="editRepositoryMaintainer" />
	<form:form method="POST" commandName="repositorymaintainer" modelAttribute="repositorymaintainer" action="${editRepositoryMaintainer}" >
    <form:hidden path="user" value="${repositorymaintainer.user.id}" />
      <table>
        <tr>
          <td><spring:message code="form.label.user"/></td>
          <td>
            <input class="form-control" size="40" value="${repositorymaintainer.user.name}" disabled >
            <form:errors path="user" cssStyle="color: red;" />
          </td>
        </tr>
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
</div>
<%@ include file="footer.jsp" %>
