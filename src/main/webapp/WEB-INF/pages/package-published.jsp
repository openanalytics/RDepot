<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ include file="header.jsp" %>

<div id="bodyColumn">

    <c:if test="${not empty error}">
	    <div class="errorblock">
	
		    <spring:message code="general.error"/>
		    <br />
		    <spring:message code="general.info"/>
		    ${error}
		
	    </div>
    </c:if>
    
    <div id="contentBox">
        <div class="PackageDetail">
            <h2>${packageBag.title}</h2>
            <div class="breadcrumb">
                <a href="<c:url value='/manager/repositories' />/${packageBag.repository.id}/published">${packageBag.repository.name}</a> 
                &gt; ${packageBag.title} &gt; 
           </div>
           <p class="description">${packageBag.description}</p>
           <table class="author_info">
               <tr>
                   <td><spring:message code="table.header.author"/></td>
                   <td>${packageBag.author}</td>
               </tr>
               <tr>
                   <td><spring:message code="table.header.maintainer"/></td>
                   <td>${packageBag.user.name} &lt;${packageBag.user.email}&gt;</td>
               </tr>
           </table>
        </div>
        <div class="InstallInstruct">
            <h3><spring:message code="package.title.install"/></h3>
            <p class="install"><spring:message code="package.info.install"/></p>
            <pre>install.packages(&quot;${packageBag.name}&quot;, repos = c(rdepot = &quot;${packageBag.repository.publicationUri}&quot;, getOption(&quot;repos&quot;)))</pre>
        </div>
        <h3><spring:message code="package.title.documentation"/></h3>
        <table class="vignette">
            <tr class="row_odd">
                <td><spring:message code="package.vignettes.none"/></td>
            </tr>
            <tr class="row_even">
                <td><a href="<c:url value='/manager/packages' />/${packageBag.id}/download/${packageBag.name}.pdf"><spring:message code="package.info.referencemanual"/></a></td>
            </tr>
        </table>
        <h3><spring:message code="package.title.details"/></h3>
        <table class="details">
            <tr class="row_odd">
                <th class="alt"><spring:message code="table.header.depends"/></th>
                <td><div class="packages">${packageBag.depends}</div></td>
            </tr>
            <tr class="row_even">
                <th class="alt"><spring:message code="table.header.imports"/></th>
                <td><div class="packages">${packageBag.imports}</div></td>
            </tr>
            <tr class="row_odd">
                 <th class="alt"><spring:message code="table.header.suggests"/></th>
                 <td><div class="packages">${packageBag.suggests}</div></td>
            </tr>
            <tr class="row_even">
                 <th class="alt"><spring:message code="table.header.systemrequirements"/></th>
                 <td><div class="packages">${packageBag.systemRequirements}</div></td>
            </tr>
            <tr class="row_odd">
                 <th class="alt"><spring:message code="table.header.license"/></th>
                 <td><div class="packages">${packageBag.license}</div></td>
            </tr>
            <tr class="row_even">
                 <th class="alt"><spring:message code="table.header.url"/></th>
                 <td><div class="packages">${packageBag.url}</div></td>
            </tr>
        </table>
        <h3><spring:message code="package.title.downloads"/></h3>
        <table class="details">
            <tr class="row_odd">
                <th class="alt"><spring:message code="table.header.source"/></th>
                <td><a href="<c:url value='/manager/packages' />/${packageBag.id}/download/${packageBag.name}_${packageBag.version}.tar.gz">${packageBag.name}_${packageBag.version}.tar.gz</a></td>
            </tr>
            <tr class="row_even">
                <th class="alt"><spring:message code="table.header.binary.windows"/></th>
                <td><spring:message code="package.info.notavailable"/></td>
            </tr>
            <tr class="row_odd">
                <th class="alt"><spring:message code="table.header.binary.macosx"/></th>
                <td><spring:message code="package.info.notavailable"/></td>
            </tr>
        </table>
    </div>		
</div>
<%@ include file="footer.jsp" %>
