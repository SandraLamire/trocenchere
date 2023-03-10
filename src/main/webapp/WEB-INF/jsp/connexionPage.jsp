<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">

<link href="<%=request.getContextPath()%>/css/styleConnexionPage.css"
	rel="stylesheet">
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<title>Connexion</title>
</head>
<body>
	<header>
		<%@include file="headerFragment.jsp"%>
	</header>

	<main>
		<form method="post"
			action="<%=request.getContextPath()%>/ServletListOfAuctionsPage">

			<div class="form-group">
				<label for="usr">Identifiant : </label> <input type="text"
					name="pseudo" value="${userSaved.getPseudo()}" class="form-control"
					id="usr" maxlength="30">
			</div>

			<div class="form-group">
				<label for="pwd">Mot de passe : </label> <input type="password"
					name="password" value="${userSaved.getPassword()}"
					class="form-control" id="pwd" maxlength="30">
			</div>
			<br>
			<br>

			<div class="form-group">
				<br>
				<br> <input type="checkbox" id="connexion" name="souvenir"
					checked> <label for="connexion">Se souvenir de moi</label>
				<input type="submit" name="connexion" value="Connexion"></input>
			</div>
			<br>
			<a href="<%=request.getContextPath()%>/ServletForgetPassword"
			class="forgotPassword">Mot de passe oublié</a> <br>
		</form>
		
			
		<form action="<%=request.getContextPath()%>/ServletRegistrationPage" class="registration">
			<input type="submit" value="Créer un compte">
		</form>
		
	</main>
</body>
</html>