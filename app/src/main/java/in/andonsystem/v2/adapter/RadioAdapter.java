package in.andonsystem.v2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.List;

import in.andonsystem.R;

/**
 * Created by razamd on 4/2/2017.
 */

public class RadioAdapter extends ArrayAdapter<String> {

    private int mSelectedItem;

    public RadioAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        View view = convertView;
        if(view==null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.radio_adapter_item,parent,false);
        }
        String value = getItem(position);

        TextView name = (TextView) view.findViewById(R.id.name);
        RadioButton radio = (RadioButton) view.findViewById(R.id.radio);

        name.setText(value);
        if(position == mSelectedItem) radio.setChecked(true);
        else radio.setChecked(false);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                mSelectedItem = position;
                RadioAdapter.this.notifyDataSetChanged();
            }
        });
        return view;
    }
}
