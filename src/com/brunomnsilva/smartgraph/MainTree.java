package com.brunomnsilva.smartgraph;

import com.brunomnsilva.smartgraph.containers.SmartGraphDemoContainer;
import com.brunomnsilva.smartgraph.graph.*;
import com.brunomnsilva.smartgraph.graphview.SmartTreePanel;
import com.brunomnsilva.smartgraph.tree.Tree;
import com.brunomnsilva.smartgraph.tree.TreeImpl;
import com.brunomnsilva.smartgraph.tree.TreePosition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainTree extends Application {

    private volatile boolean running;

    @Override
    public void start(Stage ignored) throws Exception {
        Tree<String> tree = build_tree();

        System.out.println(tree);
        System.out.println("Size: " + tree.size());
        System.out.println("Height: " + tree.height());

        SmartTreePanel<String> treeView = new SmartTreePanel(tree);
        Scene scene = new Scene(new SmartGraphDemoContainer(treeView), 1024, 768);

        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("JavaFX SmartGraph Visualization (Tree)");
        stage.setMinHeight(500);
        stage.setMinWidth(800);
        stage.setScene(scene);
        stage.show();
        treeView.init();

        treeView.getStylableVertex(tree.root().element()).setStyle("-fx-fill: gold; -fx-stroke: brown;");

        /*
        Uncomment lines to test adding of new elements
         */
        continuously_test_adding_elements(tree, treeView);
        stage.setOnCloseRequest(event -> {
            running = false;
        });
    }

    private static Tree<String> build_tree() {
        Tree<String> tree = new TreeImpl<>();

        TreePosition<String> ceo = tree.insert(null, "CEO");
        TreePosition<String> production_manager = tree.insert(ceo, "Production Manager");
        TreePosition<String> personnel_manager = tree.insert(ceo, "Personnel Manager");
        TreePosition<String> sales_manager = tree.insert(ceo, "Sales Manager");
        tree.insert(production_manager, "Purchasing Supervisor");
        tree.insert(production_manager, "Warehouse Supervisor");
        tree.insert(sales_manager, "Shipping Supervisor");

        return tree;
    }

    private static final Random random = new Random(/* seed to reproduce*/);

    private void continuously_test_adding_elements(Tree<String> tree, SmartTreePanel<String> treeView) {
        //update graph
        running = true;
        final long ITERATION_WAIT = 3000; //milliseconds

        Runnable r;
        r = () -> {
            int count = 0;

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

            while (running) {
                try {
                    Thread.sleep(ITERATION_WAIT);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }

                int size = tree.size();
                int randomIndex = random.nextInt(size);

                TreePosition<String> parent = null;
                int i = 0;
                for(TreePosition<String> p : tree.positions()) {
                    if(i == randomIndex) {
                        parent = p;
                        break;
                    }
                    i++;
                }

                String id = String.format("%02d", ++count);
                tree.insert(parent, ("N" + id));

                System.out.println("Random parent: " + parent.element());

                treeView.update();

            }
        };

        new Thread(r).start();
    }

    public static <V> Digraph<V,Integer> tree2digraph(Tree<V> tree) {
        if(tree == null) throw new NullPointerException("Tree cannot be null.");

        Digraph<V, Integer> digraph = new DigraphEdgeList<>();
        if(tree.isEmpty()) return digraph;

        Queue<TreePosition<V>> queueTree = new LinkedList<>();
        Queue<Vertex<V>> queueGraph = new LinkedList<>();

        queueTree.offer(tree.root());

        Vertex<V> vertex = digraph.insertVertex(tree.root().element());

        queueGraph.offer(vertex);

        int edgeId = 1;
        while(!queueTree.isEmpty()) {
            TreePosition<V> nodeTree = queueTree.poll();
            Vertex<V> currentThis = queueGraph.poll();

            for(TreePosition<V> childNode : tree.children(nodeTree)) {
                queueTree.offer(childNode);

                Vertex<V> inserted = digraph.insertVertex(childNode.element());
                digraph.insertEdge(currentThis, inserted, edgeId++);

                queueGraph.offer(inserted);
            }
        }

        return digraph;
    }
}
