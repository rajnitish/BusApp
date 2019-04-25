package com.nitish.busapp;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;


public class AboutUsActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us);
        final TextView mytext = (TextView) findViewById(R.id.text_view_id);

        Spanned htmlAsSpanned = Html.fromHtml("<p style=\"text-align: justify;\"><span style=\"color: #ff0000;\">&nbsp;</span></p>\n" +
                "<h3 class=\"post-title entry-title\" style=\"text-align: justify;\"><a href=\"http://horizonmediaplayer.blogspot.com/2009/05/acknowledgements.html\">A &amp; A (About Us &amp; Acknowledgements)</a></h3>\n" +
                "<div class=\"post-header\" style=\"text-align: justify;\">&nbsp;</div>\n" +
                "<div id=\"post-body-979479577280981864\" class=\"post-body entry-content\" style=\"text-align: justify;\">It is a great pleasure to thank the giants on whose shoulders we stand. First and foremost, we would like to express our deep sense of gratitude to respected&nbsp;<span style=\"color: #0000ff;\"><strong>Prof S.C. Gupta</strong></span> for his comprehensive help and encouragement towards the development of this project. He motivated us to take the project forward and provided all the necessary resources and expertise for this purpose. He helped us by closely monitoring the progress and encouraged us to do research to incorporate as many innovative features as possible.</div>\n" +
                "<div class=\"post-body entry-content\" style=\"text-align: justify;\">&nbsp;</div>\n" +
                "<div class=\"post-body entry-content\" style=\"text-align: justify;\">We are a team of four. Designed and developed this android app \"<strong>BusApp</strong>\". It is designed for helping daily Delhi bus commuter who can travels hassle-free. Information pertaining to the bus, its driver, feedback section, all of these features have been incorporated in this app.</div>\n" +
                "<div id=\"post-body-979479577280981864\" class=\"post-body entry-content\" style=\"text-align: justify;\"><br />Last but not least we would like to thank our friends, family members and all other staff members of our department for their constant support and encouragement.<br />Bus App Team Gp # 16.</div>\n" +
                "<div class=\"post-body entry-content\" style=\"text-align: justify;\">&nbsp;</div>\n" +
                "<div class=\"post-body entry-content\" style=\"text-align: justify;\"><strong>Contact Info</strong></div>\n" +
                "<div class=\"post-body entry-content\" style=\"text-align: justify;\"><span style=\"color: #008000;\"><strong>Mail us&nbsp; &nbsp; &nbsp;[raj[dot]nitp@gmail.com]</strong></span></div>\n" +
                "<div class=\"post-body entry-content\" style=\"text-align: justify;\"><span style=\"color: #008000;\"><strong>Phone us&nbsp; [+91-7827779299]</strong></span></div>\n" +
                "<div class=\"post-body entry-content\" style=\"text-align: justify;\"><span style=\"color: #008000;\"><strong>Follow us on youtube [https://www.youtube.com/TheNitishRaj]</strong></span></div>" ); // used by TextView

        mytext.setText(htmlAsSpanned);


        int finishTime = 15; //10 secs
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, finishTime * 1000);
    }
}
