package products;

/**
 *
 * @author Oliver
 */
public class Product {
    private final String name;
    
    public Product() {
        this.name = this.getClass().getSimpleName();
    }
    
    public String getName() {
        return name;
    }    
}
