
package fr.eni.enchere.dal.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import fr.eni.enchere.bo.Article;
import fr.eni.enchere.bo.User;
import fr.eni.enchere.bo.Withdrawal;
import fr.eni.enchere.dal.ArticleDAO;
import fr.eni.enchere.dal.CodesResultatDAL;
import fr.eni.enchere.dal.ConnectionProvider;
import fr.eni.enchere.dal.DAOFactory;
import fr.eni.enchere.exceptions.BusinessException;

/**
 * Classe en charge de communiquer avec la BDD et la Table ARTICLES_VENDUS
 * @author slamire2022
 * @date 9 janv. 2023 - 15:32:53
 * @version ENI_Encheres - v0.1
 */
public class ArticleDAOJdbcImpl implements ArticleDAO {

	private static final String SQL_SELECT_ALL = 			"SELECT a.no_article, nom_article, description, date_debut_encheres, date_fin_encheres, "
															+ "prix_initial, prix_vente, no_utilisateur, no_categorie, rue, code_postal, ville, image "
															+ "FROM ARTICLES_VENDUS as a "
															+ "INNER JOIN RETRAITS as r on a.no_article=r.no_article";
	
	private static final String SQL_SELECT_BY_ID = 			"SELECT a.no_article, nom_article, description,  date_debut_encheres, date_fin_encheres, "
															+ "prix_initial, prix_vente, no_utilisateur, no_categorie, rue, code_postal, ville, image "
															+ "FROM ARTICLES_VENDUS as a "
															+ "INNER JOIN RETRAITS as r on a.no_article=r.no_article "
															+ "WHERE a.no_article=?";
	
	private static final String SQL_INSERT = 				"INSERT INTO ARTICLES_VENDUS (nom_article, description, date_debut_encheres, date_fin_encheres, "
															+ "prix_initial, prix_vente, no_utilisateur, no_categorie, image) values (?,?,?,?,?,?,?,?,?)";
	
	private static final String SQL_UPDATE = 				"UPDATE ARTICLES_VENDUS SET nom_article=?, description=?, date_debut_encheres=?, date_fin_encheres=?, "
															+ "prix_initial=?, prix_vente=?, no_utilisateur=?, no_categorie=?, image=? "
															+ "WHERE no_article=?";
	
	private static final String SQL_DELETE = 				"DELETE FROM ARTICLES_VENDUS WHERE no_article=?";

	private static final String SQL_INSERT_TO_WITHDRAWAL = 	"INSERT INTO RETRAITS (no_article, rue, code_postal, ville) values(?,?,?,?)";
	
	private static final String SQL_SELECT_BY_NO_CATEGORY = "SELECT a.no_article, nom_article, description, date_debut_encheres, date_fin_encheres, "
															+ "prix_initial, prix_vente, no_utilisateur, no_categorie, rue, code_postal, ville, image "
															+ "FROM ARTICLES_VENDUS as a "
															+ "INNER JOIN RETRAITS as r on a.no_article=r.no_article "
															+ "WHERE no_categorie=?";
	
	private static final String SQL_SELECT_BY_CHAR_NAME = 	"SELECT a.no_article, nom_article, description, date_debut_encheres, date_fin_encheres, "
															+ "prix_initial, prix_vente, no_utilisateur, no_categorie, rue, code_postal, ville, image "
															+ "FROM ARTICLES_VENDUS as a "
															+ "INNER JOIN RETRAITS as r on a.no_article=r.no_article "
															+ "WHERE nom_article LIKE ? ";
	
	private static final String SQL_SELECT_BY_NO_CATEGORY_AND_CHAR_NAME = "SELECT a.no_article, nom_article, description, date_debut_encheres, date_fin_encheres, "
															+ "prix_initial, prix_vente, no_utilisateur, no_categorie, rue, code_postal, ville, image "
															+ "FROM ARTICLES_VENDUS as a "
															+ "INNER JOIN RETRAITS as r on a.no_article=r.no_article "
															+ "WHERE no_categorie=? AND nom_article LIKE ? ";
	
	
	/**
	 * Constructeur
	 */
	public ArticleDAOJdbcImpl() {
	}

	@Override
	public List<Article> selectAll() throws BusinessException {
		List<Article> listeArticles = new ArrayList<>();
		
		try (Connection cnx=ConnectionProvider.getConnection())
		{
			PreparedStatement pstmt = cnx.prepareStatement(SQL_SELECT_ALL);
			ResultSet rs= pstmt.executeQuery();
			Article articleOnGoing = new Article();
			//Tant qu'il y a des donn??es dans le resultSet
			while(rs.next()) {
				//Si l'id de l'article cr??e n'existe pas d??j?? dans la liste
				if (rs.getInt("no_article")!= articleOnGoing.getNoArticle()) {
					//Cr??er l'article
					articleOnGoing = articleBuilder(rs);
					//Et l'ajouter dans la liste d'articles
					listeArticles.add(articleOnGoing);
				}
			}
			rs.close();
			pstmt.close();
			cnx.close();
		} catch (SQLException e) {
			e.printStackTrace();
			//Transformer une SQLExcxeption en businessExcxeption personnalis??e
			BusinessException businessException = new BusinessException();
			businessException.addError(CodesResultatDAL.SELECT_LIST_ARTICLE_FAILED);
			throw businessException;
		}
		return listeArticles;
	}
		
	@Override
	public Article selectById(Integer no_article) throws BusinessException {
		//V??rification si le param??tre est valide
		if(no_article==null || no_article==0) {
			BusinessException businessException = new BusinessException();
			businessException.addError(CodesResultatDAL.INSERT_ID_ARTICLE_NULL);
			throw businessException;
		}
		
		Article articleOnGoing = new Article();
		
		try (Connection cnx=ConnectionProvider.getConnection())
		{
			PreparedStatement pstmt = cnx.prepareStatement(SQL_SELECT_BY_ID);
			pstmt.setInt(1, no_article);
			ResultSet rs= pstmt.executeQuery();
			if (rs.next()) {
				articleOnGoing = articleBuilder(rs);
			}
			rs.close();
			pstmt.close();
			cnx.close();
		}catch(Exception e) {
			e.printStackTrace();
			BusinessException businessException = new BusinessException();
			businessException.addError(CodesResultatDAL.SELECT_ARTICLE_ID_FAILED);
			throw businessException;
		} 
		return articleOnGoing;
	}

	@Override
	public void insert(Article article) throws BusinessException {
		if(article==null) {
			BusinessException businessException = new BusinessException();
			businessException.addError(CodesResultatDAL.INSERT_ARTICLE_NULL);
			throw businessException;
		}
		try(Connection cnx = ConnectionProvider.getConnection())
		{
			try
			{
				// Gestion manuelle du commit
				cnx.setAutoCommit(false);
				PreparedStatement pstmt = null;
				ResultSet rs = null;
				
				if (article.getNoArticle()==0) {
					//Passage de la requ??te au Prepared Statement et r??cup??rer la cl?? g??n??r??e
					pstmt = cnx.prepareStatement(SQL_INSERT, PreparedStatement.RETURN_GENERATED_KEYS);
					//Setter les param??tre de la requ??te SQL
					pstmt.setString(1, article.getNameArticle());
					pstmt.setString(2, article.getDescription());
					//Conversion LocalDate en date sql
					pstmt.setDate(3, java.sql.Date.valueOf(article.getAuctionStartDate()));
					pstmt.setDate(4, java.sql.Date.valueOf(article.getAuctionEndDate()));
					pstmt.setInt(5, article.getOriginalPrice());
					pstmt.setInt(6, article.getSellingPrice());
					pstmt.setInt(7, article.getUser().getNoUser());
					pstmt.setInt(8, article.getCategory().getNoCategory());
					pstmt.setString(9, article.getImageName());
					//Executer la requ??te
					pstmt.executeUpdate();
					//R??cup??rer la cl?? g??n??r??e dans le  ResultSet
					rs = pstmt.getGeneratedKeys();
					//S'il y a une cl??
					if (rs.next()) {
						//Setter le num??ro d'article avec la cl??
						article.setNoArticle(rs.getInt(1));
					}
				}
			rs.close();
			pstmt.close();
			cnx.commit();
			cnx.close();
			
			//Ins??rer le lieu de retrait dans la table Retrait
			insertToWithdrawal(article);
			
			} catch(Exception e)
			{
				e.printStackTrace();
				//Si probl??me dans l'ex??cution de la requ??te, retour de laBDD ?? l'??tat initial
				cnx.rollback();
				// propage potentielle exception en businessException dans catch suivant
				throw e;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			// cr??er une businessException perso
			BusinessException businessException = new BusinessException();
			businessException.addError(CodesResultatDAL.INSERT_ARTICLE_FAILED);
			throw businessException;
		} 
	
	}

	@Override
	public void update(Article article) throws BusinessException {
		if(article==null) {
			BusinessException businessException = new BusinessException();
			businessException.addError(CodesResultatDAL.INSERT_ARTICLE_NULL);
			throw businessException;
		}
		try(Connection cnx = ConnectionProvider.getConnection())
		{
			cnx.setAutoCommit(false);
			PreparedStatement pstmt = null;
			
			try {
				cnx.setAutoCommit(false);
				pstmt = cnx.prepareStatement(SQL_UPDATE);
				
				pstmt.setString(1, article.getNameArticle());
				pstmt.setString(2, article.getDescription());
				// conversion LocalDate en date sql
				pstmt.setDate(3, java.sql.Date.valueOf(article.getAuctionStartDate()));
				pstmt.setDate(4, java.sql.Date.valueOf(article.getAuctionEndDate()));
				pstmt.setInt(5, article.getOriginalPrice());
				pstmt.setInt(6, article.getSellingPrice());
				pstmt.setInt(7, article.getUser().getNoUser());
				pstmt.setInt(8, article.getCategory().getNoCategory());
				pstmt.setString(9, article.getImageName());
				pstmt.setInt(10, article.getNoArticle());
				
				pstmt.executeUpdate();
				pstmt.close();
				cnx.commit();
				cnx.close();
				
			} catch (Exception e) {
				e.printStackTrace();
				cnx.rollback();
				throw e;
			}
		} catch (Exception e) {
			e.printStackTrace();
			BusinessException businessException = new BusinessException();
			businessException.addError(CodesResultatDAL.UPDATE_ARTICLE_FAILED);
			throw businessException;
		}
	}

	@Override
	public void delete(Integer id) throws BusinessException {
		if(id==null || id==0)	{
			BusinessException businessException = new BusinessException();
			businessException.addError(CodesResultatDAL.INSERT_ID_ARTICLE_NULL);
			throw businessException;
		}
		
		try (Connection cnx=ConnectionProvider.getConnection())
		{
			PreparedStatement pstmt = null;
			cnx.prepareStatement(SQL_DELETE);
			
			try {
				cnx.setAutoCommit(false);
				pstmt = cnx.prepareStatement(SQL_DELETE);
				pstmt.setInt(1, id);
				pstmt.executeUpdate();
				
				pstmt.close();
				cnx.commit();
				cnx.close();
				
			} catch (Exception e) {
				e.printStackTrace();
				cnx.rollback();
				throw e;
			}
		}catch(Exception e) {
			e.printStackTrace();
			BusinessException businessException = new BusinessException();
			businessException.addError(CodesResultatDAL.DELETE_ARTICLE_FAILED);
			throw businessException;
		}
	}

	@Override
	public void insertToWithdrawal(Article article) throws BusinessException {
		if(article==null) {
			BusinessException businessException = new BusinessException();
			businessException.addError(CodesResultatDAL.INSERT_ARTICLE_NULL);
			throw businessException;
		}
		try(Connection cnx = ConnectionProvider.getConnection())
		{
			cnx.setAutoCommit(false);
			PreparedStatement pstmt = null;
			try
			{	
				pstmt = cnx.prepareStatement(SQL_INSERT_TO_WITHDRAWAL);
				if (article.getNoArticle()!=0) {
					pstmt.setInt(1, article.getNoArticle());
					pstmt.setString(2, article.getWithdrawal().getStreet());
					pstmt.setString(3, article.getWithdrawal().getCp());
					pstmt.setString(4, article.getWithdrawal().getCity());
				}
				pstmt.executeUpdate();
			pstmt.close();
			cnx.commit();
			cnx.close();
			} catch(Exception e)
			{
				e.printStackTrace();
				cnx.rollback();
				throw e;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			BusinessException businessException = new BusinessException();
			businessException.addError(CodesResultatDAL.INSERT_ARTICLE_TO_WITHDRAWAL_FAILED);
			throw businessException;
		} 
	}

	
	@Override
	public List<Article> selectByNoCategory(Integer noCategory) throws BusinessException {
		//V??rification si le param??tre est valide
		if(noCategory==null || noCategory==0) {
			BusinessException businessException = new BusinessException();
			businessException.addError(CodesResultatDAL.INSERT_ID_ARTICLE_NULL);
			throw businessException;
		}
		//D??claration d'une liste d'articles
		List<Article> listArticles = new ArrayList<>();
		//D??claration d'un Prepared Statement et initialisation ?? null
		PreparedStatement pstmt = null;	
		
		//R??cup??ration d'une connection ?? la BDD	
		try (Connection cnx=ConnectionProvider.getConnection())
		{
			//Passage de la requ??te au Prepared Statement
			pstmt = cnx.prepareStatement(SQL_SELECT_BY_NO_CATEGORY);
			//Setter le param??tre de la requ??te SQL
			pstmt.setInt(1, noCategory);
			//R??cup??ration des informations dans un ResultSet
			ResultSet rs= pstmt.executeQuery();
			//Boucler tant qu'il y a une ligne suivante
			while(rs.next()) {
				//D??claration et instanciation d'un article
				Article articleOngoing = new Article();
				//S??curit??
				if (rs.getInt("no_article") != articleOngoing.getNoArticle()) {
					//G??n??rer un article ?? partir des infos de la BDD
					articleOngoing = articleBuilder(rs);
					//Ajouter cet article ?? la liste d'articles
					listArticles.add(articleOngoing);
				}
			}
			//Fermer le ResultSet
			rs.close();
			//Fermer le Statement
			pstmt.close();
			//Fermer la connection
			cnx.close();
		}catch(Exception e) {
			e.printStackTrace();
			//D??clarer une BusinessException
			BusinessException businessException = new BusinessException();
			//Si il y a une erreur, ajouter l'erreur ?? la BusinessException
			businessException.addError(CodesResultatDAL.SELECT_ARTICLE_CATEGORY_FAILED);
			//Envoyer l'exception
			throw businessException;
		} 
		return listArticles;
	}
	
	@Override
	public List<Article> selectByCharName(String contents) throws BusinessException {
		//V??rification si le param??tre est valide
		if(contents==null || contents.equals("")) {
			BusinessException businessException = new BusinessException();
			businessException.addError(CodesResultatDAL.INSERT_STRING_NULL);
			throw businessException;
		}
		
		//D??claration d'une liste d'articles
		List<Article> listArticles = new ArrayList<>();
		//D??claration d'un Prepared Statement et initialisation ?? null
		PreparedStatement pstmt = null;	
		try (Connection cnx=ConnectionProvider.getConnection())
		{
			//Passage de la requ??te au Prepared Statement
			pstmt = cnx.prepareStatement(SQL_SELECT_BY_CHAR_NAME);
			//Setter le param??tre de la requ??te SQL
			pstmt.setString(1, ("%"+contents+"%"));
			//R??cup??ration des informations dans un ResultSet
			ResultSet rs= pstmt.executeQuery();
			//Boucler tant qu'il y a une ligne suivante
			while(rs.next()) {
				//D??claration et instanciation d'un User
				Article articleOngoing = new Article();
				//S??curit??
				if (rs.getInt("no_article") != articleOngoing.getNoArticle()) {
					//G??n??rer un User ?? partir des infos de la BDD
					articleOngoing = articleBuilder(rs);
					//Ajouter ce User ?? la liste de User
					listArticles.add(articleOngoing);
				}
			}
			//Fermer le ResultSet
			rs.close();
			//Fermer le Statement
			pstmt.close();
			//Fermer la connection
			cnx.close();
		}catch(Exception e) {
			e.printStackTrace();
			//D??clarer une BusinessException
			BusinessException businessException = new BusinessException();
			//Si il y a une erreur, ajouter l'erreur ?? la BusinessException
			businessException.addError(CodesResultatDAL.SELECT_ARTICLE_ID_FAILED);
			//Envoyer l'exception
			throw businessException;
		} 
		return listArticles;
	}
	
	@Override
	public List<Article> selectByNoCategoryAndCharName(Integer noCategory, String contents) throws BusinessException {
		//V??rification si le param??tre noCategory est valide
		if(noCategory==null || noCategory==0) {
			BusinessException businessException = new BusinessException();
			businessException.addError(CodesResultatDAL.INSERT_ID_ARTICLE_NULL);
			throw businessException;
		}
		
		//V??rification si le param??tre contents est valide
		if(contents==null || contents.equals("")) {
			BusinessException businessException = new BusinessException();
			businessException.addError(CodesResultatDAL.INSERT_STRING_NULL);
			throw businessException;
		}
		
		//D??claration d'une liste d'articles
		List<Article> listArticles = new ArrayList<>();
		//D??claration d'un Prepared Statement et initialisation ?? null
		PreparedStatement pstmt = null;	
		try (Connection cnx=ConnectionProvider.getConnection()){
			//Passage de la requ??te au Prepared Statement
			pstmt = cnx.prepareStatement(SQL_SELECT_BY_NO_CATEGORY_AND_CHAR_NAME);
			//Setter le param??tre de la requ??te SQL
			pstmt.setInt(1, noCategory);
			pstmt.setString(2,"%"+contents+"%");
			//R??cup??ration des informations dans un ResultSet
			ResultSet rs= pstmt.executeQuery();
			//Boucler tant qu'il y a une ligne suivante
			while(rs.next()) {
				//D??claration et instanciation d'un User
				Article articleOngoing = new Article();
				//S??curit??
				if (rs.getInt("no_article") != articleOngoing.getNoArticle()) {
					//G??n??rer un User ?? partir des infos de la BDD
					articleOngoing = articleBuilder(rs);
					//Ajouter ce User ?? la liste de User
					listArticles.add(articleOngoing);
				}
			}
			//Fermer le ResultSet
			rs.close();
			//Fermer le Statement
			pstmt.close();
			//Fermer la connection
			cnx.close();
		}catch(Exception e) {
			e.printStackTrace();
			//D??clarer une BusinessException
			BusinessException businessException = new BusinessException();
			//Si il y a une erreur, ajouter l'erreur ?? la BusinessException
			businessException.addError(CodesResultatDAL.SELECT_ARTICLE_ID_FAILED);
			//Envoyer l'exception
			throw businessException;
		} 
		return listArticles;
	}
	
	/**
	 * @param rs
	 * @return
	 * @throws SQLException
	 * @throws BusinessException 
	 */
	private Article articleBuilder(ResultSet rs) throws SQLException, BusinessException {
		Article articleOngoing = new Article();
		articleOngoing.setNoArticle(rs.getInt("no_article"));
		articleOngoing.setNameArticle(rs.getString("nom_article"));
		articleOngoing.setDescription(rs.getString("description"));
		articleOngoing.setAuctionStartDate(rs.getDate("date_debut_encheres").toLocalDate());
		articleOngoing.setAuctionEndDate (rs.getDate("date_fin_encheres").toLocalDate());
		//articleOngoing.setSaleStatus(rs.getString("etat_vente"));
		articleOngoing.setOriginalPrice(rs.getInt("prix_initial"));
		articleOngoing.setSellingPrice( rs.getInt("prix_vente"));
		User user = DAOFactory.getUserDAO().selectById(rs.getInt("no_utilisateur"));

		if (user != null)
	
		articleOngoing.setUser(	DAOFactory.getUserDAO().selectById(rs.getInt("no_utilisateur")));
		articleOngoing.setCategory( DAOFactory.getCategoryDAO().selectById(rs.getInt("no_categorie")));
		
		Withdrawal withdrawalOngoing = new Withdrawal(
				rs.getInt("no_article"),
				rs.getString("rue"),
				rs.getString("code_postal"),
				rs.getString("ville"));
		
		articleOngoing.setWithdrawal(withdrawalOngoing);
		articleOngoing.setImageName(rs.getString("image"));
		
		return articleOngoing;
	}

}
