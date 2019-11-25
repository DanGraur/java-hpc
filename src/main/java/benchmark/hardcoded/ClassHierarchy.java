package benchmark.hardcoded;

import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

public class ClassHierarchy {

    private List<String>[] classHierarchy;
    private String packagePath;

    public ClassHierarchy(String jsonPath, String packagePath) throws FileNotFoundException {
        this.classHierarchy = (new Gson()).fromJson(new FileReader(jsonPath), List[].class);
        this.packagePath = packagePath;
    }

    public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException {
        ClassHierarchy ch = new ClassHierarchy("class_structure.json", "generated.classes");

        for (int i = 0; i < ch.classHierarchy.length; ++i) {
            for (String s : ch.classHierarchy[i])
                System.out.print(s + " ");
            System.out.println();
        }

        Class cls = Class.forName(ch.packagePath + "." + ch.classHierarchy[1].get(0));

        System.out.println(cls);
    }

}
