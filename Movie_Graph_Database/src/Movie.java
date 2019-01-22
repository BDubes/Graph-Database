
import java.util.ArrayList;
import java.util.List;
public class Movie {

	public Cast[] actors;

	public Cast[] getActors() {
		return actors;
	}

	public void setActors(Cast[] actors) {
		this.actors = actors;
	}

	private String id;
	private String title;
	private int year;
	private String mpaa_rating;
	private Ratings ratings;
	private Cast[] actor;
	private MovieLink links;

	public int length() {
		return actors.length;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getMpaa_rating() {
		return mpaa_rating;
	}

	public void setMpaa_rating(String mpaa_rating) {
		this.mpaa_rating = mpaa_rating;
	}

	public Ratings getRatings() {
		return ratings;
	}

	public void setRatings(Ratings ratings) {
		this.ratings = ratings;
	}
	
	public Cast[] getActor() {
		return actor;
	}
	public void setActor(Cast[] actor) {
		this.actor = actor;
	}
	public MovieLink getLinks() {
		return links;
	}
	public void setLinks(MovieLink links) {
		this.links = links;
	}

}