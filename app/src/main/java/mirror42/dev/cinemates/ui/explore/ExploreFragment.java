package mirror42.dev.cinemates.ui.explore;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import mirror42.dev.cinemates.R;


public class ExploreFragment extends Fragment implements View.OnClickListener{
//    private SwipeRefreshLayout swipeRefreshLayout;

    private Button buttonLatest;
    private Button buttonUpcomings;
    private Button buttonPopular;

    private View view;
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

        initViews(view);

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


    @Override
    public void onClick(View v) {
        if(v.getId() == buttonUpcomings.getId()){
            final Toast toast = Toast.makeText(getContext(), "upcoming", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

        }else  if(v.getId() == buttonLatest.getId()){
            final Toast toast = Toast.makeText(getContext(), "Latest", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }else if(v.getId() == buttonPopular.getId()){

            final Toast toast = Toast.makeText(getContext(), "popular", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }



    private void initViews(View view){
        this.view = view;

        buttonUpcomings = view.findViewById(R.id.button_exploreFragment_seeAllUpcomings);
        buttonLatest = view.findViewById(R.id.button_exploreFragment_seeAllLatest);
        buttonPopular = view.findViewById(R.id.button_exploreFragment_seeAllPopular);

        buttonPopular.setOnClickListener(this);
        buttonLatest.setOnClickListener(this);
        buttonUpcomings.setOnClickListener(this);
    }
}// end ExploreFragment class
