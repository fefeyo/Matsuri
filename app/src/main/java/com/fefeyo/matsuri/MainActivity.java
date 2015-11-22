package com.fefeyo.matsuri;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Dwangoはてなハッカソン
 * 2015/11/22
 */
public class MainActivity extends AppCompatActivity {

    /**
     * from=の後に秒数
     */
    final static String postUrl = "http://www.nicovideo.jp/watch/sm27491227?from=30";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(postUrl);
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(i);
            }
        });

        String s = "<thread thread=\"1431922032\" version=\"20061206\" res_from=\"-500\" />";
        AsyncHttpClient client = new AsyncHttpClient();

//        getComments(client, 1431922032, 12);
        getNicoInfo(client, "sm26283665");
    }

    private void getNicoInfo(AsyncHttpClient client, String sm) {
        client.addHeader("user_session", "user_session_1079501_483a0cfc79f2b319732854999d53370aacaf2bc9157089b2853eea745c4d670b");
        client.get("http://flapi.nicovideo.jp/api/getflv/" + sm, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    Log.v("log", new String(responseBody, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    private void getComments(AsyncHttpClient client, int thread, int id) {
        try {
            client.post(getApplicationContext(),
                    "http://msg.nicovideo.jp/" + id + "/api/",
                    new StringEntity("<thread thread=\"" + thread + "\" version=\"20061206\" res_from=\"-500\" />"),
                    "text/xml",
                    new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Log.v("log", "success");

                            try {
                                Document document = loadXMLFromString(new String(responseBody, "UTF-8"));
                                NodeList nodeList = document.getElementsByTagName("chat");
                                for (int i = 0; i < nodeList.getLength(); i++) {
                                    Log.v("log", nodeList.item(i).getTextContent());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.v("log", "parseError");
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                        }
                    }
            );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
