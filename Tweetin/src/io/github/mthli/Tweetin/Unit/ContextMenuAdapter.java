package io.github.mthli.Tweetin.Unit;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import io.github.mthli.Tweetin.R;

import java.util.List;

public class ContextMenuAdapter extends ArrayAdapter<String> {
    private Context context;
    private int layoutResId;
    private List<String> list;

    public ContextMenuAdapter(
            Context context,
            int layoutResId,
            List<String> list
    ) {
        super(context, layoutResId, list);

        this.context = context;
        this.layoutResId = layoutResId;
        this.list = list;
    }

    private class Holder {
        TextView menuItem;
    }

    @Override
    public View getView(
            final int position,
            final View convertView,
            ViewGroup viewGroup
    ) {
        Holder holder;
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            view = inflater.inflate(layoutResId, viewGroup, false);

            holder = new Holder();
            holder.menuItem = (TextView) view.findViewById(R.id.context_menu_item);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }

        String str = list.get(position);
        holder.menuItem.setText(str);

        return view;
    }
}