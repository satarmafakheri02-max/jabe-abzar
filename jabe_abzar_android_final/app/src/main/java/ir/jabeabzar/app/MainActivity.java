package ir.jabeabzar.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.*;
import java.io.*;
import java.util.*;
import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class MainActivity extends Activity {
    WebView webView; ValueCallback<Uri[]> fileCallback; static final int FILE_PICKER=42;
    @Override public void onCreate(Bundle b){super.onCreate(b); getWindow().setStatusBarColor(Color.rgb(244,247,251));
        webView=new WebView(this); setContentView(webView); WebSettings s=webView.getSettings();
        s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setAllowFileAccess(true); s.setAllowContentAccess(true);
        s.setBuiltInZoomControls(false); s.setDisplayZoomControls(false); s.setMediaPlaybackRequiresUserGesture(false);
        webView.setBackgroundColor(Color.rgb(244,247,251)); webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new NativeBridge(),"Android");
        webView.setWebChromeClient(new WebChromeClient(){@Override public boolean onShowFileChooser(WebView v,ValueCallback<Uri[]> cb,FileChooserParams p){
            fileCallback=cb; Intent i=p.createIntent(); i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true); try{startActivityForResult(i,FILE_PICKER);return true;}catch(Exception e){fileCallback=null;return false;}}});
        webView.loadUrl("file:///android_asset/index.html");
    }
    @Override protected void onActivityResult(int r,int c,Intent d){if(r==FILE_PICKER&&fileCallback!=null){Uri[] out=null;if(c==RESULT_OK&&d!=null){ArrayList<Uri> a=new ArrayList<>();if(d.getClipData()!=null)for(int i=0;i<d.getClipData().getItemCount();i++)a.add(d.getClipData().getItemAt(i).getUri());else if(d.getData()!=null)a.add(d.getData());out=a.toArray(new Uri[0]);}fileCallback.onReceiveValue(out);fileCallback=null;}super.onActivityResult(r,c,d);}
    @Override public void onBackPressed(){if(webView.canGoBack())webView.goBack();else super.onBackPressed();}

    public class NativeBridge {
        @JavascriptInterface public void shareText(String text){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,text);startActivity(Intent.createChooser(i,"بفرستش برای کی؟ 😄"));}
        @JavascriptInterface public String makeQr(String text,int size){try{BitMatrix m=new QRCodeWriter().encode(text,BarcodeFormat.QR_CODE,size,size);Bitmap b=Bitmap.createBitmap(size,size,Bitmap.Config.ARGB_8888);for(int y=0;y<size;y++)for(int x=0;x<size;x++)b.setPixel(x,y,m.get(x,y)?Color.BLACK:Color.WHITE);File f=new File(getCacheDir(),"qr_"+System.currentTimeMillis()+".png");try(FileOutputStream o=new FileOutputStream(f)){b.compress(Bitmap.CompressFormat.PNG,100,o);}return Uri.fromFile(f).toString();}catch(Exception e){return "";}}
        @JavascriptInterface public void openShareFile(String path,String mime){try{Uri u=Uri.parse(path);Intent i=new Intent(Intent.ACTION_SEND);i.setType(mime);i.putExtra(Intent.EXTRA_STREAM,u);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"بفرستش 😄"));}catch(Exception ignored){}}
    }
}
