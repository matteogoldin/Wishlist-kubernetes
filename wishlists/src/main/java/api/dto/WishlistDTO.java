package api.dto;

import java.util.ArrayList;
import java.util.List;

public class WishlistDTO {

    private String name;
    private String description;
    private List<ItemDTO> items;

    public WishlistDTO() {
        this.items = new ArrayList<>();
    }

    public WishlistDTO(String name, String description, List<ItemDTO> items) {
        this.name = name;
        this.description = description;
        this.items = items != null ? items : new ArrayList<>();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<ItemDTO> getItems() { return items; }
    public void setItems(List<ItemDTO> items) { this.items = items; }
}

