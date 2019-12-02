package benchmark.hardcoded;

import benchmark.hardcoded.types.ArrayListA0;
import benchmark.hardcoded.types.ArrayListT;
import com.google.gson.Gson;
import generated.classes.A0;

import java.io.*;
import java.util.*;

enum EvaluationType {
    ALL,
    ADD_GENERIC_TL,
    ADD_GENERIC_U,
    ADD_GENERIC_L,
    ADD_HARDCODED_TL,
    ADD_HARDCODED_U,
    ADD_HARDCODED_L,
    GET_GENERIC_TL,
    GET_GENERIC_U,
    GET_GENERIC_L,
    GET_HARDCODED_TL,
    GET_HARDCODED_U,
    GET_HARDCODED_L
}

enum SamplingStrategy {
    UNIFORM,
    SAME_TOP_LVL,
    SAME_LAST_LEAF
}

public class ClassHierarchy {
    /* The size of the block which is used during strategy storage */
    private static final int BLOCK_SIZE = 100000;

    private List<String>[] classHierarchy;
    private String packagePath;

    public ClassHierarchy(String jsonPath, String packagePath) throws FileNotFoundException {
        this.classHierarchy = (new Gson()).fromJson(new FileReader(jsonPath), List[].class);
        this.packagePath = packagePath;
    }

    /**
     * Prints the class hierarchy.
     */
    public void printHierarchy() {
        for (int i = 0; i < this.classHierarchy.length; ++i) {
            for (String s : this.classHierarchy[i])
                System.out.print(s + " ");
            System.out.println();
        }
    }

    /**
     * Generate a sequence of classes which will be added to the data structures towards testing
     *
     * @param sampleCount the number of entries to be generated
     * @param strategyType the type of strategy to follow
     * @return an array of Strings containing class names
     */
    public String[] generateStrategy(int sampleCount, SamplingStrategy strategyType) {
        Random random = new Random();
        String[] res = new String[sampleCount];

        if (strategyType == SamplingStrategy.UNIFORM) {
            int classCount = getClassCount();
            int[] samples = random.ints(sampleCount, 0, classCount).toArray();

            // As this is uniform sampling, we can simply concatenate the classHierarchy elements together and sample
            int j = 0;
            String[] classes = new String[classCount];

            for (List<String> s : this.classHierarchy)
                for (String className : s)
                    classes[j++] = className;

            for (int i = 0; i < sampleCount; ++i)
                res[i] = classes[samples[i]];
        } else if (strategyType == SamplingStrategy.SAME_TOP_LVL) {
            for (int i = 0; i < sampleCount; ++i)
                res[i] = this.classHierarchy[0].get(0);
        } else if (strategyType == SamplingStrategy.SAME_LAST_LEAF) {
            int maxDepth = this.classHierarchy.length - 1;
            int maxWidth = this.classHierarchy[this.classHierarchy.length - 1].size() - 1;

            for (int i = 0; i < sampleCount; ++i)
                res[i] = this.classHierarchy[maxDepth].get(maxWidth);
        }

        return res;
    }

    /**
     * Get the total number of generated classes
     *
     * @return the total number of generated classes
     */
    private int getClassCount() {
        Optional<Integer> res = Arrays.stream(classHierarchy).map(List::size).reduce((Integer x, Integer y) -> x + y);
        return res.isPresent() ? res.get() : -1;
    }

    /**
     * Construct a HashMap which points from class names to the classes themselves. This map can then be used
     * in order to quickly lookup and instantiate class instances.
     *
     * @return the HashMap of class names to the classes themselves
     * @throws ClassNotFoundException
     */
    public HashMap<String, Class> populateClassCache() throws ClassNotFoundException {
        HashMap<String, Class> classCache = new HashMap<>();

        for (List<String> s : this.classHierarchy)
            for (String className : s)
                // This condition should normally always yield True, hence the branch will be followed
                if (!classCache.containsKey(className))
                    classCache.put(className, Class.forName(this.packagePath + "." + className));

        return classCache;
    }

    public ArrayListT<A0> generateArrayListWorkloadA0(A0[] objects) {
        ArrayListT<A0> res = new ArrayListT<>(objects.length);

        for (A0 a : objects)
            res.add(a);

        return res;
    }

    public ArrayListA0 generateA0ListWorkloadA0(A0[] objects) {
        ArrayListA0 res = new ArrayListA0(objects.length);

        for (A0 a : objects)
            res.add(a);

        return res;
    }

    /**
     * Compute the mean and standard deviation of a list of integer values.
     *
     * @param times a list of values of the Long type
     * @return a tuple containing the mean on the first position and the standard deviation on the second position
     */
    public Tuple<Double, Double> getMeanAndStdDev(ArrayList<Long> times) {
        int size = times.size();

        double meanVal = times.stream().reduce((Long x, Long y) -> x + y).get() / (double) size;
        double stdDev = Math.sqrt(times.stream().mapToDouble((Long x) -> (Math.pow(x - meanVal, 2.0))).sum() /
                (size)); // (size - 1)

        return new Tuple<>(meanVal, stdDev);
    }

    public static void serializeStrategy(String[] strategy, String path) throws IOException {
        new ObjectOutputStream(new FileOutputStream(path)).writeObject(strategy);
    }

    public static String[] deserializeStrategy(String path) throws IOException, ClassNotFoundException {
        return (String []) new ObjectInputStream(new FileInputStream(path)).readObject();
    }

    /**
     * This method writes a strategy to one or more files and returns it.
     *
     * @param strategy an array of strings which defines the strategy
     * @param path the path to the directory where the strategy will be saved
     * @param fileName the base name of the files which will store the strategy
     * @return the read strategy
     */
    public static void writeStrategy(String[] strategy, String path, String fileName) throws IOException {
        Gson gson = new Gson();

        int parts = strategy.length / BLOCK_SIZE;
        parts += strategy.length % BLOCK_SIZE == 0 ? 0 : 1;

        for (int i = 0; i < parts; ++i) {
            String name = path + "/" + fileName + "_" + i + ".json";
            gson.toJson(Arrays.copyOfRange(strategy, i * BLOCK_SIZE,
                    (i + 1) * BLOCK_SIZE < strategy.length ? (i + 1) * BLOCK_SIZE : strategy.length),
                    new FileWriter(name));
        }
    }

    /**
     * This method reads a strategy from a file and returns it.
     *
     * @param path the path to the file containing the strategy
     * @param baseName the base name of the files storing the strategy
     * @param blockCount the number of files used to store the strategy
     * @return the read strategy
     */
    public static String[] readStrategy(String path, String baseName, int blockCount) throws FileNotFoundException {
        Gson gson = new Gson();
        ArrayList<String> strategy = new ArrayList<>();


        for (int i = 0; i < blockCount; ++i) {
            String name = path + "/" + baseName + "_" + i + ".json";
            strategy.addAll(
                    gson.fromJson(new FileReader(name), ArrayList.class)
            );
        }

        return (String[]) strategy.toArray();
    }

    private A0[] instantiateObjects(String[] strategy, HashMap<String, Class> classCache) throws IllegalAccessException,
            InstantiationException {
        A0[] instantiatedObjects = new A0[strategy.length];

        for (int i = 0; i < strategy.length; ++i)
            instantiatedObjects[i] = (A0) classCache.get(strategy[i]).newInstance();

        return instantiatedObjects;
    }

    /**
     * Updates an entry in a result hash map
     *
     * @param resultMap the hash map
     * @param key the key of the experiment
     * @param newResult the result to be used for updating
     */
    private void updateResultMapEntry(HashMap<String, ArrayList<Long>> resultMap, String key, long newResult) {
        resultMap.get(key).add(newResult);
    }

    /**
     * Method which executes the benchmarks
     *
     * @param runCount the number of runs per experiment
     * @param warmupRuns the number of warmup runs (these are not taken into consideration towards the final result)
     * @param evaluationType the type of experiment being executed
     * @param sampleCount the number of objects to be used as workloads
     * @param uniformStrategy in case a UNIFORM workload is employed, this parameter may be optionally provided
     *                        to avoid creating different uniform strategies
     * @return a hash map of the results
     * @throws ClassNotFoundException
     */
    public HashMap<String, Tuple<Double, Double>> exectueBenchmarks(int runCount, int warmupRuns,
                                                                    EvaluationType evaluationType, int sampleCount,
                                                                    String[] uniformStrategy)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        long startTime;
        long time;
        HashMap<String, ArrayList<Long>> scores = new HashMap<>();
        HashMap<String, A0[]> workloads = new HashMap<>();
        HashMap<String, Class> classCache = populateClassCache();

        /* Populate the workload map */
        if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_HARDCODED_TL ||
                evaluationType == EvaluationType.GET_HARDCODED_TL ||
                evaluationType == EvaluationType.ADD_GENERIC_TL ||
                evaluationType == EvaluationType.GET_GENERIC_TL) {
            A0[] workload = instantiateObjects(generateStrategy(sampleCount, SamplingStrategy.SAME_TOP_LVL),
                    classCache);
            workloads.put("Top Level", workload);

        }

        if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_HARDCODED_L ||
                evaluationType == EvaluationType.GET_HARDCODED_L ||
                evaluationType == EvaluationType.ADD_GENERIC_L ||
                evaluationType == EvaluationType.GET_GENERIC_L) {
            A0[] workload = instantiateObjects(generateStrategy(sampleCount, SamplingStrategy.SAME_LAST_LEAF),
                    classCache);
            workloads.put("Leaf", workload);

        }

        if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_HARDCODED_U ||
                evaluationType == EvaluationType.GET_HARDCODED_U ||
                evaluationType == EvaluationType.ADD_GENERIC_U ||
                evaluationType == EvaluationType.GET_GENERIC_U) {
            A0[] workload = instantiateObjects(uniformStrategy == null ? generateStrategy(
                    sampleCount, SamplingStrategy.UNIFORM) : uniformStrategy, classCache);
            workloads.put("Uniform", workload);
        }

        /* Populate the result map */
        if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_HARDCODED_TL ||
                evaluationType == EvaluationType.GET_HARDCODED_TL)
            scores.put("Custom List, Top Level, Creation", new ArrayList<>());


        if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_HARDCODED_L ||
                evaluationType == EvaluationType.GET_HARDCODED_L)
            scores.put("Custom List, Leaf, Creation", new ArrayList<>());

        if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_HARDCODED_U ||
                evaluationType == EvaluationType.GET_HARDCODED_U)
            scores.put("Custom List, Uniform, Creation", new ArrayList<>());

        if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_GENERIC_TL ||
                evaluationType == EvaluationType.GET_GENERIC_TL)
            scores.put("Generic List, Top Level, Creation", new ArrayList<>());

        if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_GENERIC_L ||
                evaluationType == EvaluationType.GET_GENERIC_L)
            scores.put("Generic List, Leaf, Creation", new ArrayList<>());

        if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_GENERIC_U ||
                evaluationType == EvaluationType.GET_GENERIC_U)
            scores.put("Generic List, Uniform, Creation", new ArrayList<>());

        if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.GET_HARDCODED_TL)
            scores.put("Custom List, Top Level, Retrieval", new ArrayList<>());

        if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.GET_HARDCODED_L)
            scores.put("Custom List, Leaf, Retrieval", new ArrayList<>());

        if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.GET_HARDCODED_U)
            scores.put("Custom List, Uniform, Retrieval", new ArrayList<>());

        if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.GET_GENERIC_TL)
            scores.put("Generic List, Top Level, Retrieval", new ArrayList<>());

        if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.GET_GENERIC_L)
            scores.put("Generic List, Leaf, Retrieval", new ArrayList<>());

        if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.GET_GENERIC_U)
            scores.put("Generic List, Uniform, Retrieval", new ArrayList<>());

        /* These are the actual experiment runs, which count towards the final result */
        for (int i = 0; i < warmupRuns; ++i) {
            ArrayListA0 sameClassWorkloadA0 = null;
            ArrayListA0 leafWorkloadA0 = null;
            ArrayListA0 uniformClassWorkloadA0 = null;
            ArrayListT<A0> sameClassWorkload = null;
            ArrayListT<A0> leafClassWorkload = null;
            ArrayListT<A0> uniformClassWorkload = null;

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_HARDCODED_TL ||
                    evaluationType == EvaluationType.GET_HARDCODED_TL)
                sameClassWorkloadA0 = generateA0ListWorkloadA0(workloads.get("Top Level"));


            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_HARDCODED_L ||
                    evaluationType == EvaluationType.GET_HARDCODED_L)
                leafWorkloadA0 = generateA0ListWorkloadA0(workloads.get("Leaf"));

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_HARDCODED_U ||
                    evaluationType == EvaluationType.GET_HARDCODED_U)
                uniformClassWorkloadA0 = generateA0ListWorkloadA0(workloads.get("Uniform"));


            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_GENERIC_TL ||
                    evaluationType == EvaluationType.GET_GENERIC_TL)
                sameClassWorkload = generateArrayListWorkloadA0(workloads.get("Top Level"));


            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_GENERIC_L ||
                    evaluationType == EvaluationType.GET_GENERIC_L)
                leafClassWorkload = generateArrayListWorkloadA0(workloads.get("Leaf"));

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_GENERIC_U ||
                    evaluationType == EvaluationType.GET_GENERIC_U)
                uniformClassWorkload = generateArrayListWorkloadA0(workloads.get("Uniform"));

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.GET_HARDCODED_TL)
                for (int j = 0; j < sameClassWorkloadA0.size(); ++j) sameClassWorkloadA0.get(i).toString();

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.GET_HARDCODED_L)
                for (int j = 0; j < leafWorkloadA0.size(); ++j) leafWorkloadA0.get(i).toString();

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.GET_HARDCODED_U)
                for (int j = 0; j < uniformClassWorkloadA0.size(); ++j) uniformClassWorkloadA0.get(i).toString();

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.GET_GENERIC_TL)
                for (int j = 0; j < sameClassWorkload.size(); ++j) sameClassWorkload.get(i).toString();

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.GET_GENERIC_L)
                for (int j = 0; j < leafClassWorkload.size(); ++j) leafClassWorkload.get(i).toString();

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.GET_GENERIC_U)
                for (int j = 0; j < uniformClassWorkload.size(); ++j) uniformClassWorkload.get(i).toString();
        }

        // Run a GC call, to help guarantee that the first experiment will be `clean`
        System.gc();

        /* These are the actual experiment runs, which count towards the final result */
        for (int i = 0; i < runCount; ++i) {
            ArrayListA0 sameClassWorkloadA0 = null;
            ArrayListA0 leafWorkloadA0 = null;
            ArrayListA0 uniformClassWorkloadA0 = null;
            ArrayListT<A0> sameClassWorkload = null;
            ArrayListT<A0> leafClassWorkload = null;
            ArrayListT<A0> uniformClassWorkload = null;

            String[] strategy;

            if (i % 20 == 0)
                System.out.println("At iteration " + i + "...");

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_GENERIC_TL ||
                    evaluationType == EvaluationType.GET_GENERIC_TL) {
                startTime = System.nanoTime();
                sameClassWorkload = generateArrayListWorkloadA0(workloads.get("Top Level"));
                time = System.nanoTime() - startTime;
                updateResultMapEntry(scores, "Generic List, Top Level, Creation", time);
                System.gc();
            }

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_GENERIC_L ||
                    evaluationType == EvaluationType.GET_GENERIC_L) {
                startTime = System.nanoTime();
                leafClassWorkload = generateArrayListWorkloadA0(workloads.get("Leaf"));
                time = System.nanoTime() - startTime;
                updateResultMapEntry(scores, "Generic List, Leaf, Creation", time);
                System.gc();
            }

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_GENERIC_U ||
                    evaluationType == EvaluationType.GET_GENERIC_U) {
                startTime = System.nanoTime();
                uniformClassWorkload = generateArrayListWorkloadA0(workloads.get("Uniform"));
                time = System.nanoTime() - startTime;
                updateResultMapEntry(scores, "Generic List, Uniform, Creation", time);
                System.gc();
            }

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_HARDCODED_TL ||
                    evaluationType == EvaluationType.GET_HARDCODED_TL) {
                startTime = System.nanoTime();
                sameClassWorkloadA0 = generateA0ListWorkloadA0(workloads.get("Top Level"));
                time = System.nanoTime() - startTime;
                updateResultMapEntry(scores, "Custom List, Top Level, Creation", time);
                System.gc();
            }

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_HARDCODED_L ||
                    evaluationType == EvaluationType.GET_HARDCODED_L) {
                startTime = System.nanoTime();
                leafWorkloadA0 = generateA0ListWorkloadA0(workloads.get("Leaf"));
                time = System.nanoTime() - startTime;
                updateResultMapEntry(scores, "Custom List, Leaf, Creation", time);
                System.gc();
            }

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_HARDCODED_U ||
                    evaluationType == EvaluationType.GET_HARDCODED_U) {
                startTime = System.nanoTime();
                uniformClassWorkloadA0 = generateA0ListWorkloadA0(workloads.get("Uniform"));
                time = System.nanoTime() - startTime;
                updateResultMapEntry(scores, "Custom List, Uniform, Creation", time);
                System.gc();
            }

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.GET_GENERIC_TL) {
                startTime = System.nanoTime();
                for (int j = 0; j < sameClassWorkload.size(); ++j) sameClassWorkload.get(i).toString();
                time = System.nanoTime() - startTime;
                updateResultMapEntry(scores, "Generic List, Top Level, Retrieval", time);
                System.gc();
            }

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.GET_GENERIC_L) {
                startTime = System.nanoTime();
                for (int j = 0; j < leafClassWorkload.size(); ++j) leafClassWorkload.get(i).toString();
                time = System.nanoTime() - startTime;
                updateResultMapEntry(scores, "Generic List, Leaf, Retrieval", time);
                System.gc();
            }

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.GET_GENERIC_U) {
                startTime = System.nanoTime();
                for (int j = 0; j < uniformClassWorkload.size(); ++j) uniformClassWorkload.get(i).toString();
                time = System.nanoTime() - startTime;
                updateResultMapEntry(scores, "Generic List, Uniform, Retrieval", time);
                System.gc();
            }

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.GET_HARDCODED_TL) {
                startTime = System.nanoTime();
                for (int j = 0; j < sameClassWorkloadA0.size(); ++j) sameClassWorkloadA0.get(i).toString();
                time = System.nanoTime() - startTime;
                updateResultMapEntry(scores, "Custom List, Top Level, Retrieval", time);
                System.gc();
            }

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.GET_HARDCODED_L) {
                startTime = System.nanoTime();
                for (int j = 0; j < leafWorkloadA0.size(); ++j) leafWorkloadA0.get(i).toString();
                time = System.nanoTime() - startTime;
                updateResultMapEntry(scores, "Custom List, Leaf, Retrieval", time);
                System.gc();
            }

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.GET_HARDCODED_U) {
                startTime = System.nanoTime();
                for (int j = 0; j < uniformClassWorkloadA0.size(); ++j) uniformClassWorkloadA0.get(i).toString();
                time = System.nanoTime() - startTime;
                updateResultMapEntry(scores, "Custom List, Uniform, Retrieval", time);
                System.gc();
            }
        }

        HashMap<String, Tuple<Double, Double>> finalScores = new HashMap<>();

        for (Map.Entry<String, ArrayList<Long>> experimentResults : scores.entrySet()) {
            finalScores.put(
                    experimentResults.getKey(),
                    getMeanAndStdDev(experimentResults.getValue())
            );
        }

        return finalScores;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        ClassHierarchy classHierarchy = new ClassHierarchy("class_structure.json", "generated.classes");

        // This should be the number of experiment runs which are used to warm-up the system, but are not considered
        int warmupRuns = 1;
        // These are the runs which contribute towards the final results
        int runCount = 21;

        /* Run the experiments */
        String[] strategy = ClassHierarchy.deserializeStrategy("uniform_strategy.dat");
        HashMap<String, Tuple<Double, Double>> results =  classHierarchy.exectueBenchmarks(runCount, warmupRuns,
                EvaluationType.GET_HARDCODED_U,1000000, strategy);

        /* Print the results */
        for (Map.Entry<String, Tuple<Double, Double>> entry : results.entrySet())
            System.out.println(entry.getKey() + " --> (" + entry.getValue().getFirst() / 10e6 + " ms, " +
                    entry.getValue().getSecond() / 10e6 + " ms)");
    }

}
