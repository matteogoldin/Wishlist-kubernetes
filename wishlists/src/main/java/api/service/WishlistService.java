package api.service;

import api.dto.WishlistDTO;
import api.mapper.WishlistMapper;
import daos.WishlistDAO;
import model.Wishlist;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Wishlist.
 * Coordinates DAO and Mapper; has no knowledge of HTTP or Javalin.
 */
public class WishlistService {

    private final WishlistDAO wishlistDAO;

    public WishlistService(WishlistDAO wishlistDAO) {
        this.wishlistDAO = wishlistDAO;
    }

    /** Returns all wishlists as DTOs. */
    public List<WishlistDTO> findAll() {
        List<Wishlist> all = wishlistDAO.getAll();
        return WishlistMapper.toDTOList(all);
    }

    /**
     * Returns a wishlist by name/id.
     *
     * @return empty Optional if not found
     */
    public Optional<WishlistDTO> findById(String id) {
        Wishlist wl = wishlistDAO.findById(id);
        if (wl == null) {
            return Optional.empty();
        }
        return Optional.of(WishlistMapper.toDTO(wl));
    }

    /**
     * Creates a new wishlist.
     *
     * @throws IllegalArgumentException if a wishlist with the same name already exists
     */
    public WishlistDTO create(WishlistDTO dto) {
        if (wishlistDAO.findById(dto.getName()) != null) {
            throw new IllegalArgumentException(
                    String.format("Wishlist '%s' already exists", dto.getName()));
        }
        Wishlist wl = WishlistMapper.toDomain(dto);
        wishlistDAO.add(wl);
        return WishlistMapper.toDTO(wl);
    }

    /**
     * Deletes a wishlist by name/id.
     *
     * @throws IllegalArgumentException if the wishlist does not exist
     */
    public void delete(String id) {
        Wishlist wl = wishlistDAO.findById(id);
        if (wl == null) {
            throw new IllegalArgumentException(
                    String.format("Wishlist '%s' not found", id));
        }
        wishlistDAO.remove(wl);
    }
}
