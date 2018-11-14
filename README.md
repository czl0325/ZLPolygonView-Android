# ZLPolygonView-Android
类似六芒星的能力值图案，可自定义点数和值。Android版

### 效果图

![](https://github.com/czl0325/ZLPolygonView-Android/blob/master/demo.gif?raw=true)

### 导入
```JAVA
implementation 'com.github.czl0325:zlpolygonview:1.0.0'
```

### 参数

| 可配置参数               | 类型      | 作用                                                    |
|------------------------|-----------|--------------------------------------------------------|
| mPolygonValues          |  List<Float>     | 每个能力值的数值数组，介于0~1之间      |
| mTextLabels            | List<String>      | 标签数组，没有默认显示“空”           |
| InnerColor      | color\|reference      | 内部填充颜色的色值，默认为Color.CYAN   |
| LineColor            | color\|reference      | 线条的颜色，默认为Color.GRAY     |
| LineWidth            | dimension      | 线条宽度，默认为1 |
| EdgeNumber			| integer	 |	分割线的数量，默认为4 |
| DotNumber			| integer	 |	点数，默认为4 |


### 用法

```html
<com.github.zlpolygonview.ZLPolygonView
        android:id="@+id/polygonview"
        android:layout_width="200dp"
        android:layout_height="200dp"
        android:layout_marginTop="100dp"/>
```

```JAVA
polygonView = findViewById(R.id.polygonview);
List<Float> values = new ArrayList<>();
for (int i=0; i<4; i++) {
    values.add((float) (Math.random()*50/100+0.5));
}
polygonView.setPolygonValues(values);
polygonView.setOnClickPolygonListeren(new ZLPolygonView.onClickPolygonListeren() {
    @Override
    public void onClickPolygon(MotionEvent event, int index) {
        View rootView = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_main, null);

        new ValuePopupWindow(MainActivity.this).showAtLocation(rootView,
                Gravity.TOP|Gravity.LEFT,
                (int)event.getRawX(), (int)event.getRawY());
    }
});
```


