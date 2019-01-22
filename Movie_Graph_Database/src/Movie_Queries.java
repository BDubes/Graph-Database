

import java.io.File;
import java.util.Map;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.Traverser;

public class Movie_Queries {
	private static final String DB_PATH = "target/movies-db";
	private static GraphDatabaseService graphDb;
	
	public static void printMoviesIn_Java(int year){
		ResourceIterator<Node> movies = graphDb.findNodes(Labels.MOVIE, "year", year);
		while(movies.hasNext()){
			Node movie = movies.next();
			System.out.println(movie.getProperty("title"));
		}
	}
	
	public static void printCoStarts_Java(String name){
		ResourceIterator<Node> actors = graphDb.findNodes(Labels.ACTOR, "name", name);
		Node tom = actors.next();
		Traverser traverser = graphDb.traversalDescription()
				.breadthFirst()
				.evaluator(Evaluators.toDepth(2))
				.relationships( RelationsTypes.ACTING, Direction.BOTH )
				.traverse(tom);
			
		for(Path position:traverser){
			if(position.length()>1)
				System.out.println(position.endNode().getProperty("name"));
		}
	
	}
	

	public static void printMoviesIn_Cql(int year){
		Result result = graphDb.execute(
				String.format("match (m) where m.year=%d return m.title", year));
		while(result.hasNext()){
			Map<String, Object> row = result.next();
			System.out.println(row.get("m.title"));
		}		
	}
	
	public static void printCoStarts_Cql(String name){
		Result result = graphDb.execute(
				String.format("match (t)-[*2]-(a) where t.name='%s' return distinct a.name",name));
		while(result.hasNext()){
			Map<String, Object> row = result.next();
			System.out.println(row.get("a.name"));
		}

	}
	
	public static void doQueries() {
		try(Transaction tx = graphDb.beginTx()){
			
			printMoviesIn_Java(1980);
			System.out.println("--------------------");
			printCoStarts_Java("Audrey Tautou");
			System.out.println("--------------------");
			printMoviesIn_Cql(1980);
			System.out.println("--------------------");
			printCoStarts_Cql("Audrey Tautou");
			
			tx.success();
		}
	}
}
