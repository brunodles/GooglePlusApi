package com.example.testgoogleapi;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People.LoadPeopleResult;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.PersonBuffer;

public class MainActivity extends FragmentActivity implements
		ConnectionCallbacks, OnConnectionFailedListener, OnClickListener,
		ResultCallback<LoadPeopleResult> {

	private static final String STATE_RESOLVING_ERROR = "resolving_error";
	// Request code to use when launching the resolution activity
	private static final int REQUEST_RESOLVE_ERROR = 1001;
	// Unique tag for the error dialog fragment
	private static final String DIALOG_ERROR = "dialog_error";
	// Bool to track whether the app is already resolving an error
	private boolean mResolvingError = false;

	GoogleApiClient mGoogleApiClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);

		mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Plus.API)
				.addScope(Plus.SCOPE_PLUS_LOGIN)
				.addScope(Plus.SCOPE_PLUS_PROFILE).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();

		mResolvingError = savedInstanceState != null
				&& savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

		findViewById(R.id.sign_in_button).setOnClickListener(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!mResolvingError) { // more about this later
			Log.d("Plus Login", "onStart connect");
			mGoogleApiClient.connect();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
	}

	@Override
	protected void onStop() {
		mGoogleApiClient.disconnect();
		super.onStop();
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.d("Plus Login", "onConnectionFailed");
		if (mResolvingError) {
			// Already attempting to resolve an error.
			return;
		} else if (result.hasResolution()) {
			try {
				mResolvingError = true;
				result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
			} catch (SendIntentException e) {
				// There was an error with the resolution intent. Try again.
				Log.d("Plus Login", "OnConnectionFailed connect");
				mGoogleApiClient.connect();
			}
		} else {
			// Show dialog using GooglePlayServicesUtil.getErrorDialog()
			showErrorDialog(result.getErrorCode());
			mResolvingError = true;
		}
	}

	// The rest of this code is all about building the error dialog

	/* Creates a dialog for an error message */
	private void showErrorDialog(int errorCode) {
		Log.d("Plus Login", "showErrorDialog");
		// Create a fragment for the error dialog
		ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
		// Pass the error that should be displayed
		Bundle args = new Bundle();
		args.putInt(DIALOG_ERROR, errorCode);
		dialogFragment.setArguments(args);
		dialogFragment.show(getSupportFragmentManager(), "errordialog");
	}

	/* Called from ErrorDialogFragment when the dialog is dismissed. */
	public void onDialogDismissed() {
		mResolvingError = false;
	}

	/* A fragment to display an error dialog */
	public static class ErrorDialogFragment extends DialogFragment {
		public ErrorDialogFragment() {
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Get the error code and retrieve the appropriate dialog
			int errorCode = this.getArguments().getInt(DIALOG_ERROR);
			return GooglePlayServicesUtil.getErrorDialog(errorCode,
					this.getActivity(), REQUEST_RESOLVE_ERROR);
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			((MainActivity) getActivity()).onDialogDismissed();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("Plus Login", "onActivityResult");
		if (requestCode == REQUEST_RESOLVE_ERROR) {
			mResolvingError = false;
			if (resultCode == RESULT_OK) {
				// Make sure the app is not already connected or attempting to
				// connect
				if (!mGoogleApiClient.isConnecting()
						&& !mGoogleApiClient.isConnected()) {
					Log.d("Plus Login", "onActivityResult connect");
					mGoogleApiClient.connect();
				}
			}
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.d("Plus Login", "onConnected");

		Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(
				this);
	}

	@Override
	public void onConnectionSuspended(int cause) {
		Log.d("Plus Login", "onConnectionSuspended");
	}

	@Override
	public void onClick(View v) {
		// String account = Plus.AccountApi.getAccountName(mGoogleApiClient);
		String name = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient)
				.getDisplayName();
		Log.d("Bruno teste People", name);
		Toast.makeText(this, name, Toast.LENGTH_SHORT).show();

		Log.d("Bruno teste People", "Load People");
		// Plus.PeopleApi.loa
		// Plus.PeopleApi.load(mGoogleApiClient).setResultCallback(this);
	}

	@Override
	public void onResult(LoadPeopleResult people) {
		PersonBuffer personBuffer = people.getPersonBuffer();
		for (int i = 0; i < personBuffer.getCount(); i++) {
			Log.d("Bruno teste People", personBuffer.get(i).getDisplayName());
		}
	}

}
