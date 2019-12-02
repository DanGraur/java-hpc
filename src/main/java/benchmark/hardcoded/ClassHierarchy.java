package benchmark.hardcoded;

import benchmark.hardcoded.types.ArrayListA0;
import benchmark.hardcoded.types.ArrayListT;
import com.google.gson.Gson;
import generated.classes.A0;

import java.io.FileNotFoundException;
import java.io.FileReader;
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

    public ArrayListT<A0> generateArrayListWorkloadA0(String[] strategy, HashMap<String, Class> classCache)
            throws IllegalAccessException, InstantiationException {
        ArrayListT<A0> res = new ArrayListT<>(strategy.length);

        for (String s : strategy)
            res.add((A0) classCache.get(s).newInstance());

        return res;
    }

    public ArrayListA0 generateA0ListWorkloadA0(String[] strategy, HashMap<String, Class> classCache)
            throws IllegalAccessException, InstantiationException {
        ArrayListA0 res = new ArrayListA0(strategy.length);

        for (String s : strategy)
            res.add((A0) classCache.get(s).newInstance());

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

    private void updateResultMapEntry(HashMap<String, ArrayList<Long>> resultMap, String key, long newResult) {
        resultMap.get(key).add(newResult);
    }

    public HashMap<String, Tuple<Double, Double>> exectueBenchmarks(int runCount, int warmupRuns,
                                                                    EvaluationType evaluationType, int sampleCount)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        long startTime;
        long time;
        HashMap<String, ArrayList<Long>> scores = new HashMap<>();
        HashMap<String, Class> classCache = populateClassCache();

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
            scores.put("Generic List, Leaf, Creation- 1", new ArrayList<>());

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
                sameClassWorkloadA0 = generateA0ListWorkloadA0(generateStrategy(sampleCount,
                        SamplingStrategy.SAME_TOP_LVL), classCache);


            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_HARDCODED_L ||
                    evaluationType == EvaluationType.GET_HARDCODED_L)
                leafWorkloadA0 = generateA0ListWorkloadA0(generateStrategy(sampleCount,
                    SamplingStrategy.SAME_LAST_LEAF), classCache);

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_HARDCODED_U ||
                    evaluationType == EvaluationType.GET_HARDCODED_U)
                uniformClassWorkloadA0 = generateA0ListWorkloadA0(generateStrategy(sampleCount,
                        SamplingStrategy.UNIFORM), classCache);


            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_GENERIC_TL ||
                    evaluationType == EvaluationType.GET_GENERIC_TL)
                sameClassWorkload = generateArrayListWorkloadA0(generateStrategy(sampleCount,
                        SamplingStrategy.SAME_TOP_LVL), classCache);


            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_GENERIC_L ||
                    evaluationType == EvaluationType.GET_GENERIC_L)
                leafClassWorkload = generateArrayListWorkloadA0(generateStrategy(sampleCount,
                        SamplingStrategy.SAME_LAST_LEAF), classCache);


            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_GENERIC_U ||
                    evaluationType == EvaluationType.GET_GENERIC_U)
                uniformClassWorkload = generateArrayListWorkloadA0(generateStrategy(sampleCount,
                    SamplingStrategy.UNIFORM), classCache);

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
                strategy = generateStrategy(sampleCount, SamplingStrategy.SAME_TOP_LVL);
                startTime = System.nanoTime();
                sameClassWorkload = generateArrayListWorkloadA0(strategy, classCache);
                time = System.nanoTime() - startTime;
                updateResultMapEntry(scores, "Generic List, Top Level, Creation", time);
                System.gc();
            }

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_GENERIC_L ||
                    evaluationType == EvaluationType.GET_GENERIC_L) {
                strategy = generateStrategy(sampleCount, SamplingStrategy.SAME_LAST_LEAF);
                startTime = System.nanoTime();
                leafClassWorkload = generateArrayListWorkloadA0(strategy, classCache);
                time = System.nanoTime() - startTime;
                updateResultMapEntry(scores, "Generic List, Leaf, Creation", time);
                System.gc();
            }

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_GENERIC_U ||
                    evaluationType == EvaluationType.GET_GENERIC_U) {
                strategy = generateStrategy(sampleCount, SamplingStrategy.UNIFORM);
                startTime = System.nanoTime();
                uniformClassWorkload = generateArrayListWorkloadA0(strategy, classCache);
                time = System.nanoTime() - startTime;
                updateResultMapEntry(scores, "Generic List, Uniform, Creation", time);
                System.gc();
            }

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_HARDCODED_TL ||
                    evaluationType == EvaluationType.GET_HARDCODED_TL) {
                strategy = generateStrategy(sampleCount, SamplingStrategy.SAME_TOP_LVL);
                startTime = System.nanoTime();
                sameClassWorkloadA0 = generateA0ListWorkloadA0(strategy, classCache);
                time = System.nanoTime() - startTime;
                updateResultMapEntry(scores, "Custom List, Top Level, Creation", time);
                System.gc();
            }

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_HARDCODED_L ||
                    evaluationType == EvaluationType.GET_HARDCODED_L) {
                strategy = generateStrategy(sampleCount, SamplingStrategy.SAME_LAST_LEAF);
                startTime = System.nanoTime();
                leafWorkloadA0 = generateA0ListWorkloadA0(strategy, classCache);
                time = System.nanoTime() - startTime;
                updateResultMapEntry(scores, "Custom List, Leaf, Creation", time);
                System.gc();
            }

            if (evaluationType == EvaluationType.ALL || evaluationType == EvaluationType.ADD_HARDCODED_U ||
                    evaluationType == EvaluationType.GET_HARDCODED_U) {
                strategy = generateStrategy(sampleCount, SamplingStrategy.UNIFORM);
                startTime = System.nanoTime();
                uniformClassWorkloadA0 = generateA0ListWorkloadA0(strategy, classCache);
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

    public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        ClassHierarchy classHierarchy = new ClassHierarchy("class_structure.json", "generated.classes");

        // This should be the number of experiment runs which are used to warm-up the system, but are not considered
        int warmupRuns = 1;
        // These are the runs which contribute towards the final results
        int runCount = 20;

        /* Run the experiments */
        HashMap<String, Tuple<Double, Double>> results =  classHierarchy.exectueBenchmarks(runCount, warmupRuns,
                EvaluationType.GET_GENERIC_U,1000000);

        /* Print the results */
        for (Map.Entry<String, Tuple<Double, Double>> entry : results.entrySet())
            System.out.println(entry.getKey() + " --> (" + entry.getValue().getFirst() / 10e6 + " ms, " +
                    entry.getValue().getSecond() / 10e6 + " ms)");
    }

}
