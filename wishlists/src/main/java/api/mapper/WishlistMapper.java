package api.mapper;

import api.dto.ItemDTO;
import api.dto.WishlistDTO;
import model.Item;
import model.Wishlist;

import java.util.List;
import java.util.stream.Collectors;

public class WishlistMapper {

    private WishlistMapper() {}

    public static WishlistDTO toDTO(Wishlist wl) {
        List<ItemDTO> items = wl.getItems().stream()
                .map(WishlistMapper::toDTO)
                .collect(Collectors.toList());
        return new WishlistDTO(wl.getName(), wl.getDesc(), items);
    }

    public static List<WishlistDTO> toDTOList(List<Wishlist> wishlists) {
        return wishlists.stream()
                .map(WishlistMapper::toDTO)
                .collect(Collectors.toList());
    }

    public static Wishlist toDomain(WishlistDTO dto) {
        Wishlist wl = new Wishlist(dto.getName(), dto.getDescription());
        if (dto.getItems() != null) {
            dto.getItems().forEach(itemDTO -> wl.addItem(toDomain(itemDTO)));
        }
        return wl;
    }

    private static ItemDTO toDTO(Item item) {
        return new ItemDTO(item.getName(), item.getDesc(), item.getPrice());
    }

    private static Item toDomain(ItemDTO dto) {
        return new Item(dto.getName(), dto.getDescription(), dto.getPrice());
    }
}

