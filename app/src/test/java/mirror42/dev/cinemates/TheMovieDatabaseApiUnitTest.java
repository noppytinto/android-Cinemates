package mirror42.dev.cinemates;


import org.junit.Test;

import java.util.ArrayList;

import mirror42.dev.cinemates.api.tmdbAPI.TheMovieDatabaseApi;
import mirror42.dev.cinemates.model.tmdb.Movie;

import static org.junit.Assert.assertTrue;

public class TheMovieDatabaseApiUnitTest {


    //-------------------------------------- testing getMoviesByTitle()
    @Test
    public void test_getMoviesByTitle_with_validTitle_validPage() {
        String testTitle = "titanic";
        int testPage = 1;

        TheMovieDatabaseApi api = TheMovieDatabaseApi.getInstance();
        ArrayList<Movie> actual = api.getMoviesByTitle(testTitle, testPage);

        assertTrue(actual!=null && actual.size()!=0);
    }

    @Test
    public void test_getMoviesByTitle_with_invalidTitle_validPage() {
        String testTitle = "asfads";
        int testPage = 1;

        TheMovieDatabaseApi api = TheMovieDatabaseApi.getInstance();
        ArrayList<Movie> actual = api.getMoviesByTitle(testTitle, testPage);

        assertTrue(actual!=null && actual.size()==0);
    }

    @Test
    public void test_getMoviesByTitle_with_validTitle_invalidPage() {
        String testTitle = "asfads";
        int testPage = -1;

        TheMovieDatabaseApi api = TheMovieDatabaseApi.getInstance();
        ArrayList<Movie> actual = api.getMoviesByTitle(testTitle, testPage);

        assertTrue(actual!=null && actual.size()==0);
    }

    @Test
    public void test_getMoviesByTitle_with_invalidTitle_invalidPage() {
        String testTitle = "asfads";
        int testPage = -1;

        TheMovieDatabaseApi api = TheMovieDatabaseApi.getInstance();
        ArrayList<Movie> actual = api.getMoviesByTitle(testTitle, testPage);

        assertTrue(actual!=null && actual.size()==0);
    }

    @Test
    public void test_getMoviesByTitle_with_nullTitle_validPage() {
        String testTitle = null;
        int testPage = 1;

        TheMovieDatabaseApi api = TheMovieDatabaseApi.getInstance();
        ArrayList<Movie> actual = api.getMoviesByTitle(testTitle, testPage);

        assertTrue(actual!=null && actual.size()==0);
    }

    @Test
    public void test_getMoviesByTitle_with_emptyTitle_validPage() {
        String testTitle = "";
        int testPage = 1;

        TheMovieDatabaseApi api = TheMovieDatabaseApi.getInstance();
        ArrayList<Movie> actual = api.getMoviesByTitle(testTitle, testPage);

        assertTrue(actual!=null && actual.size()==0);
    }

    @Test
    public void test_getMoviesByTitle_with_validTitleExtraSpaces_validPage() {
        String testTitle = "      titanic         ";
        int testPage = 1;

        TheMovieDatabaseApi api = TheMovieDatabaseApi.getInstance();
        ArrayList<Movie> actual = api.getMoviesByTitle(testTitle, testPage);

        assertTrue(actual!=null && actual.size()!=0);
    }


}// end TheMovieDatabaseApiUnitTest
