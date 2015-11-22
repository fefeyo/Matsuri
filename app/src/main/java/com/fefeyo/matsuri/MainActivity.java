package com.fefeyo.matsuri;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Movie;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
    @InjectView(R.id.search_button)
    BootstrapButton search_button;
    @InjectView(R.id.result_list)
    ListView result_list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        mClient = new AsyncHttpClient();
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
