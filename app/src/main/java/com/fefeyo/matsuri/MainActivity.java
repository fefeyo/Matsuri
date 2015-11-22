package com.fefeyo.matsuri;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.fefeyo.matsuri.item.MovieItem;
import com.fefeyo.matsuri.item.ResultAdapter;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.cookie.BasicClientCookie;

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

    int[] thread = {1431922032, 1444496827, 1435833374};
    int[] id = {12, 0, 60};
    int[][] vpos = new int[10][500];
    int[][] vposss = new int[10][10];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);
        mClient = new AsyncHttpClient();
    }

    private void getNicoInfo(AsyncHttpClient client, String sm) {
        PersistentCookieStore cookieStore = new PersistentCookieStore(this);
        BasicClientCookie newCookie = new BasicClientCookie("user_session", "user_session_1079501_483a0cfc79f2b319732854999d53370aacaf2bc9157089b2853eea745c4d670b");
        cookieStore.addCookie(newCookie);
        client.setCookieStore(cookieStore);
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

    private void getComments(AsyncHttpClient client) throws UnsupportedEncodingException {
            for (int i = 0; i < thread.length; i++) {
                if (id[i] == 0) {
                    getCommentsTask(client, "http://nmsg.nicovideo.jp/api/", new StringEntity("<thread thread=\"" + thread[i] + "\" version=\"20061206\" res_from=\"-500\" />"), i);
                } else {
                    getCommentsTask(client, "http://msg.nicovideo.jp/" + id[i] + "/api/", new StringEntity("<thread thread=\"" + thread[i] + "\" version=\"20061206\" res_from=\"-500\" />"), i);
                }
            }
    }

    private void getCommentsTask(AsyncHttpClient client, String url, StringEntity se, final int index) {
        try {
            client.post(getApplicationContext(), url, se, "text/xml",
                    new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Log.v("log", "success");

                            try {
                                Document document = loadXMLFromString(new String(responseBody, "UTF-8"));
                                NodeList nodeList = document.getElementsByTagName("chat");
                                int max = 0;
                                for (int i = 0; i < nodeList.getLength(); i++) {
                                    String s = nodeList.item(i).getAttributes().getNamedItem("vpos").getTextContent();
                                    if (s.replaceAll("[^0-9]","").equals("")) continue;
                                    vpos[index][i] = Integer.parseInt(s.replaceAll("[^0-9]",""));
                                    if (max < vpos[index][i]) {
                                        max = vpos[index][i];
                                    }
                                }
                                max /= 10;
                                for (int i = 0; i < nodeList.getLength(); i++) {
                                    if (vpos[index][i]/max == 10) continue;
                                    vposss[index][vpos[index][i]/max]++;
                                }
                                Log.v("log", index + "ばんめ");
                                Log.v("log", vposss[index][0] + "");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                                Log.v("log", e.getMessage());
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.v("log", e.getMessage());
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                        }
                    }
            );
        } catch (Exception e) {
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
                            for (int i = 0; i < datas.length(); i++) {
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
        try {
            getComments(mClient);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        getNicoInfo(mClient, "sm26283665");
    }
}
