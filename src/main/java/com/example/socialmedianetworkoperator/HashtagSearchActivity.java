package com.example.socialmedianetworkoperator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class HashtagSearchActivity extends AppCompatActivity {

    private final static String HASHTAG_LOG = "HashtagSearch";
    private TwitterUser twitterUser;
    private FacebookPage facebookPage;
    private ArrayList<String> hashtagTrendsList = new ArrayList<>();
    TextView selectedHashtagTextView;
    ListView statusesListView;
    Button searchByTrendsBtn, searchByTypedBtn;
    AutoCompleteTextView trendsAutoCompleteTextView, typedByUserAutoCompleteTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hashtag_search);

        searchByTrendsBtn = findViewById(R.id.searchByTrendHashtagButton);
        searchByTypedBtn = findViewById(R.id.searchByWrittenHashtagButton);
        trendsAutoCompleteTextView = findViewById(R.id.trendHashtagAutoComplTV);
        typedByUserAutoCompleteTextView = findViewById(R.id.writtenHashtagAutoComplTV);
        selectedHashtagTextView = findViewById(R.id.selectedHashtagTextView);
        statusesListView = findViewById(R.id.statusesListView);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);



        twitterUser = new TwitterUser();
        facebookPage = new FacebookPage();

        Intent intent = getIntent();

        //Intent coming from MainActivity.class
        if(intent.getStringExtra("Source").equals("MainActivity.class")){
            twitterUser = (TwitterUser) intent.getSerializableExtra("TwitterUser");
            facebookPage = (FacebookPage) intent.getSerializableExtra("FacebookPage");
            new TwitterTrendHashtagsRequest(HashtagSearchActivity.this, twitterUser, facebookPage).execute();
        }
        else {        //Intent coming from TwitterTrendHashtagsRequest.class
            hashtagTrendsList = intent.getStringArrayListExtra("HashtagTrends");
            twitterUser = (TwitterUser) intent.getSerializableExtra("TwitterUser");
            facebookPage = (FacebookPage) intent.getSerializableExtra("FacebookPage");
        }



        ArrayList<String> twitterHashtagsList = new ArrayList<String>();
        twitterHashtagsList.add("Capitol");
        twitterHashtagsList.add("TheVoiceGR");
        twitterHashtagsList.add("capitolRiots");
        twitterHashtagsList.add("snow");
        twitterHashtagsList.add("κορονοιός");

        ArrayAdapter<String> trendsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, hashtagTrendsList);
        ArrayAdapter<String> typedByUserAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, twitterHashtagsList);

        trendsAutoCompleteTextView.setAdapter(trendsAdapter);
        trendsAutoCompleteTextView.setThreshold(0);

        typedByUserAutoCompleteTextView.setAdapter(typedByUserAdapter);
        typedByUserAutoCompleteTextView.setThreshold(0);

        trendsAutoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    trendsAutoCompleteTextView.showDropDown();
                }
        });

        View.OnClickListener trendsButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StatusesAdapter statusesAdapter = newStatusesAdapter();
                makeProceduresForSelectedHashtag(statusesAdapter,trendsAutoCompleteTextView,hashtagTrendsList);
            }
        };

        View.OnClickListener typedByUserButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StatusesAdapter statusesAdapter = newStatusesAdapter();
                makeProceduresForSelectedHashtag(statusesAdapter,typedByUserAutoCompleteTextView,twitterHashtagsList);
            }
        };
        searchByTrendsBtn.setOnClickListener(trendsButtonListener);
        searchByTypedBtn.setOnClickListener(typedByUserButtonListener);

        }

        private StatusesAdapter newStatusesAdapter(){
            return new StatusesAdapter(this,
                            R.layout.statuses_list_record,
                            new ArrayList<SocialMediaStatus>(),
                            statusesListView);
        }


        private void makeProceduresForSelectedHashtag(StatusesAdapter statusesAdapter,AutoCompleteTextView autoCompleteTextView,ArrayList<String> hashtagsList){
            selectedHashtagTextView.setText(autoCompleteTextView.getText());
            String selectedHashtag = selectedHashtagTextView.getText().toString();
            if (hashtagsList.contains(selectedHashtag)) {
                Log.d(HASHTAG_LOG, "Selected Hashtag is: " + selectedHashtag);

                new GetTweetsRequest(statusesAdapter,twitterUser).execute(selectedHashtag);
                new GetInstagramStatusesRequest(statusesAdapter,"recent_media",facebookPage).execute(selectedHashtag);
                new GetInstagramStatusesRequest(statusesAdapter,"top_media",facebookPage).execute(selectedHashtag);
                new GetFacebookStatusesRequest(statusesAdapter,facebookPage).execute(selectedHashtag);

                statusesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        SocialMediaStatus selectedStatus = (SocialMediaStatus) statusesListView.getItemAtPosition(position);

                        Intent intent = new Intent(view.getContext(), StatusInfoActivity.class);
                        intent.putExtra("SelectedStatus", selectedStatus);
                        intent.putExtra("TwitterUser", twitterUser);
                        startActivity(intent);
                    }
                });
            } else {
                Toast.makeText(HashtagSearchActivity.this, "Choose a hashtag that exists in the list!", Toast.LENGTH_SHORT).show();
                Log.d(HASHTAG_LOG,"The list does not contain the selected hashtag!");
            }
        }
}