package benchmark.hardcoded;

import com.x5.template.Theme;
import com.x5.template.Chunk;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class ClassGenerator {

    private final static String BASE_PACKAGE_HIERARCHY = "src.main.java";
    private final static String TEMPLATE_TOP_LEVEL_CLASS_NAME = "top_lvl_class";
    private final static String TEMPLATE_SUBCLASS_NAME = "subclass";
    private final static String EXTENSION_TYPE = "txt";

    private String packagePath;
    private String baseClassName;
    private int maxChildren;
    private int maxDepth;

    /* We'll create one Theme object, since this can be reused */
    private Theme theme;

    /* We'll need a random to add some stochasticity to the generation process */
    private Random random;

    /* We'll store the path to the directory where the .java classes will be stored */
    private String saveDirectoryPath;

    public ClassGenerator(String packageName, int maxChildren, int maxDepth, String baseClassName) {
        this.packagePath = packageName;
        this.maxChildren = maxChildren;
        this.maxDepth = maxDepth;
        this.baseClassName = baseClassName;

        this.random = new Random();
        this.theme = new Theme("src/themes", "");
        this.saveDirectoryPath = (BASE_PACKAGE_HIERARCHY + "." + this.packagePath).replace(".", "/");
    }

    /**
     * args: package hierarchy (last dir is package name), max nr of children per node, max depth (0 indexed)
     * Step 1: Make src directory hierarchy
     * Step 2: Create top lvl class in the package
     * Step 3: Create the other classes as well
     * Step 4: Collect data in an array: List[] -> ArrayList<String>, i.e. level to classes at that level
     */
    public static void main(String[] args) throws IOException {

        if (args.length != 4) {
            System.err.println("Invalid number of arguments!\nUsage: ClassGenerator " +
                    "<destination_package> <max_children> <max_depth> <base_class_name>");

            System.exit(0xFF);
        }

        ClassGenerator generator = new ClassGenerator(
                args[0],                    // The package name (a dot separated list of dir names)
                Integer.parseInt(args[1]),  // The maximal number of direct descendants a class can have
                Integer.parseInt(args[2]),  // The maximal depth of the inheritance tree
                args[3]                     // The base name for the generated classes
        );

        generator.generateClasses();

        // If we got to this place, then it means that no errors have occurred
        System.out.println("The classes have been successfully created.");
    }

    public void generateClasses() throws IOException {
        // Create a directory hierarchy
        new File(this.saveDirectoryPath).mkdirs();

        // We'll start creating the classes using a DFS approach
        String topLevelClassName = this.baseClassName + "0";
        makeTopLevelClass(topLevelClassName);

        int childrenCount = random.nextInt(this.maxChildren) + 1;

        int count = 1;

        for (int i = 0; i < childrenCount; ++i)
            count = generateChildren(count, 1, topLevelClassName, topLevelClassName);

    }

    private int generateChildren(int count, int depth, String parent, String ancestors) throws IOException {
        if (depth <= this.maxDepth) {
            String my_name = this.baseClassName + count++;
            int childrenCount = random.nextInt(this.maxChildren) + 1;

            makeSubclass(my_name, parent, ancestors);

            ancestors += "." + my_name;
            for (int i = 0; i < childrenCount; ++i)
                count = generateChildren(count, depth + 1, my_name, ancestors);
        }

        return count;
    }

    private void makeTopLevelClass(String name) throws IOException {
        Chunk chunk = this.theme.makeChunk(TEMPLATE_TOP_LEVEL_CLASS_NAME, EXTENSION_TYPE);

        chunk.set("package", this.packagePath);
        chunk.set("className", name);

        saveClass(chunk, this.saveDirectoryPath + "/" + name + ".java");
    }

    private void makeSubclass(String name, String parentName, String ancestors) throws IOException {
        Chunk chunk = this.theme.makeChunk(TEMPLATE_SUBCLASS_NAME, EXTENSION_TYPE);

        chunk.set("package", this.packagePath);
        chunk.set("className", name);
        chunk.set("parentClassName", parentName);
        chunk.set("ancestors", ancestors);

        saveClass(chunk, this.saveDirectoryPath + "/" + name + ".java");
    }

    private void saveClass(Chunk chunk, String path) throws IOException {
        FileWriter out = new FileWriter(path);

        chunk.render(out);

        out.flush();
        out.close();
    }

}
