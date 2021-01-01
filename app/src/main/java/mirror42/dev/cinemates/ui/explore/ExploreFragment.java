package mirror42.dev.cinemates.ui.explore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import mirror42.dev.cinemates.R;


public class ExploreFragment extends Fragment{
//    private SwipeRefreshLayout swipeRefreshLayout;

    public ExploreFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // TODO: swipe down to refresh
//        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//
//                try {
//                    LatestReleasesFragment f1 = (LatestReleasesFragment) getChildFragmentManager().getFragments().get(0);
//                    UpcomginsFragment f2 = (UpcomginsFragment) getChildFragmentManager().getFragments().get(1);
//                    PopularFragment f3 = (PopularFragment) getChildFragmentManager().getFragments().get(2);
//
//                    f1.downloadData();
//                    f2.downloadData();
//                    f3.downloadData();
//
//                } catch (Exception e) {
//                    e.getMessage();
//                    e.printStackTrace();
//                }
//            }
//
//
//        });

    }



}// end ExploreFragment class