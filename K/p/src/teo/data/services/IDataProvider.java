package teo.data.services;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.SimpleDirectedGraph;

import teo.data.db.AbstractRelation;
import teo.isgci.gc.GraphClass;
import teo.isgci.grapht.Inclusion;
import teo.isgci.problem.Complexity;
import teo.isgci.problem.Problem;

public interface IDataProvider {
	void loadData(String data) throws Exception;
	
	AbstractRelation findRelation(GraphClass x, GraphClass y);
	Collection<AbstractRelation> getRelations();
	
	Collection<GraphClass> getGraphClasses();
	GraphClass getClass(String name);
	SimpleDirectedGraph<GraphClass, Inclusion> getInclusionGraph();
	Set<GraphClass> getEquivalentClasses(GraphClass graph);
	
	Problem[] getProblems();
	Problem getProblem(String name);
	
	Collection<GraphClass> getSubClasses(GraphClass graph, final Boolean recursive);
	Collection<GraphClass> getSuperClasses(GraphClass graph, final Boolean recursive);
	
	Map<Problem, Complexity> getComplexityMap(GraphClass graph);
	Map<GraphClass, Complexity> getComplexityMap(Problem problem);
	
	String getDate();    
    int getNodeCount();
    int getEdgeCount();
    
	Collection<GraphClass> getNodes(Collection<GraphClass> graphs, boolean doSuper, boolean doSub);
	Collection<GraphClass> getNodes(GraphClass graph, Problem problem);
}
