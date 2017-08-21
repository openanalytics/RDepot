<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ include file="header.jsp" %>

<div id="bodyColumn">
 	<center>
    <h2><spring:message code="repositorymaintainer.create"/></h2>
    <c:if test="${not empty error}">
	    <div class="errorblock">
	
		    <spring:message code="repositorymaintainer.error"/>
		    <br />
		    <spring:message code="general.info"/>
		    ${error}
		
	    </div>
    </c:if>
    <c:url value="/manager/repositories/maintainers/create" var="createRepositoryMaintainer" />
	<form:form method="POST" commandName="repositorymaintainer" modelAttribute="repositorymaintainer" action="${createRepositoryMaintainer}" >
      <table>
        <tr>
          <td><spring:message code="form.label.user"/></td>
          <td>
            <form:select class="form-control" path="user" items="${users}" itemValue="id" />
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
            <input class="btn btn-info" type="submit" value="<spring:message code="form.button.create"/>"><br/>				
          </td>
        </tr>
      </table>
    </form:form>
    </center>

</div>
<%@ include file="footer.jsp" %>
