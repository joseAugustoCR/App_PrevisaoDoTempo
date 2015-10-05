package com.example.guto.previsaodotempo;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    // URL para acesso as informações do tempo para SM
    String URL = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20%28select%20woeid%20from%20geo.places%281%29%20where%20text%3D%22santa%20maria%20rs%2C%20br%22%29&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

    private  TextView temperaturaTextView;
    private TextView descricaoTextView;
    private TextView umidadeTextView;
    private TextView pressaoTextView;

    private String temperatura="";
    private String descricao="";
    private String pressao="";
    private String umidade="";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtém a referência para os componentes do layout
        temperaturaTextView = (TextView)findViewById(R.id.temperaturaTextView);
        descricaoTextView = (TextView)findViewById(R.id.descricaoTextView);
        umidadeTextView = (TextView)findViewById(R.id.umidadeTextView);
        pressaoTextView = (TextView)findViewById(R.id.pressaoTextView);

        // Inicia a AsyncTask que vai obter as informações da URL
        new MyAsyncTask().execute();
    }


    // AsyncTask para conexão HTTP em background
    private class MyAsyncTask extends AsyncTask<Void, Void, String>{


        @Override
        protected String doInBackground(Void... voids) {

            String response="";
            BufferedReader br = null;
            try {
                // Cria URL
                URL url  = new URL(URL);
                // Abre uma conexão
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                // Set input (recebimento de dados)
                conn.setDoInput(true);

                // Outros parâmetros que podem ser configurados - não necessário nessa aplicação
           /*
                // Timeouts
                 conn.setReadTimeout(2000);
                conn.setConnectTimeout(2000);
                conn.setRequestMethod("POST");      // Método de requisição
                conn.setDoOutput(true);                     // Output (envio de dados)
                */

                /* Para fazer o envio de dados, é preciso obter o OutputStream

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(data);   // data é um ArrayList de NameValuePairs contendo os dados a serem enviados
                    writer.flush();
                    writer.close();
                    os.close();

                 */

                // Obtém código da resposta
                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Realiza a leitura da resposta, linha por linha
                    String line;

                    br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                }




            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // Por fim, fecha o input stream
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


            // Decodifica as informações obtidas como resposta
            try{
                // Cria um JSONObject com a resposta
                JSONObject responseJSONObject = new JSONObject(response);

                // Obtém o JSONObject "query" dentro do responseJSONObject
                JSONObject queryJSONObject = responseJSONObject.getJSONObject("query");


                // Obtém o JSONObject "result" dentro do queryJSONObject
                JSONObject resultsJSONObject = queryJSONObject.getJSONObject("results");

                // Obtém o JSONObject "channel" dentro do resultsJSONObject
                JSONObject channelJSONObject = resultsJSONObject.getJSONObject("channel");


                // Obtém o JSONObject "item" dentro do channelJSONObject
                JSONObject itemJSONObject = channelJSONObject.getJSONObject("item");


                // Obtém o JSONObject "condition" dentro do itemJSONObject - Nesse objeto se encontra a temperatura e descricaol
                JSONObject conditionJSONObject = itemJSONObject.getJSONObject("condition");

                // Obtém as strings dentro de conditionJSONObject
                String temp = conditionJSONObject.getString("temp");
                descricao = conditionJSONObject.getString("text");

                Log.d("log_HTTP", "temp =" + temperatura + "  descricao= " + descricao);

                // Obtém o JSONObject "atmosphere" dentro do channelJSONObject - Nesse objeto se encontra a pressao e umidade
                JSONObject atmosphereJSONObject = channelJSONObject.getJSONObject("atmosphere");

                // Obtém as strings dentro de atmosphereJSONObject
                pressao = atmosphereJSONObject.getString("pressure");
                umidade = atmosphereJSONObject.getString("humidity");
                Log.d("log_HTTP", "pressao ="+pressao + "  umidade= " + umidade );


                // Converte ºF para ºC
                Double tempC = ((Integer.parseInt(temp) - 32)/ 1.8);
                // 2 casas depois da vírgula
                temperatura = String.format("%.2f", tempC);


            }catch (JSONException e){
                e.printStackTrace();
                Log.d("log_HTTP", "JSON exception");
            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);

            // Exibe as informações nos TextViews
            temperaturaTextView.setText(temperatura + " ºC" );
            descricaoTextView.setText(descricao);
            pressaoTextView.setText(pressao + " in");
            umidadeTextView.setText(umidade + " %");



        }


    }














    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_atualizar) {
            new MyAsyncTask().execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
