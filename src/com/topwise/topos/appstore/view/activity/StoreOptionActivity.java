package com.topwise.topos.appstore.view.activity;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.LeadingMarginSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.topwise.topos.appstore.R;
import com.topwise.topos.appstore.manager.ActivityManager;
import com.topwise.topos.appstore.view.ActionBarView;

public class StoreOptionActivity extends Activity {
    private ActionBarView mActionBarView;
    private TextView option_description;
    private TextView option_commit_count;
    private EditText option_commit;
    private EditText option_commit_contact;
    private Button button_commit;
    private InputMethodManager inputMethodManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_option);
        option_description=(TextView)findViewById(R.id.option_description);
        option_commit_count=(TextView)findViewById(R.id.option_commit_count);
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        option_commit=(EditText) findViewById(R.id.option_commit);
        option_commit_contact=(EditText) findViewById(R.id.option_commit_contact);
        button_commit=(Button) findViewById(R.id.button_commit);
        ActivityManager.addActivity(this);
        mActionBarView = (ActionBarView) findViewById(R.id.as_action_bar_layout);
        mActionBarView.setTitle(R.string.topwise_board_option);
        mActionBarView.setOnBackButtonClickListener(new ActionBarView.BackButtonClickListener() {
            @Override
            public void onBackBtnClicked(View v) {
                finish();
            }
        });
        String topwise_board_option_description=getResources().getString(R.string.topwise_board_option_description);
        SpannableString contentSpan = new SpannableString(topwise_board_option_description);
        LeadingMarginSpan.Standard standard = new LeadingMarginSpan.Standard(55, 0);
        contentSpan.setSpan(standard, 0, 0, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        option_description.setText(contentSpan);

        /**
         * 监听EditText框中的变化
         */
        option_commit.addTextChangedListener(new TextWatcher() {
            private CharSequence temp;
            private int editStart;
            private int editEnd;

            /**
             * 文本变化之前
             * @param s
             * @param start
             * @param count
             * @param after
             */
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                temp = s;
            }

            /**
             * 文本变化中
             * @param s
             * @param start
             * @param before
             * @param count
             */
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            /**
             * 文本变化之后
             * @param s
             */
            @Override
            public void afterTextChanged(Editable s) {
                editStart = option_commit.getSelectionStart();
                editEnd = option_commit.getSelectionEnd();
                option_commit_count.setText(temp.length()+"/"+100);
                if (temp.length() > 100) {//限制长度
                    Toast.makeText(StoreOptionActivity.this,
                            "输入的字数已经超过了限制！", Toast.LENGTH_SHORT)
                            .show();
                    s.delete(editStart - 1, editEnd);
                    int tempSelection = editStart;
                    option_commit.setText(s);
                    option_commit.setSelection(tempSelection);
                }

            }
        });

        button_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(option_commit_contact !=null && option_commit !=null) {
                    if(option_commit_contact.getText().length() != 0 &&option_commit.getText().length() != 0 ){
                        Toast.makeText(StoreOptionActivity.this, getResources().getString(R.string.topwise_board_option_button_commit_message), Toast.LENGTH_SHORT).show();
                        option_commit.setText(null);
                        option_commit_contact.setText(null);
                    }else {
                        Toast.makeText(StoreOptionActivity.this, getResources().getString(R.string.topwise_board_option_button_commit_message1), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    //打开输入法
    private void openKeyBoard(){
        if(inputMethodManager != null){
            if(inputMethodManager.isActive())
            inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
