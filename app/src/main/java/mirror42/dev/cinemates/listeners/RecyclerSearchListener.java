package mirror42.dev.cinemates.listeners;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerSearchListener extends RecyclerView.SimpleOnItemTouchListener {
    public interface OnClick_RecycleSearchListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);

    }

    private final OnClick_RecycleSearchListener listener;
    private final GestureDetectorCompat gestureDetector;



    //---------------------------------------------------------------- CONSTRUCTORS

    public RecyclerSearchListener(Context context, final RecyclerView recyclerView, OnClick_RecycleSearchListener listener) {
        this.listener = listener;

        // customize gestures
        this.gestureDetector = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if ((childView != null) && (listener != null)) {
                    listener.onItemClick(childView, recyclerView.getChildAdapterPosition(childView));
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if ((childView != null) && (listener != null)) {
                    listener.onItemLongClick(childView, recyclerView.getChildAdapterPosition(childView));
                }
            }
        });
    }


    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        // to intercept all touch events
        if(gestureDetector != null) {
            boolean result = gestureDetector.onTouchEvent(e);
            return result;
        }
        else {
            return false;
        }

        // this return will return back the control to the caller
        // otw onInterceptTouchEvent will capture all touch events on the screen
        // and we will be stucked
        //return super.onInterceptTouchEvent(rv, e);
    }
}// end RecyclerViewSearchListener class
