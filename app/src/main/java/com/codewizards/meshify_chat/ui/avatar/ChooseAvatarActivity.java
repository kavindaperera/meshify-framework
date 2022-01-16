package com.codewizards.meshify_chat.ui.avatar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.appcompat.app.AppCompatActivity;

import com.codewizards.meshify_chat.R;

public class ChooseAvatarActivity extends AppCompatActivity {

    //used for the table layout
    private static final int ROWS = 9;
    private static final int COLUMNS = 3;

    private Button mSaveButton;
    private ImageButton mSelectedAvatar;
    private int mUserAvatarId = R.mipmap.ic_launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_avatar);

        setupAvatars();

        mSaveButton = findViewById(R.id.saveUserAvatarButton);
        mSelectedAvatar = findViewById(R.id.choose_avatar_selected_avatar);

    }

    private void setupAvatars() {
        int avatarNum = 1;

        ScrollView scrollView = findViewById(R.id.avatarScrollView);
        TableLayout tableLayout = new TableLayout(this);

        for (int r = 0; r < ROWS; r++) {
            TableRow tableRow = (TableRow) getLayoutInflater()
                    .inflate(R.layout.table_row_choose_avatar, tableLayout, false);

            for (int c = 0; c < COLUMNS; c++) {
                final ImageButton imageButton = (ImageButton) tableRow.getChildAt(c);
                final int id = getResources().getIdentifier("avatar" + avatarNum, "mipmap", getPackageName());
                imageButton.setImageResource(id);

                imageButton.setOnClickListener(v -> {
                    mUserAvatarId = id;
                    mSelectedAvatar.setImageResource(mUserAvatarId);
                    mSaveButton.setClickable(true);
                });
                avatarNum++;
            }
            tableLayout.addView(tableRow);
        }
        scrollView.addView(tableLayout);

    }

    public void saveAvatar(View view) {
        finish();
    }
}