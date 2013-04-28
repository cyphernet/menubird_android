package com.menubird;

import android.webkit.JavascriptInterface;

public class WebAppInterface {
	MainActivity  mContext;

    /** Instantiate the interface and set the context */
    WebAppInterface(MainActivity c) {
        mContext = c;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void take_pic() {
    	this.mContext.take_picture();
    }

}