# ArcScroll
# 仿360应用助手5.1.66版本游戏详情页滚动效果
[效果视频][1]
工作中需要实现类似360手机助手的游戏详情页上滚动效果,花了1天的时间研究了下,实现出来了.记录下.
拿到效果后用uiautomatorviewer工具窥探了下360的实现方式,发现它是用RecycleView实现的,那么要实现这种效果估计就是通过自定义LayoutManager来实现的了.
视频中每一张图片缩小为一个点,然后可以看成是点沿着一个圆弧运动.自定义LayoutManager将不同的item根据index放到位于圆弧的相应位置.思考到之前用到的PathMeasure类的使用跟自定义LayoutManager接合起来使用刚好能达到这种效果.

实现这种效果用到的主要知识点

**1.自定义LayoutManager 2.Path,PathMeasure系列Api的运用** 

     RectF rectF=new RectF(0,dp2px(220),screenWidth,dp2px(290));//第2个和第
     path.addArc(rectF,0,-180); //添加一个逆时针180度的弧度，从3点钟方向为起点
     pathMeasure.setPath(path,false); 
    //注意次处要设置为false,否则pathMeasure.getLength,返回的长度包括封闭扇形的2条半径
   

 PathMeasure的getPosTan方法,通过传入在path的length范围内的值,然后计算出相应位置和角度值分别放到了pos数组和tan数组里面.
 
 **关键代码:自定义LayoutManager重新防置子item的代码**
 

     
        for (int i = 0; i < getItemCount(); i++) {
            if (Rect.intersects(displayFrame, allItemFrames.get(i))) {
                View scrap = recycler.getViewForPosition(i);
                measureChildWithMargins(scrap, 0, 0);
                addView(scrap);
                float percent=(scrap.getLeft()+scrap.getMeasuredWidth()*1.00f/2.00f)/screenWidth*1.00f; //以每个item的宽度一半位置为中心点
                float pathMeasureLength=pathMeasure.getLength();
                pathMeasure.getPosTan( (pathMeasureLength*percent),pos,tan);
                Rect frame = allItemFrames.get(i);
                //将这个item布局出来
                layoutDecorated(scrap,
                        frame.left-horizontalScrollOffset,
                        frame.top ,
                        frame.right- horizontalScrollOffset,
                        (int) pos[1]);//此处直接使用获得到的y值
            }
        }

 完整代码:[例子][2]
 **在这里有个无耻请求,-_-,如果你觉得对你有点帮助,欢迎star**
 
  感谢[稀土掘金][3]的这篇文章对自定义LayoutManager的讲解,自己从里面学到了不少.


  [1]: http://7xs2th.com1.z0.glb.clouddn.com/360scrolling.mp4
  [2]: https://github.com/happycodinggirl/ArcScroll
  [3]: https://juejin.im/entry/57597452a34131006133369e
