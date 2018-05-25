<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ include file="header.jsp" %>

<div id="bodyColumn">
	<h4>An error occured</h4>

	<c:if test="${not empty error}">
		<div class="errorblock">
		
			<spring:message code="general.error"/>
			<br />
			<spring:message code="general.info"/>
			${error}
			
		</div>
	</c:if>		
</div>
<%@ include file="footer.jsp" %>
