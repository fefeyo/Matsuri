package com.fefeyo.matsuri;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Movie;
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
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.fefeyo.matsuri.item.MovieItem;
import com.fefeyo.matsuri.item.ResultAdapter;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;

/**
 * Dwangoはてなハッカソン
 * 2015/11/22
 */
public class MainActivity extends AppCompatActivity {

    /**
     * from=の後に秒数
     */
    final static String postUrl = "http://www.nicovideo.jp/watch/sm27491227?from=30";
    final static String END = "http://api.search.nicovideo.jp/api/v2/video/contents/search?";
    /**
     * 後にcontentIdをつけることで動画のページヘ
     */
    final static String VIDEO_URL = "http://nico.ms/";

    private AsyncHttpClient mClient;
    private ArrayList<MovieItem> mItems;

    private ProgressDialog dialog;

    @InjectView(R.id.search_text)
    EditText search_text;
    @InjectView(R.id.result_list)
    ListView result_list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);
        mClient = new AsyncHttpClient();

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

    /**
     * 動画を検索する
     */
    private void searchVideo(final AsyncHttpClient client, final String search_word) {
        dialog = new ProgressDialog(this);
        dialog.setMessage("検索中・・・");
        dialog.show();
        mItems = new ArrayList<>();
        client.get(
                getApplicationContext(),
                getNicoNicoQuery(search_word),
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        try {
                            final String result = new String(responseBody, "UTF-8");
                            final JSONObject parent = new JSONObject(result);
                            final JSONArray datas = parent.getJSONArray("data");
                            for(int i = 0; i < datas.length(); i++){
                                final JSONObject data = datas.getJSONObject(i);
                                final MovieItem item = new MovieItem();
                                item.setMovieUrl(VIDEO_URL + data.getString("contentId"));
                                item.setNumber(data.getString("viewCounter"));
                                item.setPostdate(data.getString("startTime"));
                                item.setThmbnailUrl(data.getString("thumbnailUrl"));
                                item.setTitle(data.getString("title"));
                                item.setComments(null);
                                mItems.add(item);
                            }
                            result_list.setAdapter(new ResultAdapter(
                                    getApplicationContext(),
                                    0,
                                    mItems
                            ));
                            dialog.dismiss();
                        } catch (UnsupportedEncodingException | JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.i("接続エラー", "接続エラー");
                        dialog.dismiss();
                    }
                }
        );
    }

    /**
     * ニコニコ横断検索API用URLを作成
     */
    private String getNicoNicoQuery(final String q) {
        String[] queryWords = new String[]{
                "q=" + q,
                "targets=title",
                "fields=contentId,title,description,viewCounter,startTime,thumbnailUrl",
                "_sort=-viewCounter",
                "_offset=0",
                "_limit=10",
                "_context=Matsuri"
        };
        final String searator = "&";

        final StringBuilder sb = new StringBuilder();
        for (String word : queryWords) {
            if (sb.length() > 0) {
                sb.append(searator);
            }
            sb.append(word);
        }
        String queryUrl = END + sb.toString();

        return queryUrl;
    }

    @OnClick(R.id.search_button)
    void onClick(final View v) {
        searchVideo(mClient, search_text.getText().toString());
    }
}
