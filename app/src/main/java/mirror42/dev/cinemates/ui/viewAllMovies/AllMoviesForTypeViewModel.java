package mirror42.dev.cinemates.ui.viewAllMovies;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import mirror42.dev.cinemates.api.tmdbAPI.TheMovieDatabaseApi;
import mirror42.dev.cinemates.exception.NoResultException;
import mirror42.dev.cinemates.model.search.MovieSearchResult;
import mirror42.dev.cinemates.model.search.SearchResult;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.ui.explore.ExploreFragment;
import mirror42.dev.cinemates.utilities.MyValues;
import mirror42.dev.cinemates.utilities.ThreadManager;

public class AllMoviesForTypeViewModel extends ViewModel {


    private  MutableLiveData<ArrayList<Movie>> searchResult;
    private MutableLiveData<DownloadResult> downloadSearchResultStatus;
    private MutableLiveData<DownloadResult> downloadMaxPageStatus;
    private MutableLiveData<Integer>maxPage;
    private TheMovieDatabaseApi tmdb;

    public enum DownloadResult {
        FAILED,
        SUCCESS,
        NONE
    }

  public AllMoviesForTypeViewModel(){
      searchResult = new MutableLiveData<>();
      downloadSearchResultStatus = new MutableLiveData<>(DownloadResult.NONE);
      maxPage = new MutableLiveData<>();
      downloadMaxPageStatus = new MutableLiveData<>(DownloadResult.NONE);

      tmdb = TheMovieDatabaseApi.getInstance();
  }


    //--------------------------------------------------------- GETTERS/SETTERS

    public void postDownloadStatus(DownloadResult downloadMaxPageStatus) {
        this.downloadMaxPageStatus.postValue(downloadMaxPageStatus);
    }

    public LiveData<DownloadResult> getObservableDownloadMaxPageStatus() {
        return downloadMaxPageStatus;
    }

    public void postMaxPage(Integer maxPage) {
        this.maxPage.postValue(maxPage);
    }

    public Integer getMaxPage(){
        return maxPage.getValue();
    }

    public void postSearchResult(ArrayList<Movie> searchResult) {
        this.searchResult.postValue(searchResult);
    }

    public LiveData<ArrayList<Movie>> getObservableSearchResult() {
        return searchResult;
    }

    public ArrayList<Movie> getSearchResult(){
        return searchResult.getValue();
    }

    public void postDownloadSearchResultStatus(DownloadResult downloadSearchResultStatus) {
        this.downloadSearchResultStatus.postValue(downloadSearchResultStatus);
    }

    public LiveData<DownloadResult> getObservableDownloadSearchResultStatus() {
        return downloadSearchResultStatus;
    }




    //--------------------------------------------------------- MyMethods
    public void findMaxPage(ExploreFragment.MovieCategory movieCategoryToLoad){
        Runnable task = createMaxPageTask(movieCategoryToLoad);
        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTaskInPool(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable createMaxPageTask(ExploreFragment.MovieCategory movieCategoryToLoad) {
        return ()-> {

            switch(movieCategoryToLoad) {
                case POPULAR:
                    postMaxPage(tmdb.getMaxNumPagePopular());
                    break;
                case UPCOMINGS:
                    postMaxPage(tmdb.getMaxNumPageUpcoming());
                    break;
                case LATEST:
                    postMaxPage(tmdb.getMaxNumPageLatest());
                    break;
            }
                postDownloadStatus(DownloadResult.SUCCESS);
        };
    }// end createMaxPageTask()


    public void searchMovie(int page, ExploreFragment.MovieCategory movieCategoryToLoad ){
        Runnable task = createDownloadMovieTask(page, movieCategoryToLoad);
        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTaskInPool(task);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Runnable createDownloadMovieTask(int page, ExploreFragment.MovieCategory movieCategoryToLoad) {
        return ()-> {

            try{
            switch(movieCategoryToLoad) {
                case POPULAR:
                    postSearchResult(tmdb.getPopular(page));
                    break;
                case UPCOMINGS:
                    postSearchResult(tmdb.getUpcomgins(page));
                    break;
                case LATEST:
                    postSearchResult(tmdb.getLatest(page));
                    break;

            }
                postDownloadSearchResultStatus(DownloadResult.SUCCESS);
            }catch (Exception e){
                postDownloadSearchResultStatus(DownloadResult.FAILED);
            }
        };
    }


}
