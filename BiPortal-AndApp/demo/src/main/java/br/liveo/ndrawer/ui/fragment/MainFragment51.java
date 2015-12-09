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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

import br.liveo.ndrawer.R;
import br.liveo.ndrawer.ui.adapter.RequestClass;
import br.liveo.ndrawer.ui.mjpeg.DoRead;
import br.liveo.ndrawer.ui.mjpeg.MjpegInputStream;
import br.liveo.ndrawer.ui.mjpeg.MjpegView;
import br.liveo.ndrawer.ui.sensortag.MovementProfile;

// 도난 - 실시간화면
public class MainFragment51 extends Fragment {
	private boolean mSearchCheck;
	private static final String TEXT_FRAGMENT = "TEXT_FRAGMENT";

	private boolean showFps = false;
	private AlertDialog.Builder dialog;
	private static Button changeableButton;
	MjpegView mv;
	View rootView;

	// Stops scanning after 10 seconds.
	private static final long SCAN_PERIOD = 10000;

	public static MainFragment51 newInstance(String text){
		MainFragment51 mFragment = new MainFragment51();
		Bundle mBundle = new Bundle();
		mBundle.putString(TEXT_FRAGMENT, text);
		mFragment.setArguments(mBundle);

		return mFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// TODO Auto-generated method stub		
		rootView = inflater.inflate(R.layout.fragment_main51, container, false);
		mv = (MjpegView) rootView.findViewById(R.id.stream);
		changeableButton = (Button)rootView.findViewById(R.id.changeableButton);

		if(MovementProfile.mLastKnownMotionX > 50){
			Vibration vb = new Vibration();
			vb.execute();
		}

		changeableButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				changeButton();
			}
		});

		rootView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		return rootView;
	}

	private class Vibration extends AsyncTask<String, Void, String> {
		// Invoked by execute() method of this object
		String response = null;
		@Override
		protected String doInBackground(String... params) {
			try {
				String useremail = "a@ac.kr";
				RequestClass rc = new RequestClass("http://125.131.73.198:3000/vibration");
				rc.AddParam("useremail", useremail);
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
			if (response.length() != 0) {

			} else {

			}
		}
	}


	public void onPause() {
		super.onPause();
		mv.stopPlayback();
	}

	public void onStop() {
		super.onStop();
		mv.stopPlayback();
		mv.setBackgroundColor(Color.BLACK);
	}

	public View getFragmentView(){
		return rootView;
	}

	public void changeButton() {
		boolean isTextStart = "Start".equals(changeableButton.getText());

		if(isTextStart) {
			changeableButton.setText("Stop");
			start(getContext());
		}
		else {
			changeableButton.setText("Start");
			onStop();
		}
	}

	public void startStop(View v){
		if ( isConnection() ) {
			changeButton();
		} else {
			dialog.show();
		}
	}

	public boolean isConnection(){
		ConnectivityManager connMgr = (ConnectivityManager)
				getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}

	@Override
	public void onResume() {
		super.onResume();

		boolean isTextStart = "Start".equals(changeableButton.getText());
		if (!isTextStart) {
			start(getContext());
		}
	}


	public void start(final Context context) {
		// Write the correct ip of your local conection.
		// The port (8081) must not be changed
		String URL = "http://125.131.73.198:8081";

		DoRead.DoReadCallback callback = new DoRead.DoReadCallback() {
			@Override
			public void onFinish(MjpegInputStream result) {
				Log.wtf("MainFragment51", "onFinish");
				mv.setBackgroundColor(Color.TRANSPARENT);
				mv.setSource(result);
				mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
				showFps = !showFps;
				mv.showFps( showFps );
			}

			@Override
			public void onError(String errorMsg) {
				Log.wtf("Error", errorMsg);
				createDialog(context);
				dialog.show();
			}
		};

		new DoRead(callback, context).execute(URL);

	}

	public void createDialog(Context context) {
		dialog = new AlertDialog.Builder(context);
		dialog.setTitle("There is no video in the server");
		dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
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
