package mert.kadakal.deneme;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class HtmlArrayAdapter extends ArrayAdapter<String> {
    public HtmlArrayAdapter(Context context, int resource, List<String> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.textViewItem);
        String htmlText = getItem(position);
        textView.setText(Html.fromHtml(htmlText));

        return convertView;
    }
}
