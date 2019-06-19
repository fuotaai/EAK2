package bead;

import products.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 *
 * @author Oliver
 */
public class Robot implements Runnable {

    private String name;
    private Phase phase;
    private List<Product> products = new ArrayList<>();
    private Function<Phase, Map<String, Integer>> productsNeededForProduction;
    private Function<List<Product>, Product> productionLambda;
    private boolean running = true;
    
    public Robot(Phase phase, Function<Phase, Map<String, Integer>> productsNeededForProduction, Function<List<Product>, Product> productionLambda) {
        this.name = "Névtelen";
        this.phase = phase;
        this.productsNeededForProduction = productsNeededForProduction;
        this.productionLambda = productionLambda;
    }

    private Predicate<String> hasEnough = s -> {
        return products.stream().filter(p -> p.getName().equals(s)).count() >= productsNeededForProduction.apply(phase).get(s);
    };
    
    public void setName(String name) {
        this.name = name;
    }

    public synchronized void setPhase(Phase phase) {
        this.phase = phase;
    }

    public synchronized void addProducts(List<Product> products) {
        products.stream().forEach(p -> {
            synchronized(this.products) {
                this.products.add(p);                
            } 
        });
    }

    public String getName() {
        return name;
    }

    public synchronized Phase getPhase() {
        return phase;
    }

    public synchronized List<Product> getProducts() {
        return products;
    }
    
    public synchronized void setProductionLambda(Function<List<Product>, Product> productionLambda) {
        this.productionLambda = productionLambda;
    }
    

    @Override
    public void run() {
        System.out.println(name + " robot elindult!");
        while (running) {
            if (phase.equals(Phase.DONE)) {
                running = false;
            } else {
                //Az aktuális fázisban a termeléshez szükséges termék - mennyiség párok
                Map<String, Integer> productsNeeded = productsNeededForProduction.apply(phase);
                //Ezeket a termékeket fogja megkapni a gyártó lambda
                List<Product> productsForUse = new ArrayList<>();
                synchronized (products) {
                    if (productsNeeded.keySet().stream().allMatch(hasEnough::test)) {
                        productsNeeded.keySet().stream().forEach(s -> {
                            IntStream.range(0, productsNeeded.get(s)).forEach(i -> {                                
                                productsForUse.add(products.stream()
                                        .filter(p -> p.getName().equals(s))
                                        .findFirst()
                                        .get());
                            });
                        });
                        productsForUse.stream().forEach(products::remove);
                        System.out.println(name + " robot készít egy " + phase.toString() + "-beli terméket.");
                        products.add(productionLambda.apply(productsForUse)); 
                    } else {
                        System.out.println(name + " robot nem tudott termelni.");
                    }
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(phase.getRobotDelay());
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        System.out.println(name + " robot leállt.");
    }
}
