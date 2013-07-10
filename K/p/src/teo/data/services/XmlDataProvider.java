package teo.data.services;

import java.util.TreeMap;
import java.util.Vector;

import org.jgrapht.graph.SimpleDirectedGraph;

import teo.Loader;
import teo.data.xml.ISGCIReader;
import teo.data.xml.XMLParser;
import teo.isgci.gc.GraphClass;
import teo.isgci.grapht.GAlg;
import teo.isgci.grapht.Inclusion;
import teo.isgci.problem.Problem;
import teo.isgci.util.LessLatex;

/**
 * Class to provide data to the application. Uses an XML file
 * as the backing store for the data.
 * 
 * @author Calum McLellan
 *
 */
public class XmlDataProvider extends DataProvider {
	
	public XmlDataProvider()
	{
	}

	/**
	 * Loads the data from the specified file into memory.
	 * 
	 * @param	data	The path to the XML file containing the application data
	 */
	@Override
	public void loadData(String data) throws Exception {
		if (initialized) {
            return;
		}
		
		/* Initialize the collections */
        this.inclGraph = new SimpleDirectedGraph<GraphClass,Inclusion>(Inclusion.class);
        this.problems = new Vector<Problem>(); 

		/* Parse the XML */
		Loader loader = new Loader("file:"+System.getProperty("user.dir")+"/", true);
        ISGCIReader gcr = new ISGCIReader(inclGraph, problems);
        XMLParser xml=new XMLParser(loader.openInputSource(data),
                						gcr, loader.new Resolver());
        xml.parse();

        this.relations = gcr.getRelations();
        
        this.date = gcr.getDate();
        this.nodeCount = gcr.getNodeCount();
        this.edgeCount = gcr.getEdgeCount();
        this.relations = gcr.getRelations();

        // Gather the classnames
        names = new TreeMap<String,GraphClass>(new LessLatex());
        for (GraphClass gclass : inclGraph.vertexSet())
            names.put(gclass.toString(), gclass);

        // Gather the SCCs
        sccs = GAlg.calcSCCMap(inclGraph);

        initialized = true;
	}

}
