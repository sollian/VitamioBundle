package u.can.i.up.dl.utils.video;

import android.app.Activity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by lczgywzyy on 2014/12/21.
 */
public class VideoViewTouchListener implements View.OnTouchListener{

    private GestureDetector mGestureDetector;

    public VideoViewTouchListener(Activity activity){
        mGestureDetector = new GestureDetector(activity, new GestureListenerImpl());
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(VideoUtils.getInstance().isScrollScreen()){
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_CANCEL:
//                Log.i("VideoViewTouchListener", "ACTION_CANCEL");
                case MotionEvent.ACTION_UP:
//                Log.i("VideoViewTouchListener", "ACTION_UP");
                    VideoUtils.getInstance().endGesture();
                    break;
            }
        }
        mGestureDetector.onTouchEvent(event);
        return true;
    }
}