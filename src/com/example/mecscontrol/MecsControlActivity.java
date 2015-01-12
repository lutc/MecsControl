package com.example.mecscontrol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

import com.example.mecscontrol.VerticalSeekBar.OnSeekBarChangeListener;

public class MecsControlActivity extends Activity {

	protected static final long DELAY = 400;
	protected static final long Timer_Delay = 10000;
	private boolean commandping = true;

	private Boolean IsConnected = false;

	StringBuffer sb;
	Handler mHandler = new Handler();
	int len;
	SharedPreferences sp;

	private MenuItem mnitemConnect;
	private String volumes[] = new String[] { "setvolumeoff",
			"setvolumesevenfive", "setvolumesevenzero", "setvolumesixfive",
			"setvolumesixzero", "setvolumefivefive", "setvolumefivezero",
			"setvolumefourfive", "setvolumefourzero", "setvolumethreefive",
			"setvolumethreezero", "setvolumetwofive", "setvolumetwozero",
			"setvolumeonefive", "setvolumeonezero", "setvolumezerofive",
			"setvolumemax" };
	private String responses[] = new String[] { "zero", "five", "onezero",
			"onefive", "twozero", "twofive", "threezero", "threefive",
			"fourzero", "fourfive", "fivezero", "fivefive", "sixzero",
			"sixfive", "sevenzero", "sevenfive", "eightzero" };

	private View history;

	private View threed;

	private String Fav3D;

	private String Fav2D;
	private boolean readOutputThreadisCancelled = false;
	Handler timerHandler = new Handler();
	Runnable timerRunnable = new Runnable() {

		@Override
		public void run() {
			new TelnetCommandPing()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			// new TelnetCommandPing().execute();
			// TelnetControl.INSTANCE.SendCommand("list");
			if (mnitemConnect != null)
				mnitemConnect.setChecked(TelnetControl.INSTANCE
						.GetIsConnected());

			if (readOutputThreadisCancelled) {
				Log.w("MECS", "ReadOutPut Is Cancelled");
				if (TelnetControl.INSTANCE.GetIsConnected()) {
					readOutputThreadisCancelled = false;
					new TelnetReadOutput()
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
			}

			if (commandping)
				timerHandler.postDelayed(this, Timer_Delay);
		}
	};

	private List<String> responseList;
	private View threed2;
	private String Fav3D2;
	private boolean readoutput = true;
	private View progressbar;
	private View progressbackground;
	private View progresstext;
	private AsyncTask<Void, String, Void> readOutputThread;
	public boolean MecsUnresponseble;
	public boolean ProjectorUnresponseble;
	public int Trying = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		responseList = Arrays.asList(responses);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		sp = PreferenceManager.getDefaultSharedPreferences(this);

		setContentView(R.layout.activity_fullscreen);
		Connect();

		View vimediaplay = findViewById(R.id.btnPlay);
		vimediaplay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TelnetControl.INSTANCE.SendCommand("command Vimedia play");
			}
		});

		View vimediastop = findViewById(R.id.btnStop);
		vimediastop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TelnetControl.INSTANCE.SendCommand("command Vimedia stop");
			}
		});

		View vimediapause = findViewById(R.id.btnPause);
		vimediapause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TelnetControl.INSTANCE.SendCommand("command Vimedia pause");
			}
		});

		View vimediafavprev = findViewById(R.id.btnEnablePrj);
		vimediafavprev.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TelnetControl.INSTANCE
						.SendCommand("command Mitsubishi poweron");
				TelnetControl.INSTANCE.SendCommand("command Sonyone poweron");
				TelnetControl.INSTANCE.SendCommand("command Sonytwo poweron");

				TelnetControl.INSTANCE.SendCommand("command Sonyone muteoff");
				TelnetControl.INSTANCE.SendCommand("command Sonytwo muteoff");
			}
		});

		View vimediafavnext = findViewById(R.id.btnDisablePrj);
		vimediafavnext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new ConfirmProjectorsDisable(MecsControlActivity.this).show();
			}
		});

		View vimediafav1 = findViewById(R.id.btnPowerViMedia);
		vimediafav1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new ConfirmPCDisable(MecsControlActivity.this).show();
			}
		});

		View btn3DEnable = findViewById(R.id.btn3DMenu);
		btn3DEnable.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				TelnetControl.INSTANCE.SendCommand("command Mitsubishi menu");
				Open3DMode();
			}
		});

		history = findViewById(R.id.btnHistory);
		threed = findViewById(R.id.btn3D);
		threed2 = findViewById(R.id.btn3D2);
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);

		Fav3D = sharedPreferences.getString("THREEDFAV", "no selection");
		Fav3D2 = sharedPreferences.getString("THREEDFAV2", "no selection");

		Fav2D = sharedPreferences.getString("TWODFAV", "no selection");

		history.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				TelnetControl.INSTANCE.SendCommand("command Vimedia stop");

				TelnetControl.INSTANCE.SendCommand("command Sonyone muteoff");
				TelnetControl.INSTANCE.SendCommand("command Sonytwo muteoff");

				waitEx(DELAY);
				TelnetControl.INSTANCE.SendCommand("command Vimedia " + Fav2D);
				waitEx(DELAY / 2);
				TelnetControl.INSTANCE.SendCommand("command Vimedia play");

			}
		});

		threed.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				TelnetControl.INSTANCE.SendCommand("command Vimedia stop");
				long DELAY = 2000;
				TelnetControl.INSTANCE.SendCommand("command Sonyone muteon ");
				TelnetControl.INSTANCE.SendCommand("command Sonytwo muteon");
				waitEx(DELAY);
				TelnetControl.INSTANCE.SendCommand("command Vimedia " + Fav3D);
				waitEx(DELAY / 2);
				TelnetControl.INSTANCE.SendCommand("command Vimedia play");
			}
		});

		threed2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				TelnetControl.INSTANCE.SendCommand("command Vimedia stop");
				long DELAY = 2000;
				TelnetControl.INSTANCE.SendCommand("command Sonyone muteon ");
				TelnetControl.INSTANCE.SendCommand("command Sonytwo muteon");
				waitEx(DELAY);
				TelnetControl.INSTANCE.SendCommand("command Vimedia " + Fav3D2);
				waitEx(DELAY / 2);
				TelnetControl.INSTANCE.SendCommand("command Vimedia play");
			}
		});

		View v = findViewById(R.id.sbVolume);
		final VerticalSeekBar bar = (VerticalSeekBar) v;
		bar.setMax(16);
		Variables.INSTANCE.readVolumeFromSettings(this);
		bar.setProgress(Variables.INSTANCE.getVolume());

		bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(VerticalSeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(VerticalSeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(VerticalSeekBar seekBar,
					int progress, boolean fromUser) {
				if (progress < volumes.length) {
					TelnetControl.INSTANCE.SendCommand("command Apart "
							+ volumes[progress]);
					Variables.INSTANCE.setVolume(progress);
					Variables.INSTANCE
							.writeVolumetoSettings(MecsControlActivity.this);
				}
			}
		});

		progressbar = findViewById(R.id.progressBar1);
		progressbar.setVisibility(View.VISIBLE);
		progressbackground = findViewById(R.id.imageView1);
		progressbackground.setVisibility(View.VISIBLE);
		progresstext = findViewById(R.id.txtAlarmConnection);
		progresstext.setVisibility(View.VISIBLE);
		timerHandler.postDelayed(timerRunnable, 500);
		// new TelnetCommandPing()
		// .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		new TelnetReadOutput()
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	protected void Open3DMode() {
		new SelectProjectorMode(this).show();
		// showDialog(IDD_THREE_BUTTONS);
		//
		// Intent intent = new Intent(this, Select3Dmode.class);
		// startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.add(R.string.action_settings).setOnMenuItemClickListener(
				new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						OpenSettings();
						return true;
					}
				});
		mnitemConnect = menu.add(R.string.Connect);
		mnitemConnect.setCheckable(true);
		mnitemConnect.setChecked(TelnetControl.INSTANCE.GetIsConnected());
		mnitemConnect.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem item) {

				Connect();
				return true;
			}
		});

		return true;
	}

	public void OpenSettings() {
		Intent intent = new Intent(this, Settings.class);
		startActivity(intent);
	}

	public void Connect() {
		IsConnected = !IsConnected;
		if (mnitemConnect != null)
			mnitemConnect.setChecked(IsConnected);
		if (IsConnected) {
			try {
				String address = "http://"
						+ sp.getString("address", "192.168.1.119");

				ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
				if (networkInfo != null && networkInfo.isConnected()) {
					new TelnetTask().executeOnExecutor(
							AsyncTask.THREAD_POOL_EXECUTOR, address);// (address);
				} else {
					Log.d("me", "No network connection available.");
					// textView.setText("No network connection available.");
				}

			} catch (Exception e) {
				Log.e("log_tag", "Error in http connection " + e.toString());
			}
		} else
			TelnetControl.INSTANCE.Disconnect();
	}

	private void waitEx(long mil) {
		try {
			Thread.sleep(mil);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private class TelnetTask extends AsyncTask<String, Void, Void> {
		private boolean resultConnection;

		@Override
		protected Void doInBackground(String... urls) {
			try {
				URL url = new URL(urls[0]);
				resultConnection = TelnetControl.INSTANCE
						.Connect(url.getHost());

			} catch (IOException e) {
				Log.e("log_tag", "Error in http connection " + e.toString());
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (mnitemConnect != null)
				mnitemConnect.setChecked(resultConnection);
			TelnetControl.INSTANCE.SendCommand("list");
			super.onPostExecute(result);
		}

	}

	private void ChangeVisibilityOfProgress(boolean visibility) {
		View progressbar = findViewById(R.id.progressBar1);
		View progressbackground = findViewById(R.id.imageView1);
		View progresstext = findViewById(R.id.txtAlarmConnection);
		if (visibility) {
			progressbackground.setVisibility(View.VISIBLE);
			progressbar.setVisibility(View.VISIBLE);
			progresstext.setVisibility(View.VISIBLE);
		} else {
			progressbackground.setVisibility(View.INVISIBLE);
			progressbar.setVisibility(View.INVISIBLE);
			progresstext.setVisibility(View.INVISIBLE);
		}
	}

	private class TelnetCommandPing extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... arg0) {
			MecsUnresponseble = false;
			try {
				if (!TelnetControl.INSTANCE.GetIsConnected()) {
					Log.e("MECS", "Try to reconnect");
					MecsUnresponseble = true;
					TelnetControl.INSTANCE.Reconnect();
				} else {
					Log.i("MECS", "connected");
					InetAddress in = InetAddress.getByName("192.168.1.119");
					if (!in.isReachable((int) (Timer_Delay / 2))) {
						TelnetControl.INSTANCE.Disconnect();
						MecsUnresponseble = true;
					}
				}
			} catch (Exception e) {
				Log.e("MECS", "Beda0..." + e.getMessage());
				MecsUnresponseble = true;
			}

			ProjectorUnresponseble = false;
			try {
				InetAddress in = InetAddress.getByName("192.168.1.120");
				if (!in.isReachable((int) (Timer_Delay / 2))) {
					ProjectorUnresponseble = true;
				}

			} catch (Exception e) {
				Log.e("log_tag", "Error in http connection " + e.toString());
				ProjectorUnresponseble = true;
			}

			if (ProjectorUnresponseble && MecsUnresponseble) {
				Trying += 1;
			} else
				Trying = 0;

			if (Trying >= 3) {
				Trying = 0;
				WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				if ((wifi.getWifiState() != WifiManager.WIFI_STATE_DISABLED)) {
					Log.i("WIFI", "Turning off wifi");
					wifi.setWifiEnabled(false);
				}
				waitEx(1000);

				Log.i("WIFI", "Turning off wifi");
				wifi.setWifiEnabled(true);
			}

			try {
				InetAddress in = InetAddress.getByName("192.168.1.10");
				if (in.isReachable((int) (Timer_Delay / 2)))
					return true;
				else
					return false;

			} catch (Exception e) {
				Log.e("log_tag", "Error in http connection " + e.toString());
			}

			return null;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			View ven = findViewById(R.id.txtVideoEn);
			View vdis = findViewById(R.id.txtVideoDis);
			if (result) {

				ven.setVisibility(View.VISIBLE);
				vdis.setVisibility(View.INVISIBLE);
			} else {
				ven.setVisibility(View.INVISIBLE);
				vdis.setVisibility(View.VISIBLE);
			}

			try {
				ChangeVisibilityOfProgress(!TelnetControl.INSTANCE
						.GetIsConnected());

			} catch (Exception e) {
				Log.e("MECS", "Beda1..." + e.getMessage());
			}
			super.onPostExecute(result);
		}

	}

	@Override
	public void onBackPressed() {

		readoutput = false;
		commandping = false;

		finish();
		super.onBackPressed();

	}

	private class TelnetReadOutput extends AsyncTask<Void, String, Void> {

		private boolean firstTime = true;

		@Override
		protected Void doInBackground(Void... arg0) {

			try {
				TelnetControl.INSTANCE.SendCommand("list");
				waitEx(1000);
				InputStream in = TelnetControl.INSTANCE.getIn();
				if (in == null)
					return null;
				BufferedReader r = new BufferedReader(new InputStreamReader(in));

				String line;

				while (readoutput && (line = r.readLine()) != null) {

					publishProgress(line);
				}
			} catch (IOException e) {
				TelnetControl.INSTANCE.Disconnect();
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			Log.i("MECS", values[0]);
			String[] val = values[0].split(" ");
			View von;
			View vwait;
			View vwait2;
			View voff;
			if (val[0].compareTo("Sonyone") == 0) {
				von = findViewById(R.id.txtLeftEn);
				voff = findViewById(R.id.txtLeftDis);
				vwait = findViewById(R.id.txtLeftWait);

				voff.setVisibility(View.INVISIBLE);
				von.setVisibility(View.INVISIBLE);
				vwait.setVisibility(View.INVISIBLE);

				if (val[2].compareTo("enabled") == 0) {
					von.setVisibility(View.VISIBLE);

				} else if (val[2].compareTo("cooling") == 0
						|| val[2].compareTo("warmup") == 0) {
					vwait.setVisibility(View.VISIBLE);
				} else if (val[2].compareTo("unknown") == 0) {

				} else {
					voff.setVisibility(View.VISIBLE);
				}

			} else if (val[0].compareTo("Mitsubishi") == 0) {
				// Дурацкий проектор

			} else if (val[0].compareTo("Sonytwo") == 0) {
				von = findViewById(R.id.txtRightEn);
				voff = findViewById(R.id.txtRightDis);
				vwait = findViewById(R.id.txtRightWait);

				vwait2 = findViewById(R.id.txtMiddleWait);
				View von2 = findViewById(R.id.txtMidlEn);
				View voff2 = findViewById(R.id.txtMidDis);

				voff.setVisibility(View.INVISIBLE);
				von.setVisibility(View.INVISIBLE);
				vwait.setVisibility(View.INVISIBLE);
				vwait2.setVisibility(View.INVISIBLE);
				von2.setVisibility(View.INVISIBLE);
				voff2.setVisibility(View.INVISIBLE);

				if (val[2].compareTo("enabled") == 0) {
					von.setVisibility(View.VISIBLE);
					von2.setVisibility(View.VISIBLE);

				} else if (val[2].compareTo("cooling") == 0
						|| val[2].compareTo("warmup") == 0) {

					vwait.setVisibility(View.VISIBLE);
					vwait2.setVisibility(View.VISIBLE);
				} else if (val[2].compareTo("unknown") == 0) {

				} else {
					voff.setVisibility(View.VISIBLE);
					voff2.setVisibility(View.VISIBLE);
				}

			} else if (val[0].compareTo("Apart") == 0) {
				// if (responseList.contains(val[2])) {
				// if (firstTime) {
				// firstTime = false;
				// int progress = responseList.indexOf(val[2]);
				// View v = findViewById(R.id.sbVolume);
				// VerticalSeekBar bar = (VerticalSeekBar) v;
				// int iPr = bar.getMax() - progress - 1;
				// Log.i("MECS", Integer.toString(iPr));
				// bar.setProgress(iPr, true);
				// }
				// }
			}

			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Void result) {
			readOutputThreadisCancelled = true;
			super.onPostExecute(result);
		}

	}

	public class SelectProjectorMode {
		private Dialog dialog;
		private Activity activity;

		public SelectProjectorMode(Activity activity) {
			this.activity = activity;
			init();
		}

		private void init() {
			dialog = new Dialog(activity);
			dialog.setTitle(R.string.SelectProjectorMode);
			dialog.setContentView(R.layout.dlg_projector_mode);

			View up = dialog.findViewById(R.id.btnUp);
			View down = dialog.findViewById(R.id.btnDown);
			View left = dialog.findViewById(R.id.btnLeft);
			View right = dialog.findViewById(R.id.btnRight);
			View btnmenu = dialog.findViewById(R.id.btnMenu);
			up.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					try {
						TelnetControl.INSTANCE
								.SendCommand("command Mitsubishi up");
					} catch (Exception e) {
						Log.e("log_tag",
								"Error in http connection " + e.toString());
					}

				}
			});

			down.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					try {
						TelnetControl.INSTANCE
								.SendCommand("command Mitsubishi down");
					} catch (Exception e) {
						Log.e("log_tag",
								"Error in http connection " + e.toString());
					}

				}
			});

			right.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					try {
						TelnetControl.INSTANCE
								.SendCommand("command Mitsubishi left");
					} catch (Exception e) {
						Log.e("log_tag",
								"Error in http connection " + e.toString());
					}

				}
			});

			left.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					try {
						TelnetControl.INSTANCE
								.SendCommand("command Mitsubishi right");
					} catch (Exception e) {
						Log.e("log_tag",
								"Error in http connection " + e.toString());
					}

				}
			});

			btnmenu.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					TelnetControl.INSTANCE
							.SendCommand("command Mitsubishi menu");
				}
			});
		}

		public void show() {
			dialog.show();
		}
	}

	public class ConfirmProjectorsDisable {
		private Dialog dialog;
		private Activity activity;

		public ConfirmProjectorsDisable(Activity activity) {
			this.activity = activity;
			init();
		}

		private void init() {
			dialog = new Dialog(activity);
			dialog.setTitle(R.string.captionConfirmPrjDis);
			dialog.setContentView(R.layout.confirm_projectors_disable);

			View ok = dialog.findViewById(R.id.btnOk);
			View cancel = dialog.findViewById(R.id.btnCancel);

			ok.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					try {
						TelnetControl.INSTANCE
								.SendCommand("command Sonytwo poweroff");
						TelnetControl.INSTANCE
								.SendCommand("command Sonyone poweroff");
						TelnetControl.INSTANCE
								.SendCommand("command Mitsubishi poweroff");

					} catch (Exception e) {
						Log.e("log_tag",
								"Error in http connection " + e.toString());
					}
					dialog.dismiss();

				}
			});

			cancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// new TelnetCommand3D().execute();
					try {
						dialog.dismiss();

					} catch (Exception e) {
						Log.e("log_tag",
								"Error in http connection " + e.toString());
					}
				}
			});

		}

		public void show() {
			dialog.show();
		}
	}

	public class ConfirmPCDisable {
		private Dialog dialog;
		private Activity activity;

		public ConfirmPCDisable(Activity activity) {
			this.activity = activity;
			init();
		}

		private void init() {
			dialog = new Dialog(activity);
			dialog.setTitle(R.string.captionConfirmPCDis);
			dialog.setContentView(R.layout.confirm_projectors_disable);

			View ok = dialog.findViewById(R.id.btnOk);
			View cancel = dialog.findViewById(R.id.btnCancel);

			ok.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					try {
						TelnetControl.INSTANCE
								.SendCommand("command Kramer releonone");
						waitEx(200);
						TelnetControl.INSTANCE
								.SendCommand("command Kramer releoffone");

					} catch (Exception e) {
						Log.e("log_tag",
								"Error in http connection " + e.toString());
					}
					dialog.dismiss();

				}
			});

			cancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// new TelnetCommand3D().execute();
					try {
						dialog.dismiss();

					} catch (Exception e) {
						Log.e("log_tag",
								"Error in http connection " + e.toString());
					}
				}
			});

		}

		public void show() {
			dialog.show();
		}
	}

}
