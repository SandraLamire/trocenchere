<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link href="<%=request.getContextPath()%>/css/styleNewArticle.css" rel="stylesheet"> 
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<script type="text/javascript" src="winAuctionPage.js">
</script>
<title>Nouvelle vente</title>
</head>
<body>
	<header>
		<%@include file="headerFragment.jsp"%>
	</header>

	<h3>Nouvelle vente</h3>
	<fieldset>
		<form action="<%=request.getContextPath()%>/ServletListOfAuctionsPage" method="post">
			<div class="form-group">
				<label for="usr">Article : </label> 
				<input type="text" class="form-control" id="article" name="articleName" value="${article.getNameArticle()} "> <br>
				<br>
				<div class="form-group">
					<label for="desc">Description : </label> 
					<textarea class="form-control" maxlength="300" name="descriptionArticle" id="desc" rows="2"></textarea>
						
					<br><label for="categories">Catégorie:</label> 
						<select name="categorieArticle" id="categories">
							<option value="Toutes">Toutes</option>
							<c:forEach var="category" items="${categoryList}">
								<option value="${category.getWording()}">${category.getWording()}</option>
							</c:forEach>
						</select>
				</div>
			</div>
			<br> <label for="avatar">Photo de l'article</label>
			<div class="avatar">
				<input type="file" id="photoArticle" name="photoArticle" accept="image/png, image/jpeg">
			</div>
			<br>
			<div class="form-group">
				<label>Mise à prix :</label> 
				<input type="number" step="10" value="0" name="valueArticle">
				<br><br> 
				<label for="beginAuction">Début de l'enchère :</label> 
				<input type="date" name="startdateArticle" > 
				<br><br>
				<label for="endAuction">Fin de l'enchère :</label> 
				<input type="date" name="enddateArticle"> 
				<br><br>
				<fieldset>
					<legend>Retrait</legend>
					Rue : <input type="text" name="streetWithdrawal" value="${sessionScope.user.getStreet()}" size="25" maxlength="30"> <br>
					Code postal : <input type="text" name="cpWithdrawal" value="${sessionScope.user.getCp()}" size="18" maxlength="15"> <br>
					Ville : <input type="text" name="cityWithdrawal" value="${sessionScope.user.getCity()}" size="25" maxlength="30">
				</fieldset>

			</div>
			<div><br>
				<input type="submit" value="Ajouter" name="addArticle"> 
				<input type="submit" value="Annuler">
			</div>
		</form>
	</fieldset>
</body>
</html>