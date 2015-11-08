/*
 * Copyright 2015 Rudson Lima
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.liveo.ndrawer.ui.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import br.liveo.ndrawer.R;
import br.liveo.ndrawer.ui.adapter.PreTracking;
import br.liveo.ndrawer.ui.adapter.PreTrackingAdapter;


// 트레킹 - 탭 중에서 과거이력 화면
public class MainFragment12 extends Fragment {

    private boolean mSearchCheck;
    private static final String TEXT_FRAGMENT = "TEXT_FRAGMENT";
	private PreTrackingAdapter mPreTrackingAdapter;
	private ArrayList<PreTracking> mPreTrackingList;

	public static MainFragment12 newInstance(String text){
		MainFragment12 mFragment = new MainFragment12();
		Bundle mBundle = new Bundle();
		mBundle.putString(TEXT_FRAGMENT, text);
		mFragment.setArguments(mBundle);
		return mFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub		
		View rootView = inflater.inflate(R.layout.fragment_main12, container, false);
		rootView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT ));

		mPreTrackingAdapter = new PreTrackingAdapter(getContext(), R.layout.pretracking);
		mPreTrackingList = new ArrayList<PreTracking>();

		// 실제로 여기서 디비에 가서 저장된 과거 이력 데이터 불러와서
		// new PreTracking에 으로 만들어서 ArrayList 저장
		// 현재는 이런식으로 보여주면 어떨가 해서 값을 스태틱하게 넣은 상태

		for(int i = 0; i < 5; i++){
			String transI = String.valueOf(i);
			PreTracking pt = new PreTracking("date"+i, "distance"+i, "time"+i, "addr"+i);
			mPreTrackingList.add(pt);
		}

		// ArrayList 저장 끝나면 리스트 만들기
		mPreTrackingAdapter.setPreTrackingList(mPreTrackingList);

		ListView list;
		list = (ListView)rootView.findViewById(R.id.preTracking);
		list.setAdapter(mPreTrackingAdapter);

		// 클릭했을 때 디테일 페이지 또는 다이알로그 xml 만들어야 함

		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final String date = mPreTrackingAdapter.getPreTrackingList().get(position).getDate();
				final String time = mPreTrackingAdapter.getPreTrackingList().get(position).getTime();
				final String distance = mPreTrackingAdapter.getPreTrackingList().get(position).getDistance();
				final String address = mPreTrackingAdapter.getPreTrackingList().get(position).getAddress();

				Toast toast  = Toast.makeText(getContext(), date + "/" + time + "/" + distance + "/" + address, Toast.LENGTH_LONG);
				toast.show();
			}
		});

		return rootView;		
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu, menu);
        
        //Select search item
        final MenuItem menuItem = menu.findItem(R.id.menu_search);
        menuItem.setVisible(true);

        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint(this.getString(R.string.search));

        ((EditText) searchView.findViewById(R.id.search_src_text))
                .setHintTextColor(getResources().getColor(R.color.nliveo_white));
        searchView.setOnQueryTextListener(onQuerySearchView);

		//menu.findItem(R.id.menu_add).setVisible(true);

		mSearchCheck = false;	
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		
		switch (item.getItemId()) {

		/*case R.id.menu_add:
            Toast.makeText(getActivity(), R.string.add, Toast.LENGTH_SHORT).show();
			break;*/

		case R.id.menu_search:
			mSearchCheck = true;
            //Toast.makeText(getActivity(), R.string.search, Toast.LENGTH_SHORT).show();
			break;
		}
		return true;
	}	

   private SearchView.OnQueryTextListener onQuerySearchView = new SearchView.OnQueryTextListener() {
       @Override
       public boolean onQueryTextSubmit(String s) {
           return false;
       }

       @Override
       public boolean onQueryTextChange(String s) {
           if (mSearchCheck){
               // implement your search here
           }
           return false;
       }
   };
}
