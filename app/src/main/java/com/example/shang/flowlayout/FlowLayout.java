package com.example.shang.flowlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shang on 2017/9/22.
 */
public class FlowLayout extends ViewGroup {

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * @param widthMeasureSpec  因为布局文件中设置FlowLayout的宽高均为match_parent,所以widthMeasureSpec,heightMeasureSpec都是MeasureSpec.EXACTLY
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);

        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        int width = 0;// 最长那一行的宽度
        int height = 0;// 最长那一行的高度

        // 记录每一行的宽度与高度
        int lineWidth = 0;
        int lineHeight = 0;

        //得到ViewGroup内部元素的个数
        int cCount = getChildCount();

        // 遍历所有子View(内部元素)
        for (int i = 0; i < cCount; i++) {
            // 拿到每一个子View
            View child = getChildAt(i);
            // 测量子View的宽高
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            // 子View占据的宽度，包括它的对应margin
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            // 子View占据的高度，包括它的对应margin
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            // 需要换行
            if ((lineWidth + childWidth) > sizeWidth) {
                // 对比得到最大的宽度
                width = Math.max(width, lineWidth);
                // 重置lineWidth行宽
                lineWidth = childWidth;
                // 叠加行高
                height += lineHeight;
                lineHeight = childHeight;
            } else { // 未换行
                // 叠加行宽
                lineWidth += childWidth;
                // 得到当前行最大的宽度
                lineHeight = Math.max(lineHeight, childHeight);
            }

            // 到达最后一个控件，要不然最后一行显示不了
            if (i == cCount - 1) {
                width = Math.max(lineWidth, width);
                height += lineHeight;
            }
        }

        setMeasuredDimension(
                modeWidth == MeasureSpec.EXACTLY ? sizeWidth : width,
                modeHeight == MeasureSpec.EXACTLY ? sizeHeight : height
        );
    }

    /**
     * 存储所有的View的集合,一行一行进行存储
     */
    private List<List<View>> mAllViews = new ArrayList<>();
    /**
     * 存储每一行的高度的集合
     */
    private List<Integer> mLineHght = new ArrayList<>();

    // 对每一个View指定位置
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 因为onLayout()方法会被多次调用，每次先clear
        mAllViews.clear();
        mLineHght.clear();

        // 当前ViewGroup的宽度，直接拿，以为我们已经执行了onMeasure()方法
        int width = getWidth();

        int lineWidth = 0;
        int lineHeight = 0;
        // 一行的View
        List<View> lineViews = new ArrayList<>();
        int cCount = getChildCount();

        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);
            // 不用再进行测量，因为我们在onMeasure()方法测量过了
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            // 如果需要换行
            if ((childWidth + lineWidth + lp.leftMargin + lp.rightMargin) > width) {
                // 记录LineHeight
                mLineHght.add(lineHeight);
                // 记录当前行的Views
                mAllViews.add(lineViews);
                //重置我们的行宽与行高
                lineWidth = 0;
                lineHeight = childHeight + lp.topMargin + lp.bottomMargin;
                // 重置我们的View集合，new一个新的
                lineViews = new ArrayList<>();
            }
            lineWidth += (childWidth + lp.leftMargin + lp.rightMargin);
            lineHeight = Math.max(lineHeight, childHeight + lp.topMargin + lp.bottomMargin);
            lineViews.add(child);
        } // for end

        // 处理最后一行，与上面处理最后一个View的原因是一样的
        mLineHght.add(lineHeight);
        mAllViews.add(lineViews);

        // 设置子View的位置
        int left = 0;
        int top = 0;

        // 行数,也就是mAllViews中还有多少个List集合
        int lineNum = mAllViews.size();
        for (int i = 0; i < lineNum; i++) {
            // 当前行的所有的View
            lineViews = mAllViews.get(i); // 复用前面声明的lineView引用
            lineHeight = mLineHght.get(i);
            // 为一行的View进行布局，千万不要把i,j写反了
            for (int j = 0; j < lineViews.size(); j++) {
                View child = lineViews.get(j);
                // 判断child的状态，它有可能不需要显示
                if (child.getVisibility() == View.GONE) {
                    // 如果不可见，跳出for循环
                    continue;
                }

                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                int lc = left + lp.leftMargin; // c是child的意思
                int tc = top + lp.topMargin;
                int rc = lc + child.getMeasuredWidth();
                int bc = tc + child.getMeasuredHeight();

                // 为子view布局
                child.layout(lc, tc, rc, bc);
                // 一行中高度不变，左距离累加
                left += (child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
            } // for end
            // 到达下一行之后，left要重新初始化，top要累加
            left = 0;
            top += lineHeight;
        }
}

    /*
     * ViewGroup会对应一个LayoutParams
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }
}
