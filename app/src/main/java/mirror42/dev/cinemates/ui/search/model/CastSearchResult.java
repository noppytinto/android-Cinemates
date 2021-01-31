package mirror42.dev.cinemates.ui.search.model;

import java.util.ArrayList;

import mirror42.dev.cinemates.tmdbAPI.model.Movie;

public class CastSearchResult extends SearchResult {
    private final int tmdbID;
    private final String fullName;
    private final String department;
    private final String profilePictureURL;
    private final ArrayList<Movie> knownFor;

    private CastSearchResult(Builder builder) {
        this.searchType        = SearchType.CAST;
        this.tmdbID            = builder.tmdbID;
        this.fullName          = builder.fullName;
        this.department        = builder.department;
        this.profilePictureURL = builder.profilePictureURL;
        this.knownFor          = builder.knownFor;
    }

    public static class Builder {
        // required parameters
        private final int tmdbID;
        private final String fullName;

        // optional parameters
        private String department  = null;
        private String profilePictureURL  = null;
        private ArrayList<Movie> knownFor = new ArrayList<>();

        // constructor
        public Builder(int tmdbID, String fullName) {
            this.tmdbID = tmdbID;
            this.fullName = fullName;
        }

        //

        public Builder setProfilePicture(String val) {
            this.profilePictureURL = val;
            return this;
        }

        public Builder setDepartment(String val) {
            this.department = val;
            return this;
        }

        public Builder setKnownFor(ArrayList<Movie> val) {
            this.knownFor = val;
            return this;
        }

        public CastSearchResult build() {
            return new CastSearchResult(this);
        }

    }// end Builder class

    //--------------------------------------------------------- GETTERS/SETTER
    public String getKnwonForAsString() {
        String res = "";

        if(knownFor!=null) {
            for(Movie x: knownFor) {
                String title = x.getTitle();
                if(title!=null)
                    res = res.concat(x.getTitle() + "  •  ");
            }
            if(res!=null && !res.isEmpty())
                res = res.substring(0, res.length()-2);
        }

        return res;
    }

    public int getTmdbID() {
        return tmdbID;
    }

    public String getFullName() {
        return fullName;
    }

    public String getProfilePictureURL() {
        return profilePictureURL;
    }

    public ArrayList<Movie> getKnownFor() {
        return knownFor;
    }

    public String getDepartment() {
        return department;
    }
}// end ActorSearchResult class
