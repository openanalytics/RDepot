<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ include file="header.jsp" %>

<div id="bodyColumn">

 	<center>
    <h2><spring:message code="user.edit"/></h2>
    <c:if test="${not empty error}">
	    <div class="errorblock">
	
		    <spring:message code="user.error"/>
		    <br />
		    <spring:message code="general.info"/>
		    ${error}
		
	    </div>
    </c:if>

    <c:url value="/manager/users/${user.id}/edit" var="editUser" />
	<form:form method="POST" modelAttribute="user" action="${editUser}">
    <form:errors path="*" cssStyle="color: red;" />
     <table>
        <tr>
          <td><spring:message code="form.label.name"/></td>
          <td>
            <form:input disabled="true" path="name" size="40" required="true" class="form-control" />
            <form:errors path="name" cssStyle="color: red;" />
          </td>
        </tr>
        <tr>
          <td><spring:message code="form.label.email"/></td>
          <td>
            <form:input disabled="true" path="email" type="email" size="40" required="true" class="form-control" />
            <form:errors path="email" cssStyle="color: red;" />
          </td>
        </tr>
        <tr>
          <td><spring:message code="form.label.login"/></td>
          <td>
            <form:input disabled="true" path="login" size="40" required="true" class="form-control" />
            <form:errors path="login" cssStyle="color: red;" />
          </td>
        </tr>
        <tr>
          <td><spring:message code="form.label.role"/></td>
          <td>
            <form:select class="form-control" path="role" items="${roles}" itemValue="description" itemLabel="description" />
          </td>
        </tr>
        <tr>
          <td><spring:message code="form.label.active"/></td>
          <td>
            <form:checkbox path="active" />
          </td>
        </tr>
        <tr>
          <td colspan="2" style="text-align: center;">
            <input class="btn btn-info" type="submit" value="<spring:message code='form.button.update'/>"><br/>				
          </td>
        </tr>
      </table>
    </form:form>
    </center>
</div>
<%@ include file="footer.jsp" %>
