package com.example.huangxl.arcscroll;

import android.content.Context;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by huangxl on 2016/9/18. RecycleView沿扇形路径移动的LayoutManager
 */
public class ArcLayoutManager extends RecyclerView.LayoutManager {


    private final PathMeasure pathMeasure;
    private final int screenWidth;
    private int horizontalScrollOffset = 0;
    private int totalWidth = 0;
    Context context;

    float[] pos=new float[2];  //用来存圆弧上点的xy 坐标
    float[] tan =new float[2];

    //保存所有的Item的上下左右的偏移量信息
    private SparseArray<Rect> allItemFrames = new SparseArray<>();



    public ArcLayoutManager(Context context) {
        this.context=context;
        pathMeasure=new PathMeasure();
        Path path=new Path();
        screenWidth=context.getResources().getDisplayMetrics().widthPixels;
      //  RectF rectF=new RectF(0,dp2px(220),screenWidth,dp2px(290));
        RectF rectF=new RectF(0,dp2px(220),screenWidth,dp2px(290));
        path.addArc(rectF,0,-180); //添加一个逆时针180度的弧度，从3点钟方向为起点
        pathMeasure.setPath(path,false); //注意次处要设置为false,否则pathMeasure.getLength,返回的长度包括封闭扇形的2条半径
}


    public int dp2px(float dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,context.getResources().getDisplayMetrics());
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        removeAllViews();
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        //如果没有item，直接返回
        if (getItemCount() <= 0) return;
        // 跳过preLayout，preLayout主要用于支持动画
        if (state.isPreLayout()) {
            return;
        }
        //在布局之前，将所有的子View先Detach掉，放入到Scrap缓存中
        detachAndScrapAttachedViews(recycler);
        //定义竖直方向的偏移量
        int offsetX = 0;
        totalWidth = 0;
        for (int i = 0; i < getItemCount(); i++) {

            //这里就是从缓存里面取出
            View view = recycler.getViewForPosition(i);
            //将View加入到RecyclerView中
            addView(view);
            measureChildWithMargins(view, 0, 0);
            int width = getDecoratedMeasuredWidth(view);
            int height = getDecoratedMeasuredHeight(view);

            totalWidth += width;
            Rect frame = allItemFrames.get(i);
            if (frame == null) {
                frame = new Rect();
            }
            frame.set(offsetX, 0, offsetX+width,  height);
            // 将当前的Item的Rect边界数据保存
            allItemFrames.put(i, frame);

            //将竖直方向偏移量增大height
            offsetX += width;
        }
        //如果所有子View的高度和没有填满RecyclerView的高度，
        // 则将高度设置为RecyclerView的高度
        totalWidth = Math.max(totalWidth, getHorizontalSpace());
        // fixScrollOffset();
        recycleAndFillItems(recycler, state);
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        //先detach掉所有的子View
       // detachAndScrapAttachedViews(recycler);

        //实际要滑动的距离
        int travel = dx;

        //如果滑动到最顶部
        if (horizontalScrollOffset + dx < 0) {
            travel = -horizontalScrollOffset;
        } else if (horizontalScrollOffset + dx > totalWidth - getHorizontalSpace()) {//如果滑动到最底部
            travel = totalWidth - getHorizontalSpace() - horizontalScrollOffset;
        }

        //将竖直方向的偏移量+travel
        horizontalScrollOffset += travel;

        // 平移容器内的item
        //   offsetChildrenVertical(-travel);
        offsetChildrenHorizontal(-dx);
        recycleAndFillItems(recycler, state);
        //Log.d("--->", " childView count:" + getChildCount());
        return travel;
    }

    /**
     * 回收不需要的Item，并且将需要显示的Item从缓存中取出
     */
    private void recycleAndFillItems(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);
        if (state.isPreLayout()) { // 跳过preLayout，preLayout主要用于支持动画
            return;
        }

        // 当前scroll offset状态下的显示区域
        Rect displayFrame = new Rect(horizontalScrollOffset, 0, horizontalScrollOffset+getHorizontalSpace(),  getVerticalSpace());

        /**
         * 将滑出屏幕的Items回收到Recycle缓存中
         */
        Rect childFrame = new Rect();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            childFrame.left = getDecoratedLeft(child);
            childFrame.top = getDecoratedTop(child);
            childFrame.right = getDecoratedRight(child);
            childFrame.bottom = getDecoratedBottom(child);
            //如果Item没有在显示区域，就说明需要回收
            if (!Rect.intersects(displayFrame, childFrame)) {
                //回收掉滑出屏幕的View
                removeAndRecycleView(child, recycler);

            }
        }

        //重新显示需要出现在屏幕的子View
        for (int i = 0; i < getItemCount(); i++) {

            if (Rect.intersects(displayFrame, allItemFrames.get(i))) {

                View scrap = recycler.getViewForPosition(i);
                measureChildWithMargins(scrap, 0, 0);
                addView(scrap);
                float percent=(scrap.getLeft()+scrap.getMeasuredWidth()*1.00f/2*1.00f)/screenWidth*1.00f; //以每个item的宽度一半位置为中心点
                float pathMeasureLength=pathMeasure.getLength();
                pathMeasure.getPosTan( (pathMeasureLength*percent),pos,tan);
                Rect frame = allItemFrames.get(i);
                //将这个item布局出来
                layoutDecorated(scrap,
                        frame.left-horizontalScrollOffset,
                        frame.top ,
                        frame.right- horizontalScrollOffset,
                        (int) pos[1]);


            }
        }
    }

    private int getVerticalSpace() {
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }

    private int getHorizontalSpace() {
        return getWidth() - getPaddingRight() - getPaddingLeft();
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
