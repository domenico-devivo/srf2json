package eu.fbk.srf2json.views;

import eu.fbk.srf2json.logic.DefaultDict;

import java.util.List;

public class ViewsContainer {
    // Suggested usage:
    //     private final DefaultDict<String, ViewsContainer> containers;
    //     ...
    //     <constructor> {
    //        this.containers = new DefaultDict<>(ViewsContainer::new);
    //        ...
    //     }

    public static final String LOGIC_VIEW_NAME_PREFIX = "LdS_star";

    private final String sourcePlant;
    private final DefaultDict<String, DefaultDict<String, DefaultDict<String, ViewClassContainer>>> viewClasses = new DefaultDict<>(
            targetPlant -> new DefaultDict<>(       // One entry per referenced plant (`key` is plant name)
                targetView -> new DefaultDict<>(    // One entry per view name (`key` is "LdS_star[_plantName]")
                        ViewClassContainer::new     // One entry per referenced class (`key` is class name)
                )
            )
    );

    public ViewsContainer(String sourcePlant) {
        this.sourcePlant = sourcePlant;
    }

    public DefaultDict<String, ViewClassContainer> getViewClassContainers(String targetPlant) {
        String targetView = resolveViewName(targetPlant);
        return viewClasses.get(targetPlant).get(targetView);
    }

    public String resolveViewName(String targetPlant) {
        return targetPlant.equalsIgnoreCase(sourcePlant)
            ? LOGIC_VIEW_NAME_PREFIX
            : (LOGIC_VIEW_NAME_PREFIX + "_" + targetPlant)
        ;
    }

    public String toString(String targetPlant, String indent) {
        StringBuilder sb = new StringBuilder();
        DefaultDict<String, DefaultDict<String, ViewClassContainer>> secondDict = viewClasses.get(targetPlant);
        List<String> names = List.of(LOGIC_VIEW_NAME_PREFIX, LOGIC_VIEW_NAME_PREFIX + "Linea", LOGIC_VIEW_NAME_PREFIX + "Stazione");
        String nextIndent = indent + "\t ";
        String nextNextIndent = indent + "\t\t";
        for (String name : names) {
            if (secondDict.containsKey(name)) {
                sb.append(indent);
                sb.append(" View ");
                sb.append(name);
                sb.append("\n");
                secondDict.get(name).entrySet().stream().sorted((o1, o2) -> o1.getKey().compareToIgnoreCase(o2.getKey())).forEach(entry ->{
                    sb.append(nextIndent);
                    sb.append(entry.getKey());
                    sb.append("\n");
                    sb.append(entry.getValue().toString(nextNextIndent));
                    sb.append("\n");
                });
            }
        }
        return sb.toString();
    }
}
