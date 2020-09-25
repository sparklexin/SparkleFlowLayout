# Welcome to use SparkleFlowLayout!

`SparkleFlowLayout` is based on [FlowLayout](https://github.com/material-components/material-components-android/blob/master/lib/java/com/google/android/material/internal/FlowLayout.java).

Additional support setting maxLines: when reached the max line, it would be ellipsized, just like TextView's `android:ellipsize="end"`

## Usage
```xml
<xin.sparkle.flowlayout.SparkleFlowLayout
    android:id="@+id/flow_layout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:sparkleLineSpacing="8dp"
    app:sparkleItemSpacing="2dp"
    app:sparkleMaxLines="1">
    <!-- place your views here or dynamic addView -->
</xin.sparkle.flowlayout.SparkleFlowLayout>
```
If you need to customize the ellipsize symbol/layout, just use `app:sparkleEllipsizeLayout`.

## Attributes

| Attribute | Format | Description |
| ---- | ---- | ---- |
| sparkleLineSpacing | dimension | spacing between rows |
| sparkleItemSpacing | dimension | spacing between item views |
| sparkleMaxLines    | integer   | max display rows, the content exceeded would be ellipsized, just like TextView's `android:ellipsize="end"` |
| sparkleEllipsizeLayout | reference | the default ellipsize symbol is `…`, you can set customized layout. |
