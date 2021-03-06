/*
 * Find trivial inclusions, generate node info.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/util/LandMark.java,v 2.0 2011/09/25 12:33:43 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.core.util;

import java.io.*;
import java.util.*;
import java.net.URL;
import java.awt.image.BufferedImage;
import java.awt.Dimension;
import javax.imageio.ImageIO;
import org.xml.sax.InputSource;
import gnu.getopt.Getopt;
import org.jgrapht.graph.SimpleDirectedGraph;

import teo.isgci.core.App;
import teo.isgci.core.IDataProvider;
import teo.isgci.data.db.*;
import teo.isgci.data.gc.*;
import teo.isgci.data.grapht.*;
import teo.isgci.data.problem.*;
import teo.isgci.data.xml.*;
import teo.isgci.view.gui.*;
import teo.Loader;

public class LandMark {
    /** Where we check for relations */
    static SimpleDirectedGraph<GraphClass,Inclusion> thegraph;

    /** Creating the map images is done as part of the application */
    ISGCIMainFrame parent;
    /** The landmark classes. The first element is to be replaced by the
     * class to be landmarked */
    List<GraphClass> landmarks;
    /** Where to store the maps */
    String path;

    public LandMark(ISGCIMainFrame parent) {
        this.parent = parent;
        landmarks = findLandmarks();
        path = System.getProperty("org.isgci.mappath");
    }

    /**
     * Fill the landmarks array with the proper nodes.
     */
    protected List<GraphClass> findLandmarks() {
    	IDataProvider dataProvider = App.DataProvider;
        List<GraphClass> landmarks = new ArrayList<GraphClass>();
        landmarks.add(null);
        landmarks.add(dataProvider.getClass("perfect"));
        landmarks.add(dataProvider.getClass("Meyniel"));
        landmarks.add(dataProvider.getClass("threshold"));
        landmarks.add(dataProvider.getClass("tree"));
        landmarks.add(dataProvider.getClass("cograph"));
        landmarks.add(dataProvider.getClass("clique"));
        landmarks.add(dataProvider.getClass("proper interval"));
        landmarks.add(dataProvider.getClass("even-hole--free"));
        return landmarks;
    }


    /**
     * Print node/edge count statistics of the given graph.
     */
    public static void show(SimpleDirectedGraph dg){
        System.err.print("Nodes: "+ dg.vertexSet().size());
        System.err.println("     Edges: "+ dg.vertexSet().size());
    }


    /**
     * Load the ISGCI database.
     */
    public static void load(String file,
            SimpleDirectedGraph<GraphClass,Inclusion> graph, Vector problems)
            throws java.net.MalformedURLException {
        Loader loader = new Loader("file:"+System.getProperty("user.dir")+"/",
                true);
        ISGCIReader gcr = new ISGCIReader(graph, problems);
        XMLParser xml=new XMLParser(loader.openInputSource(file),
                gcr, loader.new Resolver(), new NoteFilter());
        xml.parse();
    }


    /**
     * Split the given graph on the given graphclass and return the results
     * [g \cap supers, g \cap subs, g \cap others].
     */
    /*FIXME port to jgrapht
    private static ArrayList<ISGCIGraph> split(ISGCIGraph g,
            Vector supers, Vector subs) {
        ISGCIGraph.ISGCINode n;
        Enumeration eenum;
        ArrayList<ISGCIGraph> res = new ArrayList<ISGCIGraph>(3);

        Vector gsupers = new Vector();
        Vector gsubs = new Vector();
        Vector grest = new Vector();

        eenum = g.getNodes();
        while (eenum.hasMoreElements()) {
            n = (ISGCIGraph.ISGCINode) eenum.nextElement();
            if (supers.contains(n.getUltimateParent()))
                gsupers.addElement(n);
            else if (subs.contains(n.getUltimateParent()))
                gsubs.addElement(n);
            else
                grest.addElement(n);
        }

        res.add(null);
        res.add(null);
        res.add(null);
        res.set(0, (ISGCIGraph) g.createSubgraph(gsupers.elements()));
        res.set(1, (ISGCIGraph) g.createSubgraph(gsubs.elements()));
        res.set(2, (ISGCIGraph) g.createSubgraph(grest.elements()));
        return res;
    }*/


    /**
     * Split the given graphs on the given graphclass and return the results.
     */
    /*FIXME port to jgrapht
    private static ArrayList<ISGCIGraph> split(String gc,
            ArrayList<ISGCIGraph> graphs) {
        ArrayList<ISGCIGraph> res = new ArrayList<ISGCIGraph>();

        //---- Find super/subs of splitter
        ISGCIGraph.ISGCINode splitter = null, n;
        Enumeration eenum = thegraph.getNodes();

        while (eenum.hasMoreElements()) {
            splitter = (ISGCIGraph.ISGCINode) eenum.nextElement();
            if (splitter.getName().equals(gc))
                break;
        }

        Vector supers = splitter.superNodes();
        Vector subs = splitter.subNodes();

        for (ISGCIGraph g: graphs) {
            ArrayList<ISGCIGraph> gs = split(g, supers, subs);
            for (int i = 0; i < 3; i++)
                if (gs.get(i) != null  &&  gs.get(i).countNodes() > 20)
                    res.add(gs.get(i));
        }

        return res;
    }*/


    /**
     * Print number of super/subclasses of the classes in g.
     */
    /*FIXME port to jgrapht
    private static void showClasses(ISGCIGraph g) {
        Enumeration eenum = thegraph.getNodes();
        while (eenum.hasMoreElements()) {
            ISGCIGraph.ISGCINode n= (ISGCIGraph.ISGCINode) eenum.nextElement();
            if (n.getGraphClass().getClass() != BaseClass.class)
                continue;

            Vector supers = n.superNodes();
            Vector subs = n.subNodes();
            int supn = 0, subn = 0, restn = 0;

            Enumeration gnodes = g.getNodes();
            while (gnodes.hasMoreElements()) {
                ISGCIGraph.ISGCINode gn =
                        (ISGCIGraph.ISGCINode) gnodes.nextElement();
                if (supers.contains(gn.getUltimateParent()))
                    supn++;
                else if (subs.contains(gn.getUltimateParent()))
                    subn++;
                else
                    restn++;
            }

            System.out.print(restn);
            System.out.print("\t");
            System.out.print(supn);
            System.out.print("\t");
            System.out.print(subn);
            System.out.print("\t");
            System.out.print(n);
            System.out.print("\t");
            System.out.print(n.getGraphClass());
            System.out.println("");
        }
    }

    /**
     * Main
     */
    /*FIXME port to jgrapht
    public static void main(String args[]) throws Exception {
        int i;

        Getopt opts = new Getopt("LandMark", args, "h");
        opts.setOpterr(false);
        while ((i = opts.getopt()) != -1) {
            switch (i) {
                case '?':
                case 'h':
                    usage();
                    System.exit(1);
            }
        }

        //---- Load everything
        thegraph = new ISGCIGraph();
        Vector problems = new Vector();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in));
        ArrayList<ISGCIGraph> graphs = new ArrayList<ISGCIGraph>();

        load(args[opts.getOptind()], thegraph, problems);
        show(thegraph);
        thegraph.transitiveClosure();
        graphs.add(thegraph);

        //--- split repeatedly
        while (true) {
            String classname =  in.readLine();
            int num = -1;

            try {
                num = Integer.decode(classname);
            } catch (NumberFormatException e) {}

            if (num == -1)
                graphs = split(classname, graphs);
            else
                showClasses(graphs.get(num));

            for (ISGCIGraph g : graphs) {
                System.out.print(g == null ? 0 : g.countNodes());
                System.out.print(" ");
            }
            System.out.println("");
            System.out.println("");
        }

    }


    private static void usage() {
        System.out.println("Usage: java LandMark input.xml "+
                "");
    }
    */
}
