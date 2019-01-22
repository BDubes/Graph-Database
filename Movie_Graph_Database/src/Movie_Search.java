
import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Movie_Search {
	public Movie[] getMovies() {
		return movies;
	}
	public void setMovies(Movie[] movies) {
		this.movies = movies;
	}
	private Movie [] movies;
	
	public Map<String, String> getActors(){
		Map<String, String> actors = new HashMap<String, String>();
		
		for(Movie movie:movies){
			Cast [] casts = movie.getActor();
			if(casts==null){
				continue;
			}
			
			for(Cast cast:casts){
				if(!actors.containsKey(cast.getId())){
					actors.put(cast.getId(), cast.getName());
				}
			}
		}
		return actors;
	}
	
	
	
}
