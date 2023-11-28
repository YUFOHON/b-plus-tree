package datagenerator;

import Utils.Config;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class DataGenerator {
    public static void main(String[] args) {

        // copy the settings from Config. Don't want to have "Config." everywhere in this class.
        String dataFileName = Config.dataFileName;
        String updatesFileName = Config.updatesFileName;
        String queriesFileName = Config.queriesFileName;
        int count = Config.count;
        int maxNumber = Config.maxNumber;
        double percents = Config.percents;
        double queryRange = Config.queryRange;

        // the seed for generating the updates and query workloads
        List<Integer> seeds = generateRandomSeeds(count, maxNumber);
        writeDataToFile(seeds, dataFileName);

        List<String> updates = generateUpdates(seeds, percents, count);
        writeUpdatesToFile(updates, updatesFileName);

        List<String> queries = generateQueries(seeds, 1000, queryRange);
        writeQueriesToFile(queries, queriesFileName);
    }

    private static List<Integer> generateRandomSeeds(int count, int maxNumber) {
        List<Integer> seeds = new ArrayList<>();

        //use the java random object to help to generate some seeds for generating the workload // just for convenience -- not the only way to do it
        Random random = new Random();

        while (seeds.size() < count) {
            int randomNumber = random.nextInt(maxNumber);
            if (!seeds.contains(randomNumber)) {
                seeds.add(randomNumber);
            }
        }

        seeds.sort(Integer::compareTo);

        return seeds;
    }

    private static List<String> generateUpdates(List<Integer> seeds, double percents, int count) {
        List<String> updates = new ArrayList<>();
        Random random = new Random();

        int maxSeed = seeds.get(seeds.size() - 1);
        Set<Integer> uniqueSeeds = new HashSet<>(seeds);
        Set<Integer> uniqueDeleteSeeds = new HashSet<>();

        List<Integer> seedsCopy = new ArrayList<>(seeds);
        int randomSeed = maxSeed + 1;
        for (int i = 0; i < count; i++) {
            if (random.nextDouble() < percents) {
                // Generate inserts
                int randomIncrease = random.nextInt(20);
                while (uniqueSeeds.contains(randomSeed)) {
                    randomSeed++;
                }
                updates.add("+ " + randomSeed);
                uniqueSeeds.add(randomSeed);
                randomSeed += randomIncrease;
            } else {
                // Generate deletes
                int randomSeedIndex = random.nextInt(seedsCopy.size());
                int randomDeleteSeed = seedsCopy.get(randomSeedIndex);
                while (uniqueDeleteSeeds.contains(randomDeleteSeed)) {
                    randomSeedIndex = random.nextInt(seedsCopy.size());
                    randomDeleteSeed = seedsCopy.get(randomSeedIndex);
                }
                updates.add("- " + randomDeleteSeed);
                uniqueDeleteSeeds.add(randomDeleteSeed);
            }
        }

        return updates;
    }

    private static List<String> generateQueries(List<Integer> seeds, int queryCount, double queryRange) {
        List<String> querySeeds = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < queryCount; i++) {
            int randomQuerySeed = seeds.get(random.nextInt(seeds.size()));
            int randomQueryRange = seeds.get(random.nextInt((int)(seeds.size()*queryRange)));
            querySeeds.add(String.valueOf(randomQuerySeed) + " " +
                    String.valueOf(randomQuerySeed + randomQueryRange));
        }

        return querySeeds;
    }

        // Some helper functions here.
        private static void writeDataToFile(List<Integer> data, String fileName) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                for (Integer d : data) {
                    writer.write(d.toString());
                    writer.newLine();
                }

                System.out.println("Successfully written to " + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private static void writeQueriesToFile(List<String> queries, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (String query : queries) {
                writer.write(query);
                writer.newLine();
            }

            System.out.println("Successfully written to " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeUpdatesToFile(List<String> updates, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (String update : updates) {
                writer.write(update);
                writer.newLine();
            }

            System.out.println("Successfully written to " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
