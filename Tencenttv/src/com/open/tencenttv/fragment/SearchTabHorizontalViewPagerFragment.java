package com.open.tencenttv.fragment;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.open.androidtvwidget.bridge.EffectNoDrawBridge;
import com.open.androidtvwidget.view.MainUpView;
import com.open.androidtvwidget.view.OpenTabHost;
import com.open.tencenttv.R;
import com.open.tencenttv.WeakReferenceHandler;
import com.open.tencenttv.adapter.OpenTabTitleAdapter;
import com.open.tencenttv.adapter.RankPagerAdapter;
import com.open.tencenttv.bean.RankBean;
import com.open.tencenttv.json.RankJson;

/**
 * *****************************************************************************
 * *****************************************************************************
 * ******************
 * 
 * @author :fengguangjing
 * @createTime: 16/11/18
 * @version:
 * @modifyTime:
 * @modifyAuthor:
 * @description: 
 *               ****************************************************************
 *               ***************************************************************
 *               *********************************************
 */
public class SearchTabHorizontalViewPagerFragment extends CommonTabHorizontalViewPagerFragment implements OpenTabHost.OnTabSelectListener {
	public static final String TAG = SearchTabHorizontalViewPagerFragment.class.getSimpleName();
	FrameLayout layout_search;
	String pindaoName;

	public static SearchTabHorizontalViewPagerFragment newInstance(String url, String selectElement, String liElement, String astrElement, String pindaoName, MainUpView mainUpView1, View mOldView,
			EffectNoDrawBridge mEffectNoDrawBridge) {
		SearchTabHorizontalViewPagerFragment fragment = new SearchTabHorizontalViewPagerFragment();
		fragment.url = url;
		fragment.selectElement = selectElement;
		fragment.liElement = liElement;
		fragment.astrElement = astrElement;
		fragment.mainUpView1 = mainUpView1;
		fragment.mOldView = mOldView;
		fragment.mEffectNoDrawBridge = mEffectNoDrawBridge;
		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_pindao_tab_horizontal_viewpager, container, false);
		// 初始化viewpager.
		findView();
		layout_search = (FrameLayout) view.findViewById(R.id.layout_search);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		SearchHeadFragment rightFragment = SearchHeadFragment.newInstance(weakReferenceHandler, url, pindaoName, mainUpView1, mOldView, mRecyclerViewBridge);
		FragmentManager manager = getActivity().getSupportFragmentManager();
		manager.beginTransaction().replace(R.id.layout_search, rightFragment).commit();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		weakReferenceHandler = new WeakReferenceHandler(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.open.tencenttv.BaseV4Fragment#handlerMessage(android.os.Message)
	 */
	@Override
	public void handlerMessage(Message msg) {
		super.handlerMessage(msg);
		Log.i(TAG, "CurrentItem==" + viewpager.getCurrentItem() + ";url==" + msg.obj + ";what==" + msg.what);
		((SearchListFragment) listRankFragment.get(viewpager.getCurrentItem())).refreshHandlerMessage(msg.obj + "");
	}

	@Override
	public void onCallback(RankJson result) {
		super.onCallback(result);
		titlerankList.clear();
		titlerankList.addAll(result.getList());
		// 初始化标题栏.
		titleList.clear();
		listRankFragment.clear();
		for (int i = 0; i < result.getList().size(); i++) {
			RankBean bean = result.getList().get(i);
			if (!bean.getRankName().contains("筛选") && !bean.getRankName().contains("返回")) {
				titleList.add(bean.getRankName());
				SearchListFragment fragment = SearchListFragment.newInstance(url, mainUpView1, mOldView, mRecyclerViewBridge);
				listRankFragment.add(fragment);
			}
		}
		mOpenTabTitleAdapter = new OpenTabTitleAdapter(getActivity(), titleList);
		mOpenTabHost.setAdapter(mOpenTabTitleAdapter);

		// mRankViewPagerAdapter = new
		// RankViewPagerAdapter(getActivity(),result.getTitlerankList());
		mRankPagerAdapter = new RankPagerAdapter(getChildFragmentManager(), listRankFragment, titleList);
		viewpager.setAdapter(mRankPagerAdapter);

		mFocusHandler.sendEmptyMessageDelayed(10, 5000);
	}

}
