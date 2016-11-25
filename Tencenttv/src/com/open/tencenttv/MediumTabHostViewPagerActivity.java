package com.open.tencenttv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.animation.Animator;
import android.content.res.Resources;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;

import com.open.androidtvwidget.bridge.EffectNoDrawBridge;
import com.open.androidtvwidget.bridge.OpenEffectBridge;
import com.open.androidtvwidget.utils.OPENLOG;
import com.open.androidtvwidget.view.MainUpView;
import com.open.androidtvwidget.view.OpenTabHost;
import com.open.androidtvwidget.view.OpenTabHost.OnTabSelectListener;
import com.open.androidtvwidget.view.TextViewWithTTF;
import com.open.tencenttv.adapter.OpenTabPagerAdapter;
import com.open.tencenttv.adapter.RankPagerAdapter;
import com.open.tencenttv.bean.SliderNavBean;
import com.open.tencenttv.fragment.RankV4Fragment;
import com.open.tencenttv.json.SliderNavJson;
import com.open.tencenttv.utils.UrlUtils;

/**
 * ViewPager demo：
 * 注意标题栏和viewpager的焦点控制.(在XML布局中控制了, ids)
 * 移动边框的问题也需要注意.
 * @author hailongqiu
 */
public class MediumTabHostViewPagerActivity extends CommonFragmentActivity<SliderNavJson> implements OnTabSelectListener {
    ArrayList<SliderNavBean> list = new ArrayList<SliderNavBean>();
//    private List<View> viewList  = new ArrayList<View>();// 将要分页显示的View装入数组中
    ViewPager viewpager;
    OpenTabHost mOpenTabHost;
    OpenTabPagerAdapter mOpenTabPagerAdapter;
    List<String> titleList = new ArrayList<String>();
    // 移动边框.
    EffectNoDrawBridge mEffectNoDrawBridge;
    View mNewFocus;
//    ImageLoader mImageLoader;
    private List<Fragment> listRankFragment = new ArrayList<Fragment>();// view数组
    private List<Integer> ids = new ArrayList<Integer>();
    RankPagerAdapter mRankPagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meduim_tabhost_viewpager);
        init();
    }

    @Override
    protected void findView() {
        super.findView();
        //初始化标题栏.
        mOpenTabHost = (OpenTabHost) findViewById(R.id.openTabHost);
        // 初始化viewpager.
        viewpager = (ViewPager) findViewById(R.id.viewpager);
        doAsync(this, this, this);
    }

    @Override
    protected void initValue() {
        super.initValue();
//        mImageLoader = new ImageLoader(this);
//        mImageLoader.setRequiredSize(5 * (int) getResources().getDimension(R.dimen.litpic_width));
        // 初始化移动边框.
        initMoveBridge();

    }

    @Override
    public SliderNavJson call() throws Exception {
    	SliderNavJson mCommonT = new SliderNavJson();
        ArrayList<SliderNavBean> list = new ArrayList<SliderNavBean>();//导航大图
        try {
            // 解析网络标签
            list = parseSliderNav(UrlUtils.TENCENT_TV_URL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCommonT.setList(list);
        return mCommonT;
    }

    @Override
    public void onCallback(SliderNavJson result) {
        super.onCallback(result);
        // 初始化viewpager.
//        LayoutInflater inflater = getLayoutInflater();
        list.clear();
        list.addAll(result.getList());
        titleList.clear();
        for(SliderNavBean sliderNavBean:result.getList()){
//            View view = inflater.inflate(R.layout.item_medium_pager, null);
//            ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
//            TextView textView = (TextView) view.findViewById(R.id.textview);
//            textView.setText(sliderNavBean.getTitle());
//            if(sliderNavBean.getImageUrl()!=null && sliderNavBean.getImageUrl().length()>0){
//                mImageLoader.DisplayImage(sliderNavBean.getImageUrl(), imageView);
//            }
//            viewList.add(view);
            titleList.add(sliderNavBean.getTitle());
            Fragment fragment = RankV4Fragment.newInstance();
            listRankFragment.add(fragment);
            ids.add(R.id.title_bar1);
        }
        //初始化标题栏.
        mOpenTabPagerAdapter = new OpenTabPagerAdapter(this,titleList,ids);
        mOpenTabHost.setAdapter(mOpenTabPagerAdapter);
        mOpenTabPagerAdapter.notifyDataSetChanged();
        mRankPagerAdapter = new RankPagerAdapter(getSupportFragmentManager(),listRankFragment,titleList);
        viewpager.setAdapter(mRankPagerAdapter);
//        viewpager.setAdapter(new MediumPagerAdapter(this,list));
    }

    public ArrayList<SliderNavBean> parseSliderNav(String href) {
        ArrayList<SliderNavBean> list = new ArrayList<SliderNavBean>();
        try {
            href = makeURL(href, new HashMap<String, Object>() {
                {
                }
            });
            Log.i(TAG, "url = " + href);

            Document doc = Jsoup.connect(href).userAgent(UrlUtils.userAgent).timeout(10000).get();
            Element masthead = doc.select("div.slider_nav").first();
            Elements aElements = masthead.select("a");
            /**
             * <a href="https://v.qq.com/x/cover/8479ahinkqm7czh.html" target="_blank" class="nav_link current" data-bgcolor="#e6d7b0" data-navcolor="深色" data-bgimage="http://puui.qpic.cn/tv/0/5785436_1680580/0">锦绣未央: 唐嫣玩腹黑？</a>
             */
            // 解析文件
            if (aElements != null && aElements.size() > 1) {
                for (int i = 1; i < aElements.size(); i++) {
                    SliderNavBean sliderNavBean = new SliderNavBean();
                    try {
                        Element aElement = aElements.get(i).select("a").first();
                        String hrefurl = aElement.attr("href");
                        String title = aElement.text();
                        String imageurl = aElement.attr("data-bgimage");
                        Log.i(TAG,"i==="+i+"hrefurl=="+hrefurl+";title==="+title+";imageurl=="+imageurl);
                        sliderNavBean.setTitle(title);
                        sliderNavBean.setHrefUrl(hrefurl);
                        sliderNavBean.setImageUrl(imageurl);
                        list.add(sliderNavBean);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    protected void bindEvent() {
        super.bindEvent();
        //初始化标题栏.
        mOpenTabHost.setOnTabSelectListener(this);
        // 初始化viewpager.
        // 全局焦点监听. (这里只是demo，为了方便这样写，你可以不这样写)
        viewpager.getViewTreeObserver().addOnGlobalFocusChangeListener(new OnGlobalFocusChangeListener() {
            @Override
            public void onGlobalFocusChanged(View oldFocus, View newFocus) {
                // 判断 : 避免焦点框跑到标题栏. (只是demo，你自己处理逻辑)
                // 你也可以让标题栏放大，有移动边框.
                if (newFocus != null && !(newFocus instanceof TextViewWithTTF)) {
                    mEffectNoDrawBridge.setVisibleWidget(false);
                    mNewFocus = newFocus;
                    mOldView = oldFocus;
                    mainUpView1.setFocusView(newFocus, oldFocus, 1.2f);
                    // 让被挡住的焦点控件在前面.
                    newFocus.bringToFront();
                    OPENLOG.D("addOnGlobalFocusChangeListener");
                } else { // 标题栏处理.
                    mNewFocus = null;
                    mOldView = null;
                    mainUpView1.setUnFocusView(oldFocus);
                    mEffectNoDrawBridge.setVisibleWidget(true);
                }
            }
        });
        viewpager.setOffscreenPageLimit(4);
        viewpager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                switchTab(mOpenTabHost, position);
                OPENLOG.D("onPageSelected position:" + position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // viewPager 正在滚动中.
                OPENLOG.D("onPageScrolled position:" + position + " positionOffset:" + positionOffset);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state) {
                    case ViewPager.SCROLL_STATE_IDLE: // viewpager 滚动结束.
                        mainUpView1.setFocusView(mNewFocus, mOldView, 1.2f);
                        // 监听动画事件.
                        mEffectNoDrawBridge.setOnAnimatorListener(new OpenEffectBridge.NewAnimatorListener() {
                            @Override
                            public void onAnimationStart(OpenEffectBridge bridge, View view, Animator animation) {
                            }

                            @Override
                            public void onAnimationEnd(OpenEffectBridge bridge, View view, Animator animation) {
                                // 动画结束的时候恢复原来的时间. (这里只是DEMO)
                                mEffectNoDrawBridge.setTranDurAnimTime(OpenEffectBridge.DEFAULT_TRAN_DUR_ANIM);
                            }
                        });
                        // 让被挡住的焦点控件在前面.
                        if (mNewFocus != null)
                            mNewFocus.bringToFront();
                        OPENLOG.D("SCROLL_STATE_IDLE");
                        break;
                    case ViewPager.SCROLL_STATE_DRAGGING:
                        OPENLOG.D("SCROLL_STATE_DRAGGING");
                        break;
                    case ViewPager.SCROLL_STATE_SETTLING: // viewPager开始滚动.
                        mEffectNoDrawBridge.clearAnimator(); // 清除之前的动画.
                        mEffectNoDrawBridge.setTranDurAnimTime(0); // 避免边框从其它地方跑出来.
                        OPENLOG.D("SCROLL_STATE_SETTLING");
                        break;
                }
            }
        });
        // 初始化.
        viewpager.setCurrentItem(0);
        switchTab(mOpenTabHost, 0);
    }

    private void initMoveBridge() {
        float density = getResources().getDisplayMetrics().density;
        mainUpView1 = (MainUpView) findViewById(R.id.mainUpView1);
        mEffectNoDrawBridge = new EffectNoDrawBridge();
        mainUpView1.setEffectBridge(mEffectNoDrawBridge);
        mEffectNoDrawBridge.setUpRectResource(R.drawable.white_light_10); // 设置移动边框图片.
        RectF rectF = new RectF(getDimension(R.dimen.w_10) * density, getDimension(R.dimen.h_10) * density,
                getDimension(R.dimen.w_10) * density, getDimension(R.dimen.h_10) * density);
        mEffectNoDrawBridge.setDrawUpRectPadding(rectF);
    }



    @Override
    public void onTabSelect(OpenTabHost openTabHost, View titleWidget, int position) {
        if (viewpager != null) {
            viewpager.setCurrentItem(position);
        }
    }

    /**
     * demo (翻页的时候改变状态)
     * 将标题栏的文字颜色改变. <br>
     * 你可以写自己的东西，我这里只是DEMO.
     */
    public void switchTab(OpenTabHost openTabHost, int postion) {
        List<View> viewList = openTabHost.getAllTitleView();
        for (int i = 0; i < viewList.size(); i++) {
            TextViewWithTTF view = (TextViewWithTTF) openTabHost.getTitleViewIndexAt(i);
            if (view != null) {
                Resources res = view.getResources();
                if (res != null) {
                    if (i == postion) {
                        view.setTextColor(res.getColor(android.R.color.white));
                        view.setTypeface(null, Typeface.BOLD);
                        view.setSelected(true); // 为了显示 失去焦点，选中为 true的图片.
                    } else {
                        view.setTextColor(res.getColor(R.color.white_50));
                        view.setTypeface(null, Typeface.NORMAL);
                        view.setSelected(false);
                    }
                }
            }
        }
    }

}
