package fr.eni.enchere.dal.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import fr.eni.enchere.bo.User;
import fr.eni.enchere.dal.CodesResultatDAL;
import fr.eni.enchere.dal.ConnectionProvider;
import fr.eni.enchere.dal.UserDAO;
import fr.eni.enchere.exceptions.BusinessException;

/**
 * Classe en charge de communiquer avec la BDD et la Table UTILISATEURS
 * 
 * @author slamire2022
 * @date 9 janv. 2023 - 15:32:30
 * @version ENI_Encheres - v0.1
 */
public class UserDAOJdbcImpl implements UserDAO {

	private static final String SQL_SELECT_ALL = "SELECT no_utilisateur, pseudo, nom, prenom, email, telephone, rue, code_postal, "
			+ "ville, mot_de_passe, credit, administrateur " + "FROM UTILISATEURS";
	private static final String SQL_SELECT_BY_ID = "SELECT no_utilisateur, pseudo, nom, prenom, email, telephone, rue, code_postal, "
			+ "ville, mot_de_passe, credit, administrateur " + "FROM UTILISATEURS WHERE no_utilisateur=?";
	private static final String SQL_INSERT = "INSERT INTO UTILISATEURS (pseudo, nom, prenom, email, telephone, rue, code_postal, "
			+ "ville, mot_de_passe, credit, administrateur) values(?,?,?,?,?,?,?,?,?,?,?)";
	private static final String SQL_UPDATE = "UPDATE UTILISATEURS SET pseudo=?, nom=?, prenom=?, email=?, telephone=?, rue=?, code_postal=?,"
			+ " ville=?, mot_de_passe=?, credit=?, administrateur=? " + "WHERE no_utilisateur=?";
	private static final String SQL_DELETE = "DELETE FROM UTILISATEURS WHERE no_utilisateur=?";

	private static final String SQL_SELECT_BY_PSEUDO_MDP = "SELECT no_utilisateur, pseudo, nom, prenom, email, telephone, rue, code_postal, "
			+ "ville, mot_de_passe, credit, administrateur " + "FROM UTILISATEURS WHERE pseudo=? AND mot_de_passe=?";
	private static final String SQL_SELECT_BY_EMAIL = "SELECT * FROM UTILISATEURS WHERE email = ?;";

	/**
	 * Constructeur
	 */
	public UserDAOJdbcImpl() {
	}

	@Override
	public List<User> selectAll() throws BusinessException {
		// D??claration d'une liste d'utilisateurs
		List<User> listUsers = new ArrayList<>();
		// D??claration d'un Prepared Statement et initialisation ?? null
		PreparedStatement pstmt = null;
		// R??cup??ration d'une connection ?? la BDD
		try (Connection cnx = ConnectionProvider.getConnection()) {
			// Passage de la requ??te au Prepared Statement
			pstmt = cnx.prepareStatement(SQL_SELECT_ALL);
			// R??cup??ration des informations dans un ResultSet
			ResultSet rs = pstmt.executeQuery();
			// Boucler tant qu'il y a une ligne suivante
			while (rs.next()) {
				// D??claration et instanciation d'un User
				User userOngoing = new User();
				// S??curit??
				if (rs.getInt("no_utilisateur") != userOngoing.getNoUser()) {
					// G??n??rer un User ?? partir des infos de la BDD
					userOngoing = userBuilder(rs);
					// Ajouter ce User ?? la liste de User
					listUsers.add(userOngoing);
				}
			}
			// Fermer le ResultSet
			rs.close();
			// Fermer le Statement
			pstmt.close();
			// Fermer la connection
			cnx.close();
		} catch (Exception e) {
			e.printStackTrace();
			// D??clarer une BusinessException
			BusinessException businessException = new BusinessException();
			// Si il y a une erreur, ajouter l'erreur ?? la BusinessException
			businessException.addError(CodesResultatDAL.SELECT_LIST_USER_FAILED);
			// Envoyer l'exception
			throw businessException;
		}
		// Return la liste de tous les utilisateurs
		return listUsers;
	}

	@Override
	public User selectById(Integer id) throws BusinessException {
		// V??rification si le param??tre est valide
		if (id == null || id == 0) {
			BusinessException businessException = new BusinessException();
			businessException.addError(CodesResultatDAL.INSERT_ID_USER_NULL);
			throw businessException;
		}
		// D??claration d'un Prepared Statement et initialisation ?? null
		PreparedStatement pstmt = null;
		// D??claration et instanciation d'un User
		User userOngoing = new User();
		// R??cup??ration d'une connection ?? la BDD
		try (Connection cnx = ConnectionProvider.getConnection()) {
			// Passage de la requ??te au Prepared Statement
			pstmt = cnx.prepareStatement(SQL_SELECT_BY_ID);
			// Setter le param??tre de la requ??te SQL
			pstmt.setInt(1, id);
			// R??cup??ration des informations dans un ResultSet
			ResultSet rs = pstmt.executeQuery();
			// S'il y a une ligne suivante
			if (rs.next() && rs.getInt("no_utilisateur") != userOngoing.getNoUser()) {
				// G??n??rer un User ?? partir des infos de la BDD
				userOngoing = userBuilder(rs);
			}
			// Fermer le ResultSet
			rs.close();
			// Fermer le Statement
			pstmt.close();
			// Fermer la connection
			cnx.close();
		} catch (Exception e) {
			e.printStackTrace();
			// D??clarer une BusinessException
			BusinessException businessException = new BusinessException();
			// Si il y a une erreur, ajouter l'erreur ?? la BusinessException
			businessException.addError(CodesResultatDAL.SELECT_USER_ID_FAILED);
			// Envoyer l'exception
			throw businessException;
		}
		return userOngoing;
	}

	@Override
	public void insert(User user) throws BusinessException {
		// V??rification si le param??tre est valide
		if (user == null) {
			BusinessException businessException = new BusinessException();
			businessException.addError(CodesResultatDAL.INSERT_USER_NULL);
			throw businessException;
		}
		// R??cup??ration d'une connection ?? la BDD
		try (Connection cnx = ConnectionProvider.getConnection()) {
			try {
				// Mettre l'autoCommit ?? false
				cnx.setAutoCommit(false);
				// D??claration d'un Prepared Statement et initialisation ?? null
				PreparedStatement pstmt = null;
				// D??claration d'un ResultSet et initialisation ?? null
				ResultSet rs = null;
				// Si le User n'a pas de no_utilisateur
				if (user.getNoUser() == 0) {
					// Passage de la requ??te au Prepared Statement et r??cup??rer la cl?? g??n??r??e
					pstmt = cnx.prepareStatement(SQL_INSERT, PreparedStatement.RETURN_GENERATED_KEYS);
					// Setter les param??tre de la requ??te SQL
					pstmt.setString(1, user.getPseudo());
					pstmt.setString(2, user.getName());
					pstmt.setString(3, user.getFirstName());
					pstmt.setString(4, user.getEmail());
					pstmt.setString(5, user.getPhone());
					pstmt.setString(6, user.getStreet());
					pstmt.setString(7, user.getCp());
					pstmt.setString(8, user.getCity());
					pstmt.setString(9, user.getPassword());
					pstmt.setInt(10, user.getCredit());
					pstmt.setBoolean(11, user.isAdministrator());
					// Executer la requ??te
					pstmt.executeUpdate();
					// R??cup??rer la cl?? g??n??r??e dans le ResultSet
					rs = pstmt.getGeneratedKeys();
				}
				// S'il y a une cl??
				if (rs.next()) {
					// Setter le num??ro d'utilisateur avec la cl??
					user.setNoUser(rs.getInt(1));
				}
				// Fermer le ResultSet
				rs.close();
				// Fermer le Statement
				pstmt.close();
				// Commit
				cnx.commit();
				// Fermer la connection
				cnx.close();
			} catch (Exception e) {
				e.printStackTrace();
				// Si il y a une erreur rollback et la m??thode n'est pas execut??e
				cnx.rollback();
				// Envoyer l'exception
				throw e;
			}
		} catch (Exception e) {
			e.printStackTrace();
			// D??clarer une BusinessException
			BusinessException businessException = new BusinessException();
			// Si il y a une erreur, ajouter l'erreur ?? la BusinessException
			businessException.addError(CodesResultatDAL.INSERT_USER_FAILED);
			// Envoyer l'exception
			throw businessException;
		}
	}

	@Override
	public void update(User user) throws BusinessException {
		// V??rification si le param??tre est valide
		if (user == null) {
			BusinessException businessException = new BusinessException();
			businessException.addError(CodesResultatDAL.INSERT_USER_NULL);
			throw businessException;
		}
		// R??cup??ration d'une connection ?? la BDD
		try (Connection cnx = ConnectionProvider.getConnection()) {
			// Mettre l'autoCommit ?? false
			cnx.setAutoCommit(false);
			// D??claration d'un Prepared Statement et initialisation ?? null
			PreparedStatement pstmt = null;

			try {
				// Passage de la requ??te au Prepared Statement
				pstmt = cnx.prepareStatement(SQL_UPDATE);
				// Setter les param??tre de la requ??te SQL
				pstmt.setString(1, user.getPseudo());
				pstmt.setString(2, user.getName());
				pstmt.setString(3, user.getFirstName());
				pstmt.setString(4, user.getEmail());
				pstmt.setString(5, user.getPhone());
				pstmt.setString(6, user.getStreet());
				pstmt.setString(7, user.getCp());
				pstmt.setString(8, user.getCity());
				pstmt.setString(9, user.getPassword());
				pstmt.setInt(10, user.getCredit());
				pstmt.setBoolean(11, user.isAdministrator());
				pstmt.setInt(12, user.getNoUser());
				// Executer la requ??te
				pstmt.executeUpdate();

				// Fermer le Statement
				pstmt.close();
				// Commit
				cnx.commit();
				// Fermer la connection
				cnx.close();

			} catch (Exception e) {
				e.printStackTrace();
				// Si il y a une erreur rollback et la m??thode n'est pas execut??e
				cnx.rollback();
				// Envoyer l'exception
				throw e;
			}
		} catch (Exception e) {
			e.printStackTrace();
			// D??clarer une BusinessException
			BusinessException businessException = new BusinessException();
			// Si il y a une erreur, ajouter l'erreur ?? la BusinessException
			businessException.addError(CodesResultatDAL.UPDATE_USER_FAILED);
			// Envoyer l'exception
			throw businessException;
		}
	}

	@Override
	public void delete(Integer id) throws BusinessException {
		// V??rification si le param??tre est valide
		if (id == null || id == 0) {
			BusinessException businessException = new BusinessException();
			businessException.addError(CodesResultatDAL.INSERT_ID_USER_NULL);
			throw businessException;
		}
		// R??cup??ration d'une connection ?? la BDD
		try (Connection cnx = ConnectionProvider.getConnection()) {
			// Mettre l'autoCommit ?? false
			cnx.setAutoCommit(false);
			// D??claration d'un Prepared Statement et initialisation ?? null
			PreparedStatement pstmt = null;

			try {
				// Passage de la requ??te au Prepared Statement
				pstmt = cnx.prepareStatement(SQL_DELETE);
				// Setter les param??tre de la requ??te SQL
				pstmt.setInt(1, id);
				// Executer la requ??te
				pstmt.executeUpdate();

				// Fermer le Statement
				pstmt.close();
				// Commit
				cnx.commit();
				// Fermer la connection
				cnx.close();
			} catch (Exception e) {
				e.printStackTrace();
				// Si il y a une erreur rollback et la m??thode n'est pas execut??e
				cnx.rollback();
				// Envoyer l'exception
				throw e;
			}
		} catch (Exception e) {
			e.printStackTrace();
			// D??clarer une BusinessException
			BusinessException businessException = new BusinessException();
			// Si il y a une erreur, ajouter l'erreur ?? la BusinessException
			businessException.addError(CodesResultatDAL.DELETE_USER_FAILED);
			// Envoyer l'exception
			throw businessException;
		}
	}

	@Override
	public User selectByPseudoMdp(String pseudo, String mdp) throws BusinessException {
		// V??rification si le param??tre est valide
//		if (pseudo == null || "".equals(pseudo) || mdp == null || "".equals(mdp)) {
//			BusinessException businessException = new BusinessException();
//			businessException.addError(CodesResultatDAL.INSERT_PSEUDO_MDP_USER_NULL);
//			throw businessException;
//		}
		// D??claration d'un Prepared Statement et initialisation ?? null
		PreparedStatement pstmt = null;
		// D??claration et instanciation d'un User
		User userOngoing = new User();
		// R??cup??ration d'une connection ?? la BDD
		try (Connection cnx = ConnectionProvider.getConnection()) {
			// Passage de la requ??te au Prepared Statement
			pstmt = cnx.prepareStatement(SQL_SELECT_BY_PSEUDO_MDP);
			// Setter les param??tre de la requ??te SQL
			pstmt.setString(1, pseudo);
			pstmt.setString(2, mdp);
			// R??cup??ration des informations dans un ResultSet
			ResultSet rs = pstmt.executeQuery();
			// V??rification si la requ??te ?? r??cup??rer des infos
			if (rs.next() && rs.getInt("no_utilisateur") != userOngoing.getNoUser()) {
				// G??n??rer un User ?? partir des infos de la BDD
				userOngoing = userBuilder(rs);
			}
			// Fermer le ResultSet
			rs.close();
			// Fermer le Statement
			pstmt.close();
			// Fermer la connection
		} catch (Exception e) {
			e.printStackTrace();
			// D??clarer une BusinessException
			BusinessException businessException = new BusinessException();
			// Si il y a une erreur, ajouter l'erreur ?? la BusinessException
			businessException.addError(CodesResultatDAL.SELECT_USER_PSEUDO_MDP_FAILED);
			// Envoyer l'exception
			throw businessException;
		}
		return userOngoing;
	}

	/**
	 * M??thode qui permet de g??n??rer un objet User ?? partir des infos de la BDD
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private User userBuilder(ResultSet rs) throws SQLException {
		User userOngoing = new User();

		userOngoing.setNoUser(rs.getInt("no_utilisateur"));
		userOngoing.setPseudo(rs.getString("pseudo"));
		userOngoing.setName(rs.getString("nom"));
		userOngoing.setFirstName(rs.getString("prenom"));
		userOngoing.setEmail(rs.getString("email"));
		userOngoing.setPhone(rs.getString("telephone"));
		userOngoing.setStreet(rs.getString("rue"));
		userOngoing.setCp(rs.getString("code_postal"));
		userOngoing.setCity(rs.getString("ville"));
		userOngoing.setPassword(rs.getString("mot_de_passe"));
		userOngoing.setCredit(rs.getInt("credit"));
		userOngoing.setAdministrator(rs.getBoolean("administrateur"));
		return userOngoing;
	}

	@Override
	public User selectByEmail(String email) {
		// D??claration d'un Prepared Statement et initialisation ?? null
		PreparedStatement pstmt = null;
		// D??claration et instanciation d'un User
		User userOngoing = null;
		// R??cup??ration d'une connection ?? la BDD
		try (Connection cnx = ConnectionProvider.getConnection()) {
			// Passage de la requ??te au Prepared Statement
			pstmt = cnx.prepareStatement(SQL_SELECT_BY_EMAIL);
			// Setter les param??tre de la requ??te SQL
			pstmt.setString(1, email);
			// R??cup??ration des informations dans un ResultSet
			ResultSet rs = pstmt.executeQuery();
			// V??rification si la requ??te ?? r??cup??rer des infos
			while ( rs.next() ) {
				
				// G??n??rer un User ?? partir des infos de la BDD
				userOngoing = new User();
				userOngoing.setNoUser(rs.getInt("no_utilisateur"));
				userOngoing.setPseudo(rs.getString("pseudo"));
				userOngoing.setName(rs.getString("nom"));
				userOngoing.setFirstName(rs.getString("prenom"));
				userOngoing.setEmail(rs.getString("email"));
				userOngoing.setPhone(rs.getString("telephone"));
				userOngoing.setStreet(rs.getString("rue"));
				userOngoing.setCp(rs.getString("code_postal"));
				userOngoing.setCity(rs.getString("ville"));
				userOngoing.setPassword(rs.getString("mot_de_passe"));
				userOngoing.setCredit(rs.getInt("credit"));
				userOngoing.setAdministrator(rs.getBoolean("administrateur"));
			}
			// Fermer le ResultSet
			rs.close();
			// Fermer le Statement
			pstmt.close();
			// Fermer la connection
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userOngoing;
	}
}
