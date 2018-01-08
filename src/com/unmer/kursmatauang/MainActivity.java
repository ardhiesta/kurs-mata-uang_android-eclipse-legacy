package com.unmer.kursmatauang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import com.unmer.konversimatauang.R;

import android.support.v7.app.ActionBarActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {
	TextView tvHasil;
	Spinner spinnerMataUang1, spinnerMataUang2;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        tvHasil = (TextView) findViewById(R.id.tv_hasil);
        spinnerMataUang1 = (Spinner) findViewById(R.id.spinner_mata_uang1);
        spinnerMataUang2 = (Spinner) findViewById(R.id.spinner_mata_uang2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void lihatKurs(View view){
    	String mataUang1 = ambilSimbolMataUang(String.valueOf(spinnerMataUang1.getSelectedItem()));
    	String mataUang2 = ambilSimbolMataUang(String.valueOf(spinnerMataUang2.getSelectedItem()));
    	
    	PengaksesData pengaksesData = new PengaksesData();
    	String[] mataUangDikonversi = {mataUang1, mataUang2};
		pengaksesData.execute(mataUangDikonversi);
    }
    
    /* method ambilSimbolMataUang digunakan untuk mengambil 
     * 3 karakter pertama simbol mata uang yang ada di spinner
     * contohnya "IDR - Rupiah Indonesia", akan diambil IDR
     * dengan cara memisahkan String di antara " - ", akan didapat
     * "IDR" dan "Rupiah Indonesia", yang diambil adalah "IDR" */
    private String ambilSimbolMataUang(String namaMataUang){
    	String[] mataUang = namaMataUang.split(" - ");
    	String simbolMataUang = mataUang[0];
    	return simbolMataUang;
    }
    
    /* method ubahFormatPenulisanTanggal digunakan untuk mengubah penulisan tanggal
     * dari sebelumnya format yyyy-MM-dd misalnya 2018-01-08
     * menjadi format dd-MM-yyyy, misalnya 08-01-2018 */
    private String ubahFormatPenulisanTanggal(String dataTglKurs){
    	String dataTglKursSudahDiformat = "";
    	try {
			SimpleDateFormat dateFormatAwal = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat dateFormatAkhir = new SimpleDateFormat("dd-MM-yyyy");
			Date tglKurs = dateFormatAwal.parse(dataTglKurs);
			dataTglKursSudahDiformat = dateFormatAkhir.format(tglKurs);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return dataTglKursSudahDiformat;
    }
    
    class PengaksesData extends AsyncTask<String, String, JSONObject> {
    	@Override
		protected void onPreExecute() {
    		// menampilkan tulisan loading
    		tvHasil.setText("loading...");
    	}
    	
		@Override
		protected JSONObject doInBackground(String... arg0) {
			JSONObject dataKurs = null;
			
			DefaultHttpClient httpClient = new DefaultHttpClient();
			
			// mataUang1 adalah mata uang asal yang akan dikonversi
			String mataUang1 = arg0[0];
			// mataUang2 adalah mata uang tujuan konversi
			String mataUang2 = arg0[1];
			
			// data nilai kurs mata uang di ambil dari api.fixer.io
			HttpGet httpGet = new HttpGet(
					"https://api.fixer.io/latest?base="+mataUang1+"&symbols="+mataUang2);
			
			// mengambil response dari server, hasil pemanggilan data
			try {
				HttpResponse httpResponse = httpClient.execute(httpGet);
				HttpEntity httpEntity = httpResponse.getEntity();
				InputStream is = httpEntity.getContent();

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is, "iso-8859-1"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "n");
				}
				is.close();
				String hasil = sb.toString();

				dataKurs = new JSONObject(hasil);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return dataKurs;
		}
		
		@Override
		protected void onPostExecute(JSONObject dataKurs) {
			tvHasil.setText("");
			
			// menampilkan data hasil konversi kurs ke textView
			if (dataKurs.length() > 0) {
				try {
					// contoh response {"base":"SGD","date":"2018-01-08","rates":{"IDR":10081.0}}
					// dataKurs.getString("base") mengambil nilai "base"
					// akan didapatkan simbol mata uang awal yang dikonversi
					String mataUang1 = dataKurs.getString("base");
					
					// dataKurs.getJSONObject("rates") untuk mengambil nilai "rates"
					JSONObject jsonKurs = dataKurs.getJSONObject("rates");
					Iterator<?> keys = jsonKurs.keys();
					// mataUang2 untuk mengambil simbol mata uang tujuan konversi yang ada di "rates" 
					String mataUang2 = "";
					if (keys.hasNext()) {
					    mataUang2 = (String) keys.next();
					}
					
					// dataKurs.getString("date") untuk mengambil nilai "date"
					// didapat tanggal data kurs diperbarui dengan format yyyy-MM-dd, contoh "2018-01-08" 
					String dataTglKurs = dataKurs.getString("date");
					// ubahFormatPenulisanTanggal(dataTglKurs) untuk mengubah data tanggal kurs "2018-01-08" (yyyy-MM-dd)
					// menjadi "08-01-2018" (dd-MM-yyyy)
					String dataTglKursSudahDiformat = ubahFormatPenulisanTanggal(dataTglKurs);
					
					String hasil = "1 "+mataUang1+" = "+jsonKurs.getDouble(mataUang2)+" "+mataUang2
							+"\nData kurs tanggal: "+dataTglKursSudahDiformat;
					tvHasil.setText(hasil);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
    }
}
