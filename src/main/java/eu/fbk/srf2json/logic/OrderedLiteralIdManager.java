package eu.fbk.srf2json.logic;

import eu.fbk.srf2json.dataclasses.commons.types.EnumLiteralDC;
import eu.fbk.srf2json.dataclasses.commons.types.EnumTypeDefinitionDC;

import java.util.*;

public class OrderedLiteralIdManager {
    private static class GraphNode {
        private final String literalValue;
        private final Set<GraphNode> next, previous;

        public GraphNode(String literalValue) {
            super();

            this.literalValue = literalValue;

            this.next = new HashSet<>();
            this.previous = new HashSet<>();
        }

        public void addNext(GraphNode newNext) {
            this.next.add(newNext);
            newNext.previous.add(this);
        }
    }

    private final List<GraphNode> chainStarts;
    private final Map<String, GraphNode> literalToNode;
    private final Map<String, Collection<EnumLiteralDC>> registeredLiterals;

    public OrderedLiteralIdManager() {
        this.chainStarts = new ArrayList<>();
        this.literalToNode = new HashMap<>();
        this.registeredLiterals = new HashMap<>();
    }

    public void registerEnum(EnumTypeDefinitionDC enumDC) {
        GraphNode previousOrderedNode = null;

        Iterable<EnumLiteralDC> iterable = enumDC.getLiteralsStream()::iterator;
        for (EnumLiteralDC literalDC : iterable) {
            String currentLiteralStr = literalDC.getLiteralValue();

            // first we check the ordered literals to find the node if it already exists
            GraphNode existingNode = literalToNode.get(currentLiteralStr);
            if (existingNode != null) {
                if (previousOrderedNode != null) {
                    previousOrderedNode.addNext(existingNode);
                    chainStarts.remove(existingNode);
                }
                previousOrderedNode = existingNode;
            } else {
                // otherwise we create a new (ordered) node
                GraphNode newNode = new GraphNode(currentLiteralStr);
                literalToNode.put(currentLiteralStr, newNode);

                if (previousOrderedNode != null) {
                    previousOrderedNode.addNext(newNode);
                } else {
                    chainStarts.add(newNode);
                }

                previousOrderedNode = newNode;
            }

            Collection<EnumLiteralDC> registeredForLiteralStr = registeredLiterals.computeIfAbsent(currentLiteralStr, k -> new ArrayList<>());
            registeredForLiteralStr.add(literalDC);
        }
    }

    public int assignIds(int startId, Map<String, Integer> alreadyAssigned) {
        int currentId = startId;

        List<GraphNode> queue = new ArrayList<>(chainStarts);
        int queue_index = 0;
        // if we don't have loops in the graph, we'll eventually exit
        while (queue_index < queue.size()) {
            GraphNode node = queue.get(queue_index);
            assignIdsByLiteralValue(node.literalValue, currentId);
            alreadyAssigned.put(node.literalValue, currentId);
            currentId++;

            // now we remove the node from the graph, but we leave the mappings and the chainStarts list untouched
            node.next.forEach(nextNode -> {
                nextNode.previous.remove(node);
                if (nextNode.previous.isEmpty()) {
                    queue.add(nextNode);
                }
            });
            queue_index++;
        }

        return currentId;
    }


    private void assignIdsByLiteralValue(String literalValue, int idToAssign) {
        Collection<EnumLiteralDC> registeredForLiteralStr = registeredLiterals.get(literalValue);
        if (registeredForLiteralStr == null) {
            throw new IllegalStateException("No literal objects found for the following literal value: " + literalValue);
        }
        for (EnumLiteralDC literalDC : registeredForLiteralStr) {
            literalDC.setGlobalValue(idToAssign);
        }
    }
}
