package benchmark.hardcoded;

import benchmark.hardcoded.types.ArrayListA0;
import com.google.gson.Gson;
import generated.classes.A0;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

enum SamplingStrategy {
    UNIFORM,
    SAME_TOP_LVL
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

    /* TODO: need to see if this also works with Generics, i.e. change hard-coded A0 to T */
    public ArrayList<A0> generateArrayListWorkloadA0(String[] strategy) throws IllegalAccessException,
            InstantiationException, ClassNotFoundException {
        ArrayList<A0> res = new ArrayList<>();
        HashMap<String, Class> cachedClassses = new HashMap<>();

        for (String s : strategy) {
            String className = this.packagePath + "." + s;

            if (!cachedClassses.containsKey(className))
                cachedClassses.put(className, Class.forName(className));

            res.add((A0) cachedClassses.get(className).newInstance());
        }

        return res;
    }

    /* TODO: need to see if this also works with Generics, i.e. change hard-coded A0 to T */
    public ArrayListA0 generateA0ListWorkloadA0(String[] strategy) throws IllegalAccessException,
            InstantiationException, ClassNotFoundException {
        ArrayListA0 res = new ArrayListA0();
        HashMap<String, Class> cachedClassses = new HashMap<>();

        for (String s : strategy) {
            String className = this.packagePath + "." + s;

            if (!cachedClassses.containsKey(className))
                cachedClassses.put(className, Class.forName(className));

            res.add((A0) cachedClassses.get(className).newInstance());
        }

        return res;
    }

    public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        ClassHierarchy classHierarchy = new ClassHierarchy("class_structure.json", "generated.classes");

        long startTime;
        long endTime;

        /* Create the special type ArrayListA0 workloads */
        startTime = System.nanoTime();
        ArrayListA0 sameClassWorkloadA0 = classHierarchy.generateA0ListWorkloadA0(
                classHierarchy.generateStrategy(1000000, SamplingStrategy.SAME_TOP_LVL));
        endTime = System.nanoTime();
        System.out.println("(Custom List, Top Level, Creation) The elapsed time is: " + (endTime - startTime));

        startTime = System.nanoTime();
        ArrayListA0 uniformClassWorkloadA0 = classHierarchy.generateA0ListWorkloadA0(
                classHierarchy.generateStrategy(1000000, SamplingStrategy.UNIFORM));
        endTime = System.nanoTime();
        System.out.println("(Custom List, Uniform, Creation) The elapsed time is: " + (endTime - startTime));

        /* Create the Generic ArrayList workloads */
        startTime = System.nanoTime();
        ArrayList<A0> sameClassWorkload = classHierarchy.generateArrayListWorkloadA0(
                classHierarchy.generateStrategy(1000000, SamplingStrategy.SAME_TOP_LVL));
        endTime = System.nanoTime();
        System.out.println("(Generic List, Top Level, Creation) The elapsed time is: " + (endTime - startTime));

        startTime = System.nanoTime();
        ArrayList<A0> uniformClassWorkload = classHierarchy.generateArrayListWorkloadA0(
                classHierarchy.generateStrategy(1000000, SamplingStrategy.UNIFORM));
        endTime = System.nanoTime();
        System.out.println("(Generic List, Uniform, Creation) The elapsed time is: " + (endTime - startTime));


        startTime = System.nanoTime();
        for (int i = 0; i < sameClassWorkloadA0.size(); ++i)
            sameClassWorkloadA0.get(i).toString();
        endTime = System.nanoTime();
        System.out.println("(Custom List, Top Level, Retrieval) The elapsed time is: " + (endTime - startTime));

        startTime = System.nanoTime();
        for (int i = 0; i < uniformClassWorkloadA0.size(); ++i)
            uniformClassWorkloadA0.get(i).toString();
        endTime = System.nanoTime();
        System.out.println("(Custom List, Uniform, Retrieval) The elapsed time is: " + (endTime - startTime));


        startTime = System.nanoTime();
        for (int i = 0; i < sameClassWorkload.size(); ++i)
            sameClassWorkload.get(i).toString();
        endTime = System.nanoTime();
        System.out.println("(Generic List, Top Level, Retrieval) The elapsed time is: " + (endTime - startTime));

        startTime = System.nanoTime();
        for (int i = 0; i < uniformClassWorkload.size(); ++i)
            uniformClassWorkload.get(i).toString();
        endTime = System.nanoTime();
        System.out.println("(Generic List, Uniform, Retrieval) The elapsed time is: " + (endTime - startTime));

    }

}
