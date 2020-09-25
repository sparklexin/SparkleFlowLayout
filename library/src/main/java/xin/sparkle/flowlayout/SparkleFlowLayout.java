/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xin.sparkle.flowlayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Horizontally lay out children until the row is filled and then moved to the next line.
 * Support ellipse when reached the maxLine
 */
public class SparkleFlowLayout extends ViewGroup {
    private int lineSpacing;
    private int itemSpacing;
    private int maxLines;

    private int theLastAllowMaxLineChildIndex;
    private View ellipseView;

    public SparkleFlowLayout(Context context) {
        this(context, null);
    }

    public SparkleFlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SparkleFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SparkleFlowLayout(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        loadFromAttributes(context, attrs);
    }

    private void loadFromAttributes(Context context, AttributeSet attrs) {
        final TypedArray array =
                context.getTheme().obtainStyledAttributes(attrs, R.styleable.SparkleFlowLayout, 0, 0);
        lineSpacing = array.getDimensionPixelSize(R.styleable.SparkleFlowLayout_sparkleLineSpacing, 0);
        itemSpacing = array.getDimensionPixelSize(R.styleable.SparkleFlowLayout_sparkleItemSpacing, 0);

        maxLines = array.getInt(R.styleable.SparkleFlowLayout_sparkleMaxLines, Integer.MAX_VALUE);
        if (maxLines <= 0) {
            throw new IllegalArgumentException("maxLines must be greater than zero!");
        }

        int ellipseLayoutId = array.getResourceId(R.styleable.SparkleFlowLayout_sparkleEllipsizeLayout, 0);
        if (ellipseLayoutId == 0) {
            ellipseView = new TextView(getContext());
            ((TextView) ellipseView).setText("…");
        } else {
            ellipseView = LayoutInflater.from(getContext()).inflate(ellipseLayoutId, this, false);
        }
        array.recycle();
    }

    public int getLineSpacing() {
        return lineSpacing;
    }

    public void setLineSpacing(int lineSpacing) {
        this.lineSpacing = lineSpacing;
    }

    public int getItemSpacing() {
        return itemSpacing;
    }

    public void setItemSpacing(int itemSpacing) {
        this.itemSpacing = itemSpacing;
    }

    public int getMaxLines() {
        return maxLines;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        final int height = MeasureSpec.getSize(heightMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        final int maxWidth =
                widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.EXACTLY
                        ? width
                        : Integer.MAX_VALUE;
        theLastAllowMaxLineChildIndex = Integer.MAX_VALUE;
        ellipseView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        int childLeft = getPaddingLeft();
        int childTop = getPaddingTop();
        int childBottom = childTop;
        int childRight = childLeft;
        int maxChildRight = 0;
        final int maxRight = maxWidth - getPaddingRight();
        for (int i = 0, lineCounter = 1; i < getChildCount(); i++) {
            View child = getChildAt(i);

            if (child.getVisibility() == View.GONE) {
                continue;
            }
            measureChild(child, widthMeasureSpec, heightMeasureSpec);

            LayoutParams lp = child.getLayoutParams();
            int leftMargin = 0;
            int rightMargin = 0;
            if (lp instanceof MarginLayoutParams) {
                MarginLayoutParams marginLp = (MarginLayoutParams) lp;
                leftMargin += marginLp.leftMargin;
                rightMargin += marginLp.rightMargin;
            }

            childRight = childLeft + leftMargin + child.getMeasuredWidth();

            // If the current child's right bound exceeds Flowlayout's max right bound and flowlayout is
            // not confined to a single line, move this child to the next line and reset its left bound to
            // flowlayout's left bound.
            if (childRight + ellipseView.getMeasuredWidth() > maxRight) {
                lineCounter += 1;
                if (lineCounter > maxLines) {
                    theLastAllowMaxLineChildIndex = i;
                    break;
                }
                // next line left top
                if (i < getChildCount() - 1) {
                    childLeft = getPaddingLeft();
                    childTop = childBottom + lineSpacing;
                }
            }

            childRight = childLeft + leftMargin + child.getMeasuredWidth();
            childBottom = childTop + child.getMeasuredHeight();

            // Updates Flowlayout's max right bound if current child's right bound exceeds it.
            if (childRight > maxChildRight) {
                maxChildRight = childRight;
            }

            childLeft += (leftMargin + rightMargin + child.getMeasuredWidth()) + itemSpacing;

            // For all preceding children, the child's right margin is taken into account in the next
            // child's left bound (childLeft). However, childLeft is ignored after the last child so the
            // last child's right margin needs to be explicitly added to Flowlayout's max right bound.
            if (i == (getChildCount() - 1)) {
                maxChildRight += rightMargin;
            }
        }

        maxChildRight += getPaddingRight();
        childBottom += getPaddingBottom();

        int finalWidth = getMeasuredDimension(width, widthMode, maxChildRight);
        int finalHeight = getMeasuredDimension(height, heightMode, childBottom);
        setMeasuredDimension(finalWidth, finalHeight);
    }

    private static int getMeasuredDimension(int size, int mode, int childrenEdge) {
        switch (mode) {
            case MeasureSpec.EXACTLY:
                return size;
            case MeasureSpec.AT_MOST:
                return Math.min(childrenEdge, size);
            default: // UNSPECIFIED:
                return childrenEdge;
        }
    }

    @Override
    protected void onLayout(boolean sizeChanged, int left, int top, int right, int bottom) {
        if (getChildCount() == 0) {
            // Do not re-layout when there are no children.
            return;
        }

        boolean isRtl = getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        int paddingStart = isRtl ? getPaddingRight() : getPaddingLeft();
        int paddingEnd = isRtl ? getPaddingLeft() : getPaddingRight();
        int childStart = paddingStart;
        int childTop = getPaddingTop();
        int childBottom = childTop;
        int childEnd;

        final int maxChildEnd = right - left - paddingEnd;

        boolean canContainsTheLast = false;
        int rowCount = 1;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }

            LayoutParams lp = child.getLayoutParams();
            int startMargin = 0;
            int endMargin = 0;
            if (lp instanceof MarginLayoutParams) {
                MarginLayoutParams marginLp = (MarginLayoutParams) lp;
                startMargin = marginLp.getMarginStart();
                endMargin = marginLp.getMarginEnd();
            }

            childEnd = childStart + startMargin + child.getMeasuredWidth();

            if (rowCount < maxLines && (childEnd > maxChildEnd)) {
                childStart = paddingStart;
                childTop = childBottom + lineSpacing;
                rowCount++;
            }

            childEnd = childStart + startMargin + child.getMeasuredWidth();
            childBottom = childTop + child.getMeasuredHeight();

            if (i == theLastAllowMaxLineChildIndex - 1) {
                // Enough space to display both theLastAllowMaxLineChildIndex view && ellipseView
                canContainsTheLast = (maxChildEnd - childEnd) > ellipseView.getMeasuredWidth() + startMargin + getChildAt(theLastAllowMaxLineChildIndex).getMeasuredWidth() + itemSpacing;
            }

            if (isRtl) {
                child.layout(
                        maxChildEnd - childEnd, childTop, maxChildEnd - childStart - startMargin, childBottom);
            } else {
                child.layout(childStart + startMargin, childTop, childEnd, childBottom);
            }

            if ((i == theLastAllowMaxLineChildIndex - 1 && !canContainsTheLast) || i == theLastAllowMaxLineChildIndex) {
                removeView(ellipseView);
                addView(ellipseView);
                if (isRtl) {
                    ellipseView.layout(maxChildEnd - childEnd - ellipseView.getMeasuredWidth() - startMargin, childTop, maxChildEnd - childStart, childBottom);
                } else {
                    ellipseView.layout(childEnd, childTop, childEnd + ellipseView.getMeasuredWidth(), childBottom);
                }
                return;
            }

            childStart += (startMargin + endMargin + child.getMeasuredWidth()) + itemSpacing;
        }
    }
}
