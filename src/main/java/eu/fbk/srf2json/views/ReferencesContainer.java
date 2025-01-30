package eu.fbk.srf2json.views;

import eu.fbk.srf2json.dataclasses.ClassDC;
import eu.fbk.srf2json.dataclasses.declarations.DeclarationDC;
import eu.fbk.srf2json.dataclasses.declarations.MacroDeclarationDC;
import eu.fbk.srf2json.logic.DefaultDict;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class ReferencesContainer {
    private final DefaultDict<ClassDC, Set<DeclarationReference>> storage = new DefaultDict<>(classDC -> new HashSet<>());
    private final ReferencesResolver referencesResolver;

    public ReferencesContainer(ReferencesResolver referencesResolver) {
        this.referencesResolver = referencesResolver;
    }

    public void registerDeclaration(ClassDC classDC, DeclarationReference declarationRef) {
        storage.get(classDC).add(declarationRef);
    }

    public Set<DeclarationReference> update(ReferencesContainer another) {
        Set<DeclarationReference> res = new HashSet<>();
        for (Map.Entry<ClassDC, Set<DeclarationReference>> entry : another.storage.entrySet()) {
            Set<DeclarationReference> referencesSet = storage.get(entry.getKey());
            for (DeclarationReference declarationRef : entry.getValue()) {
                if (referencesSet.add(declarationRef)) {
                    res.add(declarationRef);
                }
            }
        }
        return res;
    }

    public Stream<ViewListContext> getDeclarationsStream() {
        return storage.entrySet().stream()
                .flatMap(entry -> {
                    return entry.getValue().stream()
                            .map(referencesResolver::resolveDeclaration)
                            .filter(Objects::nonNull)
                            .map(declarationDC -> new ViewListContext(declarationDC.getParsedName(), entry.getKey()))
                    ;
                })
        ;
    }
}
