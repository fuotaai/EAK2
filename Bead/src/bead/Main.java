package bead;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Oliver
 */
public class Main {

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        final String fileName = args.length > 0 ? args[0] : "config.txt";

        int numberOfRobots;

        /**
         * A robotok a program inditasakor melyik termekbol hannyal rendelkeznek
         */
        Map<String, Map<String, Integer>> initialProducts;

        /**
         * A fazisokbol valo tovabblepeshez szukseges termekek szama
         */
        Map<String, Map<String, Integer>> productsNeededForPhases;

        Function<String, Map<String, Map<String, Integer>>> parser = s -> {
            return Stream.of(s.split("_"))
                    .map(element -> element.split(";"))
                    .collect(Collectors.toMap(e -> e[0], e -> {
                        return Stream.of(e)
                                .skip(1)
                                .map(prod -> prod.split(","))
                                .collect(Collectors.toMap(p -> p[0], p -> Integer.parseInt(p[1])));
                    }));
        };
        
        List<String> lines = Files.readAllLines(Paths.get(fileName));
        
        numberOfRobots = Integer.parseInt(lines.get(0));
        System.out.println("Robotok száma: " + numberOfRobots);
        initialProducts = parser.apply(lines.get(1));
        System.out.println("Robotok kezdeti termékei:" + "\n" + initialProducts.toString());
        productsNeededForPhases = parser.apply(lines.get(2));
        System.out.println("A fázisokból való továbblépéshez szükséges termékek száma:" + "\n" + productsNeededForPhases.toString());
        
        Controller controller = new Controller(numberOfRobots, initialProducts, productsNeededForPhases);
        Thread thread = new Thread(controller);
        thread.start();
        thread.join();
        System.out.println("A program leáll.");
    }
}
