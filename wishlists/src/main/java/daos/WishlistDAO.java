package daos;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import model.Item;
import model.Wishlist;

public class WishlistDAO extends BaseDAO<Wishlist> {
	private static final Logger LOGGER_WD = LogManager.getLogger(WishlistDAO.class);

	/** Original constructor — used by the Swing app. */
	public WishlistDAO(String persistentUnit) {
		emf = Persistence.createEntityManagerFactory(persistentUnit);
	}

	/**
	 * Constructor with property overrides — used by WishlistRestApp.
	 * Allows runtime overriding of parameters defined in persistence.xml
	 * (e.g. URL, user, password) via container environment variables.
	 *
	 * @param persistentUnit persistence unit name
	 * @param overrides      map of JPA properties to override (may be empty)
	 */
	public WishlistDAO(String persistentUnit, Map<String, String> overrides) {
		emf = Persistence.createEntityManagerFactory(persistentUnit, overrides);
	}

	@Override
	public Wishlist findById(String id) {
		Wishlist result = null;
		openEntityManager();
		try {
			result = em.createQuery("SELECT wl FROM Wishlist wl WHERE wl.name = :id", Wishlist.class)
					.setParameter("id", id)
					.getSingleResult();
		} catch (NoResultException e) {
			LOGGER_WD.info(() -> String.format("No Wishlist found with Id: %s", id));
		} finally {
			em.close();
		}
		return result;
	}

	@Override
	public void add(Wishlist wl) {
		executeInsideTransaction(entitymanager -> entitymanager.persist(wl));
	}

	@Override
	public void remove(Wishlist wl) {
		executeInsideTransaction(entitymanager -> entitymanager.remove(entitymanager.merge(wl)));
	}

	@Override
	public List<Wishlist> getAll() {
		List<Wishlist> result;
		openEntityManager();
		result = em.createQuery("SELECT wl FROM Wishlist wl", Wishlist.class).getResultList();
		em.close();
		return result;
	}

	public List<Item> getAllWlItems(Wishlist wl) {
		List<Item> result;
		openEntityManager();
		result = em.createQuery("SELECT it FROM Wishlist wl JOIN wl.items it WHERE wl.name = :wl_name", Item.class)
				.setParameter("wl_name", wl.getName())
				.getResultList();
		em.close();
		return result;
	}

	public void addItem(Wishlist wl, Item item){
		Wishlist wlPersisted;
		EntityTransaction transaction = null;
		try {
			openEntityManager();
			transaction = em.getTransaction();
			transaction.begin();
			wlPersisted = em.find(Wishlist.class, wl.getName());
			wlPersisted.getItems().add(item);
			transaction.commit();
		} catch (RuntimeException e) {
			LOGGER_WD.error("Error adding Item to Wishlist");
			throw e;
		} finally {
			em.close();
		}
	}

	public void removeItem(Wishlist wl, Item item) {
		Wishlist wlPersisted;
		EntityTransaction transaction = null;
		try {
			openEntityManager();
			transaction = em.getTransaction();
			transaction.begin();
			wlPersisted = em.find(Wishlist.class, wl.getName());
			wlPersisted.getItems().remove(item);
			transaction.commit();
		} catch (RuntimeException e) {
			LOGGER_WD.error("Error removing Item from Wishlist");
			throw e;
		} finally {
			em.close();
		}
	}

}
