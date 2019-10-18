## 1. ZoomLayout 需要实现的功能

### 1.1 需求列表

1. 触摸滑动及惯性滑动
2. 多指缩放
3. 双击缩放

除了实现这些主要功能外，还需要处理一下的细节
1. ZoomLayout 的宽高大于子 View时，子 View 居中显示
2. ZoomLayout 需要响应事件，但是不能把事件拦截掉
3. 处理滑动冲突，比如将 ZoomLayout 放在 ViewPager 中



### 1.2 使用举例

```
<?xml version="1.0" encoding="utf-8"?>
<com.xuliwen.zoom.ZoomLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:max_zoom="3.0"
    app:min_zoom="1.0"
    app:double_click_zoom="2.0">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">
        
        <ImageView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:scaleType="fitXY"
            android:adjustViewBounds="true"
            android:src="@mipmap/image1"/>

        <ImageView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:scaleType="fitXY"
            android:adjustViewBounds="true"
            android:src="@mipmap/image2"/>
        
    </LinearLayout>
```

### 1.3 效果和源码

效果如下：

![zoomlayout.gif](https://github.com/guangmomo/ZoomLayout/blob/master/demofile/zoomLayout.gif)

ZoomLayout 源码
[ZoomLayout 源码](https://github.com/guangmomo/ZoomLayout)



## 2. 实现

### 2.1 基础知识

在讲具体实现之前，先提一下会用到的一些基础的知识，不了解的同学可以先去了解一下

1. GestureDetector 用于获取单击、双击、滚动、抛掷 等动作
2. ScaleGestureDetector 用于获取缩放的动作
3. OverScroller 滚动的辅助类

### 2.2 重写 measureChildWithMargins

```
@Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed,
                                           int parentHeightMeasureSpec, int heightUsed) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin
                        + widthUsed, lp.width);
        final int usedTotal = getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin +
                heightUsed;
        final int childHeightMeasureSpec;
        if (lp.height == WRAP_CONTENT) {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    Math.max(0, MeasureSpec.getSize(parentHeightMeasureSpec) - usedTotal),
                    MeasureSpec.UNSPECIFIED);
        } else {
            childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
                    getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin
                            + heightUsed, lp.height);
        }
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }
```

ZoomLayout 继承了 LinearLayout 后，发现屏幕外的 View 是没有绘制出来的，但是我们平时使用 ScrollView 的时候，屏幕外的 View 也能绘制出来，查看 ScrollView 的源码，发现它重写了 measureChildWithMargins 方法。

### 2.3 实现滚动


```
private GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!isEnabled()) {
                return false;
            }
            processScroll((int) distanceX, (int) distanceY, getScrollRangeX(), getScrollRangeY());
            return true;
        }
    };
    
private void processScroll(int deltaX, int deltaY,
                               int scrollRangeX, int scrollRangeY) {
        int oldScrollX = getScrollX();
        int oldScrollY = getScrollY();
        int newScrollX = oldScrollX + deltaX;
        int newScrollY = oldScrollY + deltaY;
        final int left = 0;
        final int right = scrollRangeX;
        final int top = 0;
        final int bottom = scrollRangeY;

        if (newScrollX > right) {
            newScrollX = right;
        } else if (newScrollX < left) {
            newScrollX = left;
        }

        if (newScrollY > bottom) {
            newScrollY = bottom;
        } else if (newScrollY < top) {
            newScrollY = top;
        }
        if (newScrollX < 0) {
            newScrollX = 0;
        }
        if (newScrollY < 0) {
            newScrollY = 0;
        }
        scrollTo(newScrollX, newScrollY);
    }
    
```

滚动可以在 onScroll 回调拿到，distanceX、distanceY 分别是 X 轴和 Y 轴上拿到的
滚动距离，getScrollX()、getScrollY() 则拿到了当前的滚动距离，这样就可以算出
newScrollX 和 newScrollY 了，最后调用 scrollTo 进行滚动 。还有一点要注意的是，scrollX 和 ScrollY 都有滚动范围，实现如下：


```
// mCurrentZoom 是当前的缩放值；ScrollRange 大于 0 的时候说明可以滚动

private int getScrollRangeX() {
        final int contentWidth = getWidth() - getPaddingRight() - getPaddingLeft();
        return (getContentWidth() - contentWidth);
    }

    private int getContentWidth() {
        return (int) (child().getWidth() * mCurrentZoom);
    }

    private int getScrollRangeY() {
        final int contentHeight = getHeight() - getPaddingBottom() - getPaddingTop();
        return getContentHeight() - contentHeight;
    }

    private int getContentHeight() {
        return (int) (child().getHeight() * mCurrentZoom);
    }

    private View child() {
        return getChildAt(0);
    }

```

### 2.4 实现 Fling（抛掷）滚动
Fling 滚动就是我们往某个方向快速滑动，当我们手指抬起后，View 还会沿着某个方向继续滚动。


```
private GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (!isEnabled()) {
                return false;
            }
            fling((int) -velocityX, (int) -velocityY);
            return true;
        }
    };
    
private boolean fling(int velocityX, int velocityY) {
        if (Math.abs(velocityX) < mMinimumVelocity) {
            velocityX = 0;
        }
        if (Math.abs(velocityY) < mMinimumVelocity) {
            velocityY = 0;
        }
        final int scrollY = getScrollY();
        final int scrollX = getScrollX();
        final boolean canFlingX = (scrollX > 0 || velocityX > 0) &&
                (scrollX < getScrollRangeX() || velocityX < 0);
        final boolean canFlingY = (scrollY > 0 || velocityY > 0) &&
                (scrollY < getScrollRangeY() || velocityY < 0);
        boolean canFling = canFlingY || canFlingX;
        if (canFling) {
            velocityX = Math.max(-mMaximumVelocity, Math.min(velocityX, mMaximumVelocity));
            velocityY = Math.max(-mMaximumVelocity, Math.min(velocityY, mMaximumVelocity));
            int height = getHeight() - getPaddingBottom() - getPaddingTop();
            int width = getWidth() - getPaddingRight() - getPaddingLeft();
            int bottom = getContentHeight();
            int right = getContentWidth();
            mOverScroller.fling(getScrollX(), getScrollY(), velocityX, velocityY, 0, Math.max(0, right - width), 0,
                    Math.max(0, bottom - height), 0, 0);
            notifyInvalidate();
            return true;
        }
        return false;
    }
    
 private void notifyInvalidate() {
        // 效果和 invalidate 一样，但是会使得动画更平滑
        ViewCompat.postInvalidateOnAnimation(this);
    }
    
@Override
    public void computeScroll() {
        super.computeScroll();
        if (mOverScroller.computeScrollOffset()) { // 判断是否可以滚动
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mOverScroller.getCurrX();
            int y = mOverScroller.getCurrY();
            if (oldX != x || oldY != y) {
                final int rangeY = getScrollRangeY();
                final int rangeX = getScrollRangeX();
                processScroll(x - oldX, y - oldY, rangeX, rangeY);
            }
            if (!mOverScroller.isFinished()) { // 如果滚动没有停止，那就再调用一次 notifyInvalidate()，会触发下一次的 computeScroll()
                notifyInvalidate();
            }
        }
    }    
```

大体的思路是通过 onFling() 拿到手势，然后计算是否能够滑动，可以的话就调用
mOverScroller.fling() 开始滚动，最后不要忘了调用 notifyInvalidate()。调用
notifyInvalidate() 后我们就可以在 computeScroll() 回调中使用 processScroll() 进行滚动


### 2.5 手势缩放


```
 private ScaleGestureDetector.SimpleOnScaleGestureListener mSimpleOnScaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (!isEnabled()) {
                return false;
            }
            float newScale;
            newScale = mCurrentZoom * detector.getScaleFactor();
            if (newScale > mMaxZoom) {
                newScale = mMaxZoom;
            } else if (newScale < mMinZoom) {
                newScale = mMinZoom;
            }
            setScale(newScale, (int) detector.getFocusX(), (int) detector.getFocusY());
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
        }
    };
    
public void setScale(float scale, int centerX, int centerY) {
        float preScale = mCurrentZoom;
        mCurrentZoom = scale;
        int sX = getScrollX();
        int sY = getScrollY();
        int dx = (int) ((sX + centerX) * (scale / preScale - 1));
        int dy = (int) ((sY + centerY) * (scale / preScale - 1));
        child().setPivotX(0);
        child().setPivotY(0);
        child().setScaleX(mCurrentZoom);
        child().setScaleY(mCurrentZoom);
        processScroll(dx, dy, getScrollRangeX(), getScrollRangeY());
        notifyInvalidate();
    }    
```

实现思路是 onScale() 中拿到缩放手势，缩放不仅会影响到 scale，其实还会影响到
scrollX、scrollY，所以缩放的时候，也要调用 processScroll() 。由于我们的 scrollX、scrollY 是基于 ZoomLayout 的左上角计算的（这里先默认子 View 左上角和 `ZoomLayout` 左上角已知，后面还需要适配这一点），所以我们这里的缩放也要基于左上角计算，通过 setPivotX(0)，setPivotY(0) 设置缩放中心点为左上角

### 2.6 双击缩放


```
private GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            float newScale;
            if (mCurrentZoom < 1) {
                newScale = 1;
            } else if (mCurrentZoom < mDoubleClickZoom) {
                newScale = mDoubleClickZoom;
            } else {
                newScale = 1;
            }
            smoothScale(newScale, (int) e.getX(), (int) e.getY());
            return true;
        }
    };

public void smoothScale(float newScale, int centerX, int centerY) {
        if (mCurrentZoom > newScale) {
            if (mAccelerateInterpolator == null) {
                mAccelerateInterpolator = new AccelerateInterpolator();
            }
            mScaleHelper.startScale(mCurrentZoom, newScale, centerX, centerY, mAccelerateInterpolator);
        } else {
            if (mDecelerateInterpolator == null) {
                mDecelerateInterpolator = new DecelerateInterpolator();
            }
            mScaleHelper.startScale(mCurrentZoom, newScale, centerX, centerY, mDecelerateInterpolator);
        }
        notifyInvalidate();
    }

@Override
    public void computeScroll() {
        super.computeScroll();
        if (mScaleHelper.computeScrollOffset()) {
            setScale(mScaleHelper.getCurScale(), mScaleHelper.getStartX(), mScaleHelper.getStartY());
        }
    }    
    
```

双击缩放和手势缩放都是缩放，不同点在于双击缩放我们需要自己去计算每个时间点的
scale，比如说双击后，`View` 会在 200 ms 内从 1倍 scale 变成 2倍 scale，那么我们就要自己去计算 200ms，scale 的变化。看到这里大家都应该想到了其实就是对 scale 这个值做一个属性动画嘛。这里将其封装在了 ScaleHelper 中。跟 Fling 的思路一样，通过 notifyInvalidate() 和 computeScroll() 实现循环。


## 3. 其他功能

### 3.1 适配 ViewPager
很简单，`ViewPager` 会通过子 `View` 的 `canScrollHorizontally` 和 `canScrollVertically` 判断是否可以横向、竖向滚动，`ZoomLayout` 重写他们就是了


```
    @Override
    public boolean canScrollHorizontally(int direction) {
        if (direction > 0) {
            return getScrollX() < getScrollRangeX();
        } else {
            return getScrollX() > 0 && getScrollRangeX() > 0;
        }
    }

    @Override
    public boolean canScrollVertically(int direction) {
        if (direction > 0) {
            return getScrollY() < getScrollRangeY();
        } else {
            return getScrollY() > 0 && getScrollRangeY() > 0;
        }
    }
```

### 3.2 事件传递

我们不希望 ZoomLayout 或者子 View 把事件消耗掉，而是两者都能收到事件。
下面是我的实现：
`ZoomLayout` 在 `dispatchTouchEvent` 去接收事件，因为这样即使子 `View` 消耗了事件，事件依然会经过这里。
然后在 `onDraw` 设置 `child().setClickable(true)`，这里是为了让事件能被
子 View 消耗掉，因为只有子 View 消耗了事件，事件才能一直传递到 `ZoomLayout` 中去。
```
@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mGestureDetector.onTouchEvent(ev);
        mScaleDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        child().setClickable(true);
    }
```

### 3.3 布局的控制

我们希望 `ZoomLayout` 的宽高比子 View 的宽高大的时候，居中显示，否则就显示为
`left|top`，我的思路是我们可以在 `onDraw` 中拿到准确的宽、高，通过宽高的对比，决定使用什么布局


```
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        child().setClickable(true);
        if (child().getHeight() < getHeight() || child().getWidth() < getWidth()) {
            setGravity(Gravity.CENTER);
        } else {
            setGravity(Gravity.TOP);
        }
    }
```

当适配到这里的时候，我们会发现一个问题，比如子 View 的高度小于 `ZoomLayout` 的时候，我们是安装子 View 的中心放大，如果这是我们放大子 View，放大到子 View 高度大于 `ZoomLayout`，我们这时候需要将子 `View` translate 到 `ZoomLayout` 的顶部，原因是我们第 2.5 步说到的 scroolX、scrollY 是基于左上角计算的。所以适配后的代码是这样的


```
    public void setScale(float scale, int centerX, int centerY) {
        float preScale = mCurrentZoom;
        mCurrentZoom = scale;
        int sX = getScrollX();
        int sY = getScrollY();
        int dx = (int) ((sX + centerX) * (scale / preScale - 1));
        int dy = (int) ((sY + centerY) * (scale / preScale - 1));
        if (getScrollRangeX() < 0) {
            child().setPivotX(child().getWidth() / 2);
            child().setTranslationX(0);
        } else {
            child().setPivotX(0);
            int willTranslateX = -(child().getLeft());
            child().setTranslationX(willTranslateX);
        }
        if (getScrollRangeY() < 0) {
            child().setPivotY(child().getHeight() / 2);
            child().setTranslationY(0);
        } else {
            int willTranslateY = -(child().getTop());
            child().setTranslationY(willTranslateY);
            child().setPivotY(0);
        }
        child().setScaleX(mCurrentZoom);
        child().setScaleY(mCurrentZoom);
        processScroll(dx, dy, getScrollRangeX(), getScrollRangeY());
        notifyInvalidate();
    }
```

在适配业务的过程，遇到了另外一个问题，就是 ZoomLayout 的高度有可能是会发生变化的，比如键盘弹出来的时候，ZoomLayout 可能会被压小，
我的思路是在 `onDraw` 中监听宽高的变化，有变化的时候，调用 `setScale` 去设置为正确的状态。


```
public void setScale(float scale, int centerX, int centerY) {
        // 记下最近一次的状态
        mLastCenterX = centerX;
        mLastCenterY = centerY;
        mCurrentZoom = scale;
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mNeedReScale) {
            // 需要重新刷新，因为宽高已经发生变化
            setScale(mCurrentZoom, mLastCenterX, mLastCenterY);
            mNeedReScale = false;
        }
    }    
    
@Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mLastChildWidth != child().getWidth() || mLastChildHeight != child().getHeight() || mLastWidth != getWidth()
            || mLastHeight != getHeight()) {
            // 宽高变化后，记录需要重新刷新，放在下次 onLayout 处理，避免 View 的一些配置：比如 getTop() 没有初始化好
            // 下次放在 onLayout 处理的原因是 setGravity 会在 onLayout 确定完位置，这时候去 setScale 导致位置的变化就不会导致用户看到
            // 闪一下的问题
            mNeedReScale = true;
        }
        mLastChildWidth = child().getWidth();
        mLastChildHeight = child().getHeight();
        mLastWidth = child().getWidth();
        mLastHeight = getHeight();
        if (mNeedReScale) {
            notifyInvalidate();
        }
    }    
```

上面有个小细节是发现 mNeedReScale 为 true 时没有立即调用 `setScale`，因为这时候 `setGravity` 还没有生效，我把它放在了下一次
`onLayout` 中


## 总结

实现一个 ZoomLayout 主要是需要熟悉手势的使用，然后实现过程中比较难也比较麻烦的是各种坐标相关的计算，以及各种细节的适配。实现过程中很多代码都参考了 [LargeImageView](https://github.com/LuckyJayce/LargeImage) 、`ScrollView`。
`ZoomLayout` 的实现还有很多改进的地方，比如事件的处理等，欢迎交流~










