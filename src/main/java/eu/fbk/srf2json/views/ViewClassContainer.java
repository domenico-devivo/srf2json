package eu.fbk.srf2json.views;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ViewClassContainer {
    private final String targetClassName;
    private final Set<String> registeredDeclNames;

    public ViewClassContainer(String targetClassName) {
        this.targetClassName = targetClassName;
        this.registeredDeclNames = new HashSet<>();
    }

    public void registerDeclName(String declName) {
        registeredDeclNames.add(declName);
    }

    public Stream<String> getDeclNamesStream() {
        return registeredDeclNames.stream();
    }

    public Set<String> getDeclNamesSetLowerCased() {
        return registeredDeclNames.stream().filter(Objects::nonNull).map(String::toLowerCase).collect(Collectors.toSet());
    }

    public String toString(String indent) {
        StringBuilder sb = new StringBuilder();
        registeredDeclNames.stream().sorted().forEach(declName -> {
            sb.append(indent);
            sb.append(" Decl ");
            sb.append(declName);
            sb.append("\n");
        });
        return sb.toString();
    }
}
