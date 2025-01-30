package eu.fbk.srf2json.logic;

import eu.fbk.srf2json.dataclasses.declarations.AttributeDC;
import eu.fbk.srf2json.ClassJsonGeneratorVisitor;
import eu.fbk.srf2json.dataclasses.declarations.DeclarationDC;
import org.apache.commons.collections4.iterators.IteratorChain;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DeclarationIdContainer implements Iterable<DeclarationDC> {
    public static class PredicateListPair implements Iterable<DeclarationDC> {
        private final Predicate<DeclarationDC> predicate;
        private final SortedSet<DeclarationDC> list;

        public PredicateListPair(Predicate<DeclarationDC> predicate) {
            this.predicate = predicate;
            this.list = new TreeSet<>((d1, d2) -> d1.getName().compareToIgnoreCase(d2.getName()));
        }

        public boolean testPredicate(DeclarationDC declarationDC) {
            return this.predicate.test(declarationDC);
        }

        public boolean addElement(DeclarationDC declarationDC) {
            return this.list.add(declarationDC);
        }

        @Override
        public Iterator<DeclarationDC> iterator() {
            return this.list.iterator();
        }
    }

    private final List<PredicateListPair> lists;
    private final Set<String> PRIORITY_TYPOLOGIES;

    public DeclarationIdContainer() {
        this.PRIORITY_TYPOLOGIES = new HashSet<>();
        this.PRIORITY_TYPOLOGIES.add(ClassJsonGeneratorVisitor.JSONKEY_DECL_TYPOLOGY_VARIABILE);
        this.PRIORITY_TYPOLOGIES.add(ClassJsonGeneratorVisitor.JSONKEY_DECL_TYPOLOGY_RESTORE);
        this.PRIORITY_TYPOLOGIES.add(ClassJsonGeneratorVisitor.JSONKEY_DECL_TYPOLOGY_COMANDO_AL_PIAZZALE);
        this.PRIORITY_TYPOLOGIES.add(ClassJsonGeneratorVisitor.JSONKEY_DECL_TYPOLOGY_CONTROLLO_DAL_PIAZZALE);
        this.PRIORITY_TYPOLOGIES.add(ClassJsonGeneratorVisitor.JSONKEY_DECL_TYPOLOGY_COMANDO_DI_OUTPUT);

        this.lists = new ArrayList<>();
        this.lists.add(new PredicateListPair(declarationDC -> (declarationDC instanceof AttributeDC) && ((AttributeDC)declarationDC).isStateVariable()));
        this.lists.add(new PredicateListPair(declarationDC -> declarationDC.checkAttributeTypologyAmong(PRIORITY_TYPOLOGIES)));
        this.lists.add(new PredicateListPair(declarationDC -> declarationDC.checkAttributeTypology(ClassJsonGeneratorVisitor.JSONKEY_DECL_TYPOLOGY_TIMER)));
        this.lists.add(new PredicateListPair(declarationDC -> declarationDC.checkAttributeTypology(ClassJsonGeneratorVisitor.JSONKEY_DECL_TYPOLOGY_COUNTER)));
        this.lists.add(new PredicateListPair(declarationDC -> declarationDC.checkCommandTypology(ClassJsonGeneratorVisitor.JSONKEY_DECL_TYPOLOGY_COMANDO_MANUALE)));
    }

    public void registerDeclarationDC(DeclarationDC declarationDC) {
        lists.stream()
            .filter(predicateListPair -> predicateListPair.testPredicate(declarationDC))
            .findFirst()
            .ifPresent(list -> list.addElement(declarationDC))
        ;
    }

    @Override
    public Iterator<DeclarationDC> iterator() {
        return new IteratorChain<>(lists.stream().map(PredicateListPair::iterator).collect(Collectors.toList()));
    }
}
