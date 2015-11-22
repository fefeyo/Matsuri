package com.fefeyo.matsuri.item;

import android.content.Context;
import android.graphics.Color;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.echo.holographlibrary.Bar;
import com.echo.holographlibrary.BarGraph;
import com.fefeyo.matsuri.R;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import static butterknife.ButterKnife.findById;

/**
 * Created by USER on 2015/11/22.
 */
public class ResultAdapter extends ArrayAdapter<MovieItem> {

    private LayoutInflater inflater;

    public ResultAdapter(Context context, int resource, ArrayList<MovieItem> arr) {
        super(context, resource, arr);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) convertView = inflater.inflate(R.layout.result_row, null);

        final MovieItem item = getItem(position);
        final TextView title = findById(convertView, R.id.title);
        title.setText(item.getTitle());
        final TextView postdate = findById(convertView, R.id.postdate);
        postdate.setText(item.getPostdate());
        final TextView number = findById(convertView, R.id.number);
        number.setText("再生数："+item.getNumber());
        final ImageView thmbnail = findById(convertView, R.id.thmbnail);
        Picasso.with(getContext()).load(item.getThmbnailUrl()).into(thmbnail);
        final BarGraph graph = findById(convertView, R.id.comment_graph);
        buildGraph(graph, item.getComments());

        return convertView;
    }

    private void buildGraph(final BarGraph graph, final HashMap<Integer, ArrayList<String>> comments) {
        if (null != comments) {
            final ArrayList<Bar> bars = new ArrayList<>();
            int position = 0;
            for (int i = 0; i < comments.size(); i++) {
                final Bar bar = new Bar();
                bar.setColor(Color.RED);
                bar.setName(position + "秒");
                bar.setValue(comments.get(position).size());
                position += 30;
            }
            /**
             * Bar bar = new Bar();
             * bar.setColor
             * bar.setValue
             * bar.setName
             */
            graph.setBars(bars);
        } else {
            final ArrayList<Bar> bars = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                final Bar bar = new Bar();
                bar.setColor(Color.RED);
                bar.setName("さんぷる");
                bar.setValue(i * 20);
            }
            graph.setBars(bars);
        }
    }
}
