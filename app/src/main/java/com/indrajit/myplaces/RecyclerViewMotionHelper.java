package com.indrajit.myplaces;


import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

class RecyclerViewMotionHelper extends ItemTouchHelper.SimpleCallback{

    private RecyclerViewMotionListener listener;

    interface RecyclerViewMotionListener {

        void onSwipe(RecyclerView.ViewHolder holder, int direction);
    }

    RecyclerViewMotionHelper(int dragDirs, int swipeDirs, RecyclerViewMotionListener listener) {
        super(dragDirs, swipeDirs);
        this.listener = listener;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        listener.onSwipe(viewHolder, direction);
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if(viewHolder != null && viewHolder instanceof LocationAdapter.LocationViewHolder){

            View foreground = ((LocationAdapter.LocationViewHolder) viewHolder).foreground;
            getDefaultUIUtil().onSelected(foreground);
        }
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {

        View foreground = ((LocationAdapter.LocationViewHolder) viewHolder).foreground;
        getDefaultUIUtil().clearView(foreground);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

        View foreground = ((LocationAdapter.LocationViewHolder) viewHolder).foreground;
        getDefaultUIUtil().onDraw(c, recyclerView, foreground, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

        View foreground = ((LocationAdapter.LocationViewHolder) viewHolder).foreground;
        getDefaultUIUtil().onDrawOver(c, recyclerView, foreground, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {

        if(viewHolder instanceof LocationAdapter.LocationViewHolder) {
            return super.getMovementFlags(recyclerView, viewHolder);

        } else{
            return 0;
        }
    }
}
