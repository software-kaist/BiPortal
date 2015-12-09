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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import br.liveo.ndrawer.R;
import br.liveo.ndrawer.ui.adapter.CourseInfo;
import br.liveo.ndrawer.ui.adapter.CourseInfoAdapter;
import br.liveo.ndrawer.ui.adapter.RequestClass;

// 추천 - 탭 중에서 코스 화면
public class MainFragment21 extends Fragment {

    private boolean mSearchCheck;
    private static final String TEXT_FRAGMENT = "TEXT_FRAGMENT";

	private CourseInfoAdapter mCourseInfoAdapter;
	private ArrayList<CourseInfo> mCourseInfoList;
	View rootView;

	static Bundle mBundle;

	public static MainFragment21 newInstance(String text){
		MainFragment21 mFragment = new MainFragment21();
		mBundle = new Bundle();
		mBundle.putString(TEXT_FRAGMENT, text);
		mFragment.setArguments(mBundle);
		return mFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub		
		rootView = inflater.inflate(R.layout.fragment_main21, container, false);
		
		rootView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		return rootView;		
	}

	private void getAllRecoCourseList() {
		String url = "http://125.131.73.198:3000/getAllRecoCourseList";
		GetAllRecoCourseList getAllRecoCourseList = new GetAllRecoCourseList();
		getAllRecoCourseList.execute(url);
	}

	private class GetAllRecoCourseList extends AsyncTask<String, Void, String> {
		String url = null;
		String response;
		// Invoked by execute() method of this object
		@Override
		protected String doInBackground(String... params) {
			try {
				url = params[0];
				RequestClass rc = new RequestClass(url);
				rc.Execute(1);
				response = rc.getResponse();
			}catch(Exception e){
				e.printStackTrace();
			}
			return response;
		}

		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(String response) {
			mCourseInfoAdapter = new CourseInfoAdapter(getContext(), R.layout.course, mBundle);
			mCourseInfoList = new ArrayList<CourseInfo>();

			try {
				JSONArray arr = new JSONArray(response);
				for (int i = 0; i < arr.length(); i++) {
					JSONObject obj = arr.getJSONObject(i);
					CourseInfo ri = new CourseInfo(obj.getInt("coursenum"), obj.getString("coursename"), obj.getDouble("coursestartlat"),
							obj.getDouble("coursestatlng"), obj.getDouble("courseendlat"), obj.getDouble("courseendlng"),
							obj.getDouble("courselength"), obj.getInt("courseage"), obj.getInt("coursehard"), obj.getString("coursesex"));
					mCourseInfoList.add(ri);
				}
			} catch(Exception e){
				e.printStackTrace();
			}

			mCourseInfoAdapter.setCourseInfoList(mCourseInfoList);

			ListView list;
			list = (ListView)rootView.findViewById(R.id.courseList);
			list.setAdapter(mCourseInfoAdapter);

			list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				}
			});

		}
	}

	@Override
	public void onResume(){
		super.onResume();
		getAllRecoCourseList();
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
