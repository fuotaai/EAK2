package bead;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import products.Product;
import products.Product0;
import products.Product1;
import products.Product2;
import products.Product3;
import products.Product4;

/**
 *
 * @author Oliver
 */
public class Controller implements Runnable {

    private final int T3 = 1600;
    private final int T4 = 2500;

    private boolean running = true;

    private Map<Thread, Robot> robots = new HashMap<>();
    private Map<String, Map<String, Integer>> productsNeededForPhases;

    public Controller(int quantity, Map<String, Map<String, Integer>> initialProducts, Map<String, Map<String, Integer>> productsNeededForPhases) {
        this.productsNeededForPhases = productsNeededForPhases;

        createRobots(quantity, initialProducts);
    }
    
    /**
     * Ez mondja meg a robotnak, hogy melyik fázisban melyik termékfajtából
     * hánnyal kell rendelkeznie, hogy termelhessen
     */
    Function<Phase, Map<String, Integer>> productsNeededForProduction = p -> {
        Map<String, Integer> prod = new HashMap<>();
        switch (p) {
            case PHASE1:
                prod.put("Product0", 1);
                break;
            case PHASE2:
                prod.put("Product1", 2);
                break;
            case PHASE3:
                prod.put("Product0", 1);
                prod.put("Product1", 1);
                prod.put("Product2", 3);
                break;
            case PHASE4:
                prod.put("Product2", 1);
                prod.put("Product3", 2);
                break;
        }
        return prod;
    };

    /*
    Létrehozza a robotokat a config fájlban leírt nevekkel és kezdő termékekkel
    Ha a robotok száma nagyobb mint amennyi robot meg lett adva a config fájlban, akkor "Névtelen" nevű robotokat hoz létre, akik nem rendelkeznek kezdő termékkel
    */
    private void createRobots(int quantity, Map<String, Map<String, Integer>> initialProducts) {
        List<Robot> robots = new ArrayList<>();
        IntStream.range(0, quantity).forEach(n -> robots.add(new Robot(Phase.PHASE1, productsNeededForProduction, phase1Producer)));
        initialProducts.keySet().stream().forEach(s -> {
            Robot robot = robots.stream().filter(r -> r.getName().equals("Névtelen")).findFirst().get();
            robot.setName(s);
            Map<String, Integer> productsMap = initialProducts.get(s);
            productsMap.keySet().stream().forEach(p -> {
                robot.addProducts(produceInitialProducts.apply(productsMap.get(p), p));
            });
        });
        
        robots.stream().forEach(r -> {
            Thread thread = new Thread(r);
            thread.start();
            this.robots.put(thread, r);
        });
    }
    
    private int getControllerDelay() {
        return new Random().nextInt(T4 - T3 + 1) + T3;
    }

    /*
    Ellenőrzi, hogy az adott robot rendelkezik-e a szükséges termékekkel, hogy tovább léphessen a következő fázisba
    */
    private Predicate<Robot> hasEnough = r -> {
        List<Product> products = r.getProducts();
        
        return productsNeededForPhases.get(r.getPhase().toString()).entrySet().stream().allMatch(e -> {
            
            synchronized(products) {
                return products.stream().filter(p -> p.getName().equals(e.getKey())).count() >= e.getValue();
            }
        });
    };
    
    /**
     * Kezdeti termékeket gyártó Lambda
     */
    private BiFunction<Integer, String, List<Product>> produceInitialProducts = (n, s) -> {
        List<Product> products = new ArrayList<>();
        IntStream.range(0, n).forEach(i -> {
            try {
                products.add((Product) Class.forName("products." + s).newInstance());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                ex.printStackTrace();
            }
        });
        return products;
    };

    /**
     * Ezeket a gyártó lambdákat használják a robotok a fázisuknak megfelelően,
     * kezdetben Product1-et gyártanak
     */
    Function<List<Product>, Product> phase1Producer = l -> {
        System.out.println(
                l.stream().collect(Collectors.groupingBy(Product::getName, Collectors.counting())) + " -> 1 db Product1"
        );
        return new Product1();
    };

    Function<List<Product>, Product> phase2Producer = l -> {
        System.out.println(
                l.stream().collect(Collectors.groupingBy(Product::getName, Collectors.counting())) + " -> 1 db Product2"
        );
        return new Product2();
    };

    Function<List<Product>, Product> phase3Producer = l -> {
        System.out.println(
                l.stream().collect(Collectors.groupingBy(Product::getName, Collectors.counting())) + " -> 1 db Product3"
        );
        return new Product3();
    };

    Function<List<Product>, Product> phase4Producer = l -> {
        System.out.println(
                l.stream().collect(Collectors.groupingBy(Product::getName, Collectors.counting())) + " -> 1 db Product4"
        );
        return new Product4();
    };
    
    /*
    A következő lambdákkal segít a Vezérlő azoknak a robotoknak akiknek nincs elég termékük, hogy termelhessenek
    Létrehoz i db Product0-át
     */
    public Function<Integer, List<Product>> product0Producer = i -> {
        List<Product> products = new ArrayList<>();
        IntStream.range(0, i).forEach(n -> products.add(new Product0()));
        return products;
    };
    
    /*
    Létrehoz i db Product1-et
    */
    public Function<Integer, List<Product>> product1Producer = i -> {
        List<Product> products = new ArrayList<>();
        IntStream.range(0, i).forEach(n -> products.add(new Product1()));
        return products;
    };

    /*
    Létrehoz i db Product2-őt
    */
    public Function<Integer, List<Product>> product2Producer = i -> {
        List<Product> products = new ArrayList<>();
        IntStream.range(0, i).forEach(n -> products.add(new Product2()));
        return products;
    };

    /*
    Létrehoz i db Product3-at
    */
    public Function<Integer, List<Product>> product3Producer = i -> {
        List<Product> products = new ArrayList<>();
        IntStream.range(0, i).forEach(n -> products.add(new Product3()));
        return products;
    };

    /*
    Létrehoz i db Product4-et
    */
    public Function<Integer, List<Product>> product4Producer = i -> {
        List<Product> products = new ArrayList<>();
        IntStream.range(0, i).forEach(n -> products.add(new Product4()));
        return products;
    };

    /**
     * A Controller ennek a labdának a segítségével ad néhány terméket a
     * robotnak ha az nem léphet a következő fázisba
     */
    public Consumer<Robot> produceProductForRobot = r -> {
        Phase phase = r.getPhase();
        Random random = new Random();
        System.out.println("Vezérlő: " + r.getName() + " robot " + (phase.ordinal()+1) + " darab terméket kap.");
        switch (phase.ordinal() + 1) {
            case 1:
                r.addProducts(random.nextBoolean() ? product0Producer.apply(1) : product1Producer.apply(1));
                break;
            case 2:
                r.addProducts(product0Producer.apply(1));
                r.addProducts(product1Producer.apply(1));
                break;
            case 3:
                r.addProducts(product0Producer.apply(1));
                r.addProducts(product1Producer.apply(1));
                r.addProducts(product2Producer.apply(1));
                break;
            case 4:
                int i = random.nextInt(3) + 1;
                r.addProducts(product3Producer.apply(i));
                r.addProducts(product2Producer.apply(4 - i));
                break;
            default:
                System.err.println("Wrong case!");
                break;
        }
    };

    @Override
    public void run() {
        while (running) {
            if (robots.keySet().stream().anyMatch(t -> t.isAlive())) {
                robots.entrySet().removeIf(e -> !e.getKey().isAlive());

                robots.values().stream().forEach(r -> {
                    if (hasEnough.test(r)) {
                        System.out.println("Vezérlő: " + r.getName() + " robot a " + r.getPhase().toString() + " fázisba léphetett.");
                        r.setPhase(r.getPhase().getNextPhase());
                        switch (r.getPhase()) {
                            case PHASE2:
                                r.setProductionLambda(phase2Producer);
                                break;
                            case PHASE3:
                                r.setProductionLambda(phase3Producer);
                                break;
                            case PHASE4:
                                r.setProductionLambda(phase4Producer);
                                break;
                            default:
                                break;
                        }
                    } else {
                        produceProductForRobot.accept(r);
                    }

                });

                try {
                    TimeUnit.MILLISECONDS.sleep(getControllerDelay());
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

            } else {
                running = false;
            }
        }

        System.out.println("A Vezérlő végzett és leáll. ");
    }
}
