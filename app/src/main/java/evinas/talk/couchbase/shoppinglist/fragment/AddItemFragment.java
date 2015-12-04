package evinas.talk.couchbase.shoppinglist.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewEditorActionEvent;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import evinas.talk.couchbase.shoppinglist.R;
import evinas.talk.couchbase.shoppinglist.ShoppingListApplication;
import evinas.talk.couchbase.shoppinglist.eventbus.EventBus;
import evinas.talk.couchbase.shoppinglist.eventbus.event.ShoppingItem;
import evinas.talk.couchbase.shoppinglist.ws.GoogleImageResponse;
import evinas.talk.couchbase.shoppinglist.ws.GoogleImageService;
import evinas.talk.couchbase.shoppinglist.ws.Result;
import retrofit.JacksonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class AddItemFragment extends Fragment {

    private final static String EXTRA_IMG="item_img";
    private final static String EXTRA_COUNT="count";

    private int itemCount = 1;

    private Bitmap itemImage;

    private boolean searching;

    @Bind(R.id.layout_content)
    ViewGroup layoutContent;

    @Bind(R.id.progressBar)
    ProgressBar progressBar;

    @Bind(R.id.search)
    EditText name;

    @Bind(R.id.description)
    EditText description;

    @Bind(R.id.img_result)
    ImageView imgResult;

    @Bind(R.id.seekCount)
    SeekBar seekBar;

    @Bind(R.id.textViewCount)
    TextView textViewCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_item, null, false);

        ButterKnife.bind(this, view);

        if (savedInstanceState != null){
            setItemImage((Bitmap) savedInstanceState.getParcelable(EXTRA_IMG));
            itemCount = savedInstanceState.getInt(EXTRA_COUNT);
        }

        initializeSeekbar();

        initSearch();

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (itemImage != null){
            outState.putParcelable(EXTRA_IMG, itemImage);
            outState.putInt(EXTRA_COUNT, itemCount);
        }
    }

    private void initSearch() {
        // clear on empty

        subscribe(RxTextView.textChanges(name));
        subscribe(RxTextView.editorActions(name).filter(new Func1<Integer, Boolean>() {
            @Override
            public Boolean call(Integer integer) {
                return EditorInfo.IME_ACTION_SEARCH == integer;
            }
        }).map(new Func1<Integer, CharSequence>() {
            @Override
            public CharSequence call(Integer integer) {
                return name.getText();
            }
        }));

    }

    public void subscribe(Observable<CharSequence> observable){
        final int minLengthForRequest = 4;
        final GoogleImageService imageService = getImageService();
        observable.filter(new Func1<CharSequence, Boolean>() {
            @Override
            public Boolean call(CharSequence charSequence) {
                return !searching && charSequence.length()>=minLengthForRequest;
            }
        }).doOnNext(new Action1<CharSequence>() {
            @Override
            public void call(CharSequence charSequence) {
                searching = true;
            }
        })
                .debounce(200, TimeUnit.MILLISECONDS)
                .throttleLast(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence o) {
                        Log.i(ShoppingListApplication.TAG, "charsequence"+ o.length());
                        setItemImage(null);
                        progressBar.setVisibility(View.VISIBLE);
                        layoutContent.setVisibility(View.INVISIBLE);
                    }
                })
                .observeOn(Schedulers.io())
                .switchMap(new Func1<CharSequence, Observable<GoogleImageResponse>>() {
                    @Override
                    public Observable<GoogleImageResponse> call(CharSequence charSequence) {
                        Log.i(ShoppingListApplication.TAG, "function1");
                        return imageService.searchImage(charSequence.toString());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<GoogleImageResponse, List<Result>>() {
                    @Override
                    public List<Result> call(GoogleImageResponse r) {
                        return r != null && r.getResponseData() != null ? r.getResponseData().getResults() : null;
                    }
                }).filter(new Func1<List<Result>, Boolean>() {
            @Override
            public Boolean call(List<Result> res) {
                return res != null && !res.isEmpty();
            }
        })
                .map(new Func1<List<Result>, String>() {
                    @Override
                    public String call(List<Result> urls) {
                        return urls.get(0).getUrl();
                    }
                })
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        name.setText(null);
                        progressBar.setVisibility(View.INVISIBLE);
                        layoutContent.setVisibility(View.VISIBLE);
                        Log.e(ShoppingListApplication.TAG, "error" + e);
                        searching = false;
                    }

                    @Override
                    public void onNext(String url) {
                        progressBar.setVisibility(View.INVISIBLE);
                        layoutContent.setVisibility(View.VISIBLE);
                        insertImage(url);
                    }
                });
    }

    private void initializeSeekbar() {

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                itemCount = progresValue;
                textViewCount.setText(itemCount + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void insertImage(String url) {
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.i(ShoppingListApplication.TAG, "Picasso image loaded");
                setItemImage(bitmap);
                searching = false;
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.i(ShoppingListApplication.TAG, "Picasso image failed");
                itemImage = null;
                imgResult.setImageDrawable(errorDrawable);
                searching = false;
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                itemImage = null;
                imgResult.setImageDrawable(placeHolderDrawable);
            }
        };

        Picasso.with(AddItemFragment.this.getContext())
                .load(url)
                .resize(400,400)
                .centerCrop()
                .placeholder(R.drawable.abc_ab_share_pack_mtrl_alpha)
                .error(R.drawable.android_question)
                .into(target);
    }

    private void setItemImage(Bitmap bitmap){
        itemImage = bitmap;
        if (itemImage != null){
            imgResult.setImageBitmap(bitmap);
        }else{
            imgResult.setImageResource(R.drawable.abc_ab_share_pack_mtrl_alpha);
        }

    }

    private GoogleImageService getImageService() {
        OkHttpClient client = new OkHttpClient();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client.interceptors().add(interceptor);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://ajax.googleapis.com/ajax/services/")
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create()).build();
        return retrofit.create(GoogleImageService.class);
    }

    @OnClick(R.id.fab)
    public void onAddItem(){
        Log.i(ShoppingListApplication.TAG, "Event:onAddItem");
        if (TextUtils.isEmpty(name.getText())){
            Toast.makeText(getContext(), "Product name can not be empty", Toast.LENGTH_LONG).show();
        }else{
            // Add product
            ShoppingItem item = new ShoppingItem();
            item.setTitle(name.getText().toString());
            item.setDescription(!TextUtils.isEmpty(description.getText()) ? description.getText().toString() :null );
            item.setImage(itemImage);
            item.setNumber(itemCount);
            EventBus.post(item);
        }
    }
}
