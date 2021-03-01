package com.example.socialmedianetworkoperator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.squareup.picasso.Picasso;

import java.util.List;


public class StatusesAdapter extends ArrayAdapter<SocialMediaStatus> {

    private List<SocialMediaStatus> statusList;
    private final LayoutInflater inflater;
    private final int layoutResource;
    private static int counter = 0;
    private final ListView statusListView;
    private Boolean isFirstTime;


    public StatusesAdapter(@NonNull Context context, int resource, @NonNull List<SocialMediaStatus> objects, ListView listView) {
        super(context, resource, objects);
        statusList = objects;
        layoutResource = resource;
        inflater = LayoutInflater.from(context);
        statusListView = listView;
        isFirstTime = true;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        counter++;
        Log.d("ADAPTER", "get view in adapter just called. counter = " + counter);

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(layoutResource, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
            Log.w("VIEW_HOLDER", "View Holder Created");
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        SocialMediaStatus currentPost = statusList.get(position);

        viewHolder.username.setText(currentPost.getCreatorUsername() + "");
        viewHolder.name.setText(currentPost.getCreatorName() + "");
        viewHolder.date.setText(currentPost.getCreationDate() + "");
        viewHolder.bodyText.setText(currentPost.getStatusText() + "");
        Picasso.get().load(currentPost.getSocialMediaLogo()).into(viewHolder.logo);

        if (currentPost.getStatusImage() != null) {
            Picasso.get().load(currentPost.getStatusImage()).into(viewHolder.bodyImage);
            viewHolder.bodyImage.setVisibility(View.VISIBLE);
        } else {
            viewHolder.bodyImage.setVisibility(View.GONE);
        }

        return convertView;
    }

    private class ViewHolder {
        final TextView username;
        final TextView name;
        final TextView date;
        final TextView bodyText;
        final ImageView bodyImage;
        final ImageView logo;

        ViewHolder(View view) {
            name = view.findViewById(R.id.name);
            username = view.findViewById(R.id.username);
            date = view.findViewById(R.id.postCreationDate);
            bodyText = view.findViewById(R.id.statusText);
            logo = view.findViewById(R.id.socialMediaLogo);
            bodyImage = view.findViewById(R.id.postImage);
        }
    }

    @Nullable
    @Override
    public SocialMediaStatus getItem(int position) {
        return statusList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return statusList.size();
    }

    public synchronized void setData(List<SocialMediaStatus> list){
        if(isFirstTime) {
            this.statusList = list;
            statusListView.setAdapter(this);
            isFirstTime = false;
        }else{
            this.statusList.addAll(list);
            notifyDataSetChanged();
        }

    }
}