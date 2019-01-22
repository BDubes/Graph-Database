
import java.io.File;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.io.fs.FileUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;



public class DataLoader {
	private static final String DB_PATH = "target/movies-db";
	private static GraphDatabaseService graphDb;
	public static void main(String args[]) throws JsonParseException, JsonMappingException, IOException{
		createDb();
		load();
		doQueries();
		shutDown();
	}

	private static void load() throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper m = new ObjectMapper();
		m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		List<Movie> movies=new ArrayList<Movie>();
		for(int i=1;i<=25;i++){
			Movie_Search search = m.readValue(new File(
					String.format("movies/page%d.json", i)), Movie_Search.class);
			movies.addAll(Arrays.asList(search.getMovies()));
		}		
		
		store(movies);
	}
	
	public static void store(List<Movie> movies){
		try ( Transaction tx = graphDb.beginTx() ){
			Map<String, Node> nodes = new HashMap<String, Node>();
			for(Movie movie:movies){
				Node mNode = graphDb.createNode(Labels.MOVIE);
				if(nodes.containsKey(movie.getId())){
					throw new RuntimeException("duplicate movie");
				}
				mNode.setProperty("title", movie.getTitle());
				mNode.setProperty("year", movie.getYear());
				mNode.setProperty("critics_rating", movie.getRatings().getCritics_score());
				mNode.setProperty("audience_rating", movie.getRatings().getAudience_score());
				mNode.setProperty("mpaa_rating", movie.getMpaa_rating());
				
				nodes.put(movie.getId(), mNode);
				
				findAllActors(movie, nodes, graphDb);
				
				tx.success();
			}
		}
	}
	
	private static void findAllActors(Movie movie, Map<String, Node> nodes,
			GraphDatabaseService db) {
		Cast[] casts = movie.getActor();
		if(casts==null){
			return;
		}
		
		for(Cast cast:casts){
			if(!nodes.containsKey(cast.getId())){
				Node aNode = db.createNode(Labels.ACTOR);
				aNode.setProperty("name", cast.getName());
				nodes.put(cast.getId(), aNode);
			}
			
			Node aNode = nodes.get(cast.getId());
			Relationship relationship = aNode.createRelationshipTo(
					nodes.get(movie.getId()), RelationsTypes.ACTING);
			if(cast.getCharacters()!=null){
				relationship.setProperty("characters", cast.getCharacters());
			}			
		}
	}

	
	public static void createDb()
    {
        clearDb();
        // START SNIPPET: startDb
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( new File(DB_PATH) );
        registerShutdownHook( graphDb );
    }
	
    // START SNIPPET: shutdownHook
    private static void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }

    private static void clearDb()
    {
        try
        {
            FileUtils.deleteRecursively( new File( DB_PATH ) );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }
    
    public static void shutDown()
    {
        System.out.println();
        System.out.println( "Shutting down database ..." );
        // START SNIPPET: shutdownServer
        graphDb.shutdown();
        // END SNIPPET: shutdownServer
    }


    
	public static void printMoviesIn_Java(int year){
		ResourceIterator<Node> movies = graphDb.findNodes(Labels.MOVIE, "year", year);
		while(movies.hasNext()){
			Node movie = movies.next();
			System.out.println(movie.getProperty("title"));
		}
	}
	
	public static void printCoStarts_Java(String name){
		ResourceIterator<Node> actors = graphDb.findNodes(Labels.ACTOR, "name", name);
		if(actors.hasNext()){
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
			

			System.out.println("--------- Movies in 1980 (Java)-----------");
			printMoviesIn_Java(1980);
			System.out.println("\n--------- Tautou's Co-Stars (Java)-----------");
			printCoStarts_Java("Audrey Tautou");
			System.out.println("\n--------- Movies in 1980 (CQL)-----------");
			printMoviesIn_Cql(1980);
			System.out.println("\n--------- Tautou's Co-Stars (CQL)-----------");
			printCoStarts_Cql("Audrey Tautou");

			
			tx.success();
		}
	}
}
    
    
    

