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
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import br.liveo.ndrawer.R;
import br.liveo.ndrawer.ui.activity.MainActivity;
import br.liveo.ndrawer.ui.adapter.BeaconDevice;
import br.liveo.ndrawer.ui.adapter.DeviceListAdapter;

// 등록 - 탭 중에서 자전거 화면
public class MainFragment32 extends Fragment {
    private boolean mSearchCheck;
    private static final String TEXT_FRAGMENT = "TEXT_FRAGMENT";
	private BluetoothAdapter mBluetoothAdapter;
	OnMainFragment32SelectedListener mCallback;

	public static MainFragment32 newInstance(String text){
		MainFragment32 mFragment = new MainFragment32();
		Bundle mBundle = new Bundle();
		mBundle.putString(TEXT_FRAGMENT, text);
		mFragment.setArguments(mBundle);

		return mFragment;
	}

	public interface OnMainFragment32SelectedListener{
		void onBtnListRefreshClicked();
		DeviceListAdapter getAdapter();
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnMainFragment32SelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnMainFragment32SelectedListener");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
	    View rootView = inflater.inflate(R.layout.fragment_main32, container, false);

 /*       TextView mTxtTitle = (TextView) rootView.findViewById(R.id.txtTitle32);
        mTxtTitle.setText(getArguments().getString(TEXT_FRAGMENT));*/

		rootView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		// TODO: List item 얻어오기
		mCallback.onBtnListRefreshClicked();
		// TODO: List 구성하기
		ListView list;
		list = (ListView)rootView.findViewById(R.id.deviceList);
		list.setAdapter(mCallback.getAdapter());

		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final String useremail = "onlyboys@kaist.ac.kr";
				final String addr = mCallback.getAdapter().getDeviceList().get(position).getBdAddr();
				final String name = mCallback.getAdapter().getDeviceList().get(position).getBdName();

				// TODO: 등록 팝업 띄우기
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
				// Setting Dialog Title
				alertDialog.setTitle("비콘 등록 확인");

				// Setting Dialog Message
				alertDialog.setMessage("선택하신 비콘을 등록하시겠습니까?");

				// Setting Icon to Dialog
				alertDialog.setIcon(R.drawable.ic_dialog_alert);

				// Setting Positive "Yes" Button
				alertDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int which) {
						// Write your code here to invoke YES event
						Toast toast  = Toast.makeText(getContext(), useremail + "/" + addr + "/" + name, Toast.LENGTH_LONG);
						toast.show();
						dialog.cancel();

						// 성공
						successDialog();

						// 실패
						//failDialog();

						// 서버로 가서 선택한 비콘 등록하기

					}
				});

				// Setting Negative "NO" Button
				alertDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// Write your code here to invoke NO event
						dialog.cancel();
					}
				});

				// Showing Alert Message
				alertDialog.show();

			}
		});

		Button deviceRefresh = (Button)rootView.findViewById(R.id.btnRefresh);
		// button events
		deviceRefresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallback.onBtnListRefreshClicked();
			}
		});
		return rootView;		
	}

	private void successDialog(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
		// Setting Dialog Title
		alertDialog.setTitle("비콘 등록 완료");

		// Setting Dialog Message
		alertDialog.setMessage("선택한 비콘이 등록되었습니다");

		// Setting Icon to Dialog
		alertDialog.setIcon(R.drawable.ic_dialog_alert);

		// Setting Positive "Yes" Button
		alertDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int which) {
				// Write your code here to invoke YES event
				dialog.cancel();

				Fragment mFragment;
				FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();

				mFragment = new ViewPagerFragment3();

				if (mFragment != null){
					mFragmentManager.beginTransaction().replace(R.id.container, mFragment).commit();
				}
			}
		});

		alertDialog.show();
	}

	private void failDialog(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
		// Setting Dialog Title
		alertDialog.setTitle("비콘 등록 실패");

		// Setting Dialog Message
		alertDialog.setMessage("다시 시도해 주세요");

		// Setting Icon to Dialog
		alertDialog.setIcon(R.drawable.ic_dialog_alert);

		// Setting Positive "Yes" Button
		alertDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int which) {
				// Write your code here to invoke YES event
				dialog.cancel();
			}
		});

		alertDialog.show();
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
