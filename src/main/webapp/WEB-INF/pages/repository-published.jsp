<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ include file="header.jsp" %>

<div id="bodyColumn">
	<h4>${repository.name}</h4>

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
    
    <table id="packages"> 
        <tr>
            <th><spring:message code="table.header.package"/></th>
            <th><spring:message code="table.header.version"/></th>
            <th><spring:message code="table.header.maintainer"/></th>
            <th><spring:message code="table.header.title"/></th>
        </tr>
        <c:forEach items="${packages}" var="packageBag" >
            <tr>
                <td><a href="<c:url value='/manager/repositories' />/${repository.name}/packages/${packageBag.name}/${packageBag.version}">${packageBag.name}</a></td>
                <td>${packageBag.version}</td>
                <td>${packageBag.user.name}</td>
                <td>${packageBag.title}</td>
            </tr>
        </c:forEach>
    </table>
		
</div>
<%@ include file="footer.jsp" %>
