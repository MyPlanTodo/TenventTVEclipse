package com.open.tencenttv;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.open.androidtvwidget.bridge.EffectNoDrawBridge;
import com.open.androidtvwidget.view.ListViewTV;
import com.open.androidtvwidget.view.MainUpView;
import com.open.tencenttv.adapter.PinDaoAdapter;
import com.open.tencenttv.bean.CommonT;
import com.open.tencenttv.bean.PinDaoBean;
import com.open.tencenttv.bean.RankBean;
import com.open.tencenttv.fragment.PinDaoFragment;
import com.open.tencenttv.utils.UrlUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * *****************************************************************************
 * *****************************************************************************
 * ******************
 * 
 * @author :fengguangjing
 * @createTime: 16/11/2
 * @version:
 * @modifyTime:
 * @modifyAuthor:
 * @description: 
 *               ****************************************************************
 *               ***************************************************************
 *               *********************************************
 */
public class PinDaoActivity extends CommonFragmentActivity {
	private ListViewTV listView;
	private List<PinDaoBean> data = new ArrayList<PinDaoBean>();
	private List<Fragment> fragments;
	private PinDaoAdapter mPinDaoAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pindao);
		init();

	}

	@Override
	protected void findView() {
		super.findView();
		this.mInflater = LayoutInflater.from(getApplicationContext());
		listView = (ListViewTV) findViewById(R.id.listview);
		mainUpView1 = (MainUpView) findViewById(R.id.mainUpView1);
		doAsync(this, this, this);
	}

	@Override
	protected void initValue() {
		super.initValue();
		// 默认是 OpenEff...，建议使用 NoDraw... ...
		mainUpView1.setEffectBridge(new EffectNoDrawBridge());
		mRecyclerViewBridge = (EffectNoDrawBridge) mainUpView1.getEffectBridge();
		mRecyclerViewBridge.setTranDurAnimTime(200);
		mainUpView1.setUpRectResource(R.drawable.white_light_10); // 设置移动边框的图片.
		mainUpView1.setDrawUpRectPadding(new Rect(25, 25, 23, 23)); // 边框图片设置间距

		mPinDaoAdapter = new PinDaoAdapter(this, data);
		listView.setAdapter(mPinDaoAdapter);
	}

	@Override
	protected void bindEvent() {
		super.bindEvent();

		listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				System.out.println("listView item" + view.getId() + ";postion=" + (int) id + " ========onItemSelected ");
				if (view != null) {
					view.bringToFront();
					mRecyclerViewBridge.setFocusView(view, mOldView, 1.1f);
					mOldView = view;
				}
				setSelectedFragment((int) id);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				System.out.println("listView item" + " ========onNothingSelected ");
			}
		});

		listView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean b) {
				// 失去焦点时，将子view还原
				System.out.println("listView item" + view.getId() + " ========onFocusChange " + b);
				if (!b) {
					for (int i = 0; i < listView.getChildCount(); i++) {
						View mvView = listView.getChildAt(i);
						mRecyclerViewBridge.setUnFocusView(mvView);
					}
				}

			}
		});

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (view != null) {
					view.bringToFront();
					mRecyclerViewBridge.setFocusView(view, mOldView, 1.1f);
					mOldView = view;
				}
				System.out.println("listView item" + (int) id + " ========onItemClick ");
				Toast.makeText(getApplicationContext(), "position : " + position, Toast.LENGTH_LONG).show();
				setSelectedFragment((int) id);

			}
		});

	}

	/**
	 * 选择fragment
	 * 
	 * @param position
	 */
	private void setSelectedFragment(int position) {
		Fragment fragment = fragments.get(position);
		FragmentManager manager = getSupportFragmentManager();
		manager.beginTransaction().replace(R.id.layout_pindao, fragment).commit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.open.tencenttv.BaseFragmentActivity#call()
	 */
	@Override
	public CommonT call() throws Exception {
		CommonT mCommonT = new CommonT();
		List<PinDaoBean> list = parseSidenavi(UrlUtils.TENCENT_X_MOVIE_LIST);
		mCommonT.setSidenavilist(list);
		return mCommonT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.open.tencenttv.BaseFragmentActivity#onCallback(com.open.tencenttv
	 * .bean.CommonT)
	 */
	@Override
	public void onCallback(CommonT result) {
		// TODO Auto-generated method stub
		super.onCallback(result);
		data.clear();
		data.addAll(result.getSidenavilist());
		mPinDaoAdapter.notifyDataSetChanged();
		fragments = new ArrayList<Fragment>();
		for (int i = 0; i < data.size(); i++) {
			fragments.add(PinDaoFragment.newInstance(data.get(i).getHrefurl(), data.get(i).getTypeName(), mainUpView1, mOldView, mRecyclerViewBridge));
		}
		// 延时请求其它位置的item.
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				listView.setDefaultSelect(0);
				setSelectedFragment(0);
			}
		};
		handler.sendMessageDelayed(handler.obtainMessage(), 3000);
	}

	public ArrayList<PinDaoBean> parseSidenavi(String href) {
		ArrayList<PinDaoBean> list = new ArrayList<PinDaoBean>();
		try {
			href = makeURL(href, new HashMap<String, Object>() {
				{
				}
			});
			Log.i("url", "url = " + href);

			Document doc = Jsoup.connect(href).userAgent(UrlUtils.userAgent).timeout(10000).get();
			Element masthead = doc.select("ul.side_navi").first();
			Elements liElements = masthead.select("li");

			/**
			 * <li class="item current"><a _boss="movie" href=
			 * "http://v.qq.com/x/movielist/?cate=10001&#38;offset=0&#38;sort=4"
			 * >电影</a></li>
			 */
			// 解析文件
			for (int i = 0; i < liElements.size(); i++) {
				PinDaoBean bean = new PinDaoBean();
				try {
					try {
						Element aElement = liElements.get(i).select("a").first();
						String typeName = aElement.text();
						String hrefurl = aElement.attr("href");
						System.out.print("i===" + i + ";typeName ===" + typeName + ";hrefurl===" + hrefurl);
						bean.setTypeName(typeName);
						bean.setHrefurl(hrefurl);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				list.add(bean);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	// public void initData() {
	// PinDaoBean bean = new PinDaoBean();
	// bean.setType(0);
	// bean.setTypeName("筛选");
	// data.add(bean);
	//
	// bean = new PinDaoBean();
	// bean.setType(1);
	// bean.setTypeName("搜索");
	// data.add(bean);
	//
	// bean = new PinDaoBean();
	// bean.setType(2);
	// bean.setTypeName("大剧精选");
	// data.add(bean);
	//
	// bean = new PinDaoBean();
	// bean.setType(2);
	// bean.setTypeName("卫视同步");
	// data.add(bean);
	//
	// bean = new PinDaoBean();
	// bean.setType(2);
	// bean.setTypeName("抗战风云");
	// data.add(bean);
	//
	// bean = new PinDaoBean();
	// bean.setType(2);
	// bean.setTypeName("会员专享");
	// data.add(bean);
	//
	// bean = new PinDaoBean();
	// bean.setType(2);
	// bean.setTypeName("热门话题");
	// data.add(bean);
	//
	// bean = new PinDaoBean();
	// bean.setType(2);
	// bean.setTypeName("剧星专场");
	// data.add(bean);
	//
	// bean = new PinDaoBean();
	// bean.setType(2);
	// bean.setTypeName("乡里乡亲");
	// data.add(bean);
	//
	// fragments = new ArrayList<Fragment>();
	// for (int i = 0; i < data.size(); i++) {
	// fragments.add(PinDaoFragment.newInstance(data.get(i).getTypeName(),
	// mainUpView1, mOldView, mRecyclerViewBridge));
	// }
	//
	// }

}
