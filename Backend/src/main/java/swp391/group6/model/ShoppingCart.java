package swp391.group6.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "ShoppingCarts")
public class ShoppingCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "shoppingCart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShoppingCartEntry> items;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<ShoppingCartEntry> getItems() { return items; }
    public void setItems(List<ShoppingCartEntry> items) { this.items = items; }
}
