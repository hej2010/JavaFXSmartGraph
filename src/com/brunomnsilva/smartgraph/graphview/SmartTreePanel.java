package com.brunomnsilva.smartgraph.graphview;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.tree.Tree;
import com.brunomnsilva.smartgraph.tree.TreePosition;

import java.util.*;
import java.util.function.Consumer;

/**
 *
 * @param <E>
 */
public class SmartTreePanel<E> extends SmartGraphPanel<E, String> {

    /** reference to tree to display */
    private final Tree<E> tree;
    /** graph representation (actually displayed) of the tree **/
    private Digraph<E, String> graph;

    private Map<TreePosition<E>, Vertex<E>> mapping;

    public SmartTreePanel(Tree<E> tree) {
        super(new DigraphEdgeList<>(), new SmartGraphProperties(), null);
        this.tree = tree;
        initMapping();
    }

    private final void initMapping() {
        /* This is ugly (duplicate call of conversion method),
        but otherwise needs a major refactoring of the panel's class structure */
        this.graph = (Digraph<E, String>)getUnderlyingGraph();
        this.mapping = new HashMap<>();
        //TODO: wrong to call twice; different graph!!!
        tree2digraph(this.tree, this.graph, this.mapping);
        updateNodes();
    }

    private final void syncGraphWithTree() {
        /*
        Note that in a graph representation of a tree, there are no cycles
        in the graph, so when removing a vertex all corresponding tree nodes
        and children are removed from the representation.
        On the other hand, if a tree has a new node, then this node and all
        its descendents are missing from the graph.
         */

        Set<TreePosition<E>> mappedPositions = mapping.keySet();
        Collection<TreePosition<E>> positions = tree.positions();

        List<TreePosition<E>> removedPositions = new LinkedList<>();
        List<TreePosition<E>> insertedPositions = new LinkedList<>();

        for(TreePosition<E> p : mappedPositions) {
            if(!positions.contains(p)) {
                removedPositions.add(p);
                System.out.println("Removed: " + p.element());
            }
        }

        for(TreePosition<E> p :positions) {
            if(!mappedPositions.contains(p)) {
                insertedPositions.add(p);
                System.out.println("Inserted: " + p.element());
            }
        }

        /* TOOD: its not enough, may end with isolated (groups of) vertices */
        for(TreePosition<E> p : removedPositions) {
            Vertex<E> remove = mapping.remove(p);
            graph.removeVertex(remove);
        }

        for(TreePosition<E> p : insertedPositions) {
            TreePosition<E> parent = tree.parent(p);
            Vertex<E> inserted = graph.insertVertex(p.element());
            mapping.put(p, inserted);

            if(parent != null) {
                Vertex<E> parentVertex = mapping.get(parent);
                graph.insertEdge(parentVertex, inserted, UUID.randomUUID().toString());
            }
        }

        /* Update elements at vertices (may have changed) */
        //for(TreePosition<E> p : positions) {
        //    Vertex<E> vertex = mapping.get(p);
        //    graph.replace(vertex, p.element());
        //}
    }

    @Override
    public void update() {
        syncGraphWithTree();
        super.update();
    }

    @Override
    public void updateAndWait() {
        syncGraphWithTree();
        super.updateAndWait();
    }

    @Override
    public void setEdgeDoubleClickAction(Consumer<SmartGraphEdge<String, E>> action) {
        throw new UnsupportedOperationException("This panel does not support this operation.");
    }

    public SmartStylableNode getStylableTreePosition(TreePosition<E> p) {
        Vertex<E> vertex = mapping.get(p);
        return super.getStylableVertex(vertex);
    }

    @Override
    public SmartStylableNode getStylableVertex(Vertex<E> v) {
        throw new UnsupportedOperationException("This panel does not support this operation.");
    }

    @Override
    public SmartStylableNode getStylableEdge(Edge<String, E> edge) {
        throw new UnsupportedOperationException("This panel does not support this operation.");
    }

    @Override
    public SmartStylableNode getStylableEdge(String edgeElement) {
        throw new UnsupportedOperationException("This panel does not support this operation.");
    }


    private static <V> void tree2digraph(Tree<V> tree, Digraph<V,String> digraph, Map<TreePosition<V>, Vertex<V>> mapping) {
        if(tree == null) throw new NullPointerException("Tree cannot be null.");

        if(tree.isEmpty()) return;

        Queue<TreePosition<V>> queueTree = new LinkedList<>();
        Queue<Vertex<V>> queueGraph = new LinkedList<>();

        queueTree.offer(tree.root());

        Vertex<V> vertex = digraph.insertVertex(tree.root().element());
        queueGraph.offer(vertex);

        if(mapping != null) mapping.put(tree.root(), vertex);

        int edgeId = 1;
        while(!queueTree.isEmpty()) {
            TreePosition<V> nodeTree = queueTree.poll();
            Vertex<V> currentThis = queueGraph.poll();

            for(TreePosition<V> childNode : tree.children(nodeTree)) {
                queueTree.offer(childNode);

                Vertex<V> inserted = digraph.insertVertex(childNode.element());
                digraph.insertEdge(currentThis, inserted, UUID.randomUUID().toString());

                if(mapping != null) mapping.put(childNode, inserted);

                queueGraph.offer(inserted);
            }
        }

    }
}
