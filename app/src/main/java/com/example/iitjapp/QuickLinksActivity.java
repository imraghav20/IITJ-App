package com.example.iitjapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

public class QuickLinksActivity extends AppCompatActivity {

    private TextView aryabhatta, new_aryabhatta, online_fees, library, nptel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_links);

        aryabhatta = (TextView) findViewById(R.id.aryabhatta);
        new_aryabhatta = (TextView) findViewById(R.id.new_aryabhatta);
        online_fees = (TextView) findViewById(R.id.online_fees);
        library = (TextView) findViewById(R.id.library);
        nptel = (TextView) findViewById(R.id.nptel);

        aryabhatta.setMovementMethod(LinkMovementMethod.getInstance());
        new_aryabhatta.setMovementMethod(LinkMovementMethod.getInstance());
        online_fees.setMovementMethod(LinkMovementMethod.getInstance());
        library.setMovementMethod(LinkMovementMethod.getInstance());
        nptel.setMovementMethod(LinkMovementMethod.getInstance());

        stripUnderlines(aryabhatta);
        stripUnderlines(new_aryabhatta);
        stripUnderlines(online_fees);
        stripUnderlines(library);
        stripUnderlines(nptel);
    }

    private void stripUnderlines(TextView textView) {
        Spannable s = new SpannableString(textView.getText());
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span: spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s);
    }

    private class URLSpanNoUnderline extends URLSpan {
        public URLSpanNoUnderline(String url) {
            super(url);
        }
        @Override public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }
    }

}
