<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ include file="header.jsp" %>

<div id="bodyColumn">

 	<center>
    <h2><spring:message code="package.edit"/></h2>
    <c:if test="${not empty error}">
	    <div class="errorblock">
	
		    <spring:message code="package.error"/>
		    <br />
		    <spring:message code="general.info"/>
		    ${error}
		
	    </div>
    </c:if>

    <c:url value="/manager/packages/${packageBag.id}/edit" var="editPackage" />
	<form:form method="POST" commandName="packageBag" modelAttribute="packageBag" action="${editPackage}">
    <form:errors path="*" cssStyle="color: red;" />
     <table>
        <tr>
          <td><spring:message code="form.label.name"/></td>
          <td>
            <form:input path="name" size="40" required="true" class="form-control" />
            <form:errors path="name" cssStyle="color: red;" />
          </td>
        </tr>
        <tr>
          <td><spring:message code="form.label.version"/></td>
          <td>
            <form:input path="version" type="email" size="40" required="true" class="form-control" />
            <form:errors path="version" cssStyle="color: red;" />
          </td>
        </tr>
        <tr>
          <td><spring:message code="form.label.login"/></td>
          <td>
            <form:input path="login" size="40" required="true" class="form-control" />
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