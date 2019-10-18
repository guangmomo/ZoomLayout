## ZoomLayout

Android 视图缩放组件

实现的功能点

1. 触摸滑动及惯性滑动
2. 多指缩放
3. 双击缩放

同时处理了以下细节
1. 滑动冲突，比如将 `ZoomLayout` 放在 `ViewPager` 中
2. 事件冲突，`ZoomLayout` 和 子 `View` 都能接收事件
3. `ZoomLayout` 宽、高更新时能够自适应布局


## 效果
![zoomlayout.gif](https://github.com/guangmomo/ZoomLayout/blob/master/demofile/zoomLayout.gif)

Download Demo [Apk](https://github.com/guangmomo/ZoomLayout/blob/master/demofile/zoomLayout.apk)

## 使用

```
compile 'com.xlw.zoom:zoomlayout:1.0.0'
```


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

</com.xuliwen.zoom.ZoomLayout>
```

支持的属性


属性 | 意义
---|---
max_zoom | 最大缩放倍数
min_zoom | 最小缩放倍数
double_click_zoom | 双击缩放倍数

## 实现原理

[简书链接](https://www.jianshu.com/p/f0710c28f061)

[github 链接](https://github.com/guangmomo/ZoomLayout/blob/master/PRINCIPLE.md/)