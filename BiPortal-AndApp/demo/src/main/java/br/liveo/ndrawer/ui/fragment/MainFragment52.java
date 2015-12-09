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

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import br.liveo.ndrawer.R;
import br.liveo.ndrawer.ui.activity.GalleryActivity;
import br.liveo.ndrawer.ui.activity.MainActivity;
import br.liveo.ndrawer.ui.adapter.StealInfo;
import br.liveo.ndrawer.ui.adapter.StealInfoAdapter;

// 내정보 - 탭 중에서 등록자전거 화면
public class MainFragment52 extends Fragment {

	private boolean mSearchCheck;
	private static final String TEXT_FRAGMENT = "TEXT_FRAGMENT";
	private StealInfoAdapter mStealAdapter;
	private ArrayList<StealInfo> mStealList;
	View rootView;

	public static MainFragment52 newInstance(String text){
		MainFragment52 mFragment = new MainFragment52();
		Bundle mBundle = new Bundle();
		mBundle.putString(TEXT_FRAGMENT, text);
		mFragment.setArguments(mBundle);
		return mFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// TODO Auto-generated method stub		
		rootView = inflater.inflate(R.layout.fragment_main52, container, false);
		rootView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT ));

		mStealAdapter = new StealInfoAdapter(getContext(), R.layout.steal);
		mStealList = new ArrayList<StealInfo>();

		rootView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT ));
		return rootView;
	}

	private void getStealInfoList(){
		Log.i("뭔데", "왜 나오던게 안나와>");
		GetStealInfoList getStealInfoList = new GetStealInfoList();
		getStealInfoList.execute();
	}

	private class GetStealInfoList extends AsyncTask<String, Void, String> {

		String url = null;
		String useremail = null;
		String response;
		// Invoked by execute() method of this object

		@Override
		protected String doInBackground(String... params) {
			Log.i("뭔데", "나랑 장난하니?");
			/*try {
				url = params[0];
				useremail = params[1];
				beaconmac = params[2];
				beaconname = params[3];
				RequestClass rc = new RequestClass(url);
				rc.AddParam("useremail", useremail);
				rc.AddParam("beaconmac", beaconmac);
				rc.AddParam("beaconname", beaconname);
				rc.Execute(1);
				response = rc.getResponse();
			}catch(Exception e){
				e.printStackTrace();
			}
			return response;*/
			return "good";
		}

		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(String response) {
			Log.i("return respone", response);
			for(int i = 0; i < 5; i++){
				String transI = String.valueOf(i);
				StealInfo pt = null;
				if(i % 2 == 0){
					pt = new StealInfo("steal", "date"+i, "addr"+i);
				} else {
					pt = new StealInfo("vibration", "date"+i, "addr"+i);
				}

				mStealList.add(pt);
			}

			// ArrayList 저장 끝나면 리스트 만들기
			mStealAdapter.setStealInfoList(mStealList);

			ListView list;
			list = (ListView)rootView.findViewById(R.id.steal);
			list.setAdapter(mStealAdapter);

			// 클릭했을 때 디테일 페이지 또는 다이알로그 xml 만들어야 함

			list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					startGalleryActivity();
				/*final String category = mStealAdapter.getStealInfoList().get(position).getCategory();
				final String date = mStealAdapter.getStealInfoList().get(position).getDate();
				final String addr = mStealAdapter.getStealInfoList().get(position).getAddr();

				Toast toast  = Toast.makeText(getContext(), category + "/" + date + "/" + addr, Toast.LENGTH_LONG);
				toast.show();*/
				}
			});
			/*if (response.length() != 0) {
				successDialog();
			} else {
				failDialog();
			}*/
		}
	}

	private void startGalleryActivity() {
		ArrayList<String> images = new ArrayList<String>();
		images.add("http://sourcey.com/images/stock/salvador-dali-metamorphosis-of-narcissus.jpg");
		images.add("http://sourcey.com/images/stock/salvador-dali-the-dream.jpg");
		images.add("http://sourcey.com/images/stock/salvador-dali-persistence-of-memory.jpg");
		images.add("http://sourcey.com/images/stock/simpsons-persistence-of-memory.jpg");
		images.add("http://sourcey.com/images/stock/salvador-dali-the-great-masturbator.jpg");
		images.add("http://sourcey.com/images/stock/salvador-dali-metamorphosis-of-narcissus.jpg");
		images.add("http://sourcey.com/images/stock/salvador-dali-the-dream.jpg");
		images.add("http://sourcey.com/images/stock/salvador-dali-persistence-of-memory.jpg");
		images.add("http://sourcey.com/images/stock/simpsons-persistence-of-memory.jpg");
		images.add("http://sourcey.com/images/stock/salvador-dali-the-great-masturbator.jpg");
		Intent intent = new Intent(getActivity(), GalleryActivity.class);
		intent.putStringArrayListExtra(GalleryActivity.EXTRA_NAME, images);
		startActivity(intent);
	}

	@Override
	public void onResume(){
		getStealInfoList();
		super.onResume();
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
	}
	
	/*@Override
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

		*//*case R.id.menu_add:
            Toast.makeText(getActivity(), R.string.add, Toast.LENGTH_SHORT).show();
			break;*//*

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
   };*/
}
