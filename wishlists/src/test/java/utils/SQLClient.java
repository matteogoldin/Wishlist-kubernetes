package utils;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import model.Item;
import model.Wishlist;

public class SQLClient {
	EntityManagerFactory emf;

	public SQLClient(String persistenceUnit) {
		emf = Persistence.createEntityManagerFactory(persistenceUnit);
	}

	public void initEmptyDB() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.createQuery("SELECT w FROM Wishlist w", Wishlist.class).getResultList().forEach(wl -> em.remove(wl));
	    em.getTransaction().commit();
		em.close();
	}

	public void insertWishlist(String name, String desc) {
		Wishlist wl = new Wishlist(name, desc);
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.persist(wl);
		em.getTransaction().commit();
		em.close();
	}

	public Wishlist findWishlist(String name) {
		EntityManager em = emf.createEntityManager();
		Wishlist wl = em.find(Wishlist.class, name);
		em.close();
		return wl;
	}


	public Item findItem(String wlName, String itemName) {
		EntityManager em = emf.createEntityManager();
		Item item = em.createQuery("SELECT it FROM Wishlist wl JOIN wl.items it WHERE wl.name = :wl_name AND it.name = :it_name", Item.class)
					.setParameter("wl_name", wlName)
					.setParameter("it_name", itemName)
					.getSingleResult();
		em.close();
		return item;
	}

	public void insertItem(String wlName, String itemName, String itemDesc, float itemPrice) {
		String nativeQuery = "INSERT INTO item (name, description, price, wishlist_name) VALUES (?, ?, ?, ?)";
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.createNativeQuery(nativeQuery)
			.setParameter(1, itemName)
			.setParameter(2, itemDesc)
			.setParameter(3, itemPrice)
			.setParameter(4, wlName)
			.executeUpdate();
		em.getTransaction().commit();
		em.close();
	}

	public List<Item> findAllItemsFromAWL(String wlName) {
		EntityManager em = emf.createEntityManager();
		List<Item> itemList = em.createQuery("SELECT it FROM Wishlist wl JOIN wl.items it WHERE wl.name = :wl_name", Item.class)
				.setParameter("wl_name", wlName)
				.getResultList();
		em.close();
		return itemList;
	}

	public void mergeWishlist(Wishlist wl) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.merge(wl);
		em.getTransaction().commit();
		em.close();
	}
}
